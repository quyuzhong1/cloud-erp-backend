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
 * 物流渠道映射表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsMappingDTO implements Serializable {




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
        * 物流平台
        */
        private String salesPlatform;

        /**
         *  物流销售渠道id
         */
        private String logisticsSaleChannelId;


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
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        private String logisticsSaleChannelId;

        /**
        * 销售平台
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=channelSalesPlatform
        */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 30,message = "物流平台最大长度不能超过30位")
        private String salesPlatform;


    }


}