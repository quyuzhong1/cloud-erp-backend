package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FbaDeliveryStatusEnum;
import com.erp.model.wms.enums.FbaDemandTypeEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.ShipmentStatus;
import com.erp.server.wms.convert.FbaShipmentConsumerConverter;
import com.erp.server.wms.convert.FbaShipmentConverter;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * FBA货件表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaShipmentServiceImpl extends SuperServiceImpl<FbaShipmentMapper, FbaShipmentEntity> implements FbaShipmentService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Autowired
    private FbaShipmentStatusService fbaShipmentStatusService;
    @Autowired
    private FbaDeliveryService fbaDeliveryService;
    @Autowired
    private FbaDeliveryDetailService fbaDeliveryDetailService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private OmsListingInfoFeign omsListingInfoFeign;
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;
    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Override
    public PagingVO<FbaShipmentDTO.ListDTO> paging(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<FbaShipmentDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean skuMapping(FbaShipmentDTO.skuMappingParamDTO dto) {
        FbaShipmentDetailEntity detailEntity = fbaShipmentDetailService.getById(dto.getDetailId());
        if (ObjectUtil.isEmpty(detailEntity)) {
            throw new ServiceException(ApiError.FBA_SHIPMENT_DETAIL_NOT_EXIST);
        }
        FbaShipmentEntity entity = this.getById(detailEntity.getMainId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.FBA_SHIPMENT_NOT_EXIST);
        }
        //根据平台sku查询映射信息
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(Arrays.asList(detailEntity.getMsku()));
        listingInfoParamDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        List<SkuMappingDTO.SkuDTO> skuDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        List<SkuMappingDTO.SkuDTO> collect = skuDTOS.stream().filter(req -> req.getPlatformSkuNo().equals(detailEntity.getMsku())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.EXIST_SKU_MAPPING);
        }


        //映射sku
        dto.setShopId(entity.getShopId());
        dto.setPlatform(PlatformDictEnum.AMAZON.getCode());
        Boolean flag = omsListingInfoFeign.skuMapping(dto);
        if (flag) {

            detailEntity.setSkuNo(dto.getSkuNo());

            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Arrays.asList(dto.getSkuNo()));

            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuId(skuVO.getSkuId());
            //根据sku查询拥有的子sku
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(Arrays.asList(skuVO.getSkuId()));

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(skuVO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailEntity.setIsCombination(Boolean.TRUE);
            } else {
                detailEntity.setIsCombination(Boolean.FALSE);
            }
            //添加日志
            List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByIds(Arrays.asList(dto.getDetailId()));

            //操作日志
            List<Pair<String, String>> pairList = fbaShipmentDetailEntities.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getMsku() + " 映射 " + dto.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("映射了一个sku【%s】", ModuleTypeEnum.FBA_SHIPMENT.getCode(), pairList, "编辑信息");
            fbaShipmentDetailService.updateById(detailEntity);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto) {
        // 检查当前店铺是否授权
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dto.getShopId());
        if (null == shopInfoEntity) {
            throw new ServiceException(ApiError.ERROR_92058);
        }
        if (!AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(shopInfoEntity.getAuthStatus())) {
            throw new ServiceException(ApiError.SHOP_AUTH_SHIPMENT_ERROR);
        }

        DmpPullShipmentDTO pullShipmentDTO = new DmpPullShipmentDTO(dto.getShopId(), dto.getShipmentCodeList());
        dmpAmazonFeign.pullShipment(pullShipmentDTO);
        return true;
    }

    @Override
    public List<FbaDeliveryDTO.DeliverRecordView> listDeliverRecord(String id) {
        List<FbaDeliveryDTO.DeliverRecordView> deliverRecordViews = fbaDeliveryService.listDeliveryRecordBySourceIds(Arrays.asList(id));
        return deliverRecordViews;
    }

    @Override
    public List<FbaShipmentDTO.ShipmentStatusRecordView> listShipmentStatusRecord(String id) {
        List<FbaShipmentStatusEntity> fbaShipmentStatusEntities = fbaShipmentStatusService.listByMainIds(Arrays.asList(id));
        List<FbaShipmentDTO.ShipmentStatusRecordView> list = new ArrayList<>();
        for (FbaShipmentStatusEntity fbaShipmentReceiveEntity : fbaShipmentStatusEntities) {
            //映射字段
            FbaShipmentDTO.ShipmentStatusRecordView shipmentStatusRecordView = FbaShipmentConverter.INSTANCE.fbaShipmentStatusEntityToView(fbaShipmentReceiveEntity);
            list.add(shipmentStatusRecordView);
        }
        return list;
    }

    @Override
    public List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(String id) {
        List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(Arrays.asList(id));
        List<FbaShipmentDTO.ReceiveRecordView> list = new ArrayList<>();
        for (FbaShipmentReceiveEntity fbaShipmentReceiveEntity : fbaShipmentReceiveEntities) {
            //映射字段
            FbaShipmentDTO.ReceiveRecordView receiveRecordView = FbaShipmentConverter.INSTANCE.fbaShipmentReceiveEntityToView(fbaShipmentReceiveEntity);
            list.add(receiveRecordView);
        }
        return list;
    }

    @Override
    public FbaShipmentDTO.ViewDTO view(String id) {
        FbaShipmentEntity entity = this.getById(id);

        //映射字段
        FbaShipmentDTO.ViewDTO viewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentToViewDTO(entity);

        //根据主表id查询详情信息
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(id));
        List<String> skuNos = fbaShipmentDetailEntities.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
        //根据sku获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

        //设置详情信息
        List<FbaShipmentDetailDTO.ViewDTO> detailViewList = new ArrayList<>();
        for (FbaShipmentDetailEntity fbaShipmentDetailEntity : fbaShipmentDetailEntities) {

            //映射详情字段
            FbaShipmentDetailDTO.ViewDTO detailViewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentDetailToViewDTO(fbaShipmentDetailEntity);

            //产品名称
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(fbaShipmentDetailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            detailViewDTO.setProductName(skuVO.getSkuName());

            //组装详情信息
            detailViewList.add(detailViewDTO);
        }

        //给产品信息赋值
        viewDTO.setDetailList(detailViewList);
        return viewDTO;
    }

    @Override
    public Boolean finishShipment(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<FbaShipmentEntity> fbaShipmentEntities = super.listByIds(ids);
        List<FbaShipmentEntity> list = fbaShipmentEntities.stream().filter(req -> !FbaDeliveryStatusEnum.SHIPPED.getCode().equals(req.getDeliveryStatus())).collect(Collectors.toList());
        // 只有已发货的单据才能完结
        if (list.size() > 0) {
            throw new ServiceException(ApiError.IS_DELIVERY_FINISH);
        }

        Boolean flag = lambdaUpdate()
                .set(FbaShipmentEntity::getDeliveryStatus, FbaDeliveryStatusEnum.IS_OVER.getCode())
                .in(FbaShipmentEntity::getId, ids).update();


        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("完结了一个货件单【%s】", ModuleTypeEnum.FBA_SHIPMENT.getCode(), pairList, "完结操作");

        return flag;
    }

    @Override
    public List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(BaseIdsDTO.IdsDTO ids) {

        //根据id获取货件信息
        List<FbaShipmentDTO.GenerateDeliverView> list = baseMapper.generateDeliverView(ids);

        //平台SKU没有映射关系，货件没有匹配到SKU的货件不允许下推发货单
        list.forEach(req -> {
            if (StringUtils.isBlank(req.getSkuNo())) {
                throw new ServiceException(ApiError.NOT_MAPPER_SKU, req.getMsku());
            }
        });

        //货件没有下推【要货申请】的单据不允许下推发货单（做配置开关，上线前先关闭） TODO

        //校验货件单据是否存在
        List<String> shipmentIds = list.stream().map(FbaShipmentDTO.GenerateDeliverView::getMainId).distinct().collect(Collectors.toList());
        List<FbaShipmentEntity> fbaShipmentEntities = this.listByIds(shipmentIds);
        if (CollectionUtils.isEmpty(fbaShipmentEntities)) {
            throw new ServiceException(ApiError.SHIPMENT_NOT_EXIST);
        }

        //Delete和Cancel状态的货件不允许下推发货单
        List<FbaShipmentEntity> collect = fbaShipmentEntities.stream()
                .filter(req -> ShipmentStatus.DELETED.getValue().equals(req.getPlatformShipmentStatus())
                        || ShipmentStatus.CANCELLED.getValue().equals(req.getPlatformShipmentStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.SHIPMENT_STATUS_CHECK_NOT_DELETE);
        }

        //根据sku获取产品信息
        List<String> skuNos = list.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);

        //获取所有店铺id
        List<String> shopIds = list.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());

        //根据店铺id查询店铺信息
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(shopIds);

        List<String> skuIdList = skuVOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);
        for (FbaShipmentDTO.GenerateDeliverView view : list) {
            //处理字段映射
            generateDeliverViewFieldHandle(view, shopInfoEntities, skuVOList, bomChildrenSkuDTOS);
        }
        return list;
    }


    @Override
    public FbaDeliveryDTO.ViewDTO getDeliverView(String id) {
        //校验货件单据是否存在
        List<FbaShipmentEntity> fbaShipmentEntities = this.listByIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(fbaShipmentEntities)) {
            throw new ServiceException(ApiError.SHIPMENT_NOT_EXIST);
        }

        List<FbaShipmentDetailEntity> list = fbaShipmentDetailService.listByMainIds(Arrays.asList(id));
        //平台SKU没有映射关系，货件没有匹配到SKU的货件不允许下推发货单
        list.forEach(req -> {
            if (StringUtils.isBlank(req.getSkuNo())) {
                throw new ServiceException(ApiError.NOT_MAPPER_SKU, req.getMsku());
            }
        });

        //货件没有下推【要货申请】的单据不允许下推发货单（做配置开关，上线前先关闭） TODO

        //Delete和Cancel状态的货件不允许下推发货单
        List<FbaShipmentEntity> collect = fbaShipmentEntities.stream()
                .filter(req -> ShipmentStatus.DELETED.getValue().equals(req.getPlatformShipmentStatus())
                        || ShipmentStatus.CANCELLED.getValue().equals(req.getPlatformShipmentStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.SHIPMENT_STATUS_CHECK_NOT_DELETE);
        }

        FbaShipmentEntity entity = this.getById(id);
        //根据店铺id查询店铺信息
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(Arrays.asList(entity.getShopId()));
        //设置店铺的仓位为目的仓
        ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> entity.getShopId().equals(req.getId())).findFirst().orElse(new ShopInfoEntity());

        //映射主信息字段
        FbaDeliveryDTO.ViewDTO viewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentEntityToFbaDeliveryViewDTO(entity);
        viewDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
        viewDTO.setSourceTypeName(SourceTypeEnum.FBA_SHIPMENT.getName());
        viewDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());
        viewDTO.setDemandTypeName(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getName());
        viewDTO.setDestWarehouseId(shopInfoEntity.getWarehouseId());
        viewDTO.setDestWarehouseName(shopInfoEntity.getWarehouseName());
        viewDTO.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        viewDTO.setApproveStatusName(ApproveStatusEnum.WAIT_SUBMIT.getName());

        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(id));
        List<String> mskuList = fbaShipmentDetailEntities.stream().filter(req -> StringUtils.isBlank(req.getSkuNo())).map(req -> req.getMsku()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(mskuList)) {
            throw new ServiceException(ApiError.NOT_MAPPER_SKU, StrUtil.join(",", mskuList));
        }

        //查询产品信息
        List<String> skuNoList = fbaShipmentDetailEntities.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());

        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        //获取库存sku信息
        List<SkuMappingDTO.listStockSkuNoByProductSkuNoView> listStockSkuNoByProductSkuNoViews = omsListingInfoFeign.listStockSkuNoByProductSkuNo(skuNoList);

        //根据sku查询拥有的子sku
        List<String> skuIdList = skuVOList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());

        //查询已发货的货件信息
        List<String> sourceDetailIdList = fbaShipmentDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<FbaDeliveryDetailEntity> entities = fbaDeliveryDetailService.listBySourceDetailIds(sourceDetailIdList);
        List<FbaDeliveryDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (FbaShipmentDetailEntity detailEntity : fbaShipmentDetailEntities) {
            //映射详情字段
            FbaDeliveryDetailDTO.ViewDTO detailDto = FbaShipmentConverter.INSTANCE.fbaShipmentDetailEntityToDeliveryDetailViewDTO(detailEntity);

            //设置产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(detailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            detailDto.setProductName(skuVO.getSkuName());
            detailDto.setNetWeight(skuVO.getNetWeight());
            //已发货数量
            Integer useDeliveryQty = entities.stream().filter(req -> req.getSourceDetailId().equals(detailEntity.getId())).mapToInt(req -> req.getDeliveryQty()).sum();
            detailDto.setUseDeliveryQty(useDeliveryQty);
            //拆分产品尺寸
            splitProductSizeView(detailDto, skuVO.getProductSize());
            //库存sku
            String stockSku = listStockSkuNoByProductSkuNoViews.stream().filter(req -> req.getProductSkuNo().equals(detailEntity.getSkuNo())).distinct().findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getWarehouseSkuNo())).orElse("");
            detailDto.setStockSku(stockSku);

            //来源详情id
            detailDto.setSourceDetailId(detailEntity.getId());
            detailList.add(detailDto);
        }
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateDeliverSave(List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = this.generateDeliver(list, Boolean.FALSE);
        return flag;
    }

    @Override
    public Boolean generateDeliverSaveAndSubmit(List<FbaShipmentDTO.GenerateDeliverView> list) {
        Boolean flag = this.generateDeliver(list, Boolean.TRUE);
        return flag;
    }

    /**
     * 下推发货单
     *
     * @param list     下推列表数据
     * @param isSubmit 是否需要提交
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/11/6 14:41
     **/
    private Boolean generateDeliver(List<FbaShipmentDTO.GenerateDeliverView> list, Boolean isSubmit) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }

        //根据仓库id查询仓库信息
        List<String> warehouseIds = list.stream().map(req -> req.getDeliveryWarehouseId()).distinct().collect(Collectors.toList());
        List<String> destWarehouseIds = list.stream().map(req -> req.getDestWarehouseId()).distinct().collect(Collectors.toList());
        warehouseIds.addAll(destWarehouseIds);
        List<WarehouseEntity> warehouseEntities = warehouseService.listByIds(warehouseIds);

        //获取sku信息
        List<String> skuNoList = list.stream().map(FbaShipmentDTO.GenerateDeliverView::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);

        //根据货件单分组一个货件单生成一个发货单
        Map<String, List<FbaShipmentDTO.GenerateDeliverView>> map = list.stream().collect(Collectors.groupingBy(FbaShipmentDTO.GenerateDeliverView::getMainId));

        for (Map.Entry<String, List<FbaShipmentDTO.GenerateDeliverView>> entry : map.entrySet()) {
            List<FbaShipmentDTO.GenerateDeliverView> shipmentList = entry.getValue();
            //映射字段
            FbaDeliveryDTO.AddDTO addDTO = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryAdd(shipmentList.get(0));
            FbaDeliveryLogisticsDTO.AddDTO logisticsAddDTO = new FbaDeliveryLogisticsDTO.AddDTO();
            logisticsAddDTO.setLogisticsRemark("");
            logisticsAddDTO.setTrackingNoList(new ArrayList<>());
            addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
            addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());
            //设置仓库名称
            WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(addDTO.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
            addDTO.setInventoryOrgId(warehouseEntity.getOrgId());
            //详情信息
            List<FbaDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (FbaShipmentDTO.GenerateDeliverView generateDeliverView : shipmentList) {
                //映射字段
                FbaDeliveryDetailDTO.AddDTO detailAdd = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryDetailAdd(generateDeliverView);

                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(generateDeliverView.getSkuNo())).distinct().findFirst().orElse(new SkuVO());
                detailAdd.setSkuId(skuVO.getSkuId());
                detailAdd.setNetWeight(skuVO.getNetWeight());
                //拆分产品尺寸
                String productSize = skuVO.getProductSize();
                splitProductSize(detailAdd, productSize);

                detailAdd.setWarehouseLocation(generateDeliverView.getWarehouseLocation());
                detailAdd.setSourceDetailId(generateDeliverView.getId());
                detailAddList.add(detailAdd);
            }
            addDTO.setDetailList(detailAddList);
            addDTO.setLogisticsView(logisticsAddDTO);
            if (isSubmit) {
                fbaDeliveryService.addAndSubmit(addDTO);
            } else {
                fbaDeliveryService.add(addDTO);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 处理列表查询字段
     *
     * @param records
     * @Author Luo_WG
     * @Date 2023/11/2 17:35
     **/
    private void fillList(List<FbaShipmentDTO.ListDTO> records) {
        List<String> ids = records.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<String> detailIds = records.stream().map(req -> req.getDetailId()).distinct().collect(Collectors.toList());
        List<String> skuNos = records.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        //根据来源id查询发货单
        List<FbaDeliveryEntity> fbaDeliveryEntities = fbaDeliveryService.listBySourceIds(ids);
        //根据来源详情id查询发货详情
        List<FbaDeliveryDetailEntity> fbaDeliveryDetailEntities = fbaDeliveryDetailService.listBySourceDetailIds(detailIds);
        //根据详情id查询收货记录
        List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(detailIds);
        //根据sku获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        for (FbaShipmentDTO.ListDTO record : records) {
            List<FbaDeliveryEntity> deliveryEntities = fbaDeliveryEntities.stream().filter(req -> req.getSourceId().equals(record.getId())).sorted(Comparator.comparing(FbaDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deliveryEntities)) {
                record.setDeliveryCode(deliveryEntities.get(MathUtil.ZERO).getCode());
            }
            //设置发货状态中文
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));

            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
            Integer deliveryQty = fbaDeliveryDetailEntities.stream()
                    .filter(req -> req.getSourceDetailId().equals(record.getDetailId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                    .mapToInt(FbaDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            record.setDeliveryQty(deliveryQty);

            //签收数量 QuantityReceived
            Integer receiveQty = fbaShipmentReceiveEntities.stream()
                    .filter(req -> req.getDetailId().equals(record.getDetailId()))
                    .mapToInt(FbaShipmentReceiveEntity::getReceiveQty)
                    .sum();
            record.setReceiveQty(receiveQty);

            //发货数量-QuantityReceived，签收量大于等于发货量时，在途为0
            if (receiveQty >= deliveryQty) {
                record.setTransportQty(0);
            } else {
                record.setTransportQty(deliveryQty - receiveQty);
            }
            //产品名称
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(record.getSkuNo())).findFirst().orElse(new SkuVO());
            record.setProductName(skuVO.getSkuName());
        }
    }

    /**
     * 处理下推发货单列表需要映射和配置的字段
     *
     * @param view             货件信息
     * @param shopInfoEntities 店铺信息
     * @param skuVOList        产品信息
     * @param bomChildrenSkuDTOS        子件信息
     * @return com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView
     * @Author Luo_WG
     * @Date 2023/11/1 15:19
     **/
    private FbaShipmentDTO.GenerateDeliverView generateDeliverViewFieldHandle(FbaShipmentDTO.GenerateDeliverView view,
                                                                              List<ShopInfoEntity> shopInfoEntities,
                                                                              List<SkuVO> skuVOList,
                                                                              List<BomChildrenSkuDTO> bomChildrenSkuDTOS) {
        //设置店铺的仓位为目的仓
        ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> view.getShopId().equals(req.getId())).findFirst().orElse(new ShopInfoEntity());
        view.setDestWarehouseId(shopInfoEntity.getWarehouseId());
        view.setDestWarehouseName(shopInfoEntity.getWarehouseName());
        //发货数量默认给申报数量
        view.setDeliveryQty(view.getDeclareQty());
        //产品名称
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(view.getSkuNo())).findFirst().orElse(new SkuVO());
        view.setProductName(skuVO.getSkuName());

        //查询sku是否存在子SKU
        List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(skuVO.getSkuId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sonSkuList)) {
            view.setIsCombination(Boolean.TRUE);
        } else {
            view.setIsCombination(Boolean.FALSE);
        }

        return view;
    }

    /**
     * 拆分产品尺寸长宽高存入数据集
     *
     * @param detailAdd   数据集
     * @param productSize 需要拆分的尺寸
     * @return void
     * @Author Luo_WG
     * @Date 2023/11/2 17:28
     **/
    private void splitProductSize(FbaDeliveryDetailDTO.AddDTO detailAdd, String productSize) {
        if (StringUtils.isNotBlank(productSize)) {
            String[] productSizes = productSize.split("X");
            //长
            if (productSizes.length > 0) {
                if (StringUtils.isNotBlank(productSizes[0])) {
                    detailAdd.setProductSizeLength(new BigDecimal(productSizes[0]));
                } else {
                    detailAdd.setProductSizeLength(new BigDecimal(BigInteger.ZERO));
                }
            }
            //宽
            if (productSizes.length > 1) {
                if (StringUtils.isNotBlank(productSizes[1])) {
                    detailAdd.setProductSizeWidth(new BigDecimal(productSizes[1]));
                } else {
                    detailAdd.setProductSizeWidth(new BigDecimal(BigInteger.ZERO));
                }
            }
            //高
            if (productSizes.length > 2) {
                if (StringUtils.isNotBlank(productSizes[2])) {
                    detailAdd.setProductSizeHeight(new BigDecimal(productSizes[2]));
                } else {
                    detailAdd.setProductSizeHeight(new BigDecimal(BigInteger.ZERO));
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndSaveAll(FbaShipmentEntity entity,
                                Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap,
                                List<String> hasChildrenSkuIds,
                                List<PlatformFbaShipmentReceiveDTO> receiveDTOList,
                                List<PlatformFbaShipmentReceiveDTO> detailList) {
        // 生成单号
        if (!this.save(entity)) {
            throw new ServiceException("[FbaShipmentEntity] 保存失败: entity=" + JSONUtil.toJsonStr(entity));
        }
        String msg = StrUtil.format("新增了FBA货件【{}】",entity.getFbaShipmentId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "新增FBA货件");

        // 记录货件状态
        fbaShipmentStatusService.saveByFbaShipment(entity);

        // 详情
        List<FbaShipmentDetailEntity> newDetailEntityList = detailList
                .stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToDetailEntity(e,
                        entity,
                        listingInfoMap.get(e.getSellerSku())))
                .collect(Collectors.toList());

        // 设置绑定的SKU
        newDetailEntityList = newDetailEntityList.stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.detailSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku()), hasChildrenSkuIds))
                .collect(Collectors.toList());

        if (!fbaShipmentDetailService.saveBatch(newDetailEntityList)) {
            throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(newDetailEntityList));
        }
        Map<String, String> detailIdMap = newDetailEntityList
                .stream()
                .collect(Collectors.toMap(
                        e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku()),
                        FbaShipmentDetailEntity::getId
                ));

        // 记录签收详情
        List<FbaShipmentReceiveEntity> newReceiveEntityList = receiveDTOList
                .stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToReceiveEntity(
                        detailIdMap.get(StrUtil.format("{}_{}", e.getFnSku() + e.getSellerSku())),
                        e,
                        entity,
                        listingInfoMap.get(e.getSellerSku())))
                .collect(Collectors.toList());
        // 设置绑定的SKU
        newReceiveEntityList.forEach(e -> {
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingInfoMap.get(e.getMsku());
            if (null == listingInfoWithSkuMappingDTO){
                return;
            }
            e.setSkuNo(StringUtils.isBlank(listingInfoWithSkuMappingDTO.getProductSkuNo()) ? "" : listingInfoWithSkuMappingDTO.getProductSkuNo());
            e.setAsin(StringUtils.isBlank(listingInfoWithSkuMappingDTO.getPlatformSpuNo()) ? "" : listingInfoWithSkuMappingDTO.getPlatformSpuNo());
        });

        newReceiveEntityList = newReceiveEntityList.stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.receiveSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku())))
                .collect(Collectors.toList());


        if (!fbaShipmentReceiveService.saveBatch(newReceiveEntityList)) {
            throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(newReceiveEntityList));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateAll(
            FbaShipmentEntity oldEntity,
            FbaShipmentEntity entity,
            Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap,
            List<String> hasChildrenSkuIds,
            List<PlatformFbaShipmentReceiveDTO> receiveDTOList,
            List<PlatformFbaShipmentReceiveDTO> detailList) {
        // 记录货件状态
        entity.setId(oldEntity.getId());
        if (!oldEntity.getPlatformShipmentStatus().equalsIgnoreCase(entity.getPlatformShipmentStatus())) {
            fbaShipmentStatusService.saveByFbaShipment(entity);
        }
        // 主表更新
        if (!oldEntity.toString().equalsIgnoreCase(entity.toString())){
            if (oldEntity.getIsDeleted()){
                entity.setIsDeleted(false);
                String msg = StrUtil.format("重新添加FBA货件【{}】",entity.getFbaShipmentId());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "重新添加FBA货件");
            }
            entity = FbaShipmentConverter.INSTANCE.oldToNew(entity, oldEntity);
            if (!this.getBaseMapper().updateByIdWithoutIsDelete(entity)){
                throw new ServiceException("[FbaShipmentEntity] 更新失败: entity="+ JSONUtil.toJsonStr(entity));
            }
        }

        // 历史详情
        List<FbaShipmentDetailEntity> oldfbaShipmentDetailEntityList = fbaShipmentDetailService.listByMainIds(Collections.singletonList(oldEntity.getId()));
        Map<String, FbaShipmentDetailEntity> entityMap = oldfbaShipmentDetailEntityList.stream()
                .collect(Collectors.toMap(e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku()), Function.identity()));
        // 批量更新或保存详情列表
        List<FbaShipmentDetailEntity> saveOrUpdateDetailList = new LinkedList<>();

        FbaShipmentEntity finalEntity = entity;
        // 新详情
        List<FbaShipmentDetailEntity> newDetailEntityList = detailList
                .stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToDetailEntity(e,
                        finalEntity,
                        listingInfoMap.get(e.getSellerSku())))
                .collect(Collectors.toList());
        // 设置绑定的SKU和更新判断
        newDetailEntityList.forEach(e -> {
            // 详情Key
            String entityKey = StrUtil.format("{}_{}", e.getFnSku() + e.getMsku());
            FbaShipmentDetailEntity detailEntity = entityMap.get(entityKey);
            // 新增
            if (null == detailEntity) {
                // 转换
                e = FbaShipmentConsumerConverter.INSTANCE.detailSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku()), hasChildrenSkuIds);
                saveOrUpdateDetailList.add(e);
            } else {
                // 修改
                e.setId(detailEntity.getId());
                if (!e.toString().equals(detailEntity.toString())) {
                    saveOrUpdateDetailList.add(e);
                }
            }
        });

        if (CollectionUtils.isNotEmpty(saveOrUpdateDetailList)){
            if (!fbaShipmentDetailService.saveOrUpdateBatch(saveOrUpdateDetailList)) {
                throw new ServiceException("【FbaShipmentDetailEntity】批量更新或保存失败");
            }
        }

        // 批量更新或保存签收列表
        List<FbaShipmentReceiveEntity> saveOrUpdateReceiveList = new LinkedList<>();

        // 查询历史签收记录
        List<String> oldDetailIds = oldfbaShipmentDetailEntityList.stream().map(FbaShipmentDetailEntity::getId).collect(Collectors.toList());
        List<FbaShipmentReceiveEntity> oldReceiveEntitiyList = fbaShipmentReceiveService.listByDetailIds(oldDetailIds);

        Map<String, List<FbaShipmentReceiveEntity>> receiveEntityMap = oldReceiveEntitiyList.stream()
                .collect(Collectors.groupingBy(e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku())));

        Map<String, String> detailIdMap = saveOrUpdateDetailList
                .stream()
                .collect(Collectors.toMap(
                        e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku()),
                        FbaShipmentDetailEntity::getId
                ));

        // 记录签收详情和更新判断
        List<FbaShipmentReceiveEntity> newReceiveEntityList = receiveDTOList
                .stream()
                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToReceiveEntity(
                        detailIdMap.get(StrUtil.format("{}_{}", e.getFnSku() + e.getSellerSku())),
                        e,
                        finalEntity,
                        listingInfoMap.get(e.getSellerSku())))
                .collect(Collectors.toList());
        // 设置绑定的SKU
        newReceiveEntityList.forEach(e -> {
            // 详情Key
            String entityKey = StrUtil.format("{}_{}", e.getFnSku() + e.getMsku());
            List<FbaShipmentReceiveEntity> receiveEntityList = receiveEntityMap.get(entityKey);

            if (CollectionUtils.isEmpty(receiveEntityList)) {
                // 新增
                e = FbaShipmentConsumerConverter.INSTANCE.receiveSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku()));
                saveOrUpdateReceiveList.add(e);
            } else {
                int historyReceiveQty = receiveEntityList.stream().mapToInt(FbaShipmentReceiveEntity::getReceiveQty).sum();
                // 判断历史数量是否相同
                if (e.getReceiveQty() != historyReceiveQty) {
                    // 新增签收记录
                    // ERP当前签收数量 = 亚马逊当前签收数量 - ERP历史记录签收数量
                    e.setReceiveQty(e.getReceiveQty() - historyReceiveQty);
                    saveOrUpdateReceiveList.add(e);
                }
            }
        });

        if (CollectionUtils.isNotEmpty(saveOrUpdateReceiveList)){
            if (!fbaShipmentReceiveService.saveOrUpdateBatch(saveOrUpdateReceiveList)) {
                throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(newReceiveEntityList));
            }
        }
    }

    @Override
    public FbaShipmentEntity getByFbaShipmentIdAndIsDelete(String fbaShipmentId, Boolean isDelete) {
        return this.baseMapper.getByFbaShipmentIdAndIsDelete(fbaShipmentId, isDelete);
    }


    @Override
    public BatchResultDTO delete(String id) {
        FbaShipmentEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA货件单数据"));
        // 只有未发货数据允许删除
        if (!Objects.equals(FbaDeliveryStatusEnum.UN_SHIPPED.getCode(), entity.getDeliveryStatus())) {
            throw new ServiceException(ApiError.IS_DELIVERY_DELETE);
        }

        List<FbaDeliveryEntity> deliveryEntities = fbaDeliveryService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(deliveryEntities)) {
            throw new ServiceException(ApiError.EXIST_FBA_DELIVERY_NOT_DELETE);
        }

        // 删除明细数据
        fbaShipmentDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除FBA货件单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除FBA货件单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", commonService.getUserInfo().getUserName(), entity.getCode(), "FBA货件单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getCode(), "删除FBA货件单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO skuMappingBatch(String id) {
        FbaShipmentEntity entity = this.getById(id);
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(entity.getId()));
        if (ObjectUtil.isEmpty(fbaShipmentDetailEntities)) {
            throw new ServiceException(ApiError.FBA_SHIPMENT_DETAIL_NOT_EXIST);
        }
        List<String> mskuList = fbaShipmentDetailEntities.stream().map(req -> req.getMsku()).collect(Collectors.toList());
        //根据平台sku查询Listing信息
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(mskuList);
        listingInfoParamDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        List<SkuMappingDTO.SkuDTO> skuDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

        for (FbaShipmentDetailEntity detailEntity : fbaShipmentDetailEntities) {
            SkuMappingDTO.SkuDTO skuDTO = skuDTOS.stream().filter(req -> req.getPlatformSkuNo().equals(detailEntity.getMsku())).findFirst().orElse(null);
            //校验对照表是否有对照关系
            if (ObjectUtil.isNotEmpty(skuDTO)) {
                detailEntity.setSkuNo(skuDTO.getProductSkuNo());
                detailEntity.setSkuId(skuDTO.getProductSkuId());
                //添加日志
                FbaShipmentEntity old = this.getById(detailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.FBA_SHIPMENT.getCode(), detailEntity.getId(), "", "");
                fbaShipmentDetailService.updateById(detailEntity);
            } else {
                return BatchResultDTO.fail(detailEntity.getId(), detailEntity.getMsku(), "更新失败，无对照关系！");
            }
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新成功！");
    }

    @Override
    public List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> GenerateRequisitionApplicationView(BaseIdsDTO.IdsDTO ids) {
        return null;
    }

    @Override
    public Boolean generateRequisitionApplicationSave(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list) {
        return null;
    }

    @Override
    public Boolean updateDeliveryStatus(List<String> ids, String deliveryStatus) {
        return lambdaUpdate().in(FbaShipmentEntity::getId, ids).set(FbaShipmentEntity::getDeliveryStatus, deliveryStatus).update();
    }

    /**
     * 拆分产品尺寸长宽高存入数据集
     *
     * @param detailAdd   数据集
     * @param productSize 需要拆分的尺寸
     * @return void
     * @Author Luo_WG
     * @Date 2023/11/2 17:28
     **/
    private void splitProductSizeView(FbaDeliveryDetailDTO.ViewDTO detailAdd, String productSize) {
        if (StringUtils.isNotBlank(productSize)) {
            String[] productSizes = productSize.split("X");
            //长
            if (productSizes.length > 0) {
                if (StringUtils.isNotBlank(productSizes[0])) {
                    detailAdd.setProductSizeLength(new BigDecimal(productSizes[0]));
                } else {
                    detailAdd.setProductSizeLength(new BigDecimal(BigInteger.ZERO));
                }
            }
            //宽
            if (productSizes.length > 1) {
                if (StringUtils.isNotBlank(productSizes[1])) {
                    detailAdd.setProductSizeWidth(new BigDecimal(productSizes[1]));
                } else {
                    detailAdd.setProductSizeWidth(new BigDecimal(BigInteger.ZERO));
                }
            }
            //高
            if (productSizes.length > 2) {
                if (StringUtils.isNotBlank(productSizes[2])) {
                    detailAdd.setProductSizeHeight(new BigDecimal(productSizes[2]));
                } else {
                    detailAdd.setProductSizeHeight(new BigDecimal(BigInteger.ZERO));
                }
            }
        }
    }
}
