package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.WaveListDetailPdaMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.PickingDetailService;
import com.erp.server.wms.service.WaveListDetailPdaService;
import com.erp.server.wms.service.WaveListDetailService;
import com.erp.server.wms.service.WaveListService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 波次详情（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListDetailPdaServiceImpl extends SuperServiceImpl<WaveListDetailPdaMapper, WaveListDetailEntity> implements WaveListDetailPdaService {

    @Resource
    private WaveListDetailService waveDetailService;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private WaveListService waveListService;
    @Resource
    private PickingDetailService pickingDetailService;

    @Override
    public Boolean hangUp(WaveListDetailPdaDTO.HangUpParamDTO hangUpDTO) {
        String waveId = hangUpDTO.getWaveId();
        String waveCode = hangUpDTO.getWaveCode();
        List<WaveListDetailPdaDTO.PickingLocationDTO> pickingLocationList = hangUpDTO.getLocationPickingDetailList();
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().eq("main_id", waveId));
        List<String> deliveryCodes = waveDetailList.stream().map(item -> item.getDeliveryCode()).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailList = pickingDetailService.list(new QueryWrapper<PickingDetailEntity>().in("source_code", deliveryCodes));
        pickingDetailList.stream().collect(Collectors.toMap(item1 -> item1.get))


        for (WaveListDetailPdaDTO.PickingLocationDTO dto : pickingLocationList) {

        }
    }

    @Override
    public WaveListDetailPdaDTO.ViewDTO startPickingWithSideType(String waveId) {
        //返回波次详情列表
        WaveListDetailDTO.ViewDTO viewDTO = waveDetailService.view(waveId);
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = viewDTO.getDeliveryInfoList();
        Map<String, List<WaveListDetailDTO.DeliveryInfoDTO>> map = deliveryList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseLocation()));
        List<String> skuIds = deliveryList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productList = productDetailService.listByIds(skuIds);
        Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));

        WaveListDetailPdaDTO.ViewDTO resultViewDTO = new WaveListDetailPdaDTO.ViewDTO();
        resultViewDTO.setWaveId(waveId);
        resultViewDTO.setWaveCode(viewDTO.getCode());
        List<WaveListDetailPdaDTO.PickingLocationDTO> resultDetailList = new ArrayList<>();
        for (Map.Entry<String, List<WaveListDetailDTO.DeliveryInfoDTO>> entry : map.entrySet()) {
            List<WaveListDetailDTO.DeliveryInfoDTO> locationGroupList = map.get(entry.getKey());
            WaveListDetailDTO.DeliveryInfoDTO firstDelivery = locationGroupList.get(0);
            WaveListDetailPdaDTO.PickingLocationDTO dto = new WaveListDetailPdaDTO.PickingLocationDTO();
            dto.setWarehouseLocation(entry.getKey());
            dto.setSkuId(firstDelivery.getSkuId());
            dto.setSkuNo(firstDelivery.getSkuNo());
            dto.setProductName(productMap.get(firstDelivery.getSkuId()).getName());
            dto.setIsOutStock(false);   //todo 查询仓位是否缺货
            dto.setPickedTotalQty(0);
            List<WaveListDetailPdaDTO.BasketDTO> basketList = new ArrayList<>();
            for (WaveListDetailDTO.DeliveryInfoDTO deliveryDTO : locationGroupList) {
                WaveListDetailPdaDTO.BasketDTO basket = new WaveListDetailPdaDTO.BasketDTO();
                basket.setNo(deliveryDTO.getBasketNo());
                basket.setShouldPickingQty(deliveryDTO.getShouldPickQty());
                basket.setPickedQty(deliveryDTO.getPickedQty());
                basketList.add(basket);
            }
            dto.setBasketList(basketList);
            int shouldPickTotalQty = basketList.stream().mapToInt(WaveListDetailPdaDTO.BasketDTO::getShouldPickingQty).sum();
            dto.setShouldPickingTotalQty(shouldPickTotalQty);
            resultDetailList.add(dto);
        }
        resultViewDTO.setLocationPickingDetailList(resultDetailList);
        //最后更新波次状态
        waveListService.update(new UpdateWrapper<WaveListEntity>().eq("id", waveId).set("status", WaveStatusEnum.PICK_ING.getCode()));
        return resultViewDTO;
    }

    @Override
    public WaveListDetailPdaDTO.ViewDTO startPickingWithSequenceType(String waveId) {

        return null;
    }
}
