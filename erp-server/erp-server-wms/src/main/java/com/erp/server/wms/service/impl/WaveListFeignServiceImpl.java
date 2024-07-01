package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.WaveListMapper;
import com.erp.server.wms.service.WaveListDetailService;
import com.erp.server.wms.service.WaveListFeignService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 波次列表Feign业务类
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListFeignServiceImpl extends SuperServiceImpl<WaveListMapper, WaveListEntity> implements WaveListFeignService {

    @Resource
    private WaveListDetailService waveListDetailService;

    @Override
    public BatchResultDTO add(WaveListDTO.AddDTO dto) {
        WaveListEntity entity = new WaveListEntity();
        entity.setCode(dto.getCode());
        entity.setName("");
        entity.setType(dto.getWaveType());
        entity.setPickingCartCode(dto.getPickCartTypeId());
        entity.setPickType(dto.getPickingType());
        entity.setStatus(WaveStatusEnum.AWAIT_PICK.getCode());
        entity.setPrintStatus("未打印");
        this.save(entity);

        List<String> deliveryIdList = dto.getDeliveryIdList();
        List<WaveListDetailEntity> detailList = new ArrayList<>(deliveryIdList.size());
        for (int i = 0; i < deliveryIdList.size(); i++) {
            String deliveryId = deliveryIdList.get(i);
            WaveListDetailEntity detailEntity = new WaveListDetailEntity();
            detailEntity.setBasketNo(String.valueOf(i + 1));
            detailEntity.setMainId(entity.getId());
            detailEntity.setDeliveryId(deliveryId);
            detailEntity.setDeliveryCode("发货单号");
            detailEntity.setSoId("销售订单id");
            detailEntity.setSoCode("销售订单编号");
            detailEntity.setPickingStatus("拣货状态");
            detailEntity.setLogisticsChannelName("物流渠道");

            detailList.add(detailEntity);
        }

        waveListDetailService.saveBatch(detailList);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.ADD);
    }
}
