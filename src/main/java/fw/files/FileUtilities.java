package fw.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import fw.app.Translator.TKey;


/**
 * Utility class with only static functions
 */
public class FileUtilities {
	
	private static int BUFFER_SIZE = 4096;
	private static int timeOut = 10000;
	
	private static final TKey ERROR_401 = new TKey(FileUtilities.class, "error401");
	private static final TKey ERROR_403 = new TKey(FileUtilities.class, "error403");
	private static final TKey ERROR_404 = new TKey(FileUtilities.class, "error404");
	private static final TKey ERROR_407 = new TKey(FileUtilities.class, "error407");
	private static final TKey ERROR = new TKey(FileUtilities.class, "error");
	private static final TKey SERVER_ERROR = new TKey(FileUtilities.class, "serverError");
	private static final TKey TIME_OUT = new TKey(FileUtilities.class, "timeOut");
	private static final TKey NETWORK_DOWN = new TKey(FileUtilities.class, "networkDown");

	private FileUtilities() {}

	public static File copy(URL src, File dest) throws IOException, HTTPException {
		if (src.getProtocol().equals("http"))
			getConnection(src);
		InputStream input = src.openStream();
		FileOutputStream output = new FileOutputStream(dest);
		
		byte[] buf = new byte[BUFFER_SIZE];
		int len;
        while ( (len = input.read(buf)) > -1)
            output.write(buf, 0, len);
        
        output.close();
		input.close();
		return dest;
	}
	
	public static File copy(URL src) throws IOException, HTTPException{
		File dest = File.createTempFile(src.getFile(), ".tmp");
		return copy(src, dest);
	}
	
	public static String getStem(File f){
		if (f==null)
			return null;
		return getStem(f.getName());
	}

	public static String getStem(String name){
		int idx=name.lastIndexOf(".");
		if (idx<0)
			return name;
		else
			return name.substring(0, idx);
	}

	public static String getExtension(File f){
		if (f.isDirectory())
			return "dir";
		String name = f.getName();
		int idx=name.lastIndexOf(".");
		if (idx<0)
			return "";
		else
			return name.substring(idx+1);
	}
	
	public static File changeExtension(File f, String ext){
		File path = f.getParentFile();
		String name = FileUtilities.getStem(f);
		File file = new File(path, name+ext);
		return file;
	}

	/**
	 * Return a new file in parent directory named after a pattern : the pattern should contain a sequence of '%' 
	 * which will be replaced by an integer, including its leading zeroes to fit the sequence length.
	 *  
	 * @param parent
	 * @param pattern
	 * @return null if pattern does not contain  at least a '%' char 
	 */
	public static File getNewFile(File parent, String pattern) {
		if (!parent.isDirectory())
			parent = parent.getParentFile();
		if (pattern.indexOf('%')<0)
			return null;
		return getNewFile_(parent, pattern, 1);
	}
	
	private static File getNewFile_(File parent, String pattern, int n) {
		String name = pattern;
		int x1 = name.indexOf('%');
		int x2 = name.lastIndexOf('%');
		String xxx = name.substring(x1, x2+1);
		String number = n+"";
		while (number.length()<xxx.length())
			number = "0"+number;
		name = name.replace(xxx, number);
		File f = new File(parent, name);
		if (!f.exists()) 
			return f;
		
		return getNewFile_(parent, pattern, n+1);
	}


	public static File checkExtension(File file, String extension) {
		if (file.getName().endsWith("."+extension))
			return file;
		return new File(file.getAbsolutePath() + "." + extension);
	}
	
	public static InputStreamReader getInputStream(URL url) throws HTTPException, IOException {
		InputStream is = null;
		HttpURLConnection huc = getConnection(url);
		is = huc.getInputStream();
		return new InputStreamReader(is, "UTF-8");
	}
	
	private static HttpURLConnection getConnection(URL url) throws HTTPException, IOException {
		try {
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setConnectTimeout(timeOut);
			huc.setReadTimeout(timeOut);
			huc.setRequestMethod("GET");
			huc.connect();
			
			int code = huc.getResponseCode();
			testResponseCode(code);
			return huc;
		} catch (SocketTimeoutException ex) {
			throw new HTTPException(TIME_OUT);
		} catch (UnknownHostException | SocketException ex) {
			throw new HTTPException(NETWORK_DOWN);
		}
	}
	
	private static void testResponseCode(int code) throws HTTPException {
		if (code==200) // success
			return;
		if (code>=400 && code<500)
		switch (code) {
		case HttpURLConnection.HTTP_UNAUTHORIZED : // 401
			throw new HTTPException(ERROR_401);
		case HttpURLConnection.HTTP_FORBIDDEN : // 403
			throw new HTTPException(ERROR_403);
		case HttpURLConnection.HTTP_NOT_FOUND : // 404
			throw new HTTPException(ERROR_404);
		case HttpURLConnection.HTTP_PROXY_AUTH: // 407
			throw new HTTPException(ERROR_407);
		default:
			throw new HTTPException(ERROR, code+"");
		}
		if (code>=500)
			throw new HTTPException(SERVER_ERROR, code+""); // server error
	}
	
	public static class HTTPException extends Exception {

		private static final long serialVersionUID = -8082621986085858733L;

		public HTTPException(TKey key, String... args) {
			super(key.translate(args));
		}
	}
}
