package fw.xml;

public interface XMLTagged {

	public String getXMLTag();
	
	public static class Factory {
		public static XMLTagged create(final String tag) {
			return new XMLTagged() {
				@Override
				public String getXMLTag() {
					return tag;
				}
			};
		}
	}
}
