package me.roan.picross;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import me.roan.util.Dialog;

public class ImageParser{

	public static final Board parseImage(File file, int w, int h) throws IOException{
		BufferedImage image = ImageIO.read(file);
		
		BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = gray.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		BufferedImage bw = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		
		int[][] datac = new int[w][h];
		
		long sum = 0;
		
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				int black = 0;
				
				for(int dx = x * Math.floorDiv(gray.getWidth(), w); dx < (x + 1) * Math.floorDiv(gray.getWidth(), w); dx++){
					for(int dy = y * Math.floorDiv(gray.getHeight(), h); dy < (y + 1) * Math.floorDiv(gray.getHeight(), h); dy++){
						//System.out.println(dx + " | " + dy + " | " + (gray.get.getRGB(dx, dy) & 0xFF));
						if((gray.getRGB(dx, dy) & 0xFF) < 127){
							bw.setRGB(dx, dy, 0xFF000000);
							black++;
						}else{
							bw.setRGB(dx, dy, 0xFFFFFFFF);
						}
						//bw.setRGB(dx, dy, gray.getRGB(dx, dy));
					}
				}
				
				System.out.println(black + " / " + ((gray.getWidth() / w) * (gray.getHeight() / h)));
//				if(black > (gray.getWidth() / w) * (gray.getHeight() / h) / 2){
//					data[x][y] = true;
//				}
				datac[x][y] = black;
				sum += black;
			}
		}
		
		boolean[][] data = new boolean[w][h];
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				data[x][y] = datac[x][y] > (sum / (w * h));
			}
		}
		
		for(int r = 0; r < data.length; r++){
			//System.out.println(Arrays.toString(data[r]));
			for(int c = 0; c < h; c++){
				System.out.print(data[c][r] ? " x " : "   ");
			}
			System.out.println();
		}
		System.out.println("=====================");
		for(int r = 0; r < data.length; r++){
			//System.out.println(Arrays.toString(data[r]));
			for(int c = 0; c < h; c++){
				System.out.print((datac[c][r] > (gray.getWidth() / w) * (gray.getHeight() / h) / 2) ? " x " : "   ");
			}
			System.out.println();
		}
		
		
		
		
		
		
		
		{
			JFrame f = new JFrame();
			f.add(new JLabel(new ImageIcon(image)));
			f.setVisible(true);
		}
		
		{
			JFrame f = new JFrame();
			f.add(new JLabel(new ImageIcon(gray)));
			f.setVisible(true);
		}
		
		{
			JFrame f = new JFrame();
			f.add(new JLabel(new ImageIcon(bw)));
			f.setVisible(true);
		}
		
		return null;
	}
	
	
	public static void main(String[] args){
		try{
			parseImage(Dialog.showFileOpenDialog(), 10, 10);
		}catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
