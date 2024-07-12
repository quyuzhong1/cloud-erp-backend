package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.AbnormalCauseEnum;
import com.erp.model.wms.enums.PickingStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.mapper.WaveListDetailPdaMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
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
    @Resource
    private ProductDetailFeign productDetailFeign;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean hangUp(WaveListDetailPdaDTO.HangUpParamDTO hangUpDTO) {
        WaveListEntity old = waveListService.getById(hangUpDTO.getWaveId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "波次列表"));
        if (!StrUtil.equals(old.getStatus(),WaveStatusEnum.PICK_ING.getCode())) {
            throw new ServiceException(StrUtil.format("波次【{}】非拣货中，不支持挂起。",old.getCode()));
        }
        List<PickingDetailEntity> updateList = getPickingDetailEntities(hangUpDTO.getWaveId(), hangUpDTO.getLocationPickingDetailList());
        pickingDetailService.updateBatchById(updateList);

        //更新波次列表状态
        boolean isOutStock = updateList.stream().anyMatch(item -> item.getIsOutStock());
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .eq("id", hangUpDTO.getWaveId())
                .set("status", WaveStatusEnum.HANG_UP.getCode())
                .set(isOutStock, "is_out_stock", true)
        );

        //更新波次明细状态
        WaveListDetailDTO.ViewDTO view = waveDetailService.view(hangUpDTO.getWaveId());
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryInfoList = view.getDeliveryInfoList();
        Set<String> deliveryIds = new HashSet<>();
        for (WaveListDetailDTO.DeliveryInfoDTO dto : deliveryInfoList) {
            if(Objects.equals(dto.getSalesQty(), dto.getPickedSumQty())){
                deliveryIds.add(dto.getDeliveryId());
            }
        }
        if(! deliveryIds.isEmpty()){
            waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>()
                    .set("picking_status", PickingStatusEnum.FINISH.getCode())
                    .in("delivery_id", deliveryIds)
            );
        }

        return Boolean.TRUE;
    }

    @Override
    public WaveListDetailPdaDTO.ViewDTO startPicking(String waveId) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        //构造波次详情列表
        WaveListDetailPdaDTO.ViewDTO resultViewDTO = getViewDTO(waveId);
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .eq("id", waveId)
                .set("status", WaveStatusEnum.PICK_ING.getCode())
                .set("picking_user_id", loginUser.getUid())
                .set("picking_user_name", loginUser.getUserName())
                .set("picking_time", LocalDateTime.now()));
        return resultViewDTO;
    }

    private WaveListDetailPdaDTO.ViewDTO getViewDTO(String waveId) {
        WaveListDetailDTO.ViewDTO viewDTO = waveDetailService.view(waveId);
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = viewDTO.getDeliveryInfoList();
        //准备数据
        HashMap<String, String> skuMap = new HashMap<>();
        deliveryList.forEach(item -> skuMap.put(item.getSkuId(), item.getSkuNo()));

        List<String> skuIds = deliveryList.stream().map(WaveListDetailDTO.DeliveryInfoDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productList = productDetailFeign.listByIds(skuIds);
        Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));

        List<WarehouseLocationEntity> warehouseLocationList = warehouseLocationService.listByWarehouseIds(Collections.singletonList(viewDTO.getWarehouseId()));

        //主信息
        WaveListDetailPdaDTO.ViewDTO resultViewDTO = new WaveListDetailPdaDTO.ViewDTO();
        resultViewDTO.setWaveId(waveId);
        resultViewDTO.setWaveCode(viewDTO.getCode());
        int salesTotalQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getSalesQty).sum();
        resultViewDTO.setShouldPickTotalQty(salesTotalQty);
        int pickedTotalQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getPickedSumQty).sum();
        resultViewDTO.setPickedTotalQty(pickedTotalQty);
        resultViewDTO.setPickingType(viewDTO.getPickingType());

        //需要拣货的所有仓位
        HashMap<String, HashSet<String>> locationSkuMap = new HashMap<>();
        for (WaveListDetailDTO.DeliveryInfoDTO deliveryInfoDTO : deliveryList) {
            List<String> locations = deliveryInfoDTO.getLocationInfoList().stream().map(WaveListDetailDTO.LocationInfoDTO::getWarehouseLocation).collect(Collectors.toList());
            for (String location : locations) {
                if(locationSkuMap.containsKey(location)){
                    locationSkuMap.get(location).add(deliveryInfoDTO.getSkuId());
                }else {
                    HashSet<String> set = new HashSet<>();
                    set.add(deliveryInfoDTO.getSkuId());
                    locationSkuMap.put(location,set);
                }
            }
        }

        //仓位卡片排序
        ArrayList<String> locationList = new ArrayList<>(locationSkuMap.keySet());
        locationList.sort(StringUtils::compare);
        //组装仓位卡片内的数据
        List<WaveListDetailPdaDTO.PickingLocationDTO> locationCardList = new ArrayList<>();
        for (String location : locationList) {
            HashSet<String> skuSet = locationSkuMap.get(location);
            for (String skuId : skuSet) {
                WaveListDetailPdaDTO.PickingLocationDTO card = new WaveListDetailPdaDTO.PickingLocationDTO();
                card.setWarehouseLocation(location);
                WarehouseLocationEntity locationEntity = warehouseLocationList.stream().filter(item -> item.getType().equals("location") && item.getCode().equals(location)).findFirst().orElse(new WarehouseLocationEntity());
                card.setWarehouseLocationName(locationEntity.getName());
                card.setSkuId(skuId);
                card.setSkuNo(skuMap.get(skuId));
                ProductDetailEntity productDetail = productMap.get(skuId);
                card.setSkuImagesUrl(productDetail.getImagesUrl());
                card.setProductName(productDetail.getName());
                //收集skuId，location等于本卡片的发货单
                List<WaveListDetailDTO.DeliveryInfoDTO> deliveryCollect = deliveryList.stream()
                        .filter(item -> {
                            boolean skuEquals = StringUtils.equals(item.getSkuId(), skuId);
                            boolean locationEquals = item.getLocationInfoList().stream().anyMatch(locationInfoDTO -> StringUtils.equals(locationInfoDTO.getWarehouseLocation(), location));
                            return skuEquals && locationEquals;
                        }).collect(Collectors.toList());
                int pickedQty = deliveryCollect.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getPickedSumQty).sum();
                card.setPickedTotalQty(pickedQty);
                int salesQty = deliveryCollect.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getSalesQty).sum();
                card.setShouldPickingTotalQty(salesQty);
                boolean isOutStock = deliveryCollect.stream().anyMatch(item -> item.getLocationInfoList().stream().anyMatch(WaveListDetailDTO.LocationInfoDTO::getIsOutStock));
                card.setIsOutStock(isOutStock);
                //设置每一个篮筐
                List<WaveListDetailPdaDTO.BasketDTO> basketList = new ArrayList<>(deliveryCollect.size());
                for (WaveListDetailDTO.DeliveryInfoDTO delivery : deliveryCollect) {
                    WaveListDetailPdaDTO.BasketDTO basketDTO = new WaveListDetailPdaDTO.BasketDTO();
                    basketDTO.setNo(delivery.getBasketNo());
                    WaveListDetailDTO.LocationInfoDTO locationInfoDTO = delivery.getLocationInfoList().stream().filter(item -> StringUtils.equals(item.getWarehouseLocation(), location)).findAny().get();
                    basketDTO.setPickedQty(locationInfoDTO.getPickedQty());
                    basketDTO.setShouldPickingQty(locationInfoDTO.getShouldPickQty());
                    basketList.add(basketDTO);
                }
                card.setBasketList(basketList);
                locationCardList.add(card);
            }
        }
        resultViewDTO.setLocationPickingDetailList(locationCardList);
        return resultViewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaveListDetailPdaDTO.FinishResultDTO finish(WaveListDetailPdaDTO.FinishParamDTO finishParamDTO) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String waveId = finishParamDTO.getWaveId();
        List<PickingDetailEntity> updateList = getPickingDetailEntities(waveId, finishParamDTO.getLocationPickingDetailList());
        pickingDetailService.updateBatchById(updateList);

        //统计结果
        WaveListDetailPdaDTO.FinishResultDTO resultDTO = new WaveListDetailPdaDTO.FinishResultDTO();
        WaveListDetailDTO.ViewDTO view = waveDetailService.view(waveId);
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = view.getDeliveryInfoList();
        long skuIdsCount = deliveryList.stream().map(WaveListDetailDTO.DeliveryInfoDTO::getSkuId).distinct().count();
        long skuIdsPickedCount = deliveryList.stream().filter(item -> item.getPickedSumQty() != 0).map(item -> item.getSkuId()).distinct().count();
        int salesSumQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getSalesQty).sum();
        int pickedSumQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getPickedSumQty).sum();

        resultDTO.setCode(view.getCode());
        resultDTO.setSkuShouldPickingQty(Long.valueOf(skuIdsCount).intValue());
        resultDTO.setSkuPickedQty(Long.valueOf(skuIdsPickedCount).intValue());
        resultDTO.setGoodsShouldPickingQty(salesSumQty);
        resultDTO.setGoodsPickedQty(pickedSumQty);

        //更新波次列表状态
        boolean isOutStock = updateList.stream().anyMatch(item -> item.getIsOutStock());
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .set("status", WaveStatusEnum.FINISH.getCode())
                .set("picking_time", LocalDateTime.now())
                .set("picking_user_id", loginUser.getUid())
                .set("picking_user_name", loginUser.getUserName())
                .set(isOutStock, "is_out_stock", true)
                .eq("id", finishParamDTO.getWaveId()));
