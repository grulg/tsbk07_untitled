attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_combinedMat; // combined projection and view matrix
uniform mat4 u_modelViewMat;
uniform mat4 u_rotationMat;
uniform vec3 u_lDir;

varying vec3 v_eyeVector;
varying vec3 v_lightDirection;
varying vec2 v_texCoord;


void main(void)
{
	v_texCoord = 0.01*a_position.xz;

	//vec3 lightSourcePos = vec3(20.0, 20.0, 20.0); //TODO: Hard coded light source for now
	vec3 eyeVector = u_modelViewMat * a_position;

	v_eyeVector = normalize(eyeVector);
	v_lightDirection = normalize(-u_lDir);

	gl_Position = u_combinedMat * a_position;
}
