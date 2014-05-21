package se.haegers.tsbk.ehager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.emory.mathcs.jtransforms.fft.RealFFTUtils_2D;

public class Filter 
{
	public Filter(int wid, int hei)
	{
		width = wid;
		height = hei;
		
		//For "square" filters, xscale = 2*yscale.
		xscale = 2.0f;
		yscale = 1.0f;
		
		packer = new RealFFTUtils_2D(wid, hei);
		bins = new float[wid][hei];
	}
	
	public double getBin(int x, int y)
	{
		return bins[x][y];
	}
	
	/**
	 * Creates a filter that only leaves the DC component. Why would you want this?
	 */
	public void makeDC()
	{
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				bins[x][y] = 0.0f;
			}
		}
		bins[0][0] = 1.0f;
	}
	
	public void makeSquare(int x, int y, int wid, int hei, float insq, float osq)
	{
		if(x+wid > width || y+hei > height || x < 0 || y < 0)
			return;
		
		for(int lx=0; lx < width; ++lx) 
		{
			for(int ly=0; ly < height; ++ly)
			{
				if(lx == 0 || lx == width/2)
					continue;
				
				if((lx >= x && lx < x+wid) && (ly >= y && ly < y+wid))
				{
					packer.pack(insq, lx, ly, bins);
				}
				else
					packer.pack(osq, lx, ly, bins);
			}
		}
	}
	
	public void makeAllPass()
	{
		for(int lx=0; lx < width; ++lx) 
		{
			for(int ly=0; ly < height; ++ly)
			{
				if(ly == 1 && (lx == 0 || lx == width/2))
					continue;
				packer.pack(1.0f, lx, ly, bins);
			}
		}
	}
	
	public void makePink()
	{
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				if(x == 0 || x == width/2)
					continue;
				float xs, ys;
				xs = Math.abs(x*xscale);
				ys = Math.abs(y*yscale);
				
				packer.pack(1.0f/((float)Math.sqrt(xs*xs+ys*ys)+1), x, y, bins);
			}
		}
		//System.out.printf("Filter max: %f\n", max);
	}
	
	public void makeBrown()
	{
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				if(y == 1 && (x == 0 || x == width/2))
					continue;
				float xs, ys;
				xs = x*xscale;
				ys = y*yscale;

				packer.pack(1.0f/((xs*xs+ys*ys)+1.0f), x, y, bins);
			}
		}
	}
	
	public void makePowFalloff(double pw)
	{
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				if(y == 1 && (x == 0 || x == width/2))
					continue;
				float xs, ys, d;
				xs = x*xscale;
				ys = y*yscale;
				
				d = (xs*xs+ys*ys);
				pw = pw/2.0f;
				d = (float) Math.pow((double) d, pw);

				packer.pack(1.0f/(d+1.0f), x, y, bins);
			}
		}
		//bins[0][1] = 0.0f;
		//bins[width/2][1] = 0.0f;
	}
	
	/**
	 * Makes a filter from a grayscale image. No resolution checking used; wrong
	 * resolutons will result in crashes. Use the right resolution. 
	 * @param path Location of target image.
	 */
	public void makeFromGrayscale(String path)
	{
		BufferedImage pic = null;
		try
		{
			pic = ImageIO.read(new File(path));
		}
		catch(IOException e)
		{
			System.out.printf("%s\n", e.getMessage());
		}
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				if(y == 1 && (x == 0 || x == width/2))
					continue;
				packer.pack(new Color(pic.getRGB(x, y)).getBlue(), x, y, bins);
			}
			bins[0][1] = new Color(pic.getRGB(0, 1)).getBlue();
			bins[width/2][1] = new Color(pic.getRGB(width/2, 1)).getBlue();
		}
	}
	
	private float xscale, yscale;
	private float[][] bins;
	private int width, height;
	private RealFFTUtils_2D packer;
}
