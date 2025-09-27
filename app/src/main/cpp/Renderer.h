#ifndef ANDROIDGLINVESTIGATIONS_RENDERER_H
#define ANDROIDGLINVESTIGATIONS_RENDERER_H

#include <EGL/egl.h>
#include <memory>
#include <random>
#include <utility>
#include <vector>

#include "Model.h"
#include "Shader.h"

class TextureAsset;

struct android_app;

class Renderer {
public:
    /*!
     * @param pApp the android_app this Renderer belongs to, needed to configure GL
     */
    inline Renderer(android_app *pApp) :
            app_(pApp),
            display_(EGL_NO_DISPLAY),
            surface_(EGL_NO_SURFACE),
            context_(EGL_NO_CONTEXT),
            width_(0),
            height_(0),
            shaderNeedsNewProjectionMatrix_(true),
            rng_(std::random_device{}()),
            gemDistribution_(0, 2),
            sceneDirty_(true),
            boardReady_(false),
            heroHP_(heroMaxHP_),
            enemyHP_(enemyMaxHP_),
            heroMana_(0),
            battleOutcome_(BattleOutcome::None) {
        initRenderer();
    }

    virtual ~Renderer();

    /*!
     * Handles input from the android_app.
     *
     * Note: this will clear the input queue
     */
    void handleInput();

    /*!
     * Renders all the models in the renderer
     */
    void render();

private:
    /*!
     * Performs necessary OpenGL initialization. Customize this if you want to change your EGL
     * context or application-wide settings.
     */
    void initRenderer();

    /*!
     * @brief we have to check every frame to see if the framebuffer has changed in size. If it has,
     * update the viewport accordingly
     */
    void updateRenderArea();

    /*!
     * Creates the models for this sample. You'd likely load a scene configuration from a file or
     * use some other setup logic in your full game.
     */
    void createModels();

    enum class GemType {
        None = -1,
        Red = 0,
        Green = 1,
        Blue = 2,
    };

    struct MatchGroup {
        GemType type;
        std::vector<std::pair<int, int>> cells;
    };

    enum class BattleOutcome {
        None,
        Victory,
        Defeat,
    };

    void ensureBoardInitialized();
    GemType randomGem();
    GemType getGem(int row, int col) const;
    void setGem(int row, int col, GemType type);
    std::vector<MatchGroup> findMatches() const;
    void removeMatches(const std::vector<MatchGroup> &matches);
    void applyGravityAndFill();
    void applyMatchEffects(const std::vector<MatchGroup> &matches);
    bool updateBoardState();
    Model buildQuadModel(float left,
                         float top,
                         float right,
                         float bottom,
                         float z,
                         const std::shared_ptr<TextureAsset> &texture) const;
    std::shared_ptr<TextureAsset> textureForGem(GemType type) const;

    android_app *app_;
    EGLDisplay display_;
    EGLSurface surface_;
    EGLContext context_;
    EGLint width_;
    EGLint height_;

    bool shaderNeedsNewProjectionMatrix_;

    std::unique_ptr<Shader> shader_;
    std::vector<Model> models_;
    std::shared_ptr<TextureAsset> spBoardTexture_;
    std::shared_ptr<TextureAsset> spRedGemTexture_;
    std::shared_ptr<TextureAsset> spGreenGemTexture_;
    std::shared_ptr<TextureAsset> spBlueGemTexture_;
    std::shared_ptr<TextureAsset> spHeroTexture_;
    std::shared_ptr<TextureAsset> spEnemyTexture_;
    std::shared_ptr<TextureAsset> spHPBackgroundTexture_;
    std::shared_ptr<TextureAsset> spHeroHPTexture_;
    std::shared_ptr<TextureAsset> spEnemyHPTexture_;
    std::shared_ptr<TextureAsset> spManaTexture_;
    std::shared_ptr<TextureAsset> spVictoryTexture_;
    std::shared_ptr<TextureAsset> spDefeatTexture_;

    std::vector<GemType> board_;
    std::mt19937 rng_;
    std::uniform_int_distribution<int> gemDistribution_;
    bool sceneDirty_;
    bool boardReady_;
    int heroHP_;
    int enemyHP_;
    int heroMana_;
    const int heroMaxHP_ = 100;
    const int enemyMaxHP_ = 100;
    const int heroMaxMana_ = 100;
    BattleOutcome battleOutcome_;
};

#endif //ANDROIDGLINVESTIGATIONS_RENDERER_H
