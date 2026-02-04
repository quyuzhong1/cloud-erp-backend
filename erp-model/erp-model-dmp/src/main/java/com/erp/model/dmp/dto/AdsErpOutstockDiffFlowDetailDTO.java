package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 第三方仓出库单据差异表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-12
*/
@Data
@NoArgsConstructor
public class AdsErpOutstockDiffFlowDetailDTO implements Serializable {


	/**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpotParamDTO extends PagingParamDTO{
    	/**
         * 主键id
         */
        private List<String> ids;
    }

    /**
     * 详情
     */
     @Data
     @NoArgsConstructor
     public static class SourceSelfDTO {

         /**
         * 主键id
         */
         private String  id;

         /**
         * 核对周期
         */
         private String checkMonth;

         /**
         * 核对仓库
         */
         private String sourceSystemName;

         /**
         * 出库单号
         */
         private String outstockCode;

         /**
         * ERP下单单号
         */
         private String soDeliveryCode;

         /**
         * ERP销售单号
         */
         private String soCode;

         /**
         * 平台原始订单号
         */
         private String platformOrderCode;

         /**
         * 出库仓库
         */
         private String platformWarehouse;

         /**
         * 单据日期
         */
         private String platformBillDate;

         /**
         * 单据状态
         */
         private String platformBillStatus;

         /**
         * 库存SKU
         */
         private String stockSku;

         /**
         * ERP_SKU_ID
         */
         private String skuId;

         /**
         * ERP_SKU
         */
         private String skuNo;

         /**
         * 出库数量
         */
         private Integer outstockQty;

         /**
         * 流水扣减数量
         */
         private Integer flowDeductQty;

         /**
         * 差异数量
         */
         private Integer diffQty;

         /**
         * 备注
         */
         private String remark;
         
         /**
          * 执行状态
          */
          private String execStatus;

          /**
          * 执行状态名称
          */
          private String execStatusName;

          /**
          * 执行完成时间
          */
          private Date finishTime;

         /**
          * 创建时间
          */
         private LocalDateTime createTime; 
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
        * 数据唯一md5值
        */
        @NotBlank(message = "数据唯一md5值不能为空")
        @Size(max = 192,message = "数据唯一md5值最大长度不能超过192位")
        private String uniqueCode;

        /**
        * 数据字段md5值（数据json+account_code+next_level_id+bill_topic）
        */
        @NotBlank(message = "数据字段md5值（数据json+account_code+next_level_id+bill_topic）不能为空")
        @Size(max = 192,message = "数据字段md5值（数据json+account_code+next_level_id+bill_topic）最大长度不能超过192位")
        private String dataEncrypt;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        @NotBlank(message = "来源平台：gyy，kingdee，mabang不能为空")
        @Size(max = 192,message = "来源平台：gyy，kingdee，mabang最大长度不能超过192位")
        private String sourceSystem;

        /**
        * 数据来源平台
        */
        @NotBlank(message = "数据来源平台不能为空")
        @Size(max = 765,message = "数据来源平台最大长度不能超过765位")
        private String sourcePlatform;

        /**
        * 平台账号编码
        */
        @NotBlank(message = "平台账号编码不能为空")
        @Size(max = 192,message = "平台账号编码最大长度不能超过192位")
        private String accountCode;

        /**
        * 店铺ID/海外仓授权ID
        */
        @NotBlank(message = "店铺ID/海外仓授权ID不能为空")
        @Size(max = 192,message = "店铺ID/海外仓授权ID最大长度不能超过192位")
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
        * 节点id
        */
        private String nodeId;

        /**
        * 实例id/etl任务id
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
        * ETL处理状态：ready=可处理，unready=不可处理
        */
        @NotBlank(message = "ETL处理状态：ready=可处理，unready=不可处理不能为空")
        @Size(max = 192,message = "ETL处理状态：ready=可处理，unready=不可处理最大长度不能超过192位")
        private String etlStatus;

        /**
        * 核对周期
        */
        @NotBlank(message = "核对周期不能为空")
        @Size(max = 50000,message = "核对周期最大长度不能超过50000位")
        private String checkMonth;

        /**
        * 核对仓库
        */
        @NotBlank(message = "核对仓库不能为空")
        @Size(max = 192,message = "核对仓库最大长度不能超过192位")
        private String sourceSystemName;

        /**
        * 出库单号
        */
        @NotBlank(message = "出库单号不能为空")
        @Size(max = 50000,message = "出库单号最大长度不能超过50000位")
        private String outstockCode;

        /**
        * ERP下单单号
        */
        @NotBlank(message = "ERP下单单号不能为空")
        @Size(max = 50000,message = "ERP下单单号最大长度不能超过50000位")
        private String soDeliveryCode;

        /**
        * ERP销售单号
        */
        @NotBlank(message = "ERP销售单号不能为空")
        @Size(max = 50000,message = "ERP销售单号最大长度不能超过50000位")
        private String soCode;

        /**
        * 平台原始订单号
        */
        @NotBlank(message = "平台原始订单号不能为空")
        @Size(max = 50000,message = "平台原始订单号最大长度不能超过50000位")
        private String platformOrderCode;

        /**
        * 出库仓库
        */
        @NotBlank(message = "出库仓库不能为空")
        @Size(max = 50000,message = "出库仓库最大长度不能超过50000位")
        private String platformWarehouse;

        /**
        * 单据日期
        */
        @NotBlank(message = "单据日期不能为空")
        @Size(max = 50000,message = "单据日期最大长度不能超过50000位")
        private String platformBillDate;

        /**
        * 单据状态
        */
        @NotBlank(message = "单据状态不能为空")
        @Size(max = 50000,message = "单据状态最大长度不能超过50000位")
        private String platformBillStatus;

        /**
        * 库存SKU
        */
        @NotBlank(message = "库存SKU不能为空")
        @Size(max = 50000,message = "库存SKU最大长度不能超过50000位")
        private String stockSku;

        /**
        * ERP_SKU_ID
        */
        @NotBlank(message = "ERP_SKU_ID不能为空")
        @Size(max = 50000,message = "ERP_SKU_ID最大长度不能超过50000位")
        private String skuId;

        /**
        * 出库数量
        */
        @NotNull(message = "出库数量不能为空")
        private Integer outstockQty;

        /**
        * 流水扣减数量
        */
        @NotNull(message = "流水扣减数量不能为空")
        private Integer flowDeductQty;

        /**
        * 差异数量
        */
        @NotNull(message = "差异数量不能为空")
        private Integer diffQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 50000,message = "备注最大长度不能超过50000位")
        private String remark;


    }


}