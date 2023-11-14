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
        * 省 州
        */
        private String province;

        /**
        * 城市
        */
        private String city;

        /**
        * 区
        */
        private String district ;


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
        @NotBlank(message = "国家不能为空")
        private String country;

        /**
        * 省 州 id
        */
        @NotBlank(message = "省 州不能为空")
        private String province;

        /**
        * 城市id
        */
        @NotBlank(message = "城市不能为空")
        private String city;

        /**
        * 区id
        */
        @NotBlank(message = "区不能为空")
        private String district;


    }


}