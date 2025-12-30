#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_mask;

void main() {
    vec4 sceneColor = texture2D(u_texture, v_texCoords);
    vec4 maskColor = texture2D(u_mask, v_texCoords);

    if (maskColor.rgb == vec3(0,0,0)) {
        gl_FragColor = sceneColor;
    } else {
        gl_FragColor = maskColor;
    }
}