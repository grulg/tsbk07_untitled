#ifdef GL_ES\n
precision mediump float;
#endif

varying vec2 v_texCoords;

//uniform sampler2D u_texture;

void main(){
	//gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0) * texture2D(u_texture, v_texCoords);
	gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
