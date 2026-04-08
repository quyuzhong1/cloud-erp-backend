package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 质检通知单明细请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-21
*/
@Data
@NoArgsConstructor
public class QcNoticeDetailDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * main_id
        */
        private String mainId;

        /**
        * sku_id
        */
        private String skuId;

        /**
        * sku_no
        */
        private String skuNo;

        /**
        * sku_name
        */
        private String productName;

        /**
        * 质检通知数量
        */
        private Integer qcNoticeQty;

        /**
        * 质检数量
        */
        private Integer qcQty;

        /**
        * 送检差异数量
        */
        private Integer qcDiffQty;

        /**
        * 良品数量
        */
        private Integer qcGoodQty;

        /**
        * 不良品数量
        */
        private Integer qcBadQty;

        /**
        * 上架数量
        */
        private Integer putawayQty;

        /**
        * 不良备注
        */
        private String badDesc;

        /**
        * 质检员id
        */
        private String qcUserId;

        /**
        * 质检员
        */
        private String qcUserName;

        /**
        * 问题属性 type=qcProblemType
        */
        private String qcProblemDict;

        /**
        * 质检状态 QcBillStatusEnum
        */
        private String qcStatus;

        /**
        * 质检时间
        */
        private LocalDateTime qcDate;

        /**
        * 上架状态 待上架:wait  部分上架：part  已上架：finish
        */
        private String putawayStatus;

        /**
        * 上架时间
        */
        private LocalDateTime putawayDate;

        private List<String> attachNameList;
        private List<String> attachUrlList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 供应商id
         */
        private String supplierId;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * main_id
        */
        private String mainId;

        /**
        * sku_id
        */
        @NotBlank(message = "sku_id不能为空")
        @Size(max = 19,message = "sku_id最大长度不能超过19位")
        private String skuId;

        /**
        * sku_name
        */
        private String skuNo;
        /**
        * sku_name
        */
        private String productName;

        /**
        * 质检通知数量
        */
        @NotNull(message = "质检通知数量不能为空")
        @Min(value = 1,message = "质检通知数量不能小于0")
        private Integer qcNoticeQty;

        /**
        * 质检数量
        */
//        @NotNull(message = "质检数量不能为空")
        private Integer qcQty;

        /**
        * 送检差异数量
        */
//        @NotNull(message = "送检差异数量不能为空")
        private Integer qcDiffQty;

        /**
        * 良品数量
        */
//        @NotNull(message = "良品数量不能为空")
        private Integer qcGoodQty;

        /**
        * 不良品数量
        */
//        @NotNull(message = "不良品数量不能为空")
        private Integer qcBadQty;

        /**
        * 上架数量
        */
//        @NotNull(message = "上架数量不能为空")
        private Integer putawayQty;

        /**
        * 不良备注
        */
//        @NotBlank(message = "不良备注不能为空")
        @Size(max = 255,message = "不良备注最大长度不能超过255位")
        private String badDesc;

        /**
        * 质检员id
        */
//        @NotBlank(message = "质检员id不能为空")
//        @Size(max = 19,message = "质检员id最大长度不能超过19位")
        private String qcUserId;

        /**
        * 质检员
        */
        private String qcUserName;

        /**
        * 问题属性 type=qcProblemType
        */
//        @NotBlank(message = "问题属性 type=qcProblemType不能为空")
//        @Size(max = 64,message = "问题属性 type=qcProblemType最大长度不能超过64位")
        private String qcProblemDict;

        /**
        * 质检状态 QcBillStatusEnum
        */
//        @NotBlank(message = "质检状态 QcBillStatusEnum不能为空")
//        @Size(max = 64,message = "质检状态 QcBillStatusEnum最大长度不能超过64位")
        private String qcStatus;

        /**
        * 质检时间
        */
        private LocalDateTime qcDate;

        /**
        * 上架状态 待上架:wait  部分上架：part  已上架：finish
        */
//        @NotBlank(message = "上架状态 待上架:wait  部分上架：part  已上架：finish不能为空")
//        @Size(max = 64,message = "上架状态 待上架:wait  部分上架：part  已上架：finish最大长度不能超过64位")
        private String putawayStatus;

        /**
        * 上架时间
        */
        private LocalDateTime putawayDate;

        /**
         * 来源明细id
         */
        private String sourceDetailId;
    }


}