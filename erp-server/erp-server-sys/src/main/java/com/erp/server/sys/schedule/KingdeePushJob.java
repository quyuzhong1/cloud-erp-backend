package com.erp.server.sys.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysUserInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2023/1/12 15:19
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeePushJob {

   @Resource
   private SysDepartmentService sysDepartmentService;

    @Resource
    private SyncKingdeeSysDeptService syncKingdeeSysDeptService;

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SyncKingdeeSysUserInfoService syncKingdeeSysUserInfoService;


    /**
     * 推送部门
     */
    @XxlJob("kingdeePushSysDepartment")
    public void kingdeePushSysDepartment() {
        List<SysDepartmentEntity> list = sysDepartmentService.lambdaQuery()
                .in(SysDepartmentEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的部门");
            return;
        }
        list.forEach(obj->{
            //syncKingdeeSysDeptService.syncDataToKingdee(obj, obj.getSyncOperate());
        });

    }

    /**
     * 推送用户
     */
    @XxlJob("kingdeePushSysUserInfo")
    public void kingdeePushSysUserInfo() {
        List<SysUserInfoEntity> list = sysUserInfoService.lambdaQuery()
                .in(SysUserInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SysUserInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SysUserInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的用户");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSysUserInfoService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("用户【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("用户【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }


}
