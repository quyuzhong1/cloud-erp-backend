package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售价变更表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoPriceChangeDTO implements Serializable {

    /**
     * 添加销售价目变更
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends PermissionsDTO {

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 原因
         */
        private String reason;


        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        @Valid
        private List<SoPriceChangeDetailDTO.AddDTO> soPriceChangeDetailList;


    }


    /**
     * 修改销售价目变更
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  extends PermissionsDTO{



        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 原因
         */
        private String reason;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        @Valid
        private List<SoPriceChangeDetailDTO.UpdateDTO> soPriceChangeDetailList;

    }



    /**
     * 修改销售价目详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {



        @NotBlank(message = "id不能为空")
        private String id;


        private String code;


        /**
         * 销售价目表id
         */
        @NotBlank(message = "销售价目表id 不能为空")
        private String soPriceId;

        /**
         * 价目表id
         */
        private List<String> soPriceIdList;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;


        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;

        /**
         * 原因
         */
        private String reason;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        private List<SoPriceChangeDetailDTO.ViewDTO> soPriceChangeDetailList;

    }


    /**
     * 销售价目变更分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * 详情id
         */
        private String changeDetailId;

        /**
         * 价目表id
         */
        private String soPriceId;

        /**
         * 价目明细id
         */
        private String soPriceDetailId;

        /**
         * code
         */
        private String code;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名
         */
        private String customerName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 最小数量
         */
        private Integer minQty;


        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 单据状态code
         */
        private String approveStatusCode;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种 符号
         */
        private String currencySymbol;
        /**
         *  禁用启用状态
         *  true 禁用
         *  fase 启用
         */
        private Boolean disabled;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 生效时间
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间
         */
        private LocalDate expireDate;

        /**
         * 升降比例（带百分比）【不能排序】
         */
        private String offsetRate;

        /**
         * 销售组织
         */
        private String soOrgId;

        /**
         * 销售组织名
         */
        private String soOrgName;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建人名称
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 备注
         */
        private String detailRemark;

    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO  extends PagingParamDTO{

        /**
         * 主键id集合
         */
        private List<String> ids;
    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 ,(approveIng待我审核,approve已审核,reject不通过)
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


}