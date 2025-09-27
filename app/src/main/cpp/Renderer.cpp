#include "Renderer.h"

#include <game-activity/native_app_glue/android_native_app_glue.h>
#include <GLES3/gl3.h>
#include <jni.h>
#include <algorithm>
#include <array>
#include <cmath>
#include <memory>
#include <utility>
#include <vector>
#include <android/imagedecoder.h>

#include "AndroidOut.h"
#include "Shader.h"
#include "Utility.h"
#include "TextureAsset.h"

//! executes glGetString and outputs the result to logcat
#define PRINT_GL_STRING(s) {aout << #s": "<< glGetString(s) << std::endl;}

/*!
 * @brief if glGetString returns a space separated list of elements, prints each one on a new line
 *
 * This works by creating an istringstream of the input c-style string. Then that is used to create
 * a vector -- each element of the vector is a new element in the input string. Finally a foreach
 * loop consumes this and outputs it to logcat using @a aout
 */
#define PRINT_GL_STRING_AS_LIST(s) { \
std::istringstream extensionStream((const char *) glGetString(s));\
std::vector<std::string> extensionList(\
        std::istream_iterator<std::string>{extensionStream},\
        std::istream_iterator<std::string>());\
aout << #s":\n";\
for (auto& extension: extensionList) {\
    aout << extension << "\n";\
}\
aout << std::endl;\
}

//! Color for cornflower blue. Can be sent directly to glClearColor
#define CORNFLOWER_BLUE 100 / 255.f, 149 / 255.f, 237 / 255.f, 1

// Vertex shader, you'd typically load this from assets
static const char *vertex = R"vertex(#version 300 es
in vec3 inPosition;
in vec2 inUV;

out vec2 fragUV;

uniform mat4 uProjection;

void main() {
    fragUV = inUV;
    gl_Position = uProjection * vec4(inPosition, 1.0);
}
)vertex";

// Fragment shader, you'd typically load this from assets
static const char *fragment = R"fragment(#version 300 es
precision mediump float;

in vec2 fragUV;

uniform sampler2D uTexture;

out vec4 outColor;

void main() {
    outColor = texture(uTexture, fragUV);
}
)fragment";

/*!
 * Half the height of the projection matrix. This gives you a renderable area of height 4 ranging
 * from -2 to 2
 */
static constexpr float kProjectionHalfHeight = 2.f;

/*!
 * The near plane distance for the projection matrix. Since this is an orthographic projection
 * matrix, it's convenient to have negative values for sorting (and avoiding z-fighting at 0).
 */
static constexpr float kProjectionNearPlane = -1.f;

/*!
 * The far plane distance for the projection matrix. Since this is an orthographic porjection
 * matrix, it's convenient to have the far plane equidistant from 0 as the near plane.
 */
static constexpr float kProjectionFarPlane = 1.f;

static constexpr int kBoardRows = 8;
static constexpr int kBoardColumns = 5;
static constexpr float kGemVisualScale = 0.8f;

static constexpr float kBoardPixelWidth = 1022.f;
static constexpr float kBoardPixelHeight = 1535.f;
static constexpr float kBoardMarginLeftPx = 55.f;
static constexpr float kBoardMarginRightPx = 50.f;
static constexpr float kBoardMarginTopPx = 80.f;
static constexpr float kBoardMarginBottomPx = 80.f;

static constexpr float kBoardMarginScale = 0.85f;
static constexpr float kResultBannerWidthScale = 0.6f;

static constexpr int kRedMatchDamage = 10;
static constexpr int kBlueMatchHeal = 5;
static constexpr int kGreenMatchMana = 10;
static constexpr int kSkullMatchDamage = 10;
static constexpr float kSkullSpawnChance = 0.1f;

