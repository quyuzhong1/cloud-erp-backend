package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 订单标签，面单表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-04-18
*/
@Data
@NoArgsConstructor
public class SoB2cLabelDTO implements Serializable {




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
        * 订单主表id
        */
        private String mainId;

        /**
        * 平台物流面单base64
        */
        private String logisticsLabelBase64;


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
        * 订单主表id
        */
        @NotBlank(message = "订单主表id不能为空")
        @Size(max = 19,message = "订单主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台物流面单base64
        */
        @NotBlank(message = "平台物流面单base64不能为空")
        private String logisticsLabelBase64;


    }


}