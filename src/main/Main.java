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
import org.joml.Matrix4fStack;
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
    private static float pyrLocX, pyrLocY, pyrLocZ;
    
    //shader and display variables
    private static int mvLoc, projLoc;
    private static IntBuffer width = BufferUtils.createIntBuffer(1);
	private static IntBuffer height = BufferUtils.createIntBuffer(1);
    private static int newWidth;
    private static int newHeight;
    private static float aspect;
    private static Matrix4f pMat = new Matrix4f(), 
    vMat = new Matrix4f(), 
    tMat = new Matrix4f(),
    rMat = new Matrix4f(),
    mMat = new Matrix4f(), 
    mvMat = new Matrix4f();
    private static Matrix4fStack mvStack = new Matrix4fStack(10);
    
    
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
        float pyramidPositions[] = {
            -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
		    1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
		    1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
		    -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
		    -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
		    1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
	    };

        glGenVertexArrays(vao);
        glBindVertexArray(vao[0]);
        glGenBuffers(vbo);

        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexPositions, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
        glBufferData(GL_ARRAY_BUFFER, pyramidPositions, GL_STATIC_DRAW);
    }
    
    private static void init(long windowHandle) {
        renderingProgram = createShaderProgram("src/shaders/default.vert", "src/shaders/default.frag");
        
        glfwGetFramebufferSize(windowHandle, width, height);
        aspect = (float)width.get(0) / (float)height.get(0);
        pMat.setPerspective(Math.toRadians(90f), aspect, 0.1f, 1000.0f);
        
        cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
	    setupVertices();
        
    }
    
    private static void display(long windowHandle, double currentTime) {
        glClear(GL_DEPTH_BUFFER_BIT);
        glClearColor(0, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        glUseProgram(renderingProgram);

        mvLoc = glGetUniformLocation(renderingProgram, "mv_matrix");
        projLoc = glGetUniformLocation(renderingProgram, "proj_matrix");

        vMat = new Matrix4f().translate(new Vector3f(-cameraX, -cameraY, -cameraZ));
        mvStack.set(vMat);
        
        MemoryStack stack = MemoryStack.stackPush();
        FloatBuffer fb = stack.mallocFloat(16);  

        pMat.get(0, fb);
        glUniformMatrix4fv(projLoc, false, fb);
        
        glEnable(GL_CULL_FACE);

        //draw pyramid sun
        mvStack.pushMatrix();
        mvStack.translate(new Vector3f(0.0f, 0.0f, 0.0f));
        mvStack.pushMatrix();
        mvStack.rotateXYZ(new Vector3f((float)currentTime,0.0f,0.0f));
        mvStack.get(0, fb);
        glUniformMatrix4fv(mvLoc, false, fb);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
	    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(0);
        glEnable(GL_DEPTH_TEST);
	    glDepthFunc(GL_LEQUAL);
        glFrontFace(GL_CCW);
	    glDrawArrays(GL_TRIANGLES, 0, 18);
        mvStack.popMatrix();

        //draw cube planet
        mvStack.pushMatrix();
        mvStack.translate(new Vector3f(Math.sin((float)currentTime)*4.0f, 0.0f, Math.cos((float)currentTime)*4.0f));
        mvStack.pushMatrix();
        mvStack.rotateXYZ(new Vector3f(0.0f, (float)currentTime, 0.0f));
        mvStack.get(0, fb);
        glUniformMatrix4fv(mvLoc, false, fb);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glFrontFace(GL_CW);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        mvStack.popMatrix();

        //draw cube moon
        mvStack.pushMatrix();
        mvStack.translate(new Vector3f(Math.cos((float)currentTime)*2.0f, Math.sin((float)currentTime)*2.0f, 0.0f));
        mvStack.rotateXYZ(new Vector3f(0.0f, 0.0f, (float)currentTime*2.0f));
        mvStack.scale(new Vector3f(0.25f, 0.25f, 0.25f));
        mvStack.get(0, fb);
        glUniformMatrix4fv(mvLoc, false, fb);
        glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glFrontFace(GL_CW);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        mvStack.popMatrix(); mvStack.popMatrix(); mvStack.popMatrix();


        stack.close();
    }

    private static GLFWFramebufferSizeCallback window_size_callback = new GLFWFramebufferSizeCallback() {
        @Override
        public void invoke(long windowHandle, final int width, final int height) {
            aspect = (float)width / (float)height;
            glViewport(0, 0, width, height);
            pMat.setPerspective(Math.toRadians(90f), aspect, 0.1f, 1000.0f);
        }
    };

    public static void main(String[] args) {
        if (!glfwInit()) {System.exit(-1);}
        glfwWindowHint(GLFW_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_VERSION_MINOR, 6);
        windowHandle = glfwCreateWindow(1000, 1000, "chap4", NULL, NULL);
        if (windowHandle == NULL) {System.exit(-1);}
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();  //super important lwjgl thing
        glfwSwapInterval(1);
        
        glfwSetFramebufferSizeCallback(windowHandle, window_size_callback);

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
