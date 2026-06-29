package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.tms.enums.CfgFileParseFolderAccountTypeEnum;
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
 * 月结文件解析配置文件夹映射请求响应对象。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@NoArgsConstructor
public class CfgFileParseFolderDTO implements Serializable {

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
    }

    /**
     * 分页列表。
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键 ID。
         */
        private String id;
        /**
         * 月结文件解析配置主表 ID。
         */
        private String mainId;
        /**
         * 账号类型。
         * 枚举下拉：CfgFileParseFolderAccountTypeEnum，thirdWarehouse=三方仓账号，shop=店铺。
         */
        private String accountType;
        /**
         * 账号或店铺 ID。
         * 联动下拉：accountType=thirdWarehouse 时选择三方仓账号，accountType=shop 时选择店铺；提交所选账号或店铺的 ID。
         */
        private String accountId;
        /**
         * 账号或店铺编码。
         */
        private String accountCode;
        /**
         * 账号或店铺名称。
         */
        private String accountName;
        /**
         * 排序号。
         */
        private Integer sort;
        /**
         * 创建时间。
         */
        private LocalDateTime createTime;
        /**
         * 创建人名称。
         */
        private String createUserName;
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
    public static class ViewDTO extends CommonDTO {
        /**
         * 主键 ID。
         */
        private String id;
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
         * 主键 ID。聚合保存时允许为空，由后端重建子表数据。
         */
        private String id;
    }

    /**
     * 新增和修改公共字段。
     */
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        /**
         * 月结文件解析配置主表 ID。
         */
        @Size(max = 19, message = "月结文件解析配置主表ID最大长度不能超过19位")
        private String mainId;
        /**
         * 账号类型。
         * 枚举下拉：CfgFileParseFolderAccountTypeEnum，thirdWarehouse=三方仓账号，shop=店铺。
         */
        @NotBlank(message = "账号类型不能为空")
        @StateEnumValue(clazz = CfgFileParseFolderAccountTypeEnum.class, message = "账号类型录入有误")
        @Size(max = 20, message = "账号类型最大长度不能超过20位")
        private String accountType;
        /**
         * 账号或店铺 ID。
         * 联动下拉：accountType=thirdWarehouse 时选择三方仓账号，accountType=shop 时选择店铺；提交所选账号或店铺的 ID。
         */
        @NotBlank(message = "账号或店铺ID不能为空")
        @Size(max = 19, message = "账号或店铺ID最大长度不能超过19位")
        private String accountId;
        /**
         * 账号或店铺编码。
         */
        private String accountCode;
        /**
         * 账号或店铺名称。
         */
        private String accountName;
        /**
         * 排序号。
         */
        @NotNull(message = "排序号不能为空")
        private Integer sort;
    }
}
