attribute vec3 a_position, barycentric;

uniform mat4 u_combinedMat;

varying vec3 vBC;

void main() {
	vBC = barycentric;
	gl_Position = u_combinedMat * vec4(a_position, 1.0);
}

