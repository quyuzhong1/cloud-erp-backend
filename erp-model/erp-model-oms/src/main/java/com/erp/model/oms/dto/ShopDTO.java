package com.erp.model.oms.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.dto.base.UpdateStateDTO.BatchUpdateDTO;
import com.common.business.enums.ServiceCodeNameEnum;
import com.erp.model.oms.enums.ShopTypeEnum;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 平台店铺类型
         */
        @Dict(enumClass = ShopTypeEnum.class)
        private String platformShopType;

        /**
         * 扩展字段的 数据+值
         */
        private String extendData;
        
        /**
         * 结算币别
         */
         private String settlementCurrency;
        private String settlementCurrencyName;
         /**
         * 交易币别
         */
         private String tradeCurrency;
        private String tradeCurrencyName;
         /**
         * 启用时间
         */
         private LocalDateTime enableTime;
         /**
          * 停用时间
          */
         private LocalDateTime downTime;
         /**
         * 店铺退货仓库：名称字段为returnWarehouseName
         */
         private String returnWarehouse;
         private String returnWarehouseName;
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
        /**
         * 销售组织id
         */
        private List<String> salesOrgIdList;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

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
         * 金蝶客户编码
         */
        private String customerId;
        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 是否包含平台仓 true 包含
         */
        @NotNull(message = "是否包含平台仓不能为空")
        private Boolean  isHaveWarehouse;

        /**
         * ioss税号
         */
        private String  iossTaxNo;

        /**
         * 区域id
         */
        private String dictAreaCode;

        /**
         * 站点，必须选一个
         */
        private List<String> dictCountryCodeList;

        /**
         * 域名
         */
        private String domain;

        /**
         * 店铺平台仓库
         */
        private String warehouseId;

        /**
         * VOEC税号
         */
        private String voecTaxNo;
        
        /**
         * 结算币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "结算币别不能为空")
         private String settlementCurrency;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
         @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 启用时间
         */
         @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
         /**
         * 店铺退货仓库： 同店铺平台仓库获取方式
         */
         @NotBlank(message = "店铺退货仓库不能为空")
         private String returnWarehouse;
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
         * 是否包含平台仓  true 包含
         */
        private Boolean  isHaveWarehouse;

        /**
         * ioss税号
         */
        private String  iossTaxNo;
        /**
         * VOEC税号
         */
        private String voecTaxNo;
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

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 客户编号
         */
        private String customerCode;

        /**
         * 结算币别
         */
         private String settlementCurrency;
         /**
         * 交易币别
         */
         private String tradeCurrency;
         /**
         * 启用时间
         */
         private LocalDateTime enableTime;
         /**
          * 停用时间
          */
         private LocalDateTime downTime;
         /**
         * 店铺退货仓库：名称字段为returnWarehouseName
         */
         @Dict(serviceCode = ServiceCodeNameEnum.WMS , queryFieldName = "id" , returnFieldName = "name" , tableName = "warehouse")
         private String returnWarehouse;
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
         * 是否包含平台仓  true 包含
         */
        @NotNull(message = "是否包含平台仓不能为空")
        private Boolean  isHaveWarehouse;

        /**
         * ioss税号
         */
        private String  iossTaxNo;
        /**
         * VOEC税号
         */
        private String voecTaxNo;
        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 客户的id
         * 接口地址：http://172.16.100.11:3002/project/110/interface/api/13777
         */
//        @NotBlank(message = "客户的id不能为空")
        private String customerId;
        
        /**
         * 结算币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "结算币别不能为空")
         private String settlementCurrency;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
         @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 启用时间
         */
         @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
         /**
         * 店铺退货仓库：同店铺平台仓库获取方式
         */
         @NotBlank(message = "店铺退货仓库不能为空")
         private String returnWarehouse;

    }
    @Data
    @NoArgsConstructor
    public static class UpdateInternalDTO {

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
         * 客户的id
         * 接口地址：http://172.16.100.11:3002/project/110/interface/api/13777
         */
        private String customerId;
        
        /**
         * 结算币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "结算币别不能为空")
         private String settlementCurrency;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
         @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 启用时间
         */
         @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
         /**
         * 店铺退货仓库： 同店铺平台仓库获取方式
         */
         @NotBlank(message = "店铺退货仓库不能为空")
         private String returnWarehouse;

    }

    /**
     * 批量修改
     * 状态
     */
    @Data
    @NoArgsConstructor
    public static class ShopBatchUpdateDTO extends BatchUpdateDTO{
    	/**
         * 启用时间
         */
         private LocalDateTime enableTime;
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


    /**
     * 刷新token 的参数
     */
    @Data
    @NoArgsConstructor
    public static class RefreshTokenDTO{

        /**
         * 刷新token
         *
         */
        private String refreshToken;

        /**
         * 客户端id
         */
        private String clientId;

        /**
         * 客户端密码
         */
        private String clientSecret;
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名
         */
        private String shopName;

        /**
         * 店铺授权id
         */
        private String shopAuthId;

        private String baseUrl;


    }
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 区域
         */
        private String dictAreaCode;
        /**
         * 是否已授权
         */
        private Boolean showByAuth = false;
        /**
         * 平台
         */
        private List<String> shopIdList;

    }
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键
         */
        private String id;
        /**
         * 关键词
         */
        private String name;
        /**
         * 账号
         */
        private String account;
        /**
         * 禁用状态
         */
        private Boolean disabled;
        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 是否可选
         */
        private Boolean canCheck=true;

    }
    @Data
    @NoArgsConstructor
    public static class AddInternalDTO {


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
         * 金蝶客户编码
         */
        private String customerId;

        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 是否包含平台仓 true 包含
         */
        private Boolean  isHaveWarehouse;
        
        /**
         * 结算币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
        @NotBlank(message = "结算币别不能为空")
         private String settlementCurrency;
         /**
         * 交易币别 http://172.16.100.11:3002/project/36/interface/api/8485
         */
         @NotBlank(message = "交易币别不能为空")
         private String tradeCurrency;
         /**
         * 启用时间
         */
         @NotNull(message = "启用时间不能为空")
         private LocalDateTime enableTime;
    }

    /**
     * 区域
     */
    @Data
    @NoArgsConstructor
    public static class AreaDTO {

       /**
        * 区域
        */
        private String dictAreaCode;
    }

    /**
     * 区域参数
     */
    @Data
    @NoArgsConstructor
    public static class AreaParamDTO {

        /**
         * 关键词
         */
        private String searchKeyword;

        /**
         * 平台
         */
        private String dictPlatform;
    }
}
