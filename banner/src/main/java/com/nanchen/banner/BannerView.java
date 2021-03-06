package com.nanchen.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2017-08-30  9:36
 */

public class BannerView extends FrameLayout {
    private long mAutoScrollDelay = 3000;
    private Timer mAutoScrollTimer;
    private ViewPager mVpBanner;
    private BannerLine mLine;
    private List<BannerEntity> mEntities = new ArrayList<>();
    private BannerPagerAdapter mAdapter;
    private Handler mMainHandler;
    private int currentIdx = 0;
    private Timer mTimer = null;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            startAutoScroll();
        } else if (action == MotionEvent.ACTION_DOWN) {
            stopAutoScroll();
        }
        return super.dispatchTouchEvent(ev);
    }

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.view_banner, this);
        mVpBanner = (ViewPager) this.findViewById(R.id.vp_banner);
        mLine = (BannerLine) this.findViewById(R.id.line);
        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.BannerView);
        int lineColor = typeArray.getColor(R.styleable.BannerView_banner_line_color,
                ContextCompat.getColor(getContext(), R.color.banner_blue));
        typeArray.recycle();
        mLine.setLineColor(lineColor);
        mMainHandler = new Handler(context.getMainLooper());
    }

    public void setEntities(List<BannerEntity> entities) {
        addExtraPage(entities);
        showBanner();
    }

    private void addExtraPage(List<BannerEntity> entities) {
        mEntities.clear();
        mEntities.add(entities.get(entities.size() - 1));
        mEntities.addAll(entities);
        mEntities.add(entities.get(0));
    }

    private void showBanner() {
        mLine.setPageWidth(mEntities.size());
        mAdapter = new BannerPagerAdapter(getContext(), mEntities);
        mVpBanner.setAdapter(mAdapter);
        mVpBanner.setCurrentItem(1, true);
        currentIdx = 1;
        mVpBanner.clearOnPageChangeListeners();
        mVpBanner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mLine.setPageScrolled(position, positionOffset);
                currentIdx = position;
                if (positionOffsetPixels == 0.0) {
                    setViewPagerItemPosition(position);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * set auto scroll time
     */
    public void setAutoScroll(long time) {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        this.mTimer = new Timer("Banner Scroller Timer");
        TimerTask scrollerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(new Message());
            }
        };
        this.mTimer.schedule(scrollerTask, time, time);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (currentIdx >= mEntities.size()) {
                currentIdx = 0;
            }
            mVpBanner.setCurrentItem(currentIdx, true);
            currentIdx++;
        }
    };

    private void setViewPagerItemPosition(int position) {
        if (position == mEntities.size() - 1) {
            mVpBanner.setCurrentItem(1, false);
        } else if (position == 0) {
            mVpBanner.setCurrentItem(mEntities.size() - 2, false);
        } else {
            mVpBanner.setCurrentItem(position);
        }
    }

    private void nextScroll() {
        int position = mVpBanner.getCurrentItem();
        mLine.setPageScrolled(position + 1, 0);
        setViewPagerItemPosition(position + 1);
    }

    public void startAutoScroll() {
        mAutoScrollTimer = new Timer();
        mAutoScrollTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        nextScroll();
                    }
                });
            }
        }, mAutoScrollDelay, mAutoScrollDelay);
    }

    public void stopAutoScroll() {
        mAutoScrollTimer.cancel();
    }

    public void setAutoScrollDelay(long delay) {
        this.mAutoScrollDelay = delay;
    }

    public void setOnBannerClickListener(OnBannerClickListener clickListener) {
        if (mAdapter != null) {
            mAdapter.setOnBannerClickListener(clickListener);
        }
    }

}

