package vn.xdeuhug.seniorsociable.watch.model.interfaces;

/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 * 用于ViewPagerLayoutManager的监听
 */

public interface OnViewPagerListener {

    /*loading finished*/
    void onInitComplete();

    /*Released monitoring*/
    void onPageRelease(boolean isNext, int position);

    /*Selected monitoring and determining whether to slide to the bottom*/
    void onPageSelected(int position, boolean isBottom);


}
