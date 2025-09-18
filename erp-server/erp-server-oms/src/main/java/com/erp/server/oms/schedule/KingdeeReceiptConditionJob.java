package com.erp.server.oms.schedule;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.dto.KingdeeReceiptConditionDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.erp.server.oms.service.BankAccountService;
import com.erp.server.oms.service.KingdeeReceiptConditionService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶收款条件定时任务
 *
 * @author Lambda
 * @Classname KingdeeBankAccountJob
 * @Description TODO
 * @Date 2024-03-07 14:25
 * @Created by yl
 */
@Component
@Slf4j
public class KingdeeReceiptConditionJob {

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;




    @XxlJob("syncKingdeeReceiptCondition")
    public void syncBankAccountJob() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_RECCONDITION.getCode());
        //查询
        String fieldKeys = "FID,FNumber,FName,FFORBIDSTATUS,FDOCUMENTSTATUS";
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 1000;
        Boolean dataSign = true;
        List<KingdeeReceiptConditionDTO.KingdeeDTO> receiptConditionList = new ArrayList<>(20);
        while (dataSign) {
            List<Map<String, Object>> result = apiUtils.queryList("", fieldKeys, pageSize, pageIndex, 0);
            if (result.size() < pageSize) {
                dataSign = false;
            }
            List<KingdeeReceiptConditionDTO.KingdeeDTO> entityList = result.stream().map(entity ->
                    BeanUtil.toBean(entity, KingdeeReceiptConditionDTO.KingdeeDTO.class)).collect(Collectors.toList());
            receiptConditionList.addAll(entityList);
            pageIndex++;
        }
        //数据库存在的
        List<KingdeeReceiptConditionEntity> dbList = kingdeeReceiptConditionService.list();
        //这个是查询到的
        List<String> queryKingdeeIds = receiptConditionList.stream().map(KingdeeReceiptConditionDTO.KingdeeDTO::getKingdeeId).collect(Collectors.toList());
        //表示这些是删除的 那就要禁用
        List<String> disableIds = dbList.stream().filter(d -> !queryKingdeeIds.contains(d.getKingdeeId())).map(KingdeeReceiptConditionEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(disableIds)){
            kingdeeReceiptConditionService.updateDisable(disableIds,true);
        }

        List<KingdeeReceiptConditionEntity> saveOrUpdateList = new ArrayList<>(10);
        for (KingdeeReceiptConditionDTO.KingdeeDTO item : receiptConditionList) {
            String kingdeeId = item.getKingdeeId();
            String code = item.getCode();
            String name = item.getName();
            String kingdeeStatus = item.getKingdeeStatus();
            //禁用状态
            String kingdeeDisabledStatus = item.getKingdeeDisabledStatus();
            //B 表示禁用
            Boolean disabled = "B".equals(kingdeeDisabledStatus);
            //表示 未禁用
            if(!disabled){
                //表示未审核
               if(!"C".equals(kingdeeStatus)){
                   disabled=Boolean.TRUE;
               }
            }

            KingdeeReceiptConditionEntity dbEntity = dbList.stream().filter(entity -> entity.getKingdeeId().equals(kingdeeId)).
                    findFirst().orElse(null);
            if (Objects.isNull(dbEntity)) {
                KingdeeReceiptConditionEntity addEntity = new KingdeeReceiptConditionEntity();
                addEntity.setKingdeeId(kingdeeId);
                addEntity.setCode(code);
                addEntity.setName(name);
                addEntity.setKingdeeStatus(kingdeeStatus);
                addEntity.setDisabled(disabled);
                saveOrUpdateList.add(addEntity);
            } else {
                //表示有 是否修改
                if (!dbEntity.getCode().equals(code) || !dbEntity.getName().equals(name) ||
                         !dbEntity.getKingdeeStatus().
                        equals(kingdeeStatus) || !dbEntity.getDisabled().equals(disabled)) {
                    dbEntity.setCode(code);
                    dbEntity.setName(name);
                    dbEntity.setKingdeeStatus(kingdeeStatus);
                    dbEntity.setDisabled(disabled);
                    saveOrUpdateList.add(dbEntity);
                }
            }

        }
        kingdeeReceiptConditionService.saveOrUpdateBatch(saveOrUpdateList);
    }
}
