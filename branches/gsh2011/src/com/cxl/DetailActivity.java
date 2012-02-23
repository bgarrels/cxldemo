package com.cxl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
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

import com.cxl.gsh2011.R;
import com.waps.AdView;
import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

public class DetailActivity extends Activity implements UpdatePointsNotifier {

	private WebView webView;
	public static final String GBK = "GBK";
	public static final String UTF8 = "UTF8";

	Button btnPrevious;
	Button btnNext;
	Button menuButton;

	public static final int Requre_Point_Page_Index = 41;//需要积分才能查看的页面
	public static int Current_Page_Index = 1;
	public static final int Start_Page_Index = 1;//起始页索引
	public static final int Max_Page_Index = MainActivity.MENU_List.size() + Start_Page_Index - 1;//最大页索引
	private int scrollY = 0;

	public static boolean hasEnoughRequrePointPreferenceValue = false;// 保存在配置里
	public static final int requirePoint = 80;// 要求积分
	public static int currentPointTotal = 0;// 当前积分

	class MyPictureListener implements PictureListener {
		public void onNewPicture(WebView view, Picture arg1) {
			// put code here that needs to run when the page has finished
			// loading and
			// a new "picture" is on the webview.
			webView.scrollTo(0, scrollY);
		}
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);

		initRequrePointPreference();

		btnPrevious = (Button) findViewById(R.id.previous);
		btnPrevious.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String fileContent = getFileContent(DetailActivity.this, --Current_Page_Index);
				webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF8", "");
				scrollY = 0;
				setButtonVisibleAndSaveState();
			}
		});
		btnNext = (Button) findViewById(R.id.next);
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String fileContent = getFileContent(DetailActivity.this, ++Current_Page_Index);
				webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF8", "");
				scrollY = 0;
				setButtonVisibleAndSaveState();
			}
		});

		Bundle bundle = getIntent().getExtras();
		webView = (WebView) findViewById(R.id.webView);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.getSettings().setDefaultFixedFontSize(18);
		webView.getSettings().setDefaultFontSize(18);

		webView.setPictureListener(new MyPictureListener());

		boolean startByMenu = bundle.getBoolean("startByMenu");
		if (startByMenu) {
			int selectMenu = Integer.valueOf(bundle.getString("menu"));
			Current_Page_Index = selectMenu;
			String fileContent = getFileContent(DetailActivity.this, Current_Page_Index);
			webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF8", "");
			scrollY = 0;

		} else {
			Current_Page_Index = PreferenceUtil.getTxtIndex(this);
			String fileContent = getFileContent(DetailActivity.this, Current_Page_Index);
			webView.loadDataWithBaseURL(null, fileContent, "text/html", "UTF8", "");

			scrollY = PreferenceUtil.getScrollY(DetailActivity.this);
		}
		setButtonVisibleAndSaveState();

		Button offers = (Button) findViewById(R.id.OffersButton);
		offers.setText("更多下载");
		offers.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				// 显示推荐安装程序（Offer）.
				AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
			}
		});
		Button offers2 = (Button) findViewById(R.id.OffersButton2);
		offers2.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				// 显示推荐安装程序（Offer）.
				AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
			}
		});

		LinearLayout container = (LinearLayout) findViewById(R.id.AdLinearLayout);
		new AdView(this, container).DisplayAd(20);// 每20秒轮换一次广告；最少为20


	}

	protected void onDestroy() {
		webView.destroyDrawingCache();
		webView.destroy();
		super.onDestroy();
	}

	protected void onPause() {
		saveState();
		super.onPause();
	}

	// 保存当前页和滚动位置
	private void saveState() {
		PreferenceUtil.setScrollY(this, webView.getScrollY());
		PreferenceUtil.setTxtIndex(this, Current_Page_Index);
	}

	private void setButtonVisibleAndSaveState() {
		saveState();
		String currentTitle = MainActivity.MENU_List.get(Current_Page_Index - Start_Page_Index).getValue();
		setTitle(currentTitle);
		if (Current_Page_Index == Start_Page_Index) {
			btnPrevious.setVisibility(View.INVISIBLE);
		} else {
			btnPrevious.setVisibility(View.VISIBLE);
		}
		if (Current_Page_Index == Max_Page_Index) {
			btnNext.setVisibility(View.INVISIBLE);
		} else {
			btnNext.setVisibility(View.VISIBLE);
		}
	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		SubMenu menu = paramMenu.addSubMenu(0, 0, 0, "目录");
		menu.setIcon(R.drawable.menu);
		SubMenu menu2 = paramMenu.addSubMenu(0, 1, 0, "更多免费精品下载...");
		menu2.setIcon(R.drawable.more);
		return super.onCreateOptionsMenu(paramMenu);
	}

	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
		if (paramMenuItem.getItemId() == 0) {
			Intent intent = new Intent();
			intent.setClass(DetailActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		} else if (paramMenuItem.getItemId() == 1) {
			// 显示推荐安装程序（Offer）.
			AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
		}
		return super.onOptionsItemSelected(paramMenuItem);
	}

	private void initRequrePointPreference() {
		hasEnoughRequrePointPreferenceValue = PreferenceUtil.getHasEnoughRequrePoint(DetailActivity.this);
	}

	protected void onResume() {
		if (!hasEnoughRequrePointPreferenceValue) {
			AppConnect.getInstance(this).getPoints(this);
		}
		super.onResume();
	}
	Handler msgHandler = new Handler();
	/**
	 * AppConnect.getPoints()方法的实现，必须实现
	 * 
	 * @param currencyName
	 *            虚拟货币名称.
	 * @param pointTotal
	 *            虚拟货币余额.
	 */
	public void getUpdatePoints(String currencyName, int pointTotal) {
		currentPointTotal = pointTotal;
		if (pointTotal >= requirePoint) {
			hasEnoughRequrePointPreferenceValue = true;
			PreferenceUtil.setHasEnoughRequrePoint(DetailActivity.this, true);
		}
//		if (!hasEnoughRequrePointPreferenceValue) {

			msgHandler.post(new Runnable() {
				public void run() {
					new AlertDialog.Builder(DetailActivity.this)
							.setTitle("感谢使用本程序")
							.setMessage(
									"说明：\n\n可通过【更多下载】，下载更多精彩内容。\n\n通过【更多应用】，可以下载各种好玩应用"
											)
							.setPositiveButton("更多下载", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i) {
									AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
								}
							}).setNeutralButton("更多应用", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i) {
									AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
								}
							}).setNegativeButton("继续", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialoginterface, int i) {
								}
							}).show();
				}
			});
//		}
	}

	/**
	 * AppConnect.getPoints() 方法的实现，必须实现
	 * 
	 * @param error
	 *            请求失败的错误信息
	 */

	public void getUpdatePointsFailed(String error) {
		hasEnoughRequrePointPreferenceValue = false;
	}

	public String getFileContent(Context context, int fileName) {// 规划了file参数、ID参数，方便多文件写入。
		InputStream in = null;
		BufferedReader bufferedReader = null;
		StringBuilder sBuffer = new StringBuilder("");
		try {
			AssetManager assets = getAssets();
			in = assets.open(String.valueOf(fileName));

			bufferedReader = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = bufferedReader.readLine()) != null) {
				sBuffer.append(strLine + "\n");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return sBuffer.toString();
	}
}
