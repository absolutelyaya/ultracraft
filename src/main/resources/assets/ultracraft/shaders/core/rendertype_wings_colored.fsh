#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform float GameTime;

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

vec3 rgb2hsv_2(vec3 c){
    vec4 K=vec4(0.,-1./3.,2./3.,-1.),
    p=mix(vec4(c.bg ,K.wz),vec4(c.gb,K.xy ),step(c.b,c.g)),
    q=mix(vec4(p.xyw,c.r ),vec4(c.r ,p.yzx),step(p.x,c.r));
    float d=q.x-min(q.w,q.y),
    e=1e-10;
    return vec3(abs(q.z+(q.w-q.y)/(6.*d+e)),d/(q.x+e),q.x);
}

vec3 getRed(float brightness)
{
    vec3 col = vec3(64, 39, 100);
    float step = brightness * 4;
    if(step >= 3)
        return col;
    else if(step >= 2)
        return col + vec3(-11, 20, 0);
    else if(step >= 1)
        return col + vec3(-22, 40, 0);
    else
        return col + vec3(-30, 60, 0);
}

vec3 getBlue(float brightness)
{
    vec3 col = vec3(223, 54, 100);
    float step = brightness * 4;
    if(step >= 3)
        return col;
    else if(step >= 2)
        return col + vec3(9, 10, 0);
    else if(step >= 1)
        return col + vec3(11, 14, -19);
    else
        return col + vec3(12, 14, -46);
}

void main() {
    vec4 colorIn = texture(Sampler0, texCoord0);
    vec4 color = colorIn;
    if (color.a < 0.1) {
        discard;
    }
    float time = mod(abs(GameTime * 600) + texCoord0.g, 1f);
    //color *= vertexColor * ColorModulator;
    //color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color.rgb = mix(vec3(0f, 0f, 0f), hsv2rgb(getRed(colorIn.r).rgb / vec3(360f, 100f, 100f) + vec3(time, 0, 0)), colorIn.r > 0f);
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