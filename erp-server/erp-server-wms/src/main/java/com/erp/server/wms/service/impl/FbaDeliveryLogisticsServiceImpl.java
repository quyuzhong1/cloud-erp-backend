package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.wms.mapper.FbaDeliveryLogisticsMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
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
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    @Autowired
    private FbaDeliveryService fbaDeliveryService;
    @Autowired
    private LogisticsBillFeign logisticsBillFeign;
    @Autowired
    private FbaShipmentService fbaShipmentService;

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
        //更新物流信息
        this.saveUpdateLogistics(Arrays.asList(fbaDeliveryLogisticsEntity));
    }

    @Override
    public List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> updateLogisticsView(List<String> ids) {
        List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> viewList = new ArrayList<>();
        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillFeign.listLogisticsBillVoBySourceIds(ids);

        List<FbaDeliveryLogisticsEntity> fbaDeliveryLogisticsEntities = this.listByMainIds(ids);
        for (FbaDeliveryLogisticsEntity logisticsEntity : fbaDeliveryLogisticsEntities) {
            FbaDeliveryLogisticsDTO.DeliveryLogisticsView view = new FbaDeliveryLogisticsDTO.DeliveryLogisticsView();
            BeanMapper.copy(logisticsEntity, view);
            view.setLogisticsRemark(logisticsEntity.getRemark());
            view.setLogisticsMethodName(LogisticsMethodEnum.getName(view.getLogisticsMethod()));
            view.setLogisticsChannelName(LogisticsMethodEnum.getName(view.getLogisticsChannel()));
            List<String> trackNoList = logisticsBillVos.stream().filter(req -> req.getSourceId().equals(logisticsEntity.getMainId())).map(req -> req.getTrackNo()).collect(Collectors.toList());
            view.setTrackingNoList(trackNoList);
            viewList.add(view);
        }
        return viewList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean saveUpdateLogistics(List<FbaDeliveryLogisticsEntity> dto) {
        List<FbaDeliveryLogisticsEntity> list = new ArrayList<>();
        //查询发货单信息
        List<String> mainIds = dto.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        List<FbaDeliveryEntity> fbaDeliveryEntities = fbaDeliveryService.listByIds(mainIds);

        //查询FBA货件信息
        List<String> shipmentIds = fbaDeliveryEntities.stream().map(req -> req.getSourceId()).collect(Collectors.toList());
        List<FbaShipmentEntity> fbaShipmentEntities = fbaShipmentService.listByIds(shipmentIds);

        //更新FBA物流信息
        for (FbaDeliveryLogisticsEntity deliveryLogisticsSave : dto) {
            FbaDeliveryLogisticsEntity entity = new FbaDeliveryLogisticsEntity();
            BeanMapper.copy(deliveryLogisticsSave, entity);
            list.add(entity);
        }
        boolean flag = this.saveOrUpdateBatch(list);

        //更新物流单信息
        List<LogisticsBillDTO.AddDTO> addDTOList = new ArrayList<>();
        for (FbaDeliveryLogisticsEntity deliveryLogisticsSave : dto) {
            FbaDeliveryEntity fbaDeliveryEntity = fbaDeliveryEntities.stream().filter(req -> req.getId().equals(deliveryLogisticsSave.getMainId())).findFirst().orElse(new FbaDeliveryEntity());
            LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
            addDTO.setShopId(fbaDeliveryEntity.getShopId());
            addDTO.setShopName(fbaDeliveryEntity.getShopName());
            addDTO.setSourceId(fbaDeliveryEntity.getId());
            addDTO.setSourceCode(fbaDeliveryEntity.getCode());
            addDTO.setTransportNo(fbaDeliveryEntity.getCode());
            addDTO.setSalesPlatform(PlatformDictEnum.AMAZON.getCode());
            addDTO.setSourceType(SourceTypeEnum.FBA_DELIVERY.getCode());
            addDTO.setOutstockId("");
            addDTO.setOutstockCode("");
            addDTO.setChannelId(deliveryLogisticsSave.getLogisticsChannel()==null?"":deliveryLogisticsSave.getLogisticsChannel());
            if (deliveryLogisticsSave.getDeliveryTime() != null) {
                addDTO.setDeliveryTime(deliveryLogisticsSave.getDeliveryTime().toLocalDate());
            }
            List<String> trackingNoList = deliveryLogisticsSave.getTrackingNoList();
            FbaShipmentEntity entity = fbaShipmentEntities.stream().filter(req -> req.getId().equals(fbaDeliveryEntity.getSourceId())).findFirst().orElse(new FbaShipmentEntity());
            addDTO.setOrderTime(entity.getShipmentCreateTime());
            List<LogisticsBillDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (String trackingNo : trackingNoList) {
                LogisticsBillDetailDTO.AddDTO detailDto = new LogisticsBillDetailDTO.AddDTO();
                detailDto.setMainId("");
                detailDto.setTrackNo("");
                detailDto.setTrackNo(trackingNo);
                detailList.add(detailDto);
            }
            addDTO.setDetailList(detailList);
            addDTOList.add(addDTO);
        }
        logisticsBillFeign.logisticsBillBatchSave(addDTOList);
        return flag;
    }
}
