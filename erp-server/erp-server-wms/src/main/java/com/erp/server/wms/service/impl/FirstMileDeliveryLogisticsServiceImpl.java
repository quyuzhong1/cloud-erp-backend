package com.erp.server.wms.service.impl;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.LogisticsMethodEnum;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.server.wms.mapper.FirstMileDeliveryLogisticsMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FirstMileDeliveryLogisticsDTO;

import java.util.*;
import java.util.stream.Collectors;

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
public class FirstMileDeliveryLogisticsServiceImpl extends SuperServiceImpl<FirstMileDeliveryLogisticsMapper, FirstMileDeliveryLogisticsEntity> implements FirstMileDeliveryLogisticsService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private FirstMileDeliveryService firstMileDeliveryService;
    @Autowired
    private LogisticsBillFeign logisticsBillFeign;
    @Autowired
    private FbaShipmentService fbaShipmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(FirstMileDeliveryLogisticsDTO.AddDTO dto, String mainId, String code) {
        FirstMileDeliveryLogisticsEntity firstMileDeliveryLogisticsEntity = new FirstMileDeliveryLogisticsEntity();
        BeanMapperUtils.copy(dto, firstMileDeliveryLogisticsEntity);
        firstMileDeliveryLogisticsEntity.setRemark(dto.getLogisticsRemark());

        // 数据处理
        handleData(firstMileDeliveryLogisticsEntity, mainId, code);

        log.info("开始新增FBA发货单物流信息单");
        boolean save = super.save(firstMileDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }
        return firstMileDeliveryLogisticsEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileDeliveryLogisticsDTO.UpdateDTO updateDTO, String mainId) {
        FirstMileDeliveryLogisticsEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBA发货单物流信息单"));
        FirstMileDeliveryLogisticsEntity firstMileDeliveryLogisticsEntity =  BeanMapperUtils.map(FirstMileDeliveryLogisticsEntity.class, updateDTO);
        firstMileDeliveryLogisticsEntity.setRemark(updateDTO.getLogisticsRemark());
        // 数据处理
        handleData(firstMileDeliveryLogisticsEntity, mainId, old.getDeliveryCode());
        log.info("编辑 开始修改FBA发货单物流信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileDeliveryLogisticsEntity);
        if(!save) {
            throw new ServiceException("FBA发货单物流信息单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean removeByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(FirstMileDeliveryLogisticsEntity::getMainId,mainIds).remove();
    }

    @Override
    public FirstMileDeliveryLogisticsEntity listByMainId(String mainId) {
        return lambdaQuery().eq(FirstMileDeliveryLogisticsEntity::getMainId, mainId).last("LIMIT 1").one();
    }

    @Override
    public List<FirstMileDeliveryLogisticsEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(FirstMileDeliveryLogisticsEntity::getMainId, mainIds).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileDeliveryLogisticsEntity firstMileDeliveryLogisticsEntity, String mainId, String code) {
        firstMileDeliveryLogisticsEntity.setMainId(mainId);
        firstMileDeliveryLogisticsEntity.setDeliveryCode(code);

        //修改操作日志
        if (StringUtils.isNotBlank(firstMileDeliveryLogisticsEntity.getId())) {
            FirstMileDeliveryLogisticsEntity old = this.getById(firstMileDeliveryLogisticsEntity.getId());
            operateLogService.addModuleOperateLogByObj(old, firstMileDeliveryLogisticsEntity, ModuleTypeEnum.FIRST_MILE_DELIVERY.getCode(),mainId,"",String.format("【%s】",old.getDeliveryCode()));
        }

        //更新物流信息
        this.saveUpdateLogistics(Arrays.asList(firstMileDeliveryLogisticsEntity));
    }

    @Override
    public List<FirstMileDeliveryLogisticsDTO.DeliveryLogisticsView> updateLogisticsView(List<String> ids) {
        List<FirstMileDeliveryLogisticsDTO.DeliveryLogisticsView> viewList = new ArrayList<>();
        List<FirstMileDeliveryLogisticsEntity> fbaDeliveryLogisticsEntities = this.listByMainIds(ids);

        List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryService.listByIds(ids);
        List<FirstMileDeliveryEntity> deliveryEntityList = deliveryEntities.stream()
                .filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()) && !ApproveStatusEnum.APPROVE_ING.getStatus().equals(req.getApproveStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryEntityList)) {
            throw new ServiceException(ApiError.NOT_APPROVE_NOT_UPDATE_LOGISTICS);
        }

        List<LogisticsBillDTO.LogisticsBillVo> logisticsBillVos = logisticsBillFeign.listLogisticsBillVoBySourceIds(ids);
        for (FirstMileDeliveryLogisticsEntity logisticsEntity : fbaDeliveryLogisticsEntities) {
            FirstMileDeliveryLogisticsDTO.DeliveryLogisticsView view = new FirstMileDeliveryLogisticsDTO.DeliveryLogisticsView();
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
    public Boolean saveUpdateLogistics(List<FirstMileDeliveryLogisticsEntity> dto) {
        List<FirstMileDeliveryLogisticsEntity> list = new ArrayList<>();
        //查询发货单信息
        List<String> mainIds = dto.stream().map(req -> req.getMainId()).collect(Collectors.toList());
        List<FirstMileDeliveryEntity> fbaDeliveryEntities = firstMileDeliveryService.listByIds(mainIds);

        //查询FBA货件信息
        List<String> shipmentIds = fbaDeliveryEntities.stream().map(req -> req.getSourceId()).collect(Collectors.toList());
        List<FbaShipmentEntity> fbaShipmentEntities = fbaShipmentService.listByIds(shipmentIds);

        //更新FBA物流信息
        for (FirstMileDeliveryLogisticsEntity deliveryLogisticsSave : dto) {
            FirstMileDeliveryLogisticsEntity entity = new FirstMileDeliveryLogisticsEntity();
            BeanMapper.copy(deliveryLogisticsSave, entity);
            list.add(entity);
        }
        //更新物流单信息
        List<LogisticsBillDTO.AddDTO> addDTOList = new ArrayList<>();
        for (FirstMileDeliveryLogisticsEntity deliveryLogisticsSave : dto) {
            FirstMileDeliveryEntity firstMileDeliveryEntity = fbaDeliveryEntities.stream().filter(req -> req.getId().equals(deliveryLogisticsSave.getMainId())).findFirst().orElse(new FirstMileDeliveryEntity());
            LogisticsBillDTO.AddDTO addDTO = new LogisticsBillDTO.AddDTO();
            addDTO.setShopId(firstMileDeliveryEntity.getShopId());
            addDTO.setShopName(firstMileDeliveryEntity.getShopName());
            addDTO.setSourceId(firstMileDeliveryEntity.getId());
            addDTO.setSourceCode(firstMileDeliveryEntity.getCode());
            addDTO.setTransportNo(firstMileDeliveryEntity.getCode());
            addDTO.setSalesPlatform(PlatformDictEnum.AMAZON.getCode());
            addDTO.setSourceType(SourceTypeEnum.FIRST_MILE_DELIVERY.getCode());
            addDTO.setOutstockId("");
            addDTO.setOutstockCode("");
            addDTO.setChannelId(deliveryLogisticsSave.getLogisticsChannel()==null?"":deliveryLogisticsSave.getLogisticsChannel());
            if (deliveryLogisticsSave.getDeliveryTime() != null) {
                addDTO.setDeliveryTime(deliveryLogisticsSave.getDeliveryTime().toLocalDate());
            }
            List<String> trackingNoList = deliveryLogisticsSave.getTrackingNoList();
            FbaShipmentEntity entity = fbaShipmentEntities.stream().filter(req -> req.getId().equals(firstMileDeliveryEntity.getSourceId())).findFirst().orElse(new FbaShipmentEntity());
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
        return logisticsBillFeign.logisticsBillBatchSave(addDTOList);
    }
}
