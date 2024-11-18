package com.erp.model.tms.vo.request;

import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 物流发件人信息
 */
@Data
@NoArgsConstructor
public class SenderInfo  implements Serializable {
        //发件人id
        private String actId;
        //发件人税号
        private String taxNumber;


        private String id;
        /**
         * 名称
         */
        private String name;
        /**
         * 类型
         */
        private LogisticsAddressTypeEnum type;
        /**
         * 公司名
         */
        private String companyName;
        /**
         * 联系人
         */
        private String contact;
        /**
         * 邮箱
         */
        private String email;
        /**
         * 电话
         */
        private String telNumber;


        /**
         * 国家
         */
        private String country;

        /**
         * 国家
         */
        private String countryName;



        /**
         * 省
         */
        private String provinceName;
        /**
         * 城市
         */
        private String cityName;


        /**
         * 区
         */
        private String districtName;


        /**
         * 详细地址1
         */
        private String addressFirst;
        /**
         * 详细地址2
         */
        private String addressSecond;
        /**
         * 邮编
         */
        private String zipCode;
}
