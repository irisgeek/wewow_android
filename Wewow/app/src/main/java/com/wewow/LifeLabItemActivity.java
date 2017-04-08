package com.wewow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wewow.dto.LabCollection;
import com.wewow.utils.Utils;

import org.w3c.dom.Text;

/**
 * Created by suncjs on 2017/4/8.
 */

public class LifeLabItemActivity extends Activity {

    public static final String LIFELAB_COLLECTION = "LIFELAB_COLLECTION";
    private LabCollection lc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        Parcelable p = intent.getParcelableExtra(LIFELAB_COLLECTION);
        this.lc = (LabCollection) p;
        Utils.setActivityToBeFullscreen(this);
        setContentView(R.layout.activity_lifelab_item);
        this.setupUI();
    }

    private void setupUI() {
        //this.findViewById(R.id.lifelab_item_root).setBackground();
        TextView title = (TextView) this.findViewById(R.id.lifelab_item_title);
        title.setText(this.lc.title);
        ImageView ivback = (ImageView) this.findViewById(R.id.lifelab_item_back);
        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LifeLabItemActivity.this.finish();
            }
        });
    }
}
