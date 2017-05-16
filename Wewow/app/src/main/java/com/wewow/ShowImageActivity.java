package com.wewow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.boycy815.pinchimageview.PinchImageView;
import com.wewow.utils.RemoteImageLoader;
import com.wewow.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ShowImageActivity extends ActionBarActivity {

    private static final String TAG = "ShowImageActivity";
    public static final String IMAGE_INDEX = "IMAGE_INDEX";
    public static final String IMAGE_LIST = "IMAGE_LIST";
    private ArrayList<String> pictures;
    private int index;

    private TextView imageOrder;
    private TextView imageCount;
    private PinchImageView imgView;
    private ViewPager pager;
    private int screenWidth;
    private int pictureHeight;
    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_show_image);
        this.screenWidth = Utils.dipToPixel(this, Utils.getScreenWidthDp(this));
        Intent intent = this.getIntent();
        this.index = intent.getIntExtra(IMAGE_INDEX, 0);
        this.pictures = intent.getStringArrayListExtra(IMAGE_LIST);
        this.imageOrder = (TextView) this.findViewById(R.id.image_order);
        this.imageOrder.setText(String.valueOf(this.index + 1));
        this.imageCount = (TextView) this.findViewById(R.id.image_count);
        this.imageCount.setText(String.valueOf(this.pictures.size()));
        this.pager = (ViewPager) this.findViewById(R.id.image_pager);
        this.pictureHeight = this.pager.getLayoutParams().height;
        this.pager.setAdapter(new FragmentStatePagerAdapter(this.getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                String url = ShowImageActivity.this.pictures.get(position);
                ShowImageFragment sf = new ShowImageFragment();
                Bundle b = new Bundle();
                b.putString(ShowImageFragment.IMAGE_URL, url);
                b.putInt(ShowImageFragment.IMAGE_INDEX, position);
                b.putFloat(ShowImageFragment.IMAGE_AREA_HEIGHT, pictureHeight);
                b.putFloat(ShowImageFragment.IMAGE_AREA_WIDTH, screenWidth);
                sf.setArguments(b);
                return sf;
            }

            @Override
            public int getCount() {
                return ShowImageActivity.this.pictures.size();
            }
        });
        this.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //
            }

            @Override
            public void onPageSelected(int position) {
                ShowImageActivity.this.imageOrder.setText(String.valueOf(position + 1));
                index = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//
            }
        });
        this.pager.setCurrentItem(this.index);

        imgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Wewow";
        new File(imgPath).mkdirs();

        findViewById(R.id.iv_download_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = pictures.get(index);
                if(!TextUtils.isEmpty(url)){
                    new RemoteImageLoader(ShowImageActivity.this, url, new RemoteImageLoader.RemoteImageListener() {
                        @Override
                        public void onRemoteImageAcquired(Drawable dr) {
                            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                            File file = new File(imgPath + "/" + System.currentTimeMillis() + ".jpg");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.flush();
                                showToast("保存成功");
                                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                intent.setData(Uri.fromFile(file));
                                sendBroadcast(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                showToast("保存失败");
                            } finally {
                                try {
                                    bitmap.recycle();
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }else{
                    showToast("保存失败");
                }
            }
        });
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showToast(String str){
        Toast t = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        t.show();
    }
}
