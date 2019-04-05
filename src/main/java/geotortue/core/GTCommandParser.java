/**
 * 
 */
package geotortue.core;

import java.util.Vector;

import org.nfunk.jep.addon.JEPTroubleI;

import fw.text.FWParsingTools;
import fw.text.FWParsingTools.ParsingException;
import fw.text.FWScope;
import fw.text.FWScopeParserI;
import fw.text.FWScopes;
import fw.text.FWStringScopeParser;
import geotortue.core.GTCommandBundle.EmptyCommandException;
import geotortue.core.GTMessageFactory.GTTrouble;

public class GTCommandParser {

	private static final FWScopeParserI commentScopeParser = new GTCommentsScopeParser();
	private static final FWScopeParserI stringScopeParser = new FWStringScopeParser();

	/**
	 * Split src according to ';' and '\n'
	 **/
	public static Vector<SourceLocalization> parse(SourceLocalization src) throws GTException {
		Vector<SourceLocalization> locs = new Vector<>();
		SourceLocalization loc;
		try {
			loc = trim(src);
		} catch (EmptyCommandException ex) {
			return locs;
		}

		String text = loc.getRawText();
		int offset = loc.getOffset();
		int len = loc.getLength();

		int idx = 0;
		int startIdx = 0;

		FWScopes stringScopes = stringScopeParser.parse(text);
		FWScopes commentScopes = commentScopeParser.parse(text, stringScopes);

		while (idx < len) {
			FWScope cScope = commentScopes.getScope(idx);
			FWScope sScope = stringScopes.getScope(idx);
			if (cScope != null) {
				idx = cScope.getEnd();
			} else if (sScope != null) {
				idx = sScope.getEnd();
			} else if (FWParsingTools.isOpeningBracket(text.charAt(idx))) {
				idx = getClosingBracketIdx(loc, text, idx, stringScopes, commentScopes);
			} else {
				char c = text.charAt(idx);
				if (c == ';' || c == '\n') {
					try {
						locs.add(trim(new SourceLocalization(loc.getProvider(), offset + startIdx, idx - startIdx)));
					} catch (EmptyCommandException ex) {
					}
					idx++;
					startIdx = idx;
				} else
					idx++;
			}
		}
		try {
			locs.add(trim(new SourceLocalization(loc.getProvider(), offset + startIdx, len - startIdx)));
		} catch (EmptyCommandException ex) {
		}
		return locs;
	}

	/*
	 * trim and remove comments
	 */
	public static SourceLocalization trim(SourceLocalization loc) throws EmptyCommandException {
		String text = loc.getRawText();

		int start = 0;
		int end = loc.getLength();
		boolean shallContinue = true;
		FWScopes stringScopes = stringScopeParser.parse(text);
		FWScopes commentScopes = commentScopeParser.parse(text, stringScopes);
		
		while (shallContinue && start < end) {
			boolean trimPerformed = false;
			FWScope cStartScope = commentScopes.getScope(start);
			FWScope cEndScope = commentScopes.getScope(end - 1);
			if (cEndScope != null) { // remove end comments
				end = cEndScope.getStart();
			} else if (cStartScope != null) { // remove start comments
				start = cStartScope.getEnd();
			} else {
				char startC = text.charAt(start);
				while (Character.isWhitespace(startC)) { // trim starting white spaces
					trimPerformed = true;
					start++;
					if (start >= end)
						throw new EmptyCommandException();
					startC = text.charAt(start);
				}

				char endC = text.charAt(end - 1);
				while (Character.isWhitespace(endC)) { // trim ending white spaces
					trimPerformed = true;
					end--;
					if (start >= end)
						throw new EmptyCommandException();
					endC = text.charAt(end - 1);
				}

				if (!trimPerformed) {
					if (FWParsingTools.match(startC, endC)) { // trim brackets
						try {
							int cidx = FWParsingTools.getClosingBracketIdx(text, start, stringScopes, commentScopes);
							if (cidx == end - 1) {
								start++;
								end--;
							} else {
								shallContinue = false; // trim done
							}
						} catch (ParsingException e) {
							shallContinue = false; // trim done
						}
					} else
						shallContinue = false; // trim done
				}
			}
		}

		if (start >= end)
			throw new EmptyCommandException();
		return new SourceLocalization(loc.getProvider(), loc.getOffset() + start, end - start);
	}

	/**
	 * Split loc according to whitespaces
	 * 
	 * @param loc
	 * @return
	 * @throws GTException
	 */
	public static Vector<SourceLocalization> parseScopes(SourceLocalization loc) throws GTException {
		return parseScopes(loc, BYPASS_TESTER);
	}
	
	/**
	 * add hoc method to parse the 7 first scopes
	 * @param loc
	 * @return
	 * @throws GTException
	 */
	public static Vector<SourceLocalization> parse7FirstScopes(SourceLocalization loc) throws GTException {
		return parseScopes(loc, SCOPES_SIZE_LT_7_TESTER);
	}
	
