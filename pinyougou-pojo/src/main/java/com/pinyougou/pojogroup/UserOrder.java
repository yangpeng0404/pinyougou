package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author LiuXiaoDong
 * @date 2019/7/24  9:20
 * @package com.pinyougou.pojogroup
 * When I wrote this, only God and I understood what I was doing
 * Now, God only knows
 */
public class UserOrder implements Serializable {

    private Date createTime ; //创建时间
    private String status;//订单状态
    private List<TbOrderItem> orderItemList;//商品订单详情
    private String goodsName; //商品名
    private String sellerId;//店铺
    private Long orderId;//订单号

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    @Override
    public String toString() {
        return "UserOrder{" +
                "createTime=" + createTime +
                ", status='" + status + '\'' +
                ", orderItemList=" + orderItemList +
                ", goodsName='" + goodsName + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}