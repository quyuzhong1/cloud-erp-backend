package com.erp.model.dmp.dto;


import com.erp.model.dmp.enums.PlatformApiEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDTO {
    private PlatformApiEnum platformApiEnum;

    private JobTaskDTO jobTaskDTO;
}
