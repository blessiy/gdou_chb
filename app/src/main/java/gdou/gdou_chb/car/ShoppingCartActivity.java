package gdou.gdou_chb.car;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;

import com.kymjs.rxvolley.rx.Result;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import gdou.gdou_chb.R;
import gdou.gdou_chb.activity.HomeActivity;
import gdou.gdou_chb.activity.PayActivity;
import gdou.gdou_chb.model.bean.Goods;
import gdou.gdou_chb.model.bean.GoodsVo;
import gdou.gdou_chb.model.bean.Orders;
import gdou.gdou_chb.model.bean.ResultBean;
import gdou.gdou_chb.model.impl.BaseModelImpl;
import gdou.gdou_chb.model.impl.GoodModelImpl;
import gdou.gdou_chb.util.GsonUtils;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ShoppingCartActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgCart;
    private ViewGroup anim_mask_layout;
    private RecyclerView rvType, rvSelected;
    private TextView tvCount, tvCost, tvSubmit, tvTips;
    private BottomSheetLayout bottomSheetLayout;
    private View bottomSheet;
    private StickyListHeadersListView listView;
    private CompositeSubscription mSubscription;

    private ArrayList<GoodsItem> dataList, typeList;
    private SparseArray<GoodsItem> selectedList;
    private SparseIntArray groupSelect;

    private GoodModelImpl mGoodModel;
    private Long mShopId = 0L;          //默认为第一个用户

    private GoodsAdapter myAdapter;
    private SelectAdapter selectAdapter;
    private TypeAdapter typeAdapter;

    private NumberFormat nf;
    private Handler mHanlder;
    private long mBusinessId;
    private double cost;    //总价
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(2);
        mHanlder = new Handler(getMainLooper());

        mSubscription = new CompositeSubscription();
        selectedList = new SparseArray<>();//已选中的
        groupSelect = new SparseIntArray();

        dataList = new ArrayList<>();
