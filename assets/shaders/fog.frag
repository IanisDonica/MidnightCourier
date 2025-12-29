#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform vec2 u_playerWorldPos;
uniform vec2 u_camWorldPos;
uniform vec2 u_worldViewSize;
uniform float u_radiusWorld;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    float aspect = u_worldViewSize.x / u_worldViewSize.y;
    vec2 centered = v_texCoords - vec2(0.5);
    vec2 correctedCoords = vec2(centered.x * aspect, centered.y);
    vec2 pixelWorldPos = u_camWorldPos + (correctedCoords * u_worldViewSize.y);
    float dist = distance(pixelWorldPos, u_playerWorldPos);
    float vignette = smoothstep(u_radiusWorld - 3.0, u_radiusWorld, dist);
    gl_FragColor = vec4(texColor.rgb * (1.0 - vignette), texColor.a);
}