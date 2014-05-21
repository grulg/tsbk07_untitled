#ifdef GL_ES\n
precision mediump float;
#endif

varying vec3 v_normal;

void main()
{
	vec3 lDir = vec3(0.0, 1.0, 0.0);
	vec3 col = vec3(0.5, 0.5, 0.5);
	float ints = max(dot(lDir, v_normal), 0.0);
	
	
	gl_FragColor = ints*vec4(col, 1.0);
}
