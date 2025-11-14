package com.erp.model.dmp.dto;

import java.util.Date;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 平台在途报告请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
*/
@Data
@NoArgsConstructor
public class AdsErpFirstMileInTransitDiffDTO implements Serializable {



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
        * 唯一标识md5（如main_id+sku_no）
        */
        private String uniqueCode;

        /**
        * 数据字段md5值（用于去重或校验）
        */
        private String dataEncrypt;

        /**
        * 来源系统：amazon
        */
        private String sourceSystem;

        /**
        * 平台账号编码
        */
        private String accountCode;

        /**
        * 店铺ID/授权ID
        */
        private String nextLevelId;

        /**
        * 业务类型
        */
        private String billTopic;

        /**
        * 流程id
        */
        private String flowId;

        /**
        * ETL状态 ready=可处理，unready=未处理
        */
        private String etlStatus;

        /**
        * 节点id
        */
        private String nodeId;

        /**
        * 实例id
        */
        private String instanceId;

        /**
        * 任务id
        */
        private String taskId;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 是否最后一次更新记录
        */
        private String lastUpdateFlag;

        /**
        * 核算月份（YYYY-MM）
        */
        private String checkMonth;

        /**
        * 货件ID
        */
        private String shipmentId;

        /**
        * 货件单号
        */
        private String shipmentCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 客户id
        */
        private String customerId;

        /**
        * 客户姓名
        */
        private String customerName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 货件签收时间
        */
        private Date shipmentReceiveTime;

        /**
        * 货件调整时间
        */
        private Date shipmentAdjustTime;

        /**
        * 目的仓库id
        */
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        private String warehouseName;

        /**
        * 在途仓库id
        */
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        private String platformStockSku;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * ERP SKU编码
        */
        private String skuNo;

        /**
        * 货件明细id
        */
        private String shipmentDetailId;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
        * 期初在途数量
        */
        private Integer initTransitQty;

        /**
        * 本期发货数量
        */
        private Integer currentDeliveryQty;

        /**
        * 本期签收数量
        */
        private Integer currentReceiveQty;

        /**
        * 期末在途数量
        */
        private Integer endPeriodTransitQty;

        /**
        * 期末在途调整数量
        */
        private Integer endPeriodTransitAdjustQty;

        /**
        * 期末在途数量（调整后）
        */
        private Integer afterEndPeriodTransitQty;

        /**
        * 调整原因
        */
        private String adjustReason;

        /**
        * 调整时间
        */
        private Date adjustTime;

        /**
        * 调整人名称
        */
        private String adjustUserName;

        /**
        * 调整人id
        */
        private String adjustUserId;

        /**
        * 备注或附加说明
        */
        private String remark;


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
        * 唯一标识md5（如main_id+sku_no）
        */
        private String uniqueCode;

        /**
        * 数据字段md5值（用于去重或校验）
        */
        private String dataEncrypt;

        /**
        * 来源系统：amazon
        */
        private String sourceSystem;

        /**
        * 平台账号编码
        */
        private String accountCode;

        /**
        * 店铺ID/授权ID
        */
        private String nextLevelId;

        /**
        * 业务类型
        */
        private String billTopic;

        /**
        * 流程id
        */
        private String flowId;

        /**
        * ETL状态 ready=可处理，unready=未处理
        */
        private String etlStatus;

        /**
        * 节点id
        */
        private String nodeId;

        /**
        * 实例id
        */
        private String instanceId;

        /**
        * 任务id
        */
        private String taskId;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 是否最后一次更新记录
        */
        private String lastUpdateFlag;

        /**
        * 核算月份（YYYY-MM）
        */
        private String checkMonth;

        /**
        * 货件ID
        */
        private String shipmentId;

        /**
        * 货件单号
        */
        private String shipmentCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 客户id
        */
        private String customerId;

        /**
        * 客户姓名
        */
        private String customerName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 货件签收时间
        */
        private Date shipmentReceiveTime;

        /**
        * 货件调整时间
        */
        private Date shipmentAdjustTime;

