package com.erp.server.wms.service.impl;


import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.server.wms.mapper.FbaShipmentDetailMapper;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA拣货明细表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaShipmentDetailServiceImpl extends SuperServiceImpl<FbaShipmentDetailMapper, FbaShipmentDetailEntity> implements FbaShipmentDetailService {

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FbaShipmentDetailDTO.AddDTO addDTO) {
        FbaShipmentDetailEntity fbaShipmentDetailEntity = new FbaShipmentDetailEntity();
        BeanMapperUtils.copy(addDTO, fbaShipmentDetailEntity);

        // 数据处理
        handleData(fbaShipmentDetailEntity);

        log.info("开始新增FBA拣货明细单");
        boolean save = super.save(fbaShipmentDetailEntity);
        if(!save) {
            throw new ServiceException("FBA拣货明细单保存失败");
        }
        return fbaShipmentDetailEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaShipmentDetailDTO.UpdateDTO updateDTO) {
        FbaShipmentDetailEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "FBA拣货明细单");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA拣货明细单"));
        FbaShipmentDetailEntity fbaShipmentDetailEntity =  BeanMapperUtils.map(FbaShipmentDetailEntity.class, updateDTO);

        // 数据处理
        handleData(fbaShipmentDetailEntity);
        log.info("编辑 开始修改FBA拣货明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaShipmentDetailEntity);
        if(!save) {
            throw new ServiceException("FBA拣货明细单保存失败");
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FbaShipmentDetailEntity fbaShipmentDetailEntity) {
    }

    @Override
    public List<FbaShipmentDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(FbaShipmentDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(FbaShipmentDetailEntity::getMainId,mainIds).remove();
    }
}
