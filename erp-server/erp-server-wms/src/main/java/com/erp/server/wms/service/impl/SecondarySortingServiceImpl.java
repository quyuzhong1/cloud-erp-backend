package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.renovation.SecondarySortingDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoB2cDeliveryPrintTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecondarySortingServiceImpl implements SecondarySortingService {

    @Resource
    private WaveListService waveListService;
    @Resource
    private WaveListDetailService waveListDetailService;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private PickingDetailService pickingDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
    public SecondarySortingDTO.ScanCodeView scanCode(String code) {
        WaveListEntity pickingWave = waveListService.getByCodeOrCarCode(code);
        if (ObjectUtils.isEmpty(pickingWave)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, code);
        }
        SecondarySortingDTO.ScanCodeView view = new SecondarySortingDTO.ScanCodeView();
        view.setWaveId(pickingWave.getId());
        view.setCode(pickingWave.getCode());
        List<WaveListDetailEntity> details = waveListDetailService.listByMainId(pickingWave.getId());
        List<String> deliveryIds = details.stream().map(WaveListDetailEntity::getDeliveryId).collect(Collectors.toList());
        List<String> soIds = details.stream().map(WaveListDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIds);
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(deliveryIds);
        view.setBasketQty(details.size());
        List<SecondarySortingDTO.BasketDTO> dtoList = details.stream()
                .map(v -> {
                    SoB2cEntity soB2cEntity = soB2cList.stream().filter(e -> e.getId().equals(v.getSoId())).findFirst().orElse(new SoB2cEntity());
                    int qty = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).mapToInt(PickingListsDTO.SourceView::getQty).sum();
                    int allocatedQty = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).mapToInt(PickingListsDTO.SourceView::getAllocatedQty).sum();
                    boolean isOutStock = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).anyMatch(PickingListsDTO.SourceView::getIsOutStock);
                    SecondarySortingDTO.BasketDTO dto = new SecondarySortingDTO.BasketDTO();
                    dto.setBasketNo(v.getBasketNo());
                    dto.setPickingQty(qty);
                    dto.setAllocatedQty(allocatedQty);
                    dto.setIsIntercept(soB2cEntity.getIsIntercept());
                    dto.setIsOutStock(isOutStock);
                    return dto;
                }).collect(Collectors.toList());
        view.setBasketDetails(dtoList);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecondarySortingDTO.ScanSkuView scanSku(String waveId, String skuCode) {
        if (ObjectUtils.isEmpty(waveId) || ObjectUtils.isEmpty(skuCode)) {
            throw new ServiceException(ApiError.ERROR_WMS_WAVE_NO_AND_SKU_REQUIRED);
        }
        ProductDetailEntity productDetail = plmTaskFeign.getBySkuNoOrEan(skuCode);
        if (ObjectUtils.isEmpty(productDetail)) {
            throw new ServiceException(ApiError.ERROR_PLM_SKU_NOT_FOUND);
        }
        SecondarySortingDTO.ScanSkuView view = new SecondarySortingDTO.ScanSkuView();
        List<WaveListDTO.PickingWaveDetailDTO> waveDetailList = waveListService.listDetailByMainId(waveId, null, productDetail.getId());
        List<WaveListDTO.PickingWaveDetailDTO> skuList = waveDetailList.stream()
                .filter(v -> v.getSkuId().equals(productDetail.getId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_WMS_WAVE_NOT_FOUND_SKU);
        }
        int sum = skuList.stream().mapToInt(WaveListDTO.PickingWaveDetailDTO::getPickedQty).sum();
        if (sum == 0) {
            throw new ServiceException(ApiError.ERROR_WMS_SKU_NOT_PICKED_CANNOT_ALLOCATE);
        }
        WaveListDTO.PickingWaveDetailDTO detailDTO = skuList.stream()
                .filter(v -> v.getPickedQty() > v.getAllocatedQty())
                .min(Comparator.comparing(v -> Integer.parseInt(v.getBasketNo())))
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_WMS_SKU_FULLY_ALLOCATED, productDetail.getSkuNo()));
        int pickingQty = waveDetailList.stream().mapToInt(WaveListDTO.PickingWaveDetailDTO::getPickedQty).sum();
        int allocatedQty = Optional.ofNullable(detailDTO.getAllocatedQty()).orElse(0);
        view.setBasketNo(detailDTO.getBasketNo());
        view.setSkuId(detailDTO.getSkuId());
        view.setSkuNo(detailDTO.getSkuNo());
        view.setAllocatedQty(allocatedQty + 1);
        view.setPickingQty(pickingQty);
        pickingDetailService.update(Wrappers.<PickingDetailEntity>lambdaUpdate()
                .set(PickingDetailEntity::getAllocatedQty, allocatedQty + 1)
                .eq(PickingDetailEntity::getId, detailDTO.getPickDetailId())
        );
        return view;
    }

    @Override
    public List<SecondarySortingDTO.BasketDetail> basketDetail(String waveId, String basketNo) {
        if (ObjectUtils.isEmpty(waveId) || ObjectUtils.isEmpty(basketNo)) {
            throw new ServiceException(ApiError.ERROR_WMS_WAVE_NO_AND_BASKET_REQUIRED);
        }
        List<WaveListDTO.PickingWaveDetailDTO> waveDetailDTOS = waveListService.listDetailByMainId(waveId, basketNo);
        Map<String, List<WaveListDTO.PickingWaveDetailDTO>> skuMap = waveDetailDTOS.stream().collect(Collectors.groupingBy(WaveListDTO.PickingWaveDetailDTO::getSkuId));
        List<SkuVO> vos = plmTaskFeign.listSkuPurchaseByIds(new ArrayList<>(skuMap.keySet()));
        return skuMap.values()
                .stream().map(v -> {
                    WaveListDTO.PickingWaveDetailDTO detailDTO = v.stream().findFirst().orElse(new WaveListDTO.PickingWaveDetailDTO());
                    SkuVO skuVO = vos.stream().filter(e -> e.getSkuId().equals(detailDTO.getSkuId())).findFirst().orElse(new SkuVO());
                    int pickingQty = v.stream().mapToInt(WaveListDTO.PickingWaveDetailDTO::getPickedQty).sum();
                    int allocatedQty = v.stream().mapToInt(WaveListDTO.PickingWaveDetailDTO::getAllocatedQty).sum();
                    SecondarySortingDTO.BasketDetail detail = new SecondarySortingDTO.BasketDetail();
                    detail.setBasketNo(detailDTO.getBasketNo());
                    detail.setSkuId(detailDTO.getSkuId());
                    detail.setSkuNo(detailDTO.getSkuNo());
                    detail.setEan(skuVO.getEan());
                    detail.setAllocatedQty(allocatedQty);
                    detail.setPickingQty(pickingQty);
                    return detail;
                }).collect(Collectors.toList());
    }

    @Override
    public void printDistribution(String waveId, HttpServletResponse response) {
        if (ObjectUtils.isEmpty(waveId)) {
            throw new ServiceException(ApiError.ERROR_WMS_WAVE_NO_REQUIRED);
        }
        List<WaveListDetailEntity> waveDetailEntities = waveListDetailService.listByMainId(waveId);
        List<String> deliveryIds = waveDetailEntities.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam();
        param.setPrintType(SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode());
        param.setIds(deliveryIds);
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillDTOList = soB2cDeliveryService.printLogisticsWaybillPreview(param);
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> printDetailDTOList = printLogisticsWaybillDTOList.stream().map(SoB2cDeliveryDTO.PrintLogisticsWaybillDTO::getDetailList).flatMap(Collection::stream).collect(Collectors.toList());
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO();
        dto.setPrintType(SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode());
        dto.setDetailList(printDetailDTOList);
        soB2cDeliveryService.printLogisticsBillConfirm(dto,response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecondarySortingDTO.ScanCodeView reset(String waveId) {
        if (ObjectUtils.isEmpty(waveId)) {
            throw new ServiceException(ApiError.ERROR_WMS_WAVE_NO_REQUIRED);
        }
        WaveListEntity pickingWave = waveListService.getById(waveId);
        List<WaveListDTO.PickingWaveDetailDTO> waveDetailDTOS = waveListService.listDetailByMainId(waveId);
        List<String> detailIds = waveDetailDTOS.stream().map(WaveListDTO.PickingWaveDetailDTO::getPickDetailId).collect(Collectors.toList());
        pickingDetailService.update(Wrappers.<PickingDetailEntity>lambdaUpdate()
                .set(PickingDetailEntity::getAllocatedQty, 0)
                .in(PickingDetailEntity::getId, detailIds)
        );
        return scanCode(pickingWave.getCode());
    }
}
