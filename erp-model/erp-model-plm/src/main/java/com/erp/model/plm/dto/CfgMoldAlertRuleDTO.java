package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * <p>
 * 模具预警策略请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-20
*/
@Data
@NoArgsConstructor
public class CfgMoldAlertRuleDTO implements Serializable {

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
        * 是否禁用
        */
        private Boolean disabled;
        private String disabledName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 模具id
        */
        private String moldId;

        /**
        * 模具编码
        */
        private String moldCode;

        /**
        * 模具名称
        */
        private String moldName;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 供应商编号
        */
        private String supplierCode;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 寿命数量
        */
        private Integer lifeQty;

        /**
        * 预警寿命（数量）
        */
        private Integer alertLifeQty;

        /**
        * 预警寿命（%）
        */
        private BigDecimal alertLifeRate;

        /**
        * 开始日期
        */
        private LocalDate startDate;

        /**
        * 结束日期
        */
        private LocalDate endDate;

        /**
        * 预警标准
        */
        private String countDim;
        private String countDimName;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 模具id
         */
        @NotBlank(message = "模具id不能为空")
        @Size(max = 19,message = "模具id最大长度不能超过19位")
        private String moldId;

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
        * 策略状态
        */
        @NotNull(message = "策略状态不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 寿命数量
        */
        @NotNull(message = "寿命数量不能为空")
        @Min(value = 1,message = "寿命数量不能小于1")
        private Integer lifeQty;

        /**
        * 预警寿命（数量）
        */
        @NotNull(message = "预警寿命（数量）不能为空")
        @Min(value = 1,message = "预警寿命（数量）不能小于1")
        private Integer alertLifeQty;

        /**
        * 预警寿命（%）
        */
        @Digits(integer = 12, fraction = 4, message = "预警寿命（%）整数位不能超过12位，小数位不能超过4位")
        @DecimalMin(value = "0", message = "预警寿命（%）不能小于0")
        @DecimalMax(value = "100", inclusive = true, message = "预警寿命（%）不能大于100")
        private BigDecimal alertLifeRate;

        /**
        * 开始日期
        */
        @NotNull(message = "开始日期不能为空")
        private LocalDate startDate;

        /**
        * 结束日期
        */
        @NotNull(message = "结束日期不能为空")
        private LocalDate endDate;

        /**
        * 预警标准
        */
        @NotBlank(message = "预警标准不能为空")
        private String countDim;


    }


    /**
     * 列表 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListDTO implements Serializable {
        /**
         * 主键ID
         */
        private String id;
        /**
         * 创建人ID
         */
        private String createUserId;
        /**
         * 创建人姓名
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 修改人ID
         */
        private String updateUserId;
        /**
         * 修改人姓名
         */
        private String updateUserName;
        /**
         * 修改时间
         */
        private LocalDateTime updateTime;
        /**
         * 版本号
         */
        private Integer version;
        /**
         * 是否删除
         */
        private Boolean isDeleted;
        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 是否作废
         */
        private Boolean invalidStatus;
        /**
         * 备注
         */
        private String remark;
        /**
         * 作废备注
         */
        private String invalidRemark;
        /**
         * 模具ID
         */
        private String moldId;
        /**
         * 模具编码
         */
        private String moldCode;
        /**
         * 模具名称
         */
        private String moldName;
        /**
         * 供应商ID
         */
        private String supplierId;
        /**
         * 供应商编号
         */
        private String supplierCode;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 寿命数量
         */
        private Integer lifeQty;
        /**
         * 预警寿命(数量)
         */
        private Integer alertLifeQty;
        /**
         * 预警寿命(百分比)
         */
        private BigDecimal alertLifeRate;
        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
        /**
         * 预警标准
         */
        private String countDim;
        /**
         * 派生显示 禁用状态中文
         */
        private String disabledName;
        /**
         * 派生显示 作废状态中文
         */
        private String invalidStatusName;
        /**
         * 预警标准
         */
        private String countDimName;
    }

    /**
     * 分页查询参数 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
        /**
         * 勾选的id集合
         */
        private List<String> ids;

    }

    /**
     * Tab统计 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TabListDTO implements Serializable {
        /**
         * tab标识
         */
        private String tabFlag;
        /**
         * tab名称
         */
        private String tabFlagName;
        /**
         * 数量
         */
        private Integer count;
    }


}