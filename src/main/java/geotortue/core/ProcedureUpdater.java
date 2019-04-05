package geotortue.core;

import java.util.HashMap;

import fw.text.FWParsingTools;
import fw.text.FWScope;
import fw.text.FWScopes;
import geotortue.core.GTMessageFactory.GTTrouble;

public class ProcedureUpdater {

	private final SourceProvider provider;
	
	public ProcedureUpdater(SourceProvider provider) {
		super();
		this.provider = provider;
	}

	public HashMap<String, ProtoProcedure> parse(int start, int end, FWScopes commentScopes, FWScopes stringScopes) throws GTException {
		HashMap<String, ProtoProcedure> map = new HashMap<>();
		String content = provider.getText(start, end);
		
		int offset = start;

		while (offset > -1) {

			int startOffset = FWParsingTools.indexOfToken(content, KeywordManager.START_KEY.translate(), offset);
			if (startOffset < 0 || startOffset >= end)
				return map;

			FWScope cScope = commentScopes.getScope(startOffset);
			FWScope sScope = stringScopes.getScope(startOffset);
			if (cScope != null)
				offset = cScope.getEnd();
			else if (sScope != null)
				offset = sScope.getEnd();
			else {
				int nextStartOffset = FWParsingTools.indexOfToken(content, KeywordManager.START_KEY.translate(),
						offset + 1);
				int stopOffset = FWParsingTools.indexOfToken(content, KeywordManager.END_KEY.translate(), offset);

				int endOffset;
				if (nextStartOffset > -1)
					endOffset = (stopOffset > -1) ? Math.min(stopOffset, nextStartOffset) : nextStartOffset;
				else
					endOffset = (stopOffset > -1) ? stopOffset : content.length();

				int len = endOffset - startOffset;

				if (startOffset > -1 && (len > 0)) {
					SourceLocalization loc = new SourceLocalization(provider, startOffset, len);
					ProtoProcedure proc = new ProtoProcedure(loc);
					String key = proc.getKey();
					if (map.get(key)!=null) {
						throw new GTException(GTTrouble.GTJEP_CONFLICT_PROCEDURE, loc, key);
					}
					if (key != null)
						map.put(key, proc);
				}
				offset = nextStartOffset;
			}
		}
		
		return map;
	}
	
	public class ProtoProcedure {
		private final SourceLocalization localization;
		private final String key;
		
		public ProtoProcedure(SourceLocalization loc) {
			this.localization = loc;
			String text = loc.getText();
			int idx = KeywordManager.START_KEY.translate().length();
			String s = FWParsingTools.getNextToken(text, idx);
			if (s.length()>0)
				this.key = s;
			else
				this.key = null;
		}

		public int getOffset() {
			return localization.getOffset();
		}

		public int getLength() {
			return localization.getLength();
		}

		public String getKey() {
			return key;
		}


		public String toString() {
			return super.toString()+" key="+key;
		}

		/**
		 * @return
		 */
		public SourceLocalization getLocalization() {
			return localization;
		}
	}
}