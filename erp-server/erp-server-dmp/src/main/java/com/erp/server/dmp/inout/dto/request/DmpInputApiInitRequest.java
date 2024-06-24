package com.erp.server.dmp.inout.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DmpInputApiInitRequest extends DmpInputInitRequest{
	/**
    * 拉取接口条件的开始时间
    */
    private LocalDateTime startTime;
    /**
    * 拉取接口条件的结束时间
    */
    private LocalDateTime endTime;
}
