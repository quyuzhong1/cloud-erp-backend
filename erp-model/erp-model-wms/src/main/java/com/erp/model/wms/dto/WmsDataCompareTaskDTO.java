package com.erp.model.wms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
         * 对比单据类型名称：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收
         */
        private String billTypeName;

        /**
        * 任务状态：init=初始，doing=进行中，finish=已完成，error=异常
        */
        private String status;
        /**
         * 任务状态名称：init=初始，doing=进行中，finish=已完成，error=异常
         */
        private String statusName;

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
        
        /**
         * 创建人名称
         */
        private String createUserName;

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
    public static class AddDTO {
    	/**
         * 任务名称
         */
         @NotBlank(message = "任务名称不能为空")
         @Size(max = 255,message = "任务名称最大长度不能超过255位")
         private String name;
         
         /**
          * 单据类型： 枚举获取文档地址：http://172.16.100.11:3002/project/92/interface/api/9259 type=WmsDataCompareTaskBillType
          */
//          @NotBlank(message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收不能为空")
          @Size(max = 50,message = "单据类型：soOutstock=销售出库单，fbaShipment=FBA货件签收，overseasInbound=第三方仓货件签收最大长度不能超过50位")
          private String billType;
          
          /**
           * 系统数据范围条件json串，字段名称对应属性如下(默认数据类型为字符串)：
           * 销售出库单：销售平台=dictPlatform，店铺=shopId，出库日期=billDateList(时间类型数组)，仓库=warehouseId，示例{'dictPlatform' : 'Amazon' , 'billDateList' : ['2024-01-22' , '2024-03-22']}
           *FBA货件签收：店铺=shopId，出库日期=receiveDateList(时间类型数组)，示例{'shopId' : '1647810847827268690' , 'receiveDateList' : ['2024-01-22' , '2024-03-22']}
           *第三方仓货件签收：出库日期=receiveTimeList(时间类型数组)，目的仓库=toWarehouseId，示例{'receiveDateList' : ['2024-01-22' , '2024-03-22'] , 'toWarehouseId' : '1647810847827268690' }
           */
           @NotBlank(message = "系统数据范围条件json串不能为空")
           private String systemDataCondition;
         
    	/**
    	 * 导入文件
         */
    	@NotNull(message = "导入文件不能为空")
        private List<@NotBlank(message = "对比数据导入文件为空")String> excelFiles;
    	
    	/**
    	 *对比类型：pk=主键对比，group=分组对比  枚举获取文档地址：http://172.16.100.11:3002/project/92/interface/api/9259 type=WmsDataCompareType
    	 */
    	@NotBlank(message = "对比类型不能为空")
    	private String compareType;
    	
    	/**
    	 * 主数据excel文件
    	 */
    	private List<String> mainExcelFiles;
    }

    /**
     * 详情
     */
     @Data
     @NoArgsConstructor
     public static class AddViewDTO {

         /**
         * 主键id
         */
         private String  id;

         /**
          * 系统数据总行数
          */
          private Integer systemDataCount;

          /**
          * 导入数据总行数
          */
          private Integer importDataCount;
          
          /**
           * 导入数据下载地址
           */
          private List<String> importFileUrls;
          
          /**
           * 单据类型：保存映射模板使用
           */
           private String billType;
           
           /**
            * 对比设置字段映射属性
            */
           private List<DataCompareSettingDTO> settingList;

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
     * 下一步
     */
     @Data
     @NoArgsConstructor
     public static class SetNextDTO {
    	 /**
          * 主键id
          */
          @NotBlank(message = "主键id不能为空")
          private String id;
          
          /**
           * 导入数据字段映射json串
           * 系统数据字段=systemField，导入数据字段=importField，唯一键标识=status（布尔数据类型true或false），数据类型=classType，示例：[{'systemField' : 'soCode' , 'importField' : '销售单号', 'status' : true , 'classType' : 'string'} , {'systemField' : 'dictPlatform' , 'importField' : '销售平台', 'status' : false , 'classType' : 'int'} ]
           * 系统数据字段名称显示及systemField提交值获取方式取dict配置，code是systemField提交值，name名称显示。 http://172.16.100.11:3002/project/92/interface/api/13147 入参type:销售出库单=datacompare_soOutstock,FBA货件签收=datacompare_fbaShipment,第三方仓货件签收=datacompare_overseasInbound
           */
           @NotBlank(message = "导入数据字段映射json串不能为空")
           private String importDataMapping;
     }
     
     /**
      * 下一步
      */
      @Data
      @NoArgsConstructor
      public static class SetNextViewDTO {
    	  /**
    	 * 校验是否成功，true为成功，false为失败
    	 */
    	private Boolean flag = false;
    	  /**
           * 任务编号
           */
           private String code;

           /**
           * 任务名称
           */
           private String name;
           
           /**
            * 错误信息列表
            */
            private List<String> errMessageList;
      }
      
      /**
       * 创建
       */
       @Data
       @NoArgsConstructor
       public static class CreateViewDTO {
     	  
     	  /**
            * 任务编号
            */
            private String code;

            /**
            * 任务名称
            */
            private String name;
           
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
    
    @Data
    @NoArgsConstructor
    public static class DataCompareSettingDTO{
    	/**
         * 系统数据字段名称
         */
    	private String sysField;
    	
    	/**
    	 * 系统数据字段字段属性代码
    	 */
    	private String sysFieldName;
    	
    	/**
         * 系统数据字段排序
         */
    	private Integer sysFieldIndex;
    	
    	/**
    	 * 默认导入字段
    	 */
    	private String importField;
    	
    	/**
    	 * 导入数据字段下拉
    	 */
    	private List<DataCompareSettingImprotDTO> importFields;
    	
    	/**
    	 * 默认数据类型
    	 */
    	private String classType;
    	
    	/**
    	 * 数据类型下拉
    	 */
    	private List<DataCompareSettingTypeDTO> dataTypes;
    	
    	/**
    	 * 是否主键或汇总字段
    	 */
    	private Boolean status = false;
    }
    
    @Data
    @NoArgsConstructor
    public static class DataCompareSettingMapDTO{
    	/**
         * 系统数据字段
         */
    	private String sysFieldName;
    	
    	/**
         * 系统数据字段排序
         */
    	private Integer sysFieldIndex;
    	
    	/**
    	 * 导入数据字段map
    	 */
    	private Map<String , DataCompareSettingImprotDTO> importFieldMap;
    	
    }
    
    @Data
    @NoArgsConstructor
    public static class DataCompareSettingImprotDTO{
    	/**
         * 导入数据字段
         */
    	private String importField;
    	
    	/**
         * 导入数据字段排序
         */
    	private Integer importFieldIndex;
    	
    }
    
    @Data
    @NoArgsConstructor
    public static class DataCompareSettingTypeDTO{
    	/**
         * 导入数据数据类型代码
         */
    	private String typeCode;
    	
    	/**
         * 导入数据数据类型名称
         */
    	private String typeName;
    	
    }

    @Data
    @NoArgsConstructor
    public static class DataCompareDTO{
    	/**
         * 数据库数据id
         */
    	@ExcelIgnore
    	private String id;
    	
    	/**
         * 任务id
         */
    	@ExcelIgnore
    	private String taskId;
    }
    
    /**
     * 销售出库单系统比对字段
     */
     @Data
     @NoArgsConstructor
     public static class SoOutstockDTO extends DataCompareDTO{
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
    	 private String actualQty;
    	 
    	 /**
    	  * 出库日期
    	  */
    	 @ExcelProperty(value = "出库日期", index = 7)
    	 private String billDate;
    	 
    	 /**
    	 * 店铺id查询条件
    	 */
    	 @ExcelIgnore
    	private String shopId;
    	
    	/**
    	 * 仓库id查询条件
    	 */
    	 @ExcelIgnore
    	private String warehouseId;
    	 
    	 /**
    	 * 出库日期查询条件
    	 */
    	 @ExcelIgnore
    	private List<LocalDateTime> billDateList;
    	
    	/**
    	 *  销售单号查询条件
    	 */
    	 @ExcelIgnore
    	private List<String> customerIdList;
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
     	 private String receiveQty;
     	
     	/**
    	 * 店铺id查询条件
    	 */
     	@ExcelIgnore
    	private String shopId;
     	 
     	 /**
     	  * 签收日期
     	  */
     	@ExcelProperty(value = "签收日期", index = 5)
     	 private String receiveDate;
     	 
     	/**
     	  * 签收日期查询条件
     	  */
     	@ExcelIgnore
     	 private List<LocalDateTime> receiveDateList;
     	 
      }

      /**
       * FBA货件签收系统比对字段
       */
       @Data
       @NoArgsConstructor
       public static class OverseasInboundDTO extends DataCompareDTO{
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
      	 private String receiveQty;
      	 
      	 /**
      	  * 出库日期
      	  */
      	@ExcelProperty(value = "出库日期", index = 5)
      	 private String receiveTime;
      	 
      	/**
     	  * 仓库id查询条件
     	  */
      	@ExcelIgnore
     	 private String toWarehouseId;
      	
      	/**
      	  * 出库日期查询条件
      	  */
      	@ExcelIgnore
      	private List<LocalDateTime> receiveDateList;
      	 
       }
       
       /**·
        * 分页参数
        */
       @Data
       @NoArgsConstructor
       public static class PagingParamDTO extends SortDTO {

           /**
            * 页面高级查询，表别名 t
            */
           private List<AdvanceQueryDTO> advanceQueryDTOList;

           /**
            * sqlMap 默认key default
            */
           private Map<String,String> sqlMap;


       }
}