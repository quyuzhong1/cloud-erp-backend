package com.erp.server.dmp.inout.handler.output.task.mq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpBankInfoEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.sys.entity.DictBankEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;

import cn.hutool.core.collection.CollUtil;

/**
 * @description: 处理金蝶收款银行
 * @author: wuht
 * @date: 2025/7/31 15:34
 */
@Service
@Scope("prototype")
public class DmpOutputKingdeeBankRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler{
    
    // 银行状态常量
    private static final String STATUS_ACTIVE = "C";  // 状态：激活
    private static final String DISABLED_FALSE = "A"; // 禁用状态：否
    
    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpBankInfoEntity> dmpBankInfoEntityMap = new HashMap<>();
        
        for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if(CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if("dmp_bank_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        DmpBankInfoEntity dmpBankInfoEntity = (DmpBankInfoEntity) v;
                        dmpBankInfoEntityMap.put(dmpBankInfoEntity.getId(), dmpBankInfoEntity);
                    }
                }
            }
        }
        
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>(); 
        for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if(CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if("dmp_bank_info".equals(storageName)) {
                    for(BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                }
            }
        }
        
        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for(String changeId : changeIds) {
            DmpBankInfoEntity dmpBankInfoEntity = dmpBankInfoEntityMap.get(changeId);
            if(dmpBankInfoEntity != null) {
                DictBankEntity dictBankEntity = this.convert(dmpBankInfoEntity, cfgOutputId);
                if(dictBankEntity != null) {
                    map.put(dmpBankInfoEntity.getId(), JSON.toJSONString(dictBankEntity));
                }
            }
        }
        return map;
    }
    
    /**
     * 转换银行数据
     **/
    public DictBankEntity convert(DmpBankInfoEntity dmpBankInfoEntity, String cfgOutputId) {
        if(this.validateDataBlack(dmpBankInfoEntity, cfgOutputId)) {
            return null;
        }
        
        DictBankEntity dictBankEntity = new DictBankEntity();
        
        // 设置银行编号
        dictBankEntity.setBankNo(dmpBankInfoEntity.getBankNo());
        // 设置银行名称
        dictBankEntity.setName(dmpBankInfoEntity.getBankName());
        // 设置客服电话（从银行描述中提取或使用默认值）
        dictBankEntity.setServicesPhone(extractPhoneFromDescription(dmpBankInfoEntity.getBankDescription()));
        // 设置总部地址
        dictBankEntity.setHeadquarterAddress(dmpBankInfoEntity.getBankAddress());
        // 设置是否禁用（根据status和disabled字段判断）
        // 逻辑：只有当status为"C"且disabled为"A"时，银行才处于激活状态（disabled=false）
        // 其他任何情况（包括null值）都会导致银行被禁用（disabled=true）
        String status = dmpBankInfoEntity.getStatus();
        String disabled = dmpBankInfoEntity.getDisabled();
        boolean isDisabled = !(STATUS_ACTIVE.equals(status) && DISABLED_FALSE.equals(disabled));
        dictBankEntity.setDisabled(isDisabled);
        // 设置基础字段
        dictBankEntity.setCreateTime(LocalDateTime.now());
        dictBankEntity.setUpdateTime(LocalDateTime.now());
        dictBankEntity.setVersion(0);
        dictBankEntity.setIsDeleted(false);
        
        return dictBankEntity;
    }
    
    /**
     * 从银行描述中提取电话号码
     */
    private String extractPhoneFromDescription(String bankDescription) {
        if(bankDescription == null || bankDescription.trim().isEmpty()) {
            return "";
        }
        
        // 简单的电话号码提取逻辑，可以根据实际需求调整
        // 这里假设描述中包含电话号码，格式如：客服电话：95566
        if(bankDescription.contains("客服电话：")) {
            String[] parts = bankDescription.split("客服电话：");
            if(parts.length > 1) {
                String phonePart = parts[1].split("\\s")[0]; // 取第一个空格前的部分
                return phonePart.trim();
            }
        }
        
        // 如果没有找到特定格式，返回空字符串
        return "";
    }

    @Override
    protected List<String> getSourceCodeKeys() {
        return Arrays.asList("bankNo", "bankName");
    }
}
