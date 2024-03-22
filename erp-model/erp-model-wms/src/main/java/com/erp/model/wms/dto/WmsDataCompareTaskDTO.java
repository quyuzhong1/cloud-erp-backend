package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;

/**
 * <p>
 * 数据对比任务请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@NoArgsConstructor
public class WmsDataCompareTaskDTO implements Serializable {




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
        * 任务编号
        */
        private String code;

        /**
        * 任务名称
        */
        private String name;

        /**
        * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收
        */
        private String billType;

        /**
        * 任务状态：init=初始，doing=进行中，finish=已完成，error=异常
        */
        private String status;

        /**
        * 子任务状态：wait_parse=待解析，wait_compare=待比对，wait_upload=待上传，finish=已完成，error=异常
        */
        private String subStatus;

        /**
        * 异常原因
        */
        private String errorMessage;

        /**
        * 失败次数，超过3次告警
        */
        private Integer errorCount;

        /**
        * 系统数据范围条件json串
        */
        private String systemDataCondition;

        /**
        * 系统数据总行数
        */
        private Integer systemDataCount;

        /**
        * 导入数据总行数
        */
        private Integer importDataCount;

        /**
        * 导入数据字段映射json串
        */
        private String importDataMapping;

        /**
        * 对比结果-完全一致
        */
        private Integer resultSameCount;

        /**
        * 对比结果-系统多单
        */
        private Integer resultExceedCount;

        /**
        * 对比结果-系统漏单
        */
        private Integer resultMissCount;

        /**
        * 对比结果-差异条数
        */
        private Integer resultDiffCount;

        /**
        * 对比结果报告下载地址
        */
        private String resultReportUrl;

        /**
         * 导入数据字段
         */
        private List<String> importDataFields;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
    	/**
    	 * 导入文件
         */
    	@NotNull(message = "导入文件不能为空")
        private List<String> excelFiles;
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
    
    /**
     * 新增
     */
     @Data
     @NoArgsConstructor
     public static class SetNextDTO extends CommonDTO {
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
        * 任务名称
        */
        @NotBlank(message = "任务名称不能为空")
        @Size(max = 255,message = "任务名称最大长度不能超过255位")
        private String name;

        /**
        * 单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收
        */
        @NotBlank(message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收不能为空")
        @Size(max = 50,message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收最大长度不能超过50位")
        private String billType;

        /**
        * 任务状态：init=初始，doing=进行中，finish=已完成，error=异常
        */
        @NotBlank(message = "任务状态：init=初始，doing=进行中，finish=已完成，error=异常不能为空")
        @Size(max = 19,message = "任务状态：init=初始，doing=进行中，finish=已完成，error=异常最大长度不能超过19位")
        private String status;

        /**
        * 子任务状态：wait_parse=待解析，wait_compare=待比对，wait_upload=待上传，finish=已完成，error=异常
        */
        @NotBlank(message = "子任务状态：wait_parse=待解析，wait_compare=待比对，wait_upload=待上传，finish=已完成，error=异常不能为空")
        @Size(max = 19,message = "子任务状态：wait_parse=待解析，wait_compare=待比对，wait_upload=待上传，finish=已完成，error=异常最大长度不能超过19位")
        private String subStatus;

        /**
        * 异常原因
        */
        @NotBlank(message = "异常原因不能为空")
        private String errorMessage;

        /**
        * 失败次数，超过3次告警
        */
        @NotNull(message = "失败次数，超过3次告警不能为空")
        private Integer errorCount;

        /**
        * 系统数据范围条件json串
        */
        @NotBlank(message = "系统数据范围条件json串不能为空")
        private String systemDataCondition;

        /**
        * 系统数据总行数
        */
        @NotNull(message = "系统数据总行数不能为空")
        private Integer systemDataCount;

        /**
        * 导入数据总行数
        */
        @NotNull(message = "导入数据总行数不能为空")
        private Integer importDataCount;

        /**
        * 导入数据字段映射json串
        */
        @NotBlank(message = "导入数据字段映射json串不能为空")
        private String importDataMapping;

        /**
        * 对比结果-完全一致
        */
        @NotNull(message = "对比结果不能为空")
        private Integer resultSameCount;

        /**
        * 对比结果-系统多单
        */
        @NotNull(message = "对比结果不能为空")
        private Integer resultExceedCount;

        /**
        * 对比结果-系统漏单
        */
        @NotNull(message = "对比结果不能为空")
        private Integer resultMissCount;

        /**
        * 对比结果-差异条数
        */
        @NotNull(message = "对比结果不能为空")
        private Integer resultDiffCount;

        /**
        * 对比结果报告下载地址
        */
        @NotBlank(message = "对比结果报告下载地址不能为空")
        @Size(max = 1024,message = "对比结果报告下载地址最大长度不能超过1,024位")
        private String resultReportUrl;


    }

    public static class DataCompareDTO{
    	
    }
    
    /**
     * 销售出库单系统比对字段
     */
     @Data
     @NoArgsConstructor
     public static class SoOutstockDTO extends DataCompareDTO{
    	 /**
          * 销售出库单id
          */
    	 private String id;
    	 /**
          * 销售单号
          */
    	 @ExcelProperty(value = "销售单号", index = 0)
    	 private String soCode;
    	 
    	 /**
          * 平台
          */
    	 @ExcelProperty(value = "平台", index = 1)
    	 private String dictPlatform;
    	 
    	 /**
    	  * 店铺
    	  */
    	 @ExcelProperty(value = "店铺", index = 2)
    	 private String shopName;
    	 
    	 /**
    	  * 系统SKU
    	  */
    	 @ExcelProperty(value = "系统SKU", index = 3)
    	 private String skuNo;
    	 
    	 /**
    	  * 平台SKU
    	  */
    	 @ExcelProperty(value = "平台SKU", index = 4)
    	 private String platformSkuNo;
    	 
    	 /**
    	  * 仓库
    	  */
    	 @ExcelProperty(value = "仓库", index = 5)
    	 private String warehouseName;
    	 
    	 /**
    	  * 数量
    	  */
    	 @ExcelProperty(value = "数量", index = 6)
    	 private Integer actualQty;
    	 
    	 /**
    	  * 出库日期
    	  */
    	 @ExcelProperty(value = "出库日期", index = 7)
    	 private String billDate;
    	 
    	 /**
    	 * 出库日期查询条件
    	 */
    	private List<LocalDate> billDateList;
    	
    	/**
    	 *  销售单号查询条件
    	 */
    	private List<String> soCodeList;
     }
     
     /**
      * 销售出库单系统比对字段
      */
      @Data
      @NoArgsConstructor
      public static class SoB2cDTO {
     	 /**
           * 销售单号
           */
     	 private String code;
     	 
     	 /**
           * 平台
           */
     	 private String dictPlatform;
     	 
     	 /**
     	  * 店铺
     	  */
     	 private String shopName;
     	 
     	 /**
     	  * 系统SKU
     	  */
     	 private String skuNo;
     	 
     	 /**
     	  * 平台SKU
     	  */
     	 private String platformSkuNo;
     	 
      }

     /**
      * FBA货件签收系统比对字段
      */
      @Data
      @NoArgsConstructor
      public static class FbaShipmentDTO  extends DataCompareDTO{
    	  /**
           * 销售出库单id
           */
     	 private String id;
     	 /**
           * 货件单号
           */
     	@ExcelProperty(value = "货件单号", index = 0)
     	 private String code;
     	 
     	 /**
     	  * 店铺
     	  */
     	@ExcelProperty(value = "店铺", index = 1)
     	 private String shopName;
     	 
     	 /**
     	  * 系统SKU
     	  */
     	@ExcelProperty(value = "系统SKU", index = 2)
     	 private String skuNo;
     	 
     	 /**
     	  * 平台SKU
     	  */
     	@ExcelProperty(value = "平台SKU", index = 3)
     	 private String msku;
     	 
     	 /**
     	  * 数量
     	  */
     	@ExcelProperty(value = "数量", index = 4)
     	 private Integer receiveQty;
     	 
     	 /**
     	  * 数量
     	  */
     	@ExcelProperty(value = "数量", index = 5)
     	 private String receiveDate;
     	 
     	/**
     	  * 签收日期查询条件
     	  */
     	 private List<LocalDate> receiveDateList;
     	 
      }

      /**
       * FBA货件签收系统比对字段
       */
       @Data
       @NoArgsConstructor
       public static class OverseasInboundDTO extends DataCompareDTO{
    	   /**
            * 销售出库单id
            */
      	 private String id;
      	 /**
          * 第三方仓入库单号
         */
      	@ExcelProperty(value = "第三方仓入库单号", index = 0)
      	 private String sourceCode;
      	 
      	 /**
      	  * 系统SKU
      	  */
      	@ExcelProperty(value = "系统SKU", index = 1)
      	 private String skuNo;
      	 
      	 /**
      	  * 第三方仓SKU
      	  */
      	@ExcelProperty(value = "第三方仓SKU", index = 2)
      	 private String platformSkuNo;
      	 
      	 /**
      	  * 仓库
      	  */
      	@ExcelProperty(value = "仓库", index = 3)
      	 private String toWarehouseName;
      	 
      	 /**
      	  * 数量
      	  */
      	@ExcelProperty(value = "数量", index = 4)
      	 private Integer receiveQty;
      	 
      	 /**
      	  * 出库日期
      	  */
      	@ExcelProperty(value = "出库日期", index = 5)
      	 private String receiveTime;
      	 
      	/**
      	  * 出库日期查询条件
      	  */
      	private List<LocalDate> receiveTimeList;
      	 
       }
       
       /**·
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


       }
}