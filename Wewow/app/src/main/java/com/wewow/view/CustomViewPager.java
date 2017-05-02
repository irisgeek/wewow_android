package com.wewow.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by iris on 17/4/25.
 */
public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
             super(context);
       }

         public CustomViewPager(Context context, AttributeSet attrs) {
               super(context, attrs);
           }
    /**
     11      * 决定我们是否想要拦截这个手势，如果返回true,
     12      * onMotionEvent就会接受到事件，并且在其中发生滑动的操作.
     13      * 所以这段代码的原理是：当ViewPager和SwipeRefreshLayout滑动冲突的时候直接返回true,
     14      * 使得ViewPager可以正常滑动(至于怎么产生冲突的我也不知道)
     15      * @param ev
     16      * @return
     17      */
               @Override
       public boolean onInterceptTouchEvent(MotionEvent ev) {
             try {
                     return super.onInterceptTouchEvent(ev);
                } catch (Exception e) {
                     return true;
             }
            }
}
