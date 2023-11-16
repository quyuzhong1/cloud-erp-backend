package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 唯一标识
 *
 * @Author Cloud
 * @Date 2023/8/31 14:48
 **/

@Data
@NoArgsConstructor
public class UniqueDto extends DmpSyncTaskIdDTO implements Serializable {

    /**
     * 唯一标识 必填
     */
    private String uniqueId;

    /**
     * 平台code
     */
    private String platform;


    public static UniqueDto getUniqId(String uniqueId) {
        UniqueDto uniqueDto = new UniqueDto();
        uniqueDto.setUniqueId(uniqueId);
        return uniqueDto;
    }
}
