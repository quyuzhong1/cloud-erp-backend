package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.mapper.FbaShipmentDetailMapper;
import com.erp.server.wms.service.FbaShipmentDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.rtfparserkit.rtf.Command;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.rtfparserkit.rtf.Command.list;

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

    @Resource
    private FbaShipmentReceiveService fbaShipmentReceiveService;

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

    @Override
    public FbaShipmentDetailEntity getDetail(String shipmentCode, String asin, String msku) {
        if (CharSequenceUtil.isAllNotBlank(shipmentCode,asin,msku)){
            return baseMapper.getDetail(shipmentCode, asin, msku);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetailByReceiveList(FbaShipmentEntity fbaShipmentEntity) {
        if (Objects.isNull(fbaShipmentEntity) || Objects.isNull(fbaShipmentEntity.getId())){
            return;
        }
        List<FbaShipmentDetailEntity> detailEntityList = this.listByMainIds(Collections.singletonList(fbaShipmentEntity.getId()));
        if (CollUtil.isEmpty(detailEntityList)){
            return;
        }
        List<FbaShipmentReceiveEntity> list = fbaShipmentReceiveService.listByDetailIds(detailEntityList.stream().map(FbaShipmentDetailEntity::getId).collect(Collectors.toList()));
        detailEntityList.forEach(detailEntity -> {
            List<FbaShipmentReceiveEntity> collect = list.stream().filter(e -> e.getDetailId().equals(detailEntity.getId())).collect(Collectors.toList());
            int receiveQty = collect.stream().mapToInt(FbaShipmentReceiveEntity::getReceiveQty).sum();
            detailEntity.setReceiveQty(receiveQty);
            int diffQty = receiveQty - detailEntity.getDeclareQty();
            detailEntity.setDiffQty(diffQty);
            this.lambdaUpdate().eq(FbaShipmentDetailEntity::getId,detailEntity.getId())
                    .set(FbaShipmentDetailEntity::getReceiveQty,receiveQty)
                    .set(FbaShipmentDetailEntity::getDiffQty,diffQty)
                    .update();
        });
    }
}
