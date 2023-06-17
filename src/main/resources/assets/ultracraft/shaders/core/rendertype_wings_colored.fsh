#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform float GameTime;
uniform vec3 WingColor;
uniform vec3 MetalColor;
uniform int Pattern;

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

vec3 saturate(vec3 v){
    return clamp(v,0.0,1.0);
}

vec3 hsv2rgb(vec3 c){
    vec4 K=vec4(1.,2./3.,1./3.,3.);
    return c.z*mix(K.xxx,saturate(abs(fract(c.x+K.xyz)*6.-K.w)-K.x),c.y);
}

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 getRed(float brightness)
{
    vec3 colA = rgb2hsv(WingColor / vec3(255f, 255f, 255f)) * vec3(360, 100, 100);
    float step = brightness * 4 - 0.1;
    if(step > 3)
        return colA;
    else if(step > 2)
        return colA + vec3(-11, 20, 0);
    else if(step > 1)
        return colA + vec3(-22, 40, 0);
    else
        return colA + vec3(-30, 60, 0);
}

vec3 getBlue(float brightness)
{
    vec3 colB = rgb2hsv(MetalColor / vec3(255f, 255f, 255f)) * vec3(360, 100, 100);
    float step = brightness * 4 - 0.1;
    if(step > 3)
        return colB;
    else if(step > 2)
        return colB + vec3(9, 10, 0);
    else if(step > 1)
        return colB + vec3(11, 14, -19);
    else
        return colB + vec3(12, 14, -46);
}

void main() {
    vec4 colorIn = texture(Sampler0, texCoord0);
    vec4 color = colorIn;
    if (color.a < 0.1) {
        discard;
    }
    float time;
    if(Pattern == 1)
    {
        time = mod(abs(GameTime * 600) + (round((texCoord0.g + 1f / 64f) * 32f)) / 32f + (round((texCoord0.r + 1f / 64f) * 32f)) / 32f, 1f);
        color.rgb = mix(colorIn.rrr / 4, hsv2rgb(vec3(time, 1f, 1f)), colorIn.r > 0.5f);
    }
    else if(Pattern == 2)
    {
        time = sin(abs(GameTime * 600) + (round((texCoord0.g + 1f / 64f) * 32f)) / 32f + (round((texCoord0.r + 1f / 64f) * 32f)) / 32f * (sin(GameTime * 1800)) * 3) / 2f + 0.5f;
        color.rgb = mix(vec3(0f, 0f, 0f), mix(vec3(0.105f, 0.027f, 0.086f), hsv2rgb(getRed(0.5f) / vec3(360f, 100f, 100f))/*vec3(0.105f, 0.494f, 0.956f)*/, pow(time + (mod(round(texCoord0.g * 32f + 0.5f), 2) == 0 ? 0f : 0.5f), 4)), colorIn.r > 0f);
        color.rgb *= (mod(round(texCoord0.g * 32f + 0.5f), 2) == 0 ? 1f : 0.8f);
    }
    else
        color.rgb = mix(vec3(0f, 0f, 0f), hsv2rgb(getRed(colorIn.r).rgb / vec3(360f, 100f, 100f)), colorIn.r > 0f);
    color.rgb = mix(color.rgb, hsv2rgb(getBlue(colorIn.b).rgb / vec3(360f, 100f, 100f)), colorIn.b > 0f);
    color.rgb = mix(color.rgb, vec3(colorIn.g, colorIn.g, colorIn.g), colorIn.g > 0f);
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