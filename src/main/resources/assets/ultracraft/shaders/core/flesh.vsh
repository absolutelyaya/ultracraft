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
    float scale = 0.05;
    float timer = GameTime * 1500.0;
    float offsetx = sin(round(Position.y / 0.2446) + timer * 1.144) - cos(round(Position.z / 1.2) + timer * 0.6);
    float offsety = sin(round(Position.x / 0.5) + timer * 1.2) - cos(round(Position.z / 0.666) + timer) + sin(round(Position.z / 4f) + timer * 0.24);
    float offsetz = sin(round(Position.x / 0.722) + timer) - cos(round(Position.z / 0.85) + timer * 1.1);
    pos += vec3(offsetx, offsety, offsetz) * scale;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    vertexDistance = fog_distance(ModelViewMat, pos, FogShape);
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
    normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
}
