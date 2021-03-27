package tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
