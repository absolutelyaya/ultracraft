#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec2 Tiling;

in vec2 texCoord0;

out vec4 fragColor;

float getBrightness(vec3 col)
{
    return (col.r + col.g + col.b) / 3f;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    float br = pow(getBrightness(color.rgb), 3) * 8;
    color.a = (round((texCoord0.x / Tiling.x * Tiling.y + 0.5)) / Tiling.y);
    fragColor = color * ColorModulator;
}
