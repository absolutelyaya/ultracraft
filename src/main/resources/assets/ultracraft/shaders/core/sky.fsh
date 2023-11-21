#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
uniform sampler2D Sampler4;
uniform sampler2D Sampler5;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

uniform mat4 RotMat;

in vec4 glPos;

out vec4 fragColor;

vec2 sampleCube(vec3 v, out int faceIndex) {
    vec3 abs = abs(v);
    float cubiClamp; //used to keep the skybox a unit box
    vec2 uv;
    if(abs.z >= abs.x && abs.z >= abs.y) {
        faceIndex = v.z < 0 ? 1 : 3;
        cubiClamp = 0.5 / abs.z;
        uv = vec2(v.z < 0.0 ? -v.x : v.x, -v.y);
    } else if(abs.y >= abs.x) {
        faceIndex = v.y < 0 ? 5 : 0;
        cubiClamp = 0.5 / abs.y;
        uv = vec2(-v.x, v.y < 0.0 ? v.z : -v.z);
    } else {
        faceIndex = v.x < 0 ? 4 : 2;
        cubiClamp = 0.5 / abs.x;
        uv = vec2(v.x < 0.0 ? v.z : -v.z, -v.y);
    }
    return uv * cubiClamp + 0.5;
}

void main() {
    float near = 0.05;
    float far = (ProjMat[2][2]-1.)/(ProjMat[2][2]+1.) * near;
    int faceIndex = 0;
    vec4 texPos = vec4(sampleCube(normalize((inverse(ProjMat * RotMat) * vec4(glPos.xy / glPos.w * (far - near), far + near, far - near)).xyz), faceIndex), 1.0, 1.0);
    texPos = vec4(-texPos.x, texPos.y, texPos.z, texPos.w);

    vec4 color;
    switch(faceIndex)
    {
        case 0:
            color = textureProj(Sampler0, texPos);
            break;
        case 1:
            color = textureProj(Sampler1, texPos);
            break;
        case 2:
            color = textureProj(Sampler2, texPos);
            break;
        case 3:
            color = textureProj(Sampler3, texPos);
            break;
        case 4:
            color = textureProj(Sampler4, texPos);
            break;
        case 5:
            color = textureProj(Sampler5, texPos);
            break;
    }
    fragColor = color;
}