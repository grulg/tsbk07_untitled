package se.haegers.tsbk.ehager;

public class Filter 
{
	public Filter(int wid, int hei)
	{
		width = wid;
		height = hei;
		
		scale = 0.01;
		
		bins = new double[wid][hei];
	}
	
	public double getBin(int x, int y)
	{
		return bins[x][y];
	}
	
	public void makeSquare(int x, int y, int wid, int hei, double insq, double osq)
	{
		if(x+wid > width || y+hei > height || x < 0 || y < 0)
			return;
		
		for(int lx=0; lx < width; ++lx) 
		{
			for(int ly=0; ly < height; ++ly)
			{
				if((lx >= x && lx < x+wid) && (ly >= y && ly < y+wid))
				{
					bins[lx][ly] = insq;
				}
				else
					bins[lx][ly] = osq;
			}
		}
	}
	
	public void makePink()
	{
		double max = 0.0f;
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				double xs, ys;
				xs = Math.abs((x-width/2)*scale);
				ys = Math.abs((y-height/2)*scale);
				
				bins[x][y] = 1.0f/((xs+ys)+1);
				if(bins[x][y] > max)
					max = bins[x][y];
			}
		}
		System.out.printf("Filter max: %f\n", max);
	}
	
	public void makeBrown()
	{
		for(int x=0; x < width; ++x) 
		{
			for(int y=0; y < height; ++y)
			{
				double xs, ys;
				xs = x*scale;
				ys = y*scale;
				bins[x][y] = 1.0f/((xs*xs+ys*ys)+1);
			}
		}
	}
	
	private double scale;
	private double[][] bins;
	private int width, height;
}
