#version 460

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

in vec4 varyingColor;

out vec4 color;

void main(void){
    color = varyingColor;
}