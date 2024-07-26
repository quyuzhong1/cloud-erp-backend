package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.PickingListsMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 拣货单 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-06-07
 */
@Service
public class PickingListsServiceImpl extends SuperServiceImpl<PickingListsMapper, PickingListsEntity> implements PickingListsService {

    @Resource
    private PickingDetailService pickingDetailService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgRulePickingStagingService cfgRulePickingStagingService;
    @Resource
    private CfgRulePickingService cfgRulePickingService;

    @Resource
    @Lazy
    private RequisitionApplicationService requisitionApplicationService;
    @Resource
    @Lazy
    private SoDeliveryNoticeService soDeliveryNoticeService;
    @Resource
    private InventoryTransCoreService inventoryTransCoreService;
    @Resource
    private PackingTaskService packingTaskService;

    @Resource
    private WmsCartonService wmsCartonService;

    @Resource
    private WmsCartonDetailService wmsCartonDetailService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private SoInfoFeign soInfoFeign;

    @Override
    public PagingVO<PickingListsDTO.PagingView> paging(PagingDTO<PickingListsDTO.PagingParam> dto) {
        IPage<PickingListsDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public void add(PickingListsDTO.AddDTO dto) {
        generatePicking(dto);
        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        List<String> skuIds = dto.getDetails().stream().map(PickingDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIds);
        Map<String, String> warehouseMap = dto.getDetails().stream().collect(Collectors.toMap(PickingDetailDTO.AddDTO::getWarehouseId, PickingDetailDTO.AddDTO::getWarehouseName, (o1, o2) -> o1));
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> details = dto.getDetails().stream()
                .map(v -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(v.getWarehouseId(), v.getSkuId(), v.getSkuNo(), v.getQty(), v.getSourceDetailId())).collect(Collectors.toList());
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(dto.getBillType());
        executionData.setCustomerId(dto.getCustomerId());
        executionData.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        executionData.setDetails(details);
        List<LocationInventoryResultDTO> results = cfgRulePickingService.getRuleOrderMatchResult(executionData);
        Map<String, List<LocationInventoryResultDTO>> warehouseResultMap = results.stream().collect(Collectors.groupingBy(LocationInventoryResultDTO::getWarehouseId));
        for (Map.Entry<String, List<LocationInventoryResultDTO>> result : warehouseResultMap.entrySet()) {
            // 生成拣货单主表数据
            PickingListsEntity entity = new PickingListsEntity();
            entity.setId(IdWorker.getIdStr());
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHD);
            entity.setCode(code);
            entity.setWarehouseId(result.getKey());
            entity.setWarehouseName(warehouseMap.get(result.getKey()));
            entity.setSourceId(dto.getSourceId());
            entity.setSourceCode(dto.getSourceCode());
            entity.setSourceType(dto.getSourceType());
            int skuTotal = result.getValue().stream().map(LocationInventoryResultDTO::getQuantity).reduce(0, Math::addExact);
            entity.setSkuTotal(skuTotal);
            List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList = new ArrayList<>();
            List<PickingDetailEntity> entities = new ArrayList<>();
            WarehouseLocationMoveDTO.AddDTO moveDto = new WarehouseLocationMoveDTO.AddDTO();
            for (LocationInventoryResultDTO resultDTO : result.getValue()) {
                // 获取仓库暂存区默认配置
                CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                        .filter(staging -> staging.getBillType().equals(dto.getBillType()))
                        .filter(staging -> staging.getWarehouseId().equals(resultDTO.getWarehouseId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                // 获取产品信息
                ProductDetailEntity productDetailEntity = detailEntityList.stream()
                        .filter(entityClass -> entityClass.getId().equals(resultDTO.getSkuId()))
                        .findFirst().orElse(new ProductDetailEntity());
                PickingDetailEntity detail = new PickingDetailEntity();
                detail.setMainId(entity.getId());
                detail.setSkuId(resultDTO.getSkuId());
                detail.setSkuNo(resultDTO.getSkuNo());
                detail.setQty(resultDTO.getQuantity());
                detail.setUnit(productDetailEntity.getUnitName());
                detail.setWarehouseLocation(resultDTO.getWarehouseLocation());
                detail.setSourceDetailId(resultDTO.getSourceDetailId());
                detail.setStagingLocation(pickingStaging.getWarehouseLocation());
                entities.add(detail);
                moveDto.setWarehouseId(resultDTO.getWarehouseId());
                moveDetailList.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detail.getSkuId(), detail.getSkuNo(),
                        detail.getWarehouseLocation(), detail.getStagingLocation(), detail.getQty(), resultDTO.getWarehouseId()));
            }
            entity.setLocationTotal(entities.size());
            moveDto.setPcShow(true);
            moveDto.setDetailList(moveDetailList);
            ApplicationContextUtils.getBean(PickingListsServiceImpl.class).saveAddData(entity, entities, moveDto);
        }
    }

