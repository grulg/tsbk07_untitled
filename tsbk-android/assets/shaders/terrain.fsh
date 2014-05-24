#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D tex;
uniform vec3 u_lDir;

varying vec3 v_normal;
varying vec3 w_pos;
varying vec3 v_texWeights;

void main()
{
	vec3 lDir;
	lDir = normalize(u_lDir);
	

	vec4 xt = texture2D(tex, 0.2*w_pos.yz);
	vec4 yt = texture2D(tex, 0.2*w_pos.xz);
	vec4 zt = texture2D(tex, 0.2*w_pos.xy);
	
	vec4 col = xt*v_texWeights.x+yt*v_texWeights.y+zt*v_texWeights.z;
	
	float ints = max(dot(lDir, v_normal), 0.2);

	gl_FragColor = ints*col;
}
