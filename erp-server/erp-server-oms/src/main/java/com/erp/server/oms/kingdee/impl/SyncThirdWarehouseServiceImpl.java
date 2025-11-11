package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.ThirdWarehouseCreateOutboundPushDTO;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.oms.kingdee.SyncThirdWarehouseService;
import com.erp.server.oms.service.OmsPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Date 2025-11-07 11:46
 * @Created by zdy
 */
@Slf4j
@Service
public class SyncThirdWarehouseServiceImpl implements SyncThirdWarehouseService {
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private OmsPushMsgService omsPushMsgService;
    /**
     * 销售变更单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToThirdWarehouse(ThirdWarehouseCreateOutboundPushDTO entity, String operate) {
        //生成任务
        if (SyncOperateEnum.OPERATE_ADD.getCode().equals(operate)) {
            return saveTask(entity, operate, this.newSyncDataToThirdWarehouse(entity, operate));
        } else {
            throw new ServiceException("操作类型【{}】不支持同步第三方仓库存", operate);
        }
    }


    /**
     * @param pushDTO
     * @param operate
     * @param resultMap
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     */
    private DmpPushTaskEntity saveTask(ThirdWarehouseCreateOutboundPushDTO pushDTO, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_MULTI_CHANNEL.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        if (CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
            taskFeignDTO.setSourceId(pushDTO.getEntity().getId());
            taskFeignDTO.setSourceCode(pushDTO.getEntity().getCode());
            taskFeignDTO.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode());
            taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_THIRD_WAREHOUSE_ERP_TOPIC);
            taskFeignDTO.setMqTag(RocketMqTagEnum.ERP_THIRD_WAREHOUSE_TAG.getName());
            taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            taskFeignDTO.setTargetPlatformName(PlatformDictEnum.getNameByCode(pushDTO.getOverseasProviderWarehouse().getProviderCode()));
            taskFeignDTO.setSyncOperate(operate);
            return dmpMqFeign.saveTask(taskFeignDTO);
        }

        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(pushDTO.getEntity().getId());
        omsPushMsgEntity.setSourceCode(pushDTO.getEntity().getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(PlatformDictEnum.getNameByCode(pushDTO.getOverseasProviderWarehouse().getProviderCode()));
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);

        return null;
    }


    @Override
    public Map<String, Object> newSyncDataToThirdWarehouse(ThirdWarehouseCreateOutboundPushDTO entity, String operate) {
        return BeanUtil.beanToMap(entity);
    }

}
