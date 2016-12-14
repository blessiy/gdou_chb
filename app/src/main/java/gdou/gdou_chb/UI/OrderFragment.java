package gdou.gdou_chb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import gdou.gdou_chb.R;
import gdou.gdou_chb.activity.OrderdetailActivity;
import gdou.gdou_chb.adapter.OrderAdapter;
import gdou.gdou_chb.contract.HomeContract;
import gdou.gdou_chb.model.bean.Orders;
import gdou.gdou_chb.util.Java.BaseActivity;
import gdou.gdou_chb.util.MVP.BaseFragment;
import gdou.gdou_chb.util.SnackbarUtil;

/**
 * Created by Z1shiki on 2016/11/16.
 */

public class OrderFragment extends BaseFragment implements HomeContract.OrderView {

//    @BindView(R.id.toolbar)
//    Toolbar mToolBar;
//    @BindView(R.id.toolbar_text)
//    TextView mToolBarText;
@BindView(R.id.rvorder)
RecyclerView rvOrder;

//    private ArrayList<Orders> mDataList;
    private HomeContract.OrderPresenter mPresenter;
    private BaseActivity mActivity;
//    private List<Orders> mOrderList;
    private OrderAdapter adapter;
//    private OrderModelImpl orderModel;

    public OrderFragment() { //Requires empty public constructor
    }

    public static OrderFragment newInstanceState() {
        return new OrderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseActivity) this.getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity = (BaseActivity) this.getActivity();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity = (BaseActivity) this.getActivity();
        mPresenter.unsubscribe();

    }

    @Override
    public void setPresenter(@NonNull HomeContract.OrderPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void Datachanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void setAdapter(ArrayList<Orders> list) {
        adapter = new OrderAdapter(mActivity, list);
        //RecyclerView rvOrder = (RecyclerView) mActivity.findViewById(R.id.rvorder);
        //创建适配器
//        adapter = new OrderAdapter(mActivity, mOrderList);///

        //添加AdapterBtn监听
        adapter.setMyOnClickLinstener(btnOnClick());
        //绑定适配器
        rvOrder.setAdapter(adapter);
        //设置布局管理器
        rvOrder.setLayoutManager(new LinearLayoutManager(mActivity));
        //固定大小
        rvOrder.setHasFixedSize(true);
        //设置增加或删除条目的动画
        rvOrder.setItemAnimator(new DefaultItemAnimator());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.order_frag, container, false);
        setHasOptionsMenu(true);
//        initData();
        //setup
        ButterKnife.bind(this, root);

        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        //mToolBarText.setText("订单");
        ((BaseActivity)getActivity()).setSupportActionBar(mToolbar);

//        mOrderList = new ArrayList<Orders>();
//        orderModel = new OrderModelImpl();
        mPresenter.initData();


        //adapter.setOnItemClickListener(this);
        //adapter.setOnRecyclerViewListener(this);
        //adapter.notifyDataSetChanged();

        return root;
    }


    @Override
    public void showSnackbar(int i) {
        switch (i){
            case 0:
                SnackbarUtil.LongSnackbar(getActivity().findViewById(R.id.contentFrame)
                        , "加载失败",4).show();
                break;
            case 1:
                SnackbarUtil.LongSnackbar(getActivity().findViewById(R.id.contentFrame)
                        , "完成订单失败",4).show();
                break;
            case 2:
                SnackbarUtil.LongSnackbar(getActivity().findViewById(R.id.contentFrame)
                        , "取消订单失败",4).show();
                break;
        }
    }

    /**
     * 加载数据
     */



//    private void initData() {
//        //添加
//        mDataList.add(new Orders(getString(R.string.news_one_title),getString(R.string.a)));
//        mDataList.add(new Orders(getString(R.string.news_two_title),getString(R.string.b)));
//        mDataList.add(new Orders(getString(R.string.news_three_title),getString(R.string.c)));
//        mDataList.add(new Orders(getString(R.string.news_four_title),getString(R.string.d)));
//    }

    private OrderAdapter.MyOnClickLinstener btnOnClick() {
        OrderAdapter.MyOnClickLinstener onClick = new OrderAdapter.MyOnClickLinstener() {
            @Override
            public void btnClick(int viewId,Long orderId) {
                if (viewId == R.id.confirm_receipt_btn) {
                    //处理完成订单
                    mPresenter.doenOrder(orderId);
                } else if (viewId == R.id.charge_back_btn){
                    //处理退单
                    mPresenter.backOrder(orderId);
                }
            }

            @Override
            public void ItemClick(String orderJson) {
                Intent intent = new Intent(getActivity(), OrderdetailActivity.class);
                intent.putExtra("order", orderJson);
                startActivity(intent);
            }
        };
        return onClick;
    }



}