Renderer::~Renderer() {
    if (display_ != EGL_NO_DISPLAY) {
        eglMakeCurrent(display_, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (context_ != EGL_NO_CONTEXT) {
            eglDestroyContext(display_, context_);
            context_ = EGL_NO_CONTEXT;
        }
        if (surface_ != EGL_NO_SURFACE) {
            eglDestroySurface(display_, surface_);
            surface_ = EGL_NO_SURFACE;
        }
        eglTerminate(display_);
        display_ = EGL_NO_DISPLAY;
    }
}

void Renderer::render() {
    // Check to see if the surface has changed size. This is _necessary_ to do every frame when
    // using immersive mode as you'll get no other notification that your renderable area has
    // changed.
    updateRenderArea();

    auto now = std::chrono::steady_clock::now();
    float deltaTime = std::chrono::duration<float>(now - lastFrameTime_).count();
    lastFrameTime_ = now;
    if (deltaTime < 0.0f) {
        deltaTime = 0.0f;
    }
    if (deltaTime > 0.1f) {
        deltaTime = 0.1f;
    }

    ensureBoardInitialized();
    if (updateBoardState()) {
        sceneDirty_ = true;
    }

    updateRuneAnimation(deltaTime);

    // When the renderable area changes, the projection matrix has to also be updated. This is true
    // even if you change from the sample orthographic projection matrix as your aspect ratio has
    // likely changed.
    if (shaderNeedsNewProjectionMatrix_) {
        // a placeholder projection matrix allocated on the stack. Column-major memory layout
        float projectionMatrix[16] = {0};

        // build an orthographic projection matrix for 2d rendering
        Utility::buildOrthographicMatrix(
                projectionMatrix,
                kProjectionHalfHeight,
                float(width_) / height_,
                kProjectionNearPlane,
                kProjectionFarPlane);

        // send the matrix to the shader
        // Note: the shader must be active for this to work. Since we only have one shader for this
        // demo, we can assume that it's active.
        shader_->setProjectionMatrix(projectionMatrix);

        // make sure the matrix isn't generated every frame
        shaderNeedsNewProjectionMatrix_ = false;
    }

    if (sceneDirty_) {
        createModels();
    }

    // clear the color buffer
    glClear(GL_COLOR_BUFFER_BIT);

    // Render all the models. There's no depth testing in this sample so they're accepted in the
    // order provided. But the sample EGL setup requests a 24 bit depth buffer so you could
    // configure it at the end of initRenderer
    if (!models_.empty()) {
        for (const auto &model: models_) {
            shader_->drawModel(model);
        }
    }

    // Present the rendered image. This is an implicit glFlush.
    auto swapResult = eglSwapBuffers(display_, surface_);
    assert(swapResult == EGL_TRUE);
}

void Renderer::initRenderer() {
    // Choose your render attributes
    constexpr EGLint attribs[] = {
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_DEPTH_SIZE, 24,
            EGL_NONE
    };

    // The default display is probably what you want on Android
    auto display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    eglInitialize(display, nullptr, nullptr);

    // figure out how many configs there are
    EGLint numConfigs;
    eglChooseConfig(display, attribs, nullptr, 0, &numConfigs);

    // get the list of configurations
    std::unique_ptr<EGLConfig[]> supportedConfigs(new EGLConfig[numConfigs]);
    eglChooseConfig(display, attribs, supportedConfigs.get(), numConfigs, &numConfigs);

    // Find a config we like.
    // Could likely just grab the first if we don't care about anything else in the config.
    // Otherwise hook in your own heuristic
    auto config = *std::find_if(
            supportedConfigs.get(),
            supportedConfigs.get() + numConfigs,
            [&display](const EGLConfig &config) {
                EGLint red, green, blue, depth;
                if (eglGetConfigAttrib(display, config, EGL_RED_SIZE, &red)
                    && eglGetConfigAttrib(display, config, EGL_GREEN_SIZE, &green)
                    && eglGetConfigAttrib(display, config, EGL_BLUE_SIZE, &blue)
                    && eglGetConfigAttrib(display, config, EGL_DEPTH_SIZE, &depth)) {

                    aout << "Found config with " << red << ", " << green << ", " << blue << ", "
                         << depth << std::endl;
                    return red == 8 && green == 8 && blue == 8 && depth == 24;
                }
                return false;
            });

    aout << "Found " << numConfigs << " configs" << std::endl;
    aout << "Chose " << config << std::endl;

    // create the proper window surface
    EGLint format;
    eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format);
    EGLSurface surface = eglCreateWindowSurface(display, config, app_->window, nullptr);

    // Create a GLES 3 context
    EGLint contextAttribs[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    EGLContext context = eglCreateContext(display, config, nullptr, contextAttribs);

    // get some window metrics
    auto madeCurrent = eglMakeCurrent(display, surface, surface, context);
    assert(madeCurrent);

    display_ = display;
    surface_ = surface;
    context_ = context;

    // make width and height invalid so it gets updated the first frame in @a updateRenderArea()
    width_ = -1;
    height_ = -1;

    PRINT_GL_STRING(GL_VENDOR);
    PRINT_GL_STRING(GL_RENDERER);
    PRINT_GL_STRING(GL_VERSION);
    PRINT_GL_STRING_AS_LIST(GL_EXTENSIONS);

    shader_ = std::unique_ptr<Shader>(
            Shader::loadShader(vertex, fragment, "inPosition", "inUV", "uProjection"));
    assert(shader_);

    // Note: there's only one shader in this demo, so I'll activate it here. For a more complex game
    // you'll want to track the active shader and activate/deactivate it as necessary
    shader_->activate();

    // setup any other gl related global states
    glClearColor(CORNFLOWER_BLUE);

    // enable alpha globally for now, you probably don't want to do this in a game
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

}

void Renderer::updateRenderArea() {
    EGLint width;
    eglQuerySurface(display_, surface_, EGL_WIDTH, &width);

    EGLint height;
    eglQuerySurface(display_, surface_, EGL_HEIGHT, &height);

    if (width != width_ || height != height_) {
        width_ = width;
        height_ = height;
        glViewport(0, 0, width, height);

        // make sure that we lazily recreate the projection matrix before we render
        shaderNeedsNewProjectionMatrix_ = true;

        // Regenerate the scene so the board stays centered when the viewport changes
        sceneDirty_ = true;
        boardGeometryValid_ = false;
    }
}

/**
 * @brief Create any demo models we want for this demo.
 */
void Renderer::createModels() {
    if (width_ <= 0 || height_ <= 0 || !boardReady_) {
        return;
    }

    auto assetManager = app_->activity->assetManager;
    if (!spBoardTexture_) {
        spBoardTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/board.png");
        if (!spBoardTexture_) {
            spBoardTexture_ = TextureAsset::createSolidColorTexture(40, 40, 52, 255);
        }
    }

    if (!spRedGemTexture_) {
        spRedGemTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/red_gem.png");
        if (!spRedGemTexture_) {
            spRedGemTexture_ = TextureAsset::createSolidColorTexture(200, 40, 60, 255);
        }
    }

    if (!spGreenGemTexture_) {
        spGreenGemTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/green_gem.png");
        if (!spGreenGemTexture_) {
            spGreenGemTexture_ = TextureAsset::createSolidColorTexture(60, 200, 100, 255);
        }
    }

    if (!spBlueGemTexture_) {
        spBlueGemTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/blue_gem.png");
        if (!spBlueGemTexture_) {
            spBlueGemTexture_ = TextureAsset::createSolidColorTexture(60, 120, 200, 255);
        }
    }

    if (!spSkullGemTexture_) {
        spSkullGemTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/skull.png");
        if (!spSkullGemTexture_) {
            spSkullGemTexture_ = TextureAsset::createSolidColorTexture(220, 220, 220, 255);
        }
    }

    if (!spHeroTexture_) {
        spHeroTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/elf.png");
        if (!spHeroTexture_) {
            spHeroTexture_ = TextureAsset::createSolidColorTexture(120, 200, 120, 255);
        }
    }

    if (!spEnemyTexture_) {
        spEnemyTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/black_wizard.png");
        if (!spEnemyTexture_) {
            spEnemyTexture_ = TextureAsset::createSolidColorTexture(40, 40, 40, 255);
        }
    }

    if (!spWhiteTexture_) {
        spWhiteTexture_ = TextureAsset::loadAsset(assetManager, "puzzle/white.png");
        if (!spWhiteTexture_) {
            spWhiteTexture_ = TextureAsset::createSolidColorTexture(255, 255, 255, 255);
        }
    }

    models_.clear();

    const float worldHeight = kProjectionHalfHeight * 2.0f;
    const float worldWidth = worldHeight * (static_cast<float>(width_) / static_cast<float>(height_));
    const float maxBoardWidth = worldWidth * kBoardMarginScale;
    const float maxBoardHeight = worldHeight * kBoardMarginScale;
    const float boardPixelWidth = static_cast<float>(spBoardTexture_->getWidth());
    const float boardPixelHeight = static_cast<float>(spBoardTexture_->getHeight());
    const float pixelToWorld = std::min(maxBoardWidth / boardPixelWidth,
                                        maxBoardHeight / boardPixelHeight);
    boardPixelToWorld_ = pixelToWorld;
    const float boardDrawWidth = boardPixelWidth * pixelToWorld;
    const float boardDrawHeight = boardPixelHeight * pixelToWorld;
    const float boardCenterX = 0.0f;
    const float boardCenterY = 0.0f;
    const float originX = boardCenterX - boardDrawWidth * 0.5f;
    const float originY = boardCenterY + boardDrawHeight * 0.5f;
    boardLeft_ = originX;
    boardRight_ = originX + boardDrawWidth;
    boardTop_ = originY;
    boardBottom_ = originY - boardDrawHeight;
    const float boardWidth = boardDrawWidth;
    const float boardHeight = boardDrawHeight;

    models_.emplace_back(buildQuadModel(boardLeft_,
                                        boardTop_,
                                        boardRight_,
                                        boardBottom_,
                                        -0.2f,
                                        spBoardTexture_));

    const float boardScale = pixelToWorld;
    const float marginLeft = kBoardMarginLeftPx * boardScale;
    const float marginRight = kBoardMarginRightPx * boardScale;
    const float marginTop = kBoardMarginTopPx * boardScale;
    const float marginBottom = kBoardMarginBottomPx * boardScale;
    const float innerWidth = boardDrawWidth - marginLeft - marginRight;
    const float innerHeight = boardDrawHeight - marginTop - marginBottom;
    const bool geometryPreviouslyValid = boardGeometryValid_;
    const float cellWidth = innerWidth / static_cast<float>(kBoardColumns);
    const float cellHeight = innerHeight / static_cast<float>(kBoardRows);

    gridLeft_ = originX + marginLeft;
    gridTop_ = originY - marginTop;
    gridRight_ = gridLeft_ + innerWidth;
    gridBottom_ = gridTop_ - innerHeight;
    cellWidth_ = cellWidth;
    cellHeight_ = cellHeight;
    boardGeometryValid_ = true;

    const float gemSize = std::min(cellWidth, cellHeight) * kGemVisualScale;
    const float gemHalfWidth = gemSize * 0.5f;
    const float gemHalfHeight = gemSize * 0.5f;

    updateAllRuneTargets(!geometryPreviouslyValid);

    for (int row = 0; row < kBoardRows; ++row) {
        for (int col = 0; col < kBoardColumns; ++col) {
            const Rune &rune = runeAt(row, col);
            if (rune.type == GemType::None) {
                continue;
            }

            auto texture = textureForGem(rune.type);
            if (!texture) {
                continue;
            }

            float gemCenterX = rune.currentX;
            float gemCenterY = rune.currentY;

            if (!rune.positionInitialized) {
                const auto center = cellCenter(row, col);
                gemCenterX = center.first;
                gemCenterY = center.second;
            }

            models_.emplace_back(buildQuadModel(gemCenterX - gemHalfWidth,
                                                gemCenterY + gemHalfHeight,
                                                gemCenterX + gemHalfWidth,
                                                gemCenterY - gemHalfHeight,
                                                0.0f,
                                                texture));
        }
    }

    const float screenW = static_cast<float>(width_);
    const float screenH = static_cast<float>(height_);
    if (screenW > 0.0f && screenH > 0.0f) {
        const float worldWidthToPixel = worldWidth / screenW;
        const float worldHeightToPixel = worldHeight / screenH;

        auto rectFromTopLeft = [&](float leftPx,
                                   float topPx,
                                   float widthPx,
                                   float heightPx) {
            const float widthWorld = widthPx * worldWidthToPixel;
            const float heightWorld = heightPx * worldHeightToPixel;
            const float leftWorld = -worldWidth * 0.5f + (leftPx / screenW) * worldWidth;
            const float topWorld = kProjectionHalfHeight - (topPx / screenH) * worldHeight;
            const float bottomWorld = topWorld - heightWorld;
            return std::array<float, 4>{leftWorld, bottomWorld, widthWorld, heightWorld};
        };

        const float heroWidthPx = 200.0f;
        const float heroHeightPx = 240.0f;
        const float heroLeftPx = screenW * 0.5f - heroWidthPx * 0.5f;
        const float heroTopPx = screenH - heroHeightPx - 40.0f;
        const auto heroRect = rectFromTopLeft(heroLeftPx, heroTopPx, heroWidthPx, heroHeightPx);
        renderTexture(spHeroTexture_,
                      heroRect[0],
                      heroRect[1],
                      heroRect[2],
                      heroRect[3],
                      0.05f);

        const float enemyWidthPx = 200.0f;
        const float enemyHeightPx = 240.0f;
        const float enemyLeftPx = screenW * 0.5f - enemyWidthPx * 0.5f;
        const float enemyTopPx = 40.0f;
        const auto enemyRect = rectFromTopLeft(enemyLeftPx,
                                               enemyTopPx,
                                               enemyWidthPx,
                                               enemyHeightPx);
        renderTexture(spEnemyTexture_,
                      enemyRect[0],
                      enemyRect[1],
                      enemyRect[2],
                      enemyRect[3],
                      0.05f);

        const float hpBarWidthPx = 200.0f;
        const float hpBarHeightPx = 20.0f;
        const float heroHpTopPx = heroTopPx - hpBarHeightPx;
        const auto heroHpRect = rectFromTopLeft(heroLeftPx,
                                                heroHpTopPx,
                                                hpBarWidthPx,
                                                hpBarHeightPx);
        drawHPBar(heroHP_,
                  heroMaxHP_,
                  heroHpRect[0],
                  heroHpRect[1],
                  heroHpRect[2],
                  heroHpRect[3]);

        const float enemyHpTopPx = enemyTopPx + enemyHeightPx;
        const auto enemyHpRect = rectFromTopLeft(enemyLeftPx,
                                                 enemyHpTopPx,
                                                 hpBarWidthPx,
                                                 hpBarHeightPx);
        drawHPBar(enemyHP_,
                  enemyMaxHP_,
                  enemyHpRect[0],
                  enemyHpRect[1],
                  enemyHpRect[2],
                  enemyHpRect[3]);
    }

    if (gameState_ == GameState::VICTORY || gameState_ == GameState::DEFEAT) {
        std::shared_ptr<TextureAsset> spTextTexture;
        if (gameState_ == GameState::VICTORY) {
            if (!spVictoryTexture_) {
                spVictoryTexture_ = TextureAsset::createTextTexture("VICTORY", 255, 255, 255, 255);
            }
            spTextTexture = spVictoryTexture_;
        } else if (gameState_ == GameState::DEFEAT) {
            if (!spDefeatTexture_) {
                spDefeatTexture_ = TextureAsset::createTextTexture("DEFEAT", 255, 100, 100, 255);
            }
            spTextTexture = spDefeatTexture_;
        }

        if (spTextTexture) {
            const float desiredWidth = boardWidth * kResultBannerWidthScale;
            const float textureAspect = static_cast<float>(spTextTexture->getWidth()) /
                                        static_cast<float>(spTextTexture->getHeight());
            const float safeAspect = textureAspect <= 0.0f ? 1.0f : textureAspect;
            const float desiredHeight = desiredWidth / safeAspect;
            const float bannerCenterX = boardCenterX;
            const float bannerCenterY = boardCenterY + boardHeight * 0.35f;
            const float halfWidth = desiredWidth * 0.5f;
            const float halfHeight = desiredHeight * 0.5f;

            models_.emplace_back(buildQuadModel(bannerCenterX - halfWidth,
                                                bannerCenterY + halfHeight,
                                                bannerCenterX + halfWidth,
                                                bannerCenterY - halfHeight,
                                                0.1f,
                                                spTextTexture));
        }
    }

    sceneDirty_ = false;
}

void Renderer::ensureBoardInitialized() {
    if (boardReady_) {
        return;
    }

    board_.assign(kBoardRows * kBoardColumns, Rune{});
    boardReady_ = true;

    do {
        for (int row = 0; row < kBoardRows; ++row) {
            for (int col = 0; col < kBoardColumns; ++col) {
                setGem(row, col, randomGem());
            }
        }
    } while (!findMatches().empty());

    heroHP_ = heroMaxHP_;
    enemyHP_ = enemyMaxHP_;
    heroMana_ = 0;
    gameState_ = GameState::PLAYING;
    sceneDirty_ = true;
}

Renderer::GemType Renderer::randomGem() {
    if (skullChanceDistribution_(rng_) < kSkullSpawnChance) {
        return GemType::Skull;
    }

    int value = gemDistribution_(rng_);
    switch (value) {
        case 0:
            return GemType::Red;
        case 1:
            return GemType::Green;
        default:
            return GemType::Blue;
    }
}

Renderer::GemType Renderer::getGem(int row, int col) const {
    if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
        return GemType::None;
    }
    const size_t index = static_cast<size_t>(row * kBoardColumns + col);
    return board_[index].type;
}

