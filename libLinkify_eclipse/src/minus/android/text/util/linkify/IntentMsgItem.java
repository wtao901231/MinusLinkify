package minus.android.text.util.linkify;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

public class IntentMsgItem {
	
	static class IntentAttr {
		public Intent intent;
		public String name;
		public int start;
		public int end;
	}
	
	private final List<IntentAttr> mAttrs;
	private String mParsedMsg;
	
	public IntentMsgItem() {
		mAttrs = new ArrayList<IntentAttr>();
	}
	
	public String getMsgBody() {
		return mParsedMsg;
	}
	
	public void setMsgBody(String body) {
		mParsedMsg = body;
	}
	
	void addIntentAttr(IntentAttr attr) {
		mAttrs.add(attr);
	}
	
	List<IntentAttr> getIntentAttrs() {
		return mAttrs;
	}
	
}
