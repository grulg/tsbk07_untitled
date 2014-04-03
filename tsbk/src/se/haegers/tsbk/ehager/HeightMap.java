package se.haegers.tsbk.ehager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class HeightMap 
{

	public HeightMap(int width, int height) 
	{
		this.width = width;
		this.height = height;
		
		numberGenerator = new Random();		
		noise = new double[width][2*height];
		filter = new Filter(width, height);
		inFrequencyDomain = true;
		
		generateNoise();
		//filter.makeSquare(50,50, 100, 100, 1.0f, 0.3f);
		filter.makeBrown();
		applyFilter();
		transform();
		normalizeNoise();
		//normalizeHeights();
	}
	
	/**
	 * Makes white noise in the frequency domain, and stores it in noise.
	 */
	private void generateNoise() 
	{
		DoubleFFT_2D tformer = new DoubleFFT_2D(height, width);
		inFrequencyDomain = true;
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < 2*height; ++y) 
			{
				if(y < height)
					noise[x][y] = numberGenerator.nextGaussian();
				else
					noise[x][y] = 0.0f;
			}
		}
		tformer.realForwardFull(noise);
	}
	
	
	/**
	 * Frequency domain filtering: multiply noise with filter.
	 */
	private void applyFilter()
	{
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				noise[x][y] *= filter.getBin(x, y);
				noise[x][y+height] *= filter.getBin(x, y);
			}
		}
	}
	
	/**
	 * Run post-transform. Normalizes heightmap values
	 * to be in the range [0, 1].
	 */
	private void normalizeHeights()
	{
		double max=0, min=1;
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				double h = heights[x][y];
				if(h > max)
					max = h;
				if(h < min)
					min = h;
			}
		}
		max = max-min;
		System.out.printf("HMax: %f,  Min: %f", max, min);
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				heights[x][y] = (float) ((heights[x][y]-min)/max);
			}
		}
	}
	
	private void normalizeNoise()
	{
		double max=0, min=1;
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < 2*height; ++y)
			{
				double h = noise[x][y];
				if(h > max)
					max = h;
				if(h < min)
					min = h;
			}
		}
		max = max-min;
		System.out.printf("NMax: %f,  Min: %f", max, min);
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < 2*height; ++y)
			{
				noise[x][y] = (float) ((noise[x][y]-min)/max);
			}
		}
	}
	
	/**
	 * Executes FFT to create a proper heightmap.
	 */
	private void transform()
	{
		DoubleFFT_2D tformer = new DoubleFFT_2D(height, width);
		tformer.realInverseFull(noise, true);
		inFrequencyDomain = false;
		
		heights = new double[width][height];
		
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				heights[x][y] = (float) Math.sqrt(
						noise[x][y]*noise[x][y]+noise[x][y+height]*noise[x][y+height]);
			}
		}
	}
	
	/**
	 * Returns the interpolated height at requested coordinates.
	 * Only works in the positive quadrant. Returns 0.0f in case of error
	 * (bad coordinates or noise in frequency domain)
	 * 
	 * @param x X-coordinate. Must be nonnegative!
	 * @param y Y-coordinate. Must be nonnegative!
	 * @return Height at requested coordinates or 0.0 if an error occurs.
	 */
	public double getHeight(double x, double y)
	{
		if(inFrequencyDomain)
			return 0.0f;
		
		int ix = (int) Math.floor(x);
		int iy = (int) Math.floor(y);
		
		double ul = heights[ix][iy];
		double ur = heights[ix+1][iy];
		double ll = heights[ix][iy+1];
		double lr = heights[ix+1][iy+1];
		
		double x1 = ((ix+1)-x)*ul + (x-ix)*ur;
		double x2 = ((ix+1)-x)*ll + (x-ix)*lr;
		
		return ((iy+1)-y)*x1 + (y-ix)*x2;
	}
	
	public void saveImage(String path)
	{
		BufferedImage pic = new BufferedImage(width, height, 
								BufferedImage.TYPE_INT_RGB);
		
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				pic.setRGB(x, y, 
						new Color((float) noise[x][y], 
								(float) noise[x][y],
								(float) noise[x][y]).getRGB());
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
	
	private double[][] noise;
	private double[][] heights;
	private Filter filter;
	private int width, height;
	private boolean inFrequencyDomain;
	private Random numberGenerator;
}