void Renderer::setGem(int row, int col, GemType type) {
    if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
        return;
    }
    Rune &rune = board_[static_cast<size_t>(row * kBoardColumns + col)];
    if (type == GemType::None) {
        rune = Rune{};
        return;
    }

    rune.type = type;
    rune.positionInitialized = false;
    updateRuneTarget(row, col, rune);
}

std::vector<Renderer::MatchGroup> Renderer::findMatches() const {
    std::vector<MatchGroup> matches;
    if (!boardReady_) {
        return matches;
    }

    auto emitHorizontalRun = [&](GemType type, int row, int startCol, int count) {
        if (count < 3 || type == GemType::None) {
            return;
        }
        MatchGroup group{type, {}};
        group.cells.reserve(static_cast<size_t>(count));
        for (int c = startCol; c < startCol + count; ++c) {
            group.cells.emplace_back(row, c);
        }
        matches.push_back(std::move(group));
    };

    for (int row = 0; row < kBoardRows; ++row) {
        GemType current = GemType::None;
        int runStart = 0;
        int count = 0;
        for (int col = 0; col < kBoardColumns; ++col) {
            GemType type = getGem(row, col);
            if (type != GemType::None && type == current) {
                ++count;
            } else {
                emitHorizontalRun(current, row, runStart, count);
                if (type == GemType::None) {
                    current = GemType::None;
                    count = 0;
                } else {
                    current = type;
                    count = 1;
                    runStart = col;
                }
            }
        }
        emitHorizontalRun(current, row, runStart, count);
    }

    auto emitVerticalRun = [&](GemType type, int col, int startRow, int count) {
        if (count < 3 || type == GemType::None) {
            return;
        }
        MatchGroup group{type, {}};
        group.cells.reserve(static_cast<size_t>(count));
        for (int r = startRow; r < startRow + count; ++r) {
            group.cells.emplace_back(r, col);
        }
        matches.push_back(std::move(group));
    };

    for (int col = 0; col < kBoardColumns; ++col) {
        GemType current = GemType::None;
        int runStart = 0;
        int count = 0;
        for (int row = 0; row < kBoardRows; ++row) {
            GemType type = getGem(row, col);
            if (type != GemType::None && type == current) {
                ++count;
            } else {
                emitVerticalRun(current, col, runStart, count);
                if (type == GemType::None) {
                    current = GemType::None;
                    count = 0;
                } else {
                    current = type;
                    count = 1;
                    runStart = row;
                }
            }
        }
        emitVerticalRun(current, col, runStart, count);
    }

    return matches;
}

