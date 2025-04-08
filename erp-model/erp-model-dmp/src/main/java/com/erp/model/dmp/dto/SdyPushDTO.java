package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class SdyPushDTO {

    private Set<String> cfgOutputIds;
    
    private boolean allFlag = false;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String bizType;
}
