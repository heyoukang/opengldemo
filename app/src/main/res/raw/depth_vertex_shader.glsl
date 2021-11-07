attribute vec3 a_Position;
uniform mat4 u_LightSpaceMatrix;
uniform mat4 u_MMatrix;
void main()
{
    gl_Position = u_LightSpaceMatrix * u_MMatrix * vec4(a_Position, 1.0);
}