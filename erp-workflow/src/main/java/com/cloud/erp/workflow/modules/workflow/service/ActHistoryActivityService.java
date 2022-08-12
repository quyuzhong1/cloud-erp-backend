package com.cloud.erp.workflow.modules.workflow.service;

import com.cloud.erp.workflow.modules.workflow.dto.ActivityDTO;
import com.cloud.erp.workflow.modules.workflow.entity.ActHistoryActivityEntity;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
