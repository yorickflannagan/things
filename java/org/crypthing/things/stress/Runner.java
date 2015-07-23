package org.crypthing.things.stress;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;



public class Runner {

	private PrintWriter out;

	public void setOut(PrintWriter out)
	{
		this.out = out;
	}
	
	public PrintWriter getOut()
	{
		return out;
	}
	
	public void run(String[] args)  throws Exception
	{
		
	    CountDownLatch startSignal;
			
		
	    int threads = -1;
		int medidas = -1;
		try {
	    	threads = Integer.parseInt(args[0]); // Threads
			medidas= Integer.parseInt(args[1]); // "attempts"
			startSignal = new CountDownLatch(1);
		} catch (Exception e) {
	    	usage();
			return;
		}
	    CountDownLatch doneSignal = new CountDownLatch(threads);
	    Properties p = new Properties();
	    if(!"".equals(args[2])) p.load(new FileInputStream(args[2]));  // Properties
	    WorkerFactory wf = (WorkerFactory)Thread.currentThread().getContextClassLoader().loadClass(args[3]).newInstance(); // classe factory
	    wf.init(startSignal, doneSignal, medidas, p);
	    
	    
	    // Criando as threads de workers
		Worker[] clientw = new Worker[threads];
		for(int i=0;i<threads; i++ ) {
			clientw[i] = wf.getWorker();
	        new Thread(clientw[i]).start();
		}
		
		if(args.length > 6 &&  !args[6].equals(""))
		{
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path path = Paths.get(args[6]);	
			path.register(watcher, ENTRY_DELETE);
			System.out.println("Waiting for a file to be deleted at [" + args[6] + "]");
            try {
            	watcher.take();
            } catch (InterruptedException x) {
				System.out.println("Interrupted.");
            }
			System.out.println("Ok. Will warm up.");
		}
		
		long time = System.nanoTime();
		startSignal.countDown();
        doneSignal.await();
		time = System.nanoTime() - time;
		System.out.println("Warmup complete in " + (time/1000000) + " ms.");

		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(threads);
		
		for(int i=0;i<threads; i++ ) {
			clientw[i].setStartSignal(startSignal);
			clientw[i].setDoneSignal(doneSignal);
			clientw[i].reset();
	        new Thread(clientw[i]).start();
		}
		
		if(args.length > 6 &&  !args[6].equals(""))
		{
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path path = Paths.get(args[6]);	
			path.register(watcher, ENTRY_DELETE);
			System.out.println("Waiting for a file to be deleted at [" + args[6] + "]");
            try {
            	watcher.take();
            } catch (InterruptedException x) {
				System.out.println("Interrupted.");
            }
			System.out.println("Release the Kraken!");
		}		
		
		long init = System.currentTimeMillis();
		time = System.nanoTime();
		startSignal.countDown();
        doneSignal.await();
		time = System.nanoTime() - time;

		System.out.println(threads + "x" + medidas + " em : " + time + " nanosegundos, ou " + (time/1000000) + " milissegundos. M�dia de " + time/(threads * medidas) + " nanosegundos por evento");
		System.out.println("Writing down reports");
		
		wf.end();
		
		long clientwgetAverage = 0;
		for(int i=0;i<threads; i++ ) {
			clientwgetAverage += clientw[i].getAverage()/threads;
		}
		//clientwgetAverage = clientwgetAverage/threads;
        
        String title = args.length == 8 ? args[7] : "";
        
        PrintWriter out = getOut();
        if(out==null) 
        {
            if(!"".equals(args[4]))
            {
            	out = new PrintWriter(new FileWriter(args[4]+ ".html"));
            }
            else {
            	out = new PrintWriter(System.out);
            }
        }
        	
        out.write("<html><head></head><body>");
        out.write("<h1>" + title + "</h1>");
        
        out.write("<table><tr>");
        out.write("<td>M�dia individual</td>");
        out.write("<td>"+ clientwgetAverage + "</td>");
        out.write("</tr>");
        
        out.write("<tr>");
        out.write("<td>Threads</td>");
        out.write("<td>"+ threads +"</td>");
        out.write("</tr>");	        
        
        out.write("<tr>");
        out.write("<td>Medidas</td>");
        out.write("<td>"+ medidas +"</td>");
        out.write("</tr>");	        

        float thougput = (medidas*threads)/(time/1000000000f);
        
        out.write("<tr>");
        out.write("<td>Throughput</td>");
        out.write("<td>"+thougput +"/s</td>");
        out.write("</tr>");	        
        
        if(args[5].equalsIgnoreCase("true"))
        {
	        out.write("<tr>");
	        out.write("<td colspan=\"4\">Medidas Individuais </td>");
	        out.write("</tr>");
	        
	        out.write("<tr>");
	        out.write("<td></td>");
	        out.write("<td>Thread</td>");
	        out.write("<td>Medida</td>");
	        out.write("<td>Tempo</td>");
	        out.write("</tr>");
	        
	        for(int i = 0 ; i < medidas; i++) {
	            for(int j = 0 ; j< threads; j++) {
			        out.write("<tr>");
			        out.write("<td></td>");
			        out.write("<td>" + j + "</td>");
			        out.write("<td>" + i + "</td>");
			        out.write("<td>"+ clientw[j].getAvail()[i] + "</td>");
			        out.write("</tr>");
		        }
	        }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td></td>");
        sb.append("<td>Thread</td>");
        sb.append("<td>Executados</td>");
        sb.append("<td>Erros</td>");
        sb.append("<td>Médias</td>");
        sb.append("</tr>");
        
        for(int j = 0 ; j< threads; j++) {
	        sb.append("<tr>");
	        sb.append("<td></td>");
	        sb.append("<td>"+ j +"</td>");
	        sb.append("<td>" + clientw[j].getDone() + "</td>");
	        sb.append("<td>" + clientw[j].getErrors() + "</td>");
	        sb.append("<td>" + clientw[j].getAverage() + "</td>");
	        sb.append("</tr>");
        }
        
        out.write("<tr>");
        out.write("<td colspan=\"4\">Resumo</td>");
        out.write("</tr>");
        
        out.write(sb.toString());
        
        out.write("</table></body></html>");
        out.flush();
        out.close();
        
        
        if(!"".equals(args[4]))
        {
	        DataOutputStream dout = new DataOutputStream(new FileOutputStream(args[4] + ".bin"));
	        dout.writeInt(0);
	        dout.writeLong(init);
	        dout.writeInt(threads);
	        dout.writeInt(medidas);
	        for (int i = 0; i< clientw.length; i++ )
	        {
	        	long[][] measures = clientw[i].getMeasures();
	        	for(int j = 0; j <measures.length; j++)
	        	{
	        		dout.writeLong(measures[j][0]);
	        		dout.writeLong(measures[j][1]);
	        	}
	        }
	        dout.close();
        }
	}

	private static void usage() {
		System.err.println("java br.gov.caixa.psc.stress.Runner numthread attempts propertyfile workerfactoryclassname outputfile [waitforfiletobedeleted] [title]");
		System.err.println("Example java br.gov.caixa.psc.stress.Runner 1 1 crl.props br.gov.caixa.psc.stress.x509.X509CRLWorkerFactory /desenv/atendimento/icp/stress/abcd.html 1 \"cool relat\"");

		
	}
	

	public static void main(String[] args) throws Exception {
		new Runner().run(args);
	}

}
