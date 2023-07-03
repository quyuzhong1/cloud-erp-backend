package com.erp.server.dmp.service.impl;

import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.server.dmp.mapper.DmpFbaDeliveryMapper;
import com.erp.server.dmp.service.DmpFbaDeliveryDetailService;
import com.erp.server.dmp.service.DmpFbaDeliveryService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * <p>
 * FBA发货单 服务实现类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@Slf4j
@Service
public class DmpFbaDeliveryServiceImpl extends SuperServiceImpl<DmpFbaDeliveryMapper, DmpFbaDeliveryEntity> implements DmpFbaDeliveryService {

    @Autowired
    private DmpFbaDeliveryDetailService dmpFbaDeliveryDetailService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkDelivery(DmpFbaDeliveryEntity fbaDeliveryEntity) {
        DmpFbaDeliveryEntity dmpFbaDeliveryEntity =  lambdaQuery().eq(DmpFbaDeliveryEntity::getPlatformSign, fbaDeliveryEntity.getPlatformSign()).eq(DmpFbaDeliveryEntity::getDeliveryNo, fbaDeliveryEntity.getDeliveryNo())
                .one();

        if(null != dmpFbaDeliveryEntity && dmpFbaDeliveryEntity.getIsDeleted()){
            return;
        }

        if(Objects.isNull(dmpFbaDeliveryEntity)) {
            // 新增（含明细）
            this.add(fbaDeliveryEntity);
        } else {
            // 如果数据有变动需要更新数据库订单信息
            if (!fbaDeliveryEntity.toString().equals(dmpFbaDeliveryEntity.toString())) {
                dmpFbaDeliveryEntity.setId(dmpFbaDeliveryEntity.getId());
                updateById(dmpFbaDeliveryEntity);
            }
            // 判断明细是否发生变化
            dmpFbaDeliveryDetailService.update(fbaDeliveryEntity.getItemList(), dmpFbaDeliveryEntity.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(DmpFbaDeliveryEntity entity) {
        //新增主表数据
        boolean save = this.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //新增明细
        Boolean addDetail = dmpFbaDeliveryDetailService.add(entity.getItemList(), entity.getId());
        if (!addDetail) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return Boolean.TRUE;
    }

}
