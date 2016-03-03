# MinusLinkify
More flexible way to set span-text links and callback when onClick.

## Xml
``` xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context="com.example.testapk.MainActivity" >

    <minus.android.view.LinkTextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="www.google.com"
        android:autoLink="all" />

</RelativeLayout>
```

## Code
``` java
LinkTextView v = new LinkTextView(this);
v.setAutoLinkMaskCompat(Linkify.ALL);
v.setText("www.google.com");
```

## Listen onLinkClick
``` java
v.setOnLinkClickListener(new IOnLinkClickListener() {

	@Override
	public void onLinkClick(int type, String value) {
		Toast.makeText(MainActivity.this, value, Toast.LENGTH_SHORT).show();
	}
});
```

## Decide whether onInterruptSpanClick or not
``` java
v.setOnInterruptSpanClickFilter(new IOnInterruptSpanClickFilter() {

	@Override
	public boolean onInterruptSpanClick(String scheme) {
		return false;
	}
});
```

## Adavanced Usage
- **IntentSpan** &lt;a intent={-a android.intent.action.MAIN -c android.intent.category.HOME}&gt;*click and launch home*&lt;/a&gt;
> format: &lt;a intent={**ADB-SHELL-AM-COMMAND**}&gt;**LINK_TEXT**&lt;/a&gt;
> - **ADB-SHELL-AM-COMMAND** run *adb shell*, and using system *am start* to use, test and config link finally
> - **LINK_TEXT** visible to user, and response when on touch or click

- **LinkGestureDetector** helper to implement YOUR LinkTextView
