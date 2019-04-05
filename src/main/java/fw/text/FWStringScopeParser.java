/**
 * 
 */
package fw.text;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWStringScopeParser implements FWScopeParserI {

	@Override
	public FWScopes parse(String text, FWScopes... scopes) {
		return new FWStringScopes(text, scopes);
	}

	private class FWStringScopes extends FWScopes {
		
		public FWStringScopes(String text, FWScopes... scopes) {
			parseStrings(text, scopes);
		}
		
		private void parseStrings(String text, FWScopes... scopes) {
			int start = text.indexOf('"');
			while (start>=0) {
				FWScope scope = FWScopes.getScopeAt(start, scopes);
				if (scope!=null)
					start = text.indexOf('"', scope.getEnd());
				else {
					int end = text.indexOf('"', start+1);
					while (end>0 && text.charAt(end-1)=='\\') { // skip escaped string marks
						end = text.indexOf('"', end+1);
					} 
					end =  (end<0)? start+1 : end+1;
					addScope(new FWScope(start, end));
					start = text.indexOf('"', end);
				}
			}
		}
		
	
	}
}