void Renderer::removeMatches(const std::vector<MatchGroup> &matches) {
    std::vector<bool> cleared(board_.size(), false);
    for (const auto &group: matches) {
        for (const auto &cell: group.cells) {
            const int row = cell.first;
            const int col = cell.second;
            if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
                continue;
            }
            const size_t index = static_cast<size_t>(row * kBoardColumns + col);
            if (index >= cleared.size() || cleared[index]) {
                continue;
            }
            setGem(row, col, GemType::None);
            cleared[index] = true;
        }
    }
}

void Renderer::applyMatchEffects(const std::vector<MatchGroup> &matches) {
    if (matches.empty()) {
        return;
    }

    std::array<int, 4> gemCounts = {0, 0, 0, 0};
    std::vector<bool> counted(board_.size(), false);
    for (const auto &group: matches) {
        const int typeIndex = static_cast<int>(group.type);
        if (typeIndex < 0 || typeIndex >= static_cast<int>(gemCounts.size())) {
            continue;
        }

        for (const auto &cell: group.cells) {
            const int row = cell.first;
            const int col = cell.second;
            if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
                continue;
            }
            const size_t index = static_cast<size_t>(row * kBoardColumns + col);
            if (index >= counted.size() || counted[index]) {
                continue;
            }
            counted[index] = true;
            ++gemCounts[typeIndex];
        }
    }

    bool statsChanged = false;

    const int redCount = gemCounts[static_cast<int>(GemType::Red)];
    if (redCount > 0) {
        const int newEnemyHP = std::max(0, enemyHP_ - kRedMatchDamage * redCount);
        if (newEnemyHP != enemyHP_) {
            enemyHP_ = newEnemyHP;
            statsChanged = true;
        }
    }

    const int blueCount = gemCounts[static_cast<int>(GemType::Blue)];
    if (blueCount > 0) {
        const int newHeroHP = std::min(heroMaxHP_, heroHP_ + kBlueMatchHeal * blueCount);
        if (newHeroHP != heroHP_) {
            heroHP_ = newHeroHP;
            statsChanged = true;
        }
    }

    const int greenCount = gemCounts[static_cast<int>(GemType::Green)];
    if (greenCount > 0) {
        const int newMana = std::min(heroMaxMana_, heroMana_ + kGreenMatchMana * greenCount);
        if (newMana != heroMana_) {
            heroMana_ = newMana;
            statsChanged = true;
        }
    }

    const int skullCount = gemCounts[static_cast<int>(GemType::Skull)];
    if (skullCount > 0) {
        const int newHeroHP = std::max(0, heroHP_ - kSkullMatchDamage * skullCount);
        if (newHeroHP != heroHP_) {
            heroHP_ = newHeroHP;
            statsChanged = true;
        }
    }

    if (statsChanged) {
        sceneDirty_ = true;
    }

    if (gameState_ == GameState::PLAYING) {
        if (enemyHP_ <= 0) {
            gameState_ = GameState::VICTORY;
            sceneDirty_ = true;
        } else if (heroHP_ <= 0) {
            gameState_ = GameState::DEFEAT;
            sceneDirty_ = true;
        }
    }
}

