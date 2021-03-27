package main;

import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.opengl.GL46.*;
import static tools.Utils.*;
import java.nio.IntBuffer;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import static org.lwjgl.BufferUtils.*;
import org.lwjgl.BufferUtils.*;

public class Main {

    private static long windowHandle;
    private static int numVAOs = 1;
    private static int numVBOs = 2;
    private static int renderingProgram;
    private static int vao[] = new int[numVAOs];
    private static int vbo[] = new int[numVBOs];
    private static float cameraX, cameraY, cameraZ;
    private static float cubeLocX, cubeLocY, cubeLocZ;
    private static float cubeRotX, cubeRotY, cubeRotZ;
    private static float test;
    
    //shader and display variables
    private static int vLoc, projLoc;
    private static IntBuffer width = BufferUtils.createIntBuffer(1);
	private static IntBuffer height = BufferUtils.createIntBuffer(1);
    private static float aspect;
    private static Matrix4f pMat = new Matrix4f(), 
    vMat = new Matrix4f(), 
    tMat = new Matrix4f(),
    rMat = new Matrix4f(),
    mMat = new Matrix4f(), 
    mvMat = new Matrix4f();
    
    
    private static void setupVertices() {
        float vertexPositions[] = {
            -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
            1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
            1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
        };

        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);
        glGenBuffers(vbo);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexPositions, GL_STATIC_DRAW);
    }
    
    private static void init(long windowHandle) {
        renderingProgram = createShaderProgram("src/shaders/default.vert", "src/shaders/default.frag");
        cameraX = 0.0f; cameraY = 0.0f; cameraZ = 395.0f;
	    cubeLocX = 0.0f; cubeLocY = -2.0f; cubeLocZ = 0.0f;
        cubeRotX = 0.0f; cubeRotY = 0.0f; cubeRotZ = 0.0f;

        glfwGetFramebufferSize(windowHandle, width, height);
        aspect = (float)width.get(0) / (float)height.get(0);
        pMat.setPerspective(Math.toRadians(90f), aspect, 0.1f, 1500.0f);

	    setupVertices();
        
    }
    
    private static void display(long windowHandle, double currentTime) {
        glClearColor(0, 0, 0, 1);
        glClear(GL_DEPTH_BUFFER_BIT);
        glClear(GL_COLOR_BUFFER_BIT);
        glUseProgram(renderingProgram);

        vLoc = glGetUniformLocation(renderingProgram, "v_matrix");
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix");

        vMat = new Matrix4f().translate(new Vector3f(-cameraX, -cameraY, -cameraZ));
        
        MemoryStack stack = MemoryStack.stackPush();
        FloatBuffer fb = stack.mallocFloat(16);  

        vMat.get(0, fb);
        glUniformMatrix4fv(vLoc, false, fb);
        pMat.get(0, fb);
        glUniformMatrix4fv(projLoc, false, fb);

        float timeFactor = (float)currentTime;
        int tfLoc = glGetUniformLocation(renderingProgram, "tf");
        glUniform1f(tfLoc, timeFactor);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glDrawArraysInstanced(GL_TRIANGLES, 0, 36, 100000);

        stack.close();
    }

    public static void main(String[] args) {
        if (!glfwInit()) {System.exit(-1);}
        glfwWindowHint(GLFW_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_VERSION_MINOR, 6);
        windowHandle = glfwCreateWindow(1000, 1000, "chap4", NULL, NULL);
        if (windowHandle == NULL) {System.exit(-1);}
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();  //super important lmao (lwjgl quirk)
        glfwSwapInterval(1);
        
        init(windowHandle);

        while(!glfwWindowShouldClose(windowHandle)) {
            display(windowHandle, glfwGetTime());
            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }

        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
}
