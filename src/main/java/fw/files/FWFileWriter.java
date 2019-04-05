package fw.files;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import fw.xml.XMLCapabilities;
import fw.xml.XMLException;


public class FWFileWriter {
	
	private String name;
	private ZipOutputStream zip;
	private static int BUFFER_SIZE = 4096;
	
	public FWFileWriter(File file) throws FileNotFoundException {
		this.name=file.getName();
		this.zip = new ZipOutputStream(new FileOutputStream(file));
	}
	
	@Override
	protected void finalize() throws Throwable {
		zip.close();
		super.finalize();
	}
	
	public void close() throws IOException{
		zip.close();
	}
	
	/*
	 * WRITERS
	 */
	
	public void writeText(String text, String filename) throws IOException {
		File tempFile = File.createTempFile(name+"-", "-"+filename);
		
		BufferedWriter lineWriter= Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8);
		lineWriter.write(text);
		lineWriter.close();
        
		zip.putNextEntry(new ZipEntry(filename));
		
		FileInputStream fis = new FileInputStream(tempFile);
		byte[] buffer = new byte[BUFFER_SIZE];
		int length;
		while ((length = fis.read(buffer)) >= 0) {
			zip.write(buffer, 0, length);
		}
		fis.close();
		
		zip.closeEntry();
		tempFile.delete();
	}
	
	public void writeXML(XMLCapabilities xml, String filename) throws IOException, XMLException {
		writeText(xml.getXMLProperties().getXMLCode(), filename);
	}
	
	public void writeImage(BufferedImage im, String filename) throws IOException{
		File tempFile = File.createTempFile(name+"-", "-"+filename);
		ImageIO.write(im, "png", tempFile);
		
		FileImageOutputStream imgOut = new FileImageOutputStream(tempFile);
		zip.putNextEntry(new ZipEntry(filename));
		
		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while((len = imgOut.read(buffer)) > -1)
			zip.write(buffer, 0, len);
		imgOut.close();
		
		zip.closeEntry();
		tempFile.delete();
	}

	public void write(File file, String filename) throws IOException {
		FileInputStream in = new FileInputStream(file);
		zip.putNextEntry(new ZipEntry(filename));

		byte[] buffer = new byte[BUFFER_SIZE];
		int len;
		while((len = in.read(buffer)) > -1)
			zip.write(buffer, 0, len);
		in.close();
		
		zip.closeEntry();
	}
	
	public void write(File file) throws IOException {
		write(file, file.getName());
	}
}