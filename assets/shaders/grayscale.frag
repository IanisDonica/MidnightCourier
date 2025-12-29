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
    vec3 av = vec3((texColor.r + texColor.g + texColor.b) / 3.0);
    gl_FragColor = vec4(av, texColor.a);
}