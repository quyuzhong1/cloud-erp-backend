package com.erp.model.fms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.common.business.dto.AdvanceQueryDTO;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * <p>
 * 资产处置单主表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-29
*/
@Data
@NoArgsConstructor
public class AssetDisposalDTO implements Serializable {


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
         * 类型
         */
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
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 单据号
        */
        private String code;

        /**
        * 业务日期
        */
        private LocalDate businessDate;

        /**
        * 处置方式：scrap=报废，loss=盘亏
        */
        private String disposalMethod;
        private String disposalMethodName;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;

        /**
        * 处置原因
        */
        private String reason;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;


        /**
         * 明细id
         */
        private String assetDisposalDetailId;

        /**
         * 卡片编码
         */
        private String sourceCode;

        /**
         * 资产名称
         */
        private String assetName;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 处置数量
         */
        private Integer disposalQty;

        /**
         * 处置币类
         */
        private String disposalCurrency;
        private String disposalCurrencyName;

        /**
         * 清理费用
         */
        private BigDecimal cleanupCost;

        /**
         * 残值收入 含税
         */
        private BigDecimal residualValue;

        /**
         * 发票类型：ordinary=普通发票, addedValue增值发票
         */
        private String invoiceType;
        private String invoiceTypeName;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税额
         */
        private BigDecimal taxAmount;

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
        * 审批状态
        */
        private String approveStatus;
        private String approveStatusName;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 单据号
        */
        private String code;

        /**
        * 业务日期
        */
        private LocalDate businessDate;

        /**
        * 处置方式：scrap=报废，loss=盘亏
        */
        private String disposalMethod;
        private String disposalMethodName;

        /**
        * 资产组织ID
        */
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        private String assetOrgName;

        /**
        * 处置原因
        */
        private String reason;

        private List<AssetDisposalDetailDTO.ViewDTO> assetDisposalDetailDTOList;

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
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
        * 业务日期
        */
        @NotNull(message = "业务日期不能为空")
        private LocalDate businessDate;

        /**
        * 处置方式：scrap=报废，loss=盘亏   /fms/common/enumDropDown?type = AssetDisposalDisposalMethod
        */
        @NotBlank(message = "处置方式不能为空")
        private String disposalMethod;

        /**
        * 资产组织ID  /sys/company/list
        */
        @NotBlank(message = "资产组织ID不能为空")
        private String assetOrgId;

        /**
        * 资产组织名称
        */
        @NotBlank(message = "资产组织名称不能为空")
        private String assetOrgName;

        /**
        * 处置原因
        */
        private String reason;


        @NotEmpty(message = "资产明细不能为空")
        private List<AssetDisposalDetailDTO.@Valid UpdateDTO> assetDisposalDetailDTOList;
    }


}