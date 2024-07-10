package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.PrintWayBillPdfDetailDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
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
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cLabelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.enums.LogisticsLabelTypeEnum;
import com.erp.model.tms.enums.LogisticsPrintTypeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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
    private SoOutstockService soOutstockService;
    @Lazy
    @Resource
    private AsyncService asyncService;
    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private CfgRulePickingService cfgRulePickingService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;

    @Resource
    private WaveListService waveListService;
    @Resource
    private WaveListDetailService waveListDetailService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private PackingInspectionService packingInspectionService;

    @Resource
    @Lazy
    private WarehouseLocationReplenishService warehouseLocationReplenishService;


    @Resource
    private CfgRuleOutService cfgRuleOutService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    @DataIdempotent(keyIdName = "addDTO.soCode")
    public Boolean add(SoB2cDeliveryDTO.AddDTO addDTO) {
        SoB2cDeliveryEntity existEntity = this.getNotCancelBySoId(addDTO.getSourceId());
        if(ObjectUtil.isNotEmpty(existEntity)){
            throw new ServiceException(StrUtil.format("已生成发货单，发货单号：{}", existEntity.getCode()));
        }
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c发货单", soB2cDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), soB2cDeliveryEntity.getId(), "新增操作");
        // 新增明细
        soB2cDeliveryDetailService.add(soB2cDeliveryDetailEntities, soB2cDeliveryEntity.getId());

        //冻结虚拟库存
        freezeVirtualInventory(soB2cDeliveryEntity,soB2cDeliveryDetailEntities);

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
        // 手动标发
        int falseShipment = baseMapper.countShipmentMark(param);
        list.add(new SoB2cDeliveryDTO.TabListDTO("false_shipment", falseShipment));
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

        //已发货、取消发货的数据不允许手动发货，其他状态都可以直接变更为已发货  待处理数据不允许手动发货
        if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(entity.getStatus())){
            return BatchResultDTO.fail(id, entity.getCode(), ApiError.IS_NOT_MANUAL_DELIVERY.msg);
        }
        if (Objects.nonNull(soB2cEntity)) {
            String transferStatus = soB2cEntity.getTransferStatus();
            //表示要中转啊
            if(!TransferStatusEnum.NOT.getCode().equals(transferStatus) &&!TransferStatusEnum.SUCCESS.getCode().equals(transferStatus) ){
                throw new ServiceException("未预报成功不允许发货");
            }

        }

        //如果是手动标发不用再次调用第三方SDK标记发货，因为手动标发已经调用过了
        if (ShipmentMarkTypeEnum.MANUAL.getCode().equals(entity.getShipmentMark())) {
//            if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
//                //调用第三方平台SDK发货
//                PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
//                platformShipOrderDTO.setSoB2cId(entity.getSourceId());
//                platformShipOrderDTO.setDictPlatform(entity.getDictPlatform());
//                try {
//                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
//                } catch (Exception e) {
//                    log.error("【发货单手动发货】销售单【{}】 标记发货失败 >>>错误信息{}", entity.getCode(), ExceptionUtil.stacktraceToString(e));
//                    throw new ServiceException(ApiError.PLATFORM_SHIP_ORDER_ERROR, entity.getDictPlatform(), e.getMessage());
//                }
//            }
            if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
                // 调用第三方平台SDK标记发货(独立事务)
                String businessDesc = "发货单手动发货";
                asyncService.asyncShipOrder(soB2cEntity.getId(),
                        soB2cEntity.getCode(),
                        soB2cEntity.getDictPlatform(),
                        soB2cEntity.convertSubmitPlatformUniqueKey(),
                        id,
                        businessDesc, false);
            } else {
                log.warn("【{}】未达到条件:忽略标记平台发货", soB2cEntity.getCode());
            }
        }

        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();

        //修改发货状态
        lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .set(SoB2cDeliveryEntity::getShipmentMark, ShipmentMarkTypeEnum.AUTO.getCode())
                .set(SoB2cDeliveryEntity::getDeliveryTime, deliveryTime)
                .eq(SoB2cDeliveryEntity::getId, id).update();

        //修改订单状态待发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(entity.getSourceId()));
        updateDeliveryTimeDTO.setSoDeliveryDTOList(Arrays.asList(new SoB2cDTO.SoDeliveryDTO(entity.getSourceId(),entity.getCode())));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(deliveryTime);
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

        // 操作日志
        String msg = StrUtil.format("用户【{}】手动发货单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c发货单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "手动发货");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动发货");


    }

    @Override
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO falseDelivery(String id) {
        SoB2cDeliveryEntity entity = this.getById(id);
        //手动标发，已发货，取消发货的数据不允许操作手动标发
        if (SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(entity.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.fail(id, entity.getCode(), ApiError.IS_NOT_FALSE_SHIPMENT.msg);
        }

        //查询是否冻结
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        if (soB2cEntity.getIsFrozen()) {
            throw new ServiceException(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE, soB2cEntity.getCode());
        }

        //修改状态为手动标发
        update(Wrappers.<SoB2cDeliveryEntity>lambdaUpdate()
                .set(SoB2cDeliveryEntity::getShipmentMark, ShipmentMarkTypeEnum.MANUAL.getCode())
                .eq(SoB2cDeliveryEntity::getId, id));
        //修改订单状态待发货
        soB2cFeign.updateSoB2cStatus(Arrays.asList(entity.getSourceId()), SoB2cBillStatusEnum.ENUM_WAIT_SHIPPED.getCode());
        // 操作日志
        String msg = StrUtil.format("用户【{}】手动标发单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c发货单", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "手动标发");

        if (soB2cFeign.checkPlatformShipOrder(entity.getSourceId())) {
            // 调用第三方平台SDK标记发货(独立事务)
            String businessDesc = "手动标发";
            asyncService.asyncShipOrder(soB2cEntity.getId(),
                    soB2cEntity.getCode(),
                    soB2cEntity.getDictPlatform(),
                    soB2cEntity.convertSubmitPlatformUniqueKey(),
                    id,
                    businessDesc, true);
        } else {
            log.warn("【{}】未达到条件:忽略标记平台发货", soB2cEntity.getCode());
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "手动标发");
    }

    @Override
    public List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids) {
        List<SoB2cDeliveryDetailEntity> deliveryDetailEntityList = soB2cDeliveryDetailService.listByMainIds(ids);

        //待处理、已发货和取消发货单 状态，不允许在打印拣货单
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listByIds(ids);
        List<String> codeList = deliveryEntityList.stream()
                .filter(req -> SoB2cDeliveryStatusEnum.notPrint().contains(req.getStatus()))
                .map(SoB2cDeliveryEntity::getCode).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codeList)) {
            throw new ServiceException(ApiError.STATUS_NOT_PRINT_PICKING, StrUtil.join(",", codeList));
        }
        //查询产品信息
        List<String> skuIds = deliveryDetailEntityList.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingViewList = new ArrayList<>();
        List<PickingListsEntity> list = pickingListsService.list(Wrappers.<PickingListsEntity>lambdaQuery().in(PickingListsEntity::getSourceId, ids));
        List<String> pickingIds = list.stream().map(PickingListsEntity::getId).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetails = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, pickingIds));

        //波次列表信息
        List<String> sourceIdList = list.stream().map(PickingListsEntity::getSourceId).distinct().collect(Collectors.toList());
        List<WaveListDTO.WaveDeliveryDTO> waveDeliveryList = waveListService.listByDeliverIds(sourceIdList);

        for (PickingDetailEntity pickingDetail : pickingDetails) {
            SoB2cDeliveryDTO.PrintPickingViewDTO viewDTO = new SoB2cDeliveryDTO.PrintPickingViewDTO();
            BeanMapper.copy(pickingDetail, viewDTO);
            viewDTO.setPickingQty(pickingDetail.getQty());
            //匹配sku信息
            SkuVO skuVO = skuVOList.stream()
                    .filter(req -> req.getSkuId().equals(pickingDetail.getSkuId()))
                    .distinct().findFirst().orElse(new SkuVO());
            viewDTO.setProductName(skuVO.getSkuName());
            PickingListsEntity pickingLists = list.stream()
                    .filter(v -> v.getId().equals(pickingDetail.getMainId()))
                    .findFirst()
                    .orElse(new PickingListsEntity());
            viewDTO.setWarehouseId(pickingLists.getWarehouseId());
            viewDTO.setWarehouseName(pickingLists.getWarehouseName());
            //备注
            SoB2cDeliveryEntity entity = deliveryEntityList.stream().filter(req -> req.getId().equals(pickingLists.getSourceId())).findFirst().orElse(new SoB2cDeliveryEntity());
            viewDTO.setRemark(entity.getRemark());
            viewDTO.setDeliveryCode(entity.getCode());

            //波次信息
            WaveListDTO.WaveDeliveryDTO waveDeliveryDTO = waveDeliveryList.stream().filter(obj -> StrUtil.equals(obj.getDeliveryId(), entity.getId())).findFirst().orElse(new WaveListDTO.WaveDeliveryDTO());
            viewDTO.setWaveCode(waveDeliveryDTO.getWaveCode());
            printPickingViewList.add(viewDTO);
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
        return printPickingViewList.stream()
                .sorted(Comparator.comparing(SoB2cDeliveryDTO.PrintPickingViewDTO::getWarehouseName)
                        .thenComparing((s1, s2) -> {
                            if (StringUtils.isBlank(s1.getWarehouseLocation()) && !StringUtils.isBlank(s2.getWarehouseLocation())) {
                                return 1;
                            } else if (!StringUtils.isBlank(s1.getWarehouseLocation()) && StringUtils.isBlank(s2.getWarehouseLocation())) {
                                return -1;
                            } else if (StringUtils.isBlank(s1.getWarehouseLocation()) && StringUtils.isBlank(s2.getWarehouseLocation())) {
                                return 0;
                            } else {
                                return s1.getWarehouseLocation().compareTo(s2.getWarehouseLocation());
                            }
                        }))
                .collect(Collectors.toList());
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
        // 批量异步查询亚马逊状态和更新
        asyncService.asyncBatchQueryAndUpdateOrderStatus(soB2cEntities);

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
        if (SoB2cDeliveryStatusEnum.notPrint().contains(soB2cDeliveryEntity.getStatus())) {
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
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(soIds);

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
            //订单波次篮号信息
            Map<String, String> orderBasketNoMap = waveListDetailService.getOrderBasketNoMap(soIds);
            List<PrintWayBillPdfDTO> printWayBillPdfResultList = soB2cFeign.printWayBillPdf(soIds);
            printWayBillPdfResultList.forEach(v->{
                v.setBasketNo(orderBasketNoMap.getOrDefault(v.getSoId(), ""));
            });
            waybillDetailDTOList.forEach(v -> v.setIndex(orderBasketNoMap.containsKey(v.getSoB2cId()) ? Integer.parseInt(orderBasketNoMap.get(v.getSoB2cId())) : Integer.MAX_VALUE));

            //根据篮号排序，为空放最后
            waybillDetailDTOList = waybillDetailDTOList.stream()
                    .sorted(Comparator.comparing(SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO::getIndex))
                    .collect(Collectors.toList());
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
        if(CollectionUtils.isEmpty(base64List)){
            throw new ServiceException("未找到面单数据");
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

    @Override
    public void customDistribute(List<String> base64List, PrintWayBillPdfDTO printWayBillPdfDTO) {
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
    public String dimensionalWeightPipeline(DimensionalWeightDTO dto) {
        //物流单编码
        String logisticsCode = dto.getBarCode();
        //物流单信息
        SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cFeign.getByTrackNoOrTransportNo(logisticsCode);
        if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
            log.error("物流编码【{}】未查询到物流单信息",logisticsCode);
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }
        SoB2cEntity soB2cEntity = FeignQuery.getById(SoB2cEntity.class,soB2cLogisticsEntity.getMainId());
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            log.error("物流编码【{}】未查询到销售单信息",logisticsCode);
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }

        //发货单信息
        SoB2cDeliveryEntity old = getBySoCode(soB2cEntity.getCode());
        if (ObjectUtil.isEmpty(old)) {
            log.error("编码【{}】未查询到发货单信息",soB2cEntity.getCode());
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }
        //现发货单
        SoB2cDeliveryEntity entity = new SoB2cDeliveryEntity();
        BeanMapperUtils.copy(old,entity);
        if (!StrUtil.equals(entity.getStatus(),SoB2cDeliveryStatusEnum.PICKING.getCode())) {
            log.error("编码【{}】非已拣货不支持更新",soB2cEntity.getCode());
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }

        entity.setLength(dto.getLength());
        entity.setWidth(dto.getWidth());
        entity.setHeight(dto.getHeight());
        entity.setWeight(dto.getWeight());
        //单位默认kg
        entity.setWeightUnit(UnitEnum.WeightUnitEnum.KG.getCode());
        entity.setIsWeigh(Boolean.TRUE);

        //查询渠道信息
        if (StrUtil.isBlank(soB2cLogisticsEntity.getLogisticsChannelId())) {
            log.error("编码【{}】未查询到渠道id信息",soB2cEntity.getCode());
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }

        //查询渠道信息
        LogisticsChannelEntity channelEntity = logisticsFeign.getChannelById(soB2cLogisticsEntity.getLogisticsChannelId());
        if (ObjectUtil.isEmpty(channelEntity)) {
            log.error("编码【{}】未查询到渠道信息",soB2cEntity.getCode());
            return CfgRuleOutEnum.EquipmentSortingPortEnum.NINE.getCode();
        }

        //出库配置
        CfgRuleOutDTO.SortingPortRuleDTO sortingPortRuleDTO = CfgRuleOutDTO.SortingPortRuleDTO.builder()
                .scanLength(dto.getLength())
                .scanWidth(dto.getWidth())
                .scanHeight(dto.getHeight())
                .scanWeight(MathUtil.multiply(dto.getWeight(),new BigDecimal(1000)))
                .orderLength(soB2cLogisticsEntity.getLength())
                .orderWidth(soB2cLogisticsEntity.getWidth())
                .orderHeight(soB2cLogisticsEntity.getHeight())
                .orderWeight(soB2cLogisticsEntity.getWeight())
                .logisticsSupplierId(channelEntity.getMainId())
                .channelId(soB2cLogisticsEntity.getLogisticsChannelId())
                .deliveryOrderId(entity.getId())
                .build();
        //返回分检口
        String sortingPort = cfgRuleOutService.getSortingPort(sortingPortRuleDTO);
        CfgRuleOutDTO.CommonDTO commonDTO = cfgRuleOutService.view();
        Boolean isDeviation = cfgRuleOutService.handleB2cAllowableDeviations(commonDTO.getB2cAllowableDeviations(),sortingPortRuleDTO);

        //记录发货单异常
        if (!isDeviation) {
            entity.setStatus(SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode());
            entity.setAbnormalCause(AbnormalCauseEnum.EQUIPMENT_SORTING.getCode());
        }

        //自动出库
        if (isDeviation && entity.getIsAutoOut()) {
            try {
                packingInspectionService.soB2cDeliveryAutoOut(soB2cEntity,entity);
            } catch (Exception e) {
                log.error("发货单【{}】自动出库失败",entity.getCode());
            }
        }
        //更新发货单
        this.updateById(entity);

        //发货单操作日志
        //操作日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "", "");
        return sortingPort;
    }

    @Override
    public Boolean updateAbnormal(List<String> ids, AbnormalCauseEnum abnormalCauseEnum) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.FALSE;
        }

        Boolean flag = lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode())
                .set(SoB2cDeliveryEntity::getAbnormalCause, abnormalCauseEnum.getCode())
                .in(SoB2cDeliveryEntity::getId, ids)
                .update();
        return flag;
    }

    @Override
    public void printLogisticsBillConfirmById(String id, HttpServletResponse response) {
        SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto = new SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO();
        dto.setPrintType(SoB2cDeliveryPrintTypeEnum.LOGISTICS_BILL.getCode());
        List<SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO> detailList = new ArrayList<>();
        //发货单
        SoB2cDeliveryEntity soB2cDeliveryEntity = this.getById(id);
        if (ObjectUtil.isEmpty(soB2cDeliveryEntity)) {
           throw new ServiceException("未发现发货单信息");
        }
        //物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(soB2cDeliveryEntity.getSourceId()));
        if (ObjectUtil.isEmpty(soB2cDeliveryEntity)) {
            throw new ServiceException("未发现销售订单物流信息");
        }
        SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO detailDTO = new SoB2cDeliveryDTO.PrintLogisticsWaybillDetailDTO();
        BeanMapperUtils.copy(soB2cDeliveryEntity,detailDTO);
        detailDTO.setIndex(MathUtil.ZERO);
        detailDTO.setSoB2cId(soB2cDeliveryEntity.getSourceId());
        detailDTO.setLogisticType(soB2cLogisticsList.get(0).getLogisticType());
        detailList.add(detailDTO);
        dto.setDetailList(detailList);
        printLogisticsBillConfirm(dto,response);
    }

    @Override
    public List<String> listIdsByShipmentMark(ShipmentMarkTypeEnum type) {
        List<SoB2cDeliveryEntity> entities = list(Wrappers.<SoB2cDeliveryEntity>lambdaQuery()
                .eq(SoB2cDeliveryEntity::getShipmentMark, type.getCode()).select(SoB2cDeliveryEntity::getId));
        return entities.stream().map(SoB2cDeliveryEntity::getId).collect(Collectors.toList());
    }

    @Override
    public void updateShipmentMark(List<String> ids, String code) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        lambdaUpdate()
                .set(SoB2cDeliveryEntity::getShipmentMark, code)
                .in(SoB2cDeliveryEntity::getId, ids)
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                .update();
    }

    @Override
    public void updateDeliveryStatus(List<String> deliveryIdList, String status) {
        if (CollectionUtils.isEmpty(deliveryIdList)) {
            return;
        }
        lambdaUpdate().in(SoB2cDeliveryEntity::getId,deliveryIdList)
                .set(SoB2cDeliveryEntity::getStatus,status)
                .update();
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
    public void rollbackInventory(List<String> ids) {
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listByIds(ids);

        //修改状态为取消发货
        this.updateStatus(ids, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());

        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.SO_B2C_DELIVERY, ids);

        //回滚虚拟仓库存
        virtualInventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //回滚实体仓库存
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

        Boolean flag = lambdaUpdate()
                .set(SoB2cDeliveryEntity::getStatus, status)
                .in(SoB2cDeliveryEntity::getId, ids)
                .ne(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode())
                .update();
        return flag;
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
        String msg = StrUtil.format("用户【{}】更新重量为{} ", UserContext.getDefaultLoginUser().getUserName(),dto.getWeight()+dto.getWeightUnit());
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
        if (SoB2cDeliveryStatusEnum.notPrint().contains(soB2cDeliveryEntity.getStatus())) {
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
    public Boolean mergePackageDelivery(List<String> soIdList) {

        //记录需要手动标发的订单id
        List<String> deliverySoIdList = new ArrayList<>();
        for (String soId : soIdList) {
            Boolean flag = soB2cFeign.checkPlatformShipOrder(soId);
            if (flag) {
                deliverySoIdList.add(soId);
            }
        }
        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntityList = this.listBySourceIds(deliverySoIdList);
        //过滤掉取消发货
        deliveryEntityList = deliveryEntityList.stream().filter(v->!SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(v.getStatus())).collect(Collectors.toList());

        List<String> soDeliveryIds = deliveryEntityList.stream().map(req -> req.getId()).collect(Collectors.toList());
        //调用第三方平台SDK发货
        this.falseDeliveryBatch(soDeliveryIds);

        //查询发货单
        List<SoB2cDeliveryEntity> deliveryEntities = this.listBySourceIds(soIdList);
        //过滤掉取消发货
        deliveryEntities = deliveryEntities.stream().filter(v->!SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(v.getStatus())).collect(Collectors.toList());

        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();

        //将发货状态更新为已发货
        for (SoB2cDeliveryEntity deliveryEntity : deliveryEntities) {
            deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
            deliveryEntity.setDeliveryTime(deliveryTime);
        }

        //修改订单状态已发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(soIdList);
        List<SoB2cDTO.SoDeliveryDTO> soDeliveryDTOList = new ArrayList<>();
        for (String soId : soIdList) {
            SoB2cDeliveryEntity soB2cDeliveryEntity = deliveryEntityList.stream().filter(v->v.getSourceId().equals(soId)).findFirst().orElse(new SoB2cDeliveryEntity());
            soDeliveryDTOList.add(new SoB2cDTO.SoDeliveryDTO(soId,soB2cDeliveryEntity.getCode()));
        }
        updateDeliveryTimeDTO.setSoDeliveryDTOList(soDeliveryDTOList);
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(LocalDateTime.now());
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);

        //将发货状态更新为已发货
        if (!this.updateBatchById(deliveryEntities)) {
            throw new ServiceException("发货单更新失败");
        }

        for (SoB2cDeliveryEntity deliveryEntity : deliveryEntities) {
            //出库
            this.generateB2cSoOutstock(deliveryEntity);
            String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", UserContext.getDefaultLoginUser().getUserName(), "组包称重", deliveryEntity.getCode());
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
            waybillDTO.setDisabled(Boolean.FALSE);
            //打印类型
            waybillDTO.setPrintType(param.getPrintType());

            //查询是否允许打印面单和配货单
            LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthByChannelId(logisticsChannelId);
            if (ObjectUtil.isEmpty(authDTO)) {
                waybillDTO.setErrorMsg(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST.msg);
                waybillDTO.setDisabled(Boolean.TRUE);
            }

            // 配货单需要根据渠道查询是否是自定义配置，自定义配置需要组装数据
            LogisticsPrintTypeDTO.ViewDTO logisticsPrintTypeEntity = logisticsPrintTypeEntities.stream()
                    .filter(req -> LogisticsPrintTypeEnum.ALLOCATE_CARGO_BILL.getCode().equals(req.getPrintType())
                            && req.getLogisticsChannelId().equals(deliveryEntities.get(0).getLogisticsChannelId())
                    ).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(logisticsPrintTypeEntity) && !param.getPrintType().equals(SoB2cDeliveryPrintTypeEnum.LOGISTICS_BILL.getCode())) {
                waybillDTO.setErrorMsg(StrUtil.format(ApiError.LOGISTICS_PRINT_TYPE_SETTING_NOT_EXIST.msg, deliveryEntities.get(0).getLogisticsChannelName()));
                waybillDTO.setDisabled(Boolean.TRUE);
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
                    if (ObjectUtil.isNotEmpty(logisticsPrintTypeEntity) && LogisticsLabelTypeEnum.AUTHORITY.getCode().equals(logisticsPrintTypeEntity.getLabelType())) {
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

                //匹配订单
                SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(deliveryEntity.getSourceId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                    if (soB2cEntity.getIsFrozen()) {
                        waybillDTO.setErrorMsg(StrUtil.format(ApiError.ORDER_IS_INTERCEPT_NOT_UPDATE.msg, soB2cEntity.getCode()));
                        waybillDTO.setDisabled(Boolean.TRUE);
                    }
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
    public boolean hasNotGenB2cSoOutStockAndLog(SoB2cEntity currentEntity) {
        Integer count = this.lambdaQuery()
                .eq(SoB2cDeliveryEntity::getSourceId, currentEntity.getId())
                .eq(SoB2cDeliveryEntity::getSourceCode, currentEntity.getCode())
                .eq(SoB2cDeliveryEntity::getStatus, SoB2cDeliveryStatusEnum.SHIPPED.getCode())
                .count();
        if (count > 0){
            return false;
        }
        if (currentEntity.hasPlatformWarehouseOrder()){
            return false;
        }
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(currentEntity.getId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsList)){
            // 无物流信息
            String msg = StrUtil.format("soId={}, 无物流信息", currentEntity.getId());
            throw new ServiceException(msg);
        }
        SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsList.get(0);
        // 无发货单判断是否海外仓发货
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(logisticsEntity.getLogisticsChannelId());
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }
        // 如果是API对接的海外仓忽略发货单为空拦截
        LogisticsPlatformEnum platformEnum = LogisticsPlatformEnum.getByCode(auth.getLogisticsPlatform());
        if (LogisticsPlatformEnum.GOOD_CANG.equals(platformEnum) || LogisticsPlatformEnum.IML.equals(platformEnum)) {
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

    @Override
    public Boolean shipOrder(PlatformShipOrderDTO platformShipOrderDTO) {
        //调用第三方平台SDK发货
//        PlatformSaveHandler.shipOrder(platformShipOrderDTO);
        // 调用第三方平台SDK标记发货(独立事务)
        String businessDesc = "Feign标记发货";
        asyncService.asyncShipOrder(platformShipOrderDTO.getSoB2cId(),
                platformShipOrderDTO.getSoB2cId(),
                platformShipOrderDTO.getDictPlatform(),
                platformShipOrderDTO.getSoB2cId(),
                platformShipOrderDTO.getSubmitPlatformUniqueKey(),
                businessDesc, platformShipOrderDTO.isFalseDeliveryFlag());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO generationWaves(SoB2cDeliveryDTO.GenerationWavesDTO dto) {
        List<SoB2cDeliveryEntity> b2cDelivery = listByIds(dto.getIds());
        boolean match = b2cDelivery.stream().allMatch(v -> SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(v.getStatus()));
        List<String> soIds = b2cDelivery.stream().map(SoB2cDeliveryEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        boolean isIntercept = soB2cEntities.stream().anyMatch(SoB2cEntity::getIsIntercept);
        if (Boolean.FALSE.equals(match) || Boolean.TRUE.equals(isIntercept)) {
            throw new ServiceException(ApiError.ERROR_99116);
        }
        List<SoB2cDeliveryDetailEntity> detailList = soB2cDeliveryDetailService.listByMainIds(dto.getIds());
        for (SoB2cDeliveryEntity entity : b2cDelivery) {
            List<SoB2cDeliveryDetailEntity> detailEntities = detailList.stream().filter(v -> v.getMainId().equals(entity.getId())).collect(Collectors.toList());
            try {
                generatePickingDetail(entity, detailEntities);
            } catch (Exception e) {
                generateReplenish(detailEntities, entity);
                //生成拣货单失败，发货单生成异常
                updateAbnormal(Collections.singletonList(entity.getId()), AbnormalCauseEnum.GENERATION_WAVE);
            }
        }
        WaveListDTO.AddDTO addDTO = new WaveListDTO.AddDTO();
        addDTO.setPickCartTypeId(dto.getPickingCartTypeId());
        addDTO.setDeliveryIdList(dto.getIds());
        addDTO.setPickingType(dto.getPickingType());
        addDTO.setName("手动生成波次");
        addDTO.setWaveType(PickingWaveTypeEnum.MIXED_WAVE.getCode());
        return waveListService.add(addDTO);
    }

    private void generateReplenish (List<SoB2cDeliveryDetailEntity> detailList,SoB2cDeliveryEntity deliveryEntity) {
        //根据sku、仓库合并生成数据
        Map<String, List<SoB2cDeliveryDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId())));
        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : map.entrySet()) {
            SoB2cDeliveryDetailEntity detailEntity = entry.getValue().get(0);

            WarehouseLocationReplenishDTO.AddDTO addReplenishDTO = new WarehouseLocationReplenishDTO.AddDTO();
            addReplenishDTO.setSkuId(detailEntity.getSkuId());
            addReplenishDTO.setSkuNo(detailEntity.getSkuNo());
            addReplenishDTO.setSourceId(deliveryEntity.getId());
            addReplenishDTO.setSourceCode(deliveryEntity.getCode());
            addReplenishDTO.setWarehouseId(detailEntity.getWarehouseId());
            addReplenishDTO.setSourceType(ReplenishTypeEnum.DELIVER_STOCK_OUT);
            //合计数量
            Integer qty = entry.getValue().stream().map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            addReplenishDTO.setQty(qty);
            warehouseLocationReplenishService.add(addReplenishDTO);
        }
    }

    @Override
    public void generatePickingDetail(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities) {
        List<String> skuIds = soB2cDeliveryDetailEntities.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> childSkuIds = bomChildrenSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
        skuIds.addAll(childSkuIds);
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> detailList = new ArrayList<>();
        Map<String, String> sourceDetailMap = new HashMap<>();
        Map<String, String> warehouseMap = soB2cDeliveryDetailEntities.stream().collect(Collectors.toMap(SoB2cDeliveryDetailEntity::getWarehouseId, SoB2cDeliveryDetailEntity::getWarehouseName, (o1, o2) -> o1));
        for (SoB2cDeliveryDetailEntity detailEntity : soB2cDeliveryDetailEntities) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    detailList.add(new CfgRulePickingDTO.CfgExecutionDataDetailDTO(detailEntity.getWarehouseId(), bomChildrenSkuDTO.getSkuId(), bomChildrenSkuDTO.getSkuNo(),
                            detailEntity.getDeliveryQty() * bomChildrenSkuDTO.getQuantity()));
                    if (ObjectUtil.isEmpty(sourceDetailMap.get(bomChildrenSkuDTO.getSkuId()))) {
                        sourceDetailMap.put(bomChildrenSkuDTO.getSkuId(), detailEntity.getId());
                    }
                }
            } else {
                detailList.add(new CfgRulePickingDTO.CfgExecutionDataDetailDTO(detailEntity.getWarehouseId(), detailEntity.getSkuId(), detailEntity.getSkuNo(),
                        detailEntity.getDeliveryQty()));
                if (ObjectUtil.isEmpty(sourceDetailMap.get(detailEntity.getSkuId()))) {
                    sourceDetailMap.put(detailEntity.getSkuId(), detailEntity.getId());
                }
            }
        }
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(PickingBillTypeEnum.B2C.getCode());
        executionData.setDetails(detailList);
        pickingListsService.generateSoB2cPicking(soB2cDeliveryEntity, executionData, warehouseMap, sourceDetailMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO clearException(String id) {
        SoB2cDeliveryEntity entity = getById(id);
        checkDelivery(entity);
        entity.setStatus(SoB2cDeliveryStatusEnum.PICKING.getStatus());
        entity.setAbnormalCause("");
        updateById(entity);
        // 操作日志
        operateLogService.addModuleOperateLog("仓库清除异常", ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "清除异常");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "清除异常成功");
    }

    private static void checkDelivery(SoB2cDeliveryEntity entity) {
        if (!SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_99113);
        }
        if (AbnormalCauseEnum.GENERATION_WAVE.getCode().equals(entity.getAbnormalCause())){
            throw new ServiceException(ApiError.ERROR_99112);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelShipment(SoB2cDeliveryDTO.CancelShipmentDTO dto) {
        SoB2cDeliveryEntity entity = getById(dto.getId());
        checkDelivery(entity);
        //修改订单状态
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        soB2cEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        soB2cFeign.updateById(soB2cEntity);
        entity.setStatus(SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
        updateById(entity);
        //仓位库存由锁定退回
        changeInventory(dto, dto.getWarehouseLocation(), InventoryBusinessTypeEnum.SO_B2C_DELIVERY_CANCEL.getCode());
        changeInventory(dto, dto.getReturnWarehouseLocation(), InventoryBusinessTypeEnum.SO_B2C_DELIVERY_SHELVES.getCode());
        //删除拣货单
        pickingListsService.deleteBySourceId(Collections.singletonList(dto.getId()));
        // 操作日志
        operateLogService.addModuleOperateLog("仓库取消发货", ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "取消发货");
        OperateLogDTO.AddModuleOperateLogDTO operateLogDTO = new OperateLogDTO.AddModuleOperateLogDTO();
        operateLogDTO.setContent("仓库取消发货");
        operateLogDTO.setModuleType(ModuleTypeEnum.SO_B2C.getCode());
        operateLogDTO.setBusinessId(soB2cEntity.getId());
        operateLogDTO.setOperation("取消发货");
        soB2cFeign.addModuleOperateLog(operateLogDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消成功");
    }

    /**
     * 变更库存
     * @param dto 参数
     * @param warehouseLocation 仓位
     * @param businessType 业务类型
     */
    private void changeInventory(SoB2cDeliveryDTO.CancelShipmentDTO dto, String warehouseLocation, String businessType) {
        InOutStockDTO inOutStockDTO = new InOutStockDTO();
        inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY);
        inOutStockDTO.setSourceCode(dto.getCode());
        inOutStockDTO.setSourceId(dto.getId());
        inOutStockDTO.setSourceDetailId(dto.getDetailId());
        inOutStockDTO.setBillDate(LocalDate.now());
        inOutStockDTO.setSkuNo(dto.getSkuNo());
        inOutStockDTO.setSkuId(dto.getSkuId());
        inOutStockDTO.setQty(dto.getPickingQty());
        inOutStockDTO.setWarehouseId(dto.getWarehouseId());
        inOutStockDTO.setWarehouseLocation(warehouseLocation);
        //添加冻结库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(Collections.singletonList(inOutStockDTO));
        inventoryInOutStockDTO.setBusinessType(businessType);
        //更新库存
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    @Override
    public SoB2cDeliveryDTO.CancelShipmentView cancelShipmentView(List<String> ids) {
        List<SoB2cDeliveryDTO.CancelShipmentDTO> cancelShipments = baseMapper.cancelShipmentView(ids);
        List<String> warehouseIds = cancelShipments.stream().map(SoB2cDeliveryDTO.CancelShipmentDTO::getWarehouseId)
                .distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> locations = warehouseLocationService.listByWarehouseIds(warehouseIds);
        for (SoB2cDeliveryDTO.CancelShipmentDTO cancelShipment : cancelShipments) {
            WarehouseLocationEntity location = locations.stream()
                    .filter(e -> e.getWarehouseId().equals(cancelShipment.getWarehouseId()))
                    .filter(e -> e.getCode().equals(cancelShipment.getWarehouseLocation()))
                    .findFirst().orElse(new WarehouseLocationEntity());
            cancelShipment.setWarehouseLocationName(location.getName());
            cancelShipment.setReturnWarehouseLocationId(location.getId());
            cancelShipment.setReturnWarehouseLocation(cancelShipment.getWarehouseLocation());
            cancelShipment.setReturnWarehouseLocationName(cancelShipment.getWarehouseLocationName());
            cancelShipment.setReturnQty(cancelShipment.getPickingQty());
        }
        SoB2cDeliveryDTO.CancelShipmentView view = new SoB2cDeliveryDTO.CancelShipmentView();
        view.setCancelShipments(cancelShipments);
        return view;
    }

    @Override
    public List<SoB2cDeliveryEntity> listWaitHandle() {
        return lambdaQuery().eq(SoB2cDeliveryEntity::getStatus,SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode())
                .list();
    }

    /**
     * 根据销售订单编码查询
     * @author will
     * @date 2024/6/28 15:55
     * @param soCode
     * @return SoB2cDeliveryEntity
     */
    private SoB2cDeliveryEntity getBySoCode (String soCode) {
       return lambdaQuery().eq(SoB2cDeliveryEntity::getSoCode,soCode).last("limit 1").one();
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
            printLogisticsWaybill.setLogisticType(detailDTO.getLogisticType());

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
        List<String> skuIds = records.stream().map(SoB2cDeliveryDTO.ListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        //查询订单
        List<String> soIds = records.stream().map(SoB2cDeliveryDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIds);
        List<String> ids = records.stream().map(SoB2cDeliveryDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(ids);
        List<WaveListDTO.WaveDeliveryDTO> deliveryList = waveListService.listByDeliverIds(ids);
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
            }
            //波次号
            WaveListDTO.WaveDeliveryDTO dto = deliveryList.stream()
                    .filter(e -> e.getDeliveryId().equals(record.getId()))
                    .findFirst().orElse(new WaveListDTO.WaveDeliveryDTO());
            record.setWaveCode(dto.getWaveCode());
            String warehouseLocation = views.stream().filter(e -> e.getSkuId().equals(record.getSkuId()))
                    .map(PickingListsDTO.SourceView::getWarehouseLocation)
                    .distinct()
                    .collect(Collectors.joining(","));
            record.setWarehouseLocation(warehouseLocation);
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
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //单据状态中文
        data.setStatusName(SoB2cDeliveryStatusEnum.getName(data.getStatus()));
        //拣货类型中文
        data.setPickingTypeName(PickingTypeEnum.getName(data.getPickingType()));
        //详情字段设置
        List<SoB2cDeliveryDetailDTO.ViewDTO> viewDetailList = BeanMapper.copyList(detailList, SoB2cDeliveryDetailDTO.ViewDTO.class);
        List<PickingListsDTO.SourceView> views = pickingListsService.listBySourceIds(Collections.singletonList(data.getId()));
        for (SoB2cDeliveryDetailDTO.ViewDTO viewDTO : viewDetailList) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setSkuNo(skuVO.getSkuNo());
                viewDTO.setProductName(skuVO.getSkuName());
            }

            String warehouseLocation = views.stream().filter(e -> e.getSkuId().equals(viewDTO.getSkuId()))
                    .map(PickingListsDTO.SourceView::getWarehouseLocation)
                    .distinct()
                    .collect(Collectors.joining(","));
            viewDTO.setWarehouseLocation(warehouseLocation);
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
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuNoList);

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

        printWayBillPdf.setBasketNo(StringUtils.isNotBlank(printWayBillPdf.getBasketNo())?"#"+printWayBillPdf.getBasketNo():"");
        String orderTip = printWayBillPdf.getIsOutStock()?printWayBillPdf.getIsIntercept()?"缺货订单/拦截订单":"缺货订单":printWayBillPdf.getIsIntercept()?"拦截订单":"";
        printWayBillPdf.setOrderTip(orderTip);
        return printWayBillPdf;
    }


    /**
     * 同步发货单到DMP(暂不推送B2C发货单，用销售出库单代替)
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
        dmpMqFeign.saveTask(taskFeignDTO);
    }

    /**
     * 冻结虚拟库存
     * @author will
     * @date 2024/6/12 10:58
     * @param entity
     * @param soB2cDeliveryDetailList
     */
    private void freezeVirtualInventory (SoB2cDeliveryEntity entity,List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        for (SoB2cDeliveryDetailEntity detailEntity : soB2cDeliveryDetailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY);
            outInStockDTO.setSourceId(entity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setQty(detailEntity.getDeliveryQty());
            outInStockDTO.setWarehouseId(detailEntity.getWarehouseId());
            if (StrUtil.isBlank(detailEntity.getVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(detailEntity.getVirtualWarehouseId());
            paramList.add(outInStockDTO);
        }
        //无虚拟仓库不扣虚拟库存
        if (CollectionUtils.isEmpty(paramList)) {
            return;
        }
        //添加冻结库存
        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setParamList(paramList);
        dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode());
        //更新库存
        virtualInventoryTransCoreService.approve(dto);
    }
}
