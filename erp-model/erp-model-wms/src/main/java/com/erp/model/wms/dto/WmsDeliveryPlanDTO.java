package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 发货计划请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class WmsDeliveryPlanDTO implements Serializable {


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
         private Map<String, String> sqlMap;

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
        private String id;
        /**
         * 详情id
         */
        private String detailId;

        /**
         * 编号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态中文名
         */
        private String approveStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态中文名
         */
        private String invalidStatusName;

        /**
         * 发货状态
         */
        private String deliveryStatus;

        /**
         * 发货状态中文名
         */
        private String deliveryStatusName;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 货件/入库单号
         */
        private String refCode;

        /**
         * 目的仓库
         */
        private String toWarehouseId;

        /**
         * 目的仓库中文名
         */
        private String toWarehouseName;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家中文名
         */
        private String countryName;

        /**
         * 第三方仓SKU
         */
        private String thirdWarehouseSku;
        /**
         * 平台产品id
         */
        private String platformProductId;

        /**
         * 平台sku
         */
        private String platformSku;

        /**
         * FNSKU
         */
        private String platformFnSku;
        /**
         * 产品id
         */
        private String skuId;

        /**
         * 产品编号
         */
        private String skuNo;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划数量
         */
        private Integer planQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人中文名
         */
        private String createUserName;

        /**
         * 待审核人名称
         */
        private String waitApproveUserName;

        /**
         * 审核人id
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 来源平台
         */
        private String provideCode;

        /**
         * 单据类型
         */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;
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
        private String id;

        /**
         * 类型
         */
        private String type;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
        * 单据编号
        */
        private String code;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
        * 目的仓id
        */
        private String toWarehouseId;

        /**
        * 目的仓中文名
        */
        private String toWarehouseName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 国家中文名
        */
        private String countryName;

        /**
        * 计划发货日期
        */
        private LocalDate planDeliveryDate;

        /**
        * 备注
        */
        private String remark;

        /**
        * 详情
        */
        private List<WmsDeliveryPlanDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 详情
         */
        @Valid
        private List<WmsDeliveryPlanDetailDTO.AddDTO> detailList;
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

        /**
         * 详情
         */
        @Valid
        private List<WmsDeliveryPlanDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 类型 /wms/dict/drop/down?type=deliveryPlanType
         */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
        * 目的仓id
        */
        private String toWarehouseId;

        /**
         * 店铺Id
         */
        private String shopId;

        /**
        * 计划发货时间
        */
        private LocalDate planDeliveryDate;

        /**
        * 描述
        */
        private String remark;


    }

    /**
     * 发货记录
     */
    @Data
    @NoArgsConstructor
    public static class DeliverRecordDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 发货单号
         */
        private String deliveryCode;

        /**
         * 发货状态编码
         */
        private String deliveryStatus;

        /**
         * 发货状态名称
         */
        private String deliveryStatusName;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 发货时间
         */
        private List<LocalDateTime> deliverTime;

    }

    /**
     * 下推要货申请列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateRequisitionApplicationViewDTO {
        /**
         * 主表id
         */
        private String sourceId;

        /**
         * 明细id
         */
        private String sourceDetailId;

        /**
         * 发货计划单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 状态
         */
        private String approveStatus;

        /**
         * 要货类型
         */
        private String type;

        /**
         * 要货类型名称
         */
        private String typeName;

        /**
         * 渠道
         */
        private String channelId;

        /**
         * 渠道中文名
         */
        private String channelName;

        /**
         * sku表id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划数量
         */
        private Integer planQty;

        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;

        /**
         * 可用库存
         */
        private Integer usableQty;

        /**
         * 要货数量
         */
        private Integer requisitionQty;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 平台(第三方仓)sku（msku）
         */
        private String platformSku;

        /**
         * 平台产品id（asin）
         */
        private String asin;

        /**
         * fnSku
         */
        private String fnSku;

        /**
         * 平台产品名称
         */
        private String platformSkuName;

        /**
         * 单箱数量
         */
        private Integer boxQty;

        /**
         * 发货计划类型
         */
        private String deliveryPlanType;

        /**
         * 店铺Id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 虚拟仓id
         */
        private String fromVirtualWarehouseId;
        /**
         * 虚拟仓name
         */
        private String fromVirtualWarehouseName;
    }

    /**
     * 下推发货单列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateDeliverViewDTO {

        /**
         * 主表id
         */
        private String sourceId;

        /**
         * 明细id
         */
        private String sourceDetailId;

        /**
         * 发货计划单号
         */
        private String sourceCode;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 国家二字码
         */
        private String country;

        /**
         * 国家中文名
         */
        private String countryName;

        /**
         * 发货仓id
         */
        private String deliveryWarehouseId;

        /**
         * 目的仓id
         */
        private String toWarehouseId;

        /**
         * 目的仓名称
         */
        private String toWarehouseName;

        /**
         * 状态
         */
        private String approveStatus;

        /**
         * skuId
         */
        private String skuId;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 计划数量
         */
        private Integer planQty;

        /**
         * 发货数量
         */
        private Integer deliveryQty;

        /**
         * 平台sku
         */
        private String platformSku;

        /**
         * 平台产品名称
         */
        private String platformSkuName;
        /**
         * 类型
         */
        private String type;
    }


}