attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_projection;

varying vec3 v_normal;
varying vec3 w_pos;
varying vec3 v_texWeights;

void main()
{
	gl_Position = u_projection * vec4(a_position, 1.0f);
	v_normal = a_normal;
	w_pos = a_position;
	
	vec3 blend = max(abs(a_normal)-vec3(0.1,0.1,0.1),vec3(0.0,0.0,0.0));
	blend = normalize(max(blend, 0.00001));
	float b = blend.x+blend.y+blend.z;
	v_texWeights = blend/b;
}
