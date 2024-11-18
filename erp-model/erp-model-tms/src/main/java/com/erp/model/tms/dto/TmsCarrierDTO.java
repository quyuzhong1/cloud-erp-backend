package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 承运商请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-07-04
*/
@Data
@NoArgsConstructor
public class TmsCarrierDTO implements Serializable {




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
        * 承运商代号
        */
        private String code;

        /**
        * 轨迹查询地址
        */
        private String logisticsTrackUrl;

        /**
        * 承运商名称
        */
        private String name;

        /**
        * 销售平台
        */
        private String salesPlatform;


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
        * 轨迹查询地址
        */
        @NotBlank(message = "轨迹查询地址不能为空")
        @Size(max = 255,message = "轨迹查询地址最大长度不能超过255位")
        private String logisticsTrackUrl;

        /**
        * 承运商名称
        */
        @NotBlank(message = "承运商名称不能为空")
        @Size(max = 128,message = "承运商名称最大长度不能超过128位")
        private String name;

        /**
        * 销售平台
        */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 30,message = "销售平台最大长度不能超过30位")
        private String salesPlatform;


    }


}