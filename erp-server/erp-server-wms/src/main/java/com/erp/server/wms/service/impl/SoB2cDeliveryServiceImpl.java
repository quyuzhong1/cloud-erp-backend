package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.PrintWayBillPdfDetailDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.handler.PlatformSaveHandler;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.enums.LogisticsLabelTypeEnum;
import com.erp.model.tms.enums.LogisticsPrintTypeEnum;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.DeliverTypeEnum;
import com.erp.model.wms.enums.PickingTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryPrintTypeEnum;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private FileTemplateFeign fileTemplateFeign;
    @Autowired
    private InventoryTransCoreService inventoryTransCoreService;
    @Autowired
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;


    @Resource
    private SoOutstockService soOutstockService;


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
        if (!save) {
            throw new ServiceException("b2c发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单", soB2cDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "新增操作");
        // 新增明细
        soB2cDeliveryDetailService.add(soB2cDeliveryDetailEntities, soB2cDeliveryEntity.getId());

        //冻结库存
        freezeInventory(soB2cDeliveryEntity, soB2cDeliveryDetailEntities);
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
            if (!existStatusList.contains(status)) {
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
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public SoB2cDeliveryDTO.ViewDTO view(String id) {
        //发货单主信息
        SoB2cDeliveryEntity deliveryEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C发货单数据"));
        SoB2cDeliveryDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDeliveryDTO.ViewDTO.class, deliveryEntity);
        //发货单详情
        List<SoB2cDeliveryDetailEntity> detailList = soB2cDeliveryDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, detailList);
        return data;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 180000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO manualDelivery(String id) {
        SoB2cDeliveryEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.B2C_SO_DELIVERY_NOT_EXISTS);
        }

        //查询是否冻结
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        if (Objects.nonNull(soB2cEntity) && soB2cEntity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
        }

        //已发货、取消发货的数据不允许手动发货，其他状态都可以直接变更为已发货
        if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())
        ) {
            throw new ServiceException(ApiError.IS_NOT_MANUAL_DELIVERY);
        }
        if (Objects.nonNull(soB2cEntity)) {
            String transferStatus = soB2cEntity.getTransferStatus();
            //表示要中转啊
            if(!TransferStatusEnum.NOT.getCode().equals(transferStatus)){
                TransferDeclareDetailEntity transferDeclareDetailEntity = transferDeclareFeign.getBySoId(soB2cEntity.getId());
                if (Objects.nonNull(transferDeclareDetailEntity)) {
                    String uploadSuccess= TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode();
                    String uploadStatus = transferDeclareDetailEntity.getOrderUploadStatus();
                    if (!uploadSuccess.equals(uploadStatus)) {
                        throw new ServiceException(ApiError.NOT_TRANSFER_DECLARE);
                    }
                }else{
                    throw new ServiceException(ApiError.NOT_TRANSFER_DECLARE);
                }
            }

        }


        //如果是虚假发货不用再次调用第三方SDK标记发货，因为虚假发货已经调用过了
        if (!SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getCode().equals(entity.getStatus())) {
            if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
                //调用第三方平台SDK发货
                PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
                platformShipOrderDTO.setSoB2cId(entity.getSourceId());
                platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
                try {
                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
                } catch (Exception e) {
                    throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform());
                }
            }
        }
        //修改发货状态
        lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .set(SoB2cDeliveryEntity::getDeliveryTime, LocalDateTime.now())
                .eq(SoB2cDeliveryEntity::getId, id).update();

        //修改订单状态待发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(LocalDateTime.now());
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

        // 操作日志
        String msg = StrUtil.format("用户【{}】手动发货单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "手动发货");

        //推送到DMP
        this.syncDeliveryToDmp(entity.getId());
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

        //查询是否冻结
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        if (soB2cEntity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
        }

        //调用第三方平台SDK发货
        try {
            if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
                //调用第三方平台SDK发货
                PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
                platformShipOrderDTO.setSoB2cId(entity.getSourceId());
                platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
                try {
                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
                } catch (Exception e) {
                    throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform());
                }
            }
        } catch (Exception e) {
            log.error("销售单【{}】 标记发货失败 >>>错误信息{}", e.getMessage());
            throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform());
        }
        //修改状态为虚假发货
        this.updateStatus(id, SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getStatus());
        //修改订单状态待发货
        soB2cFeign.updateSoB2cStatus(Arrays.asList(entity.getSourceId()), SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        // 操作日志
        String msg = StrUtil.format("用户【{}】虚假发货单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "虚假发货");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "虚假发货");
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids) {
        List<SoB2cDeliveryDetailEntity> deliveryDetailEntityList = soB2cDeliveryDetailService.listByMainIds(ids);

        //已发货和取消发货单 状态，不允许在打印拣货单
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listByIds(ids);
        List<String> codeList = deliveryEntityList.stream()
                .filter(req -> SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus())
                        || SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(req.getStatus()))
                .map(req -> req.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codeList)) {
            throw new ServiceException(ApiError.STATUS_NOT_PRINT_PICKING, StrUtil.join(",", codeList));
        }

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
                    .filter(req -> req.getParentSkuId().equals(deliveryDetailEntity.getSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());

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

                    //备注
                    SoB2cDeliveryEntity entity = deliveryEntityList.stream().filter(req -> req.getId().equals(deliveryDetailEntity.getMainId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(entity)) {
                        viewDTO.setRemark(entity.getRemark());
                    }
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

                //备注
                SoB2cDeliveryEntity entity = deliveryEntityList.stream().filter(req -> req.getId().equals(deliveryDetailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(entity)) {
                    viewDTO.setRemark(entity.getRemark());
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
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);

        //查询是否冻结
        List<String> soIds = soB2cDeliveryEntities.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.PICKING.getCode())
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.FALSE_SHIPMENT.getCode())
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                .in(SoB2cDeliveryEntity::getId, ids).update();

        List<Pair<String, String>> addPairList = soB2cDeliveryEntities.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("打印了一张拣货单【%s】", ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), addPairList, "打印拣货单");

        //修改打印状态
        return lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintPicking, Boolean.TRUE).in(SoB2cDeliveryEntity::getId, ids).update();
    }

    @Override
    public Boolean printPickingCancel(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);

        //查询是否冻结
        List<String> soIds = soB2cDeliveryEntities.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        //修改打印状态
        return lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintPicking, Boolean.FALSE).in(SoB2cDeliveryEntity::getId, ids).update();
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillView(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);

        //已发货和取消发货单 状态，不允许在打印标签
        List<String> codeList = soB2cDeliveryEntities.stream()
                .filter(req -> SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus())
                        || SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(req.getStatus()))
                .map(req -> req.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codeList)) {
            throw new ServiceException(ApiError.STATUS_NOT_PRINT_LABEL, StrUtil.join(",", codeList));
        }

        //查询是否冻结
        List<String> soIds = soB2cDeliveryEntities.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        //查询物流商信息
        List<String> logisticsChannelIds = soB2cDeliveryEntities.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> list = new ArrayList<>();

        //查询打印类型
        List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsBillFeign.listPrintTypeByChannelIds(logisticsChannelIds);


        for (String logisticsChannelId : logisticsChannelIds) {

            SoB2cDeliveryDTO.PrintLogisticsWaybillDTO waybillDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDTO();
            //打印类型：物流面单
            waybillDTO.setPrintType(SoB2cDeliveryPrintTypeEnum.LOGISTICS_BILL.getCode());
            //渠道信息
            waybillDTO.setLogisticsChannelId(logisticsChannelId);

            // 配货单需要根据渠道查询是否是自定义配置，自定义配置需要组装数据
            LogisticsPrintTypeDTO.ViewDTO logisticsPrintTypeEntity = logisticsPrintTypeEntities.stream()
                    .filter(req -> LogisticsPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(req.getPrintType())
                    ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsPrintTypeEntity)) {
                waybillDTO.setPrintDeliveryType(logisticsPrintTypeEntity.getLabelType());
            }

            //查询是否允许打印面单和配货单
            LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (ObjectUtil.isEmpty(authDTO)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
            }

            LogisticsPlatformEnum logisticsPlatformEnum = LogisticsPlatformEnum.getByCode(authDTO.getLogisticsPlatform());
            waybillDTO.setPrintLabel(logisticsPlatformEnum.getPrintLabel().equals("Y") ? Boolean.TRUE : Boolean.FALSE);
            waybillDTO.setPrintDelivery(logisticsPlatformEnum.getPrintDelivery().equals("Y") ? Boolean.TRUE : Boolean.FALSE);

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
                waybillDetailDTO.setId(deliveryEntity.getId());
                waybillDetailDTO.setSoB2cId(deliveryEntity.getSourceId());
                waybillDetailDTO.setSoCode(deliveryEntity.getSoCode());
                waybillDetailDTO.setLogisticsChannelId(deliveryEntity.getLogisticsChannelId());
                waybillDetailDTO.setLogisticsChannelName(deliveryEntity.getLogisticsChannelName());
                waybillDetailDTO.setTransportNo(deliveryEntity.getTransportNo());
                waybillDetailDTO.setShopId(deliveryEntity.getShopId());
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
    public SoB2cDeliveryEntity getByBusinessCode(String businessCode) {
        return this.getOne(new LambdaQueryWrapper<>(SoB2cDeliveryEntity.class)
                .eq(SoB2cDeliveryEntity::getSoCode, businessCode)
                .or()
                .eq(SoB2cDeliveryEntity::getTransportNo, businessCode)
        );
    }

    @Override
    public void printLogisticsBillConfirm(SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto, HttpServletResponse response) {
        //打印类型
        String printType = dto.getPrintType();

        //明细信息
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> detailList = dto.getDetailList();
        List<String> logisticsChannelIdList = detailList.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());

        List<String> base64List = new ArrayList<>();

        //查询打印类型
        List<String> channelIds = detailList.stream().map(req -> req.getLogisticsChannelId()).collect(Collectors.toList());
        List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsBillFeign.listPrintTypeByChannelIds(channelIds);

        //循环打印的渠道
        for (String logisticsChannel : logisticsChannelIdList) {

            //查询b2c订单信息
            List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> waybillDetailDTOList = detailList.stream().filter(req -> req.getLogisticsChannelId().equals(logisticsChannel)).collect(Collectors.toList());

            //匹配订单字段，用于打印
            List<String> soIds = waybillDetailDTOList.stream().map(req -> req.getSoB2cId()).distinct().collect(Collectors.toList());
            List<PrintWayBillPdfDTO> printWayBillPdfResultList = soB2cFeign.printWayBillPdf(soIds);


            List<SoB2cDTO.WaybillDTO> platformWaybill = new ArrayList<>();

            // 配货单需要根据渠道查询是否是自定义配置
            LogisticsPrintTypeDTO.ViewDTO logisticsPrintTypeEntity = logisticsPrintTypeEntities.stream()
                    .filter(req -> LogisticsPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(req.getPrintType())
                            && LogisticsLabelTypeEnum.CUSTOM.getCode().equals(req.getLabelType())
                    ).findFirst().orElse(null);
            if (!SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(printType)) {

                //获取SDK物流商商面单
                platformWaybill = this.getPlatformWaybill(waybillDetailDTOList);
            }

            //渠道包含的单据
            for (SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO logisticsWaybillDetailDTO : waybillDetailDTOList) {

                //匹配订单
                PrintWayBillPdfDTO printWayBillPdf = printWayBillPdfResultList.stream()
                        .filter(req -> logisticsWaybillDetailDTO.getSoB2cId().equals(req.getSoId())).findFirst()
                        .orElse(new PrintWayBillPdfDTO());

                //如果打印面单
                if (SoB2cDeliveryPrintTypeEnum.LOGISTICS_BILL.getCode().equals(printType)) {
                    //先获取订单的面单，没有就请求sdk获取
                    List<String> logisticsWaybillList = platformWaybill.stream().filter(req -> req.getSoB2cId().equals(printWayBillPdf.getSoId())).map(req -> req.getLogisticsBase64()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(logisticsWaybillList)) {
                        base64List.addAll(logisticsWaybillList);
                    }
                } else if (SoB2cDeliveryPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(printType)) {
                    // 配货单需要根据渠道查询是否是自定义配置，自定义配置需要组装数据
                    if (ObjectUtil.isNotEmpty(logisticsPrintTypeEntity)) {

                        PrintWayBillPdfDTO printWayBillPdfDTO = printWayBillPdfHandle(printWayBillPdf, logisticsWaybillDetailDTO);
                        // 自定义配货单
                        customDistribute(base64List, printWayBillPdfDTO);
                    }
                } else {
                    //先获取订单的面单，没有就请求sdk获取
                    List<String> logisticsWaybillList = platformWaybill.stream().filter(req -> req.getSoB2cId().equals(printWayBillPdf.getSoId())).map(req -> req.getLogisticsBase64()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(logisticsWaybillList)) {
                        base64List.addAll(logisticsWaybillList);
                    }

                    // 配货单需要根据渠道查询是否是自定义配置，自定义配置需要组装数据
                    if (ObjectUtil.isNotEmpty(logisticsPrintTypeEntity)) {
                        PrintWayBillPdfDTO printWayBillPdfDTO = printWayBillPdfHandle(printWayBillPdf, logisticsWaybillDetailDTO);
                        // 自定义配货单
                        customDistribute(base64List, printWayBillPdfDTO);
                    }
                }

                List<SoB2cDeliveryEntity> deliveryEntityList = this.listBySourceIds(Arrays.asList(logisticsWaybillDetailDTO.getSoB2cId()));
                SoB2cDeliveryEntity entity = deliveryEntityList.stream().filter(req -> !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(entity)) {
                    // 操作日志
                    String msg = StrUtil.format("打印了订单编号为【{}】的面单/配货单", commonService.getUserInfo().getUserName(), logisticsWaybillDetailDTO.getSoCode());
                    operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "打印面单/配货单");
                }
            }
        }

        try {
            String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);

            // 设置响应头，告诉浏览器返回的是一个 PDF 文件
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
            BASE64Decoder decoder = new BASE64Decoder();
            try (OutputStream out = response.getOutputStream()) {
                // 将 Base64 编码的字符串解码为字节数组
                byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                // 将字节数组写入到响应输出流中
                out.write(pdfBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_PDF_MERGE);
        }
    }

    private void customDistribute(List<String> base64List, PrintWayBillPdfDTO printWayBillPdfDTO) {
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.DISTRIBUTE_WAYBILL);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileTemplateEntity.getUrl());
        if (inputStream == null) {
            log.info("获取fastdfs文件为空==========》地址：" + fileTemplateEntity.getUrl());
            return;
        }
        Map<String, Object> map = BeanUtil.beanToMap(printWayBillPdfDTO);
        JRBeanCollectionDataSource detail = new JRBeanCollectionDataSource(printWayBillPdfDTO.getDetailList());
        map.put("detail", detail);
