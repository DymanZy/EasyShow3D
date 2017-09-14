package com.dyman.easyshow3d.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LoadUtil {

	//求两个向量的叉积
	public static float[] getCrossProduct(float x1,float y1,float z1,float x2,float y2,float z2) {
		//求出两个矢量叉积矢量在XYZ轴的分量ABC
		float A=y1*z2-y2*z1;
		float B=z1*x2-z2*x1;
		float C=x1*y2-x2*y1;

		return new float[]{A,B,C};
	}

	//向量规格化
	public static float[] vectorNormal(float[] vector) {
		//求向量的模
		float module=(float)Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
		return new float[]{vector[0]/module,vector[1]/module,vector[2]/module};
	}

	public static FloatBuffer fromArrayToBuff(float[] a) {
		ByteBuffer bf = ByteBuffer.allocateDirect(a.length*4);
		bf.order(ByteOrder.nativeOrder());//设置字节顺序
		FloatBuffer result = bf.asFloatBuffer();
		result.put(a);
		result.position(0);
		return result;
	}


	/**
	 * 矫正坐标  坐标圆心移动
	 * @param
	 * @param position
	 */
	public static void adjust_coordinate(float[] vertex_array , int position, float adjust){
		vertex_array[position] -= adjust;
	}

}
