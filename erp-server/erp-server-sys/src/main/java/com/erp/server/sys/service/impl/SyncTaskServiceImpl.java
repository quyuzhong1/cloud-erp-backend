package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.SyncTaskService;
import com.erp.server.sys.service.SysUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 同步任务实现
 * @date 2023/10/30 11:44
 */
@Resource
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;

    @Override
    public void findDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();

        switch (sourceType) {
            case SYS_USER_INFO:
                syncSysUser(sourceDetailList);
                return;
            default:
                return;
        }
    }

    /**
     * @description: 同步用户
     * @author Will
     * @date: 2023/10/30 11:22
     * @param sourceDetailList
     */
    private void syncSysUser (List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<SysUserInfoEntity> list = sysUserInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncSysUser >>>> 未找到数据！");
            return;
        }
        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            SysUserInfoEntity sysUserInfoEntity = list.stream().filter(obj -> obj.getUid().equals(syncParamDetailDTO.getSourceId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(sysUserInfoEntity)) {
                continue;
            }
            syncKingdeeSysUserInfoService.syncDataToKingdee(sysUserInfoEntity,syncParamDetailDTO.getSyncOperate());
        }
    }
}
