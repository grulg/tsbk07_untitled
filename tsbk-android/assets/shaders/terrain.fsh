#ifdef GL_ES
precision mediump float;
#endif

attribute vec3 a_normal;
attribute vec3 a_position;



uniform sampler2D tex;

varying vec3 v_normal;
varying vec3 w_pos;

void main()
{
	vec3 lDir = vec3(0.0, 1.0, 1.0);
	lDir = normalize(lDir);
	
	vec3 blend = max(abs(a_normal)-vec3(0.1,0.1,0.1),vec3(0.0,0.0,0.0));
	blend = normalize(max(blend, 0.00001));
	float b = blend.x+blend.y+blend.z;
	blend = blend/b;

	vec4 xt = texture2D(tex, 0.2*w_pos.yz);
	vec4 yt = texture2D(tex, 0.2*w_pos.xz);
	vec4 zt = texture2D(tex, 0.2*w_pos.xy);
	
	vec4 col = xt*blend.x+yt*blend.y+zt*blend.z;
	
	float ints = max(dot(lDir, v_normal), 0.2);

	gl_FragColor = ints*col;
}
