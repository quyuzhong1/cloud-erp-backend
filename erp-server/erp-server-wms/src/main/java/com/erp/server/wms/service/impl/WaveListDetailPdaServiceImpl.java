package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.erp.model.wms.enums.*;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.mapper.WaveListDetailPdaMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import org.apache.commons.lang3.ObjectUtils;
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
        List<PickingDetailEntity> updateList = getPickingDetailEntities(hangUpDTO);
        pickingDetailService.updateBatchById(updateList);

        //更新波次列表状态
        boolean isOutStock = updateList.stream().anyMatch(PickingDetailEntity::getIsOutStock);
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .eq("id", hangUpDTO.getWaveId())
                .set("status", WaveStatusEnum.HANG_UP.getCode())
                .set("is_out_stock", isOutStock)
        );

        //更新波次明细状态
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().eq("main_id", hangUpDTO.getWaveId()));
        for (WaveListDetailEntity waveDetail : waveDetailList) {
            List<PickingDetailEntity> pickingDetailList = getPickingDetailByDeliveryId(waveDetail.getDeliveryId());
            int qty = pickingDetailList.stream().mapToInt(PickingDetailEntity::getQty).sum();
            int pickedQty = pickingDetailList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
            if(pickedQty == qty){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.FINISH.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
            if(pickedQty < qty && pickedQty != 0){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.PICK_ING.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
            if(pickedQty == 0){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.NOT_START.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
        }

//        WaveListDetailDTO.ViewDTO view = waveDetailService.view(hangUpDTO.getWaveId());
//        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryInfoList = view.getDeliveryInfoList();
//        Set<String> deliveryIds = new HashSet<>();
//        for (WaveListDetailDTO.DeliveryInfoDTO dto : deliveryInfoList) {
//            if(Objects.equals(dto.getSalesQty(), dto.getPickedSumQty())){
//                deliveryIds.add(dto.getDeliveryId());
//            }
//        }
//        if(! deliveryIds.isEmpty()){
//            waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>()
//                    .set("picking_status", PickingStatusEnum.FINISH.getCode())
//                    .in("delivery_id", deliveryIds)
//            );
//        }

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

    /**
     * 通过发货单号查询发货单关联的所有拣货单明细
     */
    private List<PickingDetailEntity> getPickingDetailByDeliveryId(String deliveryId){
        PickingListsEntity pickingEntity = pickingListsService.getOne(new QueryWrapper<PickingListsEntity>().eq("source_id", deliveryId));
        List<PickingDetailEntity> pickingDetailList = pickingDetailService.list(new QueryWrapper<PickingDetailEntity>().eq("main_id", pickingEntity.getId()));
        return pickingDetailList;
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
                    List<WaveListDetailDTO.LocationInfoDTO> collect = delivery.getLocationInfoList().stream().filter(item -> StringUtils.equals(item.getWarehouseLocation(), location)).collect(Collectors.toList());
                    basketDTO.setPickedQty(collect.stream().mapToInt(WaveListDetailDTO.LocationInfoDTO::getPickedQty).sum());
                    basketDTO.setShouldPickingQty(collect.stream().mapToInt(WaveListDetailDTO.LocationInfoDTO::getShouldPickQty).sum());
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
        List<PickingDetailEntity> updateList = getPickingDetailEntities(finishParamDTO);
        pickingDetailService.updateBatchById(updateList);

        //统计结果
        WaveListDetailPdaDTO.FinishResultDTO resultDTO = new WaveListDetailPdaDTO.FinishResultDTO();
        WaveListDetailDTO.ViewDTO view = waveDetailService.view(waveId);
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = view.getDeliveryInfoList();
        long skuIdsCount = deliveryList.stream().map(WaveListDetailDTO.DeliveryInfoDTO::getSkuId).distinct().count();
        long skuIdsPickedCount = deliveryList.stream().filter(item -> item.getPickedSumQty() != 0).map(WaveListDetailDTO.DeliveryInfoDTO::getSkuId).distinct().count();
        int salesSumQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getSalesQty).sum();
        int pickedSumQty = deliveryList.stream().mapToInt(WaveListDetailDTO.DeliveryInfoDTO::getPickedSumQty).sum();

        resultDTO.setCode(view.getCode());
        resultDTO.setSkuShouldPickingQty(Long.valueOf(skuIdsCount).intValue());
        resultDTO.setSkuPickedQty(Long.valueOf(skuIdsPickedCount).intValue());
        resultDTO.setGoodsShouldPickingQty(salesSumQty);
        resultDTO.setGoodsPickedQty(pickedSumQty);

        //更新波次列表状态
        boolean isOutStock = updateList.stream().anyMatch(PickingDetailEntity::getIsOutStock);
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .set("status", WaveStatusEnum.FINISH.getCode())
                .set("picking_time", LocalDateTime.now())
                .set("picking_user_id", loginUser.getUid())
                .set("picking_user_name", loginUser.getUserName())
                .set("is_out_stock", isOutStock)
                .eq("id", finishParamDTO.getWaveId()));
        //更新波次明细状态
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().eq("main_id", finishParamDTO.getWaveId()));
        for (WaveListDetailEntity waveDetail : waveDetailList) {
            List<PickingDetailEntity> pickingDetailList = getPickingDetailByDeliveryId(waveDetail.getDeliveryId());
            int qty = pickingDetailList.stream().mapToInt(PickingDetailEntity::getQty).sum();
            int pickedQty = pickingDetailList.stream().mapToInt(PickingDetailEntity::getPickedQty).sum();
            if(pickedQty == qty){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.FINISH.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
            if(pickedQty < qty && pickedQty != 0){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.PICK_ING.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
            if(pickedQty == 0){
                waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>().set("picking_status", PickingStatusEnum.NOT_START.getCode()).eq("delivery_id", waveDetail.getDeliveryId()));
            }
        }

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
     * 获取PDA的已拣数量明细
     */
    private <T extends WaveListDetailPdaDTO.ViewDTO> List<PickingDetailEntity> getPickingDetailEntities(T hangUpDTO) {
        String waveId = hangUpDTO.getWaveId();
        List<WaveListDetailPdaDTO.PickingLocationDTO> pickingLocationList = hangUpDTO.getLocationPickingDetailList();

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
            Integer pickedTotalQty = dto.getPickedTotalQty();
            if(ObjectUtils.isEmpty(pickedTotalQty)){
                pickedTotalQty = 0;
            }
            List<String> outStockDeliveryIds = new ArrayList<>();
            //遍历每个拣货仓位的篮筐
            for (WaveListDetailPdaDTO.BasketDTO basketDTO : basketList) {
                //根据篮筐号拿到发货单
                Optional<WaveListDetailEntity> first = waveDetailList.stream().filter(item -> item.getBasketNo().equals(basketDTO.getNo())).findFirst();
                if(! first.isPresent()){
                    //波次在拣货的过程中，发货单被移出波次
                    continue;
                }
                String deliveryId = first.get().getDeliveryId();
                //根据发货单和sku拿到拣货单
                PickingListsDTO.SourceView pickingEntity = pickingBillList.stream().filter(item -> item.getSourceId().equals(deliveryId) && item.getSkuId().equals(skuId)).findFirst().get();
                PickingDetailEntity pickingDetail = pickingDetails.stream().filter(item -> StringUtils.equals(item.getMainId(), pickingEntity.getId()) && StringUtils.equals(item.getSkuId(), skuId)).findFirst().get();
                //更新拣货单上这个sku的已拣数量和缺货状态
                PickingDetailEntity updateDto = new PickingDetailEntity();
                updateDto.setId(pickingDetail.getId());
                if(StringUtils.equals(hangUpDTO.getPickingType(), WavePickingTypeEnum.FIRST_PICK.getCode())){
                    if(pickedTotalQty >= basketDTO.getShouldPickingQty()){
                        basketDTO.setPickedQty(basketDTO.getShouldPickingQty());
                        pickedTotalQty -= basketDTO.getPickedQty();
                    } else if (pickedTotalQty >= 0) {
                        basketDTO.setPickedQty(pickedTotalQty);
                        pickedTotalQty = 0;
                    }
                }
                updateDto.setPickedQty(ObjectUtils.isEmpty(basketDTO.getPickedQty()) ? 0 : basketDTO.getPickedQty());
                updateDto.setIsOutStock(isOutStock);
                updateList.add(updateDto);

                outStockDeliveryIds.add(deliveryId);
                if(updateDto.getPickedQty() > 0){
                    pickedDeliveryIds.add(deliveryId);
                }
            }
            //仓位标记缺货，则所有涉及的发货单都置为异常，但如果发货单状态为已发货，那么跳过
            if(isOutStock){
                soB2cDeliveryService.update(new UpdateWrapper<SoB2cDeliveryEntity>()
                        .set("status", SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode())
                        .set("abnormal_cause", AbnormalCauseEnum.PICK_MARKINGS.getCode())
                        .in("id", outStockDeliveryIds)
                        .ne("status", SoB2cDeliveryStatusEnum.SHIPPED.getCode()));
            }
        }
        //将有已拣数量的发货单标记为拣货中
//        if(! pickedDeliveryIds.isEmpty()){
//            waveDetailService.update(new UpdateWrapper<WaveListDetailEntity>()
//                    .set("picking_status", PickingStatusEnum.PICK_ING.getCode())
//                    .in("delivery_id", pickedDeliveryIds));
//        }
        return updateList;
    }
}
