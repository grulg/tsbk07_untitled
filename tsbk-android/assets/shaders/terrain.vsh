attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_projection;

varying vec3 v_normal;
varying vec3 w_pos;

void main()
{
	gl_Position = u_projection * vec4(a_position, 1.0f);
	v_normal = a_normal;
	w_pos = a_position;
}
