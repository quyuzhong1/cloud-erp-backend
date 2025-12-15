package com.erp.model.dmp.dto;

import java.util.Date;
import com.common.business.dto.base.SortDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO.PagingParamDTO;

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
 * ERP出库单差异表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-18
*/
@Data
@NoArgsConstructor
public class AdsErpDiffOutstockSyncDTO implements Serializable {



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
         * 类型名称
         */
        private String tabFlagName;

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
           * 平台单据
           */
           private Integer totalPlatformQty;

           /**
           *  ERP单据
           */
           private Integer totalErpQty;

           /**
           * 差异数
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
          private String checkMonth;
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
      
      @Data
      @NoArgsConstructor
      @AllArgsConstructor
      public static class UpdateErpDTO extends PagingParamDTO{
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
         * id
         */
         private String id;

        /**
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对仓库
        */
        private String sourceSystemName;
        
        /**
         * 平台单据名称
         */
         private String platformBillName;

        /**
        * 平台单据状态名称
        */
        private String platformBillStatusName;

        /**
        * 平台ERP店铺
        */
        private String platformShopName;

        /**
        * 平台平台名称
        */
        private String platformSalesPlatformName;

        /**
        * 平台原始单号
        */
        private String platformOrderCode;

        /**
        * 平台销售单号
        */
        private String platformSoCode;

        /**
        * 平台出库单号
        */
        private String platformOutstockCode;

        /**
         * 平台库存SKU数量
         */
         private String stockSkuQty;

        /**
        * 平台ERP_SKU数量
        */
        private String platformSkuQty;

        /**
        * 平台出库仓库名称
        */
        private String platformWarehouseName;

        /**
        * 平台ERP仓库名称
        */
        private String warehouseName;

        /**
        * 平台单据日期
        */
        private Date platformBillDate;

        /**
        * ERP单据名称
        */
        private String billName;

        /**
        * ERP单据状态名称
        */
        private String billStatusName;

        /**
        * ERP店铺
        */
        private String shopName;

        /**
        * ERP平台名称
        */
        private String salesPlatformName;

        /**
        * 原始单号
        */
        private String orderCode;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 出库单号
        */
        private String outstockCode;

        /**
        * ERP_SKU数量
        */
        private String skuQty;

        /**
        * ERP仓库名称
        */
        private String erpWarehouseName;

        /**
        * erp单据日期
        */
        private Date billDate;

        /**
        * 差异数量
        */
        private Integer diffQty;

        /**
        * 差异标签
        */
        private String diffTag;

        /**
        * 差异标签名称
        */
        private String diffTagName;

        /**
        * 差异详情
        */
        private String diffDesc;

        /**
        * 建议处理方式
        */
        private String suggestType;

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
        * 核对周期页面查询
        */
        private String checkMonthQuery;

        /**
        * 核对仓库
        */
        private String sourceSystemName;

        /**
        * 平台单据状态名称
        */
        private String platformBillStatusName;

        /**
        * 平台ERP店铺
        */
        private String platformShopName;

        /**
        * 平台平台名称
        */
        private String platformSalesPlatformName;

        /**
        * 平台原始单号
        */
        private String platformOrderCode;

        /**
        * 平台销售单号
        */
        private String platformSoCode;

        /**
        * 平台出库单号
        */
        private String platformOutstockCode;

        /**
        * 平台库存SKU
        */
        private String stockSku;

        /**
        * 平台库存SKU数量
        */
        private Integer stockQty;

        /**
        * 平台ERP_SKU
        */
        private String platformSkuNo;

        /**
        * 平台ERP_SKU数量
        */
        private Integer platformQty;

        /**
        * 平台出库仓库名称
        */
        private String platformWarehouseName;

        /**
        * 平台ERP仓库名称
        */
        private String warehouseName;

        /**
        * 平台单据日期
        */
        private Date platformBillDate;

        /**
        * ERP单据名称
        */
        private String billName;

        /**
        * ERP单据状态名称
        */
        private String billStatusName;

        /**
        * ERP店铺
        */
        private String shopName;

        /**
        * ERP平台名称
        */
        private String salesPlatformName;

        /**
        * 原始单号
        */
        private String orderCode;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 出库单号
        */
        private String outstockCode;

        /**
        * ERP_SKU
        */
        private String skuNo;

        /**
        * ERP_SKU数量
        */
        private Integer qty;

        /**
        * ERP仓库名称
        */
        private String erpWarehouseName;

        /**
        * erp单据日期
        */
        private Date billDate;

        /**
        * 差异数量
        */
        private Integer diffQty;

        /**
        * 差异标签
        */
        private String diffTag;

        /**
        * 差异标签名称
        */
        private String diffTagName;

        /**
        * 差异详情
        */
        private String diffDesc;

        /**
        * 建议处理方式
        */
        private String suggestType;

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
        private String checkKey;

        /**
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对周期页面查询
        */
        private String checkMonthQuery;

        /**
        * 核对仓库
        */
        @NotBlank(message = "核对仓库不能为空")
        @Size(max = 192,message = "核对仓库最大长度不能超过192位")
        private String sourceSystemName;

        /**
        * 平台单据状态名称
        */
        private String platformBillStatusName;

        /**
        * 平台ERP店铺
        */
        private String platformShopName;

        /**
        * 平台平台名称
        */
        private String platformSalesPlatformName;

        /**
        * 平台原始单号
        */
        private String platformOrderCode;

        /**
        * 平台销售单号
        */
        private String platformSoCode;

        /**
        * 平台出库单号
        */
        private String platformOutstockCode;

        /**
        * 平台库存SKU
        */
        private String stockSku;

        /**
        * 平台库存SKU数量
        */
        @NotNull(message = "平台库存SKU数量不能为空")
        private Integer stockQty;

        /**
        * 平台ERP_SKU
        */
        private String platformSkuNo;

        /**
        * 平台ERP_SKU数量
        */
        @NotNull(message = "平台ERP_SKU数量不能为空")
        private Integer platformQty;

        /**
        * 平台出库仓库名称
        */
        private String platformWarehouseName;

        /**
        * 平台ERP仓库名称
        */
        private String warehouseName;

        /**
        * 平台单据日期
        */
        private Date platformBillDate;

        /**
        * ERP单据名称
        */
        private String billName;

        /**
        * ERP单据状态名称
        */
        private String billStatusName;

        /**
        * ERP店铺
        */
        private String shopName;

        /**
        * ERP平台名称
        */
        private String salesPlatformName;

        /**
        * 原始单号
        */
        private String orderCode;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 出库单号
        */
        private String outstockCode;

        /**
        * ERP_SKU数量
        */
        private Integer qty;

        /**
        * ERP仓库名称
        */
        private String erpWarehouseName;

        /**
        * erp单据日期
        */
        private Date billDate;

        /**
        * 差异数量
        */
        private Integer diffQty;

        /**
        * 差异标签
        */
        private String diffTag;

        /**
        * 差异标签名称
        */
        private String diffTagName;

        /**
        * 差异详情
        */
        private String diffDesc;

        /**
        * 建议处理方式
        */
        private String suggestType;

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


    }


}