package fw.text;

import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

import fw.text.FWParsingTools.ParsingException.TYPE;


public class FWParsingTools {

	public static String DELIMITERS = " ²³,.;:{}()[]+-/*^%<=>!&|~\"`≤≥≠#";
	
	
	/**
	 * Splits the String str according to regular expression regExp and removes
	 * empty pieces.
	 * 
	 * @param str
	 * @param regExp
	 * @return
	 */
	public static String[] split(String content, String regExp) {
		String[] lines = (Pattern.compile(regExp)).split(content);
		lines = removeEmptyStrings(lines);
		return lines;
	}
	
	private static String[] removeEmptyStrings(String[] lines) {
		Vector<String> vector = new Vector<String>(lines.length);
		for (int idx = 0; idx < lines.length; idx++) {
			String line = lines[idx];
			if (line.trim().length() > 0) {
				vector.add(line);
			}
		}
		vector.trimToSize();
		return vector.toArray(new String[vector.size()]);
	}

	public static String removeJavaComments(String content) {
		content = removeBlockComments(content, "/*", "*/");
		content = removeEndLineComments(content, "//");
		return content;
	}

	public static String removeHTMLComments(String content) {
		content = removeBlockComments(content, "<!--", "-->");
		return content;
	}

	private static String removeBlockComments(String text, String startMark, String endMark) {
		String uncommented = text;
		int openIdx = uncommented.indexOf(startMark);
		if (openIdx != -1) {
			int closeIdx = uncommented.indexOf(endMark, openIdx);
			if (closeIdx == -1)
				return uncommented.substring(0, openIdx);
			uncommented = uncommented.replace(uncommented.substring(openIdx, closeIdx
					+ endMark.length()), "");
			return removeBlockComments(uncommented, startMark, endMark);
		}
		return uncommented;
	}

	private static String removeEndLineComments(String text, String mark) {
		String uncommented = text;
		int commentIdx = uncommented.indexOf(mark);
		if (commentIdx != -1) {
			int endIdx = uncommented.indexOf("\n", commentIdx);
			if (endIdx == -1) {
				uncommented = uncommented.substring(0, commentIdx);
			} else {
				uncommented = uncommented.replace(uncommented.substring(commentIdx, endIdx), "");
			}
			return removeEndLineComments(uncommented, mark);
		}
		return uncommented;
	}

	private static boolean isDelimiter(Character c) {
		if (Character.isWhitespace(c))
			return true;
		if (DELIMITERS.indexOf(c) != -1)
			return true;
		return false;
	}

	public static boolean containsDelimiter(String key) {
		for (int idx = 0; idx < DELIMITERS.length(); idx++) {
			if (key.indexOf(DELIMITERS.charAt(idx))>=0)
				return true;
		}
		return false;
	}
	
	public static String getNextToken(String content, int offset) {
		if (offset<0) {
			new Exception("negative offset").printStackTrace();
			return "";
		}
		int len = content.length();
		int idx = offset;
		while(idx<len && Character.isWhitespace(content.charAt(idx)))
			idx++;
		return getToken(content, idx);
	}

	public static String getToken(String content, int offset) {
		int contentLength = content.length();
		if (offset<0||offset>=contentLength||isDelimiter(content.charAt(offset)))
			return "";
		
		int startIdx = offset;
		int endIdx = offset+1;
		
		while (startIdx>0 && !isDelimiter(content.charAt(startIdx-1))){
			startIdx--;
		}
		
		while (endIdx<contentLength && !isDelimiter(content.charAt(endIdx))){
			endIdx++;
		}
		
		return content.substring(startIdx, endIdx);
	}

	public static int indexOfToken(String content, String token, int offset) {
		if (offset<0)
			return -1;
		int n = content.indexOf(token, offset);
		if (n<0)
			return n;
		
		if (n>0){
			char c=content.charAt(n-1);
			if (!isDelimiter(c))
				return indexOfToken(content, token, n+1);
		}
		
		if (n+token.length()<content.length())
			if (!isDelimiter(content.charAt(n+token.length())))
					return indexOfToken(content, token, n+1);
		
		return n;
	}
	
	public static int indexOfToken(String content, String token, int offset, FWScopes... scopes) {
		int idx = indexOfToken(content, token, offset);
		FWScope scope = getScopeAt(idx, scopes);
		if (scope == null)
			return idx;
		idx = scope.getEnd();
		return indexOfToken(content, token, idx, scopes);
	}
	
	public static int getEndOfToken(String content, int offset) {
		int idx = offset;
		int len = content.length();
		while (idx<len && !FWParsingTools.isDelimiter(content.charAt(idx)))
			idx++;
		return idx;
	}
	
