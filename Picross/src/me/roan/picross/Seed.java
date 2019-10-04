package me.roan.picross;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class Seed{
	private long seed;
	private int width;
	private int height;
	private double density;
	
	public Seed(int width, int height, double density){
		this(ThreadLocalRandom.current().nextLong(), width, height, density);
	}
	
	public Seed(long seed, int width, int height, double density){
		this.seed = seed;
		this.width = width;
		this.height = height;
		this.density = density;
	}
	
	public Seed(String data){
		byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		buffer = Base64.getDecoder().decode(buffer);
		seed = buffer.getLong();
		width = buffer.getInt();		
		height = buffer.getInt();
		density = buffer.getDouble();
	}
	
	@Override
	public String toString(){
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 4 + 8);
		buffer.putLong(seed);
		buffer.putInt(width);
		buffer.putInt(height);
		buffer.putDouble(density);
		buffer.flip();
		return new String(Base64.getEncoder().encode(buffer).array(), StandardCharsets.UTF_8);
	}
}
