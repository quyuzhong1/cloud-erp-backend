package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.server.wms.mapper.WaveListDetailPdaMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.StringUtils;
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
    private PickingListsService pickingListsService;
    @Resource
    private PickingDetailService pickingDetailService;

    @Override
    public Boolean hangUp(WaveListDetailPdaDTO.HangUpParamDTO hangUpDTO) {
        List<PickingDetailEntity> updateList = getPickingDetailEntities(hangUpDTO.getWaveId(), hangUpDTO.getLocationPickingDetailList());
        pickingDetailService.updateBatchById(updateList);
        return Boolean.TRUE;
    }

    @Override
    public WaveListDetailPdaDTO.ViewDTO startPickingWithSideType(String waveId) {
        //构造波次详情列表
        WaveListDetailPdaDTO.ViewDTO resultViewDTO = getViewDTO(waveId);
        waveListService.update(new UpdateWrapper<WaveListEntity>().eq("id", waveId).set("status", WaveStatusEnum.PICK_ING.getCode()));
        return resultViewDTO;
    }

    private WaveListDetailPdaDTO.ViewDTO getViewDTO(String waveId) {
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
        return resultViewDTO;
    }

    @Override
    public WaveListDetailPdaDTO.ViewDTO startPickingWithSequenceType(String waveId) {

        return null;
    }

    @Override
    public WaveListDetailPdaDTO.FinishResultDTO finish(WaveListDetailPdaDTO.FinishParamDTO finishParamDTO) {
        List<PickingDetailEntity> updateList = getPickingDetailEntities(finishParamDTO.getWaveId(), finishParamDTO.getLocationPickingDetailList());
        pickingDetailService.updateBatchById(updateList);
        WaveListDetailPdaDTO.FinishResultDTO resultDTO = new WaveListDetailPdaDTO.FinishResultDTO();
        WaveListDetailPdaDTO.ViewDTO resultViewDTO = getViewDTO(waveId);
        resultDTO.setCode(finishParamDTO.getWaveCode());
        resultDTO.setSkuShouldPickingQty(0);
        resultDTO.setSkuPickedQty(0);
        resultDTO.setGoodsShouldPickingQty(0);
        resultDTO.setGoodsPickedQty(0);
        return resultDTO;
    }

    /**
     * 获取即将更新的拣货单明细
     */
    private List<PickingDetailEntity> getPickingDetailEntities(String finishParamDTO, List<WaveListDetailPdaDTO.PickingLocationDTO> finishParamDTO1) {
        String waveId = finishParamDTO;
        List<WaveListDetailPdaDTO.PickingLocationDTO> pickingLocationList = finishParamDTO1;
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().eq("main_id", waveId));
        List<String> deliveryIds = waveDetailList.stream().map(item -> item.getDeliveryId()).collect(Collectors.toList());
        List<PickingListsDTO.SourceView> pickingBillList = pickingListsService.listBySourceIds(deliveryIds);

        List<PickingDetailEntity> updateList = new ArrayList<>();
        //遍历PDA上显示的每个拣货仓位
        for (WaveListDetailPdaDTO.PickingLocationDTO dto : pickingLocationList) {
            Boolean isOutStock = dto.getIsOutStock();   //业务人员标记是否缺货
            String skuId = dto.getSkuId();  //这个仓位存放的sku
            List<WaveListDetailPdaDTO.BasketDTO> basketList = dto.getBasketList();
            //遍历每个拣货仓位的篮筐
            for (WaveListDetailPdaDTO.BasketDTO basketDTO : basketList) {
                //根据篮筐号拿到发货单
                WaveListDetailEntity waveDetailEntity = waveDetailList.stream().filter(item -> item.getBasketNo().equals(basketDTO.getNo())).findFirst().get();
                String deliveryId = waveDetailEntity.getDeliveryId();
                //根据发货单和sku拿到拣货单明细
                PickingListsDTO.SourceView pickingEntity = pickingBillList.stream().filter(item -> item.getSourceId().equals(deliveryId) && item.getSkuId().equals(skuId)).findFirst().get();
                //更新拣货单上这个sku的已拣数量和缺货状态
                PickingDetailEntity updateDto = new PickingDetailEntity();
                updateDto.setId(pickingEntity.getId());
                updateDto.setPickedQty(basketDTO.getPickedQty());
                updateDto.setIsOutStock(isOutStock);
                updateList.add(updateDto);
            }
        }
        return updateList;
    }
}
