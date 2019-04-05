/**
 * 
 */
package geotortue.core;

import fw.text.FWScope;
import fw.text.FWScopeParserI;
import fw.text.FWScopes;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTCommentsScopeParser implements FWScopeParserI {

		@Override
		public FWScopes parse(String text, FWScopes... scopes) {
			return new GTCommentsScopes(text, scopes);
		}

		private class GTCommentsScopes extends FWScopes {
			
			public GTCommentsScopes(String text, FWScopes... scopes) {
				parseBlockComments(text, scopes);
				parseLineComments(text, "#", scopes);
				parseLineComments(text, "//", scopes);
			}
			
			private void parseBlockComments(String text, FWScopes... scopes) {
				int start = text.indexOf("/*");
				while (start>=0) {
					FWScope scope = FWScopes.getScopeAt(start, scopes);
					if (scope!=null)
						start = text.indexOf("/*", scope.getEnd());
					else {
						int end = text.indexOf("*/", start+2);
						end =  (end<0)? text.length() : end+2;
						addScope(new FWScope(start, end));
						start = text.indexOf("/*", end);
					}
				}
			}
			
			private void parseLineComments(String text, String tag, FWScopes... scopes) {
				int start = text.indexOf(tag);
				while (start>=0) {
					FWScope scope = FWScopes.getScopeAt(start, scopes);
					if (scope!=null)
						start = text.indexOf(tag, scope.getEnd());
					else {
						FWScope hit = getScope(start);
						if (hit != null)
							start = text.indexOf(tag, hit.getEnd());
						else {
							int end = text.indexOf("\n", start+2);
							end =  (end<0)? text.length() : end;
							addScope(new FWScope(start, end));
							start = text.indexOf(tag, end+1);
						}
					}
				}
			}
		
		}
}
