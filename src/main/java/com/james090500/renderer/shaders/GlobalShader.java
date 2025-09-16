package com.james090500.renderer.shaders;

public class GlobalShader {

    public static final String FOG_METHOD =
        """
            // calculate fog: pos is view-space or world-space RELATIVE to camera
            vec4 calcFog(vec3 pos, vec4 colour) {
                vec3 u_fogColor = vec3(0.529, 0.808, 0.922);
                float u_fogStart = 128.0;
                float u_fogDensity = 0.005;
        
                // cylindrical distance (ignore Y)
                float horizontalDist = length(vec2(pos.x, pos.z));
            
                // subtract start distance so fog only applies after u_fogStart
                float d = max(0.0, horizontalDist - u_fogStart);
            
                // exponential-squared falloff (smooth, nice for large distances)
                // fogAmt goes from 0.0 (no fog) -> 1.0 (full fog)
                float fogAmt = 1.0 - exp(-u_fogDensity * d * d);
                fogAmt = clamp(fogAmt, 0.0, 1.0);
            
                // visibility = 1.0 near (no fog), 0.0 far (fully fogged)
                float visibility = 1.0 - fogAmt;
            
                vec3 result = mix(u_fogColor, colour.rgb, visibility);
                return vec4(result, colour.a);
            }
        """;
}
