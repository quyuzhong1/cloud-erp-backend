package com.common.business.enums;

import com.common.core.constant.EnumMessage;

/**
 * @ClassName PicExtenstionEnum
 * @Author: zhangchunlin
 * @Date: 2023/6/21 9:04
 * @Description:
 */

public enum PicFormatEnum implements EnumMessage {

    JPG("jpg", "jpg"),
    BMP("bmp", "bmp"),
    PNG("png", "png"),
    GIF("gif", "gif");


    private String code;

    private String name;

    PicFormatEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
