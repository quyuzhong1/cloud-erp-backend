package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CfgSettingDTO {
    /**
     * id
     */
    private String id;

    /**
     * Key值
     */
    private String key;

    /**
     * json数据
     */
    private String dataJson;

    /**
     * 是否禁用
     */
    private Boolean disabled;


    @Getter
    @Setter
    public static class ReplenishmentDays {
        /**
         * 开始
         */
        private Integer start;
        /**
         * 结束
         */
        private Integer end;
    }
}
