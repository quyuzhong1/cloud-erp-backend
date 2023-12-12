package com.erp.server.wms.service.impl;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentStatusEntity;
import com.erp.server.wms.convert.FbaShipmentConsumerConverter;
import com.erp.server.wms.mapper.FbaShipmentStatusMapper;
import com.erp.server.wms.service.FbaShipmentStatusService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * FBA货件状态信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Slf4j
@Service
public class FbaShipmentStatusServiceImpl extends SuperServiceImpl<FbaShipmentStatusMapper, FbaShipmentStatusEntity> implements FbaShipmentStatusService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByFbaShipment(FbaShipmentEntity sourceEntity) {
        // 映射来源
        FbaShipmentStatusEntity entity = FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToStatusEntity(sourceEntity);
        entity.setId(IdWorker.getIdStr());
        if (!this.save(entity)){
            throw new ServiceException("[FbaShipmentStatusEntity] 保存失败：entity=" + JSONUtil.toJsonStr(entity));
        }
    }

    @Override
    public List<FbaShipmentStatusEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaShipmentStatusEntity::getMainId, mainIds).list();
    }
}
