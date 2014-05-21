#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main(){
	gl_FragColor = texture2D(u_texture, v_texCoords);
	//gl_FragColor = vec4(0.9, 0.8, 0.2, 0.6);
	//gl_FragColor = vec4(1.0, 1.0, 0.0, 0.6);
}
