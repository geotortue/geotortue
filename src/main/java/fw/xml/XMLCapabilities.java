package fw.xml;

public interface XMLCapabilities extends XMLTagged{
	
	public XMLWriter getXMLProperties(); 

	/**
	 * 
	 * @param e
	 * @return
	 * @throws XMLException
	 */
	public XMLReader loadXMLProperties(XMLReader e);

	public String getXMLTag();
}