//        dataList.add(new GoodsItem(1,20.0,"meiyou",1,"meidd"));
        typeList = new ArrayList<>();

        initData();
    }

    private void initData() {
        mShopId = getIntent().getLongExtra("shopId", 0L);
        mBusinessId = getIntent().getLongExtra("businessId", 0L);
        Log.d("ShopId", "initData: " + mShopId);
        Log.d("mBusinessId", "initData: " + mBusinessId);

        mGoodModel = new GoodModelImpl();
        Subscription subscription = mGoodModel.findByGoodsId(mShopId)
                .map(new Func1<Result, String>() {

                    @Override
                    public String call(Result result) {
                        return new String(result.data);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        initView();
                        myAdapter.notifyDataSetChanged();
                        //TODO : 获取真的数据
//                        dataList = GoodsItem.getGoodsList();//商品列表
//                        typeList = GoodsItem.getTypeList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Shoppingcar", "onError: 错误输出", e);
                    }

                    @Override
                    public void onNext(String string) {
                        ResultBean resultBean = GsonUtils.getResultBeanByJson(string);
                        List<Goods> goodsList = GsonUtils.getBeanFromResultBeanListMiss(resultBean, "goodsList", Goods[].class);
                        //转换
                        for (Goods goods : goodsList) {

                            dataList.add(new GoodsItem((int) goods.getId(), goods.getPrice(),goods.getName(), 0, null,goods.getGoodImg()));
                        }
                        Log.d("Goods->List", GsonUtils.getJsonStr(goodsList));
                    }
                });
        mSubscription.add(subscription);
    }

    private void initView() {
        tvCount = (TextView) findViewById(R.id.tvCount);
        tvCost = (TextView) findViewById(R.id.tvCost);
        tvTips = (TextView) findViewById(R.id.tvTips);
        tvSubmit = (TextView) findViewById(R.id.tvSubmit);
        rvType = (RecyclerView) findViewById(R.id.typeRecyclerView);

        imgCart = (ImageView) findViewById(R.id.imgCart);
        anim_mask_layout = (RelativeLayout) findViewById(R.id.containerLayout);
        bottomSheetLayout = (BottomSheetLayout) findViewById(R.id.bottomSheetLayout);


        listView = (StickyListHeadersListView) findViewById(R.id.itemListView);

        rvType.setLayoutManager(new LinearLayoutManager(this));
        typeAdapter = new TypeAdapter(this, typeList);
        rvType.setAdapter(typeAdapter);
        rvType.addItemDecoration(new DividerDecoration(this));

        myAdapter = new GoodsAdapter(dataList, this);
        listView.setAdapter(myAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //TODO有问题
//                GoodsItem item = dataList.get(firstVisibleItem);
//                if (typeAdapter.selectTypeId != item.typeId) {
//                    typeAdapter.selectTypeId = item.typeId;
//                    typeAdapter.notifyDataSetChanged();
//                    rvType.smoothScrollToPosition(getSelectedGroupPosition(item.typeId));
//                }
            }
        });

    }

    //动画方法
    public void playAnimation(int[] start_location) {
        ImageView img = new ImageView(this);
        img.setImageResource(R.drawable.button_add);
        setAnim(img, start_location);
    }

    private Animation createAnim(int startX, int startY) {
        int[] des = new int[2];
        imgCart.getLocationInWindow(des);

        AnimationSet set = new AnimationSet(false);

        Animation translationX = new TranslateAnimation(0, des[0] - startX, 0, 0);
        translationX.setInterpolator(new LinearInterpolator());
        Animation translationY = new TranslateAnimation(0, 0, 0, des[1] - startY);
        translationY.setInterpolator(new AccelerateInterpolator());
        Animation alpha = new AlphaAnimation(1, 0.5f);
        set.addAnimation(translationX);
        set.addAnimation(translationY);
        set.addAnimation(alpha);
        set.setDuration(500);

        return set;
    }

    private void addViewToAnimLayout(final ViewGroup vg, final View view,
                                     int[] location) {
        int x = location[0];
        int y = location[1];
        int[] loc = new int[2];
        vg.getLocationInWindow(loc);
        view.setX(x);
        view.setY(y - loc[1]);
        vg.addView(view);
    }

    private void setAnim(final View v, int[] start_location) {

        addViewToAnimLayout(anim_mask_layout, v, start_location);
        Animation set = createAnim(start_location[0], start_location[1]);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                mHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anim_mask_layout.removeView(v);
                    }
                }, 100);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(set);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom:
                showBottomSheet();
                break;
            case R.id.clear:
                clearCart();
                break;
            case R.id.tvSubmit:
                Toast.makeText(ShoppingCartActivity.this, "结算", Toast.LENGTH_SHORT).show();
                sumbitcar();
                break;
            default:
                break;
        }
    }

    private void sumbitcar() {

        List<GoodsVo> goodsVoList = new ArrayList<>();
        for (int i = 0; i < selectedList.size(); i++) {
            int j = selectedList.keyAt(i);
            int id = selectedList.get(j).id;
            int count = selectedList.get(j).count;
            goodsVoList.add(new GoodsVo(id, count));
        }
        Orders order = new Orders();
        if (null != BaseModelImpl.user) {
            //TODO用户没有登陆
            //Toast.makeText(ShoppingCartActivity.class, "请去登陆", Toast.LENGTH_LONG).show();
            order.setUserId(BaseModelImpl.user.getId());
            order.setShopUserId(mBusinessId);
            toPayView(order, goodsVoList);
        }

    }

    /**
     * 跳转到支付页面
     * @param order
     * @param goodsVoList
     */
    private void toPayView(Orders order, List<GoodsVo> goodsVoList) {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("goodsList", GsonUtils.getJsonStr(goodsVoList));
        intent.putExtra("orders", GsonUtils.getJsonStr(order));
        intent.putExtra("conut", count);
        intent.putExtra("cost", cost);
        startActivity(intent);
    }

    private void toHomePage() {
        startActivity(new Intent(this, HomeActivity.class));
        this.finish();
    }

    //添加商品
    public void add(GoodsItem item, boolean refreshGoodList) {

        int groupCount = groupSelect.get(item.typeId);
        if (groupCount == 0) {
            groupSelect.append(item.typeId, 1);
        } else {
            groupSelect.append(item.typeId, ++groupCount);
        }

        GoodsItem temp = selectedList.get(item.id);
        if (temp == null) {
            item.count = 1;
            selectedList.append(item.id, item);
        } else {
            temp.count++;
        }
        update(refreshGoodList);
    }

    //移除商品
    public void remove(GoodsItem item, boolean refreshGoodList) {

        int groupCount = groupSelect.get(item.typeId);
        if (groupCount == 1) {
            groupSelect.delete(item.typeId);
        } else if (groupCount > 1) {
            groupSelect.append(item.typeId, --groupCount);
        }

        GoodsItem temp = selectedList.get(item.id);
        if (temp != null) {
            if (temp.count < 2) {
                selectedList.remove(item.id);
            } else {
                item.count--;
            }
        }
        update(refreshGoodList);
    }

    //刷新布局 总价、购买数量等
    private void update(boolean refreshGoodList) {
        int size = selectedList.size();
        count = 0;
        cost = 0;
        for (int i = 0; i < size; i++) {
            GoodsItem item = selectedList.valueAt(i);
            count += item.count;
            cost += item.count * item.price;
        }

        if (count < 1) {
            tvCount.setVisibility(View.GONE);
        } else {
            tvCount.setVisibility(View.VISIBLE);
        }

        tvCount.setText(String.valueOf(count));

        if (cost > 22) {
            tvTips.setVisibility(View.GONE);
            tvSubmit.setVisibility(View.VISIBLE);
        } else {
            tvSubmit.setVisibility(View.GONE);
            tvTips.setVisibility(View.VISIBLE);
        }

        tvCost.setText(nf.format(cost));

        if (myAdapter != null && refreshGoodList) {
            myAdapter.notifyDataSetChanged();
        }
        if (selectAdapter != null) {
            selectAdapter.notifyDataSetChanged();
        }
        if (typeAdapter != null) {
            typeAdapter.notifyDataSetChanged();
        }
        if (bottomSheetLayout.isSheetShowing() && selectedList.size() < 1) {
            bottomSheetLayout.dismissSheet();
        }
    }

    //清空购物车
    public void clearCart() {
        selectedList.clear();
        groupSelect.clear();
        update(true);
    }

    //根据商品id获取当前商品的采购数量
    public int getSelectedItemCountById(int id) {
        GoodsItem temp = selectedList.get(id);
        if (temp == null) {
            return 0;
        }
        return temp.count;
    }

    //根据类别Id获取属于当前类别的数量
    public int getSelectedGroupCountByTypeId(int typeId) {
        return groupSelect.get(typeId);
    }

    //根据类别id获取分类的Position 用于滚动左侧的类别列表
    public int getSelectedGroupPosition(int typeId) {
        for (int i = 0; i < typeList.size(); i++) {
            if (typeId == typeList.get(i).typeId) {
                return i;
            }
        }
        return 0;
    }

    public void onTypeClicked(int typeId) {
        listView.setSelection(getSelectedPosition(typeId));
    }

    private int getSelectedPosition(int typeId) {
        int position = 0;
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).typeId == typeId) {
                position = i;
                break;
            }
        }
        return position;
    }

    private View createBottomSheetView() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_bottom_sheet, (ViewGroup) getWindow().getDecorView(), false);
        rvSelected = (RecyclerView) view.findViewById(R.id.selectRecyclerView);
        rvSelected.setLayoutManager(new LinearLayoutManager(this));
        TextView clear = (TextView) view.findViewById(R.id.clear);
        clear.setOnClickListener(this);
        selectAdapter = new SelectAdapter(this, selectedList);
        rvSelected.setAdapter(selectAdapter);
        return view;
    }

    private void showBottomSheet() {
        if (bottomSheet == null) {
            bottomSheet = createBottomSheetView();
        }
        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
        } else {
            if (selectedList.size() != 0) {
                bottomSheetLayout.showWithSheetView(bottomSheet);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscription.clear();
    }
}
