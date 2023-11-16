package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
        * 主键id
        */
        private String  id;

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
        * 省 州
        */
        private String province;

        /**
         * 省名
         */
        private String provinceName;

        /**
        * 城市
        */
        private String city;

        /**
         * 城市名
         */
        private String cityName;

        /**
        * 区
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
        * 国家 id
        */
        private String country;

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