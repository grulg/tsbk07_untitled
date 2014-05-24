#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_dudvTex;
uniform sampler2D u_normalTex;
uniform sampler2D u_waterTex;

uniform float time;

varying vec3 v_eyeVector;
varying vec3 v_lightDirection;
varying vec2 v_texCoord;

// Return the average of the three components in a vec3
float get_weight(vec3 pixel)
{
  return (pixel.r+pixel.g+pixel.b)/3.0;
}
// Return the average of the two components in a vec2
float get_weight(vec2 v)
{
  return (v.x+v.y)/2.0;
}
// Simple "random" function
float rand(vec2 co)
{
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
	// Used for setting a range minVariance..maxVariance for how much the normal bump can vary
	float maxVariance = 2.0;
    float minVariance = maxVariance/2.0;

	// Movement speed of the water
	float waterSpeed = time / 30.0;
	
	// Texel of the dudv and normal maps
	vec4 dudvTexl = texture2D(u_dudvTex, v_texCoord*2+waterSpeed); 
	vec4 normalTexl = texture2D(u_normalTex, v_texCoord*2+waterSpeed);

	// Used in the interpolation below to simulate cool wave-like behaviour
	float randomWeight1 = -get_weight(dudvTexl.rgb)*25;
    float randomWeight2 = get_weight(normalTexl.rgb)*50;
	
	// Used to change the speed of the normal map texture
	float interpolation = (sin(time*10+randomWeight1+randomWeight2)+1.0)/2.0; 
	
	vec4 normalTexel = vec4(mix(dudvTexl.rgb, normalTexl.rgb, interpolation), 1.0);
	
	vec3 bumpNormal = normalize(normalTexel.rgb*maxVariance-minVariance);
	
	// Normalize the light direction so that it can fulfill it's intended use later.
	vec3 lightDirection = normalize(v_lightDirection);

	float shininess = 0.5;
	// Completely arbitrary color of the bump for now
	vec4 bumpColor = vec4(0.65,0.8,1.0,1.0)*0.23;
	// Standard diffuse component of the water bump
	float bumpDiffuse = max(dot(bumpNormal, lightDirection), 0.0);
	
	// If the bump diffuse resulted in 0.0, don't bother processing it further
	if(bumpDiffuse > 0.0)
    {
      	bumpColor += bumpDiffuse; //*diffuse;
 		// Simplified bump reflection (http://developer.download.nvidia.com/assets/gamedev/docs/ReflectiveBumpMapping.pdf)
      	vec3 bumpReflection = -2*bumpNormal*(dot(lightDirection, bumpNormal)) + lightDirection;
      	float bumpReflectionDotEyeVector = max(dot(bumpReflection,v_eyeVector),0.0);
 		
		// Same as above. If the result was 0.0, don't bother. 
     	if(bumpReflectionDotEyeVector > 0.0)
      	{
      		bumpColor += pow(bumpReflectionDotEyeVector,shininess)*(0.5, 0.5, 0.5, 1.0);//*specular; (0.5, 0.5, 0.5, 1.0) arbitrary for now
      	}
    }
    
    // Hard coded vertex normal, since all our water is completely horizontal.
	vec3 vertexNormal = (0,1,0);
	
    float NormalDotLight = max(dot(vertexNormal,v_lightDirection),0.0);
    vec4 color = (0.1, 0.1, 0.1, 1.0); //TODO: Change to fit sun color later
    // If the Phong shading resulted in 0.0, don't bother processing it further
    if(NormalDotLight > 0.0)
    {
       color += NormalDotLight;//*diffuse
       
        vec3 Reflection = -2*vertexNormal*(dot(v_lightDirection, vertexNormal)) + lightDirection;
        float ReflectionDotEyeVector = max(dot(Reflection, v_eyeVector),0.0);

     	// Same as above. If the result was 0.0, don't bother. 
        if(ReflectionDotEyeVector > 0.0)
        {
           vec4 specularColor = pow(ReflectionDotEyeVector,shininess)*(0.5, 0.5, 0.5, 1.0);//*specular; (0.5, 0.5, 0.5, 1.0) for now
           color += specularColor;
        } 
    }
    
    vec3 refractNormal = refract(v_eyeVector,bumpNormal,0.5);
    vec2 refraction = vec2(refractNormal*0.0362); // Got 0.0362 after some experimentation
    
    vec4 waterTexl = texture2D(u_waterTex, v_texCoord+refraction);
    waterTexl.b = 1.0;
    
    vec3 texelLighted = mix(color.rgb,waterTexl.rgb,0.9995); // The blue is arbitrary
    
	gl_FragColor = vec4(mix(texelLighted.rgb,bumpColor.rgb,0.10),0.8);
}















