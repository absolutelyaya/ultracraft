#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

float near = 0.1;
float far = 1000.0;
float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    for(float r = -Radius; r <= Radius; r += 1.0)
    {
        //float depth = texture(DiffuseDepthSampler, texCoord + oneTexel * r * BlurDir).r;
        //if(depth.r > 0f)
        //    continue;
        vec4 sampleValue = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);

		// Accumulate average alpha
        totalAlpha = totalAlpha + sampleValue.a;
        totalSamples = totalSamples + 1.0;

		// Accumulate smoothed blur
        float strength = 1.0 - abs(r / Radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sampleValue;
    }
    float depth = linearizeDepth(texture(DiffuseDepthSampler, texCoord).r);
    if(depth > 0)
        fragColor = vec4(depth, depth, depth, 1f);
    fragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);
}
