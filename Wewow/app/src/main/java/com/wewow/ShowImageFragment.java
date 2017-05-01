package com.wewow;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.boycy815.pinchimageview.PinchImageView;
import com.wewow.utils.RemoteImageLoader;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowImageFragment extends Fragment {

    public static final String IMAGE_URL = "IMAGE_URL";
    public static final String IMAGE_INDEX = "IMAGE_INDEX";
    public static final String IMAGE_AREA_WIDTH = "IMAGE_AREA_WIDTH";
    public static final String IMAGE_AREA_HEIGHT = "IMAGE_AREA_HEIGHT";

    private String url;
    private int index;
    private float width;
    private float height;

    public ShowImageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle b = this.getArguments();
        this.url = b.getString(IMAGE_URL);
        this.index = b.getInt(IMAGE_INDEX);
        this.width = b.getFloat(IMAGE_AREA_WIDTH);
        this.height = b.getFloat(IMAGE_AREA_HEIGHT);
        final View view = inflater.inflate(R.layout.fragment_show_image, container, false);
        new RemoteImageLoader(this.getActivity(), this.url, new RemoteImageLoader.RemoteImageListener() {
            @Override
            public void onRemoteImageAcquired(Drawable dr) {
                PinchImageView iv = (PinchImageView) view.findViewById(R.id.image_item);
                iv.setImageDrawable(dr);
                BitmapDrawable bdr = (BitmapDrawable) dr;
                float w = bdr.getBitmap().getWidth();
                float h = bdr.getBitmap().getHeight();
                ViewGroup.LayoutParams lp = iv.getLayoutParams();
                if (w > h) {
                    lp.width = Math.round(ShowImageFragment.this.width);
                    lp.height = Math.round(h / w * ShowImageFragment.this.width);
                } else {
                    lp.height = Math.round(ShowImageFragment.this.height);
                    lp.width = Math.round(w / h * ShowImageFragment.this.height);
                }
                iv.setLayoutParams(lp);
            }
        });
        return view;
    }

}
