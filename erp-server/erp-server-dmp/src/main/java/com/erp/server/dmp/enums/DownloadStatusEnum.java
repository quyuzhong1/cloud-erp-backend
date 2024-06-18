package com.erp.server.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;


/**
 * 下载状态
 *
 * @author Jim
 */
@Getter
@AllArgsConstructor
public enum DownloadStatusEnum {

    ERROR(-1, "异常数据无法更新"),
    WAIT(0, "详情数据待下载"),
    FINISH(1, "详情数据已下载"),


    ;

    @EnumValue
    private final Integer code;
    private final String name;


    /**
     * 根据code获取枚举
     */
    public static DownloadStatusEnum getByCode(Integer code) {
        return Arrays.stream(values()).filter(value -> value.getCode().equals(code))
                .findFirst().orElse(null);
    }


}
