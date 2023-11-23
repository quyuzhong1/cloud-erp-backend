package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

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
public class OverseasDeliveryPlanDTO implements Serializable {


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
         * 搜索类型
         */
         private String tabFlag;

         /**
         * 单号
         */
         private String code;

         /**
         * 产品编号
         */
         private List<String> skuNoList;

         /**
         * 审核状态
         */
         private List<String> approveStatusList;

         /**
         * 发货状态
         */
         private List<String> deliveryStatusList;

         /**
         * 发货单号
         */
         private String deliveryCode;

         /**
         * 目的仓库
         */
         private List<String> toWarehouseIdList;

         /**
         * 国家
         */
         private List<String> countryList;

         /**
         * 是否组合品
         */
         private Boolean isCombination;

         /**
         * 操作人
         */
         private List<String> updateUserIdList;

         /**
         * 创建时间
         */
         private List<LocalDate> createTimeList;

         /**
         * 审核时间
         */
         private List<LocalDate> approveTimeList;
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
        private List<OverseasDeliveryPlanDetailDTO.ViewDTO> detailList;
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
        private List<OverseasDeliveryPlanDetailDTO.AddDTO> detailList;
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
        private List<OverseasDeliveryPlanDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 目的仓id
        */
        private String toWarehouseId;

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
    }


}