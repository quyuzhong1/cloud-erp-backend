package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.PickingStatusEnum;
import com.erp.rpc.wms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.WaveListDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WaveListDetailService;
import com.erp.server.wms.service.WaveListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WaveListDetailServiceImpl extends SuperServiceImpl<WaveListDetailMapper, WaveListDetailEntity> implements WaveListDetailService {

    @Resource
    private WaveListService waveListService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private OperateLogService operateLogService;

    @Override
    public List<WaveListDetailEntity> listByMainId(String mainId) {
        return list(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getMainId, mainId));
    }

    @Override
    public Map<String, String> getOrderBasketNoMap(List<String> soIds) {
        List<WaveListDetailEntity> waveListDetailEntityList =list(Wrappers.<WaveListDetailEntity>lambdaQuery().in(WaveListDetailEntity::getSoId, soIds));
        return waveListDetailEntityList.stream().collect(Collectors.toMap(WaveListDetailEntity::getSoId, WaveListDetailEntity::getBasketNo, (k1, k2)->k1));
    }

    @Override
    public WaveListDetailDTO.ViewDTO view(String waveId) {
        WaveListEntity waveListEntity = waveListService.getById(waveId);
        WaveListDetailDTO.ViewDTO viewDTO = new WaveListDetailDTO.ViewDTO();
        BeanMapper.copy(waveListEntity, viewDTO);
        List<WaveListDetailEntity> waveDetailList = list(Wrappers.<WaveListDetailEntity>lambdaQuery().eq(WaveListDetailEntity::getMainId, waveId));
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = new ArrayList<>(waveDetailList.size());
        List<String> soIds = waveDetailList.stream().map(WaveListDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(soIds);
        //发货单明细
        for (WaveListDetailEntity waveDetailEntity : waveDetailList) {
            List<SoB2cDetailEntity> soList = soDetailList.stream().filter(item -> item.getMainId().equals(waveDetailEntity.getSoId())).collect(Collectors.toList());
            //销售订单明细
            for (SoB2cDetailEntity soEntity : soList) {
                WaveListDetailDTO.DeliveryInfoDTO deliveryDTO = new WaveListDetailDTO.DeliveryInfoDTO();
                BeanMapper.copy(waveDetailEntity, deliveryDTO);

                deliveryDTO.setSkuId(soEntity.getSkuId());
                deliveryDTO.setSkuNo(soEntity.getSkuNo());
                deliveryDTO.setSalesQty(soEntity.getQty());

                //todo 补充仓位信息
                deliveryList.add(deliveryDTO);
            }
        }
        viewDTO.setDeliveryInfoList(deliveryList);
        viewDTO.setWarehouseId(soDetailList.get(0).getWarehouseId());
        viewDTO.setWarehouseName(soDetailList.get(0).getWarehouseName());

        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<?> moveOut(WaveListDetailDTO.MoveOutDTO moveOutDTO) {
        LoginUser user = UserContext.getNonLoginUser();
        WaveListDetailEntity entity = baseMapper.selectOne(new QueryWrapper<WaveListDetailEntity>()
                .eq("main_id", moveOutDTO.getWaveId())
                .eq("delivery_id", moveOutDTO.getDeliveryId())
        );
        if(! entity.getPickingStatus().equals(PickingStatusEnum.NOT_START.getCode())){
            return ApiResult.error("只能针对未开始的订单移出波次");
        }
        baseMapper.deleteById(entity.getId());
        List<WaveListDetailEntity> detailList = baseMapper.selectList(new QueryWrapper<WaveListDetailEntity>().eq("main_id", moveOutDTO.getWaveId()));
        if(detailList.isEmpty()){
            waveListService.getBaseMapper().deleteById(moveOutDTO.getWaveId());
        }

        operateLogService.addModuleOperateLog(String.format("移除波次中的发货单【%s】", entity.getDeliveryCode()), ModuleTypeEnum.WAREHOUSE_LOCATION_REPLENISH.getCode(), entity.getMainId(), "编辑操作", user.getUid(), user.getRealName());
        return ApiResult.success();
    }
}
