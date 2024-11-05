package com.erp.model.mrp.dto;

import com.common.business.annotation.Dict;
import com.common.core.dto.FileExcelDTO;
import com.erp.model.mrp.enums.HistoryImportRecordTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 历史导入记录请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class HistoryImportRecordDTO implements Serializable {


    /**
     * 列表查询数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 类型,枚举HistoryImportRecordTypeEnum
         */
        @Dict(enumClass = HistoryImportRecordTypeEnum.class)
        private String type;

        /**
         * 文件url
         */
        private String fileUrl;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private String createTime;
    }


    /**
     * 列表查询参数数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO {
        /**
         * 业务id
         */
        private String businessId;

        /**
         * 类型,枚举HistoryImportRecordTypeEnum,cfgRuleReplenishment补货规则,salesEstimateManual运营月销预估，
         * deliverySuggestionConfirm发货备货确认表,purchaseSuggestionConfirm采购备货确认表,purchaseSuggestionConfirmMerge采购备货（合并）确认表
         */
        private String type;

        /**
         * 模块类型，取sourceType,replenishmentSuggestion补货建议,deliverySuggestion发货建议,purchaseSuggestion采购建议
         */
        private String module;

        /**
         * 平台
         */
        private String platformType;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 业务id
         */
        @Size(max = 19,message = "业务id最大长度不能超过19位")
        private String businessId;

        /**
        * 类型,HistoryImportRecordTypeEnum枚举
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 模块，SourceTypeEnum枚举
        */
        @NotBlank(message = "模块，SourceTypeEnum枚举不能为空")
        @Size(max = 64,message = "模块，SourceTypeEnum枚举最大长度不能超过64位")
        private String module;

        /**
         * 文件名称
         */
        @NotBlank(message = "文件名称不能为空")
        @Size(max = 32,message = "文件名称最大长度不能超过32位")
        private String name;

        /**
         * 平台类型
         */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 32,message = "平台类型最大长度不能超过32位")
        private String platformType;

        /**
         * 导入数据
         */
        @NotNull(message = "导入数据不能为空")
        private  FileExcelDTO.ExportFileDTO exportFileDTO;
    }


}