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
 * 产品备案表请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class ProductRegistrationDTO implements Serializable {




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
        * sku id
        */
        private String skuId;

        /**
        * sku no
        */
        private String skuNo;

        /**
        * 报关平台
        */
        private String declarePlatform;

        /**
        * 备注
        */
        private String remark;

        /**
        * 是否已备案  true 已备案 fasle 未备案
        */
        private Boolean isRegistration;


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
        * sku id
        */
        @NotBlank(message = "sku id不能为空")
        @Size(max = 19,message = "sku id最大长度不能超过19位")
        private String skuId;

        /**
        * 报关平台
        */
        @NotBlank(message = "报关平台不能为空")
        @Size(max = 32,message = "报关平台最大长度不能超过32位")
        private String declarePlatform;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 是否已备案  true 已备案 fasle 未备案
        */
        @NotNull(message = "是否已备案  true 已备案 fasle 未备案不能为空")
        private Boolean isRegistration;


    }


}