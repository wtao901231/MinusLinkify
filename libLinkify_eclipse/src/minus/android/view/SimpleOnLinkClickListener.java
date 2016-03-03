package minus.android.view;

import minus.android.util.Log;

public class SimpleOnLinkClickListener implements IOnLinkClickListener {

	private static final String TAG = SimpleOnLinkClickListener.class.getSimpleName();
	
	@Override
	public void onLinkClick(int type, String value) {
		Log.d(TAG, "onLinkClick type:", type, " value:", value);
	}

}