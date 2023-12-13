package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lambda
 * @Classname DictEnum
 * @Description 平台字典表
 * @Date 2023-08-21 14:15
 */
public enum PlatformDictEnum implements EnumMessage {
    OFFLINECELEBRITY("Offlinecelebrity","线下发网红","","023"),
    AMAZON("Amazon", "Amazon", "亚马逊", "100"),
    SHOPIFY("Shopify", "Shopify", "Shopify", "004"),
    ALI_EXPRESS("AliExpress", "速卖通", "无忧物流[速卖通]", "002"),
    SHOPEE("Shopee", "Shopee", "虾皮", "003"),
    NASDAQ_JD("JD", "京东", "京东", "007"),
    WALMART("Walmart", "Walmart", "沃尔玛", "018"),
    TIK_TOK_CN("TikTokCn", "抖音/今日头条/鲁班", "抖音中国", "013"),
    TAO_BAO("TaoBao", "淘宝", "淘宝", "005"),
    ALIBABA("Alibaba", "阿里巴巴", "Alibaba", "008"),
    YOU_ZAN("YouZan", "有赞微商城", "有赞微商城", "015"),
    OTHER_PLATFORM("Other", "Other", "其他平台", "999"),
    B2B_INTERNAL("B2B_INTERNAL", "B2B线下-国内", "B2B", "020"),
    B2B_FOREIGN("B2B_FOREIGN", "B2B线下-国外", "B2B", "021"),
    LITTLE_RED_BOOK("RedBook", "小红书", "RED", "014"),
    PDD("PDD", "拼多多", "Temu", "009"),
    TMALL("Tmall", "天猫", "Tmall", "006"),
    SOP("JDZY", "京东自营厂送", "京东自营", "007"),
    //物流平台
    DSF("DSF", "递四方", "递四方(新)", "41"),
    SF_EXPRESS("EXPRESS", "顺丰-丰桥", "顺丰国内物流", "42"),
    UBI("UBI", "UBI", "UBI物流平台", "43"),
    TRACK123("TRACK123", "track123", "track123物流平台", "44"),
    YAN_WEN("YanWen", "燕文物流(新)", "燕文物流(新)", "45"),
    WEI_SHI("WeiShi", "深圳前海纬狮物流网络科技有限公司", "深圳前海纬狮物流网络科技有限公司", "46"),
    YUN_TU("YunTu", "云途(新)", "云途(新)", "47"),
    TONG_YOU("TongYou", "去发货(通邮)", "去发货(通邮)", "48"),
    //第三方仓
    GOOD_CANG("goodcang", "谷仓", "谷仓", "49"),
    IML("iml", "艾姆勒", "艾姆勒", "50"),
        ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;

    private String desc;

    private String kingdeeCode;

    public String getDesc() {
        return desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }

    PlatformDictEnum(String code, String name, String desc, String kingdeeCode) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.kingdeeCode = kingdeeCode;
    }


    public static PlatformDictEnum getByCode(String code) {
        PlatformDictEnum[] values = values();
        for (PlatformDictEnum value : values) {
            if (value.code.equals(code) ) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByName(String name) {
        PlatformDictEnum[] values = values();
        for (PlatformDictEnum value : values) {
            if (value.name.equals(name)) {
                return value.getName();
            }
        }
        return "";
    }

    public static PlatformDictEnum getByName(String name) {
        PlatformDictEnum[] values = values();
        for (PlatformDictEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 已接入平台列表
     */
    public static List<String> hasConnectionPlatform(){
        return Stream.of(AMAZON, ALI_EXPRESS, SHOPIFY, SHOPEE, WALMART)
                .map(PlatformDictEnum::getCode)
                .collect(Collectors.toList());

    }

    /**
     * 平台列表名称
     */
    public static List<String> hasConnectionPlatformName(){
        return Stream.of(AMAZON, ALI_EXPRESS, SHOPIFY, SHOPEE, WALMART)
                .map(PlatformDictEnum::getDesc)
                .collect(Collectors.toList());

    }
}
