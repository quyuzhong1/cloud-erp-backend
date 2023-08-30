package com.common.business.dto;


import com.common.business.enums.PlatformApiEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDTO {
    private PlatformApiEnum platformApiEnum;

    private JobTaskDTO jobTaskDTO;

    public RequestDTO(JobTaskDTO jobTaskDTO, PlatformApiEnum platform) {
        this.platformApiEnum = platform;
        this.jobTaskDTO = jobTaskDTO;
    }
}
