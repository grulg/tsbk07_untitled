#ifdef GL_ES\n
precision mediump float;
#endif

varying vec3 v_normal;

void main()
{
	vec3 col = vec3(0.1, 0.2, 0.5);
	
	gl_FragColor = vec4(col, 0.5);
}
