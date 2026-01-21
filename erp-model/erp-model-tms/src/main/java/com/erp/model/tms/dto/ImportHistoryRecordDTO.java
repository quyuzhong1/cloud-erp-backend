package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
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
 * 物流授权表请求响应实体
 * </p>
 *
 * @author will
 * @since 2026-01-19
*/
@Data
@NoArgsConstructor
public class ImportHistoryRecordDTO implements Serializable {



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

        /**
         * 导入类型，最新导入：new，历史导入：history
         */
        @NotBlank(message = "导入类型不能为空")
        private String importType;
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
        * 批次编号
        */
        private String code;

        /**
        * 对账月份
        */
        private String reconciliationMonth;
        /**
         * 对账月份，YYYY年MM月
         */
        private String reconciliationMonthStr;

        /**
        * 业务类型，自发货费用/尾程费用
        */
        private String businessType;

        /**
        * 上传导入附件的url
        */
        private String fileUrl;

        /**
        * 附件名称
        */
        private String fileName;

        /**
        * 导入数量
        */
        private Integer importCount;

        /**
        * 匹配数量
        */
        private Integer matchCount;

        /**
        * 处理状态
        */
        private String status;


        /**
         * 处理状态名称
         */
        private String statusName;

        /**
        * 类型
        */
        private String type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
        * 操作人id
        */
        private String operationUserId;




        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
         * 下载结果
         */
        private String errorUrl;
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
        * 批次编号
        */
        private String code;

        /**
        * 对账月份
        */
        private String reconciliationMonth;

        /**
        * 业务类型，自发货费用/尾程费用
        */
        private String businessType;

        /**
        * 上传导入附件的url
        */
        private String fileUrl;

        /**
        * 附件名称
        */
        private String fileName;

        /**
        * 导入数量
        */
        private Integer importCount;

        /**
        * 匹配数量
        */
        private Integer matchCount;

        /**
        * 处理状态
        */
        private String status;

        /**
        * 类型
        */
        private String type;

        /**
        * 操作人id
        */
        private String operationUserId;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 对账月份
        */
        @NotBlank(message = "对账月份不能为空")
        @Size(max = 32,message = "对账月份最大长度不能超过32位")
        private String reconciliationMonth;

        /**
        * 业务类型，自发货费用/尾程费用
        */
        @NotBlank(message = "业务类型，自发货费用/尾程费用不能为空")
        @Size(max = 64,message = "业务类型，自发货费用/尾程费用最大长度不能超过64位")
        private String businessType;

        /**
        * 上传导入附件的url
        */
        @NotBlank(message = "上传导入附件的url不能为空")
        @Size(max = 255,message = "上传导入附件的url最大长度不能超过255位")
        private String fileUrl;

        /**
        * 附件名称
        */
        @NotBlank(message = "附件名称不能为空")
        @Size(max = 255,message = "附件名称最大长度不能超过255位")
        private String fileName;

        /**
        * 导入数量
        */
        @NotNull(message = "导入数量不能为空")
        private Integer importCount;

        /**
        * 匹配数量
        */
        @NotNull(message = "匹配数量不能为空")
        private Integer matchCount;

        /**
        * 处理状态
        */
        @NotBlank(message = "处理状态不能为空")
        @Size(max = 32,message = "处理状态最大长度不能超过32位")
        private String status;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 操作人id
        */
        @NotBlank(message = "操作人id不能为空")
        @Size(max = 19,message = "操作人id最大长度不能超过19位")
        private String operationUserId;


    }


}