//        JasperHelperUtil.export(FileTypeEnum.PDF.getCode(), "pfd", inputStream, map, printWayBillPdfDTO.getDetailList());

        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map, Arrays.asList(printWayBillPdfDTO));
        String base = Base64.getEncoder().encodeToString(bytes);
        base64List.add("data:application/pdf;base64," + base);
    }

    @Override
    public List<SoB2cDeliveryEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cDeliveryEntity::getSourceId, sourceIds).list();
    }

    @Override
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> retryFalseDelivery(String soB2cId) {
        List<BatchResultDTO> resultList = new ArrayList<>(1);
        List<SoB2cDeliveryEntity> soB2cDeliveryList = this.listBySoB2cId(soB2cId);
        String errorType = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
        SoB2cErrorEntity soB2cError = soB2cFeign.getB2cError(soB2cId, errorType);
        String shippedCode = SoB2cDeliveryStatusEnum.SHIPPED.getCode();
        if (Objects.nonNull(soB2cError)) {
            String type = soB2cError.getParamJson();
            for (SoB2cDeliveryEntity item : soB2cDeliveryList) {
                String status = item.getStatus();
                //表示已发货
                if (shippedCode.equals(status)) {
                   resultList.add(BatchResultDTO.success(item.getId(), item.getCode(), "手动发货"));
                }else{
                    BatchResultDTO resultDTO = this.delivery(item.getId(),type);
                    resultList.add(resultDTO);
                }

            }
        }

        return resultList;
    }

    @Override
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public void rollbackInventory(List<String> ids) {
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listByIds(ids);

        //查询是否冻结
        List<String> soIds = deliveryEntityList.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        //修改状态为取消发货
        this.updateStatus(ids, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());

        //回滚库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_B2C_DELIVERY, ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //新增日志
        List<Pair<String, String>> addPairList = deliveryEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货单【%s】取消发货", ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), addPairList, "取消发货");
    }

    @Override
    public Boolean updateStatus(List<String> ids, String status) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, status)
                .in(SoB2cDeliveryEntity::getId, ids)
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                .update();
    }

    /**
     * 发货
     *
     * @param id
     * @param deliveryType
     * @return
     */
    @Override
    public BatchResultDTO delivery(String id, String deliveryType) {
        //手工发货
        String manual = DeliverTypeEnum.MANUAL.getCode();
        if (manual.equals(deliveryType)) {
            return this.manualDelivery(id);
        } else {
            return this.falseDelivery(id);
        }

    }

    @Override
    public void generateB2cSoOutstock(SoB2cDeliveryEntity entity) {
        SoOutstockDTO.GenerateB2cDTO generateB2cDTO = soB2cFeign.getSoOutstockInfoById(entity.getSourceId());
        generateB2cDTO.setSourceId(entity.getId());
        generateB2cDTO.setSourceCode(entity.getCode());
        generateB2cDTO.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
        List<SoB2cDeliveryDetailEntity> deliveryDetailList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        List<SoOutstockDetailDTO.AddDTO> detailList = generateB2cDTO.getDetailList();
        for (SoOutstockDetailDTO.AddDTO item : detailList) {
            String soDetailId = item.getSoDetailId();
            String sourceDetailId = deliveryDetailList.stream().filter(d -> d.getSourceDetailId().equals(soDetailId)).
                    map(SoB2cDeliveryDetailEntity::getId).findFirst().orElse("");
            item.setSourceDetailId(sourceDetailId);
        }
        soOutstockService.generateB2cSoOutstock(generateB2cDTO);

    }

    private List<SoB2cDeliveryEntity> listBySoB2cId(String soB2cId) {
        return this.lambdaQuery().eq(SoB2cDeliveryEntity::getSourceId, soB2cId).list();
    }

    /**
     * 组装数据调用第三方接口打印面单/配货单,获取base64PDF信息
     *
     * @param waybillDetailDTOList 物流信息
     * @return java.util.List<com.erp.model.oms.dto.SoB2cDTO.WaybillDTO>
     * @Author Luo_WG
     * @Date 2023/12/20 15:00
     **/
    private List<SoB2cDTO.WaybillDTO> getPlatformWaybill(List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> waybillDetailDTOList) {
        List<LogisticsBillDTO.PrintLogisticsWaybillDTO> logisticsWaybillDTOList = new ArrayList<>();
//        List<SoB2cEntity> soB2cEntityList = soB2cEntities.stream()
//                .filter(req -> StringUtils.isBlank(req.getLogisticsWaybill()))
//                .collect(Collectors.toList());
        for (SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO detailDTO : waybillDetailDTOList) {
            LogisticsBillDTO.PrintLogisticsWaybillDTO printLogisticsWaybill = new LogisticsBillDTO.PrintLogisticsWaybillDTO();
            printLogisticsWaybill.setChannelId(detailDTO.getLogisticsChannelId());
            printLogisticsWaybill.setB2cSoId(detailDTO.getSoB2cId());
            printLogisticsWaybill.setDeliveryNo(detailDTO.getSoCode());
            printLogisticsWaybill.setShopId(detailDTO.getShopId());

            SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO logisticsWaybillDetailDTO = waybillDetailDTOList.stream().filter(req -> req.getSoB2cId().equals(detailDTO.getSoB2cId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(logisticsWaybillDetailDTO)) {
                printLogisticsWaybill.setTransportNo(logisticsWaybillDetailDTO.getTransportNo());
            }
            logisticsWaybillDTOList.add(printLogisticsWaybill);
        }
        return logisticsBillFeign.printLogisticsWaybill(logisticsWaybillDTOList);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> detailEntityList) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listBySourceIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        List<SoB2cDeliveryEntity> deliveryEntityList = soB2cDeliveryEntities.stream()
                .filter(req -> !SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus().equals(req.getStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deliveryEntityList)) {
            throw new ServiceException(ApiError.NOT_ADD_SO_B2C_DELIVERY, deliveryEntityList.get(0).getSoCode());
        }

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

        //查询B2C销售订单
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }

        //订单信息
        SoB2cEntity soB2cEntity = soB2cEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setDictPlatform(soB2cEntity.getDictPlatform());
        String shopId = soB2cEntity.getShopId();
        if (StringUtils.isNotBlank(shopId)) {
            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
            if (Objects.nonNull(shopInfo)) {
                soB2cDeliveryEntity.setShopName(shopInfo.getName());
            }
        }
        soB2cDeliveryEntity.setShopId(soB2cEntity.getShopId());

        //查询B2C销售订单物流信息
