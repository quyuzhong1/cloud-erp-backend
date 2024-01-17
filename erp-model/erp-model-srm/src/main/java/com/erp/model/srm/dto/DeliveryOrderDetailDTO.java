package com.erp.model.srm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 送货单明细请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-15
*/
@Data
@NoArgsConstructor
public class DeliveryOrderDetailDTO implements Serializable {

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  detailId;

        /**
        * 送货单Id
        */
        private String mainId;

        /**
        * 来源id明细
        */
        private String sourceDetailId;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 采购数量
         */
        private Integer orderQty;

        /**
        * 送货数量
        */
        private Integer deliveryQty;

        /**
        * 赠品数量
        */
        private Integer giftQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 赠品收货数量
        */
        private Integer giftReceiveQty;

        /**
        * 备注
        */
        private String remark;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
        private String detailId;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 送货单Id
        */
        private String mainId;

        /**
        * 来源id明细
        */
        @NotBlank(message = "来源id明细不能为空")
        @Size(max = 19,message = "来源id明细最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;
        /**
         * skuNo
         */
        @NotBlank(message = "skuNo不能为空")
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;

        /**
         * 订单数量
         */
        private Integer orderQty;

        /**
        * 送货数量
        */
        private Integer deliveryQty;

        /**
        * 赠品数量
        */
        private Integer giftQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 赠品收货数量
        */
        private Integer giftReceiveQty;

        /**
        * 质检合格数
        */
        private Integer qcGoodQty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 是否加急
        */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent;
        /**
         * 预计到达日期
         */
        private LocalDate planDeliveryDate;
    }


}