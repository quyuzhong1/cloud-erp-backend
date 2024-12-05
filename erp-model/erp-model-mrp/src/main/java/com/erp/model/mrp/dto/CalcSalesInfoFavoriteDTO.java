package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 试算关注表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Data
@NoArgsConstructor
public class CalcSalesInfoFavoriteDTO implements Serializable {


    /**
     * 新增
     */
    @Getter
    @Setter
    public static class AddDTO {
        /**
         * 试算id
         */
        @NotBlank(message = "试算模板id不能为空")
        private String cfgRuleCalcId;

    }


    /**
     * 新增
     */
    @Getter
    @Setter
    public static class CancelDTO {
        /**
         * 试算id
         */
        @NotBlank(message = "试算模板id不能为空")
        private String cfgRuleCalcId;

    }

}