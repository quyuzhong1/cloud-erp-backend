package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.common.annotation.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ProductSkuBaseInfoDTO {

    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品信息表主id
     */
    private String productId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 计划上市时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date planListingTime;

    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "产品状态错误")
    private Integer productState;

    /**
     * 产品图片
     */
    private String imagesUrl;

    /**
     * 产品负责人id
     */
    private String chargeId;

    /**
     * 产品负责人名称
     */
    private String chargeName;

    /**
     * 品名
     */
    private String name;

    /**
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    private Integer isFinishTask;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过
     */
    private Integer status;
}
