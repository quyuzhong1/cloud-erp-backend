package com.erp.server.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.workflow.dto.ActivityDTO;
import com.erp.model.workflow.entity.ActHistoryActivityEntity;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-12
 */

public interface ActHistoryActivityService extends IService<ActHistoryActivityEntity> {

    void saveActivity(ActivityDTO activityDTO);

    String getProActivityId(String processInstanceId, String nowActivityId);

    List<ActHistoryActivityEntity> getByProcessId(String processId);
}
