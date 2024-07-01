package com.erp.server.wms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import com.erp.model.wms.entity.WmsCartonEntity;
import com.erp.server.wms.mapper.WmsCartonMapper;
import com.erp.server.wms.service.WmsCartonService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 发货单箱子信息明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class WmsCartonServiceImpl extends SuperServiceImpl<WmsCartonMapper, WmsCartonEntity> implements WmsCartonService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCartonBillDTO.AddDTO addDTO) {
        WmsCartonEntity wmsCartonEntity = new WmsCartonEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonEntity);

        // 数据处理
        handleData(wmsCartonEntity);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.save(wmsCartonEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        return new BaseResultDTO.AddDTO(wmsCartonEntity.getId(), wmsCartonEntity.getId());
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getCartonId, cartonIds).remove();
    }

    @Override
    public Boolean deleteBySourceIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonEntity::getSourceId, mainIds).remove();
    }

    @Override
    public List<WmsCartonEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonEntity::getSourceId, sourceIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonEntity wmsCartonEntity) {

    }
}
