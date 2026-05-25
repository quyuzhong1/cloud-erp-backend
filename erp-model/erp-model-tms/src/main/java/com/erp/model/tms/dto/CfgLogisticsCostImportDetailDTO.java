package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>
 * 费用项配置字段配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@NoArgsConstructor
public class CfgLogisticsCostImportDetailDTO implements Serializable {


    /**
     * 字段清洗规则（扁平结构，无 params 嵌套）。
     * <p>
     * API 使用 {@code etlRuleList} 数组传递；勿传 {@code etlRuleListStorage}。
     * 入参可不传 {@code index}，保存时按数组下标从 1 递增写入库。
     * </p>
     * <p>各字段与 type / mode 的对应关系：</p>
     * <ul>
     *   <li>{@code type}：必填，取值 {@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlRuleTypeEnum}</li>
     *   <li>{@code mode}：replace / substring / fillEmpty 时必填，取值见各类型的子模式枚举（见各字段说明）</li>
     *   <li>{@code toPositive}、{@code toNegative}：仅需 {@code type}，无 mode 及其它字段</li>
     * </ul>
     */
    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EtlRuleDTO implements Serializable {

        /**
         * 规则类型（每行第一个下拉）。
         * <p>枚举：{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlRuleTypeEnum}，提交 {@code getCode()}。</p>
         * <ul>
         *   <li>{@code replace} — 字符替换</li>
         *   <li>{@code substring} — 字段截取</li>
         *   <li>{@code toPositive} — 转化数值为正数</li>
         *   <li>{@code toNegative} — 转化数值为负数</li>
         *   <li>{@code fillEmpty} — 为空填充</li>
         * </ul>
         */
        private String type;

        /**
         * 执行顺序。保存后由后端回填；前端新增/修改请求可省略，以 {@code etlRuleList} 数组顺序为准。
         */
        private Integer index;

        /**
         * 子模式（第二个下拉），取值随 {@link #type} 变化，统一使用字段名 mode（勿用 replaceMode、fillMode）。
         * <ul>
         *   <li>{@code type=replace} → {@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlReplaceModeEnum}
         *       （{@code replaceTo} 替换为 / {@code replaceEmpty} 替换为空）</li>
         *   <li>{@code type=substring} → {@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum}
         *       （{@code bySymbol} / {@code byOrder} / {@code chinese} / {@code english}）</li>
         *   <li>{@code type=fillEmpty} → {@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum}
         *       （{@code custom} 自定义 / {@code field} 按表头取值）</li>
         *   <li>{@code type=toPositive}、{@code toNegative} — 不传 mode</li>
         * </ul>
         */
        private String mode;

        /**
         * 被替换的原字符串。仅 {@code type=replace} 时必填。
         */
        private String sourceText;

        /**
         * 替换后的目标字符串。仅 {@code type=replace} 且
         * {@code mode=replaceTo}（{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlReplaceModeEnum#REPLACE_TO}）时必填。
         */
        private String targetText;

        /**
         * 截取用符号。仅 {@code type=substring} 且
         * {@code mode=bySymbol}（{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum#BY_SYMBOL}）时必填。
         */
        private String symbol;

        /**
         * 按符号截取时的保留侧。仅 {@code type=substring} 且 {@code mode=bySymbol} 时必填。
         * <p>枚举：{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlSymbolPositionEnum}
         * （{@code before} 符号前 / {@code after} 符号后）。</p>
         */
        private String symbolPosition;

        /**
         * 按顺序截取的方向。仅 {@code type=substring} 且
         * {@code mode=byOrder}（{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlSubstringModeEnum#BY_ORDER}）时必填。
         * <p>枚举：{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlOrderDirectionEnum}
         * （{@code left} 从左 / {@code right} 从右）。</p>
         */
        private String orderDirection;

        /**
         * 按顺序截取的长度（大于 0）。仅 {@code type=substring} 且 {@code mode=byOrder} 时必填。
         */
        private Integer length;

        /**
         * 自定义填充值。仅 {@code type=fillEmpty} 且
         * {@code mode=custom}（{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum#CUSTOM}）时必填。
         */
        private String fillValue;

        /**
         * Excel 表头名称（物流商列名）。仅 {@code type=fillEmpty} 且
         * {@code mode=field}（{@link com.erp.model.tms.enums.CfgLogisticsCostImportEtlFillModeEnum#FIELD}）时必填；
         * 从当前行该表头列取值填充空字段。
         */
        private String sourceField;
    }


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
        * 物流商抬头字段
        */
        private String sourceField;

        /**
        * 物流商明细字段
        */
        private String sourceDetailField;

        /**
        * 默认值
        */
        private String defaultValue;

        /**
        * 字段清洗规则
        */
        private List<EtlRuleDTO> etlRuleList;

        /**
        * 字段清洗规则存储值
        */
        @JsonIgnore
        private String etlRuleListStorage;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue;

        /**
        * ERP字段id
        */
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


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
        * 物流商抬头字段
        */
        private String sourceField;

        /**
        * 物流商明细字段
        */
        private String sourceDetailField;

        /**
        * 默认值
        */
        private String defaultValue;

        /**
        * 字段清洗规则
        */
        private List<EtlRuleDTO> etlRuleList;

        /**
        * 字段清洗规则存储值
        */
        @JsonIgnore
        private String etlRuleListStorage;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue;

        /**
        * ERP字段id
        */
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;


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
         * 主表id
         */
        private String mainId;

        /**
        * 物流商抬头字段
        */
        @Size(max = 200,message = "物流商抬头字段最大长度不能超过200位")
        private String sourceField;

        /**
        * 物流商明细字段
        */
        @Size(max = 200,message = "物流商明细字段最大长度不能超过200位")
        private String sourceDetailField;

        /**
        * 默认值
        */
        @Size(max = 200,message = "默认值最大长度不能超过200位")
        private String defaultValue;

        /**
        * 字段清洗规则
        */
        private List<EtlRuleDTO> etlRuleList;

        /**
        * 字段清洗规则存储值
        */
        @JsonIgnore
        private String etlRuleListStorage;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey = false;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue = false;

        /**
        * ERP字段id
        */
        @NotBlank(message = "ERP字段id不能为空")
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;
        /**
         * 费用项id
         */
        private String targetDetailFieldId;
        /**
         * 费用项
         */
        private String targetDetailField;
        /**
         * 费用项名称
         */
        private String targetDetailFieldName;
        /**
         * 排序
         */
        private Integer index;


    }


}
