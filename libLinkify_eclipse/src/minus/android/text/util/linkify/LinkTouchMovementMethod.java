package minus.android.text.util.linkify;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkTouchMovementMethod extends LinkMovementMethod {
	
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
		sHelper.onTouchEvent(widget, buffer, event);
//		return super.onTouchEvent(widget, buffer, event);

		int action = event.getAction() & MotionEvent.ACTION_MASK;

		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);
			
			// bug fix: iff link[0] is at the end of text, touch outside will triggle onClick
			if(off >= buffer.length()) {
	        	return false;
	        }

			ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

			if (link.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					link[0].onClick(widget);
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
				}

				return true;
			} else {
				Selection.removeSelection(buffer);
			}
		}

		return Touch.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        return sInstance;
    }

    private static LinkTouchMovementMethod sInstance = new LinkTouchMovementMethod();
    private static LinkTouchDecorHelper sHelper = new LinkTouchDecorHelper();
    
}
