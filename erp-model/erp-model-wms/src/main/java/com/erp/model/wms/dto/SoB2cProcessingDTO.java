package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * B2C虚拟仓订单跟踪请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-12-18
*/
@Data
@NoArgsConstructor
public class SoB2cProcessingDTO implements Serializable {


    /**
     * 分页参数
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

        /**
         * 类型，alreadyOut已出，deliveryFreeze发货冻结，unDelivery7日未发
         */
        private String label;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id【可排序】
         */
        private String id;
        /**
         * b2c销售订单编号【可排序】
         */
        private String b2cSoCode;
        /**
         * 发货单id【可排序】
         */
        private String deliveryId;
        /**
         * 发货单号【可排序】
         */
        private String deliveryCode;
        /**
         * 发货单状态【可排序】
         */
        private String deliveryStatus;
        /**
         * 发货单状态名称
         */
        private String deliveryStatusName;
        /**
         * 发货数量【可排序】
         */
        private Integer deliveryQty;
        /**
         * SKU【可排序】
         */
        private String skuId;
        /**
         * SKU编码【可排序】
         */
        private String skuNo;
        /**
         * 产品名称【可排序】
         */
        private String productName;
        /**
         * 仓库id【可排序】
         */
        private String warehouseId;
        /**
         * 仓库名称【可排序】
         */
        private String warehouseName;
        /**
         * 虚拟仓id【可排序】
         */
        private String virtualWarehouseId;
        /**
         * 虚拟仓名称【可排序】
         */
        private String virtualWarehouseName;
        /**
         * 冻结时间【可排序】
         */
        private LocalDateTime frozenTime;
        /**
         * 剩余冻结数量【可排序】
         */
        private Integer frozenQty;
        /**
         * 冻结时长（天）【可排序】
         */
        private Integer frozenDays;
        /**
         * 出库单据id【可排序】
         */
        private String outstockOrderId;
        /**
         * 出库单据编码【可排序】
         */
        private String outstockOrderCode;
        /**
         * 出库单据类型（同sourceType）【可排序】
         */
        private String outstockOrderType;
        /**
         * 出库单据类型名称（同sourceType）
         */
        private String outstockOrderTypeName;
        /**
         * 出库单据状态【可排序】
         */
        private String outstockOrderStatus;
        /**
         * 出库单据状态名称
         */
        private String outstockOrderStatusName;
        /**
         * 出库单据时间【可排序】
         */
        private LocalDateTime outstockOrderTime;
        /**
         * 出库数量【可排序】
         */
        private Integer outstockQty;

        /**
         * 更新时间【可排序】
         */
        private LocalDateTime updateTime;

        /**
         * 标签，outstock出库,frozen发货冻结,unShipped七日未发
         */
        private List<String> labelList;
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
        * b2c销售订单id
        */
        private String b2cSoId;

        /**
        * b2c销售订单编码
        */
        private String b2cSoCode;

        /**
        * 发货单id
        */
        private String deliveryId;

        /**
        * 发货单号
        */
        private String deliveryCode;

        /**
        * 发货单状态
        */
        private String deliveryStatus;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * skuId
        */
        private String skuId;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        private String virtualWarehouseId;

        /**
        * 冻结时间
        */
        private LocalDateTime frozenTime;

        /**
        * 冻结数量
        */
        private Integer frozenQty;

        /**
        * |出库单据id
        */
        private String outstockOrderId;

        /**
        * |出库单据编码
        */
        private String outstockOrderCode;

        /**
        * |出库单据类型（同sourceType）
        */
        private String outstockOrderType;

        /**
        * |出库单据状态
        */
        private String outstockOrderStatus;

        /**
        * |出库单据时间
        */
        private LocalDateTime outstockOrderTime;

        /**
        * 出库数量
        */
        private Integer outstockQty;

        /**
        * 父级skuId
        */
        private String parentSkuId;

        /**
        * bom版本
        */
        private String bomVersion;


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
    public static class AddOrUpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * b2c销售订单id
        */
        @NotBlank(message = "b2c销售订单id不能为空")
        @Size(max = 19,message = "b2c销售订单id最大长度不能超过19位")
        private String b2cSoId;

        /**
        * b2c销售订单编码
        */
        @NotBlank(message = "b2c销售订单编码不能为空")
        @Size(max = 32,message = "b2c销售订单编码最大长度不能超过32位")
        private String b2cSoCode;

        /**
        * 发货单id
        */
        @NotBlank(message = "发货单id不能为空")
        @Size(max = 19,message = "发货单id最大长度不能超过19位")
        private String deliveryId;

        /**
         * 发货单明显id
         */
        @NotBlank(message = "发货单明显id不能为空")
        @Size(max = 19,message = "发货单明显id最大长度不能超过19位")
        private String deliveryDetailId;

        /**
        * 发货单号
        */
        @NotBlank(message = "发货单号不能为空")
        @Size(max = 32,message = "发货单号最大长度不能超过32位")
        private String deliveryCode;

        /**
        * 发货单状态
        */
        @NotBlank(message = "发货单状态不能为空")
        @Size(max = 32,message = "发货单状态最大长度不能超过32位")
        private String deliveryStatus;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 32,message = "skuId最大长度不能超过32位")
        private String skuId;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 虚拟仓id
        */
        @NotBlank(message = "虚拟仓id不能为空")
        @Size(max = 19,message = "虚拟仓id最大长度不能超过19位")
        private String virtualWarehouseId;

        /**
        * 冻结时间
        */
        private LocalDateTime frozenTime;

        /**
        * 冻结数量
        */
        @NotNull(message = "冻结数量不能为空")
        private Integer frozenQty;

        /**
        * |出库单据id
        */
        @NotBlank(message = "|出库单据id不能为空")
        private String outstockOrderId;

        /**
        * |出库单据编码
        */
        @NotBlank(message = "|出库单据编码不能为空")
        @Size(max = 32,message = "|出库单据编码最大长度不能超过32位")
        private String outstockOrderCode;

        /**
        * |出库单据类型（同sourceType）
        */
        @NotBlank(message = "|出库单据类型（同sourceType）不能为空")
        @Size(max = 32,message = "|出库单据类型（同sourceType）最大长度不能超过32位")
        private String outstockOrderType;

        /**
        * |出库单据状态
        */
        @NotBlank(message = "|出库单据状态不能为空")
        @Size(max = 32,message = "|出库单据状态最大长度不能超过32位")
        private String outstockOrderStatus;

        /**
        * |出库单据时间
        */
        private LocalDateTime outstockOrderTime;

        /**
        * 出库数量
        */
        @NotNull(message = "出库数量不能为空")
        private Integer outstockQty;

        /**
        * 父级skuId
        */
        private String parentSkuId;

        /**
        * bom版本
        */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 32,message = "bom版本最大长度不能超过32位")
        private String bomVersion;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 店铺id
         */
        private String shopId;
    }


}