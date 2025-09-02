package com.common.business.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Lambda
 * @Classname LogisticsPlatformEnum1
 * @Description TODO
 * @Date 2023-11-09 16:14
 * @Created by yl
 */
public enum LogisticsPlatformEnum implements EnumMessage {
    SHOPEE("Shopee", "Shopee", "虾皮", "003","Y","N"),
    AMAZON("Amazon", "Amazon", "亚马逊", "100","Y","N"),
    SHOPIFY("Shopify", "Shopify", "Shopify", "004","Y","N"),
    WALMART("Walmart", "Walmart", "沃尔玛", "018","Y","N"),
    MERCADOLIBRE("mercadolibre", "美客多-全球站", "美客多-全球站", "","Y","N"),
    MERCADOLIBRE_LOCAL("mercadolibreLocal", "美客多-本土站", "美客多-本土站", "","Y","N"),
    TIK_TOK("TikTok", "TikTok", "TikTok", "","Y","N"),
    TIK_TOK_FULLY("TikTokFully", "TikTok全托管", "TikTok全托管", "","Y","N"),
    AMZ_MULTI_CHANNEL("AmazonMultiChannel", "亚马逊多渠道发货", "亚马逊", "","N","N"),
    //物流平台
    DSF("DSF", "递四方", "递四方(新)", "","Y","Y"),
    SF_EXPRESS("EXPRESS", "顺丰-丰桥", "顺丰国内物流", "","Y","N"),
    ALI_EXPRESS("AliExpress", "速卖通", "无忧物流[速卖通]", "002","Y","N"),
    UBI("UBI", "UBI", "UBI物流平台", "","Y","N"),
    TRACK123("TRACK123", "track123", "track123物流平台", "","N","N"),
    YAN_WEN("YanWen", "燕文物流(新)", "燕文物流(新)", "","Y","Y"),
    WEI_SHI("WeiShi", "纬狮", "深圳前海纬狮物流网络科技有限公司", "","Y","N"),
    YUN_TU("YunTu", "云途(新)", "云途(新)", "","Y","N"),
    TONG_YOU("TongYou", "去发货(通邮)", "去发货(通邮)", "","Y","Y"),
    GOOD_CANG(OmsPlatformEnum.OMS_GOOD_CANG.getCode(), OmsPlatformEnum.OMS_GOOD_CANG.getName(), "谷仓", "","",""),
    AN_TU(OmsPlatformEnum.OMS_ANTU.getCode(), OmsPlatformEnum.OMS_ANTU.getName(), "安兔", "","",""),
    IML(OmsPlatformEnum.OMS_IML.getCode(), OmsPlatformEnum.OMS_IML.getName(), "艾姆勒", "","",""),
    BAO_HONG("BaoHong", "保宏", "保宏", "","Y","N"),
    BaTong("BaTong", "巴通", "巴通", "","",""),
    ANTU(OmsPlatformEnum.OMS_ANTU.getCode(), OmsPlatformEnum.OMS_ANTU.getName(), "安兔", "","",""),
    SPT(OmsPlatformEnum.OMS_SPT.getCode(), OmsPlatformEnum.OMS_SPT.getName(), "速派通", "","",""),
    JIFENG(OmsPlatformEnum.JIFENG.getCode(), OmsPlatformEnum.JIFENG.getName(), "极风", "","",""),

    CAINIAO(OmsPlatformEnum.CAI_NIAO.getCode(), OmsPlatformEnum.CAI_NIAO.getName(), "菜鸟仓", "","",""),

    WEI_SHI_WAREHOUSE("weishi", "纬狮海外仓", "纬狮海外仓", "","Y","N"),
    DA_MAI("damai", "大卖仓", "大卖仓", "","Y","N"),
    ;


    @JsonValue
    @EnumValue
    private String code;

    private String name;

    private String desc;

    private String kingdeeCode;

    private String printLabel;

    private String printDelivery;

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

    public String getPrintLabel() {
        return printLabel;
    }

    public String getPrintDelivery() {
        return printDelivery;
    }

    LogisticsPlatformEnum(String code, String name, String desc, String kingdeeCode, String printLabel, String printDelivery){

        this.code = code;
        this.name = name;
        this.desc = desc;
        this.kingdeeCode = kingdeeCode;
        this.printLabel = printLabel;
        this.printDelivery = printDelivery;
    }


    public static LogisticsPlatformEnum getByCode(String code) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.code.equals(code) ) {
                return value;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value.getName();
            }
        }
        return "";
    }
    public static String getDescByCode(String code) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.code.equals(code)) {
                return value.getDesc();
            }
        }
        return "";
    }
    public static String getNameByName(String name) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value.getName();
            }
        }
        return "";
    }

    public static LogisticsPlatformEnum getByName(String name) {
        LogisticsPlatformEnum[] values = values();
        for (LogisticsPlatformEnum value : values) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
