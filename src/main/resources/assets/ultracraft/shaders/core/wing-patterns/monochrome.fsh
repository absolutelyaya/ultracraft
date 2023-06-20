#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec3 WingColor;
uniform vec3 MetalColor;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main()
{
    vec4 colorIn = texture(Sampler0, texCoord0);
    vec4 color = colorIn;
    if (color.a < 0.1)
        discard;

    vec3 col = mix(vec3(0, 0, 0), WingColor / 255f * colorIn.r, colorIn.r > 0);
    col = mix(col, MetalColor / 255f * colorIn.b, colorIn.b > 0);
    col = mix(col, vec3(col.g, col.g, col.g), colorIn.g > 0);
    float brightness = (col.r + col.g + col.b) / 3f;
    color.rgb = vec3(brightness, brightness, brightness);
    vec4 v = vertexColor;
    vec4 light = lightMapColor;
    if(colorIn.r > 0f)
    {
        light = vec4(1f, 1f, 1f, 1f);
        v = vec4(1f, 1f, 1f, 1f);
    }
    color *= v * ColorModulator;
    color *= light;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}