void Renderer::applyGravityAndFill() {
    for (int col = 0; col < kBoardColumns; ++col) {
        int writeRow = kBoardRows - 1;
        for (int row = kBoardRows - 1; row >= 0; --row) {
            Rune &currentRune = runeAt(row, col);
            if (currentRune.type == GemType::None) {
                continue;
            }

            if (writeRow != row) {
                Rune movedRune = currentRune;
                currentRune = Rune{};
                Rune &destinationRune = runeAt(writeRow, col);
                destinationRune = movedRune;
                updateRuneTarget(writeRow, col, destinationRune);
            } else {
                updateRuneTarget(row, col, currentRune);
            }

            --writeRow;
        }
        for (int row = writeRow; row >= 0; --row) {
            Rune &rune = runeAt(row, col);
            rune = Rune{};
            rune.type = randomGem();
            if (boardGeometryValid_) {
                const auto center = cellCenter(row, col);
                rune.currentX = center.first;
                rune.currentY = gridTop_ + cellHeight_ * 0.5f;
                rune.positionInitialized = true;
                rune.targetX = center.first;
                rune.targetY = center.second;
            } else {
                rune.positionInitialized = false;
            }
            updateRuneTarget(row, col, rune);
        }
    }
}

bool Renderer::updateBoardState() {
    if (!boardReady_) {
        return false;
    }

    if (gameState_ != GameState::PLAYING) {
        return false;
    }

    return processMatches();
}

