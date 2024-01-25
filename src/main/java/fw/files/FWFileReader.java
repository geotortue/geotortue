package fw.files;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;


public class FWFileReader {

	private final File file;
	private final String name;
	private ZipInputStream zip;
	private static final int BUFFER_SIZE = 4096;
	
	public FWFileReader(File f) throws FileNotFoundException {
		this.file = f;
		this.name = f.getName()+".";
		this.zip = new ZipInputStream(new FileInputStream(f));
	}
	
	// @Override
	// protected void finalize() throws Throwable {
	// 	zip.close();
	// 	super.finalize();
	// }

	private ZipEntry getNextEntry(String fileDescription) throws IOException, NoMoreEntryAvailableException {
		final ZipEntry e = zip.getNextEntry();
		if (e == null) {
			throw new NoMoreEntryAvailableException(file, fileDescription);
		}

		return e;
	}

	public void skip(int entriesToSkip) throws IOException {
		for (int idx = 0; idx < entriesToSkip; idx++) {
			zip.getNextEntry();
		}
	}
	
	public void reset() throws IOException {
		zip.close();
		this.zip = new ZipInputStream(new FileInputStream(file));
	}
	
	public void close() throws IOException{
		zip.close();
	}
	
	/*
	 * READERS 
	 */
	

	private File createTempFile(final String fileDescription) throws IOException{
		return File.createTempFile(name + "-", "-" + fileDescription);
	}
	
	public String readText(final String fileDescription) throws IOException, NoMoreEntryAvailableException {
		getNextEntry(fileDescription);

		final File tempFile = createTempFile(fileDescription);

		try(FileOutputStream fis = new FileOutputStream(tempFile)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int length;
			while ((length = zip.read(buffer)) > -1) {
				fis.write(buffer, 0, length);
			}
		}

		zip.closeEntry();

		StringBuilder sb = new StringBuilder();
		try (BufferedReader lineReader = Files.newBufferedReader(tempFile.toPath(), StandardCharsets.UTF_8);) {
			String line = lineReader.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = lineReader.readLine();
			}
		} catch (MalformedInputException ex) {
			try(BufferedReader lineReader = Files.newBufferedReader(tempFile.toPath(), StandardCharsets.ISO_8859_1)) {
				sb = new StringBuilder();
				String line = lineReader.readLine();
				while (line != null) {
					sb.append(line);
					sb.append("\n");
					line = lineReader.readLine();
				}	
			}
		}
		
		Files.delete(tempFile.toPath());
		
		return sb.toString();
	}

	public XMLReader getXMLReader(final String fileDescription) throws IOException, NoMoreEntryAvailableException, XMLException {
		String content = readText(fileDescription);
		return new XMLFile(content).parse();
	}
	
	public BufferedImage readImage(String fileDescription) throws IOException, NoMoreEntryAvailableException{
		getNextEntry(fileDescription);
		
		File tempFile = createTempFile(fileDescription);

		try (FileImageOutputStream imgOut = new FileImageOutputStream(tempFile)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int len;
			while((len = zip.read(buffer)) > -1) {
				imgOut.write(buffer, 0, len);
			}
		}
		
		final BufferedImage im = ImageIO.read(tempFile);
		
		zip.closeEntry();
		Files.delete(tempFile.toPath());
		return im;
	}

	private File unzip(File file) throws IOException {
		try(FileOutputStream out = new FileOutputStream(file)) {
		
			byte[] buffer = new byte[BUFFER_SIZE];
			int len;
			while((len = zip.read(buffer)) > -1) {
				out.write(buffer, 0, len);
			}
		}
		
		zip.closeEntry();
		return file;
	}

	public void unzipNextEntries(File dir) throws IOException {
		while (true) {
			try {
				ZipEntry e = getNextEntry(null);
				File file = new File(dir, e.getName());
				unzip(file);
			} catch (NoMoreEntryAvailableException ex) {
				return;
			}
		}
	}
	
//	
//	private File unzipNextEntry(String fileDescription) throws IOException, NoMoreEntryAvailableException {
//		getNextEntry(fileDescription);
//		return unzip(createTempFile(fileDescription));
//	}
//	
//	private File unzipNextEntry(File file) throws IOException, NoMoreEntryAvailableException {
//		getNextEntry(file.getName());
//		return unzip(file);
//	}
}