        /**
        * 目的仓库id
        */
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        private String warehouseName;

        /**
        * 在途仓库id
        */
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        private String platformStockSku;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * ERP SKU编码
        */
        private String skuNo;

        /**
        * 货件明细id
        */
        private String shipmentDetailId;

        /**
        * 申报数量
        */
        private Integer declareQty;

        /**
        * 收货数量
        */
        private Integer receiveQty;

        /**
        * 发货数量
        */
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        private Integer diffQty;

        /**
        * 期初在途数量
        */
        private Integer initTransitQty;

        /**
        * 本期发货数量
        */
        private Integer currentDeliveryQty;

        /**
        * 本期签收数量
        */
        private Integer currentReceiveQty;

        /**
        * 期末在途数量
        */
        private Integer endPeriodTransitQty;

        /**
        * 期末在途调整数量
        */
        private Integer endPeriodTransitAdjustQty;

        /**
        * 期末在途数量（调整后）
        */
        private Integer afterEndPeriodTransitQty;

        /**
        * 调整原因
        */
        private String adjustReason;

        /**
        * 调整时间
        */
        private Date adjustTime;

        /**
        * 调整人名称
        */
        private String adjustUserName;

        /**
        * 调整人id
        */
        private String adjustUserId;

        /**
        * 备注或附加说明
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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 唯一标识md5（如main_id+sku_no）
        */
        @NotBlank(message = "唯一标识md5（如main_id+sku_no）不能为空")
        @Size(max = 192,message = "唯一标识md5（如main_id+sku_no）最大长度不能超过192位")
        private String uniqueCode;

        /**
        * 数据字段md5值（用于去重或校验）
        */
        @NotBlank(message = "数据字段md5值（用于去重或校验）不能为空")
        @Size(max = 192,message = "数据字段md5值（用于去重或校验）最大长度不能超过192位")
        private String dataEncrypt;

        /**
        * 来源系统：amazon
        */
        @NotBlank(message = "来源系统：amazon不能为空")
        @Size(max = 192,message = "来源系统：amazon最大长度不能超过192位")
        private String sourceSystem;

        /**
        * 平台账号编码
        */
        @NotBlank(message = "平台账号编码不能为空")
        @Size(max = 192,message = "平台账号编码最大长度不能超过192位")
        private String accountCode;

        /**
        * 店铺ID/授权ID
        */
        @NotBlank(message = "店铺ID/授权ID不能为空")
        @Size(max = 192,message = "店铺ID/授权ID最大长度不能超过192位")
        private String nextLevelId;

        /**
        * 业务类型
        */
        @NotBlank(message = "业务类型不能为空")
        @Size(max = 192,message = "业务类型最大长度不能超过192位")
        private String billTopic;

        /**
        * 流程id
        */
        private String flowId;

        /**
        * ETL状态 ready=可处理，unready=未处理
        */
        @NotBlank(message = "ETL状态 ready=可处理，unready=未处理不能为空")
        @Size(max = 192,message = "ETL状态 ready=可处理，unready=未处理最大长度不能超过192位")
        private String etlStatus;

        /**
        * 节点id
        */
        private String nodeId;

        /**
        * 实例id
        */
        private String instanceId;

        /**
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 192,message = "任务id最大长度不能超过192位")
        private String taskId;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 192,message = "来源id最大长度不能超过192位")
        private String sourceId;

        /**
        * 是否最后一次更新记录
        */
        @NotBlank(message = "是否最后一次更新记录不能为空")
        private String lastUpdateFlag;

        /**
        * 核算月份（YYYY-MM）
        */
        @NotBlank(message = "核算月份（YYYY不能为空")
        @Size(max = 96,message = "核算月份（YYYY最大长度不能超过96位")
        private String checkMonth;

        /**
        * 货件ID
        */
        @NotBlank(message = "货件ID不能为空")
        @Size(max = 192,message = "货件ID最大长度不能超过192位")
        private String shipmentId;

