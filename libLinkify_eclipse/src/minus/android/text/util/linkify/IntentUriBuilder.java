package minus.android.text.util.linkify;

import android.content.Intent;

public class IntentUriBuilder extends CmdParser {

	public static void main(String[] args) {
		if(0 == args.length) {
			showUsage(true);
		}
		
		try {
			System.out.println("IntentUriBuilder start...");
			Intent intent = new IntentUriBuilder().makeIntent(args);
			System.out.println("intent uri built:\n" + intent.toUri(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
