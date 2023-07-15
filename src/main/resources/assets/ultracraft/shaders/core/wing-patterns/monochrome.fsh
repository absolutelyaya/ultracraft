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

    vec3 col = mix(vec3(0, 0, 0), WingColor / 255 * colorIn.r, colorIn.r > 0 ? 1.0 : 0.0);
    col = mix(col, MetalColor / 255 * colorIn.b, colorIn.b > 0 ? 1.0 : 0.0);
    col = mix(col, vec3(col.g, col.g, col.g), colorIn.g > 0 ? 1.0 : 0.0);
    float brightness = (col.r + col.g + col.b) / 3;
    color.rgb = vec3(brightness, brightness, brightness);
    vec4 v = vertexColor;
    vec4 light = lightMapColor;
    if(colorIn.r > 0)
    {
        light = vec4(1, 1, 1, 1);
        v = vec4(1, 1, 1, 1);
    }
    color *= v * ColorModulator;
    color *= light;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}