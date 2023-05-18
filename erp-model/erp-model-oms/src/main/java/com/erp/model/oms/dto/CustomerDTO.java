package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SalesPlatformEnum;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname CustomerDTO
 * @Description TODO
 * @Date 2023-05-10 15:43
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class CustomerDTO implements Serializable {


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * all 全部
         * waitApprove 待审核
         * approve 已审核
         * reject 审核不通过
         */
        @StateEnumValue(strValues = {"all", "waitApprove", "approve", "reject"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;


        /**
         * 单号
         */
        private String code;

        /**
         * 客户名称
         */
        private String name;

        /**
         * 客户简称
         */
        private String shortName;


        /**
         * 审核列表集合
         */
        private List<String> approveStatusList;


        /**
         * 使用组织集合
         */
        private List<String> useOrgIdList;


        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

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
         * 客户名称
         */
        private String name;

        /**
         * 简称
         */
        private String shortName;


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
        @NotNull(message = "平台类型不能为空")
        @StateEnumValue(clazz = SalesPlatformEnum.class, message = "平台类型有误")
        private SalesPlatformEnum platformType;


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
        @Size(max = 200, message = "最大200字符")
        private String remark;


        /**
         * 收款条件
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=customerCompanyCategory
         */
        @NotBlank(message = "收款条件不能为空")
        private String conditionDict;

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
         * 销售员信息
         */
        @Valid
        private List<SellerDTO.AddDTO> sellerList;

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
        private SalesPlatformEnum platformType;


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
        @NotNull(message = "平台类型不能为空")
        @StateEnumValue(clazz = SalesPlatformEnum.class, message = "平台类型有误")
        private SalesPlatformEnum platformType;


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
        @Size(max = 200, message = "最大200字符")
        private String remark;


        /**
         * 收款条件
         * http://172.16.100.11:3002/project/110/interface/api/13435
         * key=customerCompanyCategory
         */
        @NotBlank(message = "收款条件不能为空")
        private String conditionDict;

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
         * 销售员信息
         */
        private List<SellerDTO.ViewDTO> sellerList;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
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
         * 是否禁用
         * false 没有
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

    }
}
