package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname ShopDTO
 * @Date 2023-06-28 18:28
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ShopDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        private String id;


        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;




        /**
         * 店铺名称
         */
        private String name;


        /**
         * 店铺账号
         */
        private String account;


        /**
         * 店铺负责人
         */
        private String chargeId;

        /**
         * 店铺负责人
         */
        private String chargeName;

        /**
         * 销售组织
         */
        private String salesOrgId;

        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 区域id
         */
        private String dictAreaCode;

        /**
         * 区域名
         */
        private String areaName;

        /**
         * 国家id
         */
        private String dictCountryCode;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 域名
         */
        private String domain;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权状态名
         */
        private String authStatusName;


        /**
         * 禁用状态
         */
        private Boolean disabled;

        /**
         * 禁用状态名
         */
        private String disabledName;


        /**
         * 授权时间
         */
        private LocalDateTime authTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime createTime;


        /**
         * 修改人
         */
        private String updateUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime updateTime;


    }


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 店铺名称
         */
        private String name;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 账号
         */
        private String account;

        /**
         * 国家
         */
        private String dictCountryCode;

        /**
         * 禁用状态集合
         */
        private List<Boolean> disabledList;

        /**
         * 授权状态集合
         */
        private List<String> authStatusList;

        /**
         * 创建人id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间集合
         */
        private List<LocalDateTime> createTimeList;


        /**
         * 授权时间
         */
        private List<LocalDateTime> authTimeList;

        /**
         * 修改人id 集合
         */
        private List<String> updateUserIdList;

        /**
         * 修改时间
         */
        private List<LocalDateTime> updateTimeList;


    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {


        /**
         * 平台
         */
        @NotBlank(message = "平台不能为空")
        private String dictPlatform;


        /**
         * 店铺名称
         */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 100, message = "店铺名称最大100字符")
        private String name;


        /**
         * 店铺账号
         */
        @NotBlank(message = "店铺账号不能为空")
        @Size(max = 100, message = "店铺账号最大100字符")
        private String account;


        /**
         * 店铺负责人
         */
        @NotBlank(message = "负责人不能为空")
        private String chargeId;

        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 区域id
         */
        private String dictAreaCode;

        /**
         * 国家id
         */
        private List<String> dictCountryCodeList;

        /**
         * 域名
         */
        private String domain;

        /**
         * 仓库id
         */
        private String warehouseId;


    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;


        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;




        /**
         * 店铺名称
         */
        private String name;


        /**
         * 店铺账号
         */
        private String account;


        /**
         * 店铺负责人
         */
        private String chargeId;

        /**
         * 店铺负责人
         */
        private String chargeName;

        /**
         * 销售组织
         */
        private String salesOrgId;

        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 区域id
         */
        private String dictAreaCode;

        /**
         * 区域名
         */
        private String areaName;

        /**
         * 国家id
         */
        private String dictCountryCode;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 域名
         */
        private String domain;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权状态名
         */
        private String authStatusName;

        /**
         * 授权时间
         */
        private LocalDateTime authStatusTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人
         */
        private String updateUserName;

        /**
         * 创建时间时间
         */
        private LocalDateTime updateTime;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;


    }

    @Data
    @NoArgsConstructor
    public static class PlatformDTO {

        /**
         * 平台
         */
        private String dictPlatform;
    }

    @Data
    @NoArgsConstructor
    public static class ListTreeDTO {

        /**
         * 平台id
         */
        private String id;

        /**
         * 平台名称
         */
        private String name;

        /**
         * 店铺信息
         */
        private List<ListChildTreeDTO> listChildList;
    }

    @Data
    @NoArgsConstructor
    public static class ListChildTreeDTO {

        /**
         * 店铺id
         */
        private String id;

        /**
         * 店铺名称
         */
        private String name;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        @NotBlank(message = "店铺表不能为空")
        private String id;

        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 100, message = "店铺名称最大100字符")
        private String name;

        /**
         * 店铺负责人
         */
        @NotBlank(message = "负责人不能为空")
        private String chargeId;

        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

    }

    @Data
    @NoArgsConstructor
    public static class BatchSetCostDTO  {

        @NotNull(message = "店铺不能为空")
        private List<String> ids;


        @DecimalMin( value = "0",message = "平台费率必须大于0")
        @NotNull(message = "平台费率不能为空")
        private BigDecimal platformRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "平台选项不能为空")
        private String dictPlatformOption;

        @DecimalMin( value = "0",message = "vat费率必须大于0")
        @NotNull(message = "vat费率不能为空")
        private BigDecimal vatRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "vat选项不能为空")
        private String dictVatOption;


        @DecimalMin( value = "0",message = "转账费率必须大于0")
        @NotNull(message = "转账费率不能为空")
        private BigDecimal transferRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "转帐的选项不能为空")
        private String dictTransferOption;


    }

    @Data
    @NoArgsConstructor
    public static class SetCostDTO  {

        private String id;

        @NotNull(message = "店铺不能为空")
        private String shopId;

        @DecimalMin( value = "0",message = "平台费率必须大于0")
        @NotNull(message = "平台费率不能为空")
        private BigDecimal platformRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "平台选项不能为空")
        private String dictPlatformOption;

        @DecimalMin( value = "0",message = "vat费率必须大于0")
        @NotNull(message = "vat费率不能为空")
        private BigDecimal vatRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "vat选项不能为空")
        private String dictVatOption;


        @DecimalMin( value = "0",message = "转账费率必须大于0")
        @NotNull(message = "转账费率不能为空")
        private BigDecimal transferRate;

        /**
         * 平台的选项
         */
        @NotBlank(message = "转帐的选项不能为空")
        private String dictTransferOption;


    }


    @Data
    @NoArgsConstructor
    public static class ViewCostDTO  {

        /**
         * 费用id
         */
        private String id;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台费率
         */
        private BigDecimal platformRate;

        /**
         * 平台的选项
         */

        private String dictPlatformOption;

        /**
         * vat 费率
         */
        private BigDecimal vatRate;

        /**
         * vat的选项
         */
        private String dictVatOption;


        /**
         * 转账费率
         */
        private BigDecimal transferRate;

        /**
         * 转帐选项
         */
        private String dictTransferOption;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedirectDTO  {


        /**
         * 店铺id
         */
        private String id;

        /**
         * 授权地址
         */
        private String url;

    }
}
