package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * b2c退货订单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
*/
@Data
@NoArgsConstructor
public class SoB2cReturnDTO implements Serializable {




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
        * 单据编号
        */
        private String code;

        /**
        * 平台订单号
        */
        private String platformOrderNo;

        /**
        * 平台退货单号
        */
        private String platformReturnNo;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售订单编号
        */
        private String soCode;

        /**
        * 平台
        */
        private String dictPlatform;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 订单金额
        */
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        private String currency;

        /**
        * 退货类型
        */
        private String type;

        /**
        * 退货原因
        */
        private String reason;

        /**
        * 退货状态
        */
        private String status;

        /**
        * 退货入库单号
        */
        private String returnInstockCode;

        /**
        * 系统退货时间
        */
        private LocalDateTime sysReturnTime;


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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 50,message = "平台订单号最大长度不能超过50位")
        private String platformOrderNo;

        /**
        * 平台退货单号
        */
        @NotBlank(message = "平台退货单号不能为空")
        @Size(max = 50,message = "平台退货单号最大长度不能超过50位")
        private String platformReturnNo;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售订单编号
        */
        @NotBlank(message = "销售订单编号不能为空")
        @Size(max = 50,message = "销售订单编号最大长度不能超过50位")
        private String soCode;

        /**
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 30,message = "平台最大长度不能超过30位")
        private String dictPlatform;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 订单金额
        */
        @NotNull(message = "订单金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "订单金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        @NotBlank(message = "币别（原币）不能为空")
        @Size(max = 32,message = "币别（原币）最大长度不能超过32位")
        private String currency;

        /**
        * 退货类型
        */
        @NotBlank(message = "退货类型不能为空")
        @Size(max = 30,message = "退货类型最大长度不能超过30位")
        private String type;

        /**
        * 退货原因
        */
        @NotBlank(message = "退货原因不能为空")
        @Size(max = 255,message = "退货原因最大长度不能超过255位")
        private String reason;

        /**
        * 退货状态
        */
        @NotBlank(message = "退货状态不能为空")
        @Size(max = 30,message = "退货状态最大长度不能超过30位")
        private String status;

        /**
        * 系统退货时间
        */
        @NotNull(message = "系统退货时间不能为空")
        private LocalDateTime sysReturnTime;


    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingViewDTO {
        /**
         * 退货单id
         */
        private String id;
        /**
         * 退货单号
         */
        private String code;
        /**
         * 平台退货单号
         */
        private String platformReturnNo;
        /**
         * 平台订单编号
         */
        private String platformOrderNo;
        /**
         * 销售id
         */
        private String soId;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 订单金额
         */
        private BigDecimal orderAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 退款金额 + 币别
         */
        private String completeOrderAmount;

        /**
         * 退货类型
         */
        private String type;

        /**
         * 退货类型名称
         */
        private String typeName;

        /**
         * 退货原因
         */
        private String reason;

        /**
         * 退货状态
         */
        private String status;
        /**
         * 退货状态Name
         */
        private String statusName;

        /**
         * 关联入库单号
         */
        private String instockCode;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 入库状态
         */
        private String instockStatus;
        /**
         * 入库状态Name
         */
        private String instockStatusName;
        /**
         * 销售数量
         */
        private Integer saleQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 出库数量
         */
        private Integer outQty;
        /**
         * 入库数量
         */
        private Integer instockQty;
        /**
         * 备注
         */
        private String remark;

        /**
         * 系统创建时间
         */
        private LocalDateTime sysCreateTime;

        /**
         * 系统退货时间
         */
        private LocalDateTime sysReturnTime;

        /**
         * 系统入库时间
         */
        private LocalDateTime sysInstockTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateSoReturnNoticeView {

        private String id;

        private String code;

        private String soId;
        private String status;
        /**
         * 平台
         */
        private String platform;
        /**
         * 平台名称
         */
        private String platformName;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量
         */
        private Integer saleQty;
        /**
         * 出库数量
         */
        private Integer outQty;

        /**
         * 退货物流单号
         */
        private String returnLogisticCode;

        /**
         * 退货数量
         */
        @NotNull(message = "退货数量不能为空")
        private Integer returnQty;

        /**
         * 退货仓库
         */
        @NotNull(message = "退货仓库不能为空")
        private String returnWarehouseId;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 备注
         */
        private String remark;
    }
}