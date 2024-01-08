package fw.xml;

public interface XMLTagged {

	public String getXMLTag();
	
	public static class Factory {

		private Factory() {}
		
		public static XMLTagged create(final String tag) {
			return () -> tag;
		}
	}
}