    private void generatePicking(PickingListsDTO.AddDTO dto) {
        List<PickingDetailDTO.AddDTO> detailList = new ArrayList<>();
        List<String> skuIds = dto.getDetails().stream().map(PickingDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        for (PickingDetailDTO.AddDTO detail : dto.getDetails()) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detail.getSkuId())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    PickingDetailDTO.AddDTO detailAdd = PickingDetailDTO.AddDTO.getAddDTO(detail, bomChildrenSkuDTO.getSkuId(), bomChildrenSkuDTO.getSkuNo(), detail.getQty() * bomChildrenSkuDTO.getQuantity());
                    detailList.add(detailAdd);
                }
            } else {
                PickingDetailDTO.AddDTO detailAdd = PickingDetailDTO.AddDTO.getAddDTO(detail, detail.getSkuId(), detail.getSkuNo(), detail.getQty());
                detailList.add(detailAdd);
            }
        }
        dto.setDetails(detailList);
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void saveAddData(PickingListsEntity entity, List<PickingDetailEntity> entities, WarehouseLocationMoveDTO.AddDTO moveDto) {
        save(entity);
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("生成拣货单【{}】", entity.getCode()), ModuleTypeEnum.PICKING_LISTS.getCode(), entity.getId(), "新增操作");
        pickingDetailService.saveBatch(entities);
        warehouseLocationMoveService.addAndApprove(moveDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        PickingListsEntity entity = getById(id);
        checkStatus(entity);
        // 删除拣货单，进行仓位反向移动
        List<PickingDetailEntity> entityList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().eq(PickingDetailEntity::getMainId, id));
        WarehouseLocationMoveDTO.AddDTO dto = new WarehouseLocationMoveDTO.AddDTO();
        dto.setPcShow(false);
        dto.setWarehouseId(entity.getWarehouseId());
        List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList = entityList.stream()
                .map(detail -> {
                    WarehouseLocationMoveDetailDTO.AddDTO moveDetail = new WarehouseLocationMoveDetailDTO.AddDTO();
                    moveDetail.setSkuId(detail.getSkuId());
                    moveDetail.setSkuNo(detail.getSkuNo());
                    moveDetail.setWarehouseId(entity.getWarehouseId());
                    moveDetail.setOutWarehouseLocation(detail.getStagingLocation());
                    moveDetail.setInWarehouseLocation(detail.getWarehouseLocation());
                    moveDetail.setQty(detail.getQty());
                    return moveDetail;
                }).collect(Collectors.toList());
        dto.setDetailList(moveDetailList);
        baseMapper.deleteById(id);
        warehouseLocationMoveService.addAndApprove(dto);
        pickingDetailService.remove(Wrappers.<PickingDetailEntity>lambdaQuery()
                .eq(PickingDetailEntity::getMainId, id));
        List<String> sourceDetailIds = entityList.stream().map(PickingDetailEntity::getSourceDetailId).collect(Collectors.toList());
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            // 反写要货申请的拣货数量
            requisitionApplicationService.writeBackData(sourceDetailIds);
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            soDeliveryNoticeService.writeBackData(sourceDetailIds);
        }
    }

    private void checkStatus(PickingListsEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            //要货申请下推发货单后，拣货单不允许修改和删除
            int count = firstMileDeliveryService.countNotVoided(entity.getSourceId());
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99086);
            }
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            //销售通知单下推销售出库单后，拣货单不允许修改和删除
            int count = soOutstockService.countNotVoided(entity.getSourceId());
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99087);
            }
        }
    }

    @Override
    public PickingListsDTO.View view(String id) {
        PickingListsEntity entity = getById(id);
        List<WarehouseLocationEntity> locationAndAreaList = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getWarehouseId()));
        Map<String, String> locationMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, WarehouseLocationEntity::getParentId, (o1, o2) -> o1));
        Map<String, String> areaMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName, (o1, o2) -> o1));
        PickingListsDTO.View view = BeanMapperUtils.map(PickingListsDTO.View.class, entity);
        List<PickingDetailEntity> pickingDetails = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().eq(PickingDetailEntity::getMainId, id));
        List<PickingDetailDTO.View> detailList = pickingDetails.stream()
                .map(detail -> {
                    WarehouseLocationEntity location = locationAndAreaList.stream()
                            .filter(v -> v.getCode().equals(detail.getWarehouseLocation()))
                            .filter(v -> v.getWarehouseId().equals(entity.getWarehouseId()))
                            .findFirst()
                            .orElse(new WarehouseLocationEntity());
                    WarehouseLocationEntity stagingLocation = locationAndAreaList.stream()
                            .filter(v -> v.getCode().equals(detail.getStagingLocation()))
                            .filter(v -> v.getWarehouseId().equals(entity.getWarehouseId()))
                            .findFirst()
                            .orElse(new WarehouseLocationEntity());
                    PickingDetailDTO.View detailView = BeanMapperUtils.map(PickingDetailDTO.View.class, detail);
                    detailView.setWarehouseLocationId(location.getId());
                    detailView.setWarehouseLocationName(location.getName());
                    detailView.setStagingLocationName(stagingLocation.getName());
                    detailView.setWarehouseAreaId(locationMap.get(detailView.getWarehouseLocation()));
                    detailView.setWarehouseAreaName(areaMap.get(locationMap.get(detailView.getWarehouseLocation())));
                    detailView.setStagingAreaName(areaMap.get(locationMap.get(detailView.getStagingLocation())));
                    return detailView;
                }).collect(Collectors.toList());
        view.setDetails(detailList);
        return view;
    }

    @Override
    public void export(PickingListsDTO.ExportDTO dto, HttpServletResponse response) {
        List<PickingListsDTO.ExportInfoDTO> list = baseMapper.exportInfo(dto);
        List<String> warehouseIds = list.stream()
                .map(PickingListsDTO.ExportInfoDTO::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        List<WarehouseLocationEntity> locationAndAreaList = warehouseLocationService.listByWarehouseIds(warehouseIds);
        Map<String, String> locationMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(v -> v.getWarehouseId() + ":" + v.getCode(), WarehouseLocationEntity::getParentId));
        Map<String, String> areaMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));
        for (PickingListsDTO.ExportInfoDTO infoDTO : list) {
            infoDTO.setWarehouseAreaName(areaMap.get(locationMap.get(infoDTO.getWarehouseId() + ":" + infoDTO.getWarehouseLocation())));
            infoDTO.setStagingAreaName(areaMap.get(locationMap.get(infoDTO.getWarehouseId() + ":" + infoDTO.getStagingLocation())));
        }
        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/pickingLists.xlsx";
        String name = "拣货单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("拣货单导出出错 {}", e);
        }
    }

    @Override
    public List<PickingListsDTO.PrintView> print(List<String> ids) {
        LoginUser user = UserContext.getDefaultLoginUser();
        List<PickingListsEntity> pickingLists = listByIds(ids);
        if (CollectionUtils.isEmpty(pickingLists)) {
            throw new ServiceException(ApiError.ERROR_92258);
        }
        List<PickingDetailEntity> detailList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, ids));
        List<String> skuIds = detailList.stream().map(PickingDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<PickingListsDTO.PrintView> printViews = new ArrayList<>();
        List<String> sourceIds = pickingLists.stream().map(PickingListsEntity::getSourceId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationEntity> applicationEntities = requisitionApplicationService.listByIds(sourceIds);
        List<RequisitionApplicationDetailEntity> applicationDetails = requisitionApplicationDetailService.listByMainIds(sourceIds);
        List<SoDeliveryNoticeEntity> noticeEntities = soDeliveryNoticeService.listByIds(sourceIds);
        List<String> soIds = noticeEntities.stream().map(SoDeliveryNoticeEntity::getSourceId).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(soIds)) {
            soInfos = soInfoFeign.listSoInfoByIds(soIds);
        }
        for (PickingListsEntity picking : pickingLists) {
            PickingListsDTO.PrintView printView = new PickingListsDTO.PrintView();
            printView.setPrintTime(LocalDateTime.now());
            printView.setPrintUserName(user.getUserName());
            if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(picking.getSourceType())) {
                RequisitionApplicationEntity application = applicationEntities.stream().filter(v -> v.getId().equals(picking.getSourceId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION));
                printView.setCode(application.getCode());
                printView.setChannelName(application.getChannelName());
                printView.setHandlingUserName(application.getCreateUserName());
            } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(picking.getSourceType())) {
                SoDeliveryNoticeEntity soDeliveryNotice = noticeEntities.stream().filter(v -> v.getId().equals(picking.getSourceId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_SO_DELIVERY_NOTICE_NOT_EXIST));
                SoInfoEntity soInfo = soInfos.stream().filter(v -> v.getId().equals(soDeliveryNotice.getSourceId()))
                        .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_92016));
                printView.setCode(soDeliveryNotice.getSourceCode());
                printView.setChannelName(soDeliveryNotice.getCustomerName());
                printView.setHandlingUserName(soInfo.getCreateUserName());
            }
            List<PickingListsDTO.PrintDetailView> views = detailList.stream().map(detail -> {
                PickingListsEntity entity = pickingLists.stream()
                        .filter(p -> p.getId().equals(detail.getMainId()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException(ApiError.ERROR_400));
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream()
                        .filter(req -> req.getSkuId().equals(detail.getSkuId()))
                        .distinct().findFirst().orElse(new SkuVO());
                PickingListsDTO.PrintDetailView view = new PickingListsDTO.PrintDetailView();
                view.getPrintView(entity, detail, skuVO.getSkuName());
                if (ObjectUtil.isEmpty(view.getWarehouseLocation())) {
                    view.setWarehouseLocation(skuVO.getWarehouseLocationLarge());
                }
                if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(picking.getSourceType())) {
                    RequisitionApplicationEntity application = applicationEntities.stream().filter(v -> v.getId().equals(picking.getSourceId()))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION));
                    if (RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType())) {
                        RequisitionApplicationDetailEntity applicationDetail = applicationDetails.stream().filter(v -> v.getId().equals(detail.getSourceDetailId()))
                                .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION));
                        view.setThirdSku(applicationDetail.getPlatformFnSku());
                    }
                }
                return view;
            }).collect(Collectors.toList());
            List<PickingListsDTO.PrintDetailView> viewList = new ArrayList<>(views.stream().collect(Collectors.groupingBy(v -> v.getSkuNo() + ":" + v.getWarehouseId() + ":" + v.getWarehouseLocation(),
                    Collectors.collectingAndThen(Collectors.toList(), v -> {
                        PickingListsDTO.PrintDetailView view = v.get(0);
                        int totalQuantity = v.stream().mapToInt(PickingListsDTO.PrintDetailView::getPickingQty).sum();
                        view.setPickingQty(totalQuantity);
                        return view;
                    }))).values()).stream().sorted(Comparator.comparing(PickingListsDTO.PrintDetailView::getWarehouseId)
                    .thenComparing(PickingListsDTO.PrintDetailView::getWarehouseLocation)
                    .thenComparing(PickingListsDTO.PrintDetailView::getSkuNo)).collect(Collectors.toList());

            printView.setPrintDetailViews(viewList);
            printViews.add(printView);
        }
        return printViews;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PickingListsDTO.UpdateDTO dto) {
        PickingListsEntity entity = getById(dto.getId());
        checkStatus(entity);
        checkCombination(entity, dto);
        List<PickingDetailEntity> detailList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().eq(PickingDetailEntity::getMainId, dto.getId()));
        List<String> skuIds = dto.getDetails()
                .stream().map(PickingDetailDTO.View::getSkuId)
                .distinct().collect(Collectors.toList());
        this.checkPickingQty(entity, dto.getDetails());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIds);
        WarehouseLocationMoveDTO.AddDTO moveDto = new WarehouseLocationMoveDTO.AddDTO();
        moveDto.setWarehouseId(entity.getWarehouseId());
        moveDto.setPcShow(true);
        List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS = new ArrayList<>();
        // 获取新增的数据
        handlerAddData(dto, detailList, detailEntityList, entity, addDTOS);
        // 获取删除的数据
        handlerRemoveData(dto, detailList, addDTOS, entity);
        // 获取有差异的修改数据
        handlerUpdateData(dto, detailList, addDTOS, entity);
        int qty = dto.getDetails().stream()
                .map(PickingDetailDTO.View::getQty)
                .reduce(0, Math::addExact);
        entity.setSkuTotal(qty);
        entity.setLocationTotal(dto.getDetails().size());
        updateById(entity);
        // 进行对应的仓位移动
        if (!CollectionUtils.isEmpty(addDTOS)) {
            moveDto.setDetailList(addDTOS);
            warehouseLocationMoveService.addAndApprove(moveDto);
        }
        List<String> sourceDetailIds = detailList.stream().map(PickingDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            // 反写要货申请的拣货数量
            requisitionApplicationService.writeBackData(sourceDetailIds);
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            soDeliveryNoticeService.writeBackData(sourceDetailIds);
        }

        //判断虚拟库存是否足够,多添少不补(出库时统一扣减多余冻结)
        handleVirtualInventoryQty(entity);
    }

    /**
     * 处理虚拟仓数量
     * @author will
     * @date 2024/7/25 21:38
     * @param entity
     */
    private void handleVirtualInventoryQty (PickingListsEntity entity) {
        if (ObjectUtil.isEmpty(entity) || !StrUtil.equals(entity.getSourceType(),SourceTypeEnum.REQUISITION_APPLICATION.getCode())) {
           return;
        }

        //关联要货申请
        RequisitionApplicationEntity applicationEntity = requisitionApplicationService.getById(entity.getSourceId());
        if (ObjectUtil.isEmpty(applicationEntity)) {
            throw new ServiceException(StrUtil.format("拣货单【{}】关联的要货申请未找到",entity.getCode()));
        }
        List<RequisitionApplicationDetailEntity> applicationDetailList = requisitionApplicationDetailService.listByMainIds(Arrays.asList(applicationEntity.getId()));
        if (ObjectUtil.isEmpty(applicationEntity)) {
            throw new ServiceException(StrUtil.format("拣货单【{}】关联的要货申请明细未找到",entity.getCode()));
        }
        List<String> applicationDetailIdList = applicationDetailList.stream().map(RequisitionApplicationDetailEntity::getId).collect(Collectors.toList());
        List<PickingDetailEntity> pickingDetailEntityList = pickingDetailService.listPickingDetailBySourceDetailIds(applicationDetailIdList);
        //添加
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();

        for (RequisitionApplicationDetailEntity applicationDetailEntity : applicationDetailList) {

            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(applicationEntity.getId());
            outInStockDTO.setSourceCode(applicationEntity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.REQUISITION_APPLICATION);
            outInStockDTO.setSourceDetailId(applicationDetailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(applicationDetailEntity.getSkuId());
            outInStockDTO.setSkuNo(applicationDetailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
            if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
            //拣货数据
            List<PickingDetailEntity> pickingDetailList = pickingDetailEntityList.stream().filter(obj -> StrUtil.equals(obj.getSourceDetailId(), applicationDetailEntity.getId())).collect(Collectors.toList());
            Integer totalQty = pickingDetailList.stream().map(PickingDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer pickingQty = applicationDetailEntity.getPickingQty();
            if (MathUtil.compareTo(totalQty,pickingQty) > MathUtil.ZERO) {
                outInStockDTO.setQty(totalQty - pickingQty);
                addList.add(outInStockDTO);
                continue;
            }
        }
        if (CollectionUtils.isEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
    }

    private void checkCombination(PickingListsEntity entity, PickingListsDTO.UpdateDTO dto) {

        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            List<RequisitionApplicationDetailEntity> detailEntities = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(entity.getSourceId()));
            List<String> skuIds = detailEntities.stream().map(RequisitionApplicationDetailEntity::getSkuId).collect(Collectors.toList());
            //获取子SKU集合
            List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
            for (RequisitionApplicationDetailEntity detailEntity : detailEntities) {
                checkProportion(dto, detailEntity.getSkuId(), detailEntity.getSkuNo(), detailEntity.getId(), bomChildrenSkuList);

            }
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainId(entity.getSourceId());
            List<String> skuIds = noticeDetailEntities.stream().map(SoDeliveryNoticeDetailEntity::getSkuId).collect(Collectors.toList());
            //获取子SKU集合
            List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
            for (SoDeliveryNoticeDetailEntity detailEntity : noticeDetailEntities) {
                checkProportion(dto, detailEntity.getSkuId(), detailEntity.getSkuNo(), detailEntity.getId(), bomChildrenSkuList);
            }
        }

    }

    private static void checkProportion(PickingListsDTO.UpdateDTO dto, String skuId, String skuNo, String detailId, List<BomChildrenSkuDTO> bomChildrenSkuList) {
        List<BomChildrenSkuDTO> bomChildren = bomChildrenSkuList.stream()
                .filter(req -> req.getParentSkuId().equals(skuId)
                        && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(bomChildren)) {
            Map<String, Integer> skuMap = dto.getDetails().stream().filter(v -> v.getSourceDetailId().equals(detailId))
                    .collect(Collectors.toMap(PickingDetailDTO.View::getSkuNo, PickingDetailDTO.View::getQty, Integer::sum));
            //计算比例
            int proportion = Optional.ofNullable(skuMap.get(bomChildren.get(0).getSkuNo())).orElse(0) / bomChildren.get(0).getQuantity();
            for (BomChildrenSkuDTO bomChild : bomChildren) {
                int temp = Optional.ofNullable(skuMap.get(bomChild.getSkuNo())).orElse(0) / bomChild.getQuantity();
                if (proportion != temp) {
                    throw new ServiceException(ApiError.ERROR_99128, String.join(",", skuMap.keySet()));
                }
            }
        }
    }

    private void checkPickingQty(PickingListsEntity entity, List<PickingDetailDTO.View> detailList) {
        PackingTaskEntity packingTaskEntity = null;
        if (entity.getSourceType().equals(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            packingTaskEntity = packingTaskService.getBySourceCode(entity.getSourceCode());
        } else if (entity.getSourceType().equals(SourceTypeEnum.REQUISITION_APPLICATION.getCode())) {
            List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listBySourceIds(Arrays.asList(entity.getSourceId()));
            if (!CollectionUtils.isEmpty(firstMileDeliveryEntityList)) {
                FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.get(0);
                packingTaskEntity = packingTaskService.getBySourceCode(firstMileDeliveryEntity.getSourceCode());
            }
        } else {
            return;
        }
        if (Objects.isNull(packingTaskEntity)) {
            return;
        }
        List<WmsCartonEntity> wmsCartonEntityList = wmsCartonService.listByTaskIds(Arrays.asList(packingTaskEntity.getId()));
        if (CollectionUtils.isEmpty(wmsCartonEntityList)) {
            return;
        }
        List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByMainIds(wmsCartonEntityList.stream().map(v -> v.getId()).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(wmsCartonDetailEntityList)) {
            return;
        }
        Map<String, Integer> packingQtyMap = wmsCartonDetailEntityList.stream().collect(Collectors.toMap(WmsCartonDetailEntity::getSkuNo, WmsCartonDetailEntity::getPackQty, Integer::sum));
        Map<String, Integer> pickingQtyMap = detailList.stream().collect(Collectors.toMap(PickingDetailDTO.View::getSkuNo, PickingDetailDTO.View::getQty, Integer::sum));

        pickingQtyMap.forEach((skuNo, qty) -> {
            Integer packingQty = packingQtyMap.get(skuNo);
            if (Objects.nonNull(packingQty) && qty < packingQty) {
                throw new ServiceException(StrUtil.format("sku【{}】编辑数量校验不可小于装箱数量{}", skuNo, packingQty));
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySourceId(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<PickingListsEntity> list = list(Wrappers.<PickingListsEntity>lambdaQuery().in(PickingListsEntity::getSourceId, ids));
        List<String> idList = list.stream()
                .map(PickingListsEntity::getId)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(idList)) {
            removeByIds(idList);
            pickingDetailService.remove(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, idList));
        }
    }

    @Override
    public List<PickingListsDTO.SourceView> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listBySourceIds(sourceIds);
    }

    @Override
    public void exist(String id) {
        int count = count(Wrappers.<PickingListsEntity>lambdaQuery().eq(PickingListsEntity::getSourceId, id));
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99102);
        }
    }

    @Override
    public void exist(List<String> ids) {
        int count = count(Wrappers.<PickingListsEntity>lambdaQuery().in(PickingListsEntity::getSourceId, ids));
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99102);
        }
    }

    @Override
    public void generateSoB2cPicking(SoB2cDeliveryEntity soB2cDeliveryEntity, CfgRulePickingDTO.CfgExecutionDataDTO executionData, Map<String, String> warehouseMap, List<LocationInventoryResultDTO> results) {
        List<String> skuIdList = executionData.getDetails().stream().map(CfgRulePickingDTO.CfgExecutionDataDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        if (CollectionUtils.isEmpty(results)) {
            results = cfgRulePickingService.getRuleOrderMatchResult(executionData);
        }
        Map<String, List<LocationInventoryResultDTO>> resultMap = results.stream().collect(Collectors.groupingBy(LocationInventoryResultDTO::getWarehouseId));
        for (Map.Entry<String, List<LocationInventoryResultDTO>> entry : resultMap.entrySet()) {
            PickingListsEntity entity = new PickingListsEntity();
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHD);
            // 生成拣货单主表数据
            entity.setId(IdWorker.getIdStr());
            entity.setCode(code);
            entity.setWarehouseId(entry.getKey());
            entity.setWarehouseName(warehouseMap.get(entry.getKey()));
            entity.setSourceId(soB2cDeliveryEntity.getId());
            entity.setSourceCode(soB2cDeliveryEntity.getCode());
            entity.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
            int skuTotal = entry.getValue().stream().map(LocationInventoryResultDTO::getQuantity).reduce(0, Math::addExact);
            entity.setSkuTotal(skuTotal);
            List<PickingDetailEntity> entities = new ArrayList<>();
            List<InOutStockDTO> inOutStockList = new ArrayList<>();
            for (LocationInventoryResultDTO result : entry.getValue()) {
                // 获取产品信息
                ProductDetailEntity productDetailEntity = detailEntityList.stream()
                        .filter(entityClass -> entityClass.getId().equals(result.getSkuId()))
                        .findFirst().orElse(new ProductDetailEntity());
                PickingDetailEntity detail = new PickingDetailEntity();
                detail.setSkuId(result.getSkuId());
                detail.setMainId(entity.getId());
                detail.setSkuNo(result.getSkuNo());
                detail.setUnit(productDetailEntity.getUnitName());
                detail.setQty(result.getQuantity());
                detail.setWarehouseLocation(result.getWarehouseLocation());
                detail.setSourceDetailId(result.getSourceDetailId());
                entities.add(detail);
                InOutStockDTO inOutStockDTO = new InOutStockDTO();
                inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY);
                inOutStockDTO.setSourceId(soB2cDeliveryEntity.getId());
                inOutStockDTO.setSourceCode(soB2cDeliveryEntity.getCode());
                inOutStockDTO.setSourceDetailId(detail.getSourceDetailId());
                inOutStockDTO.setSkuNo(detail.getSkuNo());
                inOutStockDTO.setBillDate(LocalDate.now());
                inOutStockDTO.setSkuId(detail.getSkuId());
                inOutStockDTO.setQty(detail.getQty());
                inOutStockDTO.setWarehouseId(entity.getWarehouseId());
                inOutStockDTO.setWarehouseLocation(detail.getWarehouseLocation());
                inOutStockList.add(inOutStockDTO);
            }
            entity.setLocationTotal(entities.size());
            //添加冻结库存
            InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
            inventoryInOutStockDTO.setParamList(inOutStockList);
            inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode());
            //更新库存
            inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
            save(entity);
            pickingDetailService.saveBatch(entities);
        }
    }

    @Override
    public List<PickingListsDTO.DetailPickDTO> listDetailBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        //B2B
        List<PickingListsDTO.DetailPickDTO> detailB2BPickDTOS = baseMapper.listB2BDetailBySourceIds(sourceIds);
        //头程
        List<PickingListsDTO.DetailPickDTO> detailRequitPickDTOS = baseMapper.listRequitDetailBySourceIds(sourceIds);
        //合并集合
        return Stream.concat(detailB2BPickDTOS.stream(), detailRequitPickDTOS.stream()).collect(Collectors.toList());
    }

    /**
     * 处理编辑的数据
     *
     * @param dto        请求参数
     * @param detailList 拣货明细
     * @param entity     拣货单
     * @param addDTOS    仓位移动
     */
    private void handlerUpdateData(PickingListsDTO.UpdateDTO dto, List<PickingDetailEntity> detailList, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS, PickingListsEntity entity) {
        List<PickingDetailDTO.View> updateData = dto.getDetails()
                .stream()
                .filter(detail -> ObjectUtil.isNotEmpty(detail.getId()))
                .collect(Collectors.toList());
        List<PickingDetailEntity> updateList = new ArrayList<>();
        for (PickingDetailDTO.View view : updateData) {
            PickingDetailEntity detailEntity = detailList.stream()
                    .filter(v -> v.getId().equals(view.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_400));
            //处理仓位移动数据
            if (view.getWarehouseLocation().equals(detailEntity.getWarehouseLocation())) {
                if (detailEntity.getQty() > view.getQty()) {
                    addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detailEntity.getSkuId(), detailEntity.getSkuNo(),
                            detailEntity.getStagingLocation(), detailEntity.getWarehouseLocation(), detailEntity.getQty() - view.getQty(), entity.getWarehouseId()));
                } else if (detailEntity.getQty() < view.getQty()) {
                    //处理仓位移动数据
                    addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(view.getSkuId(), view.getSkuNo(),
                            view.getWarehouseLocation(), view.getStagingLocation(), view.getQty() - detailEntity.getQty(), entity.getWarehouseId()));
                } else {
                    continue;
                }
            } else {
                // 仓位变更了需要进行原数据仓位退回，新仓位移出
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(view.getSkuId(), view.getSkuNo(),
                        view.getWarehouseLocation(), view.getStagingLocation(), view.getQty(), entity.getWarehouseId()));
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detailEntity.getSkuId(), detailEntity.getSkuNo(),
                        detailEntity.getStagingLocation(), detailEntity.getWarehouseLocation(), detailEntity.getQty(), entity.getWarehouseId()));
            }
            String context = CharSequenceUtil.format("编辑了【{}】明细行,拣货仓位由【{}】变更为【{}】，数量由【{}】变更为【{}】", view.getSkuNo(),
                    detailEntity.getWarehouseLocation(), view.getWarehouseLocation(), detailEntity.getQty(), view.getQty());
            detailEntity.setWarehouseLocation(view.getWarehouseLocation());
            detailEntity.setQty(view.getQty());
            updateList.add(detailEntity);
            operateLogService.addModuleOperateLog(context, ModuleTypeEnum.PICKING_LISTS.getCode(), entity.getId(), "编辑操作");

        }
        if (!CollectionUtils.isEmpty(updateList)) {
            pickingDetailService.updateBatchById(updateList);
        }
    }

    /**
     * 处理删除的数据
     *
     * @param dto        请求参数
     * @param detailList 拣货明细
     * @param entity     拣货单
     * @param addDTOS    仓位移动
     */
    private void handlerRemoveData(PickingListsDTO.UpdateDTO dto, List<PickingDetailEntity> detailList, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS, PickingListsEntity entity) {
        List<String> newDetailIds = dto.getDetails()
                .stream().map(PickingDetailDTO.View::getId)
                .distinct().collect(Collectors.toList());
        List<PickingDetailEntity> removeData = detailList.stream()
                .filter(v -> !newDetailIds.contains(v.getId()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(removeData)) {
            List<String> removeIds = new ArrayList<>();
            for (PickingDetailEntity detail : removeData) {
                removeIds.add(detail.getId());
                //处理仓位移动数据
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detail.getSkuId(), detail.getSkuNo(),
                        detail.getStagingLocation(), detail.getWarehouseLocation(), detail.getQty(), entity.getWarehouseId()));
                String context = CharSequenceUtil.format("移除【{}】明细行,拣货仓位【{}}】,数量【{}】", detail.getSkuNo(), detail.getWarehouseLocation(), detail.getQty());
                operateLogService.addModuleOperateLog(context, ModuleTypeEnum.PICKING_LISTS.getCode(), entity.getId(), "编辑操作");
            }
            pickingDetailService.removeByIds(removeIds);
        }
    }

    /**
     * 处理新增的数据
     *
     * @param dto              请求参数
     * @param detailList       拣货明细
     * @param detailEntityList 产品明细
     * @param entity           拣货单
     * @param addDTOS          仓位移动
     */
    private void handlerAddData(PickingListsDTO.UpdateDTO dto, List<PickingDetailEntity> detailList, List<ProductDetailEntity> detailEntityList, PickingListsEntity entity, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS) {
        List<PickingDetailDTO.View> newAddDataList = dto.getDetails().stream()
                .filter(detail -> ObjectUtil.isEmpty(detail.getId()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newAddDataList)) {
            List<PickingDetailEntity> addData = newAddDataList.stream().map(data -> {
                PickingDetailEntity detailEntity = detailList.stream()
                        .filter(v -> v.getSkuId().equals(data.getSkuId()))
                        .findFirst().orElse(new PickingDetailEntity());
                // 获取产品信息
                ProductDetailEntity productDetailEntity = detailEntityList.stream()
                        .filter(entityClass -> entityClass.getId().equals(data.getSkuId()))
                        .findFirst().orElse(new ProductDetailEntity());
                PickingDetailEntity detail = new PickingDetailEntity();
                detail.setMainId(entity.getId());
                detail.setSkuId(data.getSkuId());
                detail.setSkuNo(data.getSkuNo());
                detail.setQty(data.getQty());
                detail.setUnit(productDetailEntity.getUnitName());
                detail.setWarehouseLocation(data.getWarehouseLocation());
                detail.setSourceDetailId(data.getSourceDetailId());
                detail.setStagingLocation(detailEntity.getStagingLocation());
                //处理仓位移动数据
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(data.getSkuId(), data.getSkuNo(),
                        data.getWarehouseLocation(), data.getStagingLocation(), data.getQty(), entity.getWarehouseId()));
                //处理日志
                String context = CharSequenceUtil.format("增加【{}】明细行,拣货仓位【{}}】,数量【{}】", data.getSkuNo(), data.getWarehouseLocation(), data.getQty());
                operateLogService.addModuleOperateLog(context, ModuleTypeEnum.PICKING_LISTS.getCode(), entity.getId(), "编辑操作");
                return detail;
            }).collect(Collectors.toList());
            pickingDetailService.saveBatch(addData);
        }
    }


}
