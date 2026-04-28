package com.erp.server.wms.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.SoB2bDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2bDeliveryInterceptDetailEntity;
import com.erp.server.wms.mapper.SoB2bDeliveryInterceptDetailMapper;
import com.erp.server.wms.service.SoB2bDeliveryInterceptDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * b2b发货拦截单详情 服务实现类
 * </p>
 *
 * @author Codex
 */
@Service
public class SoB2bDeliveryInterceptDetailServiceImpl extends SuperServiceImpl<SoB2bDeliveryInterceptDetailMapper, SoB2bDeliveryInterceptDetailEntity> implements SoB2bDeliveryInterceptDetailService {

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public void add(SoB2bDeliveryInterceptDTO.AddDTO addDTO, String mainId) {
        if (CollectionUtils.isEmpty(addDTO.getDetailList())) {
            return;
        }
        List<SoB2bDeliveryInterceptDetailEntity> detailEntityList = BeanMapper.copyList(addDTO.getDetailList(), SoB2bDeliveryInterceptDetailEntity.class);
        if (CollectionUtils.isEmpty(detailEntityList)) {
            return;
        }
        for (SoB2bDeliveryInterceptDetailEntity detailEntity : detailEntityList) {
            detailEntity.setMainId(mainId);
        }
        if (!super.saveBatch(detailEntityList)) {
            throw new ServiceException("b2b发货拦截单详情保存失败");
        }
    }

    @Override
    public List<SoB2bDeliveryInterceptDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2bDeliveryInterceptDetailEntity::getMainId, mainIds).list();
    }
}
