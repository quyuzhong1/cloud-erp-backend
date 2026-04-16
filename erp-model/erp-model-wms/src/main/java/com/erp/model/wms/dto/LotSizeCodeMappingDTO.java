package com.erp.model.wms.dto;

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
 * GB/T2828.1-2012 批量-样本量字码映射表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
@Data
@NoArgsConstructor
public class LotSizeCodeMappingDTO implements Serializable {


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
        private Map<String, String> sqlMap;

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
        private String id;

        /**
         * 批量下限
         */
        private Integer minLotQty;

        /**
         * 批量上限
         */
        private Integer maxLotQty;

        /**
         * 特殊检验水平S-1字码
         */
        private String codeS1;

        /**
         * 特殊检验水平S-2字码
         */
        private String codeS2;

        /**
         * 特殊检验水平S-3字码
         */
        private String codeS3;

        /**
         * 特殊检验水平S-4字码
         */
        private String codeS4;

        /**
         * 一般检验水平I字码
         */
        private String codeI;

        /**
         * 一般检验水平II字码（默认）
         */
        private String codeIi;

        /**
         * 一般检验水平III字码
         */
        private String codeIii;

        /**
         * 样本量字码（A/B/C...R）
         */
        private String sampleQtyCode;

        /**
         * 样本量
         */
        private Integer sampleQty;

        /**
         * 状态(禁用true启用false)
         */
        private Boolean disabled;


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
        private String id;

        /**
         * 批量下限
         */
        private Integer minLotQty;

        /**
         * 批量上限
         */
        private Integer maxLotQty;

        /**
         * 特殊检验水平S-1字码
         */
        private String codeS1;

        /**
         * 特殊检验水平S-2字码
         */
        private String codeS2;

        /**
         * 特殊检验水平S-3字码
         */
        private String codeS3;

        /**
         * 特殊检验水平S-4字码
         */
        private String codeS4;

        /**
         * 一般检验水平I字码
         */
        private String codeI;

        /**
         * 一般检验水平II字码（默认）
         */
        private String codeIi;

        /**
         * 一般检验水平III字码
         */
        private String codeIii;

        /**
         * 样本量字码（A/B/C...R）
         */
        private String sampleQtyCode;

        /**
         * 样本量
         */
        private Integer sampleQty;

        /**
         * 状态(禁用true启用false)
         */
        private Boolean disabled;


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
         * 批量下限
         */
        @NotNull(message = "批量下限不能为空")
        private Integer minLotQty;

        /**
         * 批量上限
         */
        @NotNull(message = "批量上限不能为空")
        private Integer maxLotQty;

        /**
         * 特殊检验水平S-1字码
         */
        @NotBlank(message = "特殊检验水平S不能为空")
        @Size(max = 2, message = "特殊检验水平S最大长度不能超过2位")
        private String codeS1;

        /**
         * 特殊检验水平S-2字码
         */
        @NotBlank(message = "特殊检验水平S不能为空")
        @Size(max = 2, message = "特殊检验水平S最大长度不能超过2位")
        private String codeS2;

        /**
         * 特殊检验水平S-3字码
         */
        @NotBlank(message = "特殊检验水平S不能为空")
        @Size(max = 2, message = "特殊检验水平S最大长度不能超过2位")
        private String codeS3;

        /**
         * 特殊检验水平S-4字码
         */
        @NotBlank(message = "特殊检验水平S不能为空")
        @Size(max = 2, message = "特殊检验水平S最大长度不能超过2位")
        private String codeS4;

        /**
         * 一般检验水平I字码
         */
        @NotBlank(message = "一般检验水平I字码不能为空")
        @Size(max = 2, message = "一般检验水平I字码最大长度不能超过2位")
        private String codeI;

        /**
         * 一般检验水平II字码（默认）
         */
        @NotBlank(message = "一般检验水平II字码（默认）不能为空")
        @Size(max = 2, message = "一般检验水平II字码（默认）最大长度不能超过2位")
        private String codeIi;

        /**
         * 一般检验水平III字码
         */
        @NotBlank(message = "一般检验水平III字码不能为空")
        @Size(max = 2, message = "一般检验水平III字码最大长度不能超过2位")
        private String codeIii;

        /**
         * 样本量字码（A/B/C...R）
         */
        @NotBlank(message = "样本量字码（A/B/C...R）不能为空")
        @Size(max = 2, message = "样本量字码（A/B/C...R）最大长度不能超过2位")
        private String sampleQtyCode;

        /**
         * 样本量
         */
        @NotNull(message = "样本量不能为空")
        private Integer sampleQty;

        /**
         * 状态(禁用true启用false)
         */
        @NotNull(message = "状态(禁用true启用false)不能为空")
        private Boolean disabled;


    }


}