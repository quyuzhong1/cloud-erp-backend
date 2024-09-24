package com.erp.server.dmp.inout.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 输出创建子类任务请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DmpOutputChildCreateRequest extends DmpOutputCreateRequest {
    /**
     * 输入明细信息下一层级id列表
     */
    private List<String> nextLevelIdList;

    /**
     * 拉取接口条件的开始时间
     */
    private LocalDateTime startTime;

    /**
     * 拉取接口条件的结束时间
     */
    private LocalDateTime endTime;

    /**
     * 父任务id
     */
    private String parentOutputTaskId;
}
