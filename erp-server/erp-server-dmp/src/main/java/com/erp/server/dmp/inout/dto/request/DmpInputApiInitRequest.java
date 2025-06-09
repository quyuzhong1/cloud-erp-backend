package com.erp.server.dmp.inout.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 输入任务init状态api类型请求参数
 * @author Administrator
 *
 */
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
    
    private String apiType;
	private String requestParam; 
	
	private String nextLevelId;

    /**
     * 任务记录extend_json
     */
	private String taskExtendJson;
}
