package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 平台拉取数据封装DTO
 *
 * @Author Cloud
 * @Date 2023/8/30 14:30
 **/

@Data
@NoArgsConstructor
public class PlatformDataDTO<T extends CleanBaseDTO,R extends UniqueDto> implements Serializable {
    /**
     * 平台源数据 用于保存mongo数据库，保持数据的原始性
     */
    private List<T> sourceData;

    /**
     * 目标数据， 用于发送到mq的数据，需要对数据进行清洗
     */
    private List<R> targetData;

    public PlatformDataDTO(List<T> download, List<R> convert) {
        this.sourceData = download;
        this.targetData = convert;
    }
}
