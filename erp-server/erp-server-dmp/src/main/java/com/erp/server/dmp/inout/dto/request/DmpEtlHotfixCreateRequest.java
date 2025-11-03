package com.erp.server.dmp.inout.dto.request;

import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ETL创建快速任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpEtlHotfixCreateRequest extends DmpEtlCreateRequest{
	
	/**
    * 拉取接口条件的开始时间
    */
    private LocalDateTime startTime;
    /**
    * 拉取接口条件的结束时间
    */
    private LocalDateTime endTime;
    
    /**
     * 执行超时时间，单位秒
     */
    private Integer execTimeout;
    
    /**
     * 是否切割时间
     */
    private boolean splitFlag = false;

    /**
     * 下次执行任务时间
     */
    private LocalDateTime nextExecTime;

    /**
     * 扩展json
     */
    private String extendJson;
}
