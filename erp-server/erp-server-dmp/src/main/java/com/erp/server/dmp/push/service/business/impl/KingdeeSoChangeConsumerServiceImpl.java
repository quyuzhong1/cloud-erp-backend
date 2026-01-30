package com.erp.server.dmp.push.service.business.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApi;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.business.KingdeeSoChangeConsumerService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeeSoChangeConsumerServiceImpl
 * @Description TODO
 * @Date 2023-08-02 9:00
 * @Created by yl
 */
@Service
@Slf4j
public class KingdeeSoChangeConsumerServiceImpl implements KingdeeSoChangeConsumerService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @KingdeeApi
    public void executeConsumer(Map<String, Object> map) {
        //操作项
        String operate = (String) map.get("operate");
        //模块类型
        Integer type = ApiModuleTypeEnum.SO_CHANGE.getCode();

        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(map, PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER_CHANGE.getCode());

        /**
         * 审核
         */
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operate)) {
            operateApprove(apiUtils,platformEntity, map,type);
        }
    }

    /**
     * 审核：按金蝶推荐流程处理销售订单变更单
     * 1）无 syncKingdeeId 时：先调 SaveXSaleOrder 根据原单生成变更单，再 Save 只传数量/单价等变更字段
     * 2）有 syncKingdeeId 时：View 后 Save 更新（与原先一致）
     */
    public void operateApprove(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        String syncKingdeeId = map.get("syncKingdeeId") != null ? String.valueOf(map.get("syncKingdeeId")).trim() : "";

        if (StringUtils.isBlank(syncKingdeeId)) {
            // 无金蝶变更单ID：使用 SaveXSaleOrder 生成变更单（原单数据原样带出），再 Save 修改变更数量/单价等
            operateApproveBySaveXSaleOrder(apiUtils, platformEntity, map, type);
            return;
        }

        // 已有金蝶变更单ID：用全量 makeApiFieldJson + getAllKey 更新，保存前对 Model 做空串清洗（根因修复 ")"附近有语法错误）
        JSONObject json = kingdeeCommonService.makeApiFieldJson(map, platformEntity.getId(), type);
        if (CollectionUtils.isEmpty(json)) {
            log.error(ApiError.MAPPING_NOT_SET_PUSH_FORBIDDEN.getMsg());
            throw new ServiceException(ApiError.DMP_KINGDEE_FIELD_NOT_FOUND);
        }
        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(json);
        JSONObject model;
        try {
            model = kingdeeCommonService.view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {
            kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
            return;
        }
        String documentStatus = (String) model.get("DocumentStatus");
        String id = String.valueOf(model.get("Id"));
        Boolean flag = Boolean.FALSE;
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            flag = kingdeeCommonService.unAudit(apiUtils, id);
        }
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus) || flag) {
            KingdeeUtils.makeFieldJson(json, "FId", ".", id);
            StringBuffer allKey = FastJsonUtil.getAllKey(json);
            ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
            param.setNeedUpDateFields(apiFieldList);
            kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, json, param, type);
        }
    }

    /**
     * 通过 SaveXSaleOrder 创建变更单，再 Save 修改变更数量/单价
     * 入参需来自 OMS：saleOrderBillId、soCode(saleOrderBillNo)、saleOrderEntryIds、detailList
     */
    private void operateApproveBySaveXSaleOrder(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        Object saleOrderBillIdObj = map.get("saleOrderBillId");
        Object soCodeObj = map.get("soCode");
        Object saleOrderEntryIdsObj = map.get("saleOrderEntryIds");
        if (saleOrderBillIdObj == null || soCodeObj == null || saleOrderEntryIdsObj == null) {
            log.error("销售变更单推送缺少 SaveXSaleOrder 入参：saleOrderBillId、soCode、saleOrderEntryIds 需由 OMS 提供");
            throw new ServiceException(ApiError.DMP_KINGDEE_FIELD_NOT_FOUND.getCode(), "销售变更单需先通过 SaveXSaleOrder 创建，请确保 OMS 推送了 saleOrderBillId、soCode、saleOrderEntryIds");
        }
        String saleOrderBillNo = String.valueOf(soCodeObj).trim();
        String saleOrderBillId = String.valueOf(saleOrderBillIdObj).trim();
        @SuppressWarnings("unchecked")
        List<String> soEntryIds = saleOrderEntryIdsObj instanceof List ? (List<String>) saleOrderEntryIdsObj : new ArrayList<>();
        if (soEntryIds.isEmpty()) {
            log.error("saleOrderEntryIds 为空，无法调用 SaveXSaleOrder");
            throw new ServiceException(ApiError.DMP_KINGDEE_FIELD_NOT_FOUND.getCode(), "saleOrderEntryIds 不能为空");
        }

        Map<String, Object> paramMap = new java.util.HashMap<>(3);
        paramMap.put("SaleOrderBillNo", saleOrderBillNo);
        paramMap.put("SaleOrderBillId", saleOrderBillId);
        paramMap.put("SOEntryIds", soEntryIds);

        String resultStr = kingdeeCommonService.createkingdeeSoChange(paramMap);
        JSONObject resultJson = JSONUtil.parseObj(resultStr);
        Boolean isSuccess = resultJson.getBool("IsSuccess", false);
        if (!Boolean.TRUE.equals(isSuccess)) {
            String errMsg = resultJson.getStr("Message", resultStr);
            log.error("SaveXSaleOrder 失败: {}", resultStr);
            throw new ServiceException(ApiError.DMP_KINGDEE_ADD_FAILED.getCode(), "SaveXSaleOrder 失败: " + errMsg);
        }

        Object datasObj = resultJson.get("Datas");
        if (datasObj == null || !(datasObj instanceof List) || ((List<?>) datasObj).isEmpty()) {
            log.error("SaveXSaleOrder 返回无 Datas: {}", resultStr);
            throw new ServiceException(ApiError.DMP_KINGDEE_ADD_FAILED.getCode(), "SaveXSaleOrder 返回无有效数据");
        }
        JSONObject dataJson = (JSONObject) ((List<?>) datasObj).get(0);
        String newKingdeeId = String.valueOf(dataJson.get("FID"));
        map.put("syncKingdeeId", newKingdeeId);

        Object saleOrderEntryObj = dataJson.get("SaleOrderEntry");
        List<JSONObject> newEntryList = new ArrayList<>();
        if (saleOrderEntryObj instanceof List) {
            for (Object o : (List<?>) saleOrderEntryObj) {
                if (o instanceof JSONObject) {
                    newEntryList.add((JSONObject) o);
                }
            }
        }

        Object detailListObj = map.get("detailList");
        List<Map<String, Object>> detailList = new ArrayList<>();
        if (detailListObj instanceof List) {
            for (Object o : (List<?>) detailListObj) {
                if (o instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) o;
                    detailList.add(m);
                }
            }
        }

        JSONObject modelJson = new JSONObject();
        modelJson.set("FId", newKingdeeId);
        JSONArray fSaleOrderEntry = new JSONArray();
        int size = Math.min(newEntryList.size(), detailList.size());
        for (int i = 0; i < size; i++) {
            JSONObject entry = new JSONObject();
            Object entryId = newEntryList.get(i).get("FEntryID");
            if (entryId != null) {
                entry.set("FEntryId", String.valueOf(entryId));
            }
            Map<String, Object> row = detailList.get(i);
            if (row.containsKey("qty")) {
                entry.set("FQty", row.get("qty"));
            }
            if (row.containsKey("taxPrice")) {
                entry.set("FTaxPrice", row.get("taxPrice"));
            }
            fSaleOrderEntry.add(entry);
        }
        modelJson.set("FSaleOrderEntry", fSaleOrderEntry);

        KingdeeParamDTO.SaveParamDTO param = new KingdeeParamDTO.SaveParamDTO(modelJson);
        param.setNeedUpDateFields(new ArrayList<>(Arrays.asList("FId", "FSaleOrderEntry")));
        kingdeeCommonService.saveAndAutoApprove(platformEntity, map, apiUtils, modelJson, param, type);
    }



}
