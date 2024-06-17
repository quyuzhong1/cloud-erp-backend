package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDetailDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.CfgRulePickingStagingEntity;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.mapper.PickingListsMapper;
import com.erp.server.wms.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public PagingVO<PickingListsDTO.PagingView> paging(PagingDTO<PickingListsDTO.PagingParam> dto) {
        IPage<PickingListsDTO.PagingView> page = baseMapper.paging(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public void add(PickingListsDTO.Add dto) {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_JHD);
        // 生成拣货单主表数据
        PickingListsEntity entity = new PickingListsEntity();
        entity.setId(IdWorker.getIdStr());
        entity.setCode(code);
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(dto.getSourceCode());
        entity.setSourceType(dto.getSourceType());
        int skuTotal = dto.getDetails().stream().map(PickingDetailDTO.Add::getQty).reduce(0, Math::addExact);
        entity.setSkuTotal(skuTotal);
        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        List<String> skuIds = dto.getDetails().stream().map(PickingDetailDTO.Add::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIds);
        Map<String, Integer> sku = dto.getDetails().stream().collect(Collectors.toMap(PickingDetailDTO.Add::getSkuId, PickingDetailDTO.Add::getQty, Integer::sum));
        Map<String, String> skuMap = dto.getDetails().stream().collect(Collectors.toMap(PickingDetailDTO.Add::getSkuId, PickingDetailDTO.Add::getSkuNo, (o1, o2) -> o1));
        Map<String, String> sourceDetailMap = dto.getDetails().stream().collect(Collectors.toMap(PickingDetailDTO.Add::getSkuId, PickingDetailDTO.Add::getSourceDetailId, (o1, o2) -> o1));
        Map<String, Object> map = new HashMap<>();
        map.put("billType", dto.getBillType());
        map.put("customerId", dto.getCustomerId());
        map.put("deliveryWarehouseId", dto.getDeliveryWarehouseId());
        map.put("warehouseId", dto.getWarehouseId());
        map.put("sku", sku);
        map.put("skuMap", skuMap);
        List<LocationInventoryResultDTO> results = cfgRulePickingService.getRuleOrderMatchResult(map);
        List<WarehouseLocationMoveDetailDTO.AddDTO> moveDetailList = new ArrayList<>();
        List<PickingDetailEntity> entities = new ArrayList<>();
        for (LocationInventoryResultDTO result : results) {
            // 获取仓库暂存区默认配置
            CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                    .filter(staging -> staging.getBillType().equals(dto.getBillType()))
                    .filter(staging -> staging.getWarehouseId().equals(result.getWarehouseId()))
                    .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
            // 获取产品信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream()
                    .filter(entityClass -> entityClass.getId().equals(result.getSkuId()))
                    .findFirst().orElse(new ProductDetailEntity());
            PickingDetailEntity detail = new PickingDetailEntity();
            detail.setMainId(entity.getId());
            detail.setSkuId(result.getSkuId());
            detail.setSkuNo(skuMap.get(result.getSkuId()));
            detail.setQty(result.getQuantity());
            detail.setUnit(productDetailEntity.getUnitName());
            detail.setWarehouseLocation(result.getWarehouseLocation());
            detail.setSourceDetailId(sourceDetailMap.get(result.getSkuId()));
            detail.setStagingLocation(pickingStaging.getWarehouseLocation());
            entities.add(detail);
            moveDetailList.add(WarehouseLocationMoveDetailDTO.AddDTO.getLocationMoveDTO(detail.getSkuId(), detail.getSkuNo(),
                    detail.getWarehouseLocation(), detail.getStagingLocation(), detail.getQty(), dto.getWarehouseId()));
        }
        entity.setLocationTotal(entities.size());
        WarehouseLocationMoveDTO.AddDTO moveDto = new WarehouseLocationMoveDTO.AddDTO();
        moveDto.setWarehouseId(dto.getWarehouseId());
        moveDto.setPcShow(false);
        moveDto.setDetailList(moveDetailList);
        ApplicationContextUtils.getBean(PickingListsServiceImpl.class).saveAddData(entity, entities, moveDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveAddData(PickingListsEntity entity, List<PickingDetailEntity> entities, WarehouseLocationMoveDTO.AddDTO moveDto) {
        save(entity);
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
    }

    private void checkStatus(PickingListsEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            //要货申请下推发货单后，拣货单不允许修改和删除
            int count = firstMileDeliveryService.countNotVoided(entity.getId());
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99086);
            }
        } else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())) {
            //销售通知单下推销售出库单后，拣货单不允许修改和删除
            int count = soOutstockService.countNotVoided(entity.getId());
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99086);
            }
        }
    }

    @Override
    public PickingListsDTO.View view(String id) {
        PickingListsEntity entity = getById(id);
        List<WarehouseLocationEntity> locationAndAreaList = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getWarehouseId()));
        Map<String, String> locationMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, WarehouseLocationEntity::getParentId));
        Map<String, String> areaMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));
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
                .collect(Collectors.toMap(WarehouseLocationEntity::getCode, WarehouseLocationEntity::getParentId));
        Map<String, String> areaMap = locationAndAreaList.stream()
                .collect(Collectors.toMap(WarehouseLocationEntity::getId, WarehouseLocationEntity::getName));
        for (PickingListsDTO.ExportInfoDTO infoDTO : list) {
            infoDTO.setWarehouseAreaName(areaMap.get(locationMap.get(infoDTO.getWarehouseLocation())));
            infoDTO.setStagingAreaName(areaMap.get(locationMap.get(infoDTO.getStagingLocation())));
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
        List<PickingListsEntity> pickingLists = listByIds(ids);
        List<PickingDetailEntity> detailList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, ids));
        List<String> skuIds = detailList.stream().map(PickingDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuBaseByIds(skuIds);
        return detailList.stream().map(detail -> {
            PickingListsEntity entity = pickingLists.stream()
                    .filter(picking -> picking.getId().equals(detail.getMainId()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(ApiError.ERROR_400));
            //匹配sku信息
            SkuVO skuVO = skuVOList.stream()
                    .filter(req -> req.getSkuId().equals(detail.getSkuId()))
                    .distinct().findFirst().orElse(new SkuVO());
            PickingListsDTO.PrintView view = new PickingListsDTO.PrintView();
            view.getPrintView(entity, detail, skuVO.getSkuName());
            return view;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PickingListsDTO.Update dto) {
        PickingListsEntity entity = getById(dto.getId());
        checkStatus(entity);
        List<PickingDetailEntity> detailList = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().eq(PickingDetailEntity::getMainId, dto.getId()));
        List<String> skuIds = dto.getDetails()
                .stream().map(PickingDetailDTO.View::getSkuId)
                .distinct().collect(Collectors.toList());
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
        moveDto.setDetailList(addDTOS);
        warehouseLocationMoveService.addAndApprove(moveDto);
        List<String> sourceDetailIds = detailList.stream().map(PickingDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        if (SourceTypeEnum.REQUISITION_APPLICATION.getCode().equals(entity.getSourceType())) {
            // 反写要货申请的拣货数量
            requisitionApplicationService.writeBackData(sourceDetailIds);
        }else if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(entity.getSourceType())){
            soDeliveryNoticeService.writeBackData(sourceDetailIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySourceId(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)){
            return;
        }
        List<PickingListsEntity> list = list(Wrappers.<PickingListsEntity>lambdaQuery().in(PickingListsEntity::getSourceId, ids));
        List<String> idList = list.stream()
                .map(PickingListsEntity::getId)
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(idList)){
            removeByIds(idList);
            pickingDetailService.remove(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getMainId, idList));
        }
    }

    @Override
    public List<PickingListsDTO.SourceView> listBySourceIds(List<String> sourceIds) {

        return baseMapper.listBySourceIds(sourceIds);
    }

    /**
     * 处理编辑的数据
     *
     * @param dto        请求参数
     * @param detailList 拣货明细
     * @param entity     拣货单
     * @param addDTOS    仓位移动
     */
    private void handlerUpdateData(PickingListsDTO.Update dto, List<PickingDetailEntity> detailList, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS, PickingListsEntity entity) {
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
            detailEntity.setWarehouseLocation(view.getWarehouseLocation());
            detailEntity.setQty(view.getQty());
            updateList.add(detailEntity);
            String context = CharSequenceUtil.format("编辑了【{}】明细行,拣货仓位由【{}】变更为【{}】，数量由【{}】变更为【{}】", view.getSkuNo(),
                    detailEntity.getWarehouseLocation(), view.getWarehouseLocation(), detailEntity.getQty(), view.getQty());
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
    private void handlerRemoveData(PickingListsDTO.Update dto, List<PickingDetailEntity> detailList, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS, PickingListsEntity entity) {
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
    private void handlerAddData(PickingListsDTO.Update dto, List<PickingDetailEntity> detailList, List<ProductDetailEntity> detailEntityList, PickingListsEntity entity, List<WarehouseLocationMoveDetailDTO.AddDTO> addDTOS) {
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
                detail.setSourceDetailId(detailEntity.getSourceDetailId());
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
