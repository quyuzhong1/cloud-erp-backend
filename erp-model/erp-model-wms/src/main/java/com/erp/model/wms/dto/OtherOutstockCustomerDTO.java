package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
        private String name;
        /**
         * 收货地址
         */
        private String  receiveAddress;
        /**
         * 收货人
         */
        private String    receiverName;
        /**
         * 联系电话
         */
        private String   telNumber;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends AddDTO {
        /**
         * 主键id
         */
        private String id;
    }

}
