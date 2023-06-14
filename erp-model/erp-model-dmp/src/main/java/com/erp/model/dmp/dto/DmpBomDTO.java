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
 * sku bom关系表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class DmpBomDTO implements Serializable {




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
        * sku编号
        */
        private String skuNo;
        /**
        * sku名称
        */
        private String name;
        /**
        * sku用量
        */
        private Integer qty;
        /**
        * 父sku
        */
        private String parentSku;
        /**
        * 关系类型 组合：combine 加工：machining
        */
        private String relationType;
        /**
        * 平台标识
        */
        private String platformSign;
        /**
        * 备注
        */
        private String remark;
        /**
        * 有效状态
        */
        private Boolean status;

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
        * sku编号
        */
        @NotBlank(message = "sku编号不能为空")
        @Size(max = 64,message = "sku编号最大长度不能超过64位")
        private String skuNo;
        /**
        * sku名称
        */
        @NotBlank(message = "sku名称不能为空")
        @Size(max = 255,message = "sku名称最大长度不能超过255位")
        private String name;
        /**
        * sku用量
        */
        @NotNull(message = "sku用量不能为空")
        private Integer qty;
        /**
        * 父sku
        */
        @NotBlank(message = "父sku不能为空")
        @Size(max = 255,message = "父sku最大长度不能超过255位")
        private String parentSku;
        /**
        * 关系类型 组合：combine 加工：machining
        */
        @NotBlank(message = "关系类型 组合：combine 加工：machining不能为空")
        @Size(max = 64,message = "关系类型 组合：combine 加工：machining最大长度不能超过64位")
        private String relationType;
        /**
        * 平台标识
        */
        @NotBlank(message = "平台标识不能为空")
        @Size(max = 64,message = "平台标识最大长度不能超过64位")
        private String platformSign;
        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;
        /**
        * 有效状态
        */
        @NotNull(message = "有效状态不能为空")
        private Boolean status;

    }


}