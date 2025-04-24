package com.erp.model.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 渠道黑名单表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Data
@NoArgsConstructor
public class LogisticsChannelBlacklistDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 国家
         */
        private String country;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 国家名称
         */
        @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        List<CommonViewDTO> cityList;


    }

    @Data
    @NoArgsConstructor
    public static class CommonViewDTO {


        /**
         * 省 州 id
         */
        private String province;

        /**
         * 省名
         */
        private String provinceName;

        /**
         * 城市id
         */
        private String city;

        /**
         * 城市名
         */
        private String cityName;

        /**
         * 区id
         */
        private String district;

        /**
         * 区名
         */
        private String districtName;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  {
        /**
         * 国家
         */
        private String country;


        private List<CommonDTO>  cityList;


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {


        /**
         * 国家
         */
        private String country;


        private List<CommonDTO>  cityList;



    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {


        /**
         * 省 州 id
         */
        private String province;

        /**
         * 城市id
         */
        private String city;

        /**
         * 区id
         */
        private String district;


    }


}