package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 模具返还策略请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-15
*/
@Data
@NoArgsConstructor
public class CfgMoldReturnAlertRuleDTO implements Serializable {

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
        private String tabFlagName;

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
         * 勾选的id集合
         */
        private List<String> ids;

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
         * 策略状态
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
         * 开始日期
         */
        private LocalDate startDate;

        /**
         * 结束日期
         */
        private LocalDate endDate;

        /**
         * 标准：purchaseOrder=以采购下单数量 ,warehouseReceive=以采购收货数量 ,poInstock=以采购入库数量
         * 值来源 SourceTypeEnum 的type
         * CfgMoldReturnAlertRuleCountDimEnum
         */
        private String countDim;
        private String countDimName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 修改时间
         */
        private LocalDateTime updateTime;

        /**
         * 明细id
         */
        private String detailId;

        /**
         * 修改人名称
         */
        private String updateUserName;


        /**
         * 返还数量上限
         */
        private Integer returnQtyLimit;


        /**
         * 返还金额
         */
        private BigDecimal returnPrice;


        /**
         * 明细备注
         */
        private String detailRemark;



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
        * 开始日期
        */
        private LocalDate startDate;

        /**
        * 结束日期
        */
        private LocalDate endDate;

        /**
        * 标准：purchaseOrder=以采购下单数量 ,warehouseReceive=以采购收货数量 ,poInstock=以采购入库数量
        */
        private String countDim;
        private String countDimName;

        /**
         * 明细
         */
        private List<CfgMoldReturnAlertDetailDTO.ViewDTO> detailList;


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
        private String moldId;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<CfgMoldReturnAlertDetailDTO.@Valid AddDTO> detailList;
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

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<CfgMoldReturnAlertDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


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
        * 标准：purchaseOrder=以采购下单数量 ,warehouseReceive=以采购收货数量 ,poInstock=以采购入库数量
        */
        @NotBlank(message = "返还标准不能为空")
        private String countDim;


    }


}