	private final static ScopesTester BYPASS_TESTER = new ScopesTester() {
		
		@Override
		public void test(Vector<SourceLocalization> locs) throws AbortParsingException {}
	};
	
	private final static ScopesTester SCOPES_SIZE_LT_7_TESTER = new ScopesTester() {
	
		@Override
		public void test(Vector<SourceLocalization> locs) throws AbortParsingException {
			if (locs.size()>7)
				throw new AbortParsingException();
		}
	};
	
	private static class AbortParsingException extends Exception {
		private static final long serialVersionUID = -7585155262195265547L;
	}
	
	private static Vector<SourceLocalization> parseScopes(SourceLocalization loc, ScopesTester tester) throws GTException {
		String text = loc.getRawText();
		Vector<SourceLocalization> scopes = new Vector<>();
		int start = -1;
		int idx = 0;
		int len = text.length();
		
		FWScopes stringScopes = stringScopeParser.parse(text);
		FWScopes commentScopes = commentScopeParser.parse(text, stringScopes);
		
		try {
			if (stringScopes.isEmpty() && commentScopes.isEmpty()) {
				while (idx < len) {
					char c = text.charAt(idx);
					if (FWParsingTools.isOpeningBracket(c)) {
						if (start < 0)
							start = idx;
						idx = getClosingBracketIdx(loc, text, idx);
					} else if (Character.isWhitespace(c)) {
						if (start >= 0) {
							scopes.add(loc.getSubLocalization(start, idx-start));
							tester.test(scopes);
							start = -1;
						} else {
							while (Character.isWhitespace(c) && idx < len) {
								idx++;
								if (idx == len)
									return scopes;
								c = text.charAt(idx);
							}
						}
					} else {
						if (start < 0)
							start = idx;
						if (idx == len - 1) {
							scopes.add(loc.getSubLocalization(start, len-start));
							tester.test(scopes);
						}
						idx++;
					}
				}
			} else {
				while (idx < len) {
					FWScope cScope = commentScopes.getScope(idx);
					FWScope sScope = stringScopes.getScope(idx);
					if (cScope != null) {
						idx = cScope.getEnd();
					} else if (sScope != null) {
						if (start<0)
							start = sScope.getStart();
						idx = sScope.getEnd();
						if (idx < 0 || start == idx) {
							SourceProvider provider = loc.getProvider();
							SourceLocalization loc2 = new SourceLocalization(provider, loc.getOffset() + start, 1);
							throw new GTException(GTTrouble.GTJEP_MISSING_QUOTATION_MARK, loc2);
						}
						scopes.add(loc.getSubLocalization(start, idx-start));
						tester.test(scopes);
						start = -1;
					} else {
	
						char c = text.charAt(idx);
						if (FWParsingTools.isOpeningBracket(c)) {
							if (start < 0)
								start = idx;
							idx = getClosingBracketIdx(loc, text, idx, stringScopes, commentScopes);
						} else if (Character.isWhitespace(c)) {
							if (start >= 0) {
								scopes.add(loc.getSubLocalization(start, idx-start));
								tester.test(scopes);
								start = -1;
							} else {
								while (Character.isWhitespace(c) && idx < len) {
									idx++;
									if (idx == len)
										return scopes;
									c = text.charAt(idx);
								}
							}
						} else {
							if (start < 0)
								start = idx;
							if (idx == len - 1) {
								scopes.add(loc.getSubLocalization(start, len-start));
								tester.test(scopes);
							}
							idx++;
						}
					}
				}
			}
		} catch (AbortParsingException ex){
		}
		return scopes;
	}
	
	private interface ScopesTester {
		public void test(Vector<SourceLocalization> locs) throws AbortParsingException;
	}
	
	private static int getClosingBracketIdx(SourceLocalization loc, String text, int idx, FWScopes... scopes)
			throws GTException {
		try {
			return FWParsingTools.getClosingBracketIdx(text, idx, scopes);
		} catch (ParsingException ex) {
			JEPTroubleI trouble = null;
			switch (ex.getType()) {
			case MISSING_CLOSING_BRACKET:
				trouble = GTTrouble.GTJEP_MISSING_CLOSING_BRACKET;
				break;
			case MISMATCHING_BRACKETS:
				trouble = GTTrouble.GTJEP_MISMATCHING_BRACKETS;
				break;
			case MISSING_QUOTATION_MARK:
				trouble = GTTrouble.GTJEP_MISSING_QUOTATION_MARK;
				break;
			}
			SourceProvider provider = loc.getProvider();
			int offset = loc.getOffset();
			SourceLocalization loc2 = new SourceLocalization(provider, ex.getIndex() + offset, 1);
			throw new GTException(trouble, loc2);
		}
	}
	
}