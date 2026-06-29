package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.tms.enums.CfgFileParseFolderTypeEnum;
import com.erp.model.tms.enums.CfgFileParsePeriodTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 月结文件解析配置请求响应对象。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@NoArgsConstructor
public class CfgFileParseDTO implements Serializable {

    /**
     * 状态统计。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 页签标识。
         */
        private String tabFlag;
        /**
         * 页签标识。
         */
        private String tabFlagName;
        /**
         * 数量。
         */
        private Integer count;
    }

    /**
     * 分页列表查询参数。
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询。
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        /**
         * 高级查询生成的 SQL 片段，默认 key 为 default。
         */
        private Map<String, String> sqlMap;

        private List<String> ids;
    }

    /**
     * 分页列表。
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键 ID。【可排序】
         */
        private String id;
        /**
         * 配置编码。【可排序】
         */
        private String code;
        /**
         * 任务名称。【可排序】
         */
        private String name;
        /**
         * 清洗时间维度，monthly=每月。【可排序】
         */
        private String periodType;
        /**
         * 清洗时间维度名称。
         */
        private String periodTypeName;
        /**
         * 清洗仓库编码。【可排序】
         */
        private String dictPlatform;
        /**
         * 清洗仓库名称。【可排序】
         */
        private String dictPlatformName;
        /**
         * 文件夹类型。【可排序】
         */
        private String folderType;
        /**
         * 文件夹类型名称。
         */
        private String folderTypeName;
        /**
         * 单据类型。
         */
        private String businessType;
        /**
         * 单据类型名称
         */
        private String businessTypeName;
        /**
         * 数据来源。【可排序】
         */
        private String type;
        /**
         * 数据来源摘要。
         */
        private String typeName;
        /**
         * 识别名称。【可排序】
         */
        private String fileKeyword;
        /**
         * 启用状态名称。
         */
        private String disabledName;
        /**
         * 是否停用，false=启用，true=停用。【可排序】
         */
        private Boolean disabled;
        /**
         * 备注。【可排序】
         */
        private String remark;
        /**
         * 创建时间。【可排序】
         */
        private LocalDateTime createTime;
        /**
         * 创建人名称id
         */
        private String createUserId;
        /**
         * 创建人名称。【可排序】
         */
        private String createUserName;
        /**
         * 更新人名称id
         */
        private String updateUserId;
        /**
         * 更新人名称。【可排序】
         */
        private String updateUserName;
        /**
         * 更新时间。【可排序】
         */
        private LocalDateTime updateTime;
    }

    /**
     * 导出 Excel 参数。
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的 ID 集合。
         */
        private List<String> ids;
    }

    /**
     * 详情。
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * 主键 ID。
         */
        private String id;
        /**
         * 配置编码。
         */
        private String code;
        /**
         * 任务名称。
         */
        private String name;
        /**
         * 清洗时间维度，monthly=每月。
         */
        private String periodType;
        /**
         * 清洗时间维度名称。
         */
        private String periodTypeName;
        /**
         * 清洗仓库编码，取自 /dmp/dmpBasicSystem/listDmpBasicSystem 的 code。
         */
        private String dictPlatform;
        /**
         * 清洗仓库名称。
         */
        private String dictPlatformName;
        /**
         * 文件夹类型。
         */
        private String folderType;
        /**
         * 文件夹类型名称。
         */
        private String folderTypeName;
        /**
         * 是否停用，false=启用，true=停用。
         */
        private Boolean disabled;
        /**
         * 备注。
         */
        private String remark;
        /**
         * 配置文件夹。
         */
        private List<CfgFileParseFolderDTO.UpdateDTO> folderList;
        /**
         * 文件清洗规则。
         */
        private List<CfgFileParseFileDTO.UpdateDTO> fileList;
    }

    /**
     * 新增。
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
    }

    /**
     * 修改。
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        /**
         * 主键 ID。
         */
        @NotBlank(message = "主键ID不能为空")
        private String id;
    }

    /**
     * 批量更新启用状态。
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDisabledDTO extends SuperDTO {
        /**
         * 配置 ID 集合。
         */
        @NotEmpty(message = "配置ID不能为空")
        private List<String> ids;
        /**
         * 是否停用，false=启用，true=停用。
         */
        @NotNull(message = "启用状态不能为空")
        private Boolean disabled;
    }

    /**
     * 新增和修改公共字段。
     */
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        /**
         * 任务名称。
         */
        @NotBlank(message = "任务名称不能为空")
        @Size(max = 50, message = "任务名称最大长度不能超过50位")
        private String name;
        /**
         * 清洗时间维度。
         * 枚举下拉：CfgFileParsePeriodTypeEnum，当前仅支持 monthly=每月，默认 monthly。
         */
        @StateEnumValue(clazz = CfgFileParsePeriodTypeEnum.class, message = "清洗时间仅支持每月")
        @Size(max = 20, message = "清洗时间最大长度不能超过20位")
        private String periodType;
        /**
         * 清洗仓库编码。
         * 下拉接口：/dmp/dmpBasicSystem/listDmpBasicSystem，提交 code。
         */
        @NotBlank(message = "清洗仓库编码不能为空")
        @Size(max = 32, message = "清洗仓库编码最大长度不能超过32位")
        private String dictPlatform;
        /**
         * 清洗仓库名称。
         */
        private String dictPlatformName;
        /**
         * 文件夹类型。
         * 枚举下拉：CfgFileParseFolderTypeEnum，platformThirdWarehouse=年月 > 平台 + 三方仓账号，platformShop=年月 > 平台 + 店铺。
         */
        @NotBlank(message = "文件夹类型不能为空")
        @StateEnumValue(clazz = CfgFileParseFolderTypeEnum.class, message = "文件夹类型录入有误")
        @Size(max = 50, message = "文件夹类型最大长度不能超过50位")
        private String folderType;
        /**
         * 是否停用，false=启用，true=停用。
         */
        private Boolean disabled;
        /**
         * 备注。
         */
        private String remark;
        /**
         * 配置文件夹。
         */
        @NotEmpty(message = "配置文件夹不能为空")
        private List<CfgFileParseFolderDTO. @Valid UpdateDTO> folderList;
        /**
         * 文件清洗规则。
         */
        @NotEmpty(message = "文件清洗规则不能为空")
        private List<CfgFileParseFileDTO.@Valid UpdateDTO> fileList;
    }
}
