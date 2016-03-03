package minus.android.text.util.linkify;

import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkTouchDecorHelper {
	
	/**
	 * <pre>
	 * <b>Modified:</b>
	 * getHighlightColor() deprecated and removed, no invoke any more.
	 * 
	 * <b>Modified:</b>
	 * Conflict with bgColor when {@link #setPressed(boolean)} true
	 * 
	 * <b>Added:</b>
	 * See source code <a href=
	 * "https://github.com/android/platform_frameworks_base/blob/master/core/java/android/widget/TextView.java"
	 * >TextView.java</a> to get more info.
	 * // It is possible to have a selection even when mEditor is null (programmatically set, like when
	 * // a link is pressed). These highlight-related fields do not go in mEditor.
	 * int mHighlightColor = 0x6633B5E5;
	 * </pre>
	 * 
	 * @see TextView#setHighlightColor()
	 */
	public static interface ITouchableSpan {
		
		public void setPressed(boolean pressed);
		
	}
	
	public static void updatePressedColor(TextPaint ds, Integer color) {
		if(null != color) {
			ds.bgColor = color;
		}
	}
	
    private ITouchableSpan mPressedSpan;

    public void onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
//            	textView.setHighlightColor(mPressedSpan.getHighlightColor());
            	textView.setHighlightColor(Color.TRANSPARENT); // clear
                mPressedSpan.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        	ITouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan.setPressed(false);
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(false);
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
    }

    private ITouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);
        
        if(off >= spannable.length()) {
        	return null;
        }
        ITouchableSpan[] link = spannable.getSpans(off, off, ITouchableSpan.class);
        ITouchableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

}
