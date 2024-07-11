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
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.WavePickingTypeEnum;
import com.erp.model.wms.enums.WaveStatusEnum;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.rpc.wms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.WaveListCartTypeMapper;
import com.erp.server.wms.mapper.WaveListPdaMapper;
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
    private PickingCartService pickingCartService;
    @Resource
    private PickingCartTypeService pickingCartTypeService;
    @Resource
    private ProductDetailFeign productDetailFeign;
    @Resource
    private WaveListCartTypeMapper waveListCartTypeMapper;
    @Resource
    private PickingListsService pickingListsService;

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
        List<ProductDetailEntity> productList = productDetailFeign.listByIds(skuIds);
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
        PickingCartEntity pickingCart = pickingCartService.getOne(new QueryWrapper<PickingCartEntity>().eq("code", bindDTO.getPickingCartCode()));
        if(pickingCart == null){
            return ApiResult.error("拣货车编码错误");
        }
        if(pickingCart.getDisabled()){
            return ApiResult.error("拣货车已禁用");
        }
        PickingCartTypeEntity pickingCartType = pickingCartTypeService.getOne(new QueryWrapper<PickingCartTypeEntity>().eq("id", pickingCart.getTypeId()));
        WaveListEntity waveEntity = waveListService.getById(bindDTO.getId());
        List<WaveListCartTypeEntity> cartTypeList = waveListCartTypeMapper.selectList(new QueryWrapper<WaveListCartTypeEntity>().eq("wave_id", bindDTO.getId()));
        List<String> typeIds = cartTypeList.stream().map(WaveListCartTypeEntity::getPickingCartTypeId).collect(Collectors.toList());
        if(! typeIds.contains(pickingCart.getTypeId())){
            return ApiResult.error("拣货车类型不匹配");
        }

        List<WaveListEntity> waveList = waveListService.list(new QueryWrapper<WaveListEntity>()
                .eq("picking_cart_code", pickingCart.getCode())
                .ne("id", bindDTO.getId())
                .ne("status", WaveStatusEnum.FINISH.getCode()));
        if(!waveList.isEmpty()){
            return ApiResult.error("拣货车正在使用，无法再次绑定");
        }

        update(new UpdateWrapper<WaveListEntity>()
                .set("picking_cart_code", bindDTO.getPickingCartCode())
                .set("picking_cart_type", pickingCart.getTypeId())
                .eq("id", bindDTO.getId()));
        //核对成功返回拣货车信息
        bindDTO.setPickingCartName(pickingCartType.getName());
        bindDTO.setPickingCartType(pickingCartType.getName());
        bindDTO.setPickingType(waveEntity.getPickingType());
        return ApiResult.success(bindDTO);
    }

    @Override
    public ApiResult<?> exitPicking(WaveListDetailPdaDTO.ExitPickingDTO exitDTO) {
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .eq("id", exitDTO.getId())
                .set("picking_cart_code", "")
                .set("status", WaveStatusEnum.AWAIT_PICK.getCode()));
        return ApiResult.success();
    }

    private void fillWaveInfo(WaveListPdaDTO.WaveBasicInfoDTO viewDTO) {
        //todo
        viewDTO.setPickingTypeName(WavePickingTypeEnum.getName(viewDTO.getPickingType()));
        viewDTO.setWarehouseId("");
        viewDTO.setWarehouseName("");
        viewDTO.setStatusName(WaveStatusEnum.getNameByCode(viewDTO.getStatus()));
    }

    private List<WaveListPdaDTO.ViewDTO> fillViewList(List<WaveListEntity> records) {
        if(records.isEmpty()){
            return Collections.emptyList();
        }

        List<String> ids = records.stream().map(item -> item.getId()).collect(Collectors.toList());
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new QueryWrapper<WaveListDetailEntity>().in("main_id", ids));
        Map<String, List<WaveListDetailEntity>> waveDetailMap = waveDetailList.stream().collect(Collectors.groupingBy(item -> item.getMainId()));
        List<WaveListPdaDTO.ViewDTO> viewList = new ArrayList<>();
        List<String> soIds = waveDetailList.stream().map(item -> item.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(soIds);
        Map<String, List<SoB2cDetailEntity>> soDetailMap = soDetailList.stream().collect(Collectors.groupingBy(item -> item.getMainId()));

        List<PickingCartEntity> pickingCartList = pickingCartService.list();
        List<PickingCartTypeEntity> pickingCartTypeList = pickingCartTypeService.list();

        for (WaveListEntity waveEntity : records) {
            WaveListPdaDTO.ViewDTO view = new WaveListPdaDTO.ViewDTO();
            //波次明细
            WaveListDetailDTO.ViewDTO waveDetailView = waveDetailService.view(waveEntity.getId());
            List<WaveListDetailDTO.DeliveryInfoDTO> deliveryInfoList = waveDetailView.getDeliveryInfoList();
            //波次id，波次编码，波次名称，创建时间
            BeanMapper.copy(waveEntity, view);
            //波次状态
            view.setStatusName(WaveStatusEnum.getNameByCode(view.getStatus()));
            //拣货车类型ID
            PickingCartEntity pickingCart = pickingCartList.stream().filter(item -> item.getCode().equals(waveEntity.getPickingCartCode())).findFirst().orElse(new PickingCartEntity());
            view.setPickingCartTypeId(pickingCart.getTypeId());
            //拣货车类型名称
            PickingCartTypeEntity pickingCartType = pickingCartTypeList.stream().filter(item -> item.getId().equals(pickingCart.getTypeId())).findFirst().orElse(new PickingCartTypeEntity());
            view.setPickingCartTypeName(pickingCartType.getName());
            //分拣方式
            view.setPickingTypeName(WavePickingTypeEnum.getName(waveEntity.getPickingType()));
            //订单数量
            List<WaveListDetailEntity> list = waveDetailMap.get(waveEntity.getId());
            List<String> deliveryIds = list.stream().map(WaveListDetailEntity::getDeliveryId).distinct().collect(Collectors.toList());
            view.setDeliveryBillQty(deliveryIds.size());
            //商品种类
            List<String> skuIds = deliveryInfoList.stream().map(item -> item.getSkuId()).distinct().collect(Collectors.toList());
            view.setSkuQty(skuIds.size());
            //商品数量
            int salesQty = deliveryInfoList.stream().mapToInt(item -> item.getSalesQty()).sum();
            view.setGoodsQty(salesQty);
            viewList.add(view);
        }

        return viewList;
    }
}
