#version 460

in vec4 varyingColor;
in vec2 tc;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform int doTexture;

layout (binding=0) uniform sampler2D samp;

void main(void)
{	
    if (doTexture == 1) {
        color = texture(samp, tc);
    }
    if (doTexture == 0) {
        color = varyingColor;
    }
   
}