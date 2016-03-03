package minus.android.text.util.linkify;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import minus.android.util.Log;


public class CmdParser {
	
	public enum EmComponentType {
		ACTIVITY("activity"),
		SERVICE("service"),
		BROADCAST("broadcast"),
		UNKOWN("?");
		
		private final String mCmpType;
		
		EmComponentType(String type) {
			mCmpType = type;
		}
		
		public boolean equals(String type) {
			return mCmpType.equals(type);
		}
		
		@Override
		public String toString() {
			return mCmpType;
		}
		
	}
	
	public static final String EXTRA_COMPONENT_TYPE = "component_type";
	
	public static EmComponentType getComponentType(Intent intent) {
		try {
			String type = intent.getStringExtra(EXTRA_COMPONENT_TYPE);
			if(!TextUtils.isEmpty(type)) {
				if(EmComponentType.ACTIVITY.equals(type)) {
					return EmComponentType.ACTIVITY;
				} else if(EmComponentType.SERVICE.equals(type)) {
					return EmComponentType.SERVICE;
				} else if(EmComponentType.BROADCAST.equals(type)) {
					return EmComponentType.BROADCAST;
				}
			}
		} catch(Exception e) {
			Log.w(LOG_TAG, "getComponentType err: ",	e);
		}

		return EmComponentType.UNKOWN;
	}
	
	private static final String LOG_TAG = "tagorewang:CmdParser";
	private static final boolean DEBUG = false;

	static class ParserHolder {
		public static CmdParser INSTANCE = new CmdParser();
	}
	
	protected String[] mArgs;
	protected int mNextArg;
	protected String mCurArgData;
	
	public static synchronized Intent parseIntent(String args) throws Exception {
		return ParserHolder.INSTANCE.makeIntent(args);
	}
	
	public static synchronized Intent parseIntent(String[] args) throws Exception {
		return ParserHolder.INSTANCE.makeIntent(args);
	}

	protected Intent makeIntent(String args) throws Exception {
		ArrayList<String> argsList = new ArrayList<String>();
		int idxOfStart = args.indexOf('"');
		String subArgs = substirngSafely(args, 0, idxOfStart);
		int idxOfEnd = indexOfSafely(args, '"', idxOfStart+1);
		String quotesArg = substirngSafely(args, idxOfStart+1, idxOfEnd);
		if(0 <= idxOfStart && 0 <= idxOfEnd) {
			do {
				String[] arrArgs = subArgs.split(" ");
				for(int i = 0; i != arrArgs.length; ++i) {
					if(!TextUtils.isEmpty(arrArgs[i])) {
						argsList.add(arrArgs[i]);
					}
				}
				if(!TextUtils.isEmpty(quotesArg)) {
					argsList.add(quotesArg);
				}
				
				idxOfStart = indexOfSafely(args, '"', idxOfEnd+1);
				if(idxOfStart > idxOfEnd) {
					subArgs = substirngSafely(args, idxOfEnd+1, idxOfStart);
				} else {
					subArgs = substirngSafely(args, idxOfEnd+1);
					break;
				}
				idxOfEnd = indexOfSafely(args, '"', idxOfStart+1);
				if(idxOfEnd > idxOfStart) {
					quotesArg = substirngSafely(args, idxOfStart+1, idxOfEnd);
				} else {
					subArgs = substirngSafely(args, idxOfStart+1 - subArgs.length());
					break;
				}
			} while(true);
			
			if(!TextUtils.isEmpty(subArgs)) {
				String[] arrArgs = subArgs.split(" ");
				for(int i = 0; i != arrArgs.length; ++i) {
					if(!TextUtils.isEmpty(arrArgs[i])) {
						argsList.add(arrArgs[i]);
					}
				}
			}
			
			mArgs = new String[argsList.size()];
			argsList.toArray(mArgs);
		} else {
			mArgs = args.split(" ");
		}
        mNextArg = 0;
		return makeIntent();
	}
	