bool Renderer::processMatches() {
    bool changed = false;
    while (gameState_ == GameState::PLAYING) {
        auto matches = findMatches();
        if (matches.empty()) {
            break;
        }
        applyMatchEffects(matches);
        removeMatches(matches);
        applyGravityAndFill();
        changed = true;
        if (gameState_ != GameState::PLAYING) {
            break;
        }
    }
    if (changed) {
        sceneDirty_ = true;
    }
    return changed;
}

Model Renderer::buildQuadModel(float left,
                               float top,
                               float right,
                               float bottom,
                               float z,
                               const std::shared_ptr<TextureAsset> &texture) const {
    std::vector<Vertex> vertices = {
            Vertex(Vector3{right, top, z}, Vector2{1.f, 0.f}),
            Vertex(Vector3{left, top, z}, Vector2{0.f, 0.f}),
            Vertex(Vector3{left, bottom, z}, Vector2{0.f, 1.f}),
            Vertex(Vector3{right, bottom, z}, Vector2{1.f, 1.f})
    };
    std::vector<Index> indices = {0, 1, 2, 0, 2, 3};
    return Model(std::move(vertices), std::move(indices), texture);
}

void Renderer::renderTexture(const std::shared_ptr<TextureAsset> &texture,
                             float left,
                             float bottom,
                             float width,
                             float height,
                             float z) {
    if (!texture || width <= 0.0f || height <= 0.0f) {
        return;
    }

    models_.emplace_back(buildQuadModel(left,
                                        bottom + height,
                                        left + width,
                                        bottom,
                                        z,
                                        texture));
}

void Renderer::renderQuad(float left,
                          float bottom,
                          float width,
                          float height,
                          float r,
                          float g,
                          float b,
                          float a,
                          float z) {
    if (width <= 0.0f || height <= 0.0f) {
        return;
    }

    std::shared_ptr<TextureAsset> texture;
    const bool isWhite = std::abs(r - 1.0f) < 1e-4f &&
                         std::abs(g - 1.0f) < 1e-4f &&
                         std::abs(b - 1.0f) < 1e-4f &&
                         std::abs(a - 1.0f) < 1e-4f;
    if (isWhite && spWhiteTexture_) {
        texture = spWhiteTexture_;
    } else {
        texture = getSolidColorTexture(r, g, b, a);
    }

    if (!texture) {
        return;
    }

    models_.emplace_back(buildQuadModel(left,
                                        bottom + height,
                                        left + width,
                                        bottom,
                                        z,
                                        texture));
}

void Renderer::drawHPBar(int hp,
                         int hpMax,
                         float left,
                         float bottom,
                         float width,
                         float height) {
    if (hpMax <= 0) {
        return;
    }

    renderQuad(left, bottom, width, height, 0.2f, 0.2f, 0.2f, 1.0f, 0.06f);

    float ratio = static_cast<float>(hp) / static_cast<float>(hpMax);
    ratio = std::clamp(ratio, 0.0f, 1.0f);
    if (ratio <= 0.0f) {
        return;
    }

    renderQuad(left,
               bottom,
               width * ratio,
               height,
               0.0f,
               1.0f,
               0.0f,
               1.0f,
               0.05f);
}

std::shared_ptr<TextureAsset> Renderer::getSolidColorTexture(float r,
                                                             float g,
                                                             float b,
                                                             float a) {
    auto clamp01 = [](float value) {
        return std::max(0.0f, std::min(1.0f, value));
    };

    const auto toChannel = [&](float value) -> uint8_t {
        return static_cast<uint8_t>(std::round(clamp01(value) * 255.0f));
    };

    const uint8_t red = toChannel(r);
    const uint8_t green = toChannel(g);
    const uint8_t blue = toChannel(b);
    const uint8_t alpha = toChannel(a);

    const uint32_t key = (static_cast<uint32_t>(red) << 24U) |
                         (static_cast<uint32_t>(green) << 16U) |
                         (static_cast<uint32_t>(blue) << 8U) |
                         static_cast<uint32_t>(alpha);

    auto it = solidColorTextures_.find(key);
    if (it != solidColorTextures_.end()) {
        return it->second;
    }

    auto texture = TextureAsset::createSolidColorTexture(red, green, blue, alpha);
    solidColorTextures_[key] = texture;
    return texture;
}

std::shared_ptr<TextureAsset> Renderer::textureForGem(GemType type) const {
    switch (type) {
        case GemType::Red:
            return spRedGemTexture_;
        case GemType::Green:
            return spGreenGemTexture_;
        case GemType::Blue:
            return spBlueGemTexture_;
        case GemType::Skull:
            return spSkullGemTexture_;
        default:
            return nullptr;
    }
}

Renderer::Rune &Renderer::runeAt(int row, int col) {
    const size_t index = static_cast<size_t>(row * kBoardColumns + col);
    return board_[index];
}

const Renderer::Rune &Renderer::runeAt(int row, int col) const {
    const size_t index = static_cast<size_t>(row * kBoardColumns + col);
    return board_[index];
}

void Renderer::updateRuneTarget(int row, int col, Rune &rune) {
    if (rune.type == GemType::None || !boardGeometryValid_) {
        return;
    }

    const auto center = cellCenter(row, col);
    rune.targetX = center.first;
    rune.targetY = center.second;
    if (!rune.positionInitialized) {
        rune.currentX = rune.targetX;
        rune.currentY = rune.targetY;
        rune.positionInitialized = true;
    }
}

