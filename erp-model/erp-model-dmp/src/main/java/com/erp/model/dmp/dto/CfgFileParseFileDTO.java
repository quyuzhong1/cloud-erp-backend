package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.dmp.enums.CfgFileParseFileTypeEnum;
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
 * 月结文件解析配置文件识别规则请求响应对象。
 *
 * @author jack
 * @since 2026-06-29
 */
@Data
@NoArgsConstructor
public class CfgFileParseFileDTO implements Serializable {

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
         * 单据类型。
         * 枚举下拉：/drop/down/dict/list?key=cfgFileParseFileBusinessType
         */
        private String businessType;
        /**
         * 数据来源。
         * 枚举下拉：CfgFileParseFileTypeEnum，api=API，excel=Excel。
         */
        private String type;
        /**
         * 识别名称，Excel 表格名称或 API 名称。
         */
        private String fileKeyword;
        /**
         * Excel sheet 名称，API 时可为空。
         */
        private String sheetName;
        /**
         * 默认开始行，API 时为 0。
         */
        private Integer headerRow;
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
         * 单据类型。
         * 枚举下拉：/drop/down/dict/list?key=cfgFileParseFileBusinessType
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessType;
        /**
         * 数据来源。
         * 枚举下拉：CfgFileParseFileTypeEnum，api=API，excel=Excel。
         */
        @NotBlank(message = "数据来源不能为空")
        @Size(max = 20, message = "数据来源最大长度不能超过20位")
        private String type;
        /**
         * 识别名称，Excel 表格名称或 API 名称。
         */
        @Size(max = 50, message = "识别名称最大长度不能超过50位")
        private String fileKeyword;
        /**
         * Excel sheet 名称，API 时可为空。
         */
        @Size(max = 50, message = "Excel sheet名称最大长度不能超过50位")
        private String sheetName;
        /**
         * 默认开始行，API 时为 0。
         */
        private Integer headerRow;
        /**
         * 排序号。
         */
        private Integer sort;
    }
}
