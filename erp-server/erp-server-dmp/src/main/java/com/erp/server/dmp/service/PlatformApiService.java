package com.erp.server.dmp.service;

import com.common.business.service.SuperService;
import com.erp.model.dmp.entity.PlatformApiEntity;

import java.util.List;

public interface PlatformApiService extends SuperService<PlatformApiEntity> {

    /**
     * 查询未生成任务的api
     * @Author Luo_WG
     * @Date 2022/11/8 15:33
     * @return java.util.List<com.erp.server.entity.PlatformApiEntity>
     **/
    List<PlatformApiEntity> listForDisabled(String dictPlatform);
}
