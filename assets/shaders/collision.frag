#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_mask;

uniform vec2 u_uvOffset;
uniform vec2 u_uvScale;

void main() {
    vec4 sceneColor = texture2D(u_texture, v_texCoords);
    vec2 maskCoords = u_uvOffset + (v_texCoords * u_uvScale);
    vec4 maskColor = texture2D(u_mask, maskCoords);

    gl_FragColor = vec4(sceneColor.r + maskColor.r / 2., sceneColor.g + maskColor.g / 2., sceneColor.b + maskColor.b / 2., sceneColor.a);
}