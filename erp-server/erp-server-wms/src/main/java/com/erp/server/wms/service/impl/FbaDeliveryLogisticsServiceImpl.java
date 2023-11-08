package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.FbaDeliveryDetailEntity;
import com.erp.model.wms.entity.FbaDeliveryLogisticsEntity;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.server.wms.mapper.FbaDeliveryLogisticsMapper;
import com.erp.server.wms.service.FbaDeliveryLogisticsService;
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
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;

import java.time.LocalDateTime;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBA发货单物流信息表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaDeliveryLogisticsServiceImpl extends SuperServiceImpl<FbaDeliveryLogisticsMapper, FbaDeliveryLogisticsEntity> implements FbaDeliveryLogisticsService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FbaDeliveryLogisticsDTO.AddDTO dto, String mainId, String code) {
        FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity = new FbaDeliveryLogisticsEntity();
        BeanMapperUtils.copy(dto, fbaDeliveryLogisticsEntity);
        fbaDeliveryLogisticsEntity.setRemark(dto.getLogisticsRemark());
        // 数据处理
        handleData(fbaDeliveryLogisticsEntity, mainId, code);

        log.info("开始新增FBA发货单物流信息单");
        boolean save = super.save(fbaDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "FBA发货单物流信息单" , fbaDeliveryLogisticsEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_DELIVERY.getCode(), fbaDeliveryLogisticsEntity.getId(), "新增操作");
        return fbaDeliveryLogisticsEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaDeliveryLogisticsDTO.UpdateDTO updateDTO, String mainId) {
        FbaDeliveryLogisticsEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单物流信息单"));
        FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity =  BeanMapperUtils.map(FbaDeliveryLogisticsEntity.class, updateDTO);
        fbaDeliveryLogisticsEntity.setRemark(updateDTO.getLogisticsRemark());
        // 数据处理
        handleData(fbaDeliveryLogisticsEntity, mainId, old.getDeliveryCode());
        log.info("编辑 开始修改FBA发货单物流信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(fbaDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录FBA发货单物流信息单日志数据，id：【{}】", fbaDeliveryLogisticsEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaDeliveryLogisticsEntity.getId(), "FBA发货单物流信息单");
        operateLogService.addModuleOperateLogByObj(old, fbaDeliveryLogisticsEntity, ModuleTypeEnum.FBA_DELIVERY.getCode(), fbaDeliveryLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(FbaDeliveryLogisticsEntity::getMainId,mainIds).remove();
    }

    @Override
    public FbaDeliveryLogisticsEntity listByMainId(String mainId) {
        return lambdaQuery().eq(FbaDeliveryLogisticsEntity::getMainId, mainId).last("LIMIT 1").one();
    }

    @Override
    public List<FbaDeliveryLogisticsEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(FbaDeliveryLogisticsEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FbaDeliveryLogisticsEntity fbaDeliveryLogisticsEntity, String mainId, String code) {
        fbaDeliveryLogisticsEntity.setMainId(mainId);
        fbaDeliveryLogisticsEntity.setDeliveryCode(code);

    }

    @Override
    public List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> updateLogisticsView(List<String> ids) {
        List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> viewList = new ArrayList<>();
        List<FbaDeliveryLogisticsEntity> fbaDeliveryLogisticsEntities = this.listByMainIds(ids);
        for (FbaDeliveryLogisticsEntity logisticsEntity : fbaDeliveryLogisticsEntities) {
            FbaDeliveryLogisticsDTO.DeliveryLogisticsView view = new FbaDeliveryLogisticsDTO.DeliveryLogisticsView();
            BeanMapper.copy(logisticsEntity, view);
            view.setLogisticsRemark(logisticsEntity.getRemark());
            view.setLogisticsMethodName(LogisticsMethodEnum.getName(view.getLogisticsMethod()));
            view.setLogisticsChannelName(LogisticsMethodEnum.getName(view.getLogisticsChannel()));
            viewList.add(view);
        }
        return viewList;
    }

    @Override
    public Boolean saveUpdateLogistics(List<FbaDeliveryLogisticsDTO.DeliveryLogisticsSave> dto) {
        List<FbaDeliveryLogisticsEntity> fbaDeliveryLogisticsEntities = BeanMapper.copyList(dto, FbaDeliveryLogisticsEntity.class);
        return this.updateBatchById(fbaDeliveryLogisticsEntities);
    }
}
