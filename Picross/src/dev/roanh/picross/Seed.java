/*
 * Picross: A nonogram puzzle program. 
 * Copyright (C) 2019  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/Picross
 *
 * Picross is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Picross is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.picross;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class that holds all the information required
 * to define a specific game board.
 * @author Roan
 */
public class Seed{
	/**
	 * The seed for the random number generator.
	 */
	public final long seed;
	/**
	 * The number of columns.
	 */
	public final int width;
	/**
	 * The number of rows.
	 */
	public final int height;
	/**
	 * The tile density of the board.
	 */
	public final double density;
	
	/**
	 * Constructs a new seed with the given
	 * width, height and density. The random
	 * number generator seed is randomised.
	 * @param width The number of columns.
	 * @param height The number of rows.
	 * @param density The density of the board.
	 */
	public Seed(int width, int height, double density){
		this(ThreadLocalRandom.current().nextLong(), width, height, density);
	}
	
	/**
	 * Constructs a new seed with the given
	 * width, height, density and random number
	 * generator seed.
	 * @param seed The seed for the random number generator.
	 * @param width The number of columns.
	 * @param height The number of rows.
	 * @param density The density of the board.
	 */
	public Seed(long seed, int width, int height, double density){
		this.seed = seed;
		this.width = width;
		this.height = height;
		this.density = density;
	}
	
	/**
	 * Constructs a new seed from its string form.
	 * @param data The seed to decode.
	 * @throws IllegalArgumentException If the given seed is not valid.
	 */
	public Seed(String data){
		try{
			byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			buffer = Base64.getDecoder().decode(buffer);
			seed = buffer.getLong();
			width = buffer.getInt();		
			height = buffer.getInt();
			density = buffer.getDouble();
		}catch(Exception e){
			throw new IllegalArgumentException("Invalid seed");
		}
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
