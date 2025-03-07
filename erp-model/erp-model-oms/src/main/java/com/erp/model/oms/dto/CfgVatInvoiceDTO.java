package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * VAT发票设置请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
*/
@Data
@NoArgsConstructor
public class CfgVatInvoiceDTO implements Serializable {




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
        * 禁用状态 false 启用
        */
        private Boolean disabled;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺国家id
        */
        private String shopCountryCode;

        /**
        * 启用时间
        */
        private LocalDateTime enableTime;

        /**
        * 自动上传（默认是）
        */
        private Boolean autoUpload;

        /**
        * VAT税率
        */
        private BigDecimal taxRate;

        /**
        * 公司名称
        */
        private String companyName;

        /**
        * 详细地址+城市+州/省+邮编+国家
        */
        private String companyAddress;

        /**
        * 国家二字码
        */
        private String countryCode;

        /**
        * 州/省
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 邮编
        */
        private String postCode;

        /**
        * 详细地址
        */
        private String address;

        /**
        * 税号
        */
        private String vatNo;

        /**
        * 模板类型:erp=ERP模板,official=官方模板
        */
        private String templateType;


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
    public static class CommonDTO {

        /**
        * 禁用状态 false 启用
        */
        @NotNull(message = "禁用状态 false 启用不能为空")
        private Boolean disabled;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 站点
        */
        @NotBlank(message = "站点不能为空")
        @Size(max = 10,message = "站点最大长度不能超过10位")
        private String shopCountryCode;

        /**
        * 启用时间
        */
        @NotNull(message = "启用时间不能为空")
        private LocalDateTime enableTime;

        /**
        * 自动上传（默认是）
        */
        @NotNull(message = "自动上传（默认是）不能为空")
        private Boolean autoUpload;

        /**
        * VAT税率
        */
        @NotNull(message = "VAT税率不能为空")
        private BigDecimal taxRate;

        /**
        * 公司名称
        */
        @NotBlank(message = "公司名称不能为空")
        @Size(max = 100,message = "公司名称最大长度不能超过100位")
        private String companyName;

        /**
        * 详细地址+城市+州/省+邮编+国家
        */
        private String companyAddress;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryCode;

        /**
        * 州/省
        */
        @NotBlank(message = "州/省不能为空")
        @Size(max = 50,message = "州/省最大长度不能超过50位")
        private String province;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 50,message = "城市最大长度不能超过50位")
        private String city;

        /**
        * 邮编
        */
        @NotBlank(message = "邮编不能为空")
        @Size(max = 50,message = "邮编最大长度不能超过50位")
        private String postCode;

        /**
        * 详细地址
        */
        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255,message = "详细地址最大长度不能超过255位")
        private String address;

        /**
        * 税号
        */
        @NotBlank(message = "税号不能为空")
        @Size(max = 100,message = "税号最大长度不能超过100位")
        private String vatNo;

        /**
        * 模板类型:erp=ERP模板,official=官方模板
        */
        private String templateType;

    }


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 禁用状态 false 启用
         */
        private Boolean disabled;

        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 店铺国家id
         */
        private String shopCountryCode;
        /**
         * 站点名称
         */
        private String shopCountryName;

        /**
         * 启用时间
         */
        private LocalDateTime enableTime;

        /**
         * 自动上传（默认是）
         */
        private Boolean autoUpload;

        /**
         * VAT税率
         */
        private BigDecimal taxRate;

        /**
         * 公司名称
         */
        private String companyName;

        /**
         * 详细地址+城市+州/省+邮编+国家
         */
        private String companyAddress;

        /**
         * 国家二字码
         */
        private String countryCode;
        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 州/省
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 邮编
         */
        private String postCode;

        /**
         * 详细地址
         */
        private String address;

        /**
         * VAT税号
         */
        private String vatNo;

        /**
         * 模板类型:erp=ERP模板,official=官方模板
         */
        private String templateType;
        /**
         * 发票模板名称
         */
        private String templateTypeName;
        /**
         * 更新人
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
}