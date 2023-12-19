package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.PlatformSaveHandler;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * b2c发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryServiceImpl extends SuperServiceImpl<SoB2cDeliveryMapper, SoB2cDeliveryEntity> implements SoB2cDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;
    @Autowired
    private SoB2cFeign soB2cFeign;
    @Autowired
    private LogisticsBillFeign logisticsBillFeign;
    @Autowired
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private LogisticsFeign logisticsFeign;
    @Autowired
    private ShopInfoFeign shopInfoFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(SoB2cDeliveryDTO.AddDTO addDTO) {
        SoB2cDeliveryEntity soB2cDeliveryEntity = new SoB2cDeliveryEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeliveryEntity);

        List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities = BeanMapper.copyList(addDTO.getDetailList(), SoB2cDeliveryDetailEntity.class);

        // 数据处理
        handleData(soB2cDeliveryEntity, soB2cDeliveryDetailEntities);

        log.info("开始新增b2c发货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHDC);
        soB2cDeliveryEntity.setCode(code);
        boolean save = super.save(soB2cDeliveryEntity);
        if(!save) {
            throw new ServiceException("b2c发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单" , soB2cDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "新增操作");
        // 新增明细
        soB2cDeliveryDetailService.add(soB2cDeliveryDetailEntities, soB2cDeliveryEntity.getId());
        return save;
    }

    @Override
    public List<SoB2cDeliveryDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoB2cDeliveryDTO.PagingParamDTO searchParam = new SoB2cDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoB2cDeliveryDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = SoB2cDeliveryStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SoB2cDeliveryDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new SoB2cDeliveryDTO.TabListDTO(status, 0));
            }
        });
        list.add(new SoB2cDeliveryDTO.TabListDTO("all", list.stream().mapToInt(SoB2cDeliveryDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public PagingVO<SoB2cDeliveryDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDeliveryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public SoB2cDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        SoB2cDeliveryEntity deliveryEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2C发货单数据"));
        SoB2cDeliveryDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDeliveryDTO.ViewDTO.class, deliveryEntity);
        //发货单详情
        List<SoB2cDeliveryDetailEntity> detailList = soB2cDeliveryDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, detailList);
        return data;
    }

    @Override
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualDelivery(String id) {

        SoB2cDeliveryEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.b2c_so_delivery_NOT_EXISTS);
        }

        //已发货、取消发货的数据不允许手动发货，其他状态都可以直接变更为已发货
        if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())
        ) {
            throw new ServiceException(ApiError.IS_NOT_MANUAL_DELIVERY);
        }

        //调用第三方平台SDK发货
        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
        platformShipOrderDTO.setSoB2cId(id);
        platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
        PlatformSaveHandler.shipOrder(platformShipOrderDTO);

        //修改发货状态
        this.updateStatus(id, SoB2cDeliveryStatusEnum.SHIPPED.getCode());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动发货");
    }

    @Override
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO falseDelivery(String id) {
        SoB2cDeliveryEntity entity = this.getById(id);
        //虚假发货，已发货，取消发货的数据不允许操作虚假发货
        if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getCode().equals(entity.getStatus())
        ) {
            throw new ServiceException(ApiError.IS_NOT_FALSE_SHIPMENT);
        }

        //调用第三方平台SDK发货
        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
        platformShipOrderDTO.setSoB2cId(id);
        platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
        PlatformSaveHandler.shipOrder(platformShipOrderDTO);

        //修改状态为虚假发货
        this.updateStatus(id, SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "虚假发货");
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids) {

        List<SoB2cDeliveryDetailEntity> deliveryDetailEntityList = soB2cDeliveryDetailService.listByMainIds(ids);

        //查询产品信息
        List<String> skuIds = deliveryDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);

        List<String> childSkuIds = bomChildrenSkuList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        skuIds.addAll(childSkuIds);
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingViewList = new ArrayList<>();
        for (SoB2cDeliveryDetailEntity deliveryDetailEntity : deliveryDetailEntityList) {

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(deliveryDetailEntity.getSkuId()))
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    SoB2cDeliveryDTO.PrintPickingViewDTO viewDTO = new SoB2cDeliveryDTO.PrintPickingViewDTO();
                    BeanMapper.copy(deliveryDetailEntity, viewDTO);
                    viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                        viewDTO.setPickingQty(deliveryDetailEntity.getDeliveryQty());
                    }

                    //匹配sku信息
                    SkuVO skuVO = skuVOList.stream()
                            .filter(req -> req.getSkuId().equals(bomChildrenSkuDTO.getParentSkuId()))
                            .distinct().findFirst().orElse(new SkuVO());
                    viewDTO.setProductName(skuVO.getSkuName());
                    viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocation()) ? "" : skuVO.getWarehouseLocation());
                    printPickingViewList.add(viewDTO);
                }
            } else {
                SoB2cDeliveryDTO.PrintPickingViewDTO viewDTO = new SoB2cDeliveryDTO.PrintPickingViewDTO();
                BeanMapper.copy(deliveryDetailEntity, viewDTO);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream()
                        .filter(req -> req.getSkuId().equals(deliveryDetailEntity.getSkuId()))
                        .distinct().findFirst().orElse(new SkuVO());
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocation()) ? "" : skuVO.getWarehouseLocation());
                if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                    viewDTO.setPickingQty(deliveryDetailEntity.getDeliveryQty());
                }
                printPickingViewList.add(viewDTO);
            }
        }

        List<SoB2cDeliveryDTO.PrintPickingViewDTO> resultList = printPickingViewList.stream()
                .sorted(Comparator.comparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getSkuNo).reversed()
                        .thenComparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getWarehouseName).reversed()
                        .thenComparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getWarehouseLocation).reversed()
                ).collect(Collectors.toList());

        return resultList;
    }


    @Override
    public Boolean printPicking(List<String> ids) {
        lambdaUpdate().set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.PICKING.getCode()).update();
        //修改打印状态
        return lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintPicking, Boolean.TRUE).update();
    }

    @Override
    public Boolean printPickingCancel(List<String> ids) {
        //修改打印状态
        return lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintPicking, Boolean.FALSE).update();
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillView(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);
        //查询物流商信息
        List<String> logisticsChannelIds = soB2cDeliveryEntities.stream().map(req -> req.getLogisticsChannelId()).collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> list = new ArrayList<>();
        for (String logisticsChannelId : logisticsChannelIds) {
            SoB2cDeliveryDTO.PrintLogisticsWaybillDTO waybillDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDTO();
            //打印类型：物流面单
            waybillDTO.setPrintType(SoB2cDeliveryPrintTypeEnum.LOGISTICS_WAYBILL.getCode());
            //渠道信息
            waybillDTO.setLogisticsChannelId(logisticsChannelId);
            List<SoB2cDeliveryEntity> collect = soB2cDeliveryEntities.stream().filter(req -> req.getLogisticsChannelId().equals(logisticsChannelId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                waybillDTO.setLogisticsChannelName(collect.get(MathUtil.ZERO).getLogisticsChannelName());
            }
            //有运单号数量
            Integer isTransportNoNum = Math.toIntExact(collect.stream().filter(req -> StringUtils.isNotBlank(req.getTransportNo())).count());
            waybillDTO.setIsTransportNoNum(isTransportNoNum);
            //无运单号数量
            Integer notTransportNoNum = Math.toIntExact(collect.stream().filter(req -> StringUtils.isBlank(req.getTransportNo())).count());
            waybillDTO.setNotTransportNoNum(notTransportNoNum);

            //详情
            List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> detailList = new ArrayList<>();
            for (SoB2cDeliveryEntity deliveryEntity : collect) {
                SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO waybillDetailDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO();
                waybillDetailDTO.setSoB2cId(deliveryEntity.getSourceId());
                waybillDetailDTO.setSoCode(deliveryEntity.getSoCode());
                waybillDetailDTO.setLogisticsChannelId(deliveryEntity.getLogisticsChannelId());
                waybillDetailDTO.setLogisticsChannelName(deliveryEntity.getLogisticsChannelName());
                waybillDetailDTO.setTransportNo(deliveryEntity.getTransportNo());
                //匹配物流商名称
                LogisticsChannelDTO.BaseDTO baseDTO = channelInfoList.stream().filter(req -> req.getId().equals(logisticsChannelId)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(baseDTO)) {
                    waybillDetailDTO.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
                }
                detailList.add(waybillDetailDTO);
            }
            waybillDTO.setDetailList(detailList);
            list.add(waybillDTO);
        }
        return list;
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintDistributionDTO> printDistribution(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);
        //查询物流商信息
        List<String> logisticsChannelIds = soB2cDeliveryEntities.stream().map(req -> req.getLogisticsChannelId()).collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
        List<SoB2cDeliveryDTO.PrintDistributionDTO> list = new ArrayList<>();
        for (String logisticsChannelId : logisticsChannelIds) {
            SoB2cDeliveryDTO.PrintDistributionDTO waybillDTO = new SoB2cDeliveryDTO.PrintDistributionDTO();
            //打印类型：物流面单
            waybillDTO.setPrintType(SoB2cDeliveryPrintTypeEnum.LOGISTICS_WAYBILL.getCode());
            //渠道信息
            waybillDTO.setLogisticsChannelId(logisticsChannelId);
            List<SoB2cDeliveryEntity> collect = soB2cDeliveryEntities.stream().filter(req -> req.getLogisticsChannelId().equals(logisticsChannelId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                waybillDTO.setLogisticsChannelName(collect.get(MathUtil.ZERO).getLogisticsChannelName());
            }
            //有运单号数量
            Integer isTransportNoNum = Math.toIntExact(collect.stream().filter(req -> StringUtils.isNotBlank(req.getTransportNo())).count());
            waybillDTO.setIsTransportNoNum(isTransportNoNum);
            //无运单号数量
            Integer notTransportNoNum = Math.toIntExact(collect.stream().filter(req -> StringUtils.isBlank(req.getTransportNo())).count());
            waybillDTO.setNotTransportNoNum(notTransportNoNum);

            //详情
            List<SoB2cDeliveryDTO.PrintDistributionDetailDTO> detailList = new ArrayList<>();
            for (SoB2cDeliveryEntity deliveryEntity : collect) {
                SoB2cDeliveryDTO.PrintDistributionDetailDTO distributionDetailDTO = new SoB2cDeliveryDTO.PrintDistributionDetailDTO();
                distributionDetailDTO.setSoCode(deliveryEntity.getSoCode());
                distributionDetailDTO.setLogisticsChannelId(deliveryEntity.getLogisticsChannelId());
                distributionDetailDTO.setLogisticsChannelName(deliveryEntity.getLogisticsChannelName());
                distributionDetailDTO.setTransportNo(deliveryEntity.getTransportNo());
                //匹配物流商名称
                LogisticsChannelDTO.BaseDTO baseDTO = channelInfoList.stream().filter(req -> req.getId().equals(logisticsChannelId)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(baseDTO)) {
                    distributionDetailDTO.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
                }
                detailList.add(distributionDetailDTO);
            }
            waybillDTO.setDetailList(detailList);
            list.add(waybillDTO);
        }
        return list;
    }

    @Override
    public SoB2cDeliveryEntity getByBusinessCode(String businessCode) {
        return this.getOne(new LambdaQueryWrapper<>(SoB2cDeliveryEntity.class)
                .eq(SoB2cDeliveryEntity::getSoCode, businessCode)
                .or()
                .eq(SoB2cDeliveryEntity::getTransportNo, businessCode)
        );
    }

    @Override
    public void printLogisticsBillConfirm(SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto) {
        //打印类型
        String printType = dto.getPrintType();

        //明细信息
        List<SoB2cDeliveryDTO.LogisticsChannelDTO> detailList = dto.getDetailList();

        //循环打印的渠道
        for (SoB2cDeliveryDTO.LogisticsChannelDTO logisticsChannelDTO : detailList) {

            //查询b2c订单信息
            List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> waybillDetailDTOList = logisticsChannelDTO.getDetailList();
            List<String> soIds = waybillDetailDTOList.stream().map(req -> req.getSoB2cId()).distinct().collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);

            //渠道包含的单据
            for (SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO logisticsWaybillDetailDTO : waybillDetailDTOList) {
                //匹配订单
                SoB2cEntity soB2cEntity = soB2cEntities.stream()
                        .filter(req -> req.getId().equals(logisticsWaybillDetailDTO.getSoB2cId()))
                        .findFirst().orElse(new SoB2cEntity());

                //如果打印面单
                if (SoB2cDeliveryPrintTypeEnum.LOGISTICS_WAYBILL.getCode().equals(printType)) {
                    //先获取订单的面单，没有就请求sdk获取
                    if (StringUtils.isBlank(soB2cEntity.getLogisticsWaybill())) {

                    }
                } else if (SoB2cDeliveryPrintTypeEnum.DISTRIBUTION.getCode().equals(printType)) {
                    //如果打印配货单，先获取订单的配货单，没有就请求sdk获取
                    if (StringUtils.isBlank(soB2cEntity.getDistributeWaybill())) {

                    }
                    // TODO 配货单需要根据渠道查询是否是自定义配置
                } else {

                }



            }
        }


    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> detailEntityList) {
        int deliveryQtySum = detailEntityList.stream().mapToInt(req -> req.getDeliveryQty()).sum();
        // 单品单数：SKU1个，数量1个
        if (detailEntityList.size() == 1 && deliveryQtySum == 1) {
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.SINGLE_ITEM_SINGLE.getCode());
        } else if (detailEntityList.size() == 1 && deliveryQtySum > 1) {
            // 单品多数：SKU1个，数量大于1
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.SINGLE_ITEM_MULTI.getCode());
        } else {
            // 多品多数：SKU大于1个
            soB2cDeliveryEntity.setPickingType(PickingTypeEnum.MULTI_ITEM_MULTI.getCode());
        }

        //店铺信息
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(soB2cDeliveryEntity.getShopId());
        if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
            soB2cDeliveryEntity.setShopName(shopInfoEntity.getName());
        }

        //查询B2C销售订单
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        //订单信息
        SoB2cEntity soB2cEntity = soB2cEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setDictPlatform(soB2cEntity.getDictPlatform());
        soB2cDeliveryEntity.setShopId(soB2cEntity.getShopId());
        soB2cDeliveryEntity.setShopName(soB2cEntity.getShopName());

        //查询B2C销售订单物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        soB2cDeliveryEntity.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        //根据物流跟踪单号查询物流单详情
        LogisticsBillDTO.BaseDTO logisticsBillByTrackNo = logisticsBillFeign.getLogisticsBillByTrackNo(soB2cLogisticsEntity.getCode());
        soB2cDeliveryEntity.setTransportNo(logisticsBillByTrackNo.getTransportNo());


    }

    /**
     * 分页查询字段处理
     * @param records
     */
    private void fillList(List<SoB2cDeliveryDTO.ListDTO> records) {
        List<String> skuIds = records.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIds);
        for (SoB2cDeliveryDTO.ListDTO record : records) {
            //拦截标识
            Boolean isIntercept = soB2cDeliveryInterceptService.getIsIntercept(Arrays.asList(record.getSourceId()));
            record.setIsIntercept(isIntercept);
            //平台名称
            record.setDictPlatformName(PlatformDictEnum.getByCode(record.getDictPlatform()).getName());
            //状态中文
            record.setStatusName(SoB2cDeliveryStatusEnum.getName(record.getStatus()));
            //拣货类型中文
            record.setPickingTypeName(PickingTypeEnum.getName(record.getPickingType()));
            //产品信息
            ProductDetailEntity entity = productDetailEntityList.stream().filter(req -> req.getId().equals(record.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                record.setSkuNo(entity.getSkuNo());
                record.setProductName(entity.getName());
            }

        }
    }

    private void fillOne(SoB2cDeliveryDTO.ViewDTO data, List<SoB2cDeliveryDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);

        //单据状态中文
        data.setStatusName(SoB2cDeliveryStatusEnum.getName(data.getStatus()));
        //拣货类型中文
        data.setPickingTypeName(PickingTypeEnum.getName(data.getPickingType()));
        //详情字段设置
        List<SoB2cDeliveryDetailDTO.ViewDTO> viewDetailList = BeanMapper.copyList(detailList, SoB2cDeliveryDetailDTO.ViewDTO.class);

        for (SoB2cDeliveryDetailDTO.ViewDTO viewDTO : viewDetailList) {
            //产品信息
            ProductDetailEntity entity = productDetailEntityList.stream().filter(req -> req.getId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                viewDTO.setProductName(entity.getName());
            }
        }
        data.setDetailList(viewDetailList);
    }


    /**
     * 修改单据状态
     * @Author Luo_WG
     * @Date 2023/12/15 11:28
     * @param id 主键id
     * @param status 状态编码
     * @return java.lang.Boolean
     **/
    private Boolean updateStatus(String id, String status) {
        return lambdaUpdate().set(SoB2cDeliveryEntity::getStatus, status).eq(SoB2cDeliveryEntity::getId, id).update();
    }

}
