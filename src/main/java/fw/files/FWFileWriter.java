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
	private static final int BUFFER_SIZE = 4096;
	
	public FWFileWriter(File file) throws FileNotFoundException {
		this.name=file.getName();
		this.zip = new ZipOutputStream(new FileOutputStream(file));
	}
	
	// @Override
	// protected void finalize() throws Throwable {
	// 	zip.close();
	// 	super.finalize();
	// }
	
	public void close() throws IOException{
		zip.close();
	}
	
	/*
	 * WRITERS
	 */
	
	public void writeText(String text, String filename) throws IOException {
		final File tempFile = File.createTempFile(name + "-", "-" + filename);
		
		try (BufferedWriter lineWriter= Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8)) {
			lineWriter.write(text);
		}
        
		zip.putNextEntry(new ZipEntry(filename));
		
		try (FileInputStream fis = new FileInputStream(tempFile)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = fis.read(buffer)) >= 0) {
				zip.write(buffer, 0, length);
			}
		}
		
		zip.closeEntry();
		Files.delete(tempFile.toPath());
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
		Files.delete(tempFile.toPath());
	}

	public void write(File file, String filename) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			zip.putNextEntry(new ZipEntry(filename));

			final byte[] buffer = new byte[BUFFER_SIZE];
			int len;
			while((len = in.read(buffer)) > -1) {
				zip.write(buffer, 0, len);
			}
		}
		
		zip.closeEntry();
	}
	
	public void write(File file) throws IOException {
		write(file, file.getName());
	}
}