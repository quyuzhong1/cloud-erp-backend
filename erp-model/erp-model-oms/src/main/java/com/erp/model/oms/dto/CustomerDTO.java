package com.erp.model.oms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.UpdateStateDTO.BatchUpdateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ServiceCodeNameEnum;
import com.erp.model.oms.enums.CustomerInfoBusinessModeEnum;
import com.erp.model.oms.enums.CustomerInfoCheckTypeEnum;
import com.erp.model.oms.enums.CustomerInfoPeriodSettingEnum;
import com.erp.model.oms.enums.CustomerInfoTransactionalModeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname CustomerDTO
 * @Date 2023-05-10 15:43
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomerDTO implements Serializable {


    /**
     * 第三方客户账户信息
     */
    @Data
    @NoArgsConstructor
    public static class ThirdCustomerAccountDTO{
        /**
         * 客户id
         */
        private String id;

        /**
         * 账户余额
         */
        private BigDecimal amount;

        /**
         * 返利账户余额
         */
        private BigDecimal rebateAmount;

        /**
         * 授信账户余额
         */
        private BigDecimal creditAmount;

    }
    /**
     * 分页参数
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
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String searchType;


        private Integer count;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 军区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        /**
         * 军区名称
         */
        private String partitionName;
        /**
         * 客户名称
         */
        private String name;

        /**
         * 简称
         */
        private String shortName;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 平台类型
         */
        private String platformType;

        /**
         * 平台类型名称
         */
        private String platformTypeName;

        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;


        /**
         * 禁用状态 true 禁用
         * false 启用
         */
        private Boolean disabled;

        /**
         * 禁用状态 true 禁用
         * false 启用
         */
        private String disabledName;


        /**
         * 使用组织名
         */
        private String useOrgName;

        /**
         * 使用组织id
         */
        private String useOrgId;

        /**
         * 客户分组id
         */
        private String groupId;

        /**
         * 客户分组
         */
        private String groupName;


        /**
         * 最新审核人
         */
        private String approveUserName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 结算币别 
         */
        @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "name" , tableName = "dict_currency")
        private String currency;
        private String currencyName;
        
         /**
         * 财务组织 名称字段financialOrganizationName
         */
         @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
         private String financialOrganization;
         private String financialOrganizationName;
         /**
         * 启用时间
         */
         private LocalDateTime enableTime;
         /**
          * 停用时间
          */
          private LocalDateTime downTime;
         /**
         * 交易币别 
         */
         @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "name" , tableName = "dict_currency")
         private String tradeCurrency;
         /**
         * 平台类型: 名称字段businessModeName
         */
         @Dict(enumClass = CustomerInfoBusinessModeEnum.class)
         private String businessMode;
         /**
         * 交易模式:名称字段transactionalModeName
         */
         @Dict(enumClass = CustomerInfoTransactionalModeEnum.class)
         private String transactionalMode;
         /**
         * 账期设置：名称字段periodSettingName
         */
         @Dict(enumClass = CustomerInfoPeriodSettingEnum.class)
         private String periodSetting;
         /**
         * 确收方式: 名称字段checkTypeName
         */
         @Dict(enumClass = CustomerInfoCheckTypeEnum.class)
         private String checkType;
         /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;

    }

    @Data
    @NoArgsConstructor
    public static class VirtualDTO {

        @NotBlank(message = "客户id不能为空")
        private String customerId;

        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;
    }
    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 名称
         */
        private String code;
        /**
         * 简称
         */
        private String shortName;
        /**
         * 简称
         */
        private String approveStatus;
        /**
         *
         */
        private Boolean disabled;
        /**
         * 币种
         */
        private String currency;
        /**
         * 使用组织
         */
        private String useOrgId;
    }
    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 是否禁用
         * false 没有
         */
        private Boolean disabled;

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 主键id
         */
        private String id;
    }
    /**
     * 新增加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 使用组织
         */
        @NotBlank(message = "使用组织不能为空")
        private String useOrgId;


        /**
         * 对应组织id
         */
        private String innerOrgId;

        /**
         * 分组id
         */
        private String groupId;

        /**
         * 平台类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=SalesPlatform
         */
        @NotBlank(message = "平台类型不能为空")
        private String platformType;


        /**
         * 公司类别
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * type=customerCompanyCategory
         */
        private String companyCategoryDict;


        /**
         * 国家id
         * 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        @NotBlank(message = "国家不能为空")
        private String countryId;

        /**
         * 省id
         * http://172.16.100.11:3002/project/36/interface/api/13408
         */
        private String provinceId;


        /**
         * 城市id
         * http://172.16.100.11:3002/project/36/interface/api/13408
         */
        private String cityId;


        /**
         * 客户名称
         */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 200, message = "客户名称最大200字符")
        private String name;

        /**
         * 客户简称
         */
        private String shortName;

        /**
         * 付款方
         */
        private List<String> payCodeList;


        /**
         * 结算方
         */
        private String settleCode;


        /**
         * 结算方式
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=settleMode
         */
        private String settleDict;


        /**
         * 币种
         */
        @NotBlank(message = "结算币种不能为空")
        private String currency;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;


        /**
         * 收款条件
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=customerCompanyCategory
         */
        @NotBlank(message = "收款条件不能为空")
        private String conditionDict;

        @NotBlank(message = "销售员不能为空")
        private String sellerId;
        /**
         * 销售部门id
         */
        @NotBlank(message = "销售部门id不能为空")
        private String salesDeptId;
        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 客户属性
         */
        private String customerProperty;

        /**
         * 通讯地址
         */
        @Size(max = 255, message = "通讯地址最大255字符")
        private String mailAddress;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;


        /**
         * 联系人信息
         */
        @Valid
        private List<CustomerContactDTO.AddDTO> contactList;

        /**
         * 地址信息
         */
        @Valid
        private List<CustomerAddressDTO.AddDTO> addressList;


        /**
         * 发票信息
         */
        @Valid
        private List<InvoiceDTO.AddDTO> invoiceList;

         /**
         * 财务组织 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "财务组织不能为空")
         private String financialOrganization;
         /**
         * 启用时间
         */
        @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 平台类型: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoBusinessMode
         */
        @NotBlank(message = "平台类型不能为空")
         private String businessMode;
         /**
         * 交易模式: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoTransactionalMode
         */
        @NotBlank(message = "交易模式不能为空")
         private String transactionalMode;
         /**
         * 账期设置：http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoPeriodSetting
         */
        @NotBlank(message = "账期设置不能为空")
         private String periodSetting;
         /**
         * 确收方式: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoCheckType
         */
        @NotBlank(message = "确收方式不能为空")
         private String checkType;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 军区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        /**
         * 军区名称
         */
        private String partitionName;
        /**
         * 审核状态code
         */
        private ApproveStatusEnum approveStatus;


        /**
         * 审核状态名
         */
        private String approveStatusName;


        /**
         * 使用组织
         */
        private String useOrgId;


        /**
         * 使用组织名
         */
        private String useOrgName;

        /**
         * 内部组织id
         */
        private String innerOrgId;

        /**
         * 内部组织
         */
        private String innerOrgName;

        /**
         * 分组id
         */
        private String groupId;

        /**
         * 国家id
         */
        @NotBlank(message = "国家不能为空")
        private String countryId;

        /**
         * 地区
         */
        private String areaId;

        /**
         * 地区名
         */
        private String areaName;

        /**
         * 子区域名
         */
        private String subregionName;

        /**
         * 省id
         */
        private String provinceId;


        /**
         * 城市id
         */
        private String cityId;


        /**
         * 客户名称
         */
        @NotBlank(message = "客户名称不能为空")
        private String name;

        /**
         * 客户简称
         */
        private String shortName;

        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 销售部门id
         */
        @NotBlank(message = "销售部门id不能为空")
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;

        /**
         * 付款方
         */
        private List<String> payCodeList;


        /**
         * 结算方
         */
        private String settleCode;


        /**
         * 币种
         */
        private String currency;

        /**
         * 平台类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=SalesPlatform
         */
        private String platformType;


        /**
         * 结算方式
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=settleMode
         */
        private String settleDict;


        /**
         * 公司类别
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * type=customerCompanyCategory
         */
        private String companyCategoryDict;

        /**
         * 备注
         */
        private String remark;


        /**
         * 条件
         */
        private String conditionDict;

        /**
         * 客户属性
         */
        private String customerProperty;

        /**
         * 通讯地址
         */
        private String mailAddress;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;


        /**
         * 联系人信息
         */
        private List<CustomerContactDTO.ViewDTO> contactList;

        /**
         * 地址信息
         */
        private List<CustomerAddressDTO.ViewDTO> addressList;


        /**
         * 发票信息
         */
        private List<InvoiceDTO.ViewDTO> invoiceList;

        /**
         * 销售员信息
         */
        private List<SellerDTO.ViewDTO> sellerList;
        
         /**
         * 财务组织 名称字段financialOrganizationName
         */
         @Dict(serviceCode = ServiceCodeNameEnum.SYS , queryFieldName = "id" , returnFieldName = "company_name" , tableName = "sys_accounting_company")
         private String financialOrganization;
         /**
         * 启用时间
         */
         private LocalDateTime enableTime;
         /**
          * 停用时间
          */
          private LocalDateTime downTime;
         /**
         * 交易币别 
         */
         private String tradeCurrency;
         /**
         * 平台类型: 名称字段businessModeName
         */
         @Dict(enumClass = CustomerInfoBusinessModeEnum.class)
         private String businessMode;
         /**
         * 交易模式:名称字段transactionalModeName
         */
         @Dict(enumClass = CustomerInfoTransactionalModeEnum.class)
         private String transactionalMode;
         /**
         * 账期设置：名称字段periodSettingName
         */
         @Dict(enumClass = CustomerInfoPeriodSettingEnum.class)
         private String periodSetting;
         /**
         * 确收方式: 名称字段checkTypeName
         */
         @Dict(enumClass = CustomerInfoCheckTypeEnum.class)
         private String checkType;
               
    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class CustomerBatchUpdateDTO extends BatchUpdateDTO{
    	/**
         * 启用时间
         */
         private LocalDateTime enableTime;
    }
    	/**
    	 * 修改
    	 */
    	@Data
    	@NoArgsConstructor
    	public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "客户信息不能为空")
        private String id;

        /**
         * 对应组织id
         */
        private String innerOrgId;

        /**
         * 分组id
         */
        private String groupId;

        /**
         * 平台类型
         * http://172.16.100.11:3002/project/110/interface/api/13480
         * type=SalesPlatform
         */
        @NotBlank(message = "平台类型不能为空")
        private String platformType;


        /**
         * 公司类别
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * type=customerCompanyCategory
         */
        private String companyCategoryDict;

        /**
         * 国家id
         * 来源 http://172.16.100.11:3002/project/36/interface/api/13390
         */
        @NotBlank(message = "国家不能为空")
        private String countryId;

        /**
         * 省id
         * http://172.16.100.11:3002/project/36/interface/api/13408
         */
        private String provinceId;


        /**
         * 城市id
         * http://172.16.100.11:3002/project/36/interface/api/13408
         */
        private String cityId;


        /**
         * 客户名称
         */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 200, message = "客户名称最大200字符")
        private String name;

        /**
         * 客户简称
         */
        private String shortName;

        @NotBlank(message = "销售员不能为空")
        private String sellerId;
            /**
             * 销售部门id
             */
            @NotBlank(message = "销售部门id不能为空")
            private String salesDeptId;
        /**
         * 付款方
         */
        private List<String> payCodeList;


        /**
         * 结算方
         */
        private String settleCode;


        /**
         * 结算方式
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=settleMode
         */
        private String settleDict;


        /**
         * 币种
         */
        @NotBlank(message = "结算币种不能为空")
        private String currency;

        /**
         * 备注
         */
        @Size(max = 200, message = "备注最大200字符")
        private String remark;


        /**
         * 收款条件
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=customerCompanyCategory
         */
        @NotBlank(message = "收款条件不能为空")
        private String conditionDict;

        /**
         * 客户属性
         */
        private String customerProperty;

        /**
         * 通讯地址
         */
        @Size(max = 255, message = "通讯地址最大255字符")
        private String mailAddress;

        /**
         * 附件名集合
         */
        private List<String> attachNameList;

        /**
         * 附件url集合
         */
        private List<String> attachUrlList;


        /**
         * 使用组织
         */
        @NotBlank(message = "使用组织不能为空")
        private String useOrgId;


        /**
         * 联系人信息
         */
        private List<CustomerContactDTO.ViewDTO> contactList;

        /**
         * 地址信息
         */
        private List<CustomerAddressDTO.ViewDTO> addressList;


        /**
         * 发票信息
         */
        private List<InvoiceDTO.ViewDTO> invoiceList;

         /**
         * 财务组织 http://172.16.100.11:3002/project/36/interface/api/30795
         */
        @NotBlank(message = "财务组织不能为空")
         private String financialOrganization;
         /**
         * 启用时间
         */
        @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 平台类型: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoBusinessMode
         */
        @NotBlank(message = "平台类型不能为空")
         private String businessMode;
         /**
         * 交易模式: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoTransactionalMode
         */
        @NotBlank(message = "交易模式不能为空")
         private String transactionalMode;
         /**
         * 账期设置：http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoPeriodSetting
         */
        @NotBlank(message = "账期设置不能为空")
         private String periodSetting;
         /**
         * 确收方式: http://172.16.100.11:3002/project/110/interface/api/13480 type=CustomerInfoCheckType
         */
        @NotBlank(message = "确收方式不能为空")
         private String checkType;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class InfoDTO {

        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * name
         */
        private String name;
        /**
         * 简称
         */
        private String shortName;

        private ApproveStatusEnum approveStatus;

        /**
         * 是否禁用
         * false 没有
         */
        private Boolean disabled;

        /**
         * 币种
         */
        private String currency;
    }

    /**
     * 收货信息
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveInfoDTO {

        /**
         * id
         */
        private String id;

        /**
         * code
         */
        private String code;

        /**
         * 客户名称
         */
        private String name;

        /**
         * 收货地址
         */
        private String receiveAddress;

        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 启用禁用
         */
        private Boolean disabled;

    }

    /**
     * 获取到基础的信息
     */
    @Data
    @NoArgsConstructor
    public static class BaseDTO {

        /**
         * id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 联系人
         */
        private String person;

        /**
         * 联系电话
         */
        private String telNumber;

        /**
         * 地址
         */
        private String address;

        /**
         * 地址
         */
        private String addressId;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 地址类型
         */
        private String addressType;

        /**
         * 销售员
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 使用组织
         */
        private String useOrgId;

        /**
         * 使用组织名称
         */
        private String useOrgName;

        /**
         * 收款条件
         */
        private String receiveCondition;

        /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;

        private String receiveConditionName;

        //----销售组织信息----
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;


    }

    @Data
    @NoArgsConstructor
    public static class ApproveCountDTO {
        /**
         * 类型
         */
        private String approveStatus;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class SellerUserDeptDTO {
        /**
         * 客户编码
         */
        private String code;
        /**
         * 国家
         */
        private String countryId;
        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员名称
         */
        private String sellerName;

        /**
         * 销售员部门id
         */
        private String deptId;

        /**
         * 销售员部门名称
         */
        private String deptName;
    }


    @Data
    @NoArgsConstructor
    public static class PagingExportDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 编码
         */
        private String code;
        /**
         * 简称
         */
        private String shortName;
        /**
         *区域
         */
        private String areaId;
        private String areaName;

        /**
         * 军区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        /**
         * 军区名称
         */
        private String partitionName;
        /**
         *国家
         */
        private String countryId;
        private String countryName;
        /**
         *省份
         */
        private String provinceId;
        private String provinceName;
        /**
         *城市
         */
        private String cityId;
        private String cityName;
        /**
         *销售员
         */
        private String sellerId;
        private String sellerName;
        /**
         * 平台类型: 名称字段businessModeName
         */
//        @Dict(enumClass = CustomerInfoBusinessModeEnum.class)
        private String businessMode;
        private String businessModeName;
        /**
         * 平台类型
         */
        private String platformType;
        private String platformTypeName;
        /**
         *公司类别
         */
        private String companyCategoryDict;
        private String companyCategoryDictName;
        /**
         *单据状态
         */
        private ApproveStatusEnum approveStatus;
        private String approveStatusName;
        /**
         *启用状态
         */
        private Boolean disabled;
        private String disabledName;
        /**
         *使用组织
         */
        private String useOrgId;
        private String useOrgName;
        /**
         *客户分组
         */
        private String groupName;
        /**
         *对应组织
         */
        private String innerOrgId;
        private String innerOrgName;
        /**
         *客户属性
         */
        private String customerProperty;
        /**
         *通讯地址
         */
        private String mailAddress;
        /**
         *最新审核人
         */
        private String approveUserName;
        /**
         *创建人
         */
        private String createUserId;
        private String createUserName;
        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;

        private List<InvoiceDTO.ViewDTO> invoiceList;

        private List<CustomerDTO.PagingAddressContactExportDTO> addressContactList;
    }


    @Data
    @NoArgsConstructor
    public static class  PagingInvoiceExportDTO{
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 编码
         */
        private String code;

    }

    @Data
    @NoArgsConstructor
    public static class PagingAddressContactExportDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 编码
         */
        private String code;

        //----------联系人信息-----------
        /**
         * 联系人
         */
        private String person;

        /**
         * 职位
         */
        private String position;

        /**
         * 联系电话
         */
        private String personTelNumber;

        /**
         * 邮箱
         */
        private String personEmail;
        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean personIsDefault;
        private String personIsDefaultName;
        /**
         * 是否禁用
         * true 是
         * false 不是
         */
        private Boolean personDisabled;
        private String personDisabledName;
        /**
         *备注
         */
        private String personRemark;

        //----------地址信息-----------
        /**
         * 地址
         */
        private String address;

        /**
         * 地址类型
         */
        private String type;
        private String typeName;

        /**
         * 电话
         */
        private String addressTelNumber;

        /**
         * 邮箱
         */
        private String addressEmail;

        /**
         * 是否默认
         * true 是
         * false 不是
         */
        private Boolean addressIsDefault;
        private String addressIsDefaultName;

        /**
         * 是否禁用
         * true 是
         * false 不是
         */
        private Boolean addressDisabled;
        private String addressDisabledName;

        /**
         * 备注
         */
        private String addressRemark;
    }

}
