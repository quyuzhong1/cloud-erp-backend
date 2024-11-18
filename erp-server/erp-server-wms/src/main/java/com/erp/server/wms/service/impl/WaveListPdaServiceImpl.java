package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    @Resource
    private PickingDetailService pickingDetailService;

    @Override
    public PagingVO<WaveListPdaDTO.ViewDTO> paging(PagingDTO<WaveListDTO.SearchParamDTO> pagingDTO) {
        Page<Object> page = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<WaveListEntity> result = this.baseMapper.paging(page, pagingDTO.getParams());
        List<WaveListPdaDTO.ViewDTO> viewDTOList = fillViewList(result.getRecords());
        return new PagingVO<>(viewDTOList, (int)result.getTotal(), (int)result.getSize(), (int)result.getCurrent());
    }

    @Override
    public WaveListPdaDTO.WaveBasicInfoDTO waveInfo(String waveId) {
        WaveListDetailDTO.ViewDTO view = waveDetailService.view(waveId);
        WaveListPdaDTO.WaveBasicInfoDTO basicInfoDTO = new WaveListPdaDTO.WaveBasicInfoDTO();
        basicInfoDTO.setId(view.getId());
        basicInfoDTO.setCode(view.getCode());
        basicInfoDTO.setName(view.getName());
        basicInfoDTO.setWarehouseId(view.getWarehouseId());
        basicInfoDTO.setWarehouseName(view.getWarehouseName());
        basicInfoDTO.setPickingCartCode(view.getPickingCartCode());
        basicInfoDTO.setPickingCartType(view.getPickingCartTypeName());
        basicInfoDTO.setPickingType(view.getPickingType());
        basicInfoDTO.setPickingTypeName(WavePickingTypeEnum.getName(view.getPickingType()));
        basicInfoDTO.setStatus(view.getStatus());
        basicInfoDTO.setStatusName(WaveStatusEnum.getNameByCode(view.getStatus()));
        return basicInfoDTO;
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
            if(! salesQtyMap.containsKey(skuId)){
                salesQtyMap.put(skuId, 0);
            }
            salesQtyMap.put(skuId, salesQtyMap.get(skuId) + deliveryDto.getSalesQty());

            if(! pickedTotalQtyMap.containsKey(skuId)){
                pickedTotalQtyMap.put(skuId, 0);
            }
            pickedTotalQtyMap.put(skuId, pickedTotalQtyMap.get(skuId) + deliveryDto.getPickedSumQty());
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
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        PickingCartEntity pickingCart = pickingCartService.getOne(new QueryWrapper<PickingCartEntity>().eq("code", bindDTO.getPickingCartCode()));
        if(pickingCart == null){
            return ApiResult.error("拣货车编码错误");
        }
        if(pickingCart.getDisabled()){
            return ApiResult.error("拣货车已禁用");
        }

        List<WaveListCartTypeEntity> cartTypeList = waveListCartTypeMapper.selectList(new QueryWrapper<WaveListCartTypeEntity>().eq("wave_id", bindDTO.getId()));
        List<String> typeIds = cartTypeList.stream().map(WaveListCartTypeEntity::getPickingCartTypeId).collect(Collectors.toList());
        if(! typeIds.contains(pickingCart.getTypeId())){
            return ApiResult.error("拣货车类型不匹配");
        }

        WaveListEntity currentWave = waveListService.getById(bindDTO.getId());
        if(StringUtils.equals(WaveStatusEnum.HANG_UP.getCode(), currentWave.getStatus())){
            if(StringUtils.equals(bindDTO.getPickingCartCode(), currentWave.getPickingCartCode())){
                doBindCart(bindDTO, pickingCart, loginUser);
                return ApiResult.success(bindDTO);
            }else {
                return ApiResult.error("挂起的波次只能匹配已绑定的拣货车");
            }
        }

        List<WaveListEntity> entityList = waveListService.list(new QueryWrapper<WaveListEntity>()
                .eq("picking_cart_code", pickingCart.getCode())
                .ne("status", WaveStatusEnum.FINISH.getCode()));
        if(!entityList.isEmpty()){
            return ApiResult.error("拣货车正在使用，无法再次绑定");
        }

        doBindCart(bindDTO, pickingCart, loginUser);
        return ApiResult.success(bindDTO);
    }

    private void doBindCart(WaveListPdaDTO.BindPickingCartDTO bindDTO, PickingCartEntity pickingCart, LoginUser loginUser) {
        update(new UpdateWrapper<WaveListEntity>()
//                .set("status", WaveStatusEnum.PICK_ING.getCode())
                .set("picking_cart_code", bindDTO.getPickingCartCode())
                .set("picking_cart_type", pickingCart.getTypeId())
//                .set("picking_user_id", loginUser.getUid())
//                .set("picking_user_name", loginUser.getUserName())
//                .set("picking_time", LocalDateTime.now())
                .eq("id", bindDTO.getId()));
        //核对成功返回拣货车信息
        PickingCartTypeEntity pickingCartType = pickingCartTypeService.getOne(new QueryWrapper<PickingCartTypeEntity>().eq("id", pickingCart.getTypeId()));
        WaveListEntity waveEntity = waveListService.getById(bindDTO.getId());
        bindDTO.setPickingCartName(pickingCartType.getName());
        bindDTO.setPickingCartType(pickingCartType.getName());
        bindDTO.setPickingType(waveEntity.getPickingType());
    }

    @Override
    public ApiResult<?> exitPicking(WaveListDetailPdaDTO.ExitPickingDTO exitDTO) {
        waveListService.update(new UpdateWrapper<WaveListEntity>()
                .eq("id", exitDTO.getId())
                .set("picking_cart_code", "")
                .set("picking_cart_type", "")
                .set("is_out_stock", false)
                .set("status", WaveStatusEnum.AWAIT_PICK.getCode()));
        List<WaveListDetailEntity> waveDetailList = waveDetailService.list(new LambdaQueryWrapper<WaveListDetailEntity>().eq(WaveListDetailEntity::getMainId, exitDTO.getId()));
        if(! waveDetailList.isEmpty()){
            List<String> deliveryIds = waveDetailList.stream().map(WaveListDetailEntity::getDeliveryId).collect(Collectors.toList());
            List<PickingDetailEntity> pickingDetailList = waveDetailService.getPickingDetail(deliveryIds);
            List<String> pickingDetailIds = pickingDetailList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            pickingDetailService.update(new LambdaUpdateWrapper<PickingDetailEntity>().set(PickingDetailEntity::getIsOutStock, false).in(PickingDetailEntity::getId, pickingDetailIds));
        }
        return ApiResult.success();
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
        List<WaveListCartTypeEntity> waveCartTypeList = waveListCartTypeMapper.selectList(new QueryWrapper<WaveListCartTypeEntity>().in("wave_id", ids));
        Map<String, List<WaveListCartTypeEntity>> cartTypeMap = waveCartTypeList.stream().collect(Collectors.groupingBy(item -> item.getWaveId()));

        List<PickingCartEntity> pickingCartList = pickingCartService.list();
        List<PickingCartTypeEntity> pickingCartTypeList = pickingCartTypeService.list();
        Map<String, String> typeMap = pickingCartTypeList.stream().collect(Collectors.toMap(item -> item.getId(), item2 -> item2.getName()));

        for (WaveListEntity waveEntity : records) {
            WaveListPdaDTO.ViewDTO view = new WaveListPdaDTO.ViewDTO();
            //波次明细
            WaveListDetailDTO.ViewDTO waveDetailView = null;
            try {
                waveDetailView = waveDetailService.view(waveEntity.getId());
            }catch (ServiceException e){
                continue;
            }
            List<WaveListDetailDTO.DeliveryInfoDTO> deliveryInfoList = waveDetailView.getDeliveryInfoList();
            //波次id，波次编码，波次名称，创建时间
            BeanMapper.copy(waveEntity, view);
            //波次状态
            view.setStatusName(WaveStatusEnum.getNameByCode(view.getStatus()));
            //拣货车编号
            view.setPickingCartCode(waveEntity.getPickingCartCode());
            //拣货车类型ID
            PickingCartEntity pickingCart = pickingCartList.stream().filter(item -> item.getCode().equals(waveEntity.getPickingCartCode())).findFirst().orElse(new PickingCartEntity());
            view.setPickingCartTypeId(pickingCart.getTypeId());
            //拣货车类型名称
//            PickingCartTypeEntity pickingCartType = pickingCartTypeList.stream().filter(item -> item.getId().equals(pickingCart.getTypeId())).findFirst().orElse(new PickingCartTypeEntity());
//            view.setPickingCartTypeName(pickingCartType.getName());

            if(CharSequenceUtil.isBlank(waveEntity.getPickingCartCode())){
                List<WaveListCartTypeEntity> entityList = cartTypeMap.get(waveEntity.getId());
                if(entityList != null && !entityList.isEmpty()){
                    List<String> typeIds = entityList.stream().map(WaveListCartTypeEntity::getPickingCartTypeId).collect(Collectors.toList());
                    List<String> typeNameList = new ArrayList<>();
                    typeIds.forEach(id -> typeNameList.add(typeMap.get(id)));
                    String cartTypeName = String.join(",", typeNameList);
                    view.setPickingCartTypeName(cartTypeName);
                }
            }else {
                view.setPickingCartTypeName(typeMap.get(waveEntity.getPickingCartType()));
            }

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

    @Override
    public List<WaveListDTO.TabDTO> tabList() {
        List<WaveListDTO.TabDTO> list = waveListService.tabList();
        Map<String, WaveListDTO.TabDTO> map = list.stream().collect(Collectors.toMap(item1 -> item1.getTabFlag(), item2 -> item2));

        List<WaveListDTO.TabDTO> resultList = new ArrayList<>();
        WaveListDTO.TabDTO tab_wait = map.get(WaveStatusEnum.AWAIT_PICK.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.AWAIT_PICK.getCode(), tab_wait != null ? tab_wait.getCount() : 0));

        WaveListDTO.TabDTO tab_ing = map.get(WaveStatusEnum.PICK_ING.getCode());
        WaveListDTO.TabDTO tab_hang = map.get(WaveStatusEnum.HANG_UP.getCode());
        int ing = tab_ing != null ? tab_ing.getCount() : 0;
        int hang = tab_hang != null ? tab_hang.getCount() : 0;
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.PICK_ING.getCode(), ing + hang));

        WaveListDTO.TabDTO tab_finish = map.get(WaveStatusEnum.FINISH.getCode());
        resultList.add(new WaveListDTO.TabDTO(WaveStatusEnum.FINISH.getCode(), tab_finish != null ? tab_finish.getCount() : 0));

        for (WaveListDTO.TabDTO dto : resultList) {
            dto.setTabFlagName(WaveStatusEnum.getNameByCode(dto.getTabFlag()));
        }
        return resultList;
    }
}
