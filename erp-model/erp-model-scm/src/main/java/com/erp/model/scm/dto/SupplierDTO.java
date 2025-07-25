package com.erp.model.scm.dto;

import cn.hutool.json.JSONArray;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.RegularValid;
import com.common.core.anno.StateEnumValue;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.model.scm.entity.SupplierContactEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 供应商信息
 *
 * @author yl
 * @Classname SupplierDTO

 * @Date 2023-03-15 16:35
 * @Created by yl
 */
public class SupplierDTO implements Serializable {
    @Data
    @NoArgsConstructor
    public static class VoucherNoDTO extends BaseIdsDTO.IdsDTO {

        @NotBlank(message = "外部平台单号")
        @Size(max = 255,message = "填写信息不能超过255字符")
        private String voucherNo;

    }

    /**
     * 批量修改供应商分类
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class BatchUpdateCategoryDTO {

        /**
         * 主键集合
         */
        @Valid
        @NotEmpty(message = "至少选择一条数据")
        private List<String> ids;

        /**
         * 分类Id
         */
        @NotBlank(message = "供应商分类不能为空")
        private String categoryId;

    }

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
        @NotEmpty(message = "联系信息至少有一条")
        private List<SupplierContactDTO.AddDTO> contactList;

        /**
         * 供应商银行账户信息
         */
        @Valid
        @NotEmpty(message = "银行账户信息至少有一条")
        private List<SupplierAccountDTO.AddDTO> bankAccountList;

        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.AddDTO> credentialList;

    }


    /**
     * 供应商基础导入的dto
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class ImportAddDTO {

        /**
         * 供应商主键id
         */
        private String id;

        /**
         * 名称
         */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 50, message = "供应商名称最大50字符")
        private String name;


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
        @NotBlank(message = "供应商等级不能为空")
        private String gradeId;


        /**
         * 等级名
         */
        private String gradeName;


        /**
         * 采购员id
         */
        private String purchaseUserId;


        /**
         * 采购员
         */
        private String purchaseUserName;


        /**
         * 公司地址
         */
        @Size(max = 100, message = "公司地址最大100字符")
        private String companyAddress;


        /**
         * 公司网址
         */
        @Size(max = 100, message = "公司网址最大100字符")
        @RegularValid(formatPattern = FieldFormatPatternTypeEnum.URL, message = "公司网址有误")
        private String companyWebsite;


        /**
         * 结算付款方式
         */
        //@NotBlank(message = "结算方式不能为空")
        private String payMethodId;

        /**
         * 结算付款币种
         */
        //@NotBlank(message = "结算币种不能为空")
        private String payCurrency;


        /**
         * true 禁用
         * false 启用
         */
        @NotNull(message = "供应商状态不能为空")
        private Boolean disabled;

        /**
         * 公司注册资金（万）
         */
        private BigDecimal registeredCapital;

        /**
         * 供应商属性
         */
        private JSONArray property;
        /**
         * 供应商品类
         */
        private JSONArray productCategory;

        /**
         * 供应商应用分类
         */
        private JSONArray applicationCategory;

        /**
         * 体系认证
         */
        private JSONArray certificate;

        /**
         * 工厂所在地
         */
        private List<SupplierPlantAddrDTO.AddDTO> plantAddrList;

        /**
         * 供应商联系信息
         */
        @Valid
        private List<SupplierContactDTO.ImportAddDTO> contactList;

        /**
         * 供应商银行账户信息
         */
        @Valid
        private List<SupplierAccountDTO.ImportAddDTO> bankAccountList;

        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.ImportAddDTO> credentialList;

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
        @NotEmpty(message = "联系信息至少有一条")
        private List<SupplierContactDTO.UpdateDTO> contactList;

        /**
         * 供应商银行账户信息
         */
        @Valid
        @NotEmpty(message = "银行账户信息至少有一条")
        private List<SupplierAccountDTO.UpdateDTO> bankAccountList;

        /**
         * 供应商资质信息
         */
        @Valid
        private List<SupplierCredentialDTO.UpdateDTO> credentialList;

    }


    /**
     * 供应商详情
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class SupplierViewDTO {


        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        private String name;

        /**
         * 供应商编码
         */
        private String code;

        /**
         * 外部平台编号
         */
        private String voucherNo;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 等级id
         */
        private String gradeId;

        /**
         * 等级名称
         * 日志会用到
         */
        private String gradeName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 阶段
         */
        private String phase;


        /**
         * 采购员id
         */
        private String purchaseUserId;


        /**
         * 公司地址
         */
        private String companyAddress;


        /**
         * 公司网址
         */
        private String companyWebsite;


        /**
         * 结算付款方式
         */
        private String payMethodId;

        /**
         * 结算付款币种
         */
        private String payCurrency;

        /**
         * 结算付款币种名称
         * 日志会到
         */
        private String payCurrencyName;


        /**
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * SRM协同 true 否 false 是
         */
        private Boolean srmDisabled;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 付款公司名称
         */
        private String paymentCompanyName;

        /**
         * 公司注册资金（万）
         */
        private Integer registeredCapital;

        /**
         * 供应商属性集合,字典property类型
         */
        private JSONArray propertyJson;

        /**
         * 体系认证集合，字典certificate类型
         */
        private JSONArray certificateJson;

        /**
         * 产品分类集合
         */
        private JSONArray productCategoryJson;

        /**
         * 应用分类集合,get,plm/applicationCategory/list
         */
        private JSONArray applicationCategoryJson;

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

        /**
         * 工厂所在地
         */
        private List<SupplierPlantAddrDTO.ViewDTO> plantAddrList;

    }


    /**
     * 供应商基础信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class SupplierBaseDTO extends PermissionsDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * 名称
         */
        @NotBlank(message = "供应商名称不能为空")
        @Size(max = 50, message = "供应商名称最大50字符")
        private String name;
        /**
         * 外部平台编号
         */
        private String voucherNo;


        /**
         * 分类id
         */
        @NotBlank(message = "供应商分类不能为空")
        private String categoryId;

        /**
         * 等级id
         */
        @NotBlank(message = "供应商等级不能为空")
        private String gradeId;

        /**
         * 等级名称
         * 日志会用到
         */
        private String gradeName;


        /**
         * 采购员id
         */
        private String purchaseUserId;


        /**
         * 公司地址
         */
        @Size(max = 100, message = "公司地址最大100字符")
        @NotBlank(message = "公司地址必填")
        private String companyAddress;


        /**
         * 公司网址
         */
        @Size(max = 100, message = "公司网址最大100字符")
        @RegularValid(formatPattern = FieldFormatPatternTypeEnum.URL, message = "公司网址有误")
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
         * 结算付款币种名称
         * 日志会到
         */
        private String payCurrencyName;


        /**
         * true 禁用
         * false 启用
         */
        @NotNull(message = "供应商状态不能为空")
        private Boolean disabled;

        /**
         * SRM协同 true 否 false 是
         */
        private Boolean srmDisabled;

        /**
         * 付款条件 来源 http://172.16.100.11:3002/project/83/interface/api/31039
         */
        @NotBlank(message = "付款条件不能为空")
        private String paymentCondition;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 付款公司名称
         */
        @Size(max = 200, message = "付款公司名称最大200字符")
        private String paymentCompanyName;

        /**
         * 公司注册资金（万）
         */
        @NotNull(message = "注册资金不能为空")
        private Integer registeredCapital;

        /**
         * 供应商属性集合,字典property类型
         */
        @NotEmpty(message = "供应商属性不能为空")
        private JSONArray propertyJson;

        /**
         * 体系认证集合，字典certificate类型
         */
        @NotEmpty(message = "体系认证不能为空")
        private JSONArray certificateJson;

        /**
         * 产品分类集合,get,plm/category/tree
         */
        @NotEmpty(message = "产品分类不能为空")
        private JSONArray productCategoryJson;

        /**
         * 应用分类集合,get,plm/applicationCategory/list
         */
        @NotEmpty(message = "应用分类不能为空")
        private JSONArray applicationCategoryJson;

        /**
         * 工厂所在地,get,sys/dict/city/countryTreeList
         */
        @NotEmpty(message = "工厂所在地不能为空")
        @Valid
        private List<SupplierPlantAddrDTO.AddDTO> plantAddrList;
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
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }


    /**
     * 供应商分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

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
         * 外部平台编号
         */
        private String voucherNo;

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
         * SRM协同 true 否 false 是
         */
        private Boolean srmDisabled;
        /**
         *订单接受规则
         */
        private String orderAcceptRule;
        /**
         *退货确认规则
         */
        private String returnConfirmRule;

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
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;

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
         * 联系人电话
         */
        private String contactTelNumber;


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
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;


        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 供应商代码
         */
        private String identificationCode;

        /**
         * 公司注册资金（万）
         */
        private Integer registeredCapital;

        /**
         * 供应商属性集合
         */
        private JSONArray propertyJson;
        /**
         * 供应商属性名称
         */
        private String propertyNames;

        /**
         * 体系认证集合
         */
        private JSONArray certificateJson;
        /**
         * 体系认证名称
         */
        private String certificateNames;

        /**
         * 产品分类集合
         */
        private JSONArray productCategoryJson;
        /**
         * 产品分类名称
         */
        private String productCategoryNames;
        /**
         * 应用分类集合
         */
        private JSONArray applicationCategoryJson;
        /**
         * 应用分类名称
         */
        private String applicationCategoryNames;
        /**
         * 工厂所在地名称
         */
        private String plantAddrNames;
    }


    /**
     * 导出供应商
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;

    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 联系表id
         */
        private String contactId;
        /**
         * 联系人
         */
        private String person;

        /**
         * 是否禁用
         * true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 是否默认 true  是
         */
        private Boolean isDefault;

        /**
         * 电话
         */
        private String telNumber;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 结算方式名称
         */
        private String payMethodName;

        /**
         * 结算币种
         */
        private String payCurrency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 付款条件名称
         */
        private String paymentConditionName;


        /**
         * 公司地址
         */
        private String companyAddress;

        /**
         * 供货识别码
         */
        private String identificationCode;

        /**
         * 公司注册资金（万）
         */
        private Integer registeredCapital;

        /**
         * 供应商属性集合
         */
        private JSONArray propertyJson;

        /**
         * 体系认证集合
         */
        private JSONArray certificateJson;

        /**
         * 产品分类集合
         */
        private JSONArray productCategoryJson;

        /**
         * 应用分类集合
         */
        private JSONArray applicationCategoryJson;

        /**
         * 工厂所在地
         */
        private List<SupplierPlantAddrDTO.AddDTO> plantAddrList;
    }

    /**
     * 供应商简单信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierSimpleDTO {


        /**
         * 供应商id
         */
        private String id;

        /**
         * 供应商编码
         */
        private String code;

        /**
         * 供应商名称
         */
        private String name;

        /**
         * 审核状态
         */
        @JsonIgnore
        private String approveStatus;

        /**
         * 禁用状态
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class SupplierDefaultDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商信息
         */
        private SupplierEntity supplierEntity;

        /**
         * 联系人信息
         */
        private SupplierContactEntity supplierContactEntity;

        /**
         * 账户信息
         */
        private SupplierAccountEntity accountEntity;
    }

    @Data
    @NoArgsConstructor
    public static class InsertDTO extends AddDTO{
        /**
         * 审核状态
         */
        private ApproveStatusEnum approvalStatus;
        /**
         * 第三方审核人
         */
        private String thirdApprovalUserId;
        /**
         * 审核时间
         */
        private LocalDateTime thirdApproveTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApproveStatusDTO {

        /**
         * 第三方审核人
         */
        private String thirdApprovalUserId;
        /**
         * 审核时间
         */
        private LocalDateTime thirdApproveTime;

        /**
         * 供应商id
         */
        private SupplierEntity supplierEntity;

        /**
         * 审核状态
         *
         */
        //校验数据枚举类型
        @StateEnumValue(strValues = {"waitSubmit","approveIng","reject","approve"}, message = "审核类型有误")
        private ApproveStatusEnum approveStatus;
    }
}