	public static String replaceTokens(String content, String[] targets, String[] replacements, int startOffset, int endOffset) {
		int stringsLength=targets.length;
		String newContent=content.substring(0, startOffset);
		
		int offset=startOffset;
		while (offset<endOffset){
			int occurenceIdx=endOffset;
			String target="";
			String replacement="";

			// check for the first occurence of a target
			for (int idx = 0; idx < stringsLength; idx++) {
				int i = indexOfToken(content, targets[idx], offset);
				if (i>=0 && i<=occurenceIdx){
					occurenceIdx=i;
					target=targets[idx];
					replacement=replacements[idx];
				}
			}

			if (occurenceIdx<=endOffset){
				newContent+=content.substring(offset, occurenceIdx)+replacement;
				offset = occurenceIdx+target.length();
			} else break;
		}
		newContent+=content.substring(offset);
		return newContent;
	}
	
	public static String replaceTokens(String content, String[] targets, String[] replacements) {
		return replaceTokens(content, targets, replacements, 0, content.length());
	}

	public static String surroundTokens(String content, String[] targets, String prefix, String suffix) {
		String[] replacements = surround(targets, prefix, suffix);
		return replaceTokens(content, targets, replacements, 0, content.length());
	}

	private static String[] surround(String[] s, String prefix, String suffix) {
		String[] strs = new String[s.length];
		for (int idx = 0; idx < s.length; idx++) {
			strs[idx]=prefix+s[idx]+suffix;
		}
		return strs;
	}
	
	public static class ParsingException extends Exception {

		public enum TYPE {MISSING_CLOSING_BRACKET, MISSING_QUOTATION_MARK, MISMATCHING_BRACKETS};
		private final TYPE type;
		
		private static final long serialVersionUID = -8827398086131624853L;
		private final int index;

		public ParsingException(TYPE type, int idx) {
			super(type.toString());
			this.type=type;
			this.index = idx;
		}
		
		public int getIndex() {
			return index;
		}

		public TYPE getType() {
			return type;
		}
	}

	public static boolean isOpeningBracket(char c) {
		return (c=='(')||(c=='[')||(c=='{');
	}
	
	public static boolean isClosingBracket(char c) {
		return (c==')')||(c==']')||(c=='}');
	}
//	
	public static boolean match(char c1, char c2) {
		return (c1=='(' && c2==')')
				||(c1=='['&& c2==']')
				||(c1=='{'&& c2=='}' );
	}
	

	private static FWScope getScopeAt(int offset, FWScopes... scopesArray) {
		return FWScopes.getScopeAt(offset, scopesArray);
	}

	/**
	 * 
	 * @param str
	 * @param idx
	 * @param commentScopes
	 * @return bracket idx if not found
	 * @throws ParsingException if non mathcing brackets
	 */
	public static int getClosingBracketIdx(String str, int offset, FWScopes... scopesArray) throws ParsingException {
		int len = str.length();
		Stack<Character> stack = new Stack<Character>();
		int idx = offset;
		char c = str.charAt(idx);
		if (!isOpeningBracket(c))
			new Exception(c+" is not an opening bracket").printStackTrace();
		stack.push(c);
		idx++;
		while (idx < len && !stack.isEmpty()) {
			FWScope scope = getScopeAt(idx, scopesArray);
			if (scope != null)
				idx = scope.getEnd();
			else {
				c = str.charAt(idx);
				if (isOpeningBracket(c)) {
					stack.push(c);
					idx++;
				} else if (isClosingBracket(c)) {
					if (!match(stack.pop(), c)) {
						throw new ParsingException(TYPE.MISMATCHING_BRACKETS, idx);
					}
					idx++;
				} else
					idx++;
			}
		}

		if (!stack.isEmpty()) {
			throw new ParsingException(TYPE.MISSING_CLOSING_BRACKET, offset);
		}

		return idx - 1;
	}

	/**
	 * @param str
	 * @param idx
	 * @param commentScopes
	 * @return bracket idx or -1 if not found
	 * @throws ParsingException if non mathcing brackets
	 */
	public static int getOpeningBracketIdx(String str, int offset, FWScopes... scopesArray) throws ParsingException {
		Stack<Character> stack = new Stack<Character>();
		int idx = offset;
		char c = str.charAt(idx);
		if (!isClosingBracket(c))
			new Exception(c+" is not an closing bracket").printStackTrace();
		stack.push(c);
		idx--;
		while (idx>=0 && !stack.isEmpty()) {
			c = str.charAt(idx);
			if (isClosingBracket(c)) {
				FWScope scope = getScopeAt(idx, scopesArray);
				if (scope==null) {
					stack.push(c);
					idx--;
				} else
					idx = scope.getStart();
			} else if (isOpeningBracket(c)) {
				FWScope scope = getScopeAt(idx, scopesArray);
				if (scope==null) {
					if (!match(c, stack.pop())) 
						throw new ParsingException(TYPE.MISMATCHING_BRACKETS, idx);
					idx--;
				} else 
					idx = scope.getStart();
			} else if (c == '"') {
				idx = str.lastIndexOf('"', idx-1)-1;
				if (idx<0)
					return -1;
			} else
				idx--;
		}

		
		if (!stack.isEmpty())
			return -1;
		return idx+1;
	}
}