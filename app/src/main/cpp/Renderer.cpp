#include "Renderer.h"

#include <game-activity/native_app_glue/android_native_app_glue.h>
#include <GLES3/gl3.h>
#include <algorithm>
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
static constexpr int kBoardColumns = 7;
static constexpr float kGemVisualScale = 0.8f;
static constexpr float kBoardPixelWidth = 768.f;
static constexpr float kBoardPixelHeight = 1152.f;
static constexpr float kCellPixelWidth = 110.f;
static constexpr float kCellPixelHeight = 144.f;
static constexpr float kBoardMarginScale = 0.85f;
static constexpr float kPortraitHeightScale = 0.6f;
static constexpr float kPortraitMarginScale = 0.05f;

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

    ensureBoardInitialized();
    if (updateBoardState()) {
        sceneDirty_ = true;
    }

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

    models_.clear();

    const float worldHeight = kProjectionHalfHeight * 2.0f;
    const float worldWidth = worldHeight * (static_cast<float>(width_) / static_cast<float>(height_));
    const float maxBoardWidth = worldWidth * kBoardMarginScale;
    const float maxBoardHeight = worldHeight * kBoardMarginScale;
    const float pixelToWorld = std::min(maxBoardWidth / kBoardPixelWidth,
                                        maxBoardHeight / kBoardPixelHeight);
    const float boardWidth = kBoardPixelWidth * pixelToWorld;
    const float boardHeight = kBoardPixelHeight * pixelToWorld;
    const float pixelToWorldX = pixelToWorld;
    const float pixelToWorldY = pixelToWorld;
    const float halfWidth = boardWidth * 0.5f;
    const float halfHeight = boardHeight * 0.5f;
    const float boardOriginX = -halfWidth;
    const float boardOriginY = halfHeight;
    const float boardLeft = boardOriginX;
    const float boardRight = boardOriginX + boardWidth;
    const float boardTop = boardOriginY;
    const float boardBottom = boardOriginY - boardHeight;

    models_.emplace_back(buildQuadModel(boardLeft,
                                        boardTop,
                                        boardRight,
                                        boardBottom,
                                        -0.2f,
                                        spBoardTexture_));

    const float portraitHeight = boardHeight * kPortraitHeightScale;
    const float portraitMargin = boardWidth * kPortraitMarginScale;
    const float heroAspect = static_cast<float>(spHeroTexture_->getWidth()) /
                             static_cast<float>(spHeroTexture_->getHeight());
    const float heroHalfHeight = portraitHeight * 0.5f;
    const float heroHalfWidth = portraitHeight * heroAspect * 0.5f;
    const float heroCenterX = boardLeft - heroHalfWidth - portraitMargin;
    const float heroCenterY = 0.0f;

    models_.emplace_back(buildQuadModel(heroCenterX - heroHalfWidth,
                                        heroCenterY + heroHalfHeight,
                                        heroCenterX + heroHalfWidth,
                                        heroCenterY - heroHalfHeight,
                                        0.05f,
                                        spHeroTexture_));

    const float enemyAspect = static_cast<float>(spEnemyTexture_->getWidth()) /
                              static_cast<float>(spEnemyTexture_->getHeight());
    const float enemyHalfHeight = portraitHeight * 0.5f;
    const float enemyHalfWidth = portraitHeight * enemyAspect * 0.5f;
    const float enemyCenterX = boardRight + enemyHalfWidth + portraitMargin;
    const float enemyCenterY = 0.0f;

    models_.emplace_back(buildQuadModel(enemyCenterX - enemyHalfWidth,
                                        enemyCenterY + enemyHalfHeight,
                                        enemyCenterX + enemyHalfWidth,
                                        enemyCenterY - enemyHalfHeight,
                                        0.05f,
                                        spEnemyTexture_));

    const float gemHalfWidth = (kCellPixelWidth * kGemVisualScale * pixelToWorldX) * 0.5f;
    const float gemHalfHeight = (kCellPixelHeight * kGemVisualScale * pixelToWorldY) * 0.5f;

    for (int row = 0; row < kBoardRows; ++row) {
        for (int col = 0; col < kBoardColumns; ++col) {
            GemType type = getGem(row, col);
            if (type == GemType::None) {
                continue;
            }

            auto texture = textureForGem(type);
            if (!texture) {
                continue;
            }

            const float gemCenterPixelX = (static_cast<float>(col) + 0.5f) * kCellPixelWidth;
            const float gemCenterPixelY = (static_cast<float>(row) + 0.5f) * kCellPixelHeight;
            const float gemCenterX = boardOriginX + gemCenterPixelX * pixelToWorldX;
            const float gemCenterY = boardOriginY - gemCenterPixelY * pixelToWorldY;

            models_.emplace_back(buildQuadModel(gemCenterX - gemHalfWidth,
                                                gemCenterY + gemHalfHeight,
                                                gemCenterX + gemHalfWidth,
                                                gemCenterY - gemHalfHeight,
                                                0.0f,
                                                texture));
        }
    }

    sceneDirty_ = false;
}

