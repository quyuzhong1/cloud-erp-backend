package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 策略（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-10-12
*/
@Data
@NoArgsConstructor
public class CfgRuleOrderStrategyDTO implements Serializable {




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
        * 采购建议策略,true是，false否
        */
        private Boolean isSplit;
        /**
         * 是否合并SKU集中采购,true是，false否
         */
        private Boolean isMergeSku;

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


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否拆分组合品,true是，false否
        */
        @NotNull(message = "是否拆分组合品不能为空")
        private Boolean isSplit;

        /**
         * 是否合并SKU集中采购,true是，false否
         */
        @NotNull(message = "是否合并SKU集中采购不能为空")
        private Boolean isMergeSku;
    }


}