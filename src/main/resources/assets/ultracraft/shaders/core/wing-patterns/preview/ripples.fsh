#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform float GameTime;
uniform vec3 WingColor;
uniform vec3 MetalColor;

uniform vec4 ColorModulator;

in float vertexDistance;
in vec4 vertexColor;
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

float frac(float v)
{
    return v - floor(v);
}

float getRandom(vec2 v)
{
    return frac(sin(dot(v, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 getRed(float brightness)
{
    vec3 colA = rgb2hsv(WingColor / 255f) * vec3(360, 100, 100);
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
    vec3 colB = rgb2hsv(MetalColor / 255f) * vec3(360, 100, 100);
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

void main()
{
    vec4 colorIn = texture(Sampler0, texCoord0);
    vec4 color = colorIn;
    if (color.a < 0.1)
        discard;
    vec2 pixelUV = round((texCoord0 + vec2(1f / (76f * 2), 1f / (40f * 2))) * vec2(76f, 40f)) / vec2(76f, 40f) + vec2(1f / (76f * 2), 1f / (40f * 2));
    color.rgb = hsv2rgb(getRed(colorIn.r).rgb / vec3(360f, 100f, 100f));
    for(int i = 0; i < 3; i++)
    {
        float time = mod(GameTime * -300 + distance(pixelUV, vec2(-0.25, 0.25)), 2f);
        color.rgb = mix(color.rgb, vec3(1, 1, 1), (time > 1.9 - 0.125 * i && time < 2 - 0.15 * i) ? 1 - distance(time, 1.975f - 0.125 * i) * 10: 0f);
        time = mod(GameTime * -300 + distance(pixelUV, vec2(0.75, 0.25)), 2f);
        color.rgb = mix(color.rgb, vec3(1, 1, 1), (time > 0.9 - 0.125 * i && time < 1 - 0.15 * i) ? 1 - distance(time, 0.975f - 0.125 * i) * 10: 0f);
    }
    color.rgb = mix(color.rgb, hsv2rgb(getBlue(colorIn.b).rgb / vec3(360f, 100f, 100f)), colorIn.b > 0f);
    color.rgb = mix(color.rgb, vec3(colorIn.g, colorIn.g, colorIn.g), colorIn.g > 0f);
    fragColor = color* ColorModulator;
}