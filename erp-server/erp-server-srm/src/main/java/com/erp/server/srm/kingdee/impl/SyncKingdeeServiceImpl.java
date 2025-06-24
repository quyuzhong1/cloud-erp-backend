package com.erp.server.srm.kingdee.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.server.srm.kingdee.SyncKingdeeService;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 修改操作金蝶的状态
 * @Author Luo_WG
 * @Date 2023/5/31 14:39
 **/
@Service
public class SyncKingdeeServiceImpl implements SyncKingdeeService {

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Resource
    private PoReconciliationService poReconciliationService;



    @Override
    public void updateBusinessSyncKingdeeStatus(Map<String, Object> params) {
        //模块类型编码
        String code = (String)params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");

        //明细数据
        Object details = params.get("details");

        //采购对账单
        if (ApiModuleTypeEnum.PO_RECONCILIATION.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                poReconciliationDetailScmService.updateKingdeeDetailId(list);
                return;
            }
            poReconciliationService.updateSyncKingdeeId(businessId, syncKingdeeId);
        }

    }
}
