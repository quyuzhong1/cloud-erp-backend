package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO.PagingParamDTO;

import java.util.Date;
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
 * 第三方仓流水差异表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-14
*/
@Data
@NoArgsConstructor
public class AdsErpInventoryDiffFlowDTO implements Serializable {



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
      * 详情
      */
      @Data
      @NoArgsConstructor
      public static class TotalDTO {
     	 /**
           * 计算期末库存
           */
           private Integer totalClosingQty;

           /**
           *  实际期末库存
           */
           private Integer totalRealQty;

           /**
           * 差异数量
           */
           private Integer totalDiffQty;
      }
      
      @Data
      @NoArgsConstructor
      @AllArgsConstructor
      public static class ReCreateDTO{
      	/**
           * 核算周期
           */
          @NotBlank(message = "核算周期不能为空")
          private String checkMonth;

          /**
           * 核对仓库
           */
          @NotBlank(message = "核对仓库不能为空")
          private String sourceSystem;
      }
      
      @Data
      @NoArgsConstructor
      @AllArgsConstructor
      public static class UpdateRemarkDTO{
      	/**
           * id
           */
          private String id;
          
          /**
           * 备注
           */
          private String remark;
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
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对仓库
        */
        private String sourceSystemName;

        /**
        * 出库仓库编码
        */
        private String platformWarehouseCode;

        /**
        * 出库仓库名称
        */
        private String platformWarehouseName;

        /**
        * ERP仓库编码
        */
        private String warehouseCode;

        /**
        * ERP仓库名称
        */
        private String warehouseName;

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
        * 上月期末库存
        */
        private Integer initQty;

        /**
        * 签收上架
        */
        private Integer inQty;

        /**
        * 退货上架
        */
        private Integer returnQty;

        /**
        * 订单签出
        */
        private Integer outQty;

        /**
        * 库存调整
        */
        private Integer adjustQty;

        /**
        * 盘点调整
        */
        private Integer checkQty;

        /**
        * 其他动账
        */
        private Integer otherQty;

        /**
        * 库存移动
        */
        private Integer moveQty;

        /**
        * 本月期末库存
        */
        private Integer closingQty;

        /**
        * 即时库存
        */
        private Integer realQty;

        /**
        * 扣减次月数量
        */
        private Integer nextMonthQty;

        /**
        * 实际期末库存
        */
        private Integer estimateClosingQty;

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
        * 数据唯一md5值
        */
        private String uniqueCode;

        /**
        * 数据字段md5值（数据json+account_code+next_level_id+bill_topic）
        */
        private String dataEncrypt;

        /**
        * 来源平台：gyy，kingdee，mabang
        */
        private String sourceSystem;

        /**
        * 数据来源平台
        */
        private String sourcePlatform;

        /**
        * 平台账号编码
        */
        private String accountCode;

        /**
        * 店铺ID/海外仓授权ID
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
        private String taskId;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * ETL处理状态：ready=可处理，unready=不可处理
        */
        private String etlStatus;

        /**
        * 核对唯一键
        */
        private String checkKey;

        /**
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对仓库
        */
        private String sourceSystemName;

        /**
        * 出库仓库编码
        */
        private String platformWarehouseCode;

        /**
        * 出库仓库名称
        */
        private String platformWarehouseName;

        /**
        * ERP仓库编码
        */
        private String warehouseCode;

        /**
        * ERP仓库名称
        */
        private String warehouseName;

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
        * 上月期末库存
        */
        private Integer initQty;

        /**
        * 签收上架
        */
        private Integer inQty;

        /**
        * 退货上架
        */
        private Integer returnQty;

        /**
        * 订单签出
        */
        private Integer outQty;

        /**
        * 库存调整
        */
        private Integer adjustQty;

        /**
        * 库存调整
        */
        private Integer checkQty;

        /**
        * 其他动账
        */
        private Integer otherQty;

        /**
        * 库存移动
        */
        private Integer moveQty;

        /**
        * 本月期末库存
        */
        private Integer closingQty;

        /**
        * 即时库存
        */
        private Integer realQty;

        /**
        * 扣减次月数量
        */
        private Integer nextMonthQty;

        /**
        * 实际期末库存
        */
        private Integer estimateClosingQty;

        /**
        * 差异数量
        */
        private Integer diffQty;

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
        * 核对唯一键
        */
        @NotBlank(message = "核对唯一键不能为空")
        @Size(max = 50000,message = "核对唯一键最大长度不能超过50000位")
        private String checkKey;

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
        * 出库仓库编码
        */
        @NotBlank(message = "出库仓库编码不能为空")
        @Size(max = 50000,message = "出库仓库编码最大长度不能超过50000位")
        private String platformWarehouseCode;

        /**
        * 出库仓库名称
        */
        @NotBlank(message = "出库仓库名称不能为空")
        @Size(max = 50000,message = "出库仓库名称最大长度不能超过50000位")
        private String platformWarehouseName;

        /**
        * ERP仓库编码
        */
        @NotBlank(message = "ERP仓库编码不能为空")
        @Size(max = 50000,message = "ERP仓库编码最大长度不能超过50000位")
        private String warehouseCode;

        /**
        * ERP仓库名称
        */
        @NotBlank(message = "ERP仓库名称不能为空")
        @Size(max = 50000,message = "ERP仓库名称最大长度不能超过50000位")
        private String warehouseName;

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
        * 上月期末库存
        */
        private Integer initQty;

        /**
        * 签收上架
        */
        private Integer inQty;

        /**
        * 退货上架
        */
        private Integer returnQty;

        /**
        * 订单签出
        */
        private Integer outQty;

        /**
        * 库存调整
        */
        private Integer adjustQty;

        /**
        * 库存调整
        */
        private Integer checkQty;

        /**
        * 其他动账
        */
        private Integer otherQty;

        /**
        * 库存移动
        */
        private Integer moveQty;

        /**
        * 本月期末库存
        */
        private Integer closingQty;

        /**
        * 即时库存
        */
        private Integer realQty;

        /**
        * 扣减次月数量
        */
        private Integer nextMonthQty;

        /**
        * 实际期末库存
        */
        private Integer estimateClosingQty;

        /**
        * 差异数量
        */
        private Integer diffQty;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 50000,message = "备注最大长度不能超过50000位")
        private String remark;


    }


}