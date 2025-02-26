package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 店铺渠道关联表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-02-14
*/
@Data
@NoArgsConstructor
public class ShopChannelRefDTO implements Serializable {




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
         * 渠道名称
         */
        private String logisticsChannelName;


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
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 255,message = "店铺id最大长度不能超过255位")
        private String shopId;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 255,message = "渠道id最大长度不能超过255位")
        private String logisticsChannelId;


    }


}