void Renderer::updateAllRuneTargets(bool snapToTarget) {
    if (!boardReady_ || !boardGeometryValid_) {
        return;
    }

    for (int row = 0; row < kBoardRows; ++row) {
        for (int col = 0; col < kBoardColumns; ++col) {
            Rune &rune = runeAt(row, col);
            if (rune.type == GemType::None) {
                continue;
            }

            updateRuneTarget(row, col, rune);
            if (snapToTarget) {
                rune.currentX = rune.targetX;
                rune.currentY = rune.targetY;
                rune.positionInitialized = true;
            }
        }
    }
}

void Renderer::updateRuneAnimation(float deltaTimeSeconds) {
    if (!boardReady_ || deltaTimeSeconds <= 0.0f) {
        return;
    }

    const float safePixelScale = boardPixelToWorld_ <= 0.0f ? 1.0f : boardPixelToWorld_;
    bool anyMovement = false;

    for (auto &rune: board_) {
        if (rune.type == GemType::None) {
            continue;
        }

        const float deltaX = rune.targetX - rune.currentX;
        const float deltaY = rune.targetY - rune.currentY;
        if (std::fabs(deltaX) > 0.0001f || std::fabs(deltaY) > 0.0001f) {
            anyMovement = true;
        }
        rune.currentX += deltaX * 10.0f * deltaTimeSeconds;
        rune.currentY += deltaY * 10.0f * deltaTimeSeconds;

        const float diffX = std::fabs(rune.currentX - rune.targetX) / safePixelScale;
        const float diffY = std::fabs(rune.currentY - rune.targetY) / safePixelScale;
        if (diffX < 1.0f && diffY < 1.0f) {
            rune.currentX = rune.targetX;
            rune.currentY = rune.targetY;
            rune.positionInitialized = true;
        }
    }

    if (anyMovement) {
        sceneDirty_ = true;
    }
}

std::pair<float, float> Renderer::cellCenter(int row, int col) const {
    const float gemCenterX = gridLeft_ + (static_cast<float>(col) + 0.5f) * cellWidth_;
    const float gemCenterY = gridTop_ - (static_cast<float>(row) + 0.5f) * cellHeight_;
    return {gemCenterX, gemCenterY};
}

