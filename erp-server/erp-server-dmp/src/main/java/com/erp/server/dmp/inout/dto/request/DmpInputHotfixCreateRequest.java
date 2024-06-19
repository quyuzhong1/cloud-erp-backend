package com.erp.server.dmp.inout.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class DmpInputHotfixCreateRequest extends DmpInputCreateRequest{
	/**
	 * 输入明细信息id列表
	 */
	private List<String> cfgInputDetailIdList;
	
	/**
    * 拉取接口条件的开始时间
    */
    private LocalDateTime startTime;
    /**
    * 拉取接口条件的结束时间
    */
    private LocalDateTime endTime;
    
}
