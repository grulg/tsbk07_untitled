package se.haegers.tsbk.ehager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class HeightMap 
{
	public HeightMap(int w, int h, NoiseMap ul, NoiseMap ur, NoiseMap lr, NoiseMap ll)
	{
		width = w; height = h;
		realWidth = w-smoothRegion;
		realHeight = h-smoothRegion;
		heights = new double[realWidth][realHeight];

		this.ll = ll;
		this.ur = ur;
		this.lr = lr;
		this.ul = ul;
		
		boolean xSmooth, ySmooth;
		
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				int rx=x, ry=y;
				
				if(x > width/2+smoothRegion)
					rx -= smoothRegion;
				if(y > height/2+smoothRegion)
					ry -= smoothRegion;
				
				xSmooth = !(x < width/2-smoothRegion || x > width/2+smoothRegion);
				ySmooth = !(y < height/2-smoothRegion || y > height/2+smoothRegion);
				
				//Outside the transitional region, just copy values.
				if(!(xSmooth || ySmooth))
				{
					heights[rx][ry] = noiseFromMaps(x, y);
				}
				else
				{	
					int xs, ys;
					double xf, yf, val1, val2;
					
					if(xSmooth && ySmooth)
					{
						xs = (x-width/2)+smoothRegion;
						ys = (y-height/2)+smoothRegion;
						xf = (double)xs/smoothRegion;
						yf = (double)ys/smoothRegion;
						
						val1 = (1-xf)*noiseFromMaps(x,y)+xf*noiseFromMaps(x+smoothRegion, y);
						val2 = (1-xf)*noiseFromMaps(x,y+smoothRegion)+xf*noiseFromMaps(x+smoothRegion, y+smoothRegion);
						
						heights[rx][ry] = (1-yf)*val1+yf*val2;
					}
					else if(xSmooth)
					{
						xs = (x-width/2)+smoothRegion;
						xf = (double)xs/smoothRegion;
						heights[rx][ry] = (1-xf)*noiseFromMaps(x,y)+xf*noiseFromMaps(x+smoothRegion, y);
					}
					else if(ySmooth)
					{
						ys = (y-height/2)+smoothRegion;
						yf = (double)ys/smoothRegion;
						heights[rx][ry] = (1-yf)*noiseFromMaps(x,y)+yf*noiseFromMaps(x, y+smoothRegion);
					}
				}
			}
		}
	}
	
	private void normalize()
	{
		double max=0.0f, min = 1.0f;
		
		for(int x=0; x < realWidth; ++x)
		{
			for(int y=0; y < realHeight; ++y)
			{
				if(heights[x][y] > max)
					max = heights[x][y];
				if(heights[x][y] < min)
					min = heights[x][y];
			}
		}
		max = max-min;
		for(int x=0; x < realWidth; ++x)
		{
			for(int y=0; y < realHeight; ++y)
			{
				heights[x][y] = (heights[x][y]-min)/max;
			}
		}
	}
	
	private double noiseFromMaps(int x, int y)
	{
		double tmp;
		try
		{
			if(x < width/2)
			{
				if(y < height/2)
				{
					tmp = ul.getNoise(x+width/2, y+height/2);
				}
				else
				{
					tmp = ll.getNoise(x+width/2, y-height/2);
				}
			}
			else
			{
				if(y < height/2)
				{
					tmp = ur.getNoise(x-width/2, y+height/2);
				}
				else
				{
					tmp = lr.getNoise(x-width/2, y-height/2);
				}
			}
		}
		//TODO Ugly workaround -> fix.
		catch(Exception e)
		{
			tmp = 1.0f;
		}
		
		return tmp;
	}
	
	public double getHeight(int x, int y)
	{
		return heights[x][y];
	}
	
	public void saveImage(String path)
	{
		normalize();
		
		BufferedImage pic = new BufferedImage(realWidth, realHeight, 
								BufferedImage.TYPE_INT_RGB);
		
		for(int x=0; x < realWidth; ++x)
		{
			for(int y=0; y < realHeight; ++y)
			{
				pic.setRGB(x, y, 
						new Color((float) heights[x][y], 
								(float) heights[x][y],
								(float) heights[x][y]).getRGB());
			}
		}
		File ofile = new File(path);
		try 
		{
			ImageIO.write(pic, "png", ofile);
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	private NoiseMap ul, ur, lr, ll;
	private int width, height, realWidth, realHeight;
	private final int smoothRegion = 16;
	private double[][] heights;
}
