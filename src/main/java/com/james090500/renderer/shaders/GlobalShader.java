package com.james090500.renderer.shaders;

public class GlobalShader {

    public static final String FOG_METHOD =
        """
            struct Fog {
                vec3 color;
                float start;
                float density;
            };
        
            uniform Fog fog;
        
            // calculate fog: pos is view-space or world-space RELATIVE to camera
            vec4 calcFog(vec3 pos, vec4 color) {
                // cylindrical distance (ignore Y)
                float horizontalDist = length(vec2(pos.x, pos.z));
        
                // subtract start distance so fog only applies after fog.start
                float d = max(0.0, horizontalDist - fog.start);
        
                // exponential-squared falloff (smooth, nice for large distances)
                // fogAmt goes from 0.0 (no fog) -> 1.0 (full fog)
                float fogAmt = 1.0 - exp(-fog.density * d * d);
                fogAmt = clamp(fogAmt, 0.0, 1.0);
        
                // visibility = 1.0 near (no fog), 0.0 far (fully fogged)
                float visibility = 1.0 - fogAmt;
        
                vec3 result = mix(fog.color, color.rgb, visibility);
                return vec4(result, color.a);
            }
        """;
}
