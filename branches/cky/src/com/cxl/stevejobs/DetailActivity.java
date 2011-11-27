package com.cxl.stevejobs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxl.seeanybody.R;
import com.waps.AdView;

public class DetailActivity extends Activity {

	private Button returnButton;
	private WebView textView;
	private String menu;
	public static final String GBK = "GBK";
	public static final String UTF8 = "UTF8";

	public static int Current_Page_Value = 0;
	TextView page;
	Button btnPrevious;
	Button btnNext;
	public static final int Page_Sum = MainActivity.MENU_List.size() - 1;// 由0开始，减去1，
	private int scrollY = 0;

	Handler mHandler = new Handler();
	private Runnable scrollViewRun = new Runnable() {
		public void run() {
			textView.scrollTo(0, 0);
		}
	};
	private TextView currentTextView;

	class MyPictureListener implements PictureListener {
		public void onNewPicture(WebView view, Picture arg1) {
			// put code here that needs to run when the page has finished
			// loading and
			// a new "picture" is on the webview.
			textView.scrollTo(0, scrollY);
		}
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		btnPrevious = (Button) findViewById(R.id.previous);
		btnPrevious.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				textView.loadUrl("file:///android_asset/chapter"
						+ (--Current_Page_Value) + ".html");

				textView.loadUrl("javascript:function(){alert(2);}");
				textView.post(scrollViewRun);
				setButtonVisibleAndSaveState();
			}
		});
		btnNext = (Button) findViewById(R.id.next);
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				textView.loadUrl("file:///android_asset/chapter"
						+ (++Current_Page_Value) + ".html");
				textView.post(scrollViewRun);
				setButtonVisibleAndSaveState();
			}
		});

		Bundle bundle = getIntent().getExtras();
		textView = (WebView) findViewById(R.id.textView);
		textView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

		textView.setPictureListener(new MyPictureListener());

		boolean startByMenu = bundle.getBoolean("startByMenu");
		if (startByMenu) {
			menu = bundle.getString("menu");
			Current_Page_Value = Integer.valueOf(menu);
			textView.loadUrl("file:///android_asset/chapter"
					+ Current_Page_Value + ".html");
		} else {
			Current_Page_Value = Util.getTxtIndex(this);
			if (Current_Page_Value > Page_Sum) {
				Current_Page_Value = Page_Sum;
			}
			textView.loadUrl("file:///android_asset/chapter"
					+ Current_Page_Value + ".html");

			scrollY = Util.getScrollY(DetailActivity.this);
		}
		currentTextView = (TextView)findViewById(R.id.currentTextView);
		setButtonVisibleAndSaveState();

		
		
//		returnButton = (Button) findViewById(R.id.returnButton);
//		returnButton.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View arg0) {
//				setButtonVisibleAndSaveState();
//				// finish();
//				Intent intent = new Intent();
//				intent.setClass(DetailActivity.this, MainActivity.class);
//				startActivity(intent);
//				finish();
//			}
//		});

		LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
		new AdView(this, container).DisplayAd(20);// 每20秒轮换一次广告；最少为20

	}

	protected void onPause() {
		saveState();
		super.onPause();
	}

	// 保存当前页和滚动位置
	private void saveState() {
		Util.setScrollY(this, textView.getScrollY());
		Util.setTxtIndex(this, Current_Page_Value);
	}

	private void setButtonVisibleAndSaveState() {
		saveState();
		String currentTitle = MainActivity.MENU_List.get(Current_Page_Value).getValue();
		setTitle(currentTitle);
		currentTextView.setText(currentTitle);
		if (Current_Page_Value == 0) {
			btnPrevious.setVisibility(View.INVISIBLE);
		} else {
			btnPrevious.setVisibility(View.VISIBLE);
		}
		if (Current_Page_Value == Page_Sum) {
			btnNext.setVisibility(View.INVISIBLE);
		} else {
			btnNext.setVisibility(View.VISIBLE);
		}
	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		SubMenu menu = paramMenu.addSubMenu(0, 0, 0, "目录");
		return super.onCreateOptionsMenu(paramMenu);
	}

	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
		if (paramMenuItem.getItemId() == 0) {
			Intent intent = new Intent();
			intent.setClass(DetailActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		return super.onOptionsItemSelected(paramMenuItem);
	}

}
