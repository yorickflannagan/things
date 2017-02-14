package org.crypthing.things.stress;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;

public class Analise {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args != null && args.length < 1)
		{
			usage();
			System.exit(-1);
		}
		
		File f = new File(args[0]);
		File[] binfiles = f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return  name.toLowerCase().endsWith(".bin");
			}
		});
		if (binfiles == null)
		{
			usage();
			System.exit(-2);
		}
		DataInputStream[] dim = new DataInputStream[binfiles.length];
		long[] init = new long[binfiles.length];
		int threads = 0;
		int measures = 0;
		long[][][][] everyone = null;
		
		
		
		for(int i = 0; i< binfiles.length;  i++)
		{
			dim[i] =  new DataInputStream(new FileInputStream(binfiles[i])); 
			int version = dim[i].readInt();
			if(version != 0)
			{
				throw new RuntimeException("Only 0 version supported currently");
			}
			init[i] = dim[i].readLong();
			if(threads ==0)
			{
				threads = dim[i].readInt();
				measures = dim[i].readInt();
				
				everyone = new long[dim.length][threads][measures][2];
			}
			else
			{
				if(threads != dim[i].readInt())
				{
					throw new  RuntimeException("Mixed data, thread doesn't match for file." + binfiles[i]);
				}
				if(measures != dim[i].readInt())
				{
					throw new  RuntimeException("Mixed data, measures doesn't match for file." + binfiles[i]);
				}
			}
			for(int j = 0; j < threads; j++)
			{
				for(int k = 0; k < measures; k++)
				{
					everyone[i][j][k][0] = dim[i].readLong();
					everyone[i][j][k][1] = dim[i].readLong();
				}
			}
			dim[i].close();
		}
		
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		for(int i = 0; i< binfiles.length;  i++)
		{
			for(int j = 0; j < threads; j++)
			{
				if(min > everyone[i][j][0][0]) min = everyone[i][j][0][0];
				if(max < everyone[i][j][measures-1][1]) max = everyone[i][j][measures-1][1];
//				for(int k = 0; k < measures; k++)
//				{
//				}
			}
		}
		
		int ammount = (int) ((max - min)/1000000);
		int[][] moves = new int[ammount+1][2];
		//int maxmilisseconds = 0;
		for(int i = 0; i< binfiles.length;  i++)
		{
			for(int j = 0; j < threads; j++)
			{
				for(int k = 0; k < measures; k++)
				{
					int milisseconds = (int) ((everyone[i][j][k][0] - min)/1000000);
					moves[milisseconds][0]++;
					milisseconds = (int) ((everyone[i][j][k][1] - min)/1000000);
					moves[milisseconds][1]++;
				}
			}
		}
		
		FileWriter fw = new FileWriter(args[0] + File.separatorChar + "analise.csv");
		fw.write("In;Out\n");
		for(int i = 0; i < moves.length; i++)
		{
			fw.write(moves[i][0] + ";" + moves[i][1] + "\n");
		}
		fw.close();
		
		System.out.println("Done");
		
		

		
	}

	private static void usage() {
		System.out.println("usage: java (blablabla) br.gov.caixa.psc.stress.Analise <diretorio>");
		
	}

	

//    for (int i = 0; i< clientw.length; i++ )
//    {
//    	long[][] measures = clientw[i].getMeasures();
//    	for(int j = 0; j <measures.length; j++)
//    	{
//    		dout.writeLong(measures[j][0]);
//    		dout.writeLong(measures[j][1]);
//    	}
//    }
//    dout.close();
	
}
