package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.sys.service.SysDepartmentService;
import com.erp.server.sys.service.SysUserInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SysDepartmentService sysDepartmentService;

    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, Object> params) {
        //模块类型编码
        String code = (String)params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //更新状态
        String status = (String)params.get("status");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");

        //系统用户
        if (ApiModuleTypeEnum.SYS_USER_INFO.getCode().toString().equals(code)) {
            sysUserInfoService.updateSyncKingdeeStatus(Arrays.asList(businessId),status,syncKingdeeId);
        }
        //部门
        if (ApiModuleTypeEnum.SYS_DEPARTMENT.getCode().toString().equals(code)) {
            sysDepartmentService.updateSyncKingdeeStatus(Arrays.asList(businessId),status,syncKingdeeId);
        }
    }
}
