package geotortue.core;

import java.util.Vector;

public class GTCommandBundle extends SourceLocalization  {

	private final Vector<SourceLocalization> scopes;
	
	 private GTCommandBundle(SourceLocalization loc) throws EmptyCommandException, GTException {
		 super(loc.getProvider(), loc.getOffset(), loc.getLength());
		 if (getLength()<=0)
			 throw new EmptyCommandException();
		 this.scopes = GTCommandParser.parseScopes(this);
	 }

	 private GTCommandBundles parseBundle(SourceLocalization loc) throws GTException {
		 GTCommandBundles bundles = new GTCommandBundles();
		 for (SourceLocalization l : GTCommandParser.parse(loc)) {
			 try {
				 bundles.add(new GTCommandBundle(l));
			 } catch (EmptyCommandException ex) {
			 }
		 }
		return bundles;
	 }	

	 public static GTCommandBundles parse(SourceLocalization loc) throws GTException {
		GTCommandBundles bundles = new GTCommandBundles();
		for (SourceLocalization l : GTCommandParser.parse(loc)) {
			try {
				bundles.add(new GTCommandBundle(l));
			} catch (EmptyCommandException ex) {
				return bundles;
			} 
		}
		return bundles;
	 }
	

	protected int getArgumentsCount() {
		return scopes.size()-1;
	}

	public SourceLocalization getLocalizationAt(int pos) {
		return  scopes.get(pos); 
	}
	
	public SourceLocalization getLocalizationSince(int pos) {
		int start = scopes.get(pos).getOffset();
		SourceLocalization last = scopes.lastElement();
		int end = last.getOffset()+last.getLength();
		return  new SourceLocalization(getProvider(), start, end-start);
	}
	
	public GTCommandBundles parseBundleAt(int pos) throws GTException {
		SourceLocalization loc = getLocalizationAt(pos);
		return parseBundle(loc);
	}
	
	public GTCommandBundles parseBundleSince(int pos) throws GTException {
		return parseBundle(getLocalizationSince(pos));
	}
	
	public String getKey() {
		return getLocalizationAt(0).getText(); 
	}
	

	public String toString() {
		String  str = "[Bundle : "+ "\""+getText()+"\" ]"+"\n scopes = ";
		for (SourceLocalization scope : scopes) 
			str += "\""+scope.getText()+"\"\t";
		return str; 
	}

	static class EmptyCommandException extends Exception {
		private static final long serialVersionUID = -7061290695522144875L;
	}
}