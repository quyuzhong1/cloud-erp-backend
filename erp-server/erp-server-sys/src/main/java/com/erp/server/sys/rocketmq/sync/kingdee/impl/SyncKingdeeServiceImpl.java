package com.erp.server.sys.rocketmq.sync.kingdee.impl;

import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.sys.rocketmq.sync.kingdee.SyncKingdeeService;
import com.erp.server.sys.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/10 14:43
 */
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private SysUserInfoService sysUserInfoService;


    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;

    @Resource
    private KingdeePostService kingdeePostService;

    @Resource
    private KingdeeUserRefPostService kingdeeUserRefPostService;

    @Resource
    private KingdeeOperatorRefPostService kingdeeOperatorRefPostService;

    @Resource
    private DictGlobalAreaService dictGlobalAreaService;

    @Resource
    private DictCountryService dictCountryService;

    @Resource
    private DictCityService dictCityService;

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

        //金蝶编码
        String syncKingdeeCode = (String)params.get("kingdeeCode");

        //系统用户
        if (ApiModuleTypeEnum.SYS_USER_INFO.getCode().toString().equals(code)) {
            sysUserInfoService.updateSyncKingdeeId(businessId,syncKingdeeId);
            return;
        }
        //部门
        if (ApiModuleTypeEnum.SYS_DEPARTMENT.getCode().toString().equals(code)) {
            kingdeeDepartmentService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }

        //岗位
        if (ApiModuleTypeEnum.SYS_POST.getCode().toString().equals(code)) {
            kingdeePostService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }

        //员工任岗
        if (ApiModuleTypeEnum.SYS_USER_POST.getCode().toString().equals(code)) {
            kingdeeUserRefPostService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }

        //业务员
        if (ApiModuleTypeEnum.KINGDEE_OPERATOR.getCode().toString().equals(code)) {
            kingdeeOperatorRefPostService.updateSyncKingdeeId(businessId,syncKingdeeId);
            return;
        }

        //区域
        if (ApiModuleTypeEnum.GLOBAL_AREA.getCode().toString().equals(code)) {
            dictGlobalAreaService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }

        //国家
        if (ApiModuleTypeEnum.COUNTRY.getCode().toString().equals(code)) {
            dictCountryService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }

        //省城市
        if (ApiModuleTypeEnum.PROVINCE_CITY.getCode().toString().equals(code)) {
            dictCityService.updateSyncKingdeeId(businessId,syncKingdeeId,syncKingdeeCode);
            return;
        }
    }
}
