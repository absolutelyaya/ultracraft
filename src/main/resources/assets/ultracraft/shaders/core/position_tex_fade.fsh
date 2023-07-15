#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform vec3 Tiling;

in vec2 texCoord0;

out vec4 fragColor;

float getBrightness(vec3 col)
{
    return (col.r + col.g + col.b) / 3;
}

float frac(float v)
{
    return v - floor(v);
}

float getRandom(vec2 v)
{
    return frac(sin(dot(v, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    float br = pow(getBrightness(color.rgb), 3) * 8;
    vec2 pixelCoord = round(texCoord0 * Tiling.xy + 0.5) / Tiling.xy;
    color.a = (getRandom(pixelCoord * (1 + Tiling.z)) - (1 - pixelCoord.x) + 0.5) > 0.5 ? 1 : 0;
    fragColor = color * ColorModulator;
}
