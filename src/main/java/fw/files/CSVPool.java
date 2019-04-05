package fw.files;

import java.util.HashSet;
import java.util.Hashtable;


public class CSVPool {
	
	private HashSet<String[]> pool;
	
	final private int fieldsNumber;
	private String[] header;
	
	public CSVPool(String[] header, int initialCapacity){
		this.header = header;
		fieldsNumber = header.length;
		pool = new HashSet<String[]>(initialCapacity);
	}
	
	public CSVPool(String[] header){
		this(header, 16);
	}
	
	public String[] getHeader(){
		return header;
	}

	/**
	 * @return the pool
	 */
	public HashSet<String[]> getPool() {
		return pool;
	}

	/*
	 * 
	 */
	
	public void add(String[] fields) throws CSVException {
		if (fields.length!=fieldsNumber)
			throw new CSVException("Fields count (" + fields.length +
					") doesn't match the pool's one ("+ fieldsNumber +")");
		pool.add(fields);
	}
	
	public Hashtable<String, String> getTable(String column1, String column2) throws CSVException {
		int idx1 = -1;
		int idx2 = -1;
		for (int idx = 0; idx < header.length; idx++) {
			if (header[idx].equals(column1))
				idx1=idx;
			if (header[idx].equals(column2))
				idx2=idx;
		}
		if (idx1<0)
			throw new CSVException("String \""+column1+"\" not found in header.");
		
		if (idx2<0)
			throw new CSVException("String \""+column2+"\" not found in header.");

		Hashtable<String, String> table = new Hashtable<String, String>(pool.size());
		for (String[] line : pool) 
			table.put(line[idx1], line[idx2]);
		return table;
	}
	
}