/*        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        if (StringUtils.isBlank(soB2cLogisticsEntities.get(MathUtil.ZERO).getLogisticsChannelId())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_ID_NOT_NULL);
        }
        //物流信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntities.get(MathUtil.ZERO);
        soB2cDeliveryEntity.setLogisticsChannelId(soB2cLogisticsEntity.getLogisticsChannelId());
        soB2cDeliveryEntity.setLogisticsChannelName(soB2cLogisticsEntity.getLogisticsChannelName());
        String  transportNo= soB2cLogisticsEntity.getCode();
        soB2cDeliveryEntity.setTransportNo(transportNo);*/
    }

    /**
     * 分页查询字段处理
     *
     * @param records
     */
    private void fillList(List<SoB2cDeliveryDTO.ListDTO> records) {
        List<String> skuIds = records.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);

        //查询订单
        List<String> soIds = records.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);

        for (SoB2cDeliveryDTO.ListDTO record : records) {
            //拦截标识
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(record.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                record.setIsIntercept(soB2cEntity.getIsIntercept());
                record.setPackageStatus(soB2cEntity.getPackageStatus());
                record.setTransferStatus(soB2cEntity.getTransferStatus());
            }
            //平台名称
            record.setDictPlatformName(PlatformDictEnum.getByCode(record.getDictPlatform()).getName());
            //状态中文
            record.setStatusName(SoB2cDeliveryStatusEnum.getName(record.getStatus()));
            //拣货类型中文
            record.setPickingTypeName(PickingTypeEnum.getName(record.getPickingType()));
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(record.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                record.setSkuNo(skuVO.getSkuNo());
                record.setProductName(skuVO.getSkuName());
                record.setWarehouseLocation(skuVO.getWarehouseLocation());
            }
        }
    }

    private void fillOne(SoB2cDeliveryDTO.ViewDTO data, List<SoB2cDeliveryDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);

        //单据状态中文
        data.setStatusName(SoB2cDeliveryStatusEnum.getName(data.getStatus()));
        //拣货类型中文
        data.setPickingTypeName(PickingTypeEnum.getName(data.getPickingType()));
        //详情字段设置
        List<SoB2cDeliveryDetailDTO.ViewDTO> viewDetailList = BeanMapper.copyList(detailList, SoB2cDeliveryDetailDTO.ViewDTO.class);

        for (SoB2cDeliveryDetailDTO.ViewDTO viewDTO : viewDetailList) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
                viewDTO.setWarehouseLocation(skuVO.getWarehouseLocation());
            }
        }
        data.setDetailList(viewDetailList);
    }


    /**
     * 修改单据状态
     *
     * @param id     主键id
     * @param status 状态编码
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/15 11:28
     **/
    private Boolean updateStatus(String id, String status) {
        return lambdaUpdate().set(SoB2cDeliveryEntity::getStatus, status).eq(SoB2cDeliveryEntity::getId, id).update();
    }


    /**
     * 组装打印配货单数据
     *
     * @param logisticsWaybillDetailDTO
     */
    private PrintWayBillPdfDTO printWayBillPdfHandle(PrintWayBillPdfDTO printWayBillPdf,
                                                     SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO logisticsWaybillDetailDTO) {
        List<PrintWayBillPdfDetailDTO> wayBillDetailList = new ArrayList<>();

        //详情信息
        List<SoB2cDeliveryDetailEntity> deliveryDetailEntityList = soB2cDeliveryDetailService.listByMainIds(Arrays.asList(logisticsWaybillDetailDTO.getId()));

        //查询产品信息
        List<String> skuNoList = deliveryDetailEntityList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuNoList);

        //商品种类个数
        printWayBillPdf.setSkuTotal(deliveryDetailEntityList.size());

        //商品件数
        int qtySum = deliveryDetailEntityList.stream().mapToInt(req -> req.getDeliveryQty()).sum();
        printWayBillPdf.setQtySum(qtySum);
        for (SoB2cDeliveryDetailEntity deliveryDetailEntity : deliveryDetailEntityList) {
            PrintWayBillPdfDetailDTO detailDTO = new PrintWayBillPdfDetailDTO();
            detailDTO.setQty(deliveryDetailEntity.getDeliveryQty());
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(deliveryDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                detailDTO.setSkuImagesUrl(skuVO.getSkuImagesUrl());
                if (skuVO.getVariantProperty() == null) {
                    detailDTO.setVariantProperty("");
                } else {
                    detailDTO.setVariantProperty(skuVO.getVariantProperty());
                }
                detailDTO.setWarehouseLocation(skuVO.getWarehouseLocation());
                detailDTO.setSkuNo(skuVO.getSkuNo());
                detailDTO.setProductName(skuVO.getSkuName());
            }
            wayBillDetailList.add(detailDTO);
        }
        printWayBillPdf.setDetailList(wayBillDetailList);
        return printWayBillPdf;
    }

    /**
     * 冻结库存
     *
     * @param entity
     * @param detailEntityList
     * @return void
     * @Author Luo_WG
     * @Date 2023/12/25 17:47
     **/
    private void freezeInventory(SoB2cDeliveryEntity entity, List<SoB2cDeliveryDetailEntity> detailEntityList) {
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (SoB2cDeliveryDetailEntity detailEntity : detailEntityList) {
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(LocalDate.now());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getDeliveryQty());
            inOutStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation("");
            inOutStockList.add(inOutStockDTO);
        }
        //添加冻结库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode());
        //更新库存
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * 同步发货单到DMP
     */
    private void syncDeliveryToDmp(String id) {
        SoB2cDeliveryDTO.ViewDTO view = this.view(id);
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(view.getId());
        taskFeignDTO.setSourceCode(view.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_SO_B2C_DELIVERY_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.SO_B2C_DELIVERY_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(view));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(view.getStatus());
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }
}
