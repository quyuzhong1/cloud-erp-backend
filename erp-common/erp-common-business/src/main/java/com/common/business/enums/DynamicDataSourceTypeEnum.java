package com.common.business.enums;

import com.common.core.constant.EnumMessage;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
public enum DynamicDataSourceTypeEnum implements EnumMessage {

	POSTGRES("postgres","postgres数据源"),
	DORIS("doris","doris数据源"),
    ADS_DORIS("adsDoris","doris-ads数据源"),
    ARCHIVE_DORIS("archiveDoris","doris归档数据源"),
    ;

    private final String code;

    private final String name;

    DynamicDataSourceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static boolean isDoris(DynamicDataSourceTypeEnum dataSourceTypeEnum){
        return dataSourceTypeEnum == DynamicDataSourceTypeEnum.DORIS
                || dataSourceTypeEnum == DynamicDataSourceTypeEnum.ADS_DORIS
        		|| dataSourceTypeEnum == DynamicDataSourceTypeEnum.ARCHIVE_DORIS;
    }

    public static boolean isDorisByStr(String dataSourceTypeStr){
        return Arrays.asList(DynamicDataSourceTypeEnum.DORIS.getCode(),
                DynamicDataSourceTypeEnum.ADS_DORIS.getCode(),
        		DynamicDataSourceTypeEnum.ARCHIVE_DORIS.getCode())
                .contains(dataSourceTypeStr);
    }

    public static void main(String[] args) {
        System.out.println("打印");
        System.out.println(isDoris(null));
        System.out.println(isDorisByStr(""));
        System.out.println(isDorisByStr(null));

    }
}