//        waveListService.updateStatusById(finishParamDTO.getWaveId(),WaveStatusEnum.FINISH.getCode());
        return resultDTO;
    }

    @Override
    public ApiResult<?> scanSkuOrEanCode(String skuId, String code) {
        ProductDetailDTO.ServiceToWavePickingDTO productInfo = productDetailFeign.getProductInfoBySkuId(skuId);
        if(StringUtils.isNotBlank(productInfo.getSkuNo()) && productInfo.getSkuNo().equals(code)){
            return ApiResult.success();
        }
        if(StringUtils.isNotBlank(productInfo.getEanNo()) && productInfo.getEanNo().equals(code)){
            return ApiResult.success();
        }
        return ApiResult.error("SKU不一致");
    }

    /**
     * 获取即将更新的拣货单明细
     */
    private List<PickingDetailEntity> getPickingDetailEntities(String waveId, List<WaveListDetailPdaDTO.PickingLocationDTO> pickingLocationList) {
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().eq("main_id", waveId));
        List<String> deliveryIds = waveDetailList.stream().map(item -> item.getDeliveryId()).collect(Collectors.toList());
        List<PickingListsDTO.SourceView> pickingBillList = pickingListsService.listBySourceIds(deliveryIds);
        List<String> pickingIds = pickingBillList.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetails = pickingDetailService.list(new QueryWrapper<PickingDetailEntity>().in("main_id", pickingIds));

        List<PickingDetailEntity> updateList = new ArrayList<>();
        Set<String> pickedDeliveryIds = new HashSet<>();
        //遍历PDA上显示的每个拣货仓位
        for (WaveListDetailPdaDTO.PickingLocationDTO dto : pickingLocationList) {
            Boolean isOutStock = dto.getIsOutStock();   //业务人员标记是否缺货
            String skuId = dto.getSkuId();  //这个仓位存放的sku
            List<WaveListDetailPdaDTO.BasketDTO> basketList = dto.getBasketList();
            List<String> outStockDeliveryIds = new ArrayList<>();
            //遍历每个拣货仓位的篮筐
            for (WaveListDetailPdaDTO.BasketDTO basketDTO : basketList) {
                //根据篮筐号拿到发货单
                WaveListDetailEntity waveDetailEntity = waveDetailList.stream().filter(item -> item.getBasketNo().equals(basketDTO.getNo())).findFirst().get();
                String deliveryId = waveDetailEntity.getDeliveryId();
                //根据发货单和sku拿到拣货单
                PickingListsDTO.SourceView pickingEntity = pickingBillList.stream().filter(item -> item.getSourceId().equals(deliveryId) && item.getSkuId().equals(skuId)).findFirst().get();
                PickingDetailEntity pickingDetail = pickingDetails.stream().filter(item -> StringUtils.equals(item.getMainId(), pickingEntity.getId()) && StringUtils.equals(item.getSkuId(), skuId)).findFirst().get();
                //更新拣货单上这个sku的已拣数量和缺货状态
                PickingDetailEntity updateDto = new PickingDetailEntity();
                updateDto.setId(pickingDetail.getId());
                updateDto.setPickedQty(basketDTO.getPickedQty());
                updateDto.setIsOutStock(isOutStock);
                updateList.add(updateDto);

                outStockDeliveryIds.add(deliveryId);
                if(basketDTO.getPickedQty() > 0){
                    pickedDeliveryIds.add(deliveryId);
                }
            }
            //仓位标记缺货，则所有涉及的发货单都置为异常
            if(isOutStock){
                soB2cDeliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>()
                        .set("status", SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode())
                        .set("abnormal_cause", AbnormalCauseEnum.PICK_MARKINGS.getCode())
                        .in("id", outStockDeliveryIds));
            }
        }
        //将有已拣数量的发货单标记为拣货中
        if(! pickedDeliveryIds.isEmpty()){
            waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>()
                    .set("picking_status", PickingStatusEnum.PICK_ING.getCode())
                    .in("delivery_id", pickedDeliveryIds));
        }
        return updateList;
    }
}
