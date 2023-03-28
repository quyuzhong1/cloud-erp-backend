package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.RegularValid;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商信息
 *
 * @author yl
 * @Classname SupplierDTO
 * @Description TODO
 * @Date 2023-03-15 16:35
 * @Created by yl
 */
public class SupplierDTO implements Serializable {


    /**
     * 供应商基础添加信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddDTO extends SupplierBaseDTO {

        /**
         * 供应商联系信息
         */
        @Valid
        private List<SupplierContactDTO.AddDTO> contactList;

        /**
         * 供应商银行账户信息
         */
        @Valid
        private List<SupplierAccountDTO.AddDTO> bankAccountList;

        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.AddDTO> credentialList;

    }


    /**
     * 供应商修改信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class UpdateDTO extends SupplierBaseDTO {

        /**
         * 供应商联系信息
         */
        @Valid
        private List<SupplierContactDTO.UpdateDTO> contactList;

        /**
         * 供应商银行账户信息
         */
        @Valid
        private List<SupplierAccountDTO.UpdateDTO> bankAccountList;

        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.UpdateDTO> credentialList;

    }


    /**
     * 供应商基础信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class SupplierBaseDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 50, message = "最大50字符")
        private String name;


        /**
         * 分类id
         */
        @NotBlank(message = "分类id不能为空")
        private String categoryId;

        /**
         * 等级id
         */
        @NotBlank(message = "等级id不能为空")
        private String gradeId;


        /**
         * 采购员id
         */
        private String purchaseUserId;


        /**
         * 公司地址
         */
        @Size(max = 100, message = "最大50字符")
        private String companyAddress;


        /**
         * 公司网址
         */
        @Size(max = 100, message = "最大50字符")
        @RegularValid(formatPattern = FieldFormatPatternTypeEnum.URL, message = "网址有误")
        private String companyWebsite;


        /**
         * 结算付款方式
         */
        @NotBlank(message = "结算方式不能为空")
        private String payMethodId;

        /**
         * 结算付款币种
         */
        @NotBlank(message = "结算币种不能为空")
        private String payCurrency;


        /**
         * true 禁用
         * false 启用
         */
        @NotNull(message = "启用状态不能为空")
        private Boolean disabled;

    }


    /**
     * 供应商等级信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class SupplierGradeDTO {
        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        @NotBlank(message = "等级名称不能为空")
        @Size(max = 50, message = "名称最大50字符")
        private String name;
    }


    /**
     * 供应商分页列表
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class PagingParamDTO extends SortDTO {

        /**
         * 供应商名
         */
        private String name;


        /**
         * 分类id集合
         */
        private List<String> categoryIdList;


        /**
         * 阶段
         */
        private List<String> phaseList;


        /**
         * 等级
         */
        private List<String> gradeIdList;


        /**
         * 采购员id
         */
        private List<String> purchaseUserIdList;


        /**
         * 联系人名
         */
        private String contactPerson;


        /**
         * 联系电话
         */
        private String contactTelNumber;

        /**
         * 结算方式
         */
        private List<String> payMethodIdList;

        /**
         * 禁用状态
         * true 禁用
         */
        private Boolean disabled;

        /**
         * 创建人id集合
         */
        private List<String> createUserIdList;

        /**
         * 时间
         */
        private List<LocalDate> createTimeList;
    }


    /**
     * 供应商分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO{

        /**
         * 供应商表id
         */
        private String id;




        /**
         * 名称
         */
        private String name;

        /**
         * 编号
         */
        private String code;


        /**
         * 阶段
         */

        private SupplierPhaseEnum phase;


        /**
         * 阶段code
         */
        private String phaseCode;


        /**
         * 阶段名
         */
        private String phaseName;

        /**
         * 审核状态枚举
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态
         */
        private String approveStatusCode;


        /**
         * 审核状态名
         */
        private String approveStatusName;



        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 分类名
         */
        private String categoryName;


        /**
         * 等级id
         */
        private String gradeId;


        /**
         * 等级名
         */
        private String gradeName;

        /**
         * 禁用 状态
         * true  禁用
         * false 启用
         */
        private Boolean disabled;


        /**
         * 结算付款方式
         */
        private String payMethodId;


        /**
         * 结算付款方式
         */
        private String payMethodName;

        /**
         * 结算付款币种
         */
        private String payCurrency;

        /**
         * 采购员
         */
        private String purchaseUserName;

        /**
         * 联系人名
         */
        private String contactPerson;


        /**
         * 联系人
         */
        private String contactPhone;


        /**
         * 采购次数
         */
        private Integer purchasesCount;

        /**
         * 退货率
         */
        private BigDecimal rejectRate;

        /**
         * 延期率
         */
        private BigDecimal delayRate;

        /**
         * 次品率
         */
        private BigDecimal defectiveRate;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;


        /**
         * 创建人
         */
        private String createUserName;

    }


}
