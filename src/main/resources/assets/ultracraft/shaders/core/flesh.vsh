#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;

uniform float GameTime;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

void main() {
    vec3 pos = Position + ChunkOffset;
    vec3 p = sin(mod(Position, 16.0) / 8.0) * 16.0;
    float scale = 0.02;
    float timer = GameTime * 1500.0;
    float offsetx = sin(round(p.y / 0.2446) + timer * 1.144) - cos(round(p.z / 1.2) + timer * 0.6);
    float offsety = sin(round(p.x / 0.5) + timer * 1.2) - cos(round(p.z / 0.666) + timer) + sin(round(p.z / 4.0) + timer * 0.24);
    float offsetz = sin(round(p.x / 0.722) + timer) - cos(round(p.z / 0.85) + timer * 1.1);
    pos += vec3(offsetx, offsety, offsetz) * scale;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(ModelViewMat, pos, FogShape);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
