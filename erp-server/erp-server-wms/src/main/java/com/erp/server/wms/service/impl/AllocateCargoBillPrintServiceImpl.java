package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AllocateCargoBillPrintDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.PickingWaveDetailEntity;
import com.erp.model.wms.entity.PickingWaveEntity;
import com.erp.model.wms.enums.PickingWaveStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryPrintTypeEnum;
import com.erp.server.wms.service.AllocateCargoBillPrintService;
import com.erp.server.wms.service.PickingWaveDetailService;
import com.erp.server.wms.service.PickingWaveService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 配货单打印 服务实现类
 * </p>
 *
 */
@Slf4j
@Service
public class AllocateCargoBillPrintServiceImpl implements AllocateCargoBillPrintService {

    @Resource
    private PickingWaveService pickingWaveService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private PickingWaveDetailService pickingWaveDetailService;

    @Override
    public AllocateCargoBillPrintDTO.ScanWaveDTO scanWaveOrPickingCarCode(String businessCode) {
        //查询波次号，如果查询到直接返回
        PickingWaveEntity pickingWave = pickingWaveService.getByCode(businessCode);
        if(ObjectUtil.isNotEmpty(pickingWave)){
            return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(true).waveList(Arrays.asList(AllocateCargoBillPrintDTO.WaveDTO.convertFromPickingWaveEntity(pickingWave))).build();
        }
        //根据拣货车编号查询
        List<PickingWaveEntity> pickingWaveList = pickingWaveService.listByCarCode(businessCode);
        //有拣货中的波次，直接返回
        PickingWaveEntity pickingWaveEntity = pickingWaveList.stream().filter(v->v.getStatus().equals(PickingWaveStatusEnum.PICK_ING.getCode())).findFirst().orElse(null);
        if(Objects.nonNull(pickingWaveEntity)){
            return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(true).waveList(Arrays.asList(AllocateCargoBillPrintDTO.WaveDTO.convertFromPickingWaveEntity(pickingWaveEntity))).build();
        }
        //过滤当天已完成的波次，根据拣货时间倒序，为空抛异常
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        pickingWaveList = pickingWaveList.stream().filter(v->v.getStatus().equals(PickingWaveStatusEnum.FINISH.getCode()) && v.getPickingTime().isAfter(today)).sorted((o1,o2)->o2.getPickingTime().compareTo(o1.getPickingTime())).collect(Collectors.toList());

        if(CollectionUtil.isEmpty(pickingWaveList)){
            throw new ServiceException("未查询到有效波次号");
        }

        return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(false).waveList(pickingWaveList.stream().map(AllocateCargoBillPrintDTO.WaveDTO::convertFromPickingWaveEntity).collect(Collectors.toList())).build();
    }

    @Override
    public void print(String waveId, HttpServletResponse response) {
        PickingWaveEntity pickingWave = pickingWaveService.getById(waveId);
        if(Objects.isNull(pickingWave)){
            throw new ServiceException("波次为空");
        }
        List<PickingWaveDetailEntity> detailEntityList = pickingWaveDetailService.listByMainId(pickingWave.getId());
        List<String> deliveryIds = detailEntityList.stream().map(PickingWaveDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
        if(CollectionUtil.isEmpty(deliveryIds)){
            throw new ServiceException("关联的发货单为空");
        }
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam();
        param.setIds(deliveryIds);
        param.setPrintType(SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode());
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillDTOList = soB2cDeliveryService.printLogisticsWaybillPreview(param);
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> printDetailDTOList = printLogisticsWaybillDTOList.stream().map(SoB2cDeliveryDTO.PrintLogisticsWaybillDTO::getDetailList).flatMap(Collection::stream).collect(Collectors.toList());
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO();
        dto.setDetailList(printDetailDTOList);
        dto.setPrintType(SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode());
        soB2cDeliveryService.printLogisticsBillConfirm(dto,response);
    }
}
