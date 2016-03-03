package minus.android.view;

public interface IOnLinkClickListener {
	
	public static final int LINK_TYPE_PHONE = 1;
	public static final int LINK_TYPE_EMAIL = 2;
	public static final int LINK_TYPE_WEB = 3;
	public static final int LINK_TYPE_INTENT = 4;
	public static final int LINK_TYPE_OTHER = 5;
	
    public void onLinkClick(int type, String value);

}