        /**
        * 货件单号
        */
        @NotBlank(message = "货件单号不能为空")
        @Size(max = 384,message = "货件单号最大长度不能超过384位")
        private String shipmentCode;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 192,message = "店铺id最大长度不能超过192位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 384,message = "店铺名称最大长度不能超过384位")
        private String shopName;

        /**
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 192,message = "客户id最大长度不能超过192位")
        private String customerId;

        /**
        * 客户姓名
        */
        @NotBlank(message = "客户姓名不能为空")
        @Size(max = 384,message = "客户姓名最大长度不能超过384位")
        private String customerName;

        /**
        * 货件状态
        */
        @NotBlank(message = "货件状态不能为空")
        @Size(max = 192,message = "货件状态最大长度不能超过192位")
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 货件签收时间
        */
        private Date shipmentReceiveTime;

        /**
        * 货件调整时间
        */
        private Date shipmentAdjustTime;

        /**
        * 目的仓库id
        */
        @NotBlank(message = "目的仓库id不能为空")
        @Size(max = 192,message = "目的仓库id最大长度不能超过192位")
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        @NotBlank(message = "目的仓库名称不能为空")
        @Size(max = 384,message = "目的仓库名称最大长度不能超过384位")
        private String warehouseName;

        /**
        * 在途仓库id
        */
        @NotBlank(message = "在途仓库id不能为空")
        @Size(max = 192,message = "在途仓库id最大长度不能超过192位")
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        @NotBlank(message = "在途仓库名称不能为空")
        @Size(max = 384,message = "在途仓库名称最大长度不能超过384位")
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        @NotBlank(message = "平台产品id（ASIN）不能为空")
        @Size(max = 192,message = "平台产品id（ASIN）最大长度不能超过192位")
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        @NotBlank(message = "平台sku（MSKU）/销售平台SKU不能为空")
        @Size(max = 192,message = "平台sku（MSKU）/销售平台SKU最大长度不能超过192位")
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        @NotBlank(message = "FNSKU/平台库存SKU不能为空")
        @Size(max = 192,message = "FNSKU/平台库存SKU最大长度不能超过192位")
        private String platformStockSku;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 192,message = "SKU ID最大长度不能超过192位")
        private String skuId;

        /**
        * 货件明细id
        */
        @NotBlank(message = "货件明细id不能为空")
        @Size(max = 192,message = "货件明细id最大长度不能超过192位")
        private String shipmentDetailId;

        /**
        * 申报数量
        */
        @NotNull(message = "申报数量不能为空")
        private Integer declareQty;

        /**
        * 收货数量
        */
        @NotNull(message = "收货数量不能为空")
        private Integer receiveQty;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 收发差异
        */
        @NotNull(message = "收发差异不能为空")
        private Integer diffQty;

        /**
        * 期初在途数量
        */
        @NotNull(message = "期初在途数量不能为空")
        private Integer initTransitQty;

        /**
        * 本期发货数量
        */
        @NotNull(message = "本期发货数量不能为空")
        private Integer currentDeliveryQty;

        /**
        * 本期签收数量
        */
        @NotNull(message = "本期签收数量不能为空")
        private Integer currentReceiveQty;

        /**
        * 期末在途数量
        */
        @NotNull(message = "期末在途数量不能为空")
        private Integer endPeriodTransitQty;

        /**
        * 期末在途调整数量
        */
        @NotNull(message = "期末在途调整数量不能为空")
        private Integer endPeriodTransitAdjustQty;

        /**
        * 期末在途数量（调整后）
        */
        @NotNull(message = "期末在途数量（调整后）不能为空")
        private Integer afterEndPeriodTransitQty;

        /**
        * 调整原因
        */
        private String adjustReason;

        /**
        * 调整时间
        */
        private Date adjustTime;

        /**
        * 调整人名称
        */
        private String adjustUserName;

        /**
        * 调整人id
        */
        private String adjustUserId;

        /**
        * 备注或附加说明
        */
        private String remark;


    }


}