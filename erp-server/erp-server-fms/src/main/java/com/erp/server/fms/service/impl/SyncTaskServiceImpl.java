package com.erp.server.fms.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.erp.server.fms.kingdee.SyncKingdeeAssetAcceptService;
import com.erp.server.fms.service.SyncTaskService;
import com.erp.server.fms.service.AssetAcceptDetailService;
import com.erp.server.fms.service.AssetAcceptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 更新金蝶状态
 * @author will
 * @date 2025/12/30 09:41
 */
@Slf4j
@Service
public class SyncTaskServiceImpl implements SyncTaskService {

    @Resource
    private AssetAcceptService assetAcceptService;

    @Resource
    private AssetAcceptDetailService assetAcceptDetailService;

    @Resource
    private SyncKingdeeAssetAcceptService syncKingdeeAssetAcceptService;


    @Override
    public Map<String, Map<String, Object>> newFindDataSendSyncTask(DmpSyncMqDTO.SyncParamDTO syncParamDTO) {
        List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
        SourceTypeEnum sourceType = syncParamDTO.getSourceType();
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        switch (sourceType) {
            case ASSET_ACCEPTANCE:
                resultList = syncAssetAccept(sourceDetailList);
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
    private Map<String , Map<String, Object>> syncAssetAccept(List<DmpSyncMqDTO.SyncParamDetailDTO> sourceDetailList) {
        Map<String , Map<String, Object>> resultList = new HashMap<>();
        List<String> sourceIdList = sourceDetailList.stream().map(DmpSyncMqDTO.SyncParamDetailDTO::getSourceId).collect(Collectors.toList());
        List<AssetAcceptEntity> list = assetAcceptService.listByIds(sourceIdList);
        if (CollectionUtils.isEmpty(list)) {
            log.error("syncAssetAccept >>>> 未找到数据！");
            return resultList;
        }
        //明细信息
        List<AssetAcceptDetailEntity> detailList = assetAcceptDetailService.listByMainIdList(sourceIdList);
        Map<String, List<AssetAcceptDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(AssetAcceptDetailEntity::getMainId));

        for (DmpSyncMqDTO.SyncParamDetailDTO syncParamDetailDTO :  sourceDetailList) {
            String sourceId = syncParamDetailDTO.getSourceId();
            AssetAcceptEntity entity = list.stream().filter(obj -> {
                return obj.getId().equals(sourceId);
            }).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(entity)) {
                continue;
            }
            Map<String, Object> newSyncDataToKingdee = syncKingdeeAssetAcceptService.newSyncDataToKingdee(entity, syncParamDetailDTO.getSyncOperate());
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
        //模块类型编码
        String code = (String)params.get("code");
        //业务id
        String businessId = (String)params.get("businessId");
        //金蝶id
        String syncKingdeeId = (String)params.get("kingdeeId");
        //明细数据
        Object details = params.get("details");

        //资金
        if (ApiModuleTypeEnum.ASSET_ACCEPT.getCode().toString().equals(code)) {
            if (ObjectUtils.isNotEmpty(details)) {
                JSONArray list = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("details")));
                assetAcceptDetailService.updateKingdeeDetailId(list);
                return;
            }
            assetAcceptService.updateSyncKingdeeId(businessId, syncKingdeeId);
        }
    }
}
