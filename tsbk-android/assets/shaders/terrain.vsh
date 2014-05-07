attribute vec3 a_position;
attribute vec3 a_normal;

uniform mat4 u_projection;

varying vec3 v_normal;

void main()
{
	gl_Position = u_projection * vec4(a_position, 1.0f);
	v_normal = a_normal;
}
