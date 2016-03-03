package minus.android.util;

public class Log {
	
	private static final int DISPLAY_MIN_PRIORITY = android.util.Log.VERBOSE;

	public static void v(String tag, Object... msgs) {
		println(android.util.Log.VERBOSE, tag, concatLogMessage(msgs));
	}

	public static void d(String tag, Object... msgs) {
		println(android.util.Log.DEBUG, tag, concatLogMessage(msgs));
	}

	public static void i(String tag, Object... msgs) {
		println(android.util.Log.INFO, tag, concatLogMessage(msgs));
	}

	public static void w(String tag, Object... msgs) {
		println(android.util.Log.WARN, tag, concatLogMessage(msgs));
	}

	public static void e(String tag, Object... msgs) {
		println(android.util.Log.ERROR, tag, concatLogMessage(msgs));
	}
	
	public static int println(int priority, String tag, String msg) {
		if(priority < DISPLAY_MIN_PRIORITY) {
			priority = DISPLAY_MIN_PRIORITY;
		}
		return android.util.Log.println(priority, tag, msg);
	}
	
	public static int println(int priority, String tag, String msg, Throwable tr) {
		if(priority < DISPLAY_MIN_PRIORITY) {
			priority = DISPLAY_MIN_PRIORITY;
		}
		return android.util.Log.println(priority, tag, msg + '\n' + android.util.Log.getStackTraceString(tr));
	}

	public static void printStackTrace(String tag, String reason) {
		printStackTrace(android.util.Log.WARN, tag, reason, Integer.MAX_VALUE);
	}
	
	public static void printStackTrace(int priority, String tag, String reason, int count) {
		Throwable tr = new Throwable(reason);
		
		try {
			StackTraceElement[] elems = tr.getStackTrace();
			count = Math.min(elems.length-1, count);
			StringBuilder sb = new StringBuilder();
			sb.append(reason).append('\n');
			for(int i = 0; i != count; ++i) {
				sb.append(elems[i+1]).append('\n');
			}
			String log = sb.toString();
			println(priority, tag, log);
		} catch(Exception e) {
			println(android.util.Log.WARN, tag, "printStackTrace failsafe: ", tr);
		}
	}
	
	private static String concatLogMessage(Object... msgs) {
		if(0 == msgs.length) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < msgs.length-1; ++i) {
			Object msg = msgs[i];
			builder.append(msg);
		}
		Object msg = msgs[msgs.length-1];
		if(msg instanceof Throwable) {
			builder.append('\n' + android.util.Log.getStackTraceString((Throwable) msg));
		} else {
			builder.append(msg);
		}
		return builder.toString();
	}

}
