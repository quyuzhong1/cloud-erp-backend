package com.erp.server.dmp.push.service.common.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.common.DmpSyncCommonService;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import com.erp.server.dmp.service.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @CreateTime: 2023-06-27  17:32
 * @Author: zhangchunlin
 */
@Slf4j
@Service
public class DmpSyncCommonServiceImpl implements DmpSyncCommonService {

    @Autowired
    private PlatformService platformService;

    @Autowired
    private ApiPlmSyncLogService apiPlmSyncLogService;

    @Override
    public PlatformEntity getPlatformEntity(String bizId, Integer type) {
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.MABANG.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.error("第三方平台【{}】未找到！", PlatformEnum.MABANG.getDesc());
            this.insertLogWriteBackSyncMabangStatus(platformEntity, bizId, "", StrUtil.format("第三方平台【{}】未找到！", PlatformEnum.MABANG.getDesc()), type, ApiSendStatusEnum.FAILURE.getCode());
        }
        return platformEntity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertLogWriteBackSyncMabangStatus(PlatformEntity platformEntity, String businessId, String jsonData, String msg, Integer type, Integer status) {
        //新增日志
        insertSyncLog(platformEntity, businessId, jsonData, msg, type, status);
    }

    @Override
    public void insertSyncLog(PlatformEntity platformEntity, String businessId, String jsonData, String msg, Integer type, Integer status) {
        //新增日志信息
        ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
        apiPlmSyncLogDTO.setApiPlatformId(ObjectUtils.isEmpty(platformEntity) ? "" : platformEntity.getId());
        apiPlmSyncLogDTO.setApiPlatform(ObjectUtils.isEmpty(platformEntity) ? "" : platformEntity.getName());
        apiPlmSyncLogDTO.setModuleType(type);
        apiPlmSyncLogDTO.setBusinessId(businessId);
        apiPlmSyncLogDTO.setStatus(status);
        apiPlmSyncLogDTO.setMsg(msg);
        apiPlmSyncLogDTO.setRequestParamJson(jsonData);
        apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
    }

    @Override
    public ApiPlmSyncLogEntity findLog(PlatformEntity platformEntity, String businessId, Integer type) {
        return apiPlmSyncLogService.find(platformEntity.getName(),type, businessId);
    }

    @Override
    public void updateLog(String id, String requestParam, String msg) {
        apiPlmSyncLogService.updateLog(id, requestParam, msg);
    }

}