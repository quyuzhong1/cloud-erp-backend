package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.PlatformOutboundDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.erp.server.wms.rocketmq.consumer.PlatformOutboundConsumerService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import feign.Feign;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SHOP;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_THIRD_WAREHOUSE_DELIVERY_REPORT;

/**
 * <p>
 * 三方仓发货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryMapper, ThirdWarehouseDeliveryEntity> implements ThirdWarehouseDeliveryService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private ThirdWarehouseDeliveryDetailService detailService;

    @Resource
    private ThirdWarehouseDeliveryService service;

    @Resource
    private PlatformOutboundConsumerService platformOutboundConsumerService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ThirdWarehouseDeliveryEntity add(ThirdWarehouseDeliveryEntity entity) {

        // 生成单号
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }
        entity.getDetailEntityList().forEach(v->{
            v.setMainId(entity.getId());
        });
        detailService.saveBatch(entity.getDetailEntityList());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方仓发货单" , entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.THIRD_WAREHOUSE_DELIVERY.getCode(),entity.getId(), "新增操作");

        return entity;
    }

    @Override
    public ThirdWarehouseDeliveryEntity getByCodeAndSoId(String outCode, String soId) {
        if(StringUtils.isBlank(outCode) || StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode,outCode).eq(ThirdWarehouseDeliveryEntity::getSoId,soId).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestBySoId(String soId) {
        if(StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getSoId, soId)
                .orderByDesc(ThirdWarehouseDeliveryEntity::getCreateTime).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestByCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode, code).one();
    }

    @Override
    public void generateDeliveryAndOutStock(GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO) {
        SoB2cEntity entity = generateDeliveryAndOutStockDTO.getEntity();
        List<SoB2cDetailEntity> soB2cDetailEntityList = generateDeliveryAndOutStockDTO.getDetailEntityList();
        SoB2cDTO.DeliveryWithNotOutboundDTO deliveryWithNotOutboundDTO = generateDeliveryAndOutStockDTO.getDto();
        OverseasProviderWarehouseDTO.ViewDTO viewDTO = generateDeliveryAndOutStockDTO.getOverseasWarehouseDto();
        ThirdWarehouseDeliveryEntity exist = this.getLatestBySoId(entity.getId());
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity;
        if(Objects.nonNull(exist) && (exist.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus())
                || exist.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus()))) {
            thirdWarehouseDeliveryEntity = exist;
        }else{
            ThirdWarehouseDeliveryEntity addThirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
            addThirdWarehouseDeliveryEntity.setSoCode(entity.getCode());
            addThirdWarehouseDeliveryEntity.setSoId(entity.getId());
            // 生成单号
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WFHD);
            addThirdWarehouseDeliveryEntity.setCode(code);
            addThirdWarehouseDeliveryEntity.setDictPlatform(entity.getDictPlatform());
            addThirdWarehouseDeliveryEntity.setPlatformCode(entity.getPlatformCode());
            addThirdWarehouseDeliveryEntity.setThirdWarehousePlatform(viewDTO.getProviderCode());
            addThirdWarehouseDeliveryEntity.setShippingMethod(deliveryWithNotOutboundDTO.getLogisticsChannelCode());
            addThirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus());
            List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDetailList = new ArrayList<>();
            for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
                ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
                thirdWarehouseDeliveryDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
                thirdWarehouseDeliveryDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
                thirdWarehouseDeliveryDetailEntity.setDeliveryQty(soB2cDetailEntity.getQty());
                thirdWarehouseDeliveryDetailEntity.setWarehouseId(deliveryWithNotOutboundDTO.getWarehouseId());
                thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo("");
                thirdWarehouseDeliveryDetailEntity.setPlatformWarehouseCode(viewDTO.getPlatformWarehouseCode());
                thirdWarehouseDeliveryDetailEntity.setSourceSkuId(soB2cDetailEntity.getSkuId());
                thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(soB2cDetailEntity.getSkuNo());
                thirdWarehouseDetailList.add(thirdWarehouseDeliveryDetailEntity);
            }
            addThirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDetailList);
            thirdWarehouseDeliveryEntity = service.add(addThirdWarehouseDeliveryEntity);
        }

        PlatformOutboundDTO platformOutboundDTO = new PlatformOutboundDTO();
        platformOutboundDTO.setOutBoundTime(deliveryWithNotOutboundDTO.getDeliveryTime());
        platformOutboundDTO.setTrackNo(deliveryWithNotOutboundDTO.getTrackNo());
        platformOutboundConsumerService.generateSoOut(entity,thirdWarehouseDeliveryEntity,platformOutboundDTO);
        entity = soB2cFeign.getById(entity.getId());
        if(entity.getSignOrderError().equals(SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode())){
            if(!thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus())){
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.WAIT_HANDLE.getStatus());
                this.updateById(thirdWarehouseDeliveryEntity);
            }
            throw new ServiceException("生成销售出库单失败,请查看订单异常");
        }else{
            if(!thirdWarehouseDeliveryEntity.getStatus().equals(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus())){
                thirdWarehouseDeliveryEntity.setStatus(SoB2cWarehouseDeliveryStatusEnum.SHIPPED.getStatus());
                this.updateById(thirdWarehouseDeliveryEntity);
            }
        }
    }

    @Override
    public PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> paging(PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ThirdWarehouseDeliveryDTO.PagingViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        List<ThirdWarehouseDeliveryDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> shopIds = list.stream().map(ThirdWarehouseDeliveryDTO.PagingViewDTO::getShopId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        List<String> warehouseIds = list.stream().map(ThirdWarehouseDeliveryDTO.PagingViewDTO::getWarehouseId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(warehouseIds);
        List<String> errorSoIds = list.stream()
                .filter(v -> Objects.equals(v.getSignOrderError(), SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode()))
                .map(ThirdWarehouseDeliveryDTO.PagingViewDTO::getSoId)
                .distinct()
                .collect(Collectors.toList());
        List<SoB2cErrorEntity> soB2cErrorEntityList = soB2cFeign.listSoB2cErrorByMainIds(errorSoIds);

        for (ThirdWarehouseDeliveryDTO.PagingViewDTO pagingViewDTO : list) {
            pagingViewDTO.setPlatformName(PlatformDictEnum.getNameByCode(pagingViewDTO.getPlatform()));
            shopInfoEntityList.stream()
                    .filter(shop -> shop.getId().equals(pagingViewDTO.getShopId()))
                    .findFirst().ifPresent(shopInfoEntity -> pagingViewDTO.setShopName(shopInfoEntity.getName()));
            pagingViewDTO.setStatusName(SoB2cWarehouseDeliveryStatusEnum.getName(pagingViewDTO.getStatus()));
            warehouseEntityList.stream()
                    .filter(warehouse -> Objects.equals(warehouse.getId(), pagingViewDTO.getWarehouseId()))
                    .findFirst().ifPresent(warehouseEntity -> pagingViewDTO.setWarehouseName(warehouseEntity.getName()));
            if(pagingViewDTO.getSignOrderError().equals(SoB2cErrorTypeEnum.THIRD_WAREHOUSE_OUT_EXCEPTION.getCode())){
                SoB2cErrorEntity soB2cErrorEntity = soB2cErrorEntityList.stream()
                        .filter(error -> error.getMainId().equals(pagingViewDTO.getSoId()))
                        .findFirst().orElse(new SoB2cErrorEntity());
                pagingViewDTO.setAbnormalProblemReason(soB2cErrorEntity.getMessage());
            }
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public ThirdWarehouseDeliveryDTO.ViewDTO view(String id) {
        ThirdWarehouseDeliveryEntity entity = getById(id);
        List<ThirdWarehouseDeliveryDetailEntity> detailEntityList = detailService.listByMainId(id);
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(soB2cEntity.getShopId());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(entity.getSoId()));
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(entity.getSoId())).get(0);
        WarehouseEntity warehouseEntity = warehouseService.getById(detailEntityList.get(0).getWarehouseId());
        ThirdWarehouseDeliveryDTO.ViewDTO viewDTO = ThirdWarehouseDeliveryDTO.ViewDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .transportNo(soB2cLogisticsEntity.getCode())
                .shopId(soB2cEntity.getShopId())
                .shopName(Objects.nonNull(shopInfo)?shopInfo.getName():"")
                .soCode(soB2cEntity.getCode())
                .platformOrderCode(soB2cEntity.getPlatformCode())
                .thirdCode(soB2cEntity.getShippingOrderNo())
                .status(entity.getStatus())
                .statusName(SoB2cWarehouseDeliveryStatusEnum.getName(entity.getStatus()))
                .channelId(soB2cLogisticsEntity.getLogisticsChannelId())
                .channelName(soB2cLogisticsEntity.getLogisticsChannelName())
                .warehouseId(warehouseEntity.getId())
                .warehouseName(warehouseEntity.getName())
                .build();
        List<ThirdWarehouseDeliveryDTO.ViewDetailDTO> viewDetailDTOList = new ArrayList<>();
        List<String> skuIds = detailEntityList.stream().map(v->v.getSkuId()).collect(Collectors.toList());
        List<String> platformSkuNoList = detailEntityList.stream().map(v->v.getPlatformSkuNo()).collect(Collectors.toList());
        List<ListingInfoEntity> listingInfoEntityList = FeignQuery.create(ListingInfoEntity.class).in(ListingInfoEntity::getPlatformSkuNo,platformSkuNoList).eq(ListingInfoEntity::getPlatform,entity.getThirdWarehousePlatform()).list();
        List<SkuVO> skuVOS = plmTaskFeign.listSkuPackByIds(skuIds);
        for (ThirdWarehouseDeliveryDetailEntity detailEntity : detailEntityList) {
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(v->v.getSkuId().equals(detailEntity.getSourceSkuId())).findFirst().orElse(new SoB2cDetailEntity());
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(v->v.getPlatformSkuNo().equals(detailEntity.getPlatformSkuNo())).findFirst().orElse(new ListingInfoEntity());
            SkuVO skuVO = skuVOS.stream().filter(v->v.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            ThirdWarehouseDeliveryDTO.ViewDetailDTO viewDetailDTO = ThirdWarehouseDeliveryDTO.ViewDetailDTO.builder()
                    .id(detailEntity.getId())
                    .skuNo(detailEntity.getSkuNo())
                    .productName(skuVO.getSkuName())
                    .platformSkuNo(detailEntity.getPlatformSkuNo())
                    .platformSkuName(listingInfoEntity.getPlatformSkuName())
                    .qty(detailEntity.getDeliveryQty())
                    .warehouseLocation(soB2cDetailEntity.getWarehouseLocation())
                    .build();
            viewDetailDTOList.add(viewDetailDTO);
        }
        viewDTO.setViewDetailDTOList(viewDetailDTOList);
        return viewDTO;
    }

    @Override
    public void export(ThirdWarehouseDeliveryDTO.PagingParamDTO dto) {

        downloadTaskFeign.saveDownloadTask("三方仓发货单导出", EXPORT_WMS_THIRD_WAREHOUSE_DELIVERY_REPORT.getCode(),dto);
    }

    @Override
    public List<ThirdWarehouseDeliveryDTO.TabListDTO> tabList() {
        List<ThirdWarehouseDeliveryDTO.TabListDTO> tabListDTOS = this.baseMapper.listCount();
        SoB2cWarehouseDeliveryStatusEnum[] tabEnums = SoB2cWarehouseDeliveryStatusEnum.values();
        List<ThirdWarehouseDeliveryDTO.TabListDTO> result = new ArrayList<>();
        for (SoB2cWarehouseDeliveryStatusEnum tabEnum : tabEnums) {
            ThirdWarehouseDeliveryDTO.TabListDTO tabListDTO = new ThirdWarehouseDeliveryDTO.TabListDTO();
            tabListDTO.setTabFlag(tabEnum.getCode());
            tabListDTO.setTabFlagName(tabEnum.getName());
            tabListDTO.setCount(tabListDTOS.stream().
                    filter(v -> v.getTabFlag().equals(tabEnum.getCode()))
                    .map(ThirdWarehouseDeliveryDTO.TabListDTO::getCount)
                    .findFirst()
                    .orElse(0));
            result.add(tabListDTO);
        }
        return result;
    }

    @Override
    public BatchResultDTO retryOutstock(String id) {
        ThirdWarehouseDeliveryEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            return BatchResultDTO.fail(id, id, "发货单不存在, 重新出库失败");
        }
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            return BatchResultDTO.fail(id, entity.getCode(), "销售订单不存在, 重新出库失败");
        }
        if (soB2cEntity.hasPlatformWarehouseOrder()) {
            return BatchResultDTO.fail(id, entity.getCode(), "平台仓订单无法 重新出库");
        }
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soB2cEntity.getId())).get(0);
        PlatformOutboundDTO platformOutboundDTO = new PlatformOutboundDTO();
        platformOutboundDTO.setOutBoundTime(soB2cLogisticsEntity.getDeliveryTime());
        platformOutboundDTO.setTrackNo(soB2cLogisticsEntity.getCode());
        platformOutboundConsumerService.generateSoOut(soB2cEntity,entity,platformOutboundDTO);
        return BatchResultDTO.success();
    }

    @Override
    public ThirdWarehouseDeliveryEntity generatePlatformDelivery(String soId) {
        SoB2cEntity entity = soB2cFeign.getById(soId);
        //已发货
        String shipped = SoB2cBillStatusEnum.ENUM_SHIPPED.getCode();
        String billStatus = entity.getBillStatus();

        //已存在也不生成
        ThirdWarehouseDeliveryEntity exist = this.getLatestBySoId(entity.getId());
        if (ObjectUtil.isNotEmpty(exist)) {
            log.warn("销售订单{}已存在三方仓发货单,不再重复生成", entity.getCode());
            return exist;
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
            throw new ServiceException("销售订单明细不存在");
        }
        ThirdWarehouseDeliveryEntity addThirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        addThirdWarehouseDeliveryEntity.setSoCode(entity.getCode());
        addThirdWarehouseDeliveryEntity.setSoId(entity.getId());
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WFHD);
        addThirdWarehouseDeliveryEntity.setCode(code);
        addThirdWarehouseDeliveryEntity.setDictPlatform(entity.getDictPlatform());
        addThirdWarehouseDeliveryEntity.setPlatformCode(entity.getPlatformCode());
        List<ThirdWarehouseDeliveryDetailEntity> thirdWarehouseDetailList = new ArrayList<>();
        for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
            ThirdWarehouseDeliveryDetailEntity thirdWarehouseDeliveryDetailEntity = new ThirdWarehouseDeliveryDetailEntity();
            thirdWarehouseDeliveryDetailEntity.setSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDeliveryDetailEntity.setDeliveryQty(soB2cDetailEntity.getQty());
            thirdWarehouseDeliveryDetailEntity.setWarehouseId(soB2cDetailEntity.getWarehouseId());
            thirdWarehouseDeliveryDetailEntity.setPlatformSkuNo("");
            thirdWarehouseDeliveryDetailEntity.setSourceSkuId(soB2cDetailEntity.getSkuId());
            thirdWarehouseDeliveryDetailEntity.setSourceSkuNo(soB2cDetailEntity.getSkuNo());
            thirdWarehouseDetailList.add(thirdWarehouseDeliveryDetailEntity);
        }
        addThirdWarehouseDeliveryEntity.setDetailEntityList(thirdWarehouseDetailList);
        return service.add(addThirdWarehouseDeliveryEntity);

    }

}
