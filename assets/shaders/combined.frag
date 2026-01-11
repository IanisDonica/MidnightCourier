#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

// Fog Uniforms
uniform vec2 u_playerWorldPos;
uniform vec2 u_camWorldPos;
uniform vec2 u_worldViewSize;
uniform float u_radiusWorld;

uniform int u_noireMode;

void main() {
    // 1. Get the base color from the scene FBO
    vec4 sceneColor = texture2D(u_texture, v_texCoords);

    if (u_noireMode == 1) {
        float gray = (sceneColor.r + sceneColor.g + sceneColor.b) / 3.0;
        sceneColor = vec4(vec3(gray), sceneColor.a);
    }

    // 2. Calculate Fog Effect
    float aspect = u_worldViewSize.x / u_worldViewSize.y;
    vec2 centered = v_texCoords - vec2(0.5);
    vec2 correctedCoords = vec2(centered.x * aspect, centered.y);
    vec2 pixelWorldPos = u_camWorldPos + (correctedCoords * u_worldViewSize.y);
    float dist = distance(pixelWorldPos, u_playerWorldPos);
    float vignette = smoothstep(0.0, u_radiusWorld, dist);
    vignette = pow(vignette, 0.7); // Make it darker sooner
    vec3 fogColor = sceneColor.rgb * (1.0 - vignette);

    // 3. Calculate Collision/Mask Effect
    // The user wants to remove the effect where collision blocks are darker.
    // We'll just use a uniform dark color for everything outside the fog.
    vec3 collisionColor = vec3(0.0, 0.0, 0.0); // Pure black outside fog

    // 4. Combine them
    // If we are within the fog radius, show fogColor (which includes the vignette).
    // If we are outside (isFoggedOut), show the darkened collisionColor.
    float isFoggedOut = step(length(fogColor), 0.001);
    gl_FragColor = vec4(mix(fogColor, collisionColor, isFoggedOut), sceneColor.a);
}
