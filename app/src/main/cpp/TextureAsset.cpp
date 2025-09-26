#include "TextureAsset.h"
#include "AndroidOut.h"
#include "Utility.h"
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

std::shared_ptr<TextureAsset>
TextureAsset::loadAsset(AAssetManager *assetManager, const std::string &assetPath) {
    // Get the image from asset manager
    auto pAndroidRobotPng = AAssetManager_open(
            assetManager,
            assetPath.c_str(),
            AASSET_MODE_BUFFER);

    if (!pAndroidRobotPng) {
        aout << "Unable to open asset: " << assetPath << std::endl;
        return nullptr;
    }

    // Load the entire asset into memory for decoding.
    const auto assetLength = static_cast<size_t>(AAsset_getLength(pAndroidRobotPng));
    std::vector<uint8_t> assetBuffer(assetLength);
    const auto bytesRead = AAsset_read(pAndroidRobotPng, assetBuffer.data(), assetLength);
    if (bytesRead <= 0 || static_cast<size_t>(bytesRead) != assetLength) {
        aout << "Failed to read asset: " << assetPath << std::endl;
        AAsset_close(pAndroidRobotPng);
        return nullptr;
    }

    int width = 0;
    int height = 0;
    int channels = 0;
    stbi_uc *decodedData = stbi_load_from_memory(
            assetBuffer.data(),
            static_cast<int>(bytesRead),
            &width,
            &height,
            &channels,
            STBI_rgb_alpha);

    if (!decodedData) {
        aout << "Failed to decode image data for asset: " << assetPath << std::endl;
        AAsset_close(pAndroidRobotPng);
        return nullptr;
    }

    // Get an opengl texture
    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    // Load the texture into VRAM
    glTexImage2D(
            GL_TEXTURE_2D, // target
            0, // mip level
            GL_RGBA, // internal format, often advisable to use BGR
            width, // width of the texture
            height, // height of the texture
            0, // border (always 0)
            GL_RGBA, // format
            GL_UNSIGNED_BYTE, // type
            decodedData // Data to upload
    );

    // generate mip levels. Not really needed for 2D, but good to do
    glGenerateMipmap(GL_TEXTURE_2D);

    // cleanup helpers
    stbi_image_free(decodedData);
    AAsset_close(pAndroidRobotPng);

    // Create a shared pointer so it can be cleaned up easily/automatically
    return std::shared_ptr<TextureAsset>(new TextureAsset(textureId, width, height));
}

std::shared_ptr<TextureAsset> TextureAsset::createSolidColorTexture(
        uint8_t red,
        uint8_t green,
        uint8_t blue,
        uint8_t alpha) {
    GLuint textureId = 0;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    const uint8_t pixel[] = {red, green, blue, alpha};
    glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            1,
            1,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            pixel);

    return std::shared_ptr<TextureAsset>(new TextureAsset(textureId, 1, 1));
}

TextureAsset::~TextureAsset() {
    // return texture resources
    glDeleteTextures(1, &textureID_);
    textureID_ = 0;
}