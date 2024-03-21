package com.erp.server.wms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.FirstMileCartonBillDTO;
import com.erp.model.wms.entity.WmsCartonBillEntity;
import com.erp.server.wms.mapper.WmsCartonBillMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.WmsCartonBillService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WmsCartonBillServiceImpl extends SuperServiceImpl<WmsCartonBillMapper, WmsCartonBillEntity> implements WmsCartonBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileCartonBillDTO.AddDTO addDTO) {
        WmsCartonBillEntity wmsCartonBillEntity = new WmsCartonBillEntity();
        BeanMapperUtils.copy(addDTO, wmsCartonBillEntity);

        // 数据处理
        handleData(wmsCartonBillEntity);

        log.info("开始新增发货单箱子信息明细单");
        boolean save = super.save(wmsCartonBillEntity);
        if(!save) {
            throw new ServiceException("发货单箱子信息明细单保存失败");
        }
        return new BaseResultDTO.AddDTO(wmsCartonBillEntity.getId(), wmsCartonBillEntity.getId());
    }

    @Override
    public Boolean deleteByCartonIds(List<String> cartonIds) {
        if (CollectionUtils.isEmpty(cartonIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonBillEntity::getCartonId, cartonIds).remove();
    }

    @Override
    public Boolean deleteByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().in(WmsCartonBillEntity::getMainId, mainIds).remove();
    }

    @Override
    public List<WmsCartonBillEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(WmsCartonBillEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WmsCartonBillEntity wmsCartonBillEntity) {

    }
}
