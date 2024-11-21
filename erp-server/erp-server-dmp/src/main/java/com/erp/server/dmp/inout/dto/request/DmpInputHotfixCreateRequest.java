package com.erp.server.dmp.inout.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import lombok.Data;

/**
 * 输入创建快速任务请求参数
 * @author Administrator
 *
 */
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
    
    /**
     * 执行超时时间，单位秒
     */
    private Integer execTimeout;
    
    /**
     * 是否切割时间
     */
    private boolean splitFlag = false;

    /**
     * dmp_cfg_input_detail明细扩展参数
     */
    private String detailExtendJson;

    /**
     * 任务类型:
     * DmpInputTaskTaskTypeEnum
     */
    private String taskType;

    public String checkAndGetTaskType() {
        DmpInputTaskTaskTypeEnum taskTypeEnum = DmpInputTaskTaskTypeEnum.getByType(this.taskType);
        if (null != taskTypeEnum){
           return taskTypeEnum.getCode();
        }
        return DmpInputTaskTaskTypeEnum.HOTFIX.getCode();
    }
}
