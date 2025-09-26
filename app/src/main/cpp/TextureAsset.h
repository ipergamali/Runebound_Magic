#ifndef ANDROIDGLINVESTIGATIONS_TEXTUREASSET_H
#define ANDROIDGLINVESTIGATIONS_TEXTUREASSET_H

#include <memory>
#include <android/asset_manager.h>
#include <GLES3/gl3.h>
#include <string>
#include <vector>
#include <cstdint>

class TextureAsset {
public:
    /*!
     * Loads a texture asset from the assets/ directory
     * @param assetManager Asset manager to use
     * @param assetPath The path to the asset
     * @return a shared pointer to a texture asset, resources will be reclaimed when it's cleaned up
     */
    static std::shared_ptr<TextureAsset>
    loadAsset(AAssetManager *assetManager, const std::string &assetPath);

    /*!
     * Creates a tiny 1x1 texture filled with the requested color. Handy as a
     * graceful fallback when an asset is missing while keeping the renderer
     * alive.
     */
    static std::shared_ptr<TextureAsset> createSolidColorTexture(
            uint8_t red,
            uint8_t green,
            uint8_t blue,
            uint8_t alpha = 255);

    ~TextureAsset();

    /*!
     * @return the texture id for use with OpenGL
     */
    constexpr GLuint getTextureID() const { return textureID_; }

    constexpr int getWidth() const { return width_; }

    constexpr int getHeight() const { return height_; }

private:
    inline TextureAsset(GLuint textureId, int width, int height)
            : textureID_(textureId), width_(width), height_(height) {}

    GLuint textureID_;
    int width_;
    int height_;
};

#endif //ANDROIDGLINVESTIGATIONS_TEXTUREASSET_H
