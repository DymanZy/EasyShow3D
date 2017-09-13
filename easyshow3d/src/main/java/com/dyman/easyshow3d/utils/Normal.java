package com.dyman.easyshow3d.utils;

import java.util.Set;

//表示法向量的类，此类的一个对象表示一个法向量
public class Normal
{
	public static final float DIFF=0.0000001f;//判断两个法向量是否相同的阈值
	//法向量在XYZ轴上的分量
	float nx;
	float ny;
	float nz;

	public Normal(float nx, float ny, float nz)
	{
		this.nx=nx;
		this.ny=ny;
		this.nz=nz;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof  Normal)
		{//若两个法向量XYZ三个分量的差都小于指定的阈值则认为这两个法向量相等
			Normal tn=(Normal)o;
			if(Math.abs(nx-tn.nx)<DIFF&&
					Math.abs(ny-tn.ny)<DIFF&&
					Math.abs(ny-tn.ny)<DIFF
					)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	//由于要用到HashSet，因此一定要重写hashCode方法
	@Override
	public int hashCode()
	{
		return 1;
	}

	//求法向量平均值的工具方法
	public static float[] getAverage(Set<Normal> sn)
	{
		//存放法向量和的数组
		float[] result=new float[3];
		//把集合中所有的法向量求和
		for(Normal n:sn)
		{
			result[0]+=n.nx;
			result[1]+=n.ny;
			result[2]+=n.nz;
		}
		//将求和后的法向量规格化
		return LoadUtil.vectorNormal(result);
	}
}
