package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-14
*/
@Data
@NoArgsConstructor
public class DmpSplitErrorLogDTO implements Serializable {




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
        * bom表id
        */
        private String bomId;

        /**
        * 单据原sku
        */
        private String skuNo;

        /**
        * 财务编码
        */
        private String financialCode;

        /**
        * 中台订单详情id
        */
        private String itemId;

        /**
        * 错误描述
        */
        private String msg;


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
        * bom表id
        */
        @NotBlank(message = "bom表id不能为空")
        @Size(max = 255,message = "bom表id最大长度不能超过255位")
        private String bomId;

        /**
        * 单据原sku
        */
        @NotBlank(message = "单据原sku不能为空")
        @Size(max = 255,message = "单据原sku最大长度不能超过255位")
        private String skuNo;

        /**
        * 财务编码
        */
        @NotBlank(message = "财务编码不能为空")
        @Size(max = 255,message = "财务编码最大长度不能超过255位")
        private String financialCode;

        /**
        * 中台订单详情id
        */
        @NotBlank(message = "中台订单详情id不能为空")
        @Size(max = 255,message = "中台订单详情id最大长度不能超过255位")
        private String itemId;

        /**
        * 错误描述
        */
        @NotBlank(message = "错误描述不能为空")
        private String msg;


    }


}