package minus.android.text.util.linkify;

import java.util.List;

import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import minus.android.text.util.linkify.IntentMsgItem.IntentAttr;

public class IntentMsgUtil {

	private static final String LOG_TAG = IntentMsgUtil.class.getSimpleName();

	private static IntentMsgParser sParser = new IntentMsgParser();

	public static String parseSnippet(CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			return "";
		}

		CharSequence link = sParser.parseSnippet(text);
		if (!TextUtils.isEmpty(link)) {
			return link.toString();
		}

		return sParser.parseSnippet(text);
	}

	public static IntentMsgItem parse(CharSequence orig) {
		IntentMsgItem item = sParser.parse(orig);
		return parseInternal(item, orig);
	}

	private static IntentMsgItem parseInternal(IntentMsgItem item, CharSequence orig) {
		if (null == item) {
			return null;
		}
		List<IntentAttr> attrs = item.getIntentAttrs();
		if (attrs.isEmpty()) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		int start = 0;
		for (IntentAttr attr : attrs) {
			builder.append(orig.subSequence(start, attr.start));
			attr.start = builder.length();
			builder.append(attr.name);
			start = attr.end;
			attr.end = builder.length();
		}
		builder.append(orig.subSequence(start, orig.length()));

		String parsed = builder.toString();
		//Log.d(LOG_TAG, "parse orig:", orig, " parsed: ", parsed);

		item.setMsgBody(parsed);
		return item;
	}

	public static boolean applyLink(IntentMsgItem item, Spannable text, Integer linkColor, Integer bgColor, IOnSpanClickListener l) {
		if (null == item || TextUtils.isEmpty(text)) {
			return false;
		}

		if (!text.toString().equals(item.getMsgBody())) {
			//Log.d(LOG_TAG, "applyLink inconsistent text: ", text, " parsed: ", item.getMsgBody());
			return false;
		}

		List<IntentAttr> attrs = item.getIntentAttrs();
		if (attrs.isEmpty()) {
			Log.d(LOG_TAG, "applyLink empty intent attrs");
			return false;
		}

		for (IntentAttr attr : attrs) {
			text.setSpan(new IntentSpan(attr.intent, attr.name, linkColor, bgColor, l), attr.start,
					attr.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return true;
	}

}
