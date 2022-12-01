package com.erp.server.dmp.entity.dto;

import com.erp.server.dmp.enums.PlatformApiEnum;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDTO {
    private PlatformApiEnum platformApiEnum;

    private JobTaskDTO jobTaskDTO;
}
