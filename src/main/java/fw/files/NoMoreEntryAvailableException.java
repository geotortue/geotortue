package fw.files;

import java.io.File;


public class NoMoreEntryAvailableException extends Exception {
	private static final long serialVersionUID = -7183178604926427700L;

	public NoMoreEntryAvailableException(File f, String fileDescription) {
		super("NoMoreEntryAvailableException in " + f.getName() + ", trying to read \"" +  fileDescription + "\"");
	}
}