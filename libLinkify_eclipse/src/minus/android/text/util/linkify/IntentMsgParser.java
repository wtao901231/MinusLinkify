package minus.android.text.util.linkify;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.text.TextUtils;
import minus.android.text.util.linkify.IntentMsgItem.IntentAttr;
import minus.android.util.Log;

public class IntentMsgParser extends CmdParser {

	private static final String LOG_TAG = IntentMsgParser.class.getSimpleName();

	private static final String INTENT_ATTR_REGEX = "<a intent=\\{.*\\}>.*</a>";
	public static final String INTENT_ATTR_PREFIX = "<a intent={";
	public static final String INTENT_ATTR_PREFIX_END = "}>";
	public static final String INTENT_ATTR_SUFFIX = "</a>";
	static Pattern sAttrPattern;
	static {
		if (null == sAttrPattern) {
			synchronized (IntentMsgParser.class) {
				if (null == sAttrPattern) {
					sAttrPattern = Pattern.compile(INTENT_ATTR_REGEX);
				}
			}
		}
	}

	public String parseSnippet(CharSequence text) {

		if (text instanceof String) {
			String str = (String) text;
			if (!(str.contains(IntentMsgParser.INTENT_ATTR_PREFIX) && str
					.contains(IntentMsgParser.INTENT_ATTR_SUFFIX))) {
				return str;
			}
		}

		StringBuilder builder = new StringBuilder();
		int start = 0;

		Matcher matcher = IntentMsgParser.sAttrPattern.matcher(text);
		while (matcher.find()) {
			try {
				MatchResult mr = matcher.toMatchResult();
				builder.append(text.subSequence(start, mr.start()));
				start = mr.end();
				builder.append(parseAttrName(mr.group(), mr.start(), mr.end()));
			} catch (Exception e) {
				Log.w(LOG_TAG, "continue, parse err: ", e);
			}
		}

		builder.append(text.subSequence(start, text.length()));

		return builder.toString();
	}

	private String parseAttrName(String group, int start, int end) {
		if (TextUtils.isEmpty(group)) {
			return "";
		}
		final int idxOfSeparator = group.indexOf(INTENT_ATTR_PREFIX_END);
		return group.substring(idxOfSeparator + INTENT_ATTR_PREFIX_END.length(),
				group.length() - INTENT_ATTR_SUFFIX.length()).trim();
	}

	public IntentMsgItem parse(CharSequence text) {
		IntentMsgItem item = new IntentMsgItem();
		Matcher matcher = sAttrPattern.matcher(text);
		while (matcher.find()) {
			try {
				MatchResult mr = matcher.toMatchResult();
				IntentAttr attr = parseAttr(mr.group(), mr.start(), mr.end());
				if (null != attr) {
					item.addIntentAttr(attr);
				}
			} catch (Exception e) {
				Log.w(LOG_TAG, "continue, parse err: ", e);
			}
		}
		return item;
	}

	private IntentAttr parseAttr(String group, int start, int end) {
		if (TextUtils.isEmpty(group)) {
			return null;
		}

		if (!(group.startsWith(INTENT_ATTR_PREFIX) && group.endsWith(INTENT_ATTR_SUFFIX))) {
			return null;
		}

		final String separator = "}>";
		final int idxOfSeparator = group.indexOf(separator);

		String args = group.substring(INTENT_ATTR_PREFIX.length(), idxOfSeparator);
		String name = group.substring(idxOfSeparator + separator.length(), group.length()
				- INTENT_ATTR_SUFFIX.length());

		Log.d(LOG_TAG, "message: ", name, " <INTENT> args: ", args);

		Intent intent = null;
		try {
			intent = makeIntent(args.trim());
		} catch (Exception e) {
			Log.w(LOG_TAG, "parseAttr err: ", e);
			intent = null;
		}
		if (null == intent) {
			return null;
		}

		IntentAttr attr = new IntentAttr();
		attr.intent = intent;
		attr.name = name.trim();
		attr.start = start;
		attr.end = end;

		return attr;
	}

}
