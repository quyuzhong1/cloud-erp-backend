package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
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
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
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
import com.erp.model.tms.enums.LogisticsLabelTypeEnum;
import com.erp.model.tms.enums.LogisticsPrintTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.enums.*;
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
import org.apache.commons.lang3.ObjectUtils;
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
        //拦截中 (处理中的拦截单)
        List<SoB2cDeliveryInterceptDTO.TabListDTO> interceptTabList = soB2cDeliveryInterceptService.tabList(param);
        SoB2cDeliveryInterceptDTO.TabListDTO interceptDTO = interceptTabList.stream().filter(v->SoB2cDeliveryInterceptStatusEnum.WAIT_HANDLE.getCode().equals(v.getTabFlag())).findFirst().orElse(null);
        Integer interceptCount = interceptDTO == null?0:interceptDTO.getCount();
        list.add(new SoB2cDeliveryDTO.TabListDTO("intercepting", interceptCount));
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
            return BatchResultDTO.fail(id, entity.getCode(), ApiError.B2C_SO_DELIVERY_NOT_EXISTS.msg);
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
            return BatchResultDTO.fail(id, entity.getCode(), ApiError.IS_NOT_MANUAL_DELIVERY.msg);
        }
        if (Objects.nonNull(soB2cEntity)) {
            String transferStatus = soB2cEntity.getTransferStatus();
            //表示要中转啊
            if(!TransferStatusEnum.NOT.getCode().equals(transferStatus) &&!TransferStatusEnum.SUCCESS.getCode().equals(transferStatus) ){
                throw new ServiceException("未预报成功不允许发货");
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
                    log.error("【发货单手动发货】销售单【{}】 标记发货失败 >>>错误信息{}", entity.getCode(), ExceptionUtil.stacktraceToString(e));
                    throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform(), e.getMessage());
                }
            }
        }

        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();

        //修改发货状态
        lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .set(SoB2cDeliveryEntity::getDeliveryTime, deliveryTime)
                .eq(SoB2cDeliveryEntity::getId, id).update();

        //修改订单状态待发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(deliveryTime);
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
            return BatchResultDTO.fail(id, entity.getCode(), ApiError.IS_NOT_FALSE_SHIPMENT.msg);
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
                PlatformSaveHandler.shipOrder(platformShipOrderDTO);
            }
        } catch (Exception e) {
            log.error("【虚假标记发货】销售单【{}】标记发货失败 >>>错误信息{}", entity.getCode(), ExceptionUtil.stacktraceToString(e));
            throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform(), e.getMessage());
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
                .filter(req -> SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus()))
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
        // 合并处理数量不相同的行
        Map<SoB2cDeliveryDTO.PrintPickingViewDTO, Integer> mergedMap = printPickingViewList.stream()
                .collect(Collectors.toMap(dto -> dto, SoB2cDeliveryDTO.PrintPickingViewDTO::getPickingQty, Integer::sum));
        printPickingViewList =  mergedMap.entrySet().stream()
                .map(entry -> {
                    SoB2cDeliveryDTO.PrintPickingViewDTO dto = entry.getKey();
                    dto.setPickingQty(entry.getValue());
                    return dto;
                })
                .collect(Collectors.toList());

        // 仓库+仓位排序
        List<SoB2cDeliveryDTO.PrintPickingViewDTO> resultList = printPickingViewList.stream()
                .sorted(Comparator.comparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getWarehouseName)
                        .thenComparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getWarehouseLocation)
                ).collect(Collectors.toList());

        Collections.sort(resultList, (s1, s2) -> {
            if (s1.getWarehouseLocation().isEmpty()) {
                return 1;
            } else if (s2.getWarehouseLocation().isEmpty()) {
                return -1;
            }
            return s1.getWarehouseLocation().compareTo(s2.getWarehouseLocation());
        });
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

        List<Pair<String, String>> addPairList = soB2cDeliveryEntities.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("打印了一张拣货单【%s】", ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), addPairList, "打印拣货单");

        //修改打印状态
        return lambdaUpdate().set(SoB2cDeliveryEntity::getIsPrintPicking, Boolean.TRUE).in(SoB2cDeliveryEntity::getId, ids).update();
    }

    @Override
    public BatchResultDTO printPickingCancel(String id) {
        SoB2cDeliveryEntity soB2cDeliveryEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soB2cDeliveryEntity)) {
            throw new ServiceException(ApiError.B2C_SO_DELIVERY_NOT_EXISTS);
        }
        if (!StrUtil.equals(soB2cDeliveryEntity.getStatus(),SoB2cDeliveryStatusEnum.PICKING.getCode())) {
            throw new ServiceException(ApiError.B2C_SO_DELIVERY_NOT_FINISH_PRINT,soB2cDeliveryEntity.getCode());
        }

        //查询是否冻结
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }
        //更新单据状态和拣货状态
        Boolean update = updateStatusByIdList(Arrays.asList(id), SoB2cDeliveryStatusEnum.WAIT_HANDLE.getStatus(), Boolean.FALSE,Boolean.FALSE);
        if (!update) {
            throw new ServiceException("取消打印拣货单");
        }
        operateLogService.addModuleOperateLog(StrUtil.format("取消打印了一张拣货单【{}】",soB2cDeliveryEntity.getCode()), ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "取消打印拣货单");
        return BatchResultDTO.success(soB2cDeliveryEntity.getId(), soB2cDeliveryEntity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillView(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);

        //已发货和取消发货单 状态，不允许在打印标签
        List<String> codeList = soB2cDeliveryEntities.stream()
                .filter(req -> SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus()))
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
        //查询是否是跟踪单号
        SoB2cLogisticsEntity logisticsEntity = soB2cFeign.getSoB2cLogisticsByTrackNo(businessCode);
        if (ObjectUtils.isNotEmpty(logisticsEntity)) {
            return lambdaQuery()
                    .eq(SoB2cDeliveryEntity::getSourceId, logisticsEntity.getMainId())
                    .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                    .last("LIMIT 1").one();
        } else {
            return this.getOne(new LambdaQueryWrapper<>(SoB2cDeliveryEntity.class)
                    .or(soB2cDeliveryEntityLambdaQueryWrapper -> soB2cDeliveryEntityLambdaQueryWrapper
                            .eq(SoB2cDeliveryEntity::getSoCode, businessCode)
                            .or()
                            .eq(SoB2cDeliveryEntity::getTransportNo, businessCode))
                    .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                    .last(" limit 1")
            );
        }
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
                platformWaybill = this.getPlatformWaybill(waybillDetailDTOList, printWayBillPdfResultList);

                //保存物流面单到订单标签信息表
                saveLable(platformWaybill);

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
                    if (CollectionUtils.isNotEmpty(printWayBillPdf.getLogisticsLabelBase64List())) {
                        base64List.addAll(printWayBillPdf.getLogisticsLabelBase64List());
                    } else {
                        //没有就请求sdk获取
                        List<String> logisticsWaybillList = platformWaybill.stream().filter(req -> req.getSoB2cId().equals(printWayBillPdf.getSoId())).map(req -> req.getLogisticsBase64()).findFirst().orElse(null);
                        if (CollectionUtils.isNotEmpty(logisticsWaybillList)) {
                            base64List.addAll(logisticsWaybillList);
                        }
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
                    if (CollectionUtils.isNotEmpty(printWayBillPdf.getLogisticsLabelBase64List())) {
                        base64List.addAll(printWayBillPdf.getLogisticsLabelBase64List());
                    } else {
                        //没有就请求sdk获取
                        List<String> logisticsWaybillList = platformWaybill.stream().filter(req -> req.getSoB2cId().equals(printWayBillPdf.getSoId())).map(req -> req.getLogisticsBase64()).findFirst().orElse(null);
                        if (CollectionUtils.isNotEmpty(logisticsWaybillList)) {
                            base64List.addAll(logisticsWaybillList);
                        }
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
                    String msg = StrUtil.format("打印了订单编号为【{}】的面单/配货单", logisticsWaybillDetailDTO.getSoCode());
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

    private void saveLable(List<SoB2cDTO.WaybillDTO> platformWaybill) {
        if (CollectionUtils.isNotEmpty(platformWaybill)) {
            List<SoB2cLabelDTO.UpdateDTO> dtoList = new ArrayList<>();
            for (SoB2cDTO.WaybillDTO waybillDTO : platformWaybill) {
                for (String labelBase : waybillDTO.getDistributeBase64()) {
                    SoB2cLabelDTO.UpdateDTO updateDTO = new SoB2cLabelDTO.UpdateDTO();
                    updateDTO.setLogisticsLabelBase64(labelBase);
                    updateDTO.setMainId(waybillDTO.getSoB2cId());
                    dtoList.add(updateDTO);
                }
            }
            soB2cFeign.saveSoB2cLabel(dtoList);
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
    @DataIdempotent(keyIdName = "id")
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

    @Override
    public Boolean updateB2cDeliveryWeightBySoId(SoB2cDeliveryDTO.UpdateWeightDTO dto) {
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listBySoB2cId(dto.getSoId());
        SoB2cDeliveryEntity deliveryEntity= deliveryEntityList.stream().filter(v->!v.getStatus().equals(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())).findFirst().orElse(null);
        if(Objects.isNull(deliveryEntity)){
            return false;
        }
        String msg = StrUtil.format("用户【{}】更新重量为{} ", commonService.getUserInfo().getUserName(),dto.getWeight()+dto.getWeightUnit());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), deliveryEntity.getId(), "【组包称重】");
        deliveryEntity.setWeight(dto.getWeight());
        deliveryEntity.setWeighingTime(LocalDateTime.now());
        deliveryEntity.setWeightUnit(dto.getWeightUnit());
        deliveryEntity.setIsWeigh(true);
        return updateById(deliveryEntity);
    }

    @Override
    public BatchResultDTO finishPrint(String id) {
        SoB2cDeliveryEntity soB2cDeliveryEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soB2cDeliveryEntity)) {
            throw new ServiceException(ApiError.B2C_SO_DELIVERY_NOT_EXISTS);
        }
        if (!StrUtil.equals(soB2cDeliveryEntity.getStatus(),SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode())) {
            throw new ServiceException(ApiError.B2C_SO_DELIVERY_FINISH_PRINT,soB2cDeliveryEntity.getCode());
        }

        //查询是否冻结
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }
        //更新单据状态和拣货状态
        Boolean update = updateStatusByIdList(Arrays.asList(id), SoB2cDeliveryStatusEnum.PICKING.getStatus(), Boolean.TRUE, Boolean.TRUE);
        if (!update) {
            throw new ServiceException("完成打印失败");
        }
        operateLogService.addModuleOperateLog(StrUtil.format("完成单据单号为【{}】的打印操作",soB2cDeliveryEntity.getCode()), ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "完成打印");
        return BatchResultDTO.success(soB2cDeliveryEntity.getId(), soB2cDeliveryEntity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public Boolean exportExcel(SoB2cDeliveryDTO.PagingParamDTO dto, HttpServletResponse response) {
        dto.setPermissionSql(dto.getPermissionSql());
        List<SoB2cDeliveryDTO.ListDTO> list = this.baseMapper.list(dto);
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 数据处理
        fillList(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/b2cDeliveryOrderExport.xlsx";
        String name = "发货单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean falseDeliveryBatch(List<String> ids) {
        for (String id : ids) {
            this.falseDelivery(id);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean addDeliveryLog(List<SoB2cDeliveryEntity> deliveryEntities) {
        for (SoB2cDeliveryEntity entity : deliveryEntities) {
            String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", commonService.getUserInfo().getUserName(), "组包预报", entity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "称重出库");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean mergePackageDelivery(List<String> soIdList) {

        //记录需要虚假发货的订单id
        List<String> deliverySoIdList = new ArrayList<>();
        for (String soId : soIdList) {
            Boolean flag = soB2cFeign.checkPlatformShipOrder(soId);
            if (flag) {
                deliverySoIdList.add(soId);
            }
        }
        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listBySourceIds(deliverySoIdList);
        List<String> soDeliveryIds = deliveryEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        //调用第三方平台SDK发货
        this.falseDeliveryBatch(soDeliveryIds);

        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntities = this.listBySourceIds(soIdList);

        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();

        //将发货状态更新为已发货
        for (SoB2cDeliveryEntity deliveryEntity : deliveryEntities) {
            deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
            deliveryEntity.setDeliveryTime(deliveryTime);
        }

        //将发货状态更新为已发货
        if (!this.updateBatchById(deliveryEntities)) {
            throw new ServiceException("发货单更新失败");
        }

        for (SoB2cDeliveryEntity deliveryEntity : deliveryEntities) {
            //出库
            this.generateB2cSoOutstock(deliveryEntity);
            String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", commonService.getUserInfo().getUserName(), "组包称重", deliveryEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), deliveryEntity.getId(), "组包称重");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillPreview(SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param) {
        List<String> ids = param.getIds();

        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);

        //已发货和取消发货单 状态，不允许在打印标签
        List<String> codeList = soB2cDeliveryEntities.stream()
                .filter(req -> SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(req.getStatus()))
                .map(req -> req.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codeList)) {
            throw new ServiceException(ApiError.STATUS_NOT_PRINT_LABEL, StrUtil.join(",", codeList));
        }

        //查询是否冻结
        List<String> soIds = soB2cDeliveryEntities.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(soIds);

        for (SoB2cEntity soB2cEntity : soB2cEntities) {
            if (soB2cEntity.getIsFrozen()) {
                throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
            }
        }

        //查询物流商信息
        List<String> logisticsChannelIds = soB2cDeliveryEntities.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelInfoList = logisticsFeign.listChannelInfoById(logisticsChannelIds);
        List<String> paperSizeList = channelInfoList.stream().map(req -> req.getPaperSize()).distinct().collect(Collectors.toList());
        if (paperSizeList.size() > 1) {
            throw new ServiceException(ApiError.PAPER_SIZE_INCONSISTENT_NOT_PRINT);
        }


        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> list = new ArrayList<>();


        //查询打印类型
        List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsBillFeign.listPrintTypeByChannelIds(logisticsChannelIds);

        //根据渠道分组
        Map<String, List<SoB2cDeliveryEntity>> logisticsChannelMap = soB2cDeliveryEntities.stream().collect(Collectors.groupingBy(req -> req.getLogisticsChannelId()));
        for (Map.Entry<String, List<SoB2cDeliveryEntity>> stringListEntry : logisticsChannelMap.entrySet()) {
            String logisticsChannelId = stringListEntry.getKey();
            List<SoB2cDeliveryEntity> deliveryEntities = stringListEntry.getValue();
            SoB2cDeliveryDTO.PrintLogisticsWaybillDTO waybillDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDTO();
            //打印类型
            waybillDTO.setPrintType(param.getPrintType());

            //查询是否允许打印面单和配货单
            LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (ObjectUtil.isEmpty(authDTO)) {
                throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
            }

            // 配货单需要根据渠道查询是否是自定义配置，自定义配置需要组装数据
            LogisticsPrintTypeDTO.ViewDTO logisticsPrintTypeEntity = logisticsPrintTypeEntities.stream()
                    .filter(req -> LogisticsPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(req.getPrintType())
                            && req.getLogisticsChannelId().equals(deliveryEntities.get(0).getLogisticsChannelId())
                    ).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(logisticsPrintTypeEntity) && !param.getPrintType().equals(SoB2cDeliveryPrintTypeEnum.LOGISTICS_BILL.getCode())) {
                throw new ServiceException(ApiError.LOGISTICS_PRINT_TYPE_SETTING_NOT_EXIST, deliveryEntities.get(0).getLogisticsChannelName());
            }

            //根据渠道id查询渠道名称
            List<SoB2cDeliveryEntity> soB2cDeliveryEntityList = deliveryEntities.stream().filter(req -> req.getLogisticsChannelId().equals(logisticsChannelId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(soB2cDeliveryEntityList)) {
                waybillDTO.setLogisticsChannelName(soB2cDeliveryEntityList.get(MathUtil.ZERO).getLogisticsChannelName());
                waybillDTO.setLogisticsChannelId(soB2cDeliveryEntityList.get(MathUtil.ZERO).getLogisticsChannelId());
            }

            //有运单号数量
            Integer isTransportNoNum = Math.toIntExact(soB2cDeliveryEntityList.stream().filter(req -> StringUtils.isNotBlank(req.getTransportNo())).count());
            waybillDTO.setIsTransportNoNum(isTransportNoNum);
            //无运单号数量
            Integer notTransportNoNum = Math.toIntExact(soB2cDeliveryEntityList.stream().filter(req -> StringUtils.isBlank(req.getTransportNo())).count());
            waybillDTO.setNotTransportNoNum(notTransportNoNum);
            waybillDTO.setDisabled(Boolean.FALSE);

            //判断打印类型校验
            LogisticsPlatformEnum logisticsPlatformEnum = LogisticsPlatformEnum.getByCode(authDTO.getLogisticsPlatform());

            switch (SoB2cDeliveryPrintTypeEnum.getByCode(param.getPrintType())){
                case LOGISTICS_BILL :
                    //打印面单预览
                    if ("N".equalsIgnoreCase(logisticsPlatformEnum.getPrintLabel())) {
                        waybillDTO.setErrorMsg(StrUtil.format(ApiError.LOGISTICS_NOT_PRINT_LOGISTICS_BILL.msg, logisticsPlatformEnum.getName()));
                        waybillDTO.setDisabled(Boolean.TRUE);
                    }
                    break;
                case ALLOCATE_CARGO_BILL :
                    //打印配货单预览
                    if (LogisticsLabelTypeEnum.AUTHORITY.getCode().equals(logisticsPrintTypeEntity.getLabelType())) {
                        if ("N".equalsIgnoreCase(logisticsPlatformEnum.getPrintDelivery())) {
                            waybillDTO.setErrorMsg(StrUtil.format(ApiError.LOGISTICS_NOT_PRINT_ALLOCATE_CARGO_BILL.msg, logisticsPlatformEnum.getName()));
                            waybillDTO.setDisabled(Boolean.TRUE);
                        }
                    }
                    break;
                case ALL :
                default:
                    break;
            }

            //详情
            List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> detailList = new ArrayList<>();
            for (SoB2cDeliveryEntity deliveryEntity : deliveryEntities) {
                SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO waybillDetailDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO();
                waybillDetailDTO.setId(deliveryEntity.getId());
                waybillDetailDTO.setSoB2cId(deliveryEntity.getSourceId());
                waybillDetailDTO.setSoCode(deliveryEntity.getSoCode());
                waybillDetailDTO.setLogisticsChannelId(deliveryEntity.getLogisticsChannelId());
                waybillDetailDTO.setLogisticsChannelName(deliveryEntity.getLogisticsChannelName());
                waybillDetailDTO.setTransportNo(deliveryEntity.getTransportNo());
                waybillDetailDTO.setShopId(deliveryEntity.getShopId());
                String logisticType = soB2cLogisticsEntities.stream().filter(req -> req.getMainId().equals(deliveryEntity.getSourceId())).map(SoB2cLogisticsEntity::getLogisticType).findFirst().orElse("");
                waybillDetailDTO.setLogisticType(logisticType);

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
    @Transactional(rollbackFor = Exception.class)
    public boolean hasNotShippedDeliveryAndLog(SoB2cEntity currentEntity) {
        Integer count = this.lambdaQuery()
                .eq(SoB2cDeliveryEntity::getSourceId, currentEntity.getId())
                .eq(SoB2cDeliveryEntity::getSourceCode, currentEntity.getCode())
                .eq(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .count();
        if (count > 0){
            return false;
        }
        // 记录日志
        OperateLogDTO.AddModuleOperateLogDTO operateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
        operateLogDTO.setContent(StrUtil.format("【】因无已发货的发货单跳过生成销售出库", currentEntity.getCode()));
        operateLogDTO.setModuleType(ModuleTypeEnum.SO_B2C.getCode());
        operateLogDTO.setBusinessId(currentEntity.getId());
        operateLogDTO.setOperation("重新生成销售出库单");
        soB2cFeign.addModuleOperateLog(operateLogDTO);
        return true;
    }

    @Override
    public List<BatchResultDTO> logisticsIntercept(List<String> ids) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);
        List<SoB2cDeliveryInterceptEntity> soB2cDeliveryInterceptEntityList = soB2cDeliveryInterceptService.listByDeliveryIds(ids);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO result;
            SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryEntities.stream().filter(v->v.getId().equals(id)).findFirst().orElse(new SoB2cDeliveryEntity());
            SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity = soB2cDeliveryInterceptEntityList.stream().filter(v->v.getDeliveryId().equals(soB2cDelivery.getId())).findFirst().orElse(null);
            if(Objects.isNull(soB2cDeliveryInterceptEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"物流拦截单不存在, 物流拦截失败"));
                continue;
            }
            try {
                result = soB2cDeliveryInterceptService.logisticsIntercept(soB2cDeliveryInterceptEntity.getId());
            }catch (Exception e){
                log.error("物流拦截单 物流拦截失败",e);
                result = BatchResultDTO.fail(soB2cDeliveryInterceptEntity.getId(), soB2cDeliveryInterceptEntity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS;
    }

    @Override
    public List<BatchResultDTO> interceptResultConfirm(SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto) {
        List<String> ids = dto.getIds();
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = this.listByIds(ids);
        List<SoB2cDeliveryInterceptEntity> soB2cDeliveryInterceptEntityList = soB2cDeliveryInterceptService.listByDeliveryIds(ids);
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO result;
            SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryEntities.stream().filter(v->v.getId().equals(id)).findFirst().orElse(new SoB2cDeliveryEntity());
            SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity = soB2cDeliveryInterceptEntityList.stream().filter(v->v.getDeliveryId().equals(soB2cDelivery.getId())).findFirst().orElse(null);
            if(Objects.isNull(soB2cDeliveryInterceptEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"物流拦截单不存在, 确认失败"));
                continue;
            }
            try {
                result = soB2cDeliveryInterceptService.interceptResultConfirm(dto,soB2cDeliveryInterceptEntity.getId());
            }catch (Exception e){
                log.error("物流拦截单 拦截结果确认失败",e);
                result = BatchResultDTO.fail(soB2cDeliveryInterceptEntity.getId(), soB2cDeliveryInterceptEntity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS;
    }

    /**
     *
     * @param soId
     * @return
     */
    @Override
    public SoB2cDeliveryEntity getNotCancelBySoId(String soId) {
        if(StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(SoB2cDeliveryEntity ::getSourceId,soId)
                .ne(SoB2cDeliveryEntity::getStatus,SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                .last(" LIMIT 1").one();
    }

    /**
     * @description: 根据id集合更新修改状态
     * @author Will
     * @date: 2024/4/17 10:58
     * @param idList
     * @param status
     * @return Boolean
     */
    private Boolean updateStatusByIdList(List<String> idList,String status,Boolean isPrintPicking,Boolean isPrintLogistics) {
        if (CollectionUtils.isEmpty(idList)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate().in(SoB2cDeliveryEntity::getId,idList).set(SoB2cDeliveryEntity::getStatus,status)
                .set(SoB2cDeliveryEntity::getIsPrintPicking,isPrintPicking)
                .set(SoB2cDeliveryEntity::getIsPrintLogistic,isPrintLogistics)
                .set(isPrintPicking,SoB2cDeliveryEntity::getFinishPrintTime,LocalDateTime.now())
                .set(!isPrintPicking,SoB2cDeliveryEntity::getFinishPrintTime,null)
                .update();
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
    private List<SoB2cDTO.WaybillDTO> getPlatformWaybill(List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> waybillDetailDTOList, List<PrintWayBillPdfDTO> printWayBillPdfResultList) {
        List<LogisticsBillDTO.PrintLogisticsWaybillDTO> logisticsWaybillDTOList = new ArrayList<>();
//        List<SoB2cEntity> soB2cEntityList = soB2cEntities.stream()
//                .filter(req -> StringUtils.isBlank(req.getLogisticsWaybill()))
//                .collect(Collectors.toList());
        for (SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO detailDTO : waybillDetailDTOList) {
            PrintWayBillPdfDTO printWayBillPdfDTO = printWayBillPdfResultList.stream().filter(req -> detailDTO.getSoB2cId().equals(req.getSoId()) && CollectionUtils.isEmpty(req.getLogisticsLabelBase64List())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(printWayBillPdfDTO)) {
                continue;
            }
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
        soB2cDeliveryEntity.setPlatformCode(soB2cEntity.getPlatformCode());
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
        List<SkuVO> skuVOList = plmTaskFeign.getSkuBaseByIds(skuIds);

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
                record.setOrderRemark(soB2cEntity.getRemark());
            }
            //平台名称
            PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(record.getDictPlatform());
            if (ObjectUtil.isNotEmpty(platformDictEnum)){
                record.setDictPlatformName(platformDictEnum.getName());
            }
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
            record.setWeightName(record.getWeight() + record.getWeightUnit());
            record.setInspectionName(record.getIsInspection()? InspectionEnum.YES.getName(): InspectionEnum.NO.getName());
            record.setWeighName(record.getIsWeigh()? WeightEnum.YES.getName(): WeightEnum.NO.getName());
            record.setPrintPickingName(record.getIsPrintPicking()? PrintPickingEnum.YES.getName(): PrintPickingEnum.NO.getName());
            record.setPrintLogisticName(record.getIsPrintLogistic()? PrintPickingEnum.YES.getName(): PrintPickingEnum.NO.getName());
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
