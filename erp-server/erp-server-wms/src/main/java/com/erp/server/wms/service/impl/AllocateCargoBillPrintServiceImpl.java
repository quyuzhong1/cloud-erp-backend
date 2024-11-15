package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.dto.AllocateCargoBillPrintDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryPrintTypeEnum;
import com.erp.server.wms.service.AllocateCargoBillPrintService;
import com.erp.server.wms.service.WaveListDetailService;
import com.erp.server.wms.service.WaveListService;
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
    private WaveListService waveListService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private WaveListDetailService waveListDetailService;

    @Override
    public AllocateCargoBillPrintDTO.ScanWaveDTO scanWaveOrPickingCarCode(String businessCode) {
        //查询波次号，如果查询到直接返回
        WaveListEntity pickingWave = waveListService.getByCode(businessCode);
        if(ObjectUtil.isNotEmpty(pickingWave)){
            return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(true).waveList(Arrays.asList(AllocateCargoBillPrintDTO.WaveDTO.convertFromPickingWaveEntity(pickingWave))).build();
        }
        //根据拣货车编号查询
        List<WaveListEntity> pickingWaveList = waveListService.listByCarCode(businessCode);
        if(CollUtil.isEmpty(pickingWaveList)){
            if(businessCode.contains("JHBC")){
                throw new ServiceException("拣货波次编号错误");
            }else{
                throw new ServiceException("拣货车编号错误");
            }
        }
        //有拣货中的波次，直接返回
        WaveListEntity waveListEntity = pickingWaveList.stream().filter(v->v.getStatus().equals(WaveStatusEnum.PICK_ING.getCode())).findFirst().orElse(null);
        if(Objects.nonNull(waveListEntity)){
            return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(true).waveList(Arrays.asList(AllocateCargoBillPrintDTO.WaveDTO.convertFromPickingWaveEntity(waveListEntity))).build();
        }
        //过滤当天已完成的波次，根据拣货时间倒序，为空抛异常
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        pickingWaveList = pickingWaveList.stream().filter(v->v.getStatus().equals(WaveStatusEnum.FINISH.getCode()) && Objects.nonNull(v.getPickingTime()) && v.getPickingTime().isAfter(today)).sorted((o1, o2)->o2.getPickingTime().compareTo(o1.getPickingTime())).collect(Collectors.toList());

        if(CollUtil.isEmpty(pickingWaveList)){
            throw new ServiceException("拣货车关联没有今天完成的波次");
        }

        return AllocateCargoBillPrintDTO.ScanWaveDTO.builder().isDirectPrint(false).waveList(pickingWaveList.stream().map(AllocateCargoBillPrintDTO.WaveDTO::convertFromPickingWaveEntity).collect(Collectors.toList())).build();
    }

    @Override
    public void print(String waveId, HttpServletResponse response) {
        WaveListEntity pickingWave = waveListService.getById(waveId);
        if(Objects.isNull(pickingWave)){
            throw new ServiceException("波次为空");
        }
        List<WaveListDetailEntity> detailEntityList = waveListDetailService.listByMainId(pickingWave.getId());
        List<String> deliveryIds = detailEntityList.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
        if(CollUtil.isEmpty(deliveryIds)){
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
