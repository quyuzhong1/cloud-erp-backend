package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.constant.DictKindgeeConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.LengthConverterUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBA_SHIPMENT;

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
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private FbaShipmentReceiveService fbaShipmentReceiveService;
    @Autowired
    private FbaShipmentStatusService fbaShipmentStatusService;
    @Autowired
    private FirstMileDeliveryService firstMileDeliveryService;
    @Autowired
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
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
    @Resource
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private OtherInstockService otherInstockService;
    @Resource
    private OtherOutstockService otherOutstockService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private InventoryClosedRecordService inventoryClosedRecordService;
    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SysDictFeign sysDictFeign;
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
    public Boolean skuMapping(FbaShipmentDTO.SkuMappingParamDTO dto) {
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
        listingInfoParamDTO.setShopIdList(Collections.singletonList(entity.getShopId()));
        List<SkuMappingDTO.MappingSkuViewDTO> skuDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
        List<SkuMappingDTO.MappingSkuViewDTO> collect = skuDTOS.stream()
                .filter(req -> req.getPlatformSkuNo().equals(detailEntity.getMsku())
                        && StringUtils.isNotBlank(req.getProductSkuNo()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new ServiceException(ApiError.EXIST_SKU_MAPPING);
        }
        if (ObjectUtil.isEmpty(skuDTOS)) {
            throw new ServiceException(ApiError.ERROR_M_SKU_NOT_EXIST);
        }

        //映射sku
        dto.setShopId(entity.getShopId());
        dto.setPlatform(PlatformDictEnum.AMAZON.getCode());
        dto.setId(skuDTOS.get(0).getId());
        Boolean flag = omsListingInfoFeign.skuMapping(dto);
        if (flag) {

            detailEntity.setSkuNo(dto.getSkuNo());

            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(Arrays.asList(dto.getSkuNo()));

            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(dto.getSkuNo())).findFirst().orElse(new SkuVO());
            detailEntity.setSkuId(skuVO.getSkuId());
            detailEntity.setAsin(skuDTOS.get(0).getPlatformSpuNo());
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
    public BatchResultDTO skuMappingBatch(String id) {
        FbaShipmentEntity entity = this.getById(id);
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(entity.getId()));


        if (ObjectUtil.isEmpty(fbaShipmentDetailEntities)) {
            throw new ServiceException(ApiError.FBA_SHIPMENT_DETAIL_NOT_EXIST);
        }

        //有发货单不允许修改映射关系
        List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(deliveryEntities)) {
            throw new ServiceException(ApiError.IS_DELIVERY_NOT_UPDATE_MAPPING);
        }


        List<String> mskuList = fbaShipmentDetailEntities.stream().map(req -> req.getMsku()).collect(Collectors.toList());
        //根据平台sku查询Listing信息
        ListingInfoParamDTO listingInfoParamDTO = new ListingInfoParamDTO();
        listingInfoParamDTO.setPlatformSkuNoList(mskuList);
        listingInfoParamDTO.setPlatform(PlatformDictEnum.AMAZON.getCode());
        listingInfoParamDTO.setShopIdList(Collections.singletonList(entity.getShopId()));
        List<SkuMappingDTO.MappingSkuViewDTO> skuDTOS = skuMappingFeign.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);

        //根据sku查询拥有的子sku
        List<String> skuIds = skuDTOS.stream().map(req -> req.getProductSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        for (FbaShipmentDetailEntity detailEntity : fbaShipmentDetailEntities) {

            FbaShipmentDetailEntity old = new FbaShipmentDetailEntity();
            BeanMapper.copy(detailEntity, old);

            SkuMappingDTO.MappingSkuViewDTO skuDTO = skuDTOS.stream().filter(req -> req.getPlatformSkuNo().equals(detailEntity.getMsku())).findFirst().orElse(null);
            //校验对照表是否有对照关系
            if (ObjectUtil.isNotEmpty(skuDTO)) {
                detailEntity.setSkuNo(skuDTO.getProductSkuNo());
                detailEntity.setSkuId(skuDTO.getProductSkuId());
                detailEntity.setAsin(skuDTO.getPlatformSpuNo());
                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(skuDTO.getProductSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    detailEntity.setIsCombination(Boolean.TRUE);
                } else {
                    detailEntity.setIsCombination(Boolean.FALSE);
                }

                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.FBA_SHIPMENT_NOT_EXIST);
                }
                operateLogService.addModuleOperateLogByObj(old, detailEntity, ModuleTypeEnum.FBA_SHIPMENT.getCode(),detailEntity.getMainId(),"",String.format("【%s】",old.getSkuNo()));

                fbaShipmentDetailService.updateById(detailEntity);
            } else {
                return BatchResultDTO.fail(detailEntity.getId(), detailEntity.getMsku(), "更新失败，无对照关系！");
            }
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "更新成功！");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pullShipment(FbaShipmentDTO.PullShipmentDTO dto) {
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
    public List<FirstMileDeliveryDTO.DeliverRecordView> listDeliverRecord(String id) {
        List<FirstMileDeliveryDTO.DeliverRecordView> deliverRecordViews = firstMileDeliveryService.listDeliveryRecordBySourceIds(Arrays.asList(id));
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
        List<FbaShipmentDTO.ShipmentStatusRecordView> listSort = list.stream().sorted(Comparator.comparing(FbaShipmentDTO.ShipmentStatusRecordView::getUpdateTime).reversed()).collect(Collectors.toList());
        return listSort;
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
        return list.stream()
                .sorted(Comparator.comparing(FbaShipmentDTO.ReceiveRecordView::getReceiveTime))
                .collect(Collectors.toList());
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishShipment(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<FbaShipmentEntity> fbaShipmentEntities = super.listByIds(ids);
        List<FbaShipmentEntity> list = fbaShipmentEntities.stream()
                .filter(req -> !FbaDeliveryStatusEnum.SHIPPED.getCode().equals(req.getDeliveryStatus())
                        && !FbaDeliveryStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(req.getDeliveryStatus()))
                .collect(Collectors.toList());
        // 只有已发货或自动完结的单据才能完结
        if (list.size() > 0) {
            throw new ServiceException(ApiError.IS_DELIVERY_FINISH);
        }

        // 查询店铺信息
        List<String> shopIds = fbaShipmentEntities.stream().map(req -> req.getShopId()).collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(shopIds);

        //根据用户id查询用户详情信息
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        FindUserDTO userByUserId = sysUserFeign.getUserByUserId(userInfo.getUid());

        //根据主表id查询货件详情信息
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(ids);

        List<String> detailIds = fbaShipmentDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());

        //根据来源详情id查询发货详情
        List<FirstMileDeliveryDetailEntity> fbaDeliveryDetailEntities = firstMileDeliveryDetailService.listBySourceDetailIds(detailIds);

        //检查是否有差异数据
        for (FbaShipmentEntity fbaShipmentEntity : fbaShipmentEntities) {
            List<FbaShipmentDetailEntity> detailEntityList = fbaShipmentDetailEntities.stream().filter(req -> req.getMainId().equals(fbaShipmentEntity.getId())).collect(Collectors.toList());

            //查询店铺信息,用户获取目的仓
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> req.getId().equals(fbaShipmentEntity.getShopId())).findFirst().orElse(null);

            //其他入库单详情集合
            List<OtherInstockDetailDTO.AddDTO> instockDetailList = new ArrayList<>();

            //其他出库单详情集合
            List<OtherOutstockDetailDTO.AddDTO> outstockDetailList = new ArrayList<>();

            //处理货件详情
            for (FbaShipmentDetailEntity detailEntity : detailEntityList) {
                //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
                Integer deliveryQtySum = fbaDeliveryDetailEntities.stream()
                        .filter(req -> req.getSourceDetailId().equals(detailEntity.getId())
                                && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                        )
                        .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                        .sum();

                // 如果签收数大于发货数量，其他入库单报溢
                if (detailEntity.getReceiveQty() > deliveryQtySum) {
                    OtherInstockDetailDTO.AddDTO addDTO = new OtherInstockDetailDTO.AddDTO();
                    addDTO.setSkuId(detailEntity.getSkuId());
                    addDTO.setSkuNo(detailEntity.getSkuNo());
                    addDTO.setWarehouseLocation("");
                    addDTO.setActualQty(detailEntity.getReceiveQty() - deliveryQtySum);
                    addDTO.setRemark(StrUtil.format("[手动完结]FBA货件【{}】超收，自动生成其他入库报溢", fbaShipmentEntity.getCode()));
                    instockDetailList.add(addDTO);
                } else if (detailEntity.getReceiveQty() < deliveryQtySum) {
                    // 如果签收数小于发货数量，其他出库单报损
                    OtherOutstockDetailDTO.AddDTO addDTO = new OtherOutstockDetailDTO.AddDTO();
                    addDTO.setSkuId(detailEntity.getSkuId());
                    addDTO.setSkuNo(detailEntity.getSkuNo());
                    addDTO.setWarehouseLocation("");
                    addDTO.setActualQty(deliveryQtySum - detailEntity.getReceiveQty());
                    addDTO.setRemark(StrUtil.format("[手动完结]FBA货件【{}】手动完结，自动生成其他出库报损", fbaShipmentEntity.getCode()));
                    outstockDetailList.add(addDTO);
                }
            }

            //新增其他入库单
            if (CollectionUtils.isNotEmpty(instockDetailList)) {
                this.generateOtherInstock(shopInfoEntity.getWarehouseId(), userByUserId.getDepartmentId(), instockDetailList);
            }
            //新增其他出库单
            if (CollectionUtils.isNotEmpty(outstockDetailList)) {
                this.generateOtherOutstock(shopInfoEntity.getWarehouseId(), userByUserId.getDepartmentId(), outstockDetailList,true);
            }
        }

        //修改发货状态
        Boolean flag = lambdaUpdate()
                .set(FbaShipmentEntity::getDeliveryStatus, FbaDeliveryStatusEnum.MANUAL_COMPLETION.getCode())
                .in(FbaShipmentEntity::getId, ids).update();

        //操作日志
        List<Pair<String, String>> pairList = fbaShipmentEntities.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
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

            //货件没有下推【要货申请】的单据不允许下推发货单（做配置开关，上线前先关闭） TODO
/*
            RequisitionApplicationEntity applicationEntity = requisitionApplicationEntities.stream()
                    .filter(req -> req.getSourceId().equals(view.getMainId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(applicationEntity)) {
                throw new ServiceException(ApiError.REQUISITION_APPLICATION_NOT_EXIST, view.getCode());
            }
*/

            //处理字段映射
            generateDeliverViewFieldHandle(view, shopInfoEntities, skuVOList, bomChildrenSkuDTOS);
        }
        return list;
    }


    @Override
    public FirstMileDeliveryDTO.ViewDTO getDeliverView(String id) {
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
/*
            RequisitionApplicationEntity applicationEntity = requisitionApplicationEntities.stream()
                    .filter(req -> req.getSourceId().equals(view.getMainId()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isEmpty(applicationEntity)) {
                throw new ServiceException(ApiError.REQUISITION_APPLICATION_NOT_EXIST, view.getCode());
            }
*/

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
        FirstMileDeliveryDTO.ViewDTO viewDTO = FbaShipmentConverter.INSTANCE.fbaShipmentEntityToFbaDeliveryViewDTO(entity);
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

        //查询已发货的货件信息
        List<String> sourceDetailIdList = fbaShipmentDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> entities = firstMileDeliveryDetailService.listBySourceDetailIds(sourceDetailIdList);
        List<FirstMileDeliveryDetailDTO.ViewDTO> detailList = new ArrayList<>();
        for (FbaShipmentDetailEntity detailEntity : fbaShipmentDetailEntities) {
            //映射详情字段
            FirstMileDeliveryDetailDTO.ViewDTO detailDto = FbaShipmentConverter.INSTANCE.fbaShipmentDetailEntityToDeliveryDetailViewDTO(detailEntity);

            //设置产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(detailEntity.getSkuNo())).findFirst().orElse(new SkuVO());
            detailDto.setProductName(skuVO.getSkuName());
            detailDto.setNetWeight(skuVO.getNetWeight());

            //获取已出库数量（排除此单出库数量）
            Integer useDeliveryQty = entities.stream()
                    .filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                            && req.getSourceDetailId().equals(detailEntity.getId())
                            && !req.getId().equals(detailEntity.getId()))
                    .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            detailDto.setUseDeliveryQty(useDeliveryQty);
            detailDto.setProductSizeLength(skuVO.getProductLength());
            detailDto.setProductSizeWidth(skuVO.getProductWidth());
            detailDto.setProductSizeHeight(skuVO.getProductHeight());
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

        //查询已发货的货件信息
        List<String> sourceDetailIdList = list.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> entities = firstMileDeliveryDetailService.listBySourceDetailIds(sourceDetailIdList);

        for (Map.Entry<String, List<FbaShipmentDTO.GenerateDeliverView>> entry : map.entrySet()) {
            List<FbaShipmentDTO.GenerateDeliverView> shipmentList = entry.getValue();
            //映射字段
            FirstMileDeliveryDTO.AddDTO addDTO = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryAdd(shipmentList.get(0));

            addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
            addDTO.setDemandType(FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode());
            //设置仓库名称
            WarehouseEntity warehouseEntity = warehouseEntities.stream().filter(req -> req.getId().equals(addDTO.getDeliveryWarehouseId())).findFirst().orElse(new WarehouseEntity());
            addDTO.setInventoryOrgId(warehouseEntity.getOrgId());


            //详情信息
            List<FirstMileDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (FbaShipmentDTO.GenerateDeliverView generateDeliverView : shipmentList) {

                //获取已出库数量（排除此单出库数量）
                Integer useDeliveryQty = entities.stream()
                        .filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())
                                && req.getSourceDetailId().equals(generateDeliverView.getId())
                                && !req.getId().equals(generateDeliverView.getId()))
                        .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                        .sum();
                //发货数量大于申报数量
                if (useDeliveryQty > generateDeliverView.getDeclareQty()) {
                    throw new ServiceException(ApiError.DELIVERY_QTY_EXCEED_DECLAREQTY, generateDeliverView.getSkuNo());
                }

                //映射字段
                FirstMileDeliveryDetailDTO.AddDTO detailAdd = FbaShipmentConverter.INSTANCE.fbaGenerateDeliverViewToDeliveryDetailAdd(generateDeliverView);

                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuNo().equals(generateDeliverView.getSkuNo())).distinct().findFirst().orElse(null);
                if(Objects.isNull(skuVO)){
                    throw new ServiceException(StrUtil.format("{}未找到产品信息",generateDeliverView.getSkuNo()));
                }
                detailAdd.setSkuId(skuVO.getSkuId());
                detailAdd.setNetWeight(skuVO.getNetWeight());
                detailAdd.setProductSizeLength(LengthConverterUtil.mmToCm(skuVO.getProductLength()));
                detailAdd.setProductSizeWidth(LengthConverterUtil.mmToCm(skuVO.getProductWidth()));
                detailAdd.setProductSizeHeight(LengthConverterUtil.mmToCm(skuVO.getProductHeight()));

                detailAdd.setWarehouseLocation(generateDeliverView.getWarehouseLocation());
                detailAdd.setSourceDetailId(generateDeliverView.getId());
                detailAddList.add(detailAdd);
            }
            addDTO.setDetailList(detailAddList);
            if (isSubmit) {
                firstMileDeliveryService.addAndSubmit(addDTO);
            } else {
                firstMileDeliveryService.add(addDTO);
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
        List<FirstMileDeliveryEntity> fbaDeliveryEntities = firstMileDeliveryService.listBySourceIds(ids);
        //根据来源详情id查询发货详情
        List<FirstMileDeliveryDetailEntity> fbaDeliveryDetailEntities = firstMileDeliveryDetailService.listBySourceDetailIds(detailIds);
        //根据详情id查询收货记录
        List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntities = fbaShipmentReceiveService.listByDetailIds(detailIds);
        //根据sku获取产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
        for (FbaShipmentDTO.ListDTO record : records) {
            List<FirstMileDeliveryEntity> deliveryEntities = fbaDeliveryEntities.stream().filter(req -> req.getSourceId().equals(record.getId())).sorted(Comparator.comparing(FirstMileDeliveryEntity::getCreateTime).reversed()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deliveryEntities)) {
                record.setDeliveryCode(deliveryEntities.get(MathUtil.ZERO).getCode());
            }
            record.setPackingDownload(record.getIsPackingDownload()?"已下载":"未下载");
            //设置发货状态中文
            record.setDeliveryStatusName(FbaDeliveryStatusEnum.getName(record.getDeliveryStatus()));

            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
            Integer deliveryQty = fbaDeliveryDetailEntities.stream()
                    .filter(req -> req.getSourceDetailId().equals(record.getDetailId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                    .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            record.setDeliveryQty(deliveryQty);

            //签收数量 QuantityReceived
//            Integer receiveQty = fbaShipmentReceiveEntities.stream()
//                    .filter(req -> req.getDetailId().equals(record.getDetailId()))
//                    .mapToInt(FbaShipmentReceiveEntity::getReceiveQty)
//                    .sum();
            Integer receiveQty = record.getReceiveQty();
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

        // 检查历史领星的签收记录绑定
        List<FbaShipmentReceiveEntity> list = fbaShipmentReceiveService.checkAndBindHistory(entity, newDetailEntityList, PlatformEnum.LINGXING.getName());
        if (CollectionUtils.isNotEmpty(list)){
            // 查询最新库存关账记录
            Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());
            // 按签收日期分组调拨
            Map<LocalDateTime, List<FbaShipmentReceiveEntity>> groupMap = list.stream().collect(Collectors.groupingBy(FbaShipmentReceiveEntity::getReceiveDate));

            for (Map.Entry<LocalDateTime, List<FbaShipmentReceiveEntity>> entry : groupMap.entrySet()) {
                LocalDate billDate = entry.getKey().toLocalDate();
                // 执行调拨逻辑
                this.handlerWarehouse(entity, entry.getValue(), billDate, closedDateMap);
            }
        }

        // 检查货件是否生成签收记录
//        if (this.checkStopGenReceived(entity)){
//            // 检查历史领星的签收记录绑定
//            fbaShipmentReceiveService.checkAndBindHistory(entity, newDetailEntityList, PlatformEnum.LINGXING.getName());
//            return;
//        }

//        Map<String, String> detailIdMap = newDetailEntityList
//                .stream()
//                .collect(Collectors.toMap(
//                        e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku()),
//                        FbaShipmentDetailEntity::getId
//                ));
//
//        // 记录签收详情
//        List<FbaShipmentReceiveEntity> newReceiveEntityList = receiveDTOList
//                .stream()
//                .filter(e-> e.getReceiveQty() > 0)
//                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToReceiveEntity(
//                        detailIdMap.get(StrUtil.format("{}_{}", e.getFnSku() + e.getSellerSku())),
//                        e,
//                        entity,
//                        listingInfoMap.get(e.getSellerSku())))
//                .collect(Collectors.toList());
//        // 设置绑定的SKU
//        newReceiveEntityList.forEach(e -> {
//            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = listingInfoMap.get(e.getMsku());
//            if (null == listingInfoWithSkuMappingDTO){
//                return;
//            }
//            e.setSkuNo(StringUtils.isBlank(listingInfoWithSkuMappingDTO.getProductSkuNo()) ? "" : listingInfoWithSkuMappingDTO.getProductSkuNo());
//            e.setAsin(StringUtils.isBlank(listingInfoWithSkuMappingDTO.getPlatformSpuNo()) ? "" : listingInfoWithSkuMappingDTO.getPlatformSpuNo());
//        });
//
//        newReceiveEntityList = newReceiveEntityList.stream()
//                .map(e -> FbaShipmentConsumerConverter.INSTANCE.receiveSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku())))
//                .collect(Collectors.toList());
//
//        if (!CollectionUtils.isEmpty(newReceiveEntityList)){
//            if (!fbaShipmentReceiveService.saveBatch(newReceiveEntityList)) {
//                throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(newReceiveEntityList));
//            }
//            // 查询最新库存关账记录
//            Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId();
//            handlerWarehouse(entity, newReceiveEntityList, LocalDate.now(), closedDateMap);
//        }
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
            String msg = StrUtil.format("FBA货件【{}】平台状态由【{}】变更为【{}】",
                    entity.getFbaShipmentId(),
                    oldEntity.getPlatformShipmentStatus(),
                    entity.getPlatformShipmentStatus()
            );
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "FBA货件状态变更");
            fbaShipmentStatusService.saveByFbaShipment(entity);
        }
        // 主表更新
        if (!oldEntity.toString().equalsIgnoreCase(entity.toString())){
//            if (oldEntity.getIsDeleted()){
//                entity.setIsDeleted(false);
//                String msg = StrUtil.format("重新添加FBA货件【{}】",entity.getFbaShipmentId());
//                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "重新添加FBA货件");
//            }
            entity = FbaShipmentConverter.INSTANCE.oldToNew(entity, oldEntity);
            if (!this.updateById(entity)){
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
                e.setDeliveryQty(detailEntity.getDeliveryQty());
                e.setReceiveDate(detailEntity.getReceiveDate());
                if (!Objects.equals(e.getReceiveQty(), detailEntity.getReceiveQty()) && e.getReceiveQty() > 0){
                    e.setReceiveDate(LocalDateTime.now(ZoneId.systemDefault()));
                }
                if (!oldEntity.getDeliveryStatus().equalsIgnoreCase(DeliveryStatusEnum.UN_SHIPPED.getCode())){
                    e.setDiffQty(e.getReceiveQty() - detailEntity.getDeliveryQty());
                }
                // 保留历史映射关系
                if (StringUtils.isNotBlank(detailEntity.getSkuId()) && StringUtils.isNotBlank(detailEntity.getSkuNo())){
                    e.setSkuId(detailEntity.getSkuId());
                    e.setSkuNo(detailEntity.getSkuNo());
                    e.setIsCombination(detailEntity.getIsCombination());
                }
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

        // 检查货件是否生成签收记录
//        if (this.checkStopGenReceived(oldEntity)){
//            return;
//        }

//        // 批量更新或保存签收列表
//        List<FbaShipmentReceiveEntity> saveReceiveList = new LinkedList<>();
//
//        // 查询历史签收记录
//        List<String> oldDetailIds = oldfbaShipmentDetailEntityList.stream().map(FbaShipmentDetailEntity::getId).collect(Collectors.toList());
//        List<FbaShipmentReceiveEntity> oldReceiveEntitiyList = fbaShipmentReceiveService.listByDetailIds(oldDetailIds);
//
//        Map<String, List<FbaShipmentReceiveEntity>> receiveEntityMap = oldReceiveEntitiyList.stream()
//                .collect(Collectors.groupingBy(e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku())));
//
//        Map<String, String> detailIdMap = saveOrUpdateDetailList
//                .stream()
//                .collect(Collectors.toMap(
//                        e -> StrUtil.format("{}_{}", e.getFnSku() + e.getMsku()),
//                        FbaShipmentDetailEntity::getId
//                ));
//
//        // 记录签收详情和更新判断
//        List<FbaShipmentReceiveEntity> newReceiveEntityList = receiveDTOList
//                .stream()
////                .filter(e-> e.getReceiveQty() > 0)
//                .map(e -> FbaShipmentConsumerConverter.INSTANCE.fbaShipmentToReceiveEntity(
//                        detailIdMap.get(StrUtil.format("{}_{}", e.getFnSku() + e.getSellerSku())),
//                        e,
//                        finalEntity,
//                        listingInfoMap.get(e.getSellerSku())))
//                .collect(Collectors.toList());
//
//        if (CollectionUtils.isEmpty(newReceiveEntityList)){
//            return;
//        }
//
//        // 设置绑定的SKU
//        newReceiveEntityList.forEach(e -> {
//            // 详情Key
//            String entityKey = StrUtil.format("{}_{}", e.getFnSku() + e.getMsku());
//            List<FbaShipmentReceiveEntity> receiveEntityList = receiveEntityMap.get(entityKey);
//
//            if (CollectionUtils.isEmpty(receiveEntityList)) {
//                // 新增
//                e = FbaShipmentConsumerConverter.INSTANCE.receiveSetSkuMappingInfo(e, listingInfoMap.get(e.getMsku()));
//                saveReceiveList.add(e);
//            } else {
//                int historyReceiveQty = receiveEntityList.stream().mapToInt(FbaShipmentReceiveEntity::getReceiveQty).sum();
//                // 判断历史数量是否相同
//                if (e.getReceiveQty() != historyReceiveQty) {
//                    String msg = StrUtil.format("FBA货件【{}】,平台SKU【{}】签收数量由【{}】变更为【{}】",
//                            finalEntity.getFbaShipmentId(),
//                            e.getMsku(),
//                            historyReceiveQty,
//                            e.getReceiveQty()
//                    );
//                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), finalEntity.getId(), "FBA签收数量变更");
//
//                    // 新增签收记录
//                    // ERP当前签收数量 = 亚马逊当前签收数量 - ERP历史记录签收数量
//                    e.setReceiveQty(e.getReceiveQty() - historyReceiveQty);
//                    saveReceiveList.add(e);
//                }
//            }
//        });
//
//
//        if (CollectionUtils.isNotEmpty(saveReceiveList)){
//            if (!fbaShipmentReceiveService.saveBatch(saveReceiveList)) {
//                throw new ServiceException("[FbaShipmentDetailEntity] 批量保存失败: entity=" + JSONUtil.toJsonStr(saveReceiveList));
//            }
//            // 查询最新库存关账记录
//            Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId();
//            handlerWarehouse(entity, saveReceiveList, LocalDate.now(), closedDateMap);
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handlerWarehouse(FbaShipmentEntity entity, List<FbaShipmentReceiveEntity> saveReceiveList, LocalDate billDate, Map<String, LocalDate> closedDateMap) {

        // 查询是否有发货单号
        FirstMileDeliveryEntity deliveryEntity = firstMileDeliveryService.findBySourceId(entity.getId());

        //其他入库单明细信息
        List<OtherInstockDetailDTO.AddDTO> detailAddList = new ArrayList<>();

        //其他出库单明细信息
        List<OtherOutstockDetailDTO.AddDTO> outstockDetailList = new ArrayList<>();

        // 当前店铺
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(entity.getShopId());
        //根据用户id查询用户详情信息
        FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(shopInfoEntity.getChargeId());
        if (null != deliveryEntity ){

            // 校验是否手动完结，如果已经手动完结，多余的放到其他入库，入到目的仓然后return，不用调拨
            if (FbaDeliveryStatusEnum.MANUAL_COMPLETION.getCode().equals(entity.getDeliveryStatus())) {
                //查询原签收数量，比较新获取的签收数量，多的新增其他入库
                for (FbaShipmentReceiveEntity detailEntity : saveReceiveList) {
                    if(detailEntity.getReceiveQty() > 0){
                        OtherInstockDetailDTO.AddDTO addDTO = new OtherInstockDetailDTO.AddDTO();
                        addDTO.setSkuId(detailEntity.getSkuId());
                        addDTO.setSkuNo(detailEntity.getSkuNo());
                        addDTO.setWarehouseLocation("");
                        addDTO.setActualQty(detailEntity.getReceiveQty());
                        addDTO.setRemark(StrUtil.format("FBA货件【{}】超收，自动生成其他入库报溢", entity.getCode()));
                        detailAddList.add(addDTO);
                    }else{
                        // 如果签收数小于发货数量，其他出库单报损
                        OtherOutstockDetailDTO.AddDTO addDTO = new OtherOutstockDetailDTO.AddDTO();
                        addDTO.setSkuId(detailEntity.getSkuId());
                        addDTO.setSkuNo(detailEntity.getSkuNo());
                        addDTO.setWarehouseLocation("");
                        addDTO.setActualQty(Math.abs(detailEntity.getReceiveQty()));
                        addDTO.setRemark(StrUtil.format("【{}】签收数量减少后出库反冲", entity.getCode()));
                        outstockDetailList.add(addDTO);
                    }
                }
                if (CollectionUtils.isNotEmpty(detailAddList)){
                    FindUserDTO userByUserId = sysUserFeign.getUserByUserId(entity.getUpdateUserId());
                    String dept = StringUtils.isBlank(findUserDTO.getDepartmentId()) ? userByUserId.getDepartmentId() : findUserDTO.getDepartmentId();
                    this.generateOtherInstock(shopInfoEntity.getWarehouseId(), dept, detailAddList);
                }
                //如果是负数生成其他出库单，目的仓报损
                if (CollectionUtils.isNotEmpty(outstockDetailList)) {
                    FindUserDTO userByUserId = sysUserFeign.getUserByUserId(entity.getUpdateUserId());
                    this.generateOtherOutstock(shopInfoEntity.getWarehouseId(), userByUserId.getDepartmentId(), outstockDetailList,false);
                }
                return;
            }

            //新增直接调拨单
            List<FbaShipmentReceiveEntity> greaterThanZeroReceiveList = saveReceiveList.stream().filter(v->v.getReceiveQty()>0).collect(Collectors.toList());
            List<FbaShipmentReceiveEntity> lessThanZeroReceiveList = saveReceiveList.stream().filter(v->v.getReceiveQty()<0).collect(Collectors.toList());
            //正数签收生成在途仓-目的仓
            if(CollectionUtils.isNotEmpty(greaterThanZeroReceiveList)){
                this.generateTransfer(shopInfoEntity,entity,greaterThanZeroReceiveList,false,String.format("FBA货件【%s】签收自动创建", entity.getCode()), billDate, TransferDirectionEnum.ORDINARY.getCode(), closedDateMap);
            }
            //负数签收生成目的仓-在途仓
            if(CollectionUtils.isNotEmpty(lessThanZeroReceiveList)){
                this.generateTransfer(shopInfoEntity,entity,lessThanZeroReceiveList,true,String.format("【%s】签收数量减少后反向调拨", entity.getCode()), billDate, TransferDirectionEnum.RETURN_GOODS.getCode(), closedDateMap);
            }


            // 校验是否自动完结，生成直接调拨单后，状态改为已发货-已签收
            if (FbaDeliveryStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(entity.getDeliveryStatus())) {
                this.updateDeliveryStatus(entity.getId(), FbaDeliveryStatusEnum.SHIPPED.getCode());
            }

            //如果收货数量等于发货数量，修改货件状态为自动完结
            List<FbaShipmentDetailEntity> allDetailList = fbaShipmentDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            int receiveQtySum = allDetailList.stream().mapToInt(FbaShipmentDetailEntity::getReceiveQty).sum();
            List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            List<String> detailIds = fbaShipmentDetailEntities.stream().map(BaseEntity::getId).collect(Collectors.toList());

            //根据来源详情id查询发货详情
            List<FirstMileDeliveryDetailEntity> fbaDeliveryDetailEntities = firstMileDeliveryDetailService.listBySourceDetailIds(detailIds);
            //发货数量 关联的发货单中SKU的发货数量，多个发货单汇总
            int deliveryQtySum = fbaDeliveryDetailEntities.stream()
                    .filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                    .mapToInt(FirstMileDeliveryDetailEntity::getDeliveryQty)
                    .sum();
            if (receiveQtySum == deliveryQtySum) {
                this.updateDeliveryStatus(entity.getId(), FbaDeliveryStatusEnum.AUTOMATIC_COMPLETION.getCode());
            }

        } else {

            log.warn("【FBA货件更新】无找到有发货单, 不下推直接调拨单");
            // 检查是否都有映射
            boolean allMatch = saveReceiveList.stream().allMatch(e -> StringUtils.isNotBlank(e.getSkuId()) && StringUtils.isNotBlank(e.getSkuNo()));
            if (!allMatch){
                log.warn("【FBA货件更新】未找到所有映射数据, 暂下推直接调拨单");
                return;
            }
/*
            for (FbaShipmentReceiveEntity fbaShipmentReceiveEntity : saveReceiveList) {
                OtherInstockDetailDTO.AddDTO addDTO = new OtherInstockDetailDTO.AddDTO();
                addDTO.setSkuId(fbaShipmentReceiveEntity.getSkuId());
                addDTO.setSkuNo(fbaShipmentReceiveEntity.getSkuNo());
                addDTO.setWarehouseLocation("");
                addDTO.setActualQty(fbaShipmentReceiveEntity.getReceiveQty());
                addDTO.setRemark(StrUtil.format("货件【{}】未找到发货单，自动生成其他入库报溢", entity.getCode()));
                detailAddList.add(addDTO);
            }
            FindUserDTO userByUserId = sysUserFeign.getUserByUserId(entity.getUpdateUserId());
            String dept = StringUtils.isBlank(findUserDTO.getDepartmentId()) ? userByUserId.getDepartmentId() : findUserDTO.getDepartmentId();
            // 找不到发货单 直接生成其他入库到目的仓的可用
            this.generateOtherInstock(shopInfoEntity.getWarehouseId(), dept, detailAddList);
*/
            //新增直接调拨单
            List<FbaShipmentReceiveEntity> greaterThanZeroReceiveList = saveReceiveList.stream().filter(v->v.getReceiveQty()>0).collect(Collectors.toList());
            List<FbaShipmentReceiveEntity> lessThanZeroReceiveList = saveReceiveList.stream().filter(v->v.getReceiveQty()<0).collect(Collectors.toList());
            //正数签收生成在途仓-目的仓
            if(CollectionUtils.isNotEmpty(greaterThanZeroReceiveList)){
                this.generateTransfer(shopInfoEntity,entity,greaterThanZeroReceiveList,false,String.format("FBA货件【%s】签收自动创建", entity.getCode()), billDate, TransferDirectionEnum.ORDINARY.getCode(), closedDateMap);
            }
            //负数签收生成目的仓-在途仓
            if(CollectionUtils.isNotEmpty(lessThanZeroReceiveList)){
                this.generateTransfer(shopInfoEntity,entity,lessThanZeroReceiveList,true,String.format("【%s】签收数量减少后反向调拨", entity.getCode()), billDate, TransferDirectionEnum.RETURN_GOODS.getCode(), closedDateMap);
            }
        }
    }

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/12/7 16:54
     * @param id
     * @param deliveryStatus
     * @return java.lang.Boolean
     **/
    private Boolean updateDeliveryStatus(String id, String deliveryStatus) {
        return lambdaUpdate().eq(FbaShipmentEntity::getId, id).set(FbaShipmentEntity::getDeliveryStatus, deliveryStatus).update();
    }

    /**
     * 生成其他入库单
     * @Author Luo_WG
     * @Date 2023/12/8 9:34
     * @param warehouseId 调入仓库
     * @param deptId 部门
     * @param detailAddList 入库单明细信息
     * @return void
     **/
    private void generateOtherInstock(String warehouseId, String deptId, List<OtherInstockDetailDTO.AddDTO> detailAddList) {
        OtherInstockDTO.AddDTO addDTO = new OtherInstockDTO.AddDTO();
        //入库日期
        addDTO.setBillDate(LocalDate.now());
        //库存方向
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        //收货仓库id
        addDTO.setWarehouseId(warehouseId);
        //部门
        addDTO.setDeptId(deptId);
        //入库类型：报溢
        addDTO.setType(InstockTypeEnum.REPORT_OVERFLOW.getCode());
        //详情
        addDTO.setDetailList(detailAddList);
        otherInstockService.addAndApprove(addDTO);
    }

    /**
     * 生成其他出库单
     * @Author Luo_WG
     * @Date 2023/12/7 16:15
     * @param warehouseId 调入仓库
     * @param deptId 部门
     * @param detailAddList 出库单明细信息
     * @return void
     **/
    private void generateOtherOutstock(String warehouseId, String deptId, List<OtherOutstockDetailDTO.AddDTO> detailAddList,Boolean isOnwayWarehouse) {
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);

        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        checkOnwayWarehouse(warehouseEntity);

        OtherOutstockDTO.AddDTO addDTO = new OtherOutstockDTO.AddDTO();
        //出库日期
        addDTO.setBillDate(LocalDate.now());
        //库存方向：普通
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        //发货仓库id
        addDTO.setWarehouseId(isOnwayWarehouse?warehouseEntity.getOnwayWarehouseId():warehouseEntity.getId());
        //处理类型
        List<DictKingdeeDTO.ListDTO> typeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_TYPE_NAME);
        List<DictKingdeeDTO.ListDTO> outTypeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_OUT_TYPE_NAME);
        DictKingdeeDTO.ListDTO typeDTO = typeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_MATERIAL_PICKING)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        // 业务类型
        addDTO.setType(typeDTO.getCode());
        addDTO.setTypeName(typeDTO.getName());
        //出库类型
        DictKingdeeDTO.ListDTO outTypeDTO = outTypeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_RECEIVE_THE_DIFFERENCE)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        addDTO.setOutType(outTypeDTO.getCode());
        addDTO.setOutTypeName(outTypeDTO.getName());

        //领料部门
        addDTO.setDeptId(deptId);
        //详情信息
        addDTO.setDetailList(detailAddList);
        otherOutstockService.addAndApprove(addDTO);
    }

    @Override
    public FbaShipmentEntity getByFbaShipmentId(String fbaShipmentId) {
        return lambdaQuery()
                .eq(FbaShipmentEntity::getFbaShipmentId, fbaShipmentId)
                .last("LIMIT 1")
                .one();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        FbaShipmentEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA货件单数据"));
        // 只有未发货数据允许删除
        if (!Objects.equals(FbaDeliveryStatusEnum.UN_SHIPPED.getCode(), entity.getDeliveryStatus())) {
            throw new ServiceException(ApiError.IS_DELIVERY_DELETE);
        }
        // 当前停止生成签收记录的时间
        LocalDate stopReceivedDate = this.getStopGenReceivedDate(entity);

        // 已生成调拨单不允许删除
        Integer count = transferInfoService.lambdaQuery()
                .in(TransferInfoEntity::getSourceCode, entity.getCode())
                .eq(TransferInfoEntity::getInvalidStatus, Boolean.FALSE)
                // 校验关账时间之后
                .gt(null != stopReceivedDate, TransferInfoEntity::getBillDate, stopReceivedDate)
                .count();
        if (count > 0){
            throw new ServiceException("已生成调拨单不允许删除");
        }

        List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryService.listBySourceIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(deliveryEntities)) {
            throw new ServiceException(ApiError.EXIST_FBA_DELIVERY_NOT_DELETE);
        }
        // 如果存在非Erp系统的签收记录， 移除关联关系
        fbaShipmentReceiveService.checkAndRemoveDetailIds(Collections.singletonList(id), stopReceivedDate);

        // 删除明细数据
        fbaShipmentDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除FBA货件单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除FBA货件单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "FBA货件单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.FBA_SHIPMENT.getCode(), entity.getId(), "删除FBA货件单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> ids) {
        List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list = baseMapper.generateRequisitionApplicationView(ids);

        //平台SKU没有映射关系，货件没有匹配到SKU的货件不允许下推发货单
        list.forEach(req -> {
            if (StringUtils.isBlank(req.getSkuNo())) {
                throw new ServiceException(ApiError.NOT_MAPPER_SKU, req.getAsin());
            }
        });

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        for (FbaShipmentDTO.GenerateRequisitionApplicationViewDTO viewDTO : list) {
            //FBA下推要货单要货类型默认是：销售平台
            viewDTO.setType(RequisitionApplicationTypeEnum.FBA.getCode());
            viewDTO.setTypeName(RequisitionApplicationTypeEnum.FBA.getName());
            //来源类型
            viewDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
            //来源类型中文
            viewDTO.setSourceTypeName(SourceTypeEnum.FBA_SHIPMENT.getName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
                viewDTO.setBomVersion(sonSkuList.get(MathUtil.ZERO).getBomVersion());
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }

            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }
            viewDTO.setRequisitionQty(viewDTO.getDeclareQty());
        }
        return list;
    }

    @Override
    public Boolean generateRequisitionApplicationSave(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list) {
        return generateRequisitionApplication(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateRequisitionApplicationSaveAndSubmit(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list) {
        return generateRequisitionApplication(list, Boolean.TRUE);
    }

    private Boolean generateRequisitionApplication(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list, Boolean isSubmit) {
        //一个发货计划单，生成一个要货申请单
        Map<String, List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>> map = list.stream().collect(Collectors.groupingBy(FbaShipmentDTO.GenerateRequisitionApplicationViewDTO::getSourceId));

        //根据仓库id查询仓库信息
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getRequisitionWarehouseId()).distinct().collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(requisitionWarehouseIds);

        for (Map.Entry<String, List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>> entry : map.entrySet()) {
            List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> value = entry.getValue();
            //映射主表信息
            RequisitionApplicationDTO.AddDTO addDTO = FbaShipmentConverter.INSTANCE.DeliveryPlanGRA(value.get(MathUtil.ZERO));

            //要货仓库中文
            WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(w -> w.getId().equals(value.get(MathUtil.ZERO).getRequisitionWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            addDTO.setRequisitionWarehouseName(updateDTO.getName());

            //映射详情信息
            List<RequisitionApplicationDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (FbaShipmentDTO.GenerateRequisitionApplicationViewDTO viewDTO : value) {

                RequisitionApplicationDetailDTO.AddDTO detailAddDto = FbaShipmentConverter.INSTANCE.DeliveryPlanDetailGRA(viewDTO);

                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);

            BaseResultDTO.AddDTO add = requisitionApplicationService.add(addDTO);
            if (isSubmit) {
                requisitionApplicationService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deliveryStatus(FirstMileDeliveryEntity deliveryEntity) {
        List<FirstMileDeliveryDetailEntity> detailList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(deliveryEntity.getId()));
        String fbaCode = detailList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).findFirst().orElse(null);
        if(StringUtils.isBlank(fbaCode)){
            return true;
        }
        FbaShipmentEntity shipmentEntity = this.getByCode(fbaCode);
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(shipmentEntity.getId()));

        //查询所有关联的发货单
        List<String> detailIds = fbaShipmentDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listBySourceDetailIds(detailIds);

        //数量计算
        for (FbaShipmentDetailEntity fbaShipmentDetailEntity : fbaShipmentDetailEntities) {
            int sumDeliveryQty = detailEntityList.stream()
                    .filter(req -> req.getSourceDetailId().equals(fbaShipmentDetailEntity.getId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                    .mapToInt(req -> req.getDeliveryQty())
                    .sum();
            //发货数量=总发货数量
            fbaShipmentDetailEntity.setDeliveryQty(sumDeliveryQty);
            //收发差异=收货数量-总发货数量
            fbaShipmentDetailEntity.setDiffQty(fbaShipmentDetailEntity.getReceiveQty() - sumDeliveryQty);
        }

        //完结不修改状态
        if (ObjectUtils.isNotEmpty(shipmentEntity)
                && !FbaDeliveryStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(shipmentEntity.getDeliveryStatus())
                && !FbaDeliveryStatusEnum.MANUAL_COMPLETION.getCode().equals(shipmentEntity.getDeliveryStatus())) {
            lambdaUpdate().eq(FbaShipmentEntity::getId, shipmentEntity.getId()).set(FbaShipmentEntity::getDeliveryStatus, FbaDeliveryStatusEnum.SHIPPED.getCode()).update();
        }
        return fbaShipmentDetailService.updateBatchById(fbaShipmentDetailEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deliveryDisApprove(FirstMileDeliveryEntity deliveryEntity) {
        List<FirstMileDeliveryDetailEntity> detailList = firstMileDeliveryDetailService.listByMainIds(Arrays.asList(deliveryEntity.getId()));
        String fbaCode = detailList.stream().map(FirstMileDeliveryDetailEntity::getFbaShipmentCode).filter(StringUtils::isNotBlank).findFirst().orElse(null);
        if(StringUtils.isBlank(fbaCode)){
            return true;
        }
        FbaShipmentEntity shipmentEntity = this.getByCode(fbaCode);
        List<FbaShipmentDetailEntity> fbaShipmentDetailEntities = fbaShipmentDetailService.listByMainIds(Arrays.asList(shipmentEntity.getId()));

        //查询所有关联的发货单
        List<String> detailIds = fbaShipmentDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> detailEntityList = firstMileDeliveryDetailService.listBySourceDetailIds(detailIds);

        //数量计算
        for (FbaShipmentDetailEntity fbaShipmentDetailEntity : fbaShipmentDetailEntities) {
            int sumDeliveryQty = detailEntityList.stream()
                    .filter(req -> req.getSourceDetailId().equals(fbaShipmentDetailEntity.getId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus()))
                    .mapToInt(req -> req.getDeliveryQty())
                    .sum();
            //发货数量=总发货数量
            fbaShipmentDetailEntity.setDeliveryQty(sumDeliveryQty);
            //收发差异=收货数量-总发货数量+本次反审的发货数量
            fbaShipmentDetailEntity.setDiffQty(fbaShipmentDetailEntity.getReceiveQty() - sumDeliveryQty);
        }
        List<FirstMileDeliveryEntity> deliveryEntities = firstMileDeliveryService.listBySourceIds(Arrays.asList(shipmentEntity.getId()));
        long count = deliveryEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        //完结不修改状态
        if (ObjectUtils.isNotEmpty(shipmentEntity)
                && !FbaDeliveryStatusEnum.AUTOMATIC_COMPLETION.getCode().equals(shipmentEntity.getDeliveryStatus())
                && !FbaDeliveryStatusEnum.MANUAL_COMPLETION.getCode().equals(shipmentEntity.getDeliveryStatus())
                && count <= 1) {
            lambdaUpdate().eq(FbaShipmentEntity::getId, shipmentEntity.getId()).set(FbaShipmentEntity::getDeliveryStatus, FbaDeliveryStatusEnum.UN_SHIPPED.getCode()).update();
        }
        return fbaShipmentDetailService.updateBatchById(fbaShipmentDetailEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Tuple generateTransferOut(ShopInfoEntity shopEntity, FbaShipmentEntity shipmentEntity, List<FbaShipmentReceiveEntity> newReceiveEntityList, Boolean isToOnwayWarehouse, String remark, LocalDate billDate, String transferDirection) {
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(shopEntity.getWarehouseId()));

        //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(req -> req.getId().equals(shopEntity.getWarehouseId())).findFirst().orElse(new WarehouseEntity());
        checkOnwayWarehouse(warehouseEntity);

        //查询在途仓
        WarehouseEntity onwayWarehouse = warehouseService.getById(warehouseEntity.getOnwayWarehouseId());

        WarehouseEntity outWarehouse = isToOnwayWarehouse?warehouseEntity:onwayWarehouse;

        WarehouseEntity inWarehouse = isToOnwayWarehouse?onwayWarehouse:warehouseEntity;

        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
        //默认来源类型：FBA货件
        addDTO.setSourceType(SourceTypeEnum.FBA_SHIPMENT.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(billDate);
        //默认调拨方向：普通
        addDTO.setTransferDirection(transferDirection);
        //调入组织
        addDTO.setInOrgId(inWarehouse.getOrgId());
        //调出组织
        addDTO.setOutOrgId(outWarehouse.getOrgId());
        //调拨类型
        if (warehouseEntity.getOrgId().equals(onwayWarehouse.getOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }

        addDTO.setSourceId(shipmentEntity.getId());
        addDTO.setSourceCode(shipmentEntity.getCode());
        addDTO.setRemark(remark);

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (FbaShipmentReceiveEntity detailEntity : newReceiveEntityList) {
            //映射产品信息
            TransferInfoDetailDTO.AddDTO detailAddDto = new TransferInfoDetailDTO.AddDTO();
            detailAddDto.setSkuId(detailEntity.getSkuId());
            detailAddDto.setSkuNo(detailEntity.getSkuNo());
            detailAddDto.setQty(Math.abs(detailEntity.getReceiveQty()));
            detailAddDto.setOutWarehouseId(outWarehouse.getId());
            detailAddDto.setOutWarehouseLocation("");
            detailAddDto.setInWarehouseId(inWarehouse.getId());
            detailAddDto.setInWarehouseLocation("");
            detailAddDto.setSourceDetailId(detailEntity.getId());
            detailAddDtoList.add(detailAddDto);
        }
        addDTO.setDetailList(detailAddDtoList);
        String transferOutId = transferInfoService.add(addDTO);
        return new Tuple(transferOutId, addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO regenerateTransferOut(String id) {
        FbaShipmentEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到FBA货件单数据"));

        // 当前停止生成签收记录的时间
        LocalDate stopReceivedDate = this.getStopGenReceivedDate(entity);

        // 已生成调拨单不允许生成
        Integer count = transferInfoService.lambdaQuery()
                .in(TransferInfoEntity::getSourceCode, entity.getCode())
                .eq(TransferInfoEntity::getInvalidStatus, Boolean.FALSE)
                .gt(null != stopReceivedDate, TransferInfoEntity::getBillDate, stopReceivedDate)
                .count();
        if (count > 0){
            throw new ServiceException("已生成调拨单不允许重新调拨");
        }
        // 历史详情
        List<FbaShipmentDetailEntity> oldDetailEntityList = fbaShipmentDetailService.listByMainIds(Collections.singletonList(id));
        if (CollectionUtils.isEmpty(oldDetailEntityList)){
            throw new ServiceException("数据异常:FBA货件详情为空");
        }
        List<String> detailIds = oldDetailEntityList.stream().map(FbaShipmentDetailEntity::getId).collect(Collectors.toList());

        // 最终处理的签收日志
        Set<FbaShipmentReceiveEntity> receiveEntitySet = new HashSet<>();
        // 查询签收记录
        List<FbaShipmentReceiveEntity> receiveEntityList = fbaShipmentReceiveService.listByDetailIdsAndSourceType(detailIds, PlatformEnum.LINGXING.getName());
        if (!CollectionUtils.isEmpty(receiveEntityList)){
            // 检查和设置最新映射关系到签收记录
            receiveEntityList = fbaShipmentReceiveService.checkAndSetReceiveSkuMapping(oldDetailEntityList, receiveEntityList);
            receiveEntitySet = new HashSet<>(receiveEntityList);
        }

        // 检查历史领星的签收记录绑定
        List<FbaShipmentReceiveEntity> list = fbaShipmentReceiveService.checkAndBindHistory(entity, oldDetailEntityList, PlatformEnum.LINGXING.getName());
        if (CollectionUtils.isNotEmpty(list)){
            receiveEntitySet.addAll(new HashSet<>(list));
        }
        if (CollectionUtils.isEmpty(receiveEntityList)){
            throw new ServiceException("未找到FBA货件签收记录");
        }

        // 根据调拨日志分组
        Map<LocalDate, List<FbaShipmentReceiveEntity>> groupBillDateMap = receiveEntitySet.stream()
                .collect(Collectors.groupingBy(e-> e.getReceiveDate().toLocalDate()));

        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());

        for (Map.Entry<LocalDate, List<FbaShipmentReceiveEntity>> entry : groupBillDateMap.entrySet()) {
            // 根据签收时间作为调拨时间
            handlerWarehouse(entity, entry.getValue(), entry.getKey(), closedDateMap);
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.REGENERATE);
    }

    /**
     * 在途仓校验
     * @Author Luo_WG
     * @Date 2023/12/8 11:24
     * @param warehouseEntity
     * @return void
     **/
    private void checkOnwayWarehouse(WarehouseEntity warehouseEntity) {
        //校验目的仓是否为FBA第三方仓
        List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("warehouseType");
        DictBasicDTO.ListDTO listDTO = warehouseTypes.stream().filter(req -> "FBA".equals(req.getValue())).findFirst().orElse(null);
        //如果是FBA第三方仓
        if (listDTO.getId().equals(warehouseEntity.getTypeId())) {
            //如果配置为空时默认为“FBA在途仓-xgwj-fba”
            if (StringUtils.isBlank(warehouseEntity.getOnwayWarehouseId())) {
                List<WarehouseEntity> warehouseEntities = warehouseService.listByKingdeeCodeList(Arrays.asList("xgwj-fba"));
                if (CollectionUtils.isEmpty(warehouseEntities)) {
                    throw new ServiceException(ApiError.WAREHOUSE_CODE_XGWJ_FBA_NOT_EXIST);
                }
                warehouseEntity.setOnwayWarehouseId(warehouseEntities.get(0).getId());
                warehouseEntity.setOnwayWarehouseName(warehouseEntities.get(0).getName());
            }
        }

        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (StringUtils.isBlank(warehouseEntity.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateTransfer(ShopInfoEntity shopInfoEntity , FbaShipmentEntity entity, List<FbaShipmentReceiveEntity> receiveList , Boolean isToOnwayWarehouse, String remark, LocalDate billDate, String transferDirection, Map<String, LocalDate> closedDateMap){
        Tuple tuple =  this.generateTransferOut(shopInfoEntity, entity,  receiveList,isToOnwayWarehouse,remark, billDate, transferDirection);
        String transferOutId = tuple.get(0);
        if (StringUtils.isNotBlank(transferOutId)) {
            // 关账时间之前的不审核
            TransferInfoDTO.AddDTO addDTO = tuple.get(1);
            LocalDate inClosedDate = closedDateMap.get(addDTO.getInOrgId());
            LocalDate outClosedDate = closedDateMap.get(addDTO.getOutOrgId());
            if (null != inClosedDate) {
                if (!billDate.isAfter(inClosedDate)) {
                    return;
                }
            }
            if (null != outClosedDate) {
                if (!billDate.isAfter(outClosedDate)) {
                    return;
                }
            }
            //提交
            transferInfoService.submit(Collections.singletonList(transferOutId));

            //审核
            TransferInfoEntity entity1 = transferInfoService.getById(transferOutId);
            if (Objects.nonNull(entity1)){
                transferInfoService.approve(entity1,ApproveType.PASS,"", null , Boolean.TRUE);
            }
        } else {
            throw new ServiceException("[FBA货件签收]新增直接调拨单失败");
        }
    }

    @Override
    public void export(FbaShipmentDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("FBA货件导出", EXPORT_WMS_FBA_SHIPMENT.getCode(), dto);
    }

    private List<FbaShipmentDTO.ExportDTO> fillReceive(List<FbaShipmentDTO.ExportDTO> exportDTOList) {
        List<FbaShipmentDTO.ExportDTO> result = new ArrayList<>();
        List<String> detailIds = exportDTOList.stream().map(FbaShipmentDTO.ExportDTO::getDetailId).distinct().collect(Collectors.toList());
        Map<String,List<FbaShipmentReceiveEntity>> fbaShipmentReceiveEntitieMap = fbaShipmentReceiveService.listByDetailIds(detailIds).stream().collect(Collectors.groupingBy(FbaShipmentReceiveEntity::getDetailId));
        for(FbaShipmentDTO.ExportDTO exportDTO : exportDTOList){
            List<FbaShipmentReceiveEntity> fbaShipmentReceiveEntityList = fbaShipmentReceiveEntitieMap.get(exportDTO.getDetailId());
            if(CollectionUtils.isEmpty(fbaShipmentReceiveEntityList)){
                exportDTO.setReceiveQty("0");
                result.add(exportDTO);
                continue;
            }
            String receive;
            for (FbaShipmentReceiveEntity fbaShipmentReceiveEntity : fbaShipmentReceiveEntityList) {
                //映射字段
                FbaShipmentDTO.ReceiveRecordView receiveRecordView = FbaShipmentConverter.INSTANCE.fbaShipmentReceiveEntityToView(fbaShipmentReceiveEntity);
//                if(receive == null){
//                    receive = receiveRecordView.toString();
//                }else{
//                    receive = receive + ",\n\r" + receiveRecordView.toString();
//                }
                FbaShipmentDTO.ExportDTO fillDTO = new FbaShipmentDTO.ExportDTO();
                BeanUtil.copyProperties(exportDTO,fillDTO);
                receive = receiveRecordView.toString();
                fillDTO.setReceiveQty(receive);
                result.add(fillDTO);
            }
        }
        return result;
    }

    @Override
    public LocalDate getStopGenReceivedDate(FbaShipmentEntity fbaShipmentEntity) {
        List<DictBasicDTO.ListDTO> stopGenReceivedTimeList = dictBasicService.getByKey(DictBasicEnum.STOP_GEN_RECEIVE_TIME.getKey());
        if (!CollectionUtils.isEmpty(stopGenReceivedTimeList)) {
            DictBasicDTO.ListDTO configDTO = stopGenReceivedTimeList.stream().findFirst().orElse(null);
            LocalDateTime stopTime;
            if (null != configDTO && StringUtils.isNotBlank(configDTO.getValue())) {
                // 配置时间为主
                stopTime = LocalDateTime.parse(configDTO.getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return stopTime.toLocalDate();
            }
        }
        // 按关账日期
        // 查询最新库存关账记录
        Map<String, LocalDate> closedDateMap = inventoryClosedRecordService.mapByOrgId(InventoryClosedRecordEnum.STK.getCode());
        // 最新的关账记录时间
        if (!closedDateMap.isEmpty()){
            // 来源店铺
            ShopInfoEntity shopEntity = shopInfoFeign.getShopInfoById(fbaShipmentEntity.getShopId());
            List<WarehouseEntity> warehouseList = warehouseService.listByIds(Collections.singletonList(shopEntity.getWarehouseId()));
            //仓库列表配置的在途归属仓库，目的仓为FBA第三方仓时，在途仓优先取仓库列表配置，配置为空时默认为“FBA在途仓-xgwj-fba”
            WarehouseEntity warehouseEntity = warehouseList.stream().filter(req -> req.getId().equals(shopEntity.getWarehouseId())).findFirst().orElse(new WarehouseEntity());
            checkOnwayWarehouse(warehouseEntity);
            //查询在途仓
            WarehouseEntity onWayWarehouse = warehouseService.getById(warehouseEntity.getOnwayWarehouseId());
            if (null == onWayWarehouse){
                throw new ServiceException("未找到在途仓：id=" + warehouseEntity.getOnwayWarehouseId());
            }
            LocalDate onWaylocalDate = closedDateMap.get(onWayWarehouse.getOrgId());
            LocalDate mainClosedDate = closedDateMap.get(warehouseEntity.getOrgId());
            if (null != onWaylocalDate && null != mainClosedDate){
                return onWaylocalDate.isAfter(mainClosedDate) ? onWaylocalDate : mainClosedDate;
            } else if (null != onWaylocalDate) {
                return onWaylocalDate;
            } else return mainClosedDate;
        }

        return null;
    }

    @Override
    public PagingVO<FbaShipmentDTO.SearchResultDTO> search(PagingDTO<FbaShipmentDTO.SearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<FbaShipmentDTO.SearchResultDTO> searchData = this.baseMapper.search(query, dto.getParams());
        if (CollUtil.isEmpty(searchData.getRecords())) {
            return new PagingVO(searchData);
        }
        return new PagingVO(searchData);
    }

    @Override
    public FbaShipmentEntity getByCode(String fbaShipmentCode) {
        return lambdaQuery().eq(FbaShipmentEntity::getCode,fbaShipmentCode)
                .last("limit 1")
                .one();
    }

    @Override
    public List<FbaShipmentEntity> listByCodes(List<String> fbaShipmentCodeList) {
        if(CollectionUtils.isEmpty(fbaShipmentCodeList)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaShipmentEntity::getCode,fbaShipmentCodeList)
                .list();
    }

    @Override
    public PagingVO<FbaShipmentDTO.ExportDTO> exportFbaShipment(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto) {
        Page<FbaShipmentDTO.ListDTO> page = baseMapper.export(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        fillList(page.getRecords());
        List<FbaShipmentDTO.ExportDTO> exportDTOList = BeanUtil.copyToList(page.getRecords(),FbaShipmentDTO.ExportDTO.class, CopyOptions.create(FbaShipmentDTO.ExportDTO.class,false,"receiveQty"));
        List<FbaShipmentDTO.ExportDTO> fillDTOList = fillReceive(exportDTOList);
        return new PagingVO<>(fillDTOList, (int) page.getTotal(),dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public boolean updatePackingStatus(String id) {
        if(StringUtils.isBlank(id)){
            return false;
        }
        return lambdaUpdate().eq(FbaShipmentEntity::getId,id).set(FbaShipmentEntity::getIsPackingDownload,true).update();
    }

    @Override
    public PagingVO<FbaShipmentDTO.SearchResultDTO> searchByCodeWithRequisition(PagingDTO<FbaShipmentDTO.SearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        FbaShipmentDTO.SearchDTO searchDTO = dto.getParams();
        if(StringUtils.isBlank(searchDTO.getRequisitionId())){
            throw new ServiceException("要货申请不能为空");
        }
        if(StringUtils.isBlank(searchDTO.getCode())){
            throw new ServiceException("单号不能为空");
        }
        RequisitionApplicationEntity requisitionApplicationEntity = Optional.ofNullable(requisitionApplicationService.getById(searchDTO.getRequisitionId())).orElseThrow(()->new ServiceException("要货申请为空"));
        searchDTO.setShopId(requisitionApplicationEntity.getChannelId());
        IPage<FbaShipmentDTO.SearchResultDTO> searchData = this.baseMapper.search(query, dto.getParams());
        if (CollUtil.isEmpty(searchData.getRecords())) {
            return new PagingVO(searchData);
        }
        return new PagingVO(searchData);
    }

    @Override
    public List<String> requisitionFbaQuickPaste(FbaShipmentDTO.QuickPasteDTO dto) {
        RequisitionApplicationEntity requisitionApplicationEntity = Optional.ofNullable(requisitionApplicationService.getById(dto.getRequisitionId())).orElseThrow(()->new ServiceException("要货申请为空"));
        String shopId = requisitionApplicationEntity.getChannelId();
        List<String> codeList = dto.getCodeList();
        List<FbaShipmentEntity> fbaShipmentEntityList = this.lambdaQuery().eq(FbaShipmentEntity::getShopId,shopId).in(FbaShipmentEntity::getCode,codeList).list();
        List<String> list = new ArrayList<>();
        for (String code : codeList) {
            FbaShipmentEntity fbaShipmentEntity = fbaShipmentEntityList.stream().filter(v->v.getCode().equals(code)).findFirst().orElse(null);
            if(Objects.isNull(fbaShipmentEntity)){
                list.add("");
            }else{
                list.add(code);
            }
        }
        return list;
    }
}
