package com.erp.model.mrp.dto;

import com.common.business.annotation.Dict;
import com.erp.model.mrp.enums.HistoryImportRecordTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

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
         * 类型,枚举HistoryImportRecordTypeEnum,cfgRuleReplenishment补货规则,salesEstimateManual运营月销预估
         */
        private String type;

        /**
         * 模块类型，取sourceType
         */
        private String module;
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
        * 类型
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
         * 导入附件
         */
        @NotNull(message = "导入附件不能为空")
        private List<Object> importList;
    }


}