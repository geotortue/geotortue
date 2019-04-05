package fw.files;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import fw.files.FileUtilities.HTTPException;


public class CSVFile extends TextFile {

	private char fieldSeparator = ';';
	private char textSeparator = '\"';
	
	public CSVFile(URL url) throws IOException, HTTPException  {
		super(url);
	}

	public CSVFile(String text) {
		super(text);
	}

	public CSVPool getCSVPool() throws CSVException {
		String[] lines = splitContent("\n");
		if (lines.length==0)
			return null;
		CSVPool pool = new CSVPool(split(lines[0]), lines.length-1);
		for (int idx = 1; idx < lines.length; idx++)
			pool.add(split(lines[idx]));
		return pool;
	}

	
	private String[] split(String line) throws CSVException {
		Vector<String> strings = new Vector<String>();
		String field;
		int separatorIdx, fieldStart, fieldEnd;

		int idx = 0;
		while (idx>=0){
			field = "";
			fieldStart = line.indexOf(textSeparator, idx);
			separatorIdx = line.indexOf(fieldSeparator, idx);

			if (fieldStart>=0 && fieldStart < separatorIdx){
				fieldEnd = getFieldEnd(line, fieldStart+1);
				field =	line.substring(fieldStart+1, fieldEnd);
				field = field.replace("\"\"", "\"");
				int nextSeparator = line.indexOf(fieldSeparator, fieldEnd);
				idx = (nextSeparator<0)? -1 : nextSeparator+1;
			} else if (fieldStart < 0 ){
				if (separatorIdx<0) {
					field =	line.substring(idx);
					idx=-1;
				} else {
					field =	line.substring(idx, separatorIdx);
					idx = separatorIdx+1;
				}
			} else { // separatorIdx < fieldStart 
				if (separatorIdx<0) {
					fieldEnd = getFieldEnd(line, fieldStart+1);
					field = line.substring(fieldStart+1, fieldEnd);
					field = field.replace("\"\"", "\"");
					idx = -1;
				} else {
					field = line.substring(idx, separatorIdx);
					idx = separatorIdx+1;
				}
			}
			strings.add(field);
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	private int getFieldEnd(String line, int idx) throws CSVException{
		int end = line.indexOf(textSeparator, idx);
		if (end<0)
			throw new CSVException("malformed field in line : "+line);
		if (end<line.length()-1 && (line.charAt(end+1)==textSeparator))
			return getFieldEnd(line, end+2);
		else return end;
		
	}
}