package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Description: 多规格sku信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
@NoArgsConstructor
public class ProductDetailDTO implements Serializable {
    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品表id
     */
    private String productId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 产品sku名称
     */
    @Size(max = 50, message = "产品名称最大50字符")
    private String name;

    /**
     * 变体属性
     */
    private String variantProperty;

    /**
     * 计划上市时间
     */
    private LocalDate planListingTime;

    /**
     * 首批量产入库时间
     */
    private LocalDate firstMassProductDate;

    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private Integer productState;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * sku图片
     */
    private String imagesUrl;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 产品负责人id
     */
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    private String chargeName;

    /**
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    private Integer isFinishTask;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过
     */
    private Integer status;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程表id
     */
    private String businessProcessId;

    private static final long serialVersionUID = 1L;


    /**
     * 搜索sku
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {


        /**
         * 搜索关键字
         */
        private String searchKeyword;


        /**
         * 状态
         */
        private  Integer status;

    }

    /**
     * 采购员、供应商信息
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseSupplierInfoDTO {


        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 采购员id
         */
        private  String purchaseUserId;

        /**
         * 采购员名称
         */
        private  String purchaseUserName;

        /**
         * 供应商id
         */
        private  String supplierId;

        /**
         * 供应商名称
         */
        private  String supplierName;


    }

}
