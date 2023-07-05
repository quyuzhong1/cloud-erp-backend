package com.erp.model.dmp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 清洗基础数据
 *
 * @Author Cloud
 * @Date 2023/6/13 16:12
 **/
@Getter
@Setter
public class CleanBaseDTO {
    /**
     * 清洗数据 0 未清洗 1 清洗中 2 清洗完成
     */
    private Integer isClean;

    /**
     * 上次推送时间
     */
    private LocalDateTime lastPushTime;

    /**
     * 数据下载时间
     */
    private LocalDateTime downloadTime;
}