	static int indexOfSafely(String str, int c, int start) {
		try {
			return str.indexOf(c, start);
		} catch(Exception e) {
			return -1;
		}
	}
	
	static String substirngSafely(String origStr, int start) {
		return substirngSafely(origStr, start, origStr.length());
	}
	
	static String substirngSafely(String origStr, int start, int end) {
		try {
			return origStr.substring(start, end);
		} catch(IndexOutOfBoundsException e) {
			if(0 > end || end > origStr.length()) {
				end = origStr.length();
			}
			if(start < 0) {
				start = 0;
			} else if(start > end) {
				start = end;
			}
			return origStr.substring(start, end);
		}
	}
	
	protected Intent makeIntent(String[] args) throws Exception {
		mArgs = args;
        mNextArg = 0;
		return makeIntent();
	}
	
	@SuppressLint("NewApi")
	protected Intent makeIntent() throws URISyntaxException {
		Intent intent = new Intent();
		Intent baseIntent = intent;
		boolean hasIntentInfo = false;
		Uri data = null;
		String type = null;
		String opt;
		while ((opt = nextOption()) != null) {
			if (opt.equals("-a")) {
				intent.setAction(nextArgRequired());
				if (intent == baseIntent) {
					hasIntentInfo = true;
				}
			} else if (opt.equals("-d")) {
				data = Uri.parse(nextArgRequired());
				if (intent == baseIntent) {
					hasIntentInfo = true;
				}
			} else if (opt.equals("-t")) {
				type = nextArgRequired();
				if (intent == baseIntent) {
					hasIntentInfo = true;
				}
			} else if (opt.equals("-c")) {
				intent.addCategory(nextArgRequired());
				if (intent == baseIntent) {
					hasIntentInfo = true;
				}
			} else if (opt.equals("-e") || opt.equals("--es")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				intent.putExtra(key, value);
			} else if (opt.equals("--esn")) {
				String key = nextArgRequired();
				intent.putExtra(key, (String) null);
			} else if (opt.equals("--ei")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				intent.putExtra(key, Integer.valueOf(value));
			} else if (opt.equals("--eu")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				intent.putExtra(key, Uri.parse(value));
			} else if (opt.equals("--eia")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				String[] strings = value.split(",");
				int[] list = new int[strings.length];
				for (int i = 0; i < strings.length; i++) {
					list[i] = Integer.valueOf(strings[i]);
				}
				intent.putExtra(key, list);
			} else if (opt.equals("--el")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				intent.putExtra(key, Long.valueOf(value));
			} else if (opt.equals("--ela")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				String[] strings = value.split(",");
				long[] list = new long[strings.length];
				for (int i = 0; i < strings.length; i++) {
					list[i] = Long.valueOf(strings[i]);
				}
				intent.putExtra(key, list);
			} else if (opt.equals("--ez")) {
				String key = nextArgRequired();
				String value = nextArgRequired();
				intent.putExtra(key, Boolean.valueOf(value));
			} else if(opt.equals("--intent")) {
				String key = nextArgRequired();
				try {
					CmdParser parser = new CmdParser();
					Intent extraIntent = parser.makeIntent(nextArgRequired());
					intent.putExtra(key, extraIntent);
				} catch (Exception e) {
					Log.w(LOG_TAG, "makeIntent extra Intent error, key: ", key, e);
				}
			} else if (opt.equals("-n")) {
				String str = nextArgRequired();
				ComponentName cn = ComponentName.unflattenFromString(str);
				if (cn == null)
					throw new IllegalArgumentException("Bad component name: "
							+ str);
				intent.setComponent(cn);
				if (intent == baseIntent) {
					hasIntentInfo = true;
				}
			} else if (opt.equals("-f")) {
				String str = nextArgRequired();
				intent.setFlags(Integer.decode(str).intValue());
			} else if (opt.equals("--grant-read-uri-permission")) {
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			} else if (opt.equals("--grant-write-uri-permission")) {
				intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			} else if (opt.equals("--exclude-stopped-packages")) {
				intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
			} else if (opt.equals("--include-stopped-packages")) {
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			} else if (opt.equals("--debug-log-resolution")) {
				intent.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
			} else if (opt.equals("--activity-brought-to-front")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			} else if (opt.equals("--activity-clear-top")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			} else if (opt.equals("--activity-clear-when-task-reset")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			} else if (opt.equals("--activity-exclude-from-recents")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			} else if (opt.equals("--activity-launched-from-history")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
			} else if (opt.equals("--activity-multiple-task")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			} else if (opt.equals("--activity-no-animation")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			} else if (opt.equals("--activity-no-history")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			} else if (opt.equals("--activity-no-user-action")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
			} else if (opt.equals("--activity-previous-is-top")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			} else if (opt.equals("--activity-reorder-to-front")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			} else if (opt.equals("--activity-reset-task-if-needed")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			} else if (opt.equals("--activity-single-top")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			} else if (opt.equals("--activity-clear-task")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			} else if (opt.equals("--activity-task-on-home")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
			} else if (opt.equals("--receiver-registered-only")) {
				intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
			} else if (opt.equals("--receiver-replace-pending")) {
				intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
			} else if (opt.equals("--selector")) {
				intent.setDataAndType(data, type);
				intent = new Intent();
			} else if(opt.equals("--component-type")) {
				intent.putExtra(EXTRA_COMPONENT_TYPE, nextArgRequired());
			} else {
				Log.w(LOG_TAG, "Error: Unknown option: " + opt);
				showUsage();
				return null;
			}
		}
		intent.setDataAndType(data, type);
		final boolean hasSelector = intent != baseIntent;
		if (Build.VERSION.SDK_INT >= 15) { // Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			if (hasSelector) {
				// A selector was specified; fix up.
				baseIntent.setSelector(intent);
				intent = baseIntent;
			}
		}
		String arg = nextArg();
		baseIntent = null;
		if (arg == null) {
			if (hasSelector) {
				// If a selector has been specified, and no arguments
				// have been supplied for the main Intent, then we can
				// assume it is ACTION_MAIN CATEGORY_LAUNCHER; we don't
				// need to have a component name specified yet, the
				// selector will take care of that.
				baseIntent = new Intent(Intent.ACTION_MAIN);
				baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			}
		} else if (arg.indexOf(':') >= 0) {
			// The argument is a URI. Fully parse it, and use that result
			// to fill in any data not specified so far.
			try {
				//baseIntent = Intent.parseUri(arg, Intent.URI_INTENT_SCHEME);
				//��ʱ���Σ���Ϊ�˴�ʵ��������Ҳ��ʱ�ò��������Ҳ����ι����˽����� yongzhiguo
			} catch (Throwable e) {
				Log.w(LOG_TAG, "", "");
			}
			if(baseIntent == null){
				baseIntent = new Intent(Intent.ACTION_MAIN);
				baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			}
		} else if (arg.indexOf('/') >= 0) {
			// The argument is a component name. Build an Intent to launch
			// it.
			baseIntent = new Intent(Intent.ACTION_MAIN);
			baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			baseIntent.setComponent(ComponentName.unflattenFromString(arg));
		} else {
			// Assume the argument is a package name.
			baseIntent = new Intent(Intent.ACTION_MAIN);
			baseIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			baseIntent.setPackage(arg);
		}
		if (baseIntent != null) {
			Bundle extras = intent.getExtras();
			intent.replaceExtras((Bundle) null);
			Bundle uriExtras = baseIntent.getExtras();
			baseIntent.replaceExtras((Bundle) null);
			if (intent.getAction() != null
					&& baseIntent.getCategories() != null) {
				HashSet<String> cats = new HashSet<String>(
						baseIntent.getCategories());
				for (String c : cats) {
					baseIntent.removeCategory(c);
				}
			}
			intent.fillIn(baseIntent, Intent.FILL_IN_COMPONENT
					| Intent.FILL_IN_SELECTOR);
			if (extras == null) {
				extras = uriExtras;
			} else if (uriExtras != null) {
				uriExtras.putAll(extras);
				extras = uriExtras;
			}
			intent.replaceExtras(extras);
			hasIntentInfo = true;
		}
		if (!hasIntentInfo)
			throw new IllegalArgumentException("No intent supplied");
		return intent;
	}
	
	protected static void showUsage() {
		showUsage(false);
	}

	protected static void showUsage(boolean force) {
		if(!(force || DEBUG)) {
			return;
		}
	    System.out.println(
	            "<INTENT> specifications include these flags and arguments:\n" +
	            "    [-a <ACTION>] [-d <DATA_URI>] [-t <MIME_TYPE>]\n" +
	            "    [-c <CATEGORY> [-c <CATEGORY>] ...]\n" +
	            "    [-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...]\n" +
	            "    [--esn <EXTRA_KEY> ...]\n" +
	            "    [--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...]\n" +
	            "    [--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...]\n" +
	            "    [--el <EXTRA_KEY> <EXTRA_LONG_VALUE> ...]\n" +
	            "    [--eu <EXTRA_KEY> <EXTRA_URI_VALUE> ...]\n" +
	            "    [--eia <EXTRA_KEY> <EXTRA_INT_VALUE>[,<EXTRA_INT_VALUE...]]\n" +
	            "    [--ela <EXTRA_KEY> <EXTRA_LONG_VALUE>[,<EXTRA_LONG_VALUE...]]\n" +
	            "    [--intent <EXTRA_KEY> <INTENT>]\n" +
	            "    [-n <COMPONENT>] [-f <FLAGS>]\n" +
	            "    [--grant-read-uri-permission] [--grant-write-uri-permission]\n" +
	            "    [--debug-log-resolution] [--exclude-stopped-packages]\n" +
	            "    [--include-stopped-packages]\n" +
	            "    [--activity-brought-to-front] [--activity-clear-top]\n" +
	            "    [--activity-clear-when-task-reset] [--activity-exclude-from-recents]\n" +
	            "    [--activity-launched-from-history] [--activity-multiple-task]\n" +
	            "    [--activity-no-animation] [--activity-no-history]\n" +
	            "    [--activity-no-user-action] [--activity-previous-is-top]\n" +
	            "    [--activity-reorder-to-front] [--activity-reset-task-if-needed]\n" +
	            "    [--activity-single-top] [--activity-clear-task]\n" +
	            "    [--activity-task-on-home]\n" +
	            "    [--receiver-registered-only] [--receiver-replace-pending]\n" +
	            "    [--selector]\n" +
	            "    [--component-type activity | service | broadcast]\n" +
	            "    [<URI> | <PACKAGE> | <COMPONENT>]\n"
	            );
	}

	private String nextOption() {
		if (mCurArgData != null) {
			String prev = mArgs[mNextArg - 1];
			throw new IllegalArgumentException("No argument expected after \""
					+ prev + "\"");
		}
		if (mNextArg >= mArgs.length) {
			return null;
		}
		String arg = mArgs[mNextArg];
		if (!arg.startsWith("-")) {
			return null;
		}
		mNextArg++;
		if (arg.equals("--")) {
			return null;
		}
		if (arg.length() > 1 && arg.charAt(1) != '-') {
			if (arg.length() > 2) {
				mCurArgData = arg.substring(2);
				return arg.substring(0, 2);
			} else {
				mCurArgData = null;
				return arg;
			}
		}
		mCurArgData = null;
		return arg;
	}

	private String nextArg() {
		if (mCurArgData != null) {
			String arg = mCurArgData;
			mCurArgData = null;
			return arg;
		} else if (mNextArg < mArgs.length) {
			return mArgs[mNextArg++];
		} else {
			return null;
		}
	}

	private String nextArgRequired() {
		String arg = nextArg();
		if (arg == null) {
			String prev = mArgs[mNextArg - 1];
			throw new IllegalArgumentException("Argument expected after \""
					+ prev + "\"");
		}
		return arg;
	}

}
