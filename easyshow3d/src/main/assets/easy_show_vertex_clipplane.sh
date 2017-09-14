uniform mat4 uMVPMatrix; //总变换矩阵
uniform mat4 uMMatrix; //变换矩阵
uniform vec3 uLightLocation;  //光源位置
uniform vec3 uCamera; //摄像机位置
attribute vec3 aPosition;  //顶点位置
attribute vec3 aNormal;    //顶点法向量
uniform vec4 u_clipPlane;//剪裁平面
//用于传递给片元着色器的变量
varying vec4 ambient;
varying vec4 diffuse;
varying vec4 specular;
varying float u_clipDist;

//定位光光照计算的方法
void pointLight(          //定位光光照计算的方法
  in vec3 normal,       //法向量
  inout vec4 ambient,     //环境光最终强度
  inout vec4 diffuse,       //散射光最终强度
  inout vec4 specular,      //镜面光最终强度
  in vec3 lightLocation,      //光源位置
  in vec4 lightAmbient,     //环境光强度
  in vec4 lightDiffuse,     //散射光强度
  in vec4 lightSpecular     //镜面光强度
){
  ambient=lightAmbient;     //直接得出环境光的最终强度  
  vec3 normalTarget=aPosition+normal; //计算变换后的法向量
  vec3 newNormal=(uMMatrix*vec4(normalTarget,1)).xyz-(uMMatrix*vec4(aPosition,1)).xyz;
  newNormal=normalize(newNormal);   //对法向量规格化
  //计算从表面点到摄像机的向量
  vec3 eye= normalize(uCamera-(uMMatrix*vec4(aPosition,1)).xyz);  
  //计算从表面点到光源位置的向量vp
  vec3 vp= normalize(lightLocation-(uMMatrix*vec4(aPosition,1)).xyz);  
  vp=normalize(vp);//格式化vp
  vec3 halfVector=normalize(vp+eye);  //求视线与光线的半向量    
  float shininess=50.0;       //粗糙度，越小越光滑
  float nDotViewPosition=max(0.0,dot(newNormal,vp));  //求法向量与vp的点积与0的最大值
  diffuse=lightDiffuse*nDotViewPosition;        //计算散射光的最终强度
  float nDotViewHalfVector=dot(newNormal,halfVector); //法线与半向量的点积 
  float powerFactor=max(0.0,pow(nDotViewHalfVector,shininess));   //镜面反射光强度因子
  specular=lightSpecular*powerFactor;         //计算镜面光的最终强度
}


void main()     
{ 
   gl_Position = uMVPMatrix * vec4(aPosition,1); //根据总变换矩阵计算此次绘制此顶点位置  
   pointLight(normalize(aNormal),ambient,diffuse,specular,
   uLightLocation,vec4(0.1,0.1,0.1,1.0),vec4(0.7,0.7,0.7,1.0),vec4(0.3,0.3,0.3,1.0));

   //将顶点位置(x0,y0,z0)代入平面方程Ax+By+Cz+D=0,
   //若Ax0+By0+Cz0+D>0，则顶点在平面的上侧，反之在平面的下侧
   u_clipDist = dot(aPosition.xyz, u_clipPlane.xyz) +u_clipPlane.w;
}                      