package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
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
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.model.wms.enums.RequisitionApplicationTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.PickingListsMapper;
import com.erp.server.wms.service.*;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PICKING_LISTS;

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
    private SoInfoFeign soInfoFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;


    @Override
    public PagingVO<PickingListsDTO.PagingView> paging(PagingDTO<PickingListsDTO.PagingParam> dto) {
        IPage<PickingListsDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public void add(PickingListsDTO.AddDTO dto) {
        generatePicking(dto);
        // 获取所有拣货暂存配置
        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        List<String> skuIds = dto.getDetails().stream().map(PickingDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIds);
        Map<String, String> warehouseMap = dto.getDetails().stream().collect(Collectors.toMap(PickingDetailDTO.AddDTO::getWarehouseId, PickingDetailDTO.AddDTO::getWarehouseName, (o1, o2) -> o1));
        // 拣货明细转换为规则执行数据明细
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> details = dto.getDetails().stream()
                .map(v -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(v.getWarehouseId(), v.getSkuId(), v.getSkuNo(), v.getQty(), v.getSourceDetailId())).collect(Collectors.toList());
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(dto.getBillType());
        executionData.setCustomerId(dto.getCustomerId());
        executionData.setDeliveryWarehouseId(dto.getDeliveryWarehouseId());
        executionData.setSourceCode(dto.getSourceCode());
        executionData.setCountryCode(dto.getCountryCode());
        executionData.setDetails(details);
        // 执行拣货规则
        List<LocationInventoryResultDTO> results = cfgRulePickingService.getRuleOrderMatchResult(executionData);
        // 根据仓库分组，生成不同的拣货单
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
                detail.setId(IdWorker.getIdStr());
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
                        detail.getWarehouseLocation(), detail.getStagingLocation(), detail.getQty(), resultDTO.getWarehouseId(), detail.getId()));
            }
            entity.setLocationTotal(entities.size());
            moveDto.setPcShow(true);
            moveDto.setSourceId(entity.getId());
            moveDto.setSourceCode(code);
            moveDto.setSourceType(SourceTypeEnum.PICKING_LISTS_ADD.getCode());
            moveDto.setDetailList(moveDetailList);
            ApplicationContextUtils.getBean(PickingListsServiceImpl.class).saveAddData(entity, entities, moveDto);
        }
    }

    /**
     * 处理组合sku
     */
    private void generatePicking(PickingListsDTO.AddDTO dto) {
        List<PickingDetailDTO.AddDTO> detailList = new ArrayList<>();
        List<String> skuIds = dto.getDetails().stream().map(PickingDetailDTO.AddDTO::getSkuId).distinct().collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        for (PickingDetailDTO.AddDTO detail : dto.getDetails()) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detail.getSkuId())
                            && req.getBomVersion().equals(detail.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    PickingDetailDTO.AddDTO detailAdd = PickingDetailDTO.AddDTO.getAddDTO(detail, bomChildrenSkuDTO.getSkuId(), bomChildrenSkuDTO.getSkuNo(), detail.getQty() * bomChildrenSkuDTO.getQuantity());
                    detailList.add(detailAdd);
                }
            } else {
                detailList.add(detail);
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
        dto.setSourceId(id);
        dto.setSourceCode(entity.getCode());
        dto.setSourceType(SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode());
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
            requisitionApplicationService.writeBackData(sourceDetailIds, Boolean.FALSE);
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
            //要货申请完成后，拣货单不允许修改和删除
            RequisitionApplicationEntity application = requisitionApplicationService.getById(entity.getSourceId());
            if (RequisitionApplicationStatusEnum.HANDLE.getStatus().equals(application.getStatus())) {
                throw new ServiceException(ApiError.ERROR_99130);
            }
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            //销售通知单下推销售出库单后，拣货单不允许修改和删除
            int count = soOutstockService.countNotVoided(entity.getSourceId());
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99087);
            }
            SoDeliveryNoticeEntity soDeliveryNotice = soDeliveryNoticeService.getById(entity.getSourceId());
            if (ApproveStatusEnum.APPROVE.getStatus().equals(soDeliveryNotice.getApproveStatus())) {
                throw new ServiceException(ApiError.ERROR_99161);
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
        List<PickingDetailEntity> pickingDetails = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().eq(PickingDetailEntity::getMainId, id).orderByDesc(PickingDetailEntity::getCreateTime));
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
    public void export(PickingListsDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("拣货单", EXPORT_WMS_PICKING_LISTS.getCode(), dto);
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
        List<String> sourceDetailIds = detailList.stream().map(PickingDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationEntity> applicationEntities = requisitionApplicationService.listByIds(sourceIds);
        List<RequisitionApplicationDetailEntity> applicationDetails = requisitionApplicationDetailService.listByIds(sourceDetailIds);
        List<String> applicationDetailSkuIds = applicationDetails.stream().map(RequisitionApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(applicationDetailSkuIds);

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
            List<PickingDetailEntity> details = detailList.stream().filter(v->v.getMainId().equals(picking.getId())).collect(Collectors.toList());
            List<PickingListsDTO.PrintDetailView> views = details.stream().map(detail -> {
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream()
                        .filter(req -> req.getSkuId().equals(detail.getSkuId()))
                        .distinct().findFirst().orElse(new SkuVO());
                PickingListsDTO.PrintDetailView view = new PickingListsDTO.PrintDetailView();
                view.getPrintView(picking, detail, skuVO.getSkuName());
                if (ObjectUtil.isEmpty(view.getWarehouseLocation())) {
                    view.setWarehouseLocation(skuVO.getWarehouseLocationLarge());
                }
                view.setThirdSku("");
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
            List<PickingListsDTO.PrintDetailView> viewList = new ArrayList<>(views.stream().collect(Collectors.groupingBy(v -> v.getThirdSku() + ":"+  v.getSkuNo() + ":" + v.getWarehouseId() + ":" + v.getWarehouseLocation(),
                    Collectors.collectingAndThen(Collectors.toList(), v -> {
                        PickingListsDTO.PrintDetailView view = v.get(0);
                        int totalQuantity = v.stream().mapToInt(PickingListsDTO.PrintDetailView::getPickingQty).sum();
                        view.setPickingQty(totalQuantity);
                        return view;
                    }))).values()).stream().sorted(Comparator.comparing(PickingListsDTO.PrintDetailView::getWarehouseId)
                    .thenComparing(PickingListsDTO.PrintDetailView::getWarehouseLocation)
                    .thenComparing(PickingListsDTO.PrintDetailView::getSkuNo)).collect(Collectors.toList());

            printView.setPrintDetailViews(viewList);
            //封装组合品明细
            if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(picking.getSourceType())) {
                List<PickingListsDTO.CombinationPrintDetailView> combinationPrintDetailViewList = new ArrayList<>();
                for (RequisitionApplicationDetailEntity requisitionApplicationDetail : applicationDetails) {
                    RequisitionApplicationEntity application = applicationEntities.stream().filter(v -> v.getId().equals(picking.getSourceId()))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION));

                    //查询sku是否存在子SKU
                    List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                            .filter(req -> req.getParentSkuId().equals(requisitionApplicationDetail.getSkuId())
                                    && req.getBomVersion().equals(requisitionApplicationDetail.getBomVersion())
                                    && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                            ).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(sonSkuList)){
                        continue;
                    }
                    for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                        PickingListsDTO.CombinationPrintDetailView combinationPrintDetailView = new PickingListsDTO.CombinationPrintDetailView();
                        combinationPrintDetailView.setThirdSku("");
                        if (RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType())) {
                            combinationPrintDetailView.setThirdSku((requisitionApplicationDetail.getPlatformFnSku()));
                        }
                        combinationPrintDetailView.setParentSku(requisitionApplicationDetail.getSkuNo());
                        combinationPrintDetailView.setParentSkuQty(requisitionApplicationDetail.getPickingQty());
                        combinationPrintDetailView.setChildSku(bomChildrenSkuDTO.getSkuNo());
                        combinationPrintDetailView.setChildSkuQty(requisitionApplicationDetail.getPickingQty() * bomChildrenSkuDTO.getQuantity());
                        combinationPrintDetailViewList.add(combinationPrintDetailView);
                    }
                }
                List<PickingListsDTO.CombinationPrintDetailView> combinationList = new ArrayList<>(combinationPrintDetailViewList.stream().collect(Collectors.groupingBy(v -> v.getThirdSku() + ":"+  v.getParentSku() + ":" + v.getChildSku(),
                        Collectors.collectingAndThen(Collectors.toList(), v -> {
                            PickingListsDTO.CombinationPrintDetailView view = v.get(0);
                            Integer totalParentQty = v.stream().mapToInt(PickingListsDTO.CombinationPrintDetailView::getParentSkuQty).sum();
                            view.setParentSkuQty(totalParentQty);
                            int totalChildQty = v.stream().mapToInt(PickingListsDTO.CombinationPrintDetailView::getChildSkuQty).sum();
                            view.setChildSkuQty(totalChildQty);
                            return view;
                        }))).values()).stream().sorted(Comparator.comparing(PickingListsDTO.CombinationPrintDetailView::getParentSku)
                        .thenComparing(PickingListsDTO.CombinationPrintDetailView::getThirdSku)).collect(Collectors.toList());
                //清空上层相同父sku
                String currentParentSku = "";
                Integer parentQty = 0;
                for (PickingListsDTO.CombinationPrintDetailView combinationPrintDetailView : combinationList) {
                    if(StringUtils.isBlank(currentParentSku)){
                        currentParentSku = combinationPrintDetailView.getParentSku();
                        parentQty = combinationPrintDetailView.getParentSkuQty();
                        continue;
                    }
                    if(combinationPrintDetailView.getParentSku().equals(currentParentSku) && parentQty.equals(combinationPrintDetailView.getParentSkuQty())){
                        combinationPrintDetailView.setParentSku("");
                        combinationPrintDetailView.setParentSkuQty(null);
                    }else{
                        currentParentSku = combinationPrintDetailView.getParentSku();
                        parentQty = combinationPrintDetailView.getParentSkuQty();
                    }
                }
                printView.setCombinationPrintDetailView(combinationList);
            }
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
        List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS = new ArrayList<>();
        List<WarehouseLocationMoveDetailDTO.AddDTO> subtractDTOS = new ArrayList<>();
        // 获取新增的数据
        handlerAddData(dto, detailList, detailEntityList, entity, addDTOS);
        // 获取删除的数据
        handlerRemoveData(dto, detailList, subtractDTOS, entity);
        // 获取有差异的修改数据
        handlerUpdateData(dto, detailList, addDTOS, subtractDTOS, entity);
        int qty = dto.getDetails().stream()
                .map(PickingDetailDTO.View::getQty)
                .reduce(0, Math::addExact);
        entity.setSkuTotal(qty);
        entity.setLocationTotal(dto.getDetails().size());
        updateById(entity);
        // 进行对应的仓位移动
        if (!CollectionUtils.isEmpty(addDTOS)) {
            WarehouseLocationMoveDTO.AddDTO moveDto = new WarehouseLocationMoveDTO.AddDTO();
            moveDto.setWarehouseId(entity.getWarehouseId());
            moveDto.setPcShow(true);
            moveDto.setSourceId(entity.getId());
            moveDto.setSourceType(SourceTypeEnum.PICKING_LISTS_ADD.getCode());
            moveDto.setSourceCode(entity.getCode());
            moveDto.setDetailList(addDTOS);
            warehouseLocationMoveService.addAndApprove(moveDto);
        }
        if (!CollectionUtils.isEmpty(subtractDTOS)) {
            WarehouseLocationMoveDTO.AddDTO moveDto = new WarehouseLocationMoveDTO.AddDTO();
            moveDto.setWarehouseId(entity.getWarehouseId());
            moveDto.setPcShow(true);
            moveDto.setSourceId(entity.getId());
            moveDto.setSourceType(SourceTypeEnum.PICKING_LISTS_SUBTRACT.getCode());
            moveDto.setSourceCode(entity.getCode());
            moveDto.setDetailList(subtractDTOS);
            warehouseLocationMoveService.addAndApprove(moveDto);
        }
        List<String> sourceDetailIds = detailList.stream().map(PickingDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            // 反写要货申请的拣货数量
            requisitionApplicationService.writeBackData(sourceDetailIds, Boolean.TRUE);
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            soDeliveryNoticeService.writeBackData(sourceDetailIds);
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
            BigDecimal proportion = new BigDecimal(Optional.ofNullable(skuMap.get(bomChildren.get(0).getSkuNo())).orElse(0)).divide(new BigDecimal(bomChildren.get(0).getQuantity()), 6, RoundingMode.HALF_UP);
            for (BomChildrenSkuDTO bomChild : bomChildren) {
                BigDecimal temp = new BigDecimal(Optional.ofNullable(skuMap.get(bomChild.getSkuNo())).orElse(0)).divide(new BigDecimal(bomChild.getQuantity()), 6, RoundingMode.HALF_UP);
                if (proportion.compareTo(temp) != 0) {
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
            List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listBySourceIds(Collections.singletonList(entity.getSourceId()));
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
        List<WmsCartonEntity> wmsCartonEntityList = wmsCartonService.listByTaskIds(Collections.singletonList(packingTaskEntity.getId()));
        if (CollectionUtils.isEmpty(wmsCartonEntityList)) {
            return;
        }
        List<WmsCartonDetailEntity> wmsCartonDetailEntityList = wmsCartonDetailService.listByMainIds(wmsCartonEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(wmsCartonDetailEntityList)) {
            return;
        }
        Map<String, Integer> packingQtyMap = wmsCartonDetailEntityList.stream().collect(Collectors.toMap(WmsCartonDetailEntity::getSkuNo, WmsCartonDetailEntity::getPackQty, Integer::sum));
        Map<String, Integer> pickingQtyMap = detailList.stream().collect(Collectors.toMap(PickingDetailDTO.View::getSkuNo, PickingDetailDTO.View::getQty, Integer::sum));

        pickingQtyMap.forEach((skuNo, qty) -> {
            Integer packingQty = packingQtyMap.get(skuNo);
            if (Objects.nonNull(packingQty) && qty < packingQty) {
                throw new ServiceException(CharSequenceUtil.format("sku【{}】编辑数量校验不可小于装箱数量{}", skuNo, packingQty));
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
    public List<String> generateSoB2cPicking(SoB2cDeliveryEntity soB2cDeliveryEntity, CfgRulePickingDTO.CfgExecutionDataDTO executionData, Map<String, String> warehouseMap, List<LocationInventoryResultDTO> results) {
        List<String> skuIdList = executionData.getDetails().stream().map(CfgRulePickingDTO.CfgExecutionDataDetailDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> resultData = Pair.create(Collections.emptyList(), Collections.emptyMap());
        if (CollectionUtils.isEmpty(results)) {
            resultData = cfgRulePickingService.getSoB2CRuleOrderMatchResult(executionData);
            results = resultData.getFirst();
            if (!CollectionUtils.isEmpty(resultData.getSecond())) {
                return new ArrayList<>(resultData.getSecond().keySet());
            }
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
        return new ArrayList<>(resultData.getSecond().keySet());
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

    @Override
    public PagingVO<PickingListsDTO.ExportInfoDTO> exportPickingLists(PagingDTO<PickingListsDTO.ExportDTO> dto) {

        Page<PickingListsDTO.ExportInfoDTO> page = baseMapper.exportInfo(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        List<String> warehouseIds = page.getRecords().stream()
                .map(PickingListsDTO.ExportInfoDTO::getWarehouseId)
                .distinct()
                .collect(Collectors.toList());
        List<WarehouseLocationEntity> locationAndAreaList = warehouseLocationService.listByWarehouseIds(warehouseIds);
        Map<String, String> locationMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(v -> v.getWarehouseId() + ":" + v.getCode(), WarehouseLocationEntity::getParentId));
        Map<String, String> areaMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));
        for (PickingListsDTO.ExportInfoDTO infoDTO : page.getRecords()) {
            infoDTO.setWarehouseAreaName(areaMap.get(locationMap.get(infoDTO.getWarehouseId() + ":" + infoDTO.getWarehouseLocation())));
            infoDTO.setStagingAreaName(areaMap.get(locationMap.get(infoDTO.getWarehouseId() + ":" + infoDTO.getStagingLocation())));
        }
        return new PagingVO<>(page);
    }

    /**
     * 处理编辑的数据
     *
     * @param dto          请求参数
     * @param detailList   拣货明细
     * @param addDTOS      仓位移动
     * @param subtractDTOS 仓位移动
     * @param entity       拣货单
     */
    private void handlerUpdateData(PickingListsDTO.UpdateDTO dto, List<PickingDetailEntity> detailList, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS, List<WarehouseLocationMoveDetailDTO.AddDTO> subtractDTOS, PickingListsEntity entity) {
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
                    subtractDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detailEntity.getSkuId(), detailEntity.getSkuNo(),
                            detailEntity.getStagingLocation(), detailEntity.getWarehouseLocation(), detailEntity.getQty() - view.getQty(), entity.getWarehouseId(), entity.getId()));
                } else if (detailEntity.getQty() < view.getQty()) {
                    //处理仓位移动数据
                    addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(view.getSkuId(), view.getSkuNo(),
                            view.getWarehouseLocation(), view.getStagingLocation(), view.getQty() - detailEntity.getQty(), entity.getWarehouseId(), entity.getId()));
                } else {
                    continue;
                }
            } else {
                // 仓位变更了需要进行原数据仓位退回，新仓位移出
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(view.getSkuId(), view.getSkuNo(),
                        view.getWarehouseLocation(), view.getStagingLocation(), view.getQty(), entity.getWarehouseId(), entity.getId()));
                subtractDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detailEntity.getSkuId(), detailEntity.getSkuNo(),
                        detailEntity.getStagingLocation(), detailEntity.getWarehouseLocation(), detailEntity.getQty(), entity.getWarehouseId(), entity.getId()));
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
                        detail.getStagingLocation(), detail.getWarehouseLocation(), detail.getQty(), entity.getWarehouseId(), detail.getId()));
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
                detail.setId(IdWorker.getIdStr());
                detail.setSkuId(data.getSkuId());
                detail.setSkuNo(data.getSkuNo());
                detail.setQty(data.getQty());
                detail.setUnit(productDetailEntity.getUnitName());
                detail.setWarehouseLocation(data.getWarehouseLocation());
                detail.setSourceDetailId(data.getSourceDetailId());
                detail.setStagingLocation(detailEntity.getStagingLocation());
                //处理仓位移动数据
                addDTOS.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(data.getSkuId(), data.getSkuNo(),
                        data.getWarehouseLocation(), data.getStagingLocation(), data.getQty(), entity.getWarehouseId(), detail.getId()));
                //处理日志
                String context = CharSequenceUtil.format("增加【{}】明细行,拣货仓位【{}}】,数量【{}】", data.getSkuNo(), data.getWarehouseLocation(), data.getQty());
                operateLogService.addModuleOperateLog(context, ModuleTypeEnum.PICKING_LISTS.getCode(), entity.getId(), "编辑操作");
                return detail;
            }).collect(Collectors.toList());
            pickingDetailService.saveBatch(addData);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDelivery(List<String> codes) {
        for (String code : codes) {
            SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryService.getOne(Wrappers.<SoB2cDeliveryEntity>lambdaQuery().eq(SoB2cDeliveryEntity::getCode, code));
            List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(soB2cDeliveryEntity.getId()));
            List<String> skuIds = soB2cDeliveryDetailEntities.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
            //获取子SKU集合
            List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIds);
            List<String> childSkuIds = bomChildrenSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList());
            skuIds.addAll(childSkuIds);
            List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIds);
            PickingListsEntity entity = new PickingListsEntity();
            String pickCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHD);
            // 生成拣货单主表数据
            entity.setId(IdWorker.getIdStr());
            entity.setCode(pickCode);
            entity.setSourceId(soB2cDeliveryEntity.getId());
            entity.setSourceCode(soB2cDeliveryEntity.getCode());
            entity.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY.getCode());
            entity.setSkuTotal(soB2cDeliveryDetailEntities.size());
            List<PickingDetailEntity> entities = new ArrayList<>();
            for (SoB2cDeliveryDetailEntity detailEntity : soB2cDeliveryDetailEntities) {
                entity.setWarehouseId(detailEntity.getWarehouseId());
                entity.setWarehouseName(detailEntity.getWarehouseName());
                ProductDetailEntity productDetailEntity = detailEntityList.stream()
                        .filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId()))
                        .findFirst().orElse(new ProductDetailEntity());
                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                        .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                                && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                        ).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sonSkuList)) {
                    for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                        PickingDetailEntity detail = new PickingDetailEntity();
                        detail.setSkuId(bomChildrenSkuDTO.getSkuId());
                        detail.setMainId(entity.getId());
                        detail.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                        detail.setUnit(productDetailEntity.getUnitName());
                        detail.setQty(detailEntity.getDeliveryQty() * bomChildrenSkuDTO.getQuantity());
                        detail.setWarehouseLocation("");
                        detail.setSourceDetailId(detailEntity.getId());
                        entities.add(detail);
                    }
                } else {
                    PickingDetailEntity detail = new PickingDetailEntity();
                    detail.setSkuId(detailEntity.getSkuId());
                    detail.setMainId(entity.getId());
                    detail.setSkuNo(detailEntity.getSkuNo());
                    detail.setUnit(productDetailEntity.getUnitName());
                    detail.setQty(detailEntity.getDeliveryQty());
                    detail.setWarehouseLocation("");
                    detail.setSourceDetailId(detailEntity.getId());
                    entities.add(detail);
                }
            }
            entity.setLocationTotal(entities.size());
            save(entity);
            pickingDetailService.saveBatch(entities);
        }

    }

}
