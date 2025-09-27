#include "TextureAsset.h"
#include "AndroidOut.h"
#include "Utility.h"
#include <algorithm>
#include <array>
#include <cctype>
#include <unordered_map>
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

std::shared_ptr<TextureAsset> TextureAsset::createTextTexture(
        const std::string &text,
        uint8_t red,
        uint8_t green,
        uint8_t blue,
        uint8_t alpha) {
    if (text.empty()) {
        return createSolidColorTexture(0, 0, 0, 0);
    }

    static const int glyphWidth = 5;
    static const int glyphHeight = 7;
    static const int glyphSpacing = 1;
    static const int pixelScale = 6;

    struct Glyph {
        std::array<uint8_t, glyphHeight> rows;
    };

    static const std::unordered_map<char, Glyph> glyphs = {
            {'A', {{0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001}}},
            {'C', {{0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110}}},
            {'D', {{0b11110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b11110}}},
            {'E', {{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111}}},
            {'F', {{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000}}},
            {'I', {{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b11111}}},
            {'O', {{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110}}},
            {'R', {{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001}}},
            {'T', {{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100}}},
            {'V', {{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100}}},
            {'Y', {{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100}}},
            {' ', {{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000}}}
    };

    const int normalizedWidth = static_cast<int>(text.size()) * (glyphWidth + glyphSpacing) - glyphSpacing;
    const int textureWidth = std::max(normalizedWidth, glyphWidth) * pixelScale;
    const int textureHeight = glyphHeight * pixelScale;

    std::vector<uint8_t> pixels(textureWidth * textureHeight * 4, 0);

    int cursorX = 0;
    for (char ch: text) {
        char upper = static_cast<char>(std::toupper(static_cast<unsigned char>(ch)));
        auto glyphIt = glyphs.find(upper);
        Glyph glyph = glyphIt != glyphs.end() ? glyphIt->second : glyphs.at(' ');

        for (int row = 0; row < glyphHeight; ++row) {
            for (int col = 0; col < glyphWidth; ++col) {
                bool filled = (glyph.rows[row] >> (glyphWidth - 1 - col)) & 0x1;
                if (!filled) {
                    continue;
                }
                for (int y = 0; y < pixelScale; ++y) {
                    for (int x = 0; x < pixelScale; ++x) {
                        int targetX = (cursorX + col) * pixelScale + x;
                        int targetY = row * pixelScale + y;
                        if (targetX < 0 || targetX >= textureWidth || targetY < 0 || targetY >= textureHeight) {
                            continue;
                        }
                        size_t idx = static_cast<size_t>(targetY * textureWidth + targetX) * 4;
                        pixels[idx + 0] = red;
                        pixels[idx + 1] = green;
                        pixels[idx + 2] = blue;
                        pixels[idx + 3] = alpha;
                    }
                }
            }
        }

        cursorX += glyphWidth + glyphSpacing;
    }

    GLuint textureId = 0;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            textureWidth,
            textureHeight,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            pixels.data());

    return std::shared_ptr<TextureAsset>(new TextureAsset(textureId, textureWidth, textureHeight));
}

TextureAsset::~TextureAsset() {
    // return texture resources
    glDeleteTextures(1, &textureID_);
    textureID_ = 0;
}