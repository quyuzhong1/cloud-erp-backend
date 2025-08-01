package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname BankDTO

 * @Date 2023-03-21 16:14
 * @Created by yl
 */
public class BankDTO  implements Serializable {


    /**
     * 添加或者修改银行
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddOrUpdateDTO{

        private String id;

        @NotBlank(message = "银行名称不能为空")
        @Size(max=50,message = "银行名称最大50字符")
        private String name;

        /**
         * 客服电话
         */
        private String servicesPhone;

        /**
         * 总部地址
         */
        private String headquarterAddress;

        /**
         * 是否禁用
         */
        private Boolean disabled;

    }




}
