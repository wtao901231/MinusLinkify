package minus.android.text.util.linkify;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import minus.android.text.util.linkify.CmdParser.EmComponentType;

public class IntentSpan extends ClickableSpan implements LinkTouchDecorHelper.ITouchableSpan {

	private final Intent mIntent;
//	private final String mName;
	private final Integer mLinkColor;
	private final Integer mBgColor;
	private boolean mPressed = false;
	private IOnSpanClickListener mOnSpanClickListener;

	public IntentSpan(Intent intent, String name, Integer linkColor, Integer bgColor, IOnSpanClickListener l) {
		mIntent = intent;
//		mName = name;
		mLinkColor = linkColor;
		mBgColor = bgColor;
		mOnSpanClickListener = l;
	}

	@Override
	public void onClick(View widget) {
		if(null != mOnSpanClickListener) {
			if(mOnSpanClickListener.onSpanClick(mIntent.toUri(Intent.URI_INTENT_SCHEME))) {
				return;
			}
		}
		
		Context context = widget.getContext();
		try {
			EmComponentType type = CmdParser.getComponentType(mIntent);
			switch (type) {
			case ACTIVITY:
				context.startActivity(mIntent);
				break;
			case SERVICE:
				context.startService(mIntent);
				break;
			case BROADCAST:
				context.sendBroadcast(mIntent);
				break;
			default:
				context.startActivity(mIntent);
				break;
			}
		} catch (ActivityNotFoundException e) {
		} catch (Exception e) {
			// protect
		}
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		if (null != mLinkColor) {
			ds.linkColor = mLinkColor;
		}
		if(mPressed) {
			LinkTouchDecorHelper.updatePressedColor(ds, mBgColor);
		}
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
	}
	
	@Override
	public void setPressed(boolean pressed) {
		mPressed = pressed;
	}

}
