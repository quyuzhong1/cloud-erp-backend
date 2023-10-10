package com.common.business.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
}