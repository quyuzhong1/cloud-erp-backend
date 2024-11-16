package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 物流平台服务表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2024-03-04
*/
@Data
@NoArgsConstructor
public class LogisticsServicePlatformDTO implements Serializable {




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
        * 服务名称
        */
        private String serviceName;

        /**
        * 物流平台
        */
        private String logisticsPlatform;


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
        * 服务名称
        */
        @NotBlank(message = "服务名称不能为空")
        @Size(max = 200,message = "服务名称最大长度不能超过200位")
        private String serviceName;

        /**
        * 物流平台
        */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 32,message = "物流平台最大长度不能超过32位")
        private String logisticsPlatform;


    }

    @Data
    @NoArgsConstructor
    public static class ServiceNameDTO{
        /**
         * 服务名称
         */
        private String serviceName;
    }

}