package gdou.gdou_chb.model;

import com.kymjs.rxvolley.rx.Result;

import rx.Observable;

/**
 * Created by WT on 2016/11/30.
 */
public interface GoodModel {
    /**
     * 查询商品
     * @param shopId
     * @return
     */
    Observable<Result> findByGoodsId(Long shopId);
}

