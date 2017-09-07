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
	
	public void console() {
		console(".",false);
	}

	public void consoleln() {
		console("",true);
	}

	public void console(String value) {
		console(value, false);
	}


	public void consoleln(String value) {
		console(value, true);
	}
	
	private void console(String value, boolean line) {
		if(writer!=null) {
			counter++;
			
			if(line) {
				System.out.println(value);
				counter=0;
			}
			else {
				System.out.print(value);
				if(counter % 40 == 0)
					System.out.println("");
			}
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
