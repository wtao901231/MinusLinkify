package minus.android.text.util.linkify;

import android.text.style.URLSpan;
import android.view.View;

public abstract class StyleableURLSpan extends URLSpan implements LinkTouchDecorHelper.ITouchableSpan {
	
	protected boolean mPressed = false;
	protected String mUrl;
	protected IOnSpanClickListener mOnSpanClickListener;

	public StyleableURLSpan(String url, IOnSpanClickListener l) {
		super(url);
		mUrl = url;
		mOnSpanClickListener = l;
	}

	@Override
	public void setPressed(boolean pressed) {
		mPressed = pressed;
	}
	
	@Override
	public void onClick(View widget) {
		if(mOnSpanClickListener.onSpanClick(mUrl)) {
			return;
		}
		super.onClick(widget);
	}
	
}
