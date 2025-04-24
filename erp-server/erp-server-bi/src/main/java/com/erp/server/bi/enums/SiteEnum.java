package com.erp.server.bi.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @Classname SiteEnum

 * @Date 2022-12-28 17:09
 * @Created by yl
 */
public enum SiteEnum {

    IT("it", "欧洲站"),
    ES("es", "欧洲站"),
    FR("fr", "欧洲站"),

    BE("be", "欧洲站"),
    TR("tr", "欧洲站"),
    DE("de", "欧洲站"),

    NL("nl", "欧洲站"),
    GB("gb", "欧洲站"),
    SE("se", "欧洲站"),

    PL("pl", "欧洲站"),
    US("us", "美国站"),
    JP("jp", "日本站");

    private String site;

    private String name;

    SiteEnum(String site, String name) {
        this.site = site;
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public String getName() {
        return name;
    }


    public static List<String> getSiteList(String name) {
        List<String> resultList = new ArrayList<>();
        for (SiteEnum item : SiteEnum.values()) {
            if (name.equals(item.getName())) {
                resultList.add(item.getSite());
            }
        }
        return resultList;
    }
}
