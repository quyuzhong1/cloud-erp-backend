package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
        @Size(max = 1,024,message = "对比结果报告下载地址最大长度不能超过1,024位")
        private String resultReportUrl;


    }


}