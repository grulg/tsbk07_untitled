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
		rW = w-smoothRegion;
		rH = h-smoothRegion;
		heights = new float[rW][rH];
		
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
					float xf, yf, val1, val2;
					
					if(xSmooth && ySmooth)
					{
						xs = (x-width/2)+smoothRegion;
						ys = (y-height/2)+smoothRegion;
						xf = (float)xs/smoothRegion;
						yf = (float)ys/smoothRegion;
						
						val1 = (1-xf)*noiseFromMaps(x,y)+xf*noiseFromMaps(x+smoothRegion, y);
						val2 = (1-xf)*noiseFromMaps(x,y+smoothRegion)+xf*noiseFromMaps(x+smoothRegion, y+smoothRegion);
						
						heights[rx][ry] = (1-yf)*val1+yf*val2;
					}
					else if(xSmooth)
					{
						xs = (x-width/2)+smoothRegion;
						xf = (float)xs/smoothRegion;
						heights[rx][ry] = (1-xf)*noiseFromMaps(x,y)+xf*noiseFromMaps(x+smoothRegion, y);
					}
					else if(ySmooth)
					{
						ys = (y-height/2)+smoothRegion;
						yf = (float)ys/smoothRegion;
						heights[rx][ry] = (1-yf)*noiseFromMaps(x,y)+yf*noiseFromMaps(x, y+smoothRegion);
					}
				}
			}
		}
		normalize();
	}
	
	public HeightMap(NoiseMap src)
	{
		
	}

	private void normalize()
	{
		float max=0.0f, min = 1.0f;
		
		for(int x=0; x < rW; ++x)
		{
			for(int y=0; y < rH; ++y)
			{
				if(heights[x][y] > max)
					max = heights[x][y];
				if(heights[x][y] < min)
					min = heights[x][y];
			}
		}
		max = max-min;
		for(int x=0; x < rW; ++x)
		{
			for(int y=0; y < rH; ++y)
			{
				heights[x][y] = (heights[x][y]-min)/max;
			}
		}
	}
	
	private float noiseFromMaps(int x, int y)
	{
		float tmp;
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
	
	public float getHeight(int x, int y)
	{
		return heights[x][y];
	}
	
	public int getW()
	{
		return rW;
	}
	
	public int getH()
	{
		return rH;
	}
	
	public void saveImage(String path)
	{
		//normalize();
		
		BufferedImage pic = new BufferedImage(rW, rH, 
								BufferedImage.TYPE_INT_RGB);
		
		for(int x=0; x < rW; ++x)
		{
			for(int y=0; y < rH; ++y)
			{
				pic.setRGB(x, y, 
						new Color(heights[x][y], 
								 heights[x][y],
								 heights[x][y]).getRGB());
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
	private int width, height, rW, rH;
	private final int smoothRegion = 18;
	private float[][] heights;
}
