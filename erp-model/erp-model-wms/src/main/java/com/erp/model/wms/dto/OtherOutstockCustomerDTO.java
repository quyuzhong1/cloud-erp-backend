package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 15:04
 */
@Data
@NoArgsConstructor
public class OtherOutstockCustomerDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 客户名称
         */
        @Size(max = 50,message = "客户名称不能大于255字符")
        private String name;

        /**
         * 收货地址
         */
        @Size(max = 50,message = "收货地址不能大于100字符")
        private String  receiveAddress;

        /**
         * 收货人
         */
        @Size(max = 20,message = "收货人不能大于20字符")
        private String receiverName;

        /**
         * 联系电话
         */
        @Size(max = 20,message = "联系电话不能大于20字符")
        private String telNumber;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "客户信息主键id不能为空")
        private String id;
    }

}
