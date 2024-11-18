package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

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
        @Size(max = 19, message = "试算id最大长度不能超过19位")
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
        @Size(min = 1, message = "试算模板id不能为空")
        private List<String> cfgRuleCalcId;

    }

}