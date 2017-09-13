package com.dyman.easyshow3d.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	/**
	 * Convert <code>input</code> stream into byte[].
	 * 
	 * @param input
	 * @return Array of Byte
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}
	
	/**
	 * Copy <code>length</code> size of <code>input</code> stream to <code>output</code> stream.
	 * This method will NOT close input and output stream.
	 * 
	 * @param input
	 * @param output
	 * @return long copied length
	 * @throws IOException
	 */
	private static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	/**
	 * Copy <code>length</code> size of <code>input</code> stream to <code>output</code> stream.
	 * 
	 * @param input
	 * @param output
	 * @return long copied length
	 * @throws IOException
	 */
	public static long copy(InputStream input, OutputStream output, int length) throws IOException {
		byte[] buffer = new byte[length];
		int count = 0;
		int n = 0;
		int max = length;
		while ((n = input.read(buffer, 0, max)) != -1) {
			output.write(buffer, 0, n);
			count += n;
			if (count > length) {
				break;
			}
			
			max -= n;
			if (max <= 0) {
				break;
			}
		}
		return count;
	}
	
	/**
	 * Close <code>closeable</code> quietly.
	 * 
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		
		try {
			closeable.close();
		} catch (Throwable e) {
			System.out.println("文件关闭失败");
		}		
	}
}
