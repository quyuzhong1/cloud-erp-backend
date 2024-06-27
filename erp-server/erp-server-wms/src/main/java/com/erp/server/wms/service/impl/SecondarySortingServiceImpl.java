package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.dto.renovation.PickingWaveDTO;
import com.erp.model.wms.dto.renovation.SecondarySortingDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecondarySortingServiceImpl implements SecondarySortingService {

    @Resource
    private PickingWaveService pickingWaveService;
    @Resource
    private PickingWaveDetailService pickingWaveDetailService;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private PickingDetailService pickingDetailService;
    @Resource
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Override
    public SecondarySortingDTO.ScanCodeView scanCode(String code) {
        PickingWaveEntity pickingWave = pickingWaveService.getByCodeOrCarCode(code);
        if (ObjectUtils.isEmpty(pickingWave)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, code);
        }
        SecondarySortingDTO.ScanCodeView view = new SecondarySortingDTO.ScanCodeView();
        view.setWaveId(pickingWave.getId());
        view.setCode(pickingWave.getCode());
        List<PickingWaveDetailEntity> details = pickingWaveDetailService.listByMainId(pickingWave.getId());
        List<String> deliveryIds = details.stream().map(PickingWaveDetailEntity::getDeliveryId).collect(Collectors.toList());
        List<SoB2cDeliveryInterceptEntity> interceptList = soB2cDeliveryInterceptService.listByDeliveryIds(deliveryIds);
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(deliveryIds);
        view.setBasketQty(details.size());
        List<SecondarySortingDTO.BasketDTO> dtoList = details.stream()
                .map(v -> {
                    SoB2cDeliveryInterceptEntity intercept = interceptList.stream().findFirst().orElse(null);
                    int qty = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).mapToInt(PickingListsDTO.SourceView::getQty).sum();
                    int allocatedQty = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).mapToInt(PickingListsDTO.SourceView::getAllocatedQty).sum();
                    boolean isOutStock = views.stream().filter(e -> e.getSourceId().equals(v.getDeliveryId())).anyMatch(PickingListsDTO.SourceView::getIsOutStock);
                    SecondarySortingDTO.BasketDTO dto = new SecondarySortingDTO.BasketDTO();
                    dto.setBasketNo(v.getBasketNo());
                    dto.setPickingQty(qty);
                    dto.setAllocatedQty(allocatedQty);
                    if (!ObjectUtils.isEmpty(intercept) && SoB2cDeliveryInterceptStatusEnum.CANCEL.getCode().equals(intercept.getHandleStatus())) {
                        dto.setIsIntercept(true);
                    } else {
                        dto.setIsIntercept(false);
                    }
                    dto.setIsOutStock(isOutStock);
                    return dto;
                }).collect(Collectors.toList());
        view.setBasketDetails(dtoList);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecondarySortingDTO.ScanSkuView scanSku(String waveId, String skuCode) {

        ProductDetailEntity productDetail = plmTaskFeign.getBySkuNoOrEan(skuCode);
        SecondarySortingDTO.ScanSkuView view = new SecondarySortingDTO.ScanSkuView();
        List<PickingWaveDTO.PickingWaveDetailDTO> waveDetailList = pickingWaveService.listDetailByMainId(waveId, null, productDetail.getId());
        PickingWaveDTO.PickingWaveDetailDTO detailDTO = waveDetailList.stream()
                .filter(v -> v.getSkuId().equals(productDetail.getId()))
                .filter(v -> v.getPickedQty() > v.getAllocatedQty())
                .findFirst()
                .orElseThrow(() -> new ServiceException(ApiError.ERROR_99111));
        int pickingQty = waveDetailList.stream().mapToInt(PickingWaveDTO.PickingWaveDetailDTO::getPickedQty).sum();
        int allocatedQty = waveDetailList.stream().mapToInt(PickingWaveDTO.PickingWaveDetailDTO::getAllocatedQty).sum();
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
        List<PickingWaveDTO.PickingWaveDetailDTO> waveDetailDTOS = pickingWaveService.listDetailByMainId(waveId, basketNo);
        Map<String, List<PickingWaveDTO.PickingWaveDetailDTO>> skuMap = waveDetailDTOS.stream().collect(Collectors.groupingBy(PickingWaveDTO.PickingWaveDetailDTO::getSkuId));
        List<SkuVO> vos = plmTaskFeign.listSkuPurchaseByIds(new ArrayList<>(skuMap.keySet()));
        return skuMap.values()
                .stream().map(v -> {
                    PickingWaveDTO.PickingWaveDetailDTO detailDTO = v.stream().findFirst().orElse(new PickingWaveDTO.PickingWaveDetailDTO());
                    SkuVO skuVO = vos.stream().filter(e -> e.getSkuId().equals(detailDTO.getSkuId())).findFirst().orElse(new SkuVO());
                    int pickingQty = v.stream().mapToInt(PickingWaveDTO.PickingWaveDetailDTO::getPickedQty).sum();
                    int allocatedQty = v.stream().mapToInt(PickingWaveDTO.PickingWaveDetailDTO::getAllocatedQty).sum();
                    SecondarySortingDTO.BasketDetail detail = new SecondarySortingDTO.BasketDetail();
                    detail.setSkuId(detailDTO.getSkuId());
                    detail.setSkuNo(detailDTO.getSkuNo());
                    detail.setEan(skuVO.getEan());
                    detail.setAllocatedQty(allocatedQty);
                    detail.setPickingQty(pickingQty);
                    return detail;
                }).collect(Collectors.toList());
    }

    @Override
    public void printDistribution(String code, HttpServletResponse response) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecondarySortingDTO.ScanCodeView reset(String waveId) {
        PickingWaveEntity pickingWave = pickingWaveService.getById(waveId);
        List<PickingWaveDTO.PickingWaveDetailDTO> waveDetailDTOS = pickingWaveService.listDetailByMainId(waveId);
        List<String> detailIds = waveDetailDTOS.stream().map(PickingWaveDTO.PickingWaveDetailDTO::getPickDetailId).collect(Collectors.toList());
        pickingDetailService.update(Wrappers.<PickingDetailEntity>lambdaUpdate()
                .set(PickingDetailEntity::getAllocatedQty, 0)
                .in(PickingDetailEntity::getId, detailIds)
        );
        return scanCode(pickingWave.getCode());
    }
}
