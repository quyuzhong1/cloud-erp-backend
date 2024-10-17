package com.erp.model.oms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单处理规则枚举
 */
@AllArgsConstructor
@Getter
public enum RuleOrderHandleEnum {
    ;
    @Getter
    @AllArgsConstructor
    public enum ProvinceRuleContentEnum implements EnumMessage {
        REPLACE_WITH_CITY("replaceWithCity","若收货地址省/州为空,使用城市信息进行填充"),
        REPLACE_BLANK("replaceBlank","省州推送物流商下单为空"),
        CUSTOM_REPLACE("customReplace","自定义替换"),
        ;
        private final String code;
        private final String name;

    }

    @Getter
    @AllArgsConstructor
    public enum CityRuleContentEnum implements EnumMessage {
        REPLACE_WITH_PROVINCE("replaceWithProvince","若收货地址城市为空,使用州省信息进行填充"),
        REPLACE_BLANK("replaceBlank","城市推送物流商下单为空"),
        CUSTOM_REPLACE("customReplace","自定义替换"),
        ;
        private final String code;
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum Address1FilterEnum implements EnumMessage {
        BLANK(" ","空格"),
        SYMBOL_1("@","@"),
        SYMBOL_2("#","#"),
        SYMBOL_3("+","+"),
        SYMBOL_4("-","-"),
        SYMBOL_5("*","*"),
        SYMBOL_6("&","&"),
        SYMBOL_7("/","/"),
        SYMBOL_8("(","("),
        SYMBOL_9(")",")"),
        ;
        private final String code;
        private final String name;

    }

    @Getter
    @AllArgsConstructor
    public enum PhoneFilterEnum implements EnumMessage {
        BLANK(" ","空格"),
        SYMBOL_1("@","@"),
        SYMBOL_2("#","#"),
        SYMBOL_3("+","+"),
        SYMBOL_4("-","-"),
        SYMBOL_5("*","*"),
        SYMBOL_6("&","&"),
        SYMBOL_7("/","/"),
        SYMBOL_8("(","("),
        SYMBOL_9(")",")"),
        ;
        private final String code;
        private final String name;

    }
    @Getter
    @AllArgsConstructor
    public enum ZipCodeFilterEnum implements EnumMessage {
        BLANK(" ","空格"),
        SYMBOL_1("@","@"),
        SYMBOL_2("#","#"),
        SYMBOL_3("+","+"),
        SYMBOL_4("-","-"),
        SYMBOL_5("*","*"),
        SYMBOL_6("&","&"),
        SYMBOL_7("/","/"),
        SYMBOL_8("(","("),
        SYMBOL_9(")",")"),
        ;
        private final String code;
        private final String name;

    }

    @Getter
    @AllArgsConstructor
    public enum ReceiveFillRuleContentEnum implements EnumMessage {
        FILL_WITH_CUSTOMER_NAME("fillWithCustomerName","按照买家全名填充"),
        CUSTOMIZE("customize","自定义"),
        ;
        private final String code;
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum ReceiveFilterEnum implements EnumMessage {
        BLANK(" ","空格"),
        SYMBOL_1("@","@"),
        SYMBOL_2("#","#"),
        SYMBOL_3("+","+"),
        SYMBOL_4("-","-"),
        SYMBOL_5("*","*"),
        SYMBOL_6("&","&"),
        SYMBOL_7("/","/"),
        SYMBOL_8("(","("),
        SYMBOL_9(")",")"),
        ;
        private final String code;
        private final String name;

    }
}
