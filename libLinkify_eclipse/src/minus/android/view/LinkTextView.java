package minus.android.view;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;
import minus.android.text.util.linkify.IOnSpanClickListener;
import minus.android.text.util.linkify.LinkTouchMovementMethod;
import minus.android.text.util.linkify.Linkify;

public class LinkTextView extends TextView implements IOnSpanClickListener, IOnInterruptSpanClickFilter {

	public static final int LINKIFY_SPAN_FORE_COLOR_DEFAULT = Color.parseColor("#ff277bde");
    public static final int LINKIFY_SPAN_BACK_COLOR_DEFAULT = Color.parseColor("#3e277bde");

    private static int AUTO_LINK_MASK_REQUIRED = Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS;
    private static Map<String, Integer> AUTO_LINK_SCHEME_INTERRUPTED = new HashMap<String, Integer>();
    static {
        AUTO_LINK_SCHEME_INTERRUPTED.put("tel", Linkify.PHONE_NUMBERS);
        AUTO_LINK_SCHEME_INTERRUPTED.put("mailto", Linkify.EMAIL_ADDRESSES);
        AUTO_LINK_SCHEME_INTERRUPTED.put("http", Linkify.WEB_URLS);
        AUTO_LINK_SCHEME_INTERRUPTED.put("https", Linkify.WEB_URLS);
        AUTO_LINK_SCHEME_INTERRUPTED.put("rtsp", Linkify.WEB_URLS);
//        AUTO_LINK_SCHEME_INTERRUPTED.put("intent", Linkify.INTENT_SPAN);
    }

    private int mLinkColor = LINKIFY_SPAN_FORE_COLOR_DEFAULT;
    private int mBgColor = LINKIFY_SPAN_BACK_COLOR_DEFAULT;

    private int mAutoLinkMaskCompat;
    private LinkGestureDetector mLinkGestureDetector;

    public LinkTextView(Context context) {
    	this(context, null);
    }
    
    public LinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int origMask = getAutoLinkMask();
        if((origMask & android.text.util.Linkify.ALL) == android.text.util.Linkify.ALL) {
        	origMask = Linkify.ALL;
        }
        
        mAutoLinkMaskCompat = origMask | AUTO_LINK_MASK_REQUIRED;
        setAutoLinkMask(0);
        setMovementMethod(LinkTouchMovementMethod.getInstance());
        
        if(0 != origMask) { // reset
	        CharSequence text = getText();
	        if(!TextUtils.isEmpty(text)) {
	        	setText(text);
	        }
        }
        
        mLinkGestureDetector = new LinkGestureDetector();
        mLinkGestureDetector.setOnInterruptSpanClickFilter(this);
    }

    public void setLinkColor(int foreColor, int backColor) {
        mLinkColor = foreColor;
        mBgColor = backColor;
    }

    public int getAutoLinkMaskCompat() {
        return mAutoLinkMaskCompat;
    }

    public void setAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat = mask;
    }

    public void addAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat |= mask;
    }

    public void setOnLinkClickListener(IOnLinkClickListener l) {
    	mLinkGestureDetector.setOnLinkClickListener(l);
    }
    
    public void setOnInterruptSpanClickFilter(IOnInterruptSpanClickFilter filter) {
    	if(null == filter) {
    		mLinkGestureDetector.setOnInterruptSpanClickFilter(this);
    	} else {
    		mLinkGestureDetector.setOnInterruptSpanClickFilter(filter);
    	}
    }
    
    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!TextUtils.isEmpty(text)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            addLinks(builder);
            text = builder;
        }
		
        super.setText(text, type);
    }

    private void addLinks(SpannableStringBuilder builder) {
        setLinksClickable(true);
        Linkify.addLinks(builder, mAutoLinkMaskCompat, mLinkColor, mBgColor, this);
    }
    
    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent event) {
    	mLinkGestureDetector.onTouchEvent(event);
    	return super.onTouchEvent(event);
    }

	@Override
	public boolean onSpanClick(String url) {
		return mLinkGestureDetector.onSpanClick(url);
	}

	@Override
	public boolean onInterruptSpanClick(String scheme) {
		Integer flag = AUTO_LINK_SCHEME_INTERRUPTED.get(scheme);
        if (null != flag && 0 != (flag & mAutoLinkMaskCompat)) {
        	return true;
        }
        return false;
	}

}
