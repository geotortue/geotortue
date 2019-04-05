/**
 * 
 */
package fw.text;

/**
 *
 */
public class FWHTMLCommentScopeParser implements FWScopeParserI {

	@Override
	public FWScopes parse(String text, FWScopes... scopes) {
		return new FWHTMLCommentsScopes(text, scopes);
	}
	
	private class FWHTMLCommentsScopes extends FWScopes {
		
		public FWHTMLCommentsScopes(String text, FWScopes... scopes) {
			parseBlockComments(text, scopes);
		}
		
		private void parseBlockComments(String text, FWScopes... scopes) {
			int start = text.indexOf("<!--");;
			while (start>=0) {
				FWScope scope = FWScopes.getScopeAt(start, scopes);
				int end;
				if (scope != null)
					start = text.indexOf("<!--", scope.getEnd());
				else {
					end = text.indexOf("-->", start+4);
					end =  (end<0)? text.length() : end+3;
					addScope(new FWScope(start, end));
					start = text.indexOf("<!--", end);
				}
			}
		}
	}
}
