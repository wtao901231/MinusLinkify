package minus.android.view;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import minus.android.text.util.linkify.IOnSpanClickListener;
import minus.android.util.Log;

public class LinkGestureDetector implements IOnSpanClickListener {

	private static final String TAG = LinkGestureDetector.class.getSimpleName();
	
    private static final long TAP_TIMEOUT = 180; // ViewConfiguration.getTapTimeout();
    private static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    
    private long mDownMillis = 0;
    private IOnLinkClickListener mOnLinkClickListener = new SimpleOnLinkClickListener();
    private IOnInterruptSpanClickFilter mOnInterruptSpanClickFilter;
    
    public void setOnLinkClickListener(IOnLinkClickListener l) {
        mOnLinkClickListener = l;
    }
    
    public void setOnInterruptSpanClickFilter(IOnInterruptSpanClickFilter filter) {
    	mOnInterruptSpanClickFilter = filter;
    }
	
    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                boolean hasSingleTap = mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
                Log.w(TAG, "onTouchEvent hasSingleTap: ", hasSingleTap);
                if (!hasSingleTap) {
                    mDownMillis = SystemClock.uptimeMillis();
                } else {
                    Log.w(TAG, "onTouchEvent disallow onSpanClick mSingleTapConfirmedHandler because of DOUBLE TAP");
                    disallowOnSpanClickInterrupt();
                }
                break;
        }
    }

    private static final int MSG_CHECK_DOUBLE_TAP_TIMEOUT = 1000;
    private Handler mSingleTapConfirmedHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            if (MSG_CHECK_DOUBLE_TAP_TIMEOUT != msg.what) {
                return;
            }

            String url = (String) msg.obj;
            if (null != mOnLinkClickListener && !TextUtils.isEmpty(url)) {
                String schemeUrl = url.toLowerCase();
                if (schemeUrl.startsWith("tel:")) {
                    String phoneNumber = Uri.parse(url).getSchemeSpecificPart();
                    mOnLinkClickListener.onLinkClick(IOnLinkClickListener.LINK_TYPE_PHONE, phoneNumber);
                } else if (schemeUrl.startsWith("mailto:")) {
                    String mailAddr = Uri.parse(url).getSchemeSpecificPart();
                    mOnLinkClickListener.onLinkClick(IOnLinkClickListener.LINK_TYPE_EMAIL, mailAddr);
                } else if (schemeUrl.startsWith("http:") || schemeUrl.startsWith("https:") || schemeUrl.startsWith("rtsp:")) {
                    mOnLinkClickListener.onLinkClick(IOnLinkClickListener.LINK_TYPE_WEB, url);
                } else if(schemeUrl.startsWith("intent:")) {
                	mOnLinkClickListener.onLinkClick(IOnLinkClickListener.LINK_TYPE_INTENT, url);
                } else {
                	mOnLinkClickListener.onLinkClick(IOnLinkClickListener.LINK_TYPE_OTHER, url);
                }
            }
        }

    };

    private void disallowOnSpanClickInterrupt() {
        mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
        mDownMillis = 0;
    }

    @Override
    public boolean onSpanClick(String url) {
        if (null == url) {
            Log.w(TAG, "onSpanClick interrupt null url");
            return true;
        }
        long clickUpTime = (SystemClock.uptimeMillis() - mDownMillis);
        if (mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT)) {
            Log.w(TAG, "onSpanClick interrupted check DOUBLE_TAP_TIMEOUT#2: ", DOUBLE_TAP_TIMEOUT, DOUBLE_TAP_TIMEOUT - clickUpTime);
            disallowOnSpanClickInterrupt();
            return true;
        }
        if (TAP_TIMEOUT < clickUpTime) {
            Log.w(TAG, "onSpanClick interrupted because of TAP_TIMEOUT: ", TAP_TIMEOUT, clickUpTime);
            return true;
        }
        String scheme = Uri.parse(url).getScheme();
		if (null != scheme) {
			scheme = scheme.toLowerCase();
		}
		if(onInterruptSpanClick(scheme)) {
            long waitTime = DOUBLE_TAP_TIMEOUT - clickUpTime;
            Log.w(TAG, "onSpanClick interrupted wait for check DOUBLE_TAP_TIMEOUT#1: ", DOUBLE_TAP_TIMEOUT, waitTime);
            mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
            Message msg = Message.obtain();
            msg.what = MSG_CHECK_DOUBLE_TAP_TIMEOUT;
            msg.obj = url;
            mSingleTapConfirmedHandler.sendMessageDelayed(msg, waitTime);
            return true;
        }
        return false;
    }
    
    private boolean onInterruptSpanClick(String scheme) {
		if (null != mOnInterruptSpanClickFilter) {
			return mOnInterruptSpanClickFilter.onInterruptSpanClick(scheme);
    	}
		return false;
    }
	
}
