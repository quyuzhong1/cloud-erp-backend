package com.erp.server.dmp.inout.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.common.business.wrapper.QueryParam;

import lombok.Data;

/**
 * 输出创建任务请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputHotfixCreateRequest extends DmpOutputCreateRequest{
	private List<QueryParam> queryParams;

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

    /**
     * 任务类型:
     * DmpOutputTaskTaskTypeEnum
     */
    private String taskType;
}
