package com.erp.model.bi.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺下拉列表
 *
 * @Author Cloud
 * @Date 2022/12/19 11:32
 **/

@Data
public class ShopDropDownVO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopDropDownNameVO {
        /**
         * 店铺名称
         */
        private String name;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopDropDownIdVO extends ShopDropDownNameVO{

        /**
         * 店铺id
         */
        private String id;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        public ShopDropDownIdVO(String id, String name,Boolean disabled) {
            this.id = id ;
            this.disabled = disabled;
            this.setName(name);
        }
    }

}
