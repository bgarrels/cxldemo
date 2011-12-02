package com.cxl;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cxl.car.R;
import com.waps.AdView;
import com.waps.AppConnect;

public class DetailActivity extends Activity {

	private Button returnButton;
	private ImageView imageView;
	private TextView textView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);

		Bundle bundle = getIntent().getExtras();
		String car = bundle.getString("car");
		imageView = (ImageView) findViewById(R.id.ImageView);
		AssetManager assets = getAssets();
		
		try {
			// 打开指定资源对应的输入流  
			InputStream assetFile = assets.open("image/"+ListManager.CarImageMap.get(car)+".jpg");
			// 改变ImageView显示的图片  
			imageView.setImageBitmap(BitmapFactory.decodeStream(assetFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		textView = (TextView) findViewById(R.id.TextView);
		textView.setText(ListManager.carMap.get(car));

		if (!MainActivity.hasEnoughRequrePoint) {// 没达到积分
													//			showDialog();
		}

		returnButton = (Button) findViewById(R.id.returnButton);

		returnButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				finish();
			}
		});
		Button owns = (Button) findViewById(R.id.OwnsButton);
		owns.setText("更多精品下载...");
		owns.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				// 显示推荐安装程序（Offer）.
				AppConnect.getInstance(DetailActivity.this).showOffers(DetailActivity.this);
			}
		});
		LinearLayout container2 = (LinearLayout) findViewById(R.id.AdLinearLayout);
		new AdView(this, container2).DisplayAd(20);// 每20秒轮换一次广告；最少为20
	}

}
