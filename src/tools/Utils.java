package tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.stb.STBImage;
import static org.lwjgl.stb.STBImage.*;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL46.*;

public class Utils {
    public static String loadResource(String fileName){
        try {
            String result;
            result = Files.readString(Path.of(fileName));
            return result;
        } catch (FileNotFoundException e) {
            System.out.println("fill not found" + fileName);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int createShaderProgram(String vertAddress, String fragAddress) {
        String vShaderSource = loadResource(vertAddress);
        String fShaderSource = loadResource(fragAddress);

        int vShader = glCreateShader(GL_VERTEX_SHADER);
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(vShader, vShaderSource);
        glShaderSource(fShader, fShaderSource);
        glCompileShader(vShader);
        glCompileShader(fShader);

        checkOpenGlError();
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Could not compile shader.\n" + vertAddress);
            printShaderLog(vShader);
            System.exit(-1);
        }
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Could not compile shader.\n" + fragAddress);
            printShaderLog(fShader);
            System.exit(-1);

        }

        int vfProgram = glCreateProgram();
        glAttachShader(vfProgram, vShader);
        glAttachShader(vfProgram, fShader);
        glLinkProgram(vfProgram);
        checkOpenGlError();
        if (glGetProgrami(vfProgram, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Could not link program. \n" + vfProgram);
            printProgramLog(vfProgram);
            System.exit(-1);
        }

        return vfProgram;
    }

    public static boolean checkOpenGlError() {
        boolean foundError = false;
        int glErr = glGetError();
        while(glErr != GL_NO_ERROR) {
            System.out.println("openGL error: " + glErr);
            foundError = true;
            glErr = glGetError();
        }
        return foundError;
    }

    public static void printShaderLog(int shader) {
        System.err.println(glGetShaderInfoLog(shader));
    }

    public static void printProgramLog(int program) {
        System.err.println(glGetProgramInfoLog(program));
    }

    public static int loadImage(String imgLocation) {
        IntBuffer xBuffer= BufferUtils.createIntBuffer(1);
        IntBuffer yBuffer= BufferUtils.createIntBuffer(1);
        IntBuffer channelsBuffer= BufferUtils.createIntBuffer(1);
        ByteBuffer image = stbi_load(imgLocation, xBuffer, yBuffer, channelsBuffer, STBI_default);
        if (image == null){
            System.out.println("failed to load image: " + imgLocation +". "+ STBImage.stbi_failure_reason());
        }
        int width = xBuffer.get();
        int height = yBuffer.get();

        //create a new opengl texture
        int textureId = glGenTextures();
        //bind the texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        //tell opengl how to unpack the rgba bytes
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        //generate mip map
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(image);

        return textureId;
    }

    // public static ByteBuffer decodeImage(String imgLocation) {
    //     //String imageLocation = new String("E:/Games/My games/JackEngineI/resources/ghghhg.png");

    //     IntBuffer xBuffer= BufferUtils.createIntBuffer(1);
    //     IntBuffer yBuffer= BufferUtils.createIntBuffer(1);
    //     IntBuffer channelsBuffer= BufferUtils.createIntBuffer(1);
    //     ByteBuffer image = stbi_load(imgLocation, xBuffer, yBuffer, channelsBuffer, STBI_default);
    //     if (image == null){
    //         System.out.println("failed to load image: " + imgLocation +". "+ STBImage.stbi_failure_reason());
    //     }
    //     return image;
    // }
}
