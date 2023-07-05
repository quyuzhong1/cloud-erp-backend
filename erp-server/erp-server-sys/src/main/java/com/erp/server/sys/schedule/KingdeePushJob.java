package com.erp.server.sys.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysDeptService;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeSysUserInfoService;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysUserInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
                .in(SysDepartmentEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的部门");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSysDeptService.syncDataToKingdee(obj, obj.getSyncOperate());
        });

    }

    /**
     * 推送用户
     */
    @XxlJob("kingdeePushSysUserInfo")
    public void kingdeePushSysUserInfo() {
        List<SysUserInfoEntity> list = sysUserInfoService.lambdaQuery()
                .in(SysUserInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的用户");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSysUserInfoService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }


}
