package de.schulte.testverteilung;
	import java.io.*;

import org.apache.commons.codec.binary.Base64;
	import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;



	public class PDFTest {
	  
	  static final byte[] HEX_CHAR_TABLE = {
	    (byte)'0', (byte)'1', (byte)'2', (byte)'3',
	    (byte)'4', (byte)'5', (byte)'6', (byte)'7',
	    (byte)'8', (byte)'9', (byte)'a', (byte)'b',
	    (byte)'c', (byte)'d', (byte)'e', (byte)'f'
	  };    

	 static ByteArrayOutputStream bys = new ByteArrayOutputStream();
	 static  BufferedOutputStream bos = new BufferedOutputStream(bys);

	 public static void main(String[] args){
	 PDDocument pd;
	 BufferedWriter wr;
	 try {
	   String a = "a";
	   String b = "b";
	   getData(a, true);
	   getData(b, false);
       getData(a, true);
       getData(b, false);
       getData(b, true);
       getData(a, false);
	   System.out.println(new String(bys.toByteArray()));
//	         File input = new File("/Users/klaus.SCHULTE/Downloads/000000A8.pdf");  // The PDF file from where you would like to extract
//	         File output = new File("/u/m500288/SampleText.txt"); // The text file where you are going to store the extracted data
//	         FileInputStream fileInputStream = new FileInputStream(input);
//	         byte[] data = new byte[(int) input.length()];
//	         fileInputStream.read(data);
//	         fileInputStream.close();
//	         for (int i = 0; i < 29; i++) {
//              System.out.println(data[i]);
//            }
//	         String hex = getHexString(data);
//	      
//	         System.out.println(hex);
//	         System.out.println(hex.length());
//	         pd = PDDocument.load(input);
//	         System.out.println(pd.getNumberOfPages());
//	         System.out.println(pd.isEncrypted());
//	         pd.save("CopyOfInvoice.pdf"); // Creates a copy called "CopyOfInvoice.pdf"
//	         PDFTextStripper stripper = new PDFTextStripper();
//	         wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
//	         stripper.writeText(pd, wr);
//	         if (pd != null) {
//	             pd.close();
//	         }
//	        // I use close() to flush the stream.
//	        wr.close();
	 } catch (Exception e){
	         e.printStackTrace();
	        }
	     }
	 
	 public static String getHexString(byte[] raw) 
	     throws UnsupportedEncodingException 
	   {
	     byte[] hex = new byte[2 * raw.length];
	     int index = 0;

	     for (byte b : raw) {
	       int v = b & 0xFF;
	       hex[index++] = HEX_CHAR_TABLE[v >>> 4];
	       hex[index++] = HEX_CHAR_TABLE[v & 0xF];
	     }
	     return new String(hex, "ASCII");
	   }
	 
	 public static void getData(final String str, final boolean init) {
	   byte[] ret = str.getBytes();
	    try {
	      if (init)
	        bys.reset();
	      bos.write(ret, 0, ret.length);
	      bos.flush();
	    } catch (IOException e) {
	      System.out.println(e.getMessage());
	      e.printStackTrace();
	    }
	 }
}
