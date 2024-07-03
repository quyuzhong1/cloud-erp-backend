package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.WaveListDetailDTO;
import com.erp.model.wms.dto.WaveListDetailPdaDTO;
import com.erp.model.wms.dto.WaveListPdaDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.model.wms.entity.WaveListDetailEntity;
import com.erp.model.wms.entity.WaveListEntity;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.rpc.wms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.WaveListPdaMapper;
import com.erp.server.wms.pull.service.ProductDetailService;
import com.erp.server.wms.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 波次列表（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Service
public class WaveListPdaServiceImpl extends SuperServiceImpl<WaveListPdaMapper, WaveListEntity> implements WaveListPdaService {
    @Resource
    private WaveListService waveListService;
    @Resource
    private WaveListDetailService waveDetailService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private PickingCartService pickingCartService;
    @Resource
    private PickingCartTypeService pickingCartTypeService;

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

    @Override
    public PagingVO<WaveListPdaDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO) {
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WaveListEntity> result = this.baseMapper.paging(page, pagingDTO.getParams());
        List<WaveListPdaDTO.ViewDTO> viewDTOList = fillViewList(result.getRecords());
        return new PagingVO<>(viewDTOList, (int)result.getTotal(), (int)result.getSize(), (int)result.getCurrent());
    }

    @Override
    public WaveListPdaDTO.WaveBasicInfoDTO waveInfo(String waveId) {
        WaveListEntity waveListEntity = waveListService.getById(waveId);
        WaveListPdaDTO.WaveBasicInfoDTO viewDTO = new WaveListPdaDTO.WaveBasicInfoDTO();
        BeanMapper.copy(waveListEntity, viewDTO);
        fillWaveInfo(viewDTO);
        return viewDTO;
    }

    @Override
    public List<WaveListPdaDTO.ProductDetailDTO> productDetail(String waveId) {
        WaveListDetailDTO.ViewDTO view = waveDetailService.view(waveId);
        HashMap<String, Integer> salesQtyMap = new HashMap<>();
        HashMap<String, Integer> pickedTotalQtyMap = new HashMap<>();
        List<WaveListDetailDTO.DeliveryInfoDTO> deliveryList = view.getDeliveryInfoList();
        List<String> skuIds = deliveryList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productList = productDetailService.listByIds(skuIds);
        Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(item1 -> item1.getId(), item2 -> item2));
        for (WaveListDetailDTO.DeliveryInfoDTO deliveryDto : deliveryList) {
            String skuId = deliveryDto.getSkuId();
            if(salesQtyMap.containsKey(skuId)){
                salesQtyMap.put(skuId, salesQtyMap.get(skuId) + deliveryDto.getSalesQty());
            }else {
                salesQtyMap.put(skuId, 0);
            }

            if(pickedTotalQtyMap.containsKey(skuId)){
                pickedTotalQtyMap.put(skuId, pickedTotalQtyMap.get(skuId) + deliveryDto.getPickedSumQty());
            }else {
                pickedTotalQtyMap.put(skuId, 0);
            }
        }

        List<WaveListPdaDTO.ProductDetailDTO> productDetailList = new ArrayList<>();
        for (String skuId : skuIds) {
            ProductDetailEntity product = productMap.get(skuId);
            WaveListPdaDTO.ProductDetailDTO dto = new WaveListPdaDTO.ProductDetailDTO();
            dto.setSkuId(skuId);
            dto.setSkuNo(product.getSkuNo());
            dto.setProductName(product.getName());
            if(salesQtyMap.containsKey(skuId)){
                dto.setShouldPickTotalQty(salesQtyMap.get(skuId));
            }
            if(pickedTotalQtyMap.containsKey(skuId)){
                dto.setPickedTotalQty(pickedTotalQtyMap.get(skuId));
            }
            dto.setVariantProperty(product.getVariantProperty());
            dto.setImageUrl(product.getImagesUrl());
            dto.setRemark("");

            productDetailList.add(dto);
        }

        return productDetailList;
    }

    @Override
    public ApiResult<?> bindPickingCart(WaveListPdaDTO.BindPickingCartDTO bindDTO) {
        //核对扫描的拣货车类型是否匹配，核对失败返回错误
        PickingCartEntity pickingCart = pickingCartService.getOne(new QueryWrapper<PickingCartEntity>().eq("code", bindDTO.getPickingCartCode()));
        PickingCartTypeEntity pickingCartType = pickingCartTypeService.getOne(new QueryWrapper<PickingCartTypeEntity>().eq("id", pickingCart.getTypeId()));
        WaveListEntity waveEntity = waveListService.getById(bindDTO.getId());
        if(! waveEntity.getPickingCartType().equals(pickingCartType.getName())){
            return ApiResult.error("拣货车不匹配");
        }
        //核对成功返回拣货车信息
        bindDTO.setPickingCartName("");
        bindDTO.setPickingCartType(pickingCartType.getName());
        bindDTO.setPickingType(waveEntity.getPickingType());
        return ApiResult.success(bindDTO);
    }

    private void fillWaveInfo(WaveListPdaDTO.WaveBasicInfoDTO viewDTO) {
        //todo
        viewDTO.setPickingTypeName("");
        viewDTO.setWarehouseId("");
        viewDTO.setWarehouseName("");
        viewDTO.setStatusName(WaveStatusEnum.getNameByCode(viewDTO.getStatus()));
    }

    private List<WaveListPdaDTO.ViewDTO> fillViewList(List<WaveListEntity> records) {
        if(records.isEmpty()){
            return Collections.emptyList();
        }

        List<String> ids = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<WaveListDetailEntity> detailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().in("main_id", ids));
        Map<String, List<WaveListDetailEntity>> waveDetailMap = detailList.stream().collect(Collectors.groupingBy(item -> item.getMainId()));
        List<WaveListPdaDTO.ViewDTO> viewList = new ArrayList<>();
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(ids);
        Map<String, List<SoB2cDetailEntity>> soDetailMap = soDetailList.stream().collect(Collectors.groupingBy(item -> item.getMainId()));
        for (WaveListEntity record : records) {
            WaveListPdaDTO.ViewDTO view = new WaveListPdaDTO.ViewDTO();
            BeanMapper.copy(record, view);

            view.setStatusName(WaveStatusEnum.getNameByCode(view.getStatus()));

            List<WaveListDetailEntity> list = waveDetailMap.get(record.getId());
            List<String> deliveryIds = list.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
            view.setDeliveryBillQty(deliveryIds.size());

            List<SoB2cDetailEntity> soB2cDetails = soDetailMap.get(record.getId());
            List<String> skuIds = soB2cDetails.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
            view.setSkuQty(skuIds.size());

            int goodsQty = soB2cDetails.stream().mapToInt(item -> item.getQty()).sum();
            view.setGoodsQty(goodsQty);
            viewList.add(view);
        }

        return viewList;
    }
}