void Renderer::handleInput() {
    // handle all queued inputs
    auto *inputBuffer = android_app_swap_input_buffers(app_);
    if (!inputBuffer) {
        // no inputs yet.
        return;
    }

    // handle motion events (motionEventsCounts can be 0).
    for (auto i = 0; i < inputBuffer->motionEventsCount; i++) {
        auto &motionEvent = inputBuffer->motionEvents[i];
        auto action = motionEvent.action;

        if (motionEvent.pointerCount <= 0) {
            continue;
        }

        // Find the pointer index, mask and bitshift to turn it into a readable value.
        auto pointerIndex = (action & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
                >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
        if (pointerIndex >= motionEvent.pointerCount) {
            pointerIndex = motionEvent.pointerCount - 1;
        }
        auto &pointer = motionEvent.pointers[pointerIndex];
        auto x = GameActivityPointerAxes_getX(&pointer);
        auto y = GameActivityPointerAxes_getY(&pointer);

        // determine the action type and process the event accordingly.
        switch (action & AMOTION_EVENT_ACTION_MASK) {
            case AMOTION_EVENT_ACTION_DOWN:
            case AMOTION_EVENT_ACTION_POINTER_DOWN:
                handlePointerDown(pointer.id, x, y);
                break;

            case AMOTION_EVENT_ACTION_CANCEL:
                // treat the CANCEL as an UP event: doing nothing in the app, except
                // removing the pointer from the cache if pointers are locally saved.
                // code pass through on purpose.
            case AMOTION_EVENT_ACTION_UP:
            case AMOTION_EVENT_ACTION_POINTER_UP:
                handlePointerUp(pointer.id, x, y);
                break;

            case AMOTION_EVENT_ACTION_MOVE:
                break;
            default:
                break;
        }
    }
    // clear the motion input count in this buffer for main thread to re-use.
    android_app_clear_motion_events(inputBuffer);

    // handle input key events.
    for (auto i = 0; i < inputBuffer->keyEventsCount; i++) {
        auto &keyEvent = inputBuffer->keyEvents[i];
        (void) keyEvent;
    }
    // clear the key input count too.
    android_app_clear_key_events(inputBuffer);
}

bool Renderer::attemptSwap(int startRow, int startCol, int endRow, int endCol) {
    if (!boardReady_ || gameState_ != GameState::PLAYING) {
        return false;
    }

    if (startRow < 0 || startRow >= kBoardRows || startCol < 0 || startCol >= kBoardColumns) {
        return false;
    }

    if (endRow < 0 || endRow >= kBoardRows || endCol < 0 || endCol >= kBoardColumns) {
        return false;
    }

    if (startRow == endRow && startCol == endCol) {
        return false;
    }

    const int rowDelta = std::abs(startRow - endRow);
    const int colDelta = std::abs(startCol - endCol);
    if (!((rowDelta == 1 && colDelta == 0) || (rowDelta == 0 && colDelta == 1))) {
        return false;
    }

    Rune &firstRune = runeAt(startRow, startCol);
    Rune &secondRune = runeAt(endRow, endCol);
    if (firstRune.type == GemType::None || secondRune.type == GemType::None) {
        return false;
    }

    std::swap(firstRune, secondRune);
    updateRuneTarget(startRow, startCol, firstRune);
    updateRuneTarget(endRow, endCol, secondRune);

    auto matches = findMatches();
    if (matches.empty()) {
        std::swap(firstRune, secondRune);
        updateRuneTarget(startRow, startCol, firstRune);
        updateRuneTarget(endRow, endCol, secondRune);
        return false;
    }

    processMatches();
    return true;
}

bool Renderer::screenToWorld(float screenX, float screenY, float &worldX, float &worldY) const {
    if (width_ <= 0 || height_ <= 0) {
        return false;
    }

    const float worldHeight = kProjectionHalfHeight * 2.0f;
    const float worldWidth = worldHeight * (static_cast<float>(width_) / static_cast<float>(height_));

    worldX = (screenX / static_cast<float>(width_)) * worldWidth - worldWidth * 0.5f;
    worldY = kProjectionHalfHeight - (screenY / static_cast<float>(height_)) * worldHeight;
    return true;
}

bool Renderer::worldToScreen(float worldX, float worldY, float &screenX, float &screenY) const {
    if (width_ <= 0 || height_ <= 0) {
        return false;
    }

    const float worldHeight = kProjectionHalfHeight * 2.0f;
    const float worldWidth = worldHeight * (static_cast<float>(width_) / static_cast<float>(height_));

    screenX = ((worldX + worldWidth * 0.5f) / worldWidth) * static_cast<float>(width_);
    screenY = ((kProjectionHalfHeight - worldY) / worldHeight) * static_cast<float>(height_);
    return true;
}

bool Renderer::worldToBoardCell(float worldX, float worldY, int &outRow, int &outCol) const {
    if (!boardGeometryValid_) {
        return false;
    }

    if (worldX < gridLeft_ || worldX > gridRight_ || worldY > gridTop_ || worldY < gridBottom_) {
        return false;
    }

    const float columnFloat = (worldX - gridLeft_) / cellWidth_;
    const float rowFloat = (gridTop_ - worldY) / cellHeight_;

    int col = static_cast<int>(std::floor(columnFloat));
    int row = static_cast<int>(std::floor(rowFloat));

    if (col < 0 || col >= kBoardColumns || row < 0 || row >= kBoardRows) {
        return false;
    }

    outRow = row;
    outCol = col;
    return true;
}

void Renderer::handlePointerDown(int32_t pointerId, float screenX, float screenY) {
    if (activePointerId_ != -1) {
        return;
    }

    float worldX = 0.0f;
    float worldY = 0.0f;
    if (!screenToWorld(screenX, screenY, worldX, worldY)) {
        return;
    }

    int row = 0;
    int col = 0;
    if (!worldToBoardCell(worldX, worldY, row, col)) {
        return;
    }

    triggerRuneSelectionEffect(row, col);

    hasSelectedCell_ = true;
    selectedRow_ = row;
    selectedCol_ = col;
    activePointerId_ = pointerId;
}

void Renderer::handlePointerUp(int32_t pointerId, float screenX, float screenY) {
    if (pointerId != activePointerId_) {
        return;
    }

    float worldX = 0.0f;
    float worldY = 0.0f;
    if (screenToWorld(screenX, screenY, worldX, worldY)) {
        int row = 0;
        int col = 0;
        if (hasSelectedCell_ && worldToBoardCell(worldX, worldY, row, col)) {
            attemptSwap(selectedRow_, selectedCol_, row, col);
        }
    }

    hasSelectedCell_ = false;
    activePointerId_ = -1;
}

void Renderer::triggerRuneSelectionEffect(int row, int col) {
    if (!boardReady_ || !boardGeometryValid_) {
        return;
    }

    if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
        return;
    }

    const Rune &rune = runeAt(row, col);
    if (rune.type == GemType::None) {
        return;
    }

    float centerWorldX = rune.positionInitialized ? rune.currentX : 0.0f;
    float centerWorldY = rune.positionInitialized ? rune.currentY : 0.0f;
    if (!rune.positionInitialized) {
        const auto center = cellCenter(row, col);
        centerWorldX = center.first;
        centerWorldY = center.second;
    }

    float centerScreenX = 0.0f;
    float centerScreenY = 0.0f;
    if (!worldToScreen(centerWorldX, centerWorldY, centerScreenX, centerScreenY)) {
        return;
    }

    const float worldHeight = kProjectionHalfHeight * 2.0f;
    const float pixelsPerWorldUnit = static_cast<float>(height_) / worldHeight;
    const float gemWorldSize = std::min(cellWidth_, cellHeight_) * kGemVisualScale;
    const float effectSizePx = gemWorldSize * pixelsPerWorldUnit;

    sendRuneSelectionToJava(centerScreenX, centerScreenY, effectSizePx);
}

void Renderer::sendRuneSelectionToJava(float centerX, float centerY, float sizePx) {
    if (!app_ || !app_->activity) {
        return;
    }

    auto *activity = app_->activity;
    JavaVM *vm = activity->vm;
    if (!vm) {
        return;
    }

    JNIEnv *env = nullptr;
    const jint getEnvResult = vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    bool didAttach = false;
    if (getEnvResult == JNI_EDETACHED) {
        if (vm->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return;
        }
        didAttach = true;
    } else if (getEnvResult != JNI_OK) {
        return;
    }

    jclass activityClass = env->GetObjectClass(activity->clazz);
    if (!activityClass) {
        if (didAttach) {
            vm->DetachCurrentThread();
        }
        return;
    }

    jmethodID methodId = env->GetMethodID(activityClass, "onRuneSelected", "(FFF)V");
    if (methodId) {
        env->CallVoidMethod(activity->clazz,
                            methodId,
                            static_cast<jfloat>(centerX),
                            static_cast<jfloat>(centerY),
                            static_cast<jfloat>(sizePx));
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
    }

    env->DeleteLocalRef(activityClass);

    if (didAttach) {
        vm->DetachCurrentThread();
    }
}
