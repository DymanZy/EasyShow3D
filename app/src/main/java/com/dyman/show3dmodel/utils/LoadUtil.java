package com.dyman.show3dmodel.utils;

public class LoadUtil
{
	//求两个向量的叉积
	public static float[] getCrossProduct(float x1,float y1,float z1,float x2,float y2,float z2)
	{
		//求出两个矢量叉积矢量在XYZ轴的分量ABC
		float A=y1*z2-y2*z1;
		float B=z1*x2-z2*x1;
		float C=x1*y2-x2*y1;

		return new float[]{A,B,C};
	}

	//向量规格化
	public static float[] vectorNormal(float[] vector)
	{
		//求向量的模
		float module=(float)Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
		return new float[]{vector[0]/module,vector[1]/module,vector[2]/module};
	}

}
