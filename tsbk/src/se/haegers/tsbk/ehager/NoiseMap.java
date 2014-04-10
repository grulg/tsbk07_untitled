package se.haegers.tsbk.ehager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class NoiseMap 
{

	public NoiseMap(int width, int height, int x, int y) 
	{
		this.width = width;
		this.height = height;
		
		long seed = ((long)x << 32) | (long) y;
		
		numberGenerator = new Random(seed);		
		noise = new double[width][height];
		
		filter = new Filter(width, height);
		filter.makeBrown();
		//filter.makeFromGrayscale("filter.png");
		
		generateNoise();
	}
	
	/**
	 * Makes some frequency-domain white noise.
	 */
	private void generateNoise() 
	{
		
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y) 
			{
				double n = numberGenerator.nextGaussian();
				noise[x][y] = n;	
			}
		}
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
			}
		}
	}
	
	/**
	 * Post-filter application, transform back from frequency domain.
	 */
	private void transform()
	{	
		DoubleFFT_2D tformer = new DoubleFFT_2D(height, width);
		tformer.realInverse(noise, true);
	}
	
	/**
	 * Re-generates everything. Doesn't reseed the RNG, so expect a different result.
	 */
	public void renew()
	{
		generateNoise();
		applyFilter();
		transform();
		normalizeNoise();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double getNoise(int x, int y)
	{
		return noise[x][y];
	}
	
	private void normalizeNoise()
	{
		double max=0.0f, min = 1.0f;
		
		nNoise = new double[width][height];
		
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				if(noise[x][y] > max)
					max = noise[x][y];
				if(noise[x][y] < min)
					min = noise[x][y];
			}
		}
		max = max-min;
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				nNoise[x][y] = (noise[x][y]-min)/max;
			}
		}
	}
	/*
	public void getEdges(NoiseMap[] m)
	{
		for(int x=0; x < edgePixels; ++x)
		{
			for(int y=0; y < edgePixels; ++y)
			{
				//From upper left.
				noise[x][y] = m[0].getNoise(x+width-3*edgePixels-1, y+height-3*edgePixels-1);
				//From upper right.
				noise[width-1-x][y] = m[1].getNoise(edgePixels-1-x, y+height-3*edgePixels-1);
				//From lower right.
				noise[width-1-x][height-1-y] = m[2].getNoise(edgePixels-1-x, edgePixels-1-y);
				//From lower left.
				noise[x][height-1-y] = m[3].getNoise(x+width-3*edgePixels-1, edgePixels-1-y);
			}
		}
		for(int x=0; x < edgePixels; ++x)
		{
			for(int y=0; y < height-2*edgePixels; ++y)
			{
				//From the left.
				noise[x][y+edgePixels] = m[4].getNoise(x+width-3*edgePixels-1, y);
				//From the right
				noise[width-1-x][y+edgePixels] = m[5].getNoise(edgePixels-1-x, y);
			}
		}
		for(int y=0; y < edgePixels; ++y)
		{
			for(int x=0; x < width-2*edgePixels; ++x)
			{
				//From above
				noise[x+edgePixels][y] = m[6].getNoise(x, y+width-3*edgePixels-1);
				//From below
				noise[x+edgePixels][height-1-y] = m[7].getNoise(x, edgePixels-1-y);
			}
		}
	}
	*/
	
	/**
	 * Saves the generated heightmap as a grayscale image.
	 * @param path File to save to.
	 */
	public void saveImage(String path)
	{
		normalizeNoise();
		BufferedImage pic = new BufferedImage(width, height, 
								BufferedImage.TYPE_INT_RGB);
		
		for(int x=0; x < width; ++x)
		{
			for(int y=0; y < height; ++y)
			{
				pic.setRGB(x, y, 
						new Color((float) nNoise[x][y], 
								(float) nNoise[x][y],
								(float) nNoise[x][y]).getRGB());
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
	
	public void setFilter(Filter f)
	{
		filter = f;
	}
	
	/**
	 * Noise-to-frequency, filter multiplication, inverse transformation.
	 */
	public void filterNoise()
	{
		applyFilter();
		transform();
		//normalizeNoise();
	}
	
	private double[][] noise;
	private double[][] nNoise;
	private Filter filter;
	private int width, height;
	private Random numberGenerator;
}
