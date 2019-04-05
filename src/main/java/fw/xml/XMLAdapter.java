package fw.xml;

public abstract class XMLAdapter implements XMLCapabilities {
	private final String key;

	public XMLAdapter(String key) {
		this.key = key;
	}

	@Override
	public String getXMLTag() {
		return key;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return null;
	}

	@Override
	public abstract XMLWriter getXMLProperties();
}