attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_worldMat;
uniform mat4 u_viewMat;
uniform mat4 u_projectionMat;

varying vec2 v_texCoords;

void main(){
	gl_Position = u_projectionMat * u_viewMat * u_worldMat * a_position;
	v_texCoords = a_texCoord0;
}
