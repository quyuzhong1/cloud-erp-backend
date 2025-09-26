package com.erp.server.srm.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.erp.model.srm.entity.PayableInfoEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.srm.kingdee.SyncKingdeePayableInfoService;
import com.erp.server.srm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 修改操作金蝶的状态
 * @Author Luo_WG
 * @Date 2023/5/31 14:39
 **/
@Service
@Slf4j
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;

    @Resource
    private PoReconciliationService poReconciliationService;

    @Resource
    private SyncKingdeePayableInfoService syncKingdeePayableInfoService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private PayableInfoService payableInfoService;

    @Resource
    private PayableDetailService payableDetailService;

    @Override
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case PAYABLE_INFO:
                resultList = syncPayableInfo(sourceDetailList);
                break;
            default:
                break;
        }
		return resultList;
    }


    /**
     * 其他出库单
     * @param sourceDetailList
     */
    private Map<String , Map<String, Object>> syncPayableInfo(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<PayableInfoEntity> list = payableInfoService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncPayableInfo >>>> 未找到数据！");
            return resultList;
        }
        //明细信息
        List<PayableDetailEntity> detailList = payableDetailService.listMainIdList(sourceIdList);
        Map<String, List<PayableDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(PayableDetailEntity::getMainId));

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            PayableInfoEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            Map<String, Object> newSyncDataToKingdee = syncKingdeePayableInfoService.newSyncDataToKingdee(entity,map.get(sourceId), syncParamDetailDTO.getSyncOperate());
            if(newSyncDataToKingdee == null) {
                continue;
            }
            resultList.put(syncParamDetailDTO.getDataId(), newSyncDataToKingdee);
        }
        return resultList;
    }


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