void Renderer::ensureBoardInitialized() {
    if (boardReady_) {
        return;
    }

    board_.assign(kBoardRows * kBoardColumns, GemType::None);
    for (int row = 0; row < kBoardRows; ++row) {
        for (int col = 0; col < kBoardColumns; ++col) {
            setGem(row, col, randomGem());
        }
    }

    boardReady_ = true;
    sceneDirty_ = true;
}

Renderer::GemType Renderer::randomGem() {
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
    return board_[row * kBoardColumns + col];
}

void Renderer::setGem(int row, int col, GemType type) {
    if (row < 0 || row >= kBoardRows || col < 0 || col >= kBoardColumns) {
        return;
    }
    board_[row * kBoardColumns + col] = type;
}

std::vector<std::pair<int, int>> Renderer::findMatches() const {
    std::vector<std::pair<int, int>> matches;
    if (!boardReady_) {
        return matches;
    }

    std::vector<bool> marked(board_.size(), false);

    for (int row = 0; row < kBoardRows; ++row) {
        GemType current = GemType::None;
        int runStart = 0;
        int count = 0;
        for (int col = 0; col < kBoardColumns; ++col) {
            GemType type = getGem(row, col);
            if (type != GemType::None && type == current) {
                count++;
            } else {
                if (count >= 3 && current != GemType::None) {
                    for (int c = runStart; c < runStart + count; ++c) {
                        marked[row * kBoardColumns + c] = true;
                    }
                }
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
        if (count >= 3 && current != GemType::None) {
            for (int c = runStart; c < runStart + count; ++c) {
                marked[row * kBoardColumns + c] = true;
            }
        }
    }

    for (int col = 0; col < kBoardColumns; ++col) {
        GemType current = GemType::None;
        int runStart = 0;
        int count = 0;
        for (int row = 0; row < kBoardRows; ++row) {
            GemType type = getGem(row, col);
            if (type != GemType::None && type == current) {
                count++;
            } else {
                if (count >= 3 && current != GemType::None) {
                    for (int r = runStart; r < runStart + count; ++r) {
                        marked[r * kBoardColumns + col] = true;
                    }
                }
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
        if (count >= 3 && current != GemType::None) {
            for (int r = runStart; r < runStart + count; ++r) {
                marked[r * kBoardColumns + col] = true;
            }
        }
    }

    for (int row = 0; row < kBoardRows; ++row) {
        for (int col = 0; col < kBoardColumns; ++col) {
            if (marked[row * kBoardColumns + col]) {
                matches.emplace_back(row, col);
            }
        }
    }

    return matches;
}

void Renderer::removeMatches(const std::vector<std::pair<int, int>> &matches) {
    for (const auto &match: matches) {
        setGem(match.first, match.second, GemType::None);
    }
}

void Renderer::applyGravityAndFill() {
    for (int col = 0; col < kBoardColumns; ++col) {
        int writeRow = kBoardRows - 1;
        for (int row = kBoardRows - 1; row >= 0; --row) {
            GemType type = getGem(row, col);
            if (type != GemType::None) {
                setGem(writeRow, col, type);
                if (writeRow != row) {
                    setGem(row, col, GemType::None);
                }
                --writeRow;
            }
        }
        for (int row = writeRow; row >= 0; --row) {
            setGem(row, col, randomGem());
        }
    }
}

bool Renderer::updateBoardState() {
    if (!boardReady_) {
        return false;
    }

    bool changed = false;
    while (true) {
        auto matches = findMatches();
        if (matches.empty()) {
            break;
        }
        removeMatches(matches);
        applyGravityAndFill();
        changed = true;
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

std::shared_ptr<TextureAsset> Renderer::textureForGem(GemType type) const {
    switch (type) {
        case GemType::Red:
            return spRedGemTexture_;
        case GemType::Green:
            return spGreenGemTexture_;
        case GemType::Blue:
            return spBlueGemTexture_;
        default:
            return nullptr;
    }
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

        // Find the pointer index, mask and bitshift to turn it into a readable value.
        auto pointerIndex = (action & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK)
                >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
        aout << "Pointer(s): ";

        // get the x and y position of this event if it is not ACTION_MOVE.
        auto &pointer = motionEvent.pointers[pointerIndex];
        auto x = GameActivityPointerAxes_getX(&pointer);
        auto y = GameActivityPointerAxes_getY(&pointer);

        // determine the action type and process the event accordingly.
        switch (action & AMOTION_EVENT_ACTION_MASK) {
            case AMOTION_EVENT_ACTION_DOWN:
            case AMOTION_EVENT_ACTION_POINTER_DOWN:
                aout << "(" << pointer.id << ", " << x << ", " << y << ") "
                     << "Pointer Down";
                break;

            case AMOTION_EVENT_ACTION_CANCEL:
                // treat the CANCEL as an UP event: doing nothing in the app, except
                // removing the pointer from the cache if pointers are locally saved.
                // code pass through on purpose.
            case AMOTION_EVENT_ACTION_UP:
            case AMOTION_EVENT_ACTION_POINTER_UP:
                aout << "(" << pointer.id << ", " << x << ", " << y << ") "
                     << "Pointer Up";
                break;

            case AMOTION_EVENT_ACTION_MOVE:
                // There is no pointer index for ACTION_MOVE, only a snapshot of
                // all active pointers; app needs to cache previous active pointers
                // to figure out which ones are actually moved.
                for (auto index = 0; index < motionEvent.pointerCount; index++) {
                    pointer = motionEvent.pointers[index];
                    x = GameActivityPointerAxes_getX(&pointer);
                    y = GameActivityPointerAxes_getY(&pointer);
                    aout << "(" << pointer.id << ", " << x << ", " << y << ")";

                    if (index != (motionEvent.pointerCount - 1)) aout << ",";
                    aout << " ";
                }
                aout << "Pointer Move";
                break;
            default:
                aout << "Unknown MotionEvent Action: " << action;
        }
        aout << std::endl;
    }
    // clear the motion input count in this buffer for main thread to re-use.
    android_app_clear_motion_events(inputBuffer);

    // handle input key events.
    for (auto i = 0; i < inputBuffer->keyEventsCount; i++) {
        auto &keyEvent = inputBuffer->keyEvents[i];
        aout << "Key: " << keyEvent.keyCode <<" ";
        switch (keyEvent.action) {
            case AKEY_EVENT_ACTION_DOWN:
                aout << "Key Down";
                break;
            case AKEY_EVENT_ACTION_UP:
                aout << "Key Up";
                break;
            case AKEY_EVENT_ACTION_MULTIPLE:
                // Deprecated since Android API level 29.
                aout << "Multiple Key Actions";
                break;
            default:
                aout << "Unknown KeyEvent Action: " << keyEvent.action;
        }
        aout << std::endl;
    }
    // clear the key input count too.
    android_app_clear_key_events(inputBuffer);
}