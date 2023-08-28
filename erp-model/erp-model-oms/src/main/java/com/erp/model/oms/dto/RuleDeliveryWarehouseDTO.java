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
 * 发货仓库规则表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@NoArgsConstructor
public class RuleDeliveryWarehouseDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 优先级
        */
        private String priority;

        /**
        * 禁用状态false 未禁用
        */
        private Boolean disabled;

        /**
        * 备注描述
        */
        private String remark;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名
        */
        private String warehouseName;


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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 优先级
        */
        @NotBlank(message = "优先级不能为空")
        @Size(max = 1,message = "优先级最大长度不能超过1位")
        private String priority;

        /**
        * 禁用状态false 未禁用
        */
        @NotNull(message = "禁用状态false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 备注描述
        */
        @NotBlank(message = "备注描述不能为空")
        @Size(max = 255,message = "备注描述最大长度不能超过255位")
        private String remark;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名
        */
        @NotBlank(message = "仓库名不能为空")
        @Size(max = 50,message = "仓库名最大长度不能超过50位")
        private String warehouseName;


    }


}