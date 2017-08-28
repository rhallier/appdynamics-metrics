package org.appdynamics.metrics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class PrinterAdapter {

	private PrintWriter writer = null;
	private int counter=0;
	
	public PrinterAdapter() {
	}
	
	public PrinterAdapter(String filename) throws FileNotFoundException {
		if(filename!=null)
			writer = new PrintWriter(filename);
	}
	
	public void health() {
		if(writer!=null) {
			counter++;
			System.out.print(".");
			if(counter % 40 == 0)
				System.out.println("");
		}
	}

	public void println(String value) {
		if(writer!=null) {
			writer.println(value);
		}
		else
			System.out.println(value);
	}
	
	public void close() {
		if(writer!=null) {
			writer.flush();
			writer.close();
			writer=null;
		}
	}
}
