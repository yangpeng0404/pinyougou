package com.pinyougou.pojo;

import java.io.Serializable;
import javax.persistence.*;

@Table(name = "tb_specification")
public class TbSpecification implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    /**
     * 状态 状态id  0代表正在审核 1 代表通过 2代表驳回 状态id  0代表正在审核 1 代表通过 2代表驳回
     */
    @Column(name = "status")
    private String status;

    /**
     * 申请商家名称
     */
    @Column(name = "seller_id")
    private String sellerId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    @Override
    public String toString() {
        return "TbSpecification{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", sellerId='" + sellerId + '\'' +
                ", specName='" + specName + '\'' +
                '}';
    }

    /**
     * 名称
     */
    @Column(name = "spec_name")
    private String specName;


    private static final long serialVersionUID = 1L;

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取名称
     *
     * @return spec_name - 名称
     */
    public String getSpecName() {
        return specName;
    }

    /**
     * 设置名称
     *
     * @param specName 名称
     */
    public void setSpecName(String specName) {
        this.specName = specName;
    }
}