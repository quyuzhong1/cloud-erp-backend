package com.erp.model.oms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 多渠道订单主表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SoMultiChannelDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
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
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 单据编码
        */
        private String code;

        /**
        * 卖家订单编号/发货单号
        */
        private String deliveryCode;

        /**
        * 发货平台
        */
        private String deliveryPlatform;

        /**
        * 销售平台
        */
        private String dictPlatform;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 货件编号
        */
        private String shipmentCode;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售单据编码
        */
        private String soCode;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 订单状态
        */
        private String billStatus;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名
        */
        private String logisticsChannelName;

        /**
        * 发货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 配送条件
        */
        private String shippingMethod;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
        */
        private String approveUserName;

        /**
        * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        */
        private String abnormalType;

        /**
        * 订单异常标示
        */
        private String signOrderError;

        /**
        * 订单备注
        */
        private String remark;

        /**
        * 系统是否已出库
        */
        private Boolean hasOutstock;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

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
        * 单据编码
        */
        private String code;

        /**
        * 卖家订单编号/发货单号
        */
        private String deliveryCode;

        /**
        * 发货平台
        */
        private String deliveryPlatform;

        /**
        * 销售平台
        */
        private String dictPlatform;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 货件编号
        */
        private String shipmentCode;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 销售单据编码
        */
        private String soCode;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 订单状态
        */
        private String billStatus;

        /**
        * 发货状态
        */
        private String deliveryStatus;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道名
        */
        private String logisticsChannelName;

        /**
        * 发货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 配送条件
        */
        private String shippingMethod;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
        */
        private String approveUserName;

        /**
        * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        */
        private String abnormalType;

        /**
        * 订单异常标示
        */
        private String signOrderError;

        /**
        * 订单备注
        */
        private String remark;

        /**
        * 系统是否已出库
        */
        private Boolean hasOutstock;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细列表
         */
        private List<SoMultiChannelDetailDTO.AddDTO> detailList;
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
        * 卖家订单编号/发货单号
        */
        @NotBlank(message = "卖家订单编号/发货单号不能为空")
        @Size(max = 32,message = "卖家订单编号/发货单号最大长度不能超过32位")
        private String deliveryCode;

        /**
        * 发货平台
        */
        @NotBlank(message = "发货平台不能为空")
        @Size(max = 32,message = "发货平台最大长度不能超过32位")
        private String deliveryPlatform;

        /**
        * 销售平台
        */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 32,message = "销售平台最大长度不能超过32位")
        private String dictPlatform;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 100,message = "平台订单号最大长度不能超过100位")
        private String platformCode;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 100,message = "店铺名称最大长度不能超过100位")
        private String shopName;

        /**
        * 货件编号
        */
        @NotBlank(message = "货件编号不能为空")
        @Size(max = 32,message = "货件编号最大长度不能超过32位")
        private String shipmentCode;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 19,message = "销售订单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售单据编码
        */
        @NotBlank(message = "销售单据编码不能为空")
        @Size(max = 32,message = "销售单据编码最大长度不能超过32位")
        private String soCode;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 100,message = "物流跟踪号最大长度不能超过100位")
        private String trackNo;

        /**
        * 订单状态
        */
        @NotBlank(message = "订单状态不能为空")
        @Size(max = 32,message = "订单状态最大长度不能超过32位")
        private String billStatus;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 32,message = "发货状态最大长度不能超过32位")
        private String deliveryStatus;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道名
        */
        @NotBlank(message = "物流渠道名不能为空")
        @Size(max = 100,message = "物流渠道名最大长度不能超过100位")
        private String logisticsChannelName;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 19,message = "发货仓库id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        @NotBlank(message = "发货仓库名称不能为空")
        @Size(max = 100,message = "发货仓库名称最大长度不能超过100位")
        private String deliveryWarehouseName;

        /**
        * 配送条件
        */
        @NotBlank(message = "配送条件不能为空")
        @Size(max = 32,message = "配送条件最大长度不能超过32位")
        private String shippingMethod;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        */
        @NotBlank(message = "异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）不能为空")
        @Size(max = 32,message = "异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）最大长度不能超过32位")
        private String abnormalType;

        /**
        * 订单异常标示
        */
        @NotBlank(message = "订单异常标示不能为空")
        @Size(max = 30,message = "订单异常标示最大长度不能超过30位")
        private String signOrderError;

        /**
        * 订单备注
        */
        @NotBlank(message = "订单备注不能为空")
        private String remark;

        /**
        * 系统是否已出库
        */
        @NotNull(message = "系统是否已出库不能为空")
        private Boolean hasOutstock;


    }


    @Data
    @NoArgsConstructor
    public static class SoViewDTO {
        /**
         * 订单ID
         */
        private String soId;
        /**
         * 订单编码
         */
        private String soCode;
        private String approveStatus;
        private String billStatus;
        /**
         * 销售平台
         */
        private String dictPlatform;
        /**
         * 销售平台名称
         */
        private String dictPlatformName;
        /**
         * 平台订单号
         */
        private String platformCode;
        /**
         * 订单详情ID
         */
        private String soDetailId;
        /**
         * 商品ID
         */
        private String skuId;
        /**
         * 商品编码
         */
        private String skuNo;
        /**
         * 商品名称
         */
        private String productName;
        /**
         * 订单数量
         */
        private Integer qty;
        /**
         * FBA可售
         */
        private Integer fulfillableQty;
        /**
         * 发货数量
         */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;
        /**
         * 平台SKU
         */
        @NotBlank(message = "平台SKU不能为空")
        private String platformSkuNo;
        /**
         * 平台产品id
         */
        private String platformSpuNo;
        /**
         * _fn_商品编码
         */
        private String fnSku;
        /**
         * 平台sku
         */
        private String asin;
        /**
         * 平台商品名称
         */
        private String platformProductName;

    }

    @Data
    @NoArgsConstructor
    public static class IdsDTO {
        /**
         * 订单ID列表
         */
        private List<String> soIds;
        /**
         * 店铺ID
         */
        private String shopId;
        /**
         * 发货仓库ID
         */
        private String deliveryWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class SaveDTO {
        /**
         * 店铺ID
         */
        @NotBlank(message = "店铺ID不能为空")
        private String shopId;
        private String shopName;
        private String deliveryPlatform;
        @NotBlank(message = "发货仓库ID不能为空")
        private String deliveryWarehouseId;
        private String deliveryWarehouseName;
        @NotBlank(message = "物流渠道ID不能为空")
        private String logisticsChannelId;
        private String logisticsChannelName;
        @NotBlank(message = "配送条件不能为空")
        private String shippingMethod;
        private String remark;
        /**
         * 订单发货明细
         */
        @NotEmpty(message = "订单发货明细不能为空")
        @Valid
        private List<SoViewDTO> detailList;
    }
}