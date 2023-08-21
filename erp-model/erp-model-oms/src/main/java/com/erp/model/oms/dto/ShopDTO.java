package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
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


        /**
         * 平台
         */
        private String dictPlatform;


        /**
         * 店铺账号
         */
        private String account;


        /**
         * 区域id
         */
        private String dictAreaId;

        /**
         * 区域名称
         */
        private String areaName;


        private List<ShopDTO.ViewDTO> detailList;


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
        private String dictCountryId;

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
        private List<LocalDate> createTimeList;


        /**
         * 授权时间
         */
        private List<LocalDate> authTimeList;

        /**
         * 修改人id 集合
         */
        private List<String> updateUserIdList;

        /**
         * 修改时间
         */
        private List<LocalDate> updateTimeList;


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
         * 店铺负责人
         */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
         * 区域id
         */
        private String dictAreaId;

        /**
         * 国家id
         */
        private List<String> dictCountryIdList;

        /**
         * 域名
         */
        private String domain;


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
        private String dictAreaId;

        /**
         * 区域名
         */
        private String areaName;

        /**
         * 国家id
         */
        private String dictCountryId;

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


    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {

        @NotBlank(message = "店铺表不能为空")
        private String id;

    }
}
