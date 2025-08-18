package com.common.core.enums;

import lombok.Getter;

/**
 * @Classname CurrencyEnum

 * @Date 2022-08-09 10:31
 * @Created by yl
 */
@Getter
public enum CurrencyEnum {

    //有的第三方接口使用这个
//    RMB("RMB","人民币","¥"),

    CNY("CNY","人民币","¥"),
    CNH("CNH","离岸人民币","¥"),
    SKK("SKK","斯洛伐克克朗","Sk"),
    HKD("HKD","港币","HK$"),
    MOP("MOP","澳门元","MOP$"),
    USD("USD","美元","$"),
    EUR("EUR","欧元","€"),
    GBP("GBP","英镑","￡"),
    JPY("JPY","日元","Ұ"),
    AUD("AUD","澳大利亚元","A$"),
    NZD("NZD","纽元","NZ$"),
    CHF("CHF","瑞士法郎","CHF"),
    CAD("CAD","加拿大元","C$"),
    RUR("RUR","俄罗斯卢布","₽"),
    SGD("SGD","新加坡元","S$"),
    SAR("SAR","沙特阿拉伯里亚尔","SAR"),
    ZAR("ZAR","南非兰特","R"),
    TRL("TRL","土耳其里拉","₺"),
    SEK("SEK","瑞典克郎","Kr"),
    ATS("ATS","奥地利先令","S"),
    AWF("AWF","阿鲁巴岛弗罗林","ƒ"),
    AZM("AZM","阿塞拜疆马纳特","¤"),
    ARP("ARP","阿根廷比索","¤"),
    AON("AON","安哥拉宽扎","¤"),
    AMD("AMD","亚美尼亚德拉姆","¤"),
    ANG("ANG","安第列斯群岛盾","¤"),
    AFA("AFA","阿富汗尼","¤"),
    AED("AED","阿联酋迪拉姆","د.إ"),
    ALL("ALL","阿尔巴尼亚列克","¤"),
    BZD("BZD","洪都拉斯元","¤"),
    BRL("BRL","巴西里亚伊","¤"),
    BSD("BSD","巴哈马群岛元","¤"),
    BEF("BEF","比利时法郎","¤"),
    BTR("BTR","不丹卢比","¤"),
    BND("BND","文莱元","¤"),
    BGL("BGL","保加利亚列弗","¤"),
    BAK("BAK","波斯尼亚和黑塞哥维那波黑","¤"),
    BBD("BBD","巴巴多斯元","¤"),
    BWP("BWP","博茨瓦纳普拉","¤"),
    BOB("BOB","玻利维亚诺","¤"),
    BIF("BIF","布隆迪法郎","¤"),
    CYP("CYP","塞普路斯镑","¤"),
    CDF("CDF","刚果法郎","¤"),
    CVE("CVE","佛得角埃斯库多","¤"),
    CLP("AFA","智利比索","¤"),
    CRC("CRC","哥斯达黎加科郎","¤"),
    CZK("CZK","捷克克郎","¤"),
    COP("COP","哥伦比亚比索","¤"),
    DZD("DZD","阿尔及利亚第纳尔","¤"),
    DEM("DEM","德国马克","¤"),
    DOP("DOP","多美尼加比索","¤"),
    DKK("DKK","丹麦克郎","¤"),
    ECS("ECS","厄瓜多尔苏克雷","¤"),
    EGP("EGP","埃及镑","¤"),
    ETB("ETB","埃塞俄比亚比尔","¤"),
    FRF("FRF","法国法郎","¤"),
    FIM("FIM","芬兰马克","¤"),
    FJD("FJD","斐济元","＄"),
    GYD("GYD","圭亚那元","¤"),
    GRD("GRD","希腊德拉克马","¤"),
    GNF("GNF","几内亚法郎","¤"),
    GTQ("GTQ","危地马拉格查尔","¤"),
    GHC("GHC","加纳塞地","¤"),
    GIP("GIP","直布罗陀镑","¤"),
    GMD("GMD","冈比亚达拉西","¤"),
    GEL("GEL","乔治亚拉里","¤"),
    HUF("HUF","匈牙利福林","¤"),
    HTG("HTG","海地古德","¤"),
    HRK("HRK","克罗地亚库纳","¤"),
    TJR("TJR","塔吉克斯坦卢布","¤"),
    TMM("TMM","土库曼斯坦马纳特","¤"),
    ITL("ITL","意大利里拉","¤"),
    IRR("IRR","伊朗里亚尔","¤"),
    ILS("ILS","以色列新谢克尔","¤"),
    IEP("IEP","爱尔兰镑","¤"),
    IDR("IDR","印尼卢比","¤"),
    THB("THB","泰国泰铢","฿"),
    INR("INR","印度卢比","¤"),
    SLL("SLL","塞拉利昂利昂","¤"),
    SOS("SOS","索马里先令","¤"),
    SYP("SYP","叙利亚镑","¤"),
    SZL("SZL","斯威士兰里兰吉尼","¤"),
    TND("TND","突尼斯第纳尔","¤"),
    TOP("TOP","汤加潘加","¤"),
    TZS("TZS","坦桑尼亚先令","¤"),
    UAH("UAH","乌克兰格里夫纳","¤"),
    UGX("UGX","乌干达先令","¤"),
    UYU("UYU","乌拉圭比索","¤"),
    UZS("UZS","乌兹别克斯坦苏姆","¤"),
    IQD("IQD","伊拉克第纳尔","¤"),
    ISK("ISK","冰岛克郎","¤"),
    JOD("JOD","约旦第纳尔","¤"),
    JMD("JMD","牙买加元","¤"),
    KES("KES","肯尼亚先令","¤"),
    KWD("KWD","科威特第纳尔","¤"),
    KZT("KZT","哈萨克斯坦坚戈","¤"),
    KRW("KRW","韩圆","¤"),
    KPW("KPW","朝鲜元","¤"),
    LAK("LAK","老挝基普","¤"),
    LRD("LRD","利比里亚元","¤"),
    LUF("LUF","卢森堡法郎","¤"),
    LYD("LYD","利比亚第纳尔","¤"),
    LTL("LTL","立陶宛立特","¤"),
    LKR("LKR","斯里兰卡卢比 ","¤"),
    LBP("LBP","黎巴嫩镑","¤"),
    LSL("LSL","莱索托洛蒂","¤"),
    LVL("LVL","拉脱维亚拉特","¤"),
    MTL("MTL","马耳他里拉","¤"),
    MNT("MNT","蒙古图格里克","¤"),
    MKD("MKD","马其顿第纳尔","¤"),
    MAD("MAD","摩洛哥迪拉姆","¤"),
    MDL("MDL","摩尔多瓦列伊","¤"),
    MUR("MUR","毛里求斯卢比","¤"),
    MXP("MXP","墨西哥比索","¤"),
    MZM("MZM","莫桑比克美提卡","¤"),
    MVR("MVR","马尔代夫拉菲亚","¤"),
    MRO("MRO","毛里塔尼亚乌吉亚","¤"),
    NOK("NOK","挪威克郎","¤"),
    NGN("NGN","尼日利亚奈拉","¤"),
    NAD("NAD","纳米比亚元","¤"),
    NPR("NPR","尼泊尔卢比","¤"),
    NIO("NIO","尼加拉瓜科多巴","¤"),
    OMR("OMR","阿曼里亚尔","¤"),
    PYG("PYG","巴拉圭瓜拉尼","¤"),
    PHP("PHP","菲律宾比索","¤"),
    PKR("PKR","巴基斯坦卢","¤"),
    PAB("PAB","巴拿马巴波亚","¤"),
    PEN("PEN","秘鲁新索尔","¤"),
    PLN("PLN","波兰兹罗提","¤"),
    VEB("VEB","委内瑞拉玻利瓦尔","¤"),
    VND("VND","越南盾","¤"),
    VUV("VUV","瓦努阿图瓦","¤"),
    YER("YER","也门里亚尔","¤"),
    YUN("YUN","南斯拉夫新第纳尔","¤"),
    ZMK("ZMK","赞比亚","¤"),
    ZWD("ZWD","津巴布韦元","¤"),
    QAR("QAR","卡塔尔利尔","¤"),
    ROL("ROL","罗马尼亚列伊","¤"),
    RWF("RWF","卢旺达法郎","¤"),
    SDD("SDD","苏丹第纳尔","¤"),
    SIT("SIT","斯洛文尼亚托拉尔","¤"),
    ;


    private String currencyCode;
    private String currencyName;
    private String currencySymbol;

    CurrencyEnum(String currencyCode, String currencyName, String currencySymbol) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.currencySymbol = currencySymbol;

    }
    public static CurrencyEnum getByCode(String currencyCode) {
        CurrencyEnum[] values = values();
        for (CurrencyEnum value : values) {
            if (value.currencyCode.equals(currencyCode)) {
                return value;
            }
        }
        return null;
    }

    public static String getSymbolByCode(String currencyCode) {
        CurrencyEnum[] values = values();
        for (CurrencyEnum value : values) {
            if (value.currencyCode.equals(currencyCode)) {
                return value.currencySymbol;
            }
        }
        return null;
    }

    public static String getNameByCode(String currencyCode) {
        CurrencyEnum[] values = values();
        for (CurrencyEnum value : values) {
            if (value.currencyCode.equals(currencyCode)) {
                return value.currencyName;
            }
        }
        return null;
    }

    public static CurrencyEnum getByNameOrCode(String currency) {
        CurrencyEnum[] values = values();
        for (CurrencyEnum value : values) {
            if (value.currencyCode.equals(currency) || value.currencyName.equals(currency)) {
                return value;
            }
        }
        return null;
    }
}
