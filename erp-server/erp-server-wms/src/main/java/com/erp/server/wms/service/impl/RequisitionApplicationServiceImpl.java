package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelExportFillCellMergeStrategy;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.RequisitionApplicationAssembleExportDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SkuMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.RequisitionApplicationConverter;
import com.erp.server.wms.mapper.RequisitionApplicationMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REQUISITION_APPLICATION;

/**
 * <p>
 * 要货申请单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationServiceImpl extends SuperServiceImpl<RequisitionApplicationMapper, RequisitionApplicationEntity> implements RequisitionApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private TransferInfoService transferInfoService;
    @Autowired
    private WarehouseService warehouseService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;
    @Resource
    private VirtualWarehousePushHandleDetailService virtualWarehousePushHandledetailService;
    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;
    @Resource
    private SyncWdtVirtualWarehousePushOrderService syncWdtVirtualWarehousePushOrderService;
    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SkuMappingFeign skuMappingFeign;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private PickingListsService pickingListsService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Resource
    private FbaShipmentDetailService fbaShipmentDetailService;
    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;
    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    @Resource
    @Lazy
    private RequisitionApplicationServiceImpl service;
    @Resource
    @Lazy
    private WmsDeliveryPlanService wmsDeliveryPlanService;
    @Resource
    private CfgRulePickingStagingService cfgRulePickingStagingService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private FirstMileDeliveryDetailService firstMileDeliveryDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private PackingTaskService packingTaskService;

    @Resource
    private CfgRuleOutService cfgRuleOutService;

    @Resource
    private WmsCartonService cartonService;

    @Resource
    private FbaShipmentPackingService fbaShipmentPackingService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO addDTO) {
        RequisitionApplicationEntity requisitionApplicationEntity = new RequisitionApplicationEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationEntity);

        // 数据处理
        handleData(requisitionApplicationEntity);

        log.info("开始新增要货申请单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YHSQ);
        requisitionApplicationEntity.setCode(code);
        boolean save = super.save(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // 新增明细
        requisitionApplicationDetailService.add(addDTO, requisitionApplicationEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "要货申请" , requisitionApplicationEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(requisitionApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationDTO.UpdateDTO updateDTO) {
        RequisitionApplicationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请单"));
        RequisitionApplicationEntity requisitionApplicationEntity =  BeanMapperUtils.map(RequisitionApplicationEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationEntity);
        log.info("编辑 开始修改要货申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // 修改明细数据（包含增删改）
        requisitionApplicationDetailService.update(updateDTO, requisitionApplicationEntity.getId());
        return Boolean.TRUE;
    }

    @Override
    public List<RequisitionApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        FirstMileDeliveryDTO.PagingParamDTO searchParam = new FirstMileDeliveryDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<RequisitionApplicationDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = RequisitionApplicationStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(RequisitionApplicationDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                list.add(new RequisitionApplicationDTO.TabListDTO(status, 0));
            }
        });
        list.add(new RequisitionApplicationDTO.TabListDTO("all", list.stream().mapToInt(RequisitionApplicationDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public PagingVO<RequisitionApplicationDTO.ListDTO> paging(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<RequisitionApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public RequisitionApplicationDTO.ViewDTO view(String id) {
        //发货单主信息
        RequisitionApplicationEntity applicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到要货申请单数据"));
        RequisitionApplicationDTO.ViewDTO data = BeanMapperUtils.map(RequisitionApplicationDTO.ViewDTO.class, applicationEntity);
        //发货单详情
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, requisitionApplicationDetailEntities);
        return data;
    }

    @Override
    public BatchResultDTO submit(String id) {
        RequisitionApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到要货申请数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改要货申请状态数据，id：【{}】", id);

        this.updateApproveStatus(id, RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus());

        // 记录操作日志
        log.info("提交 开始记录要货申请日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public List<RequisitionApplicationDTO.HandleListDTO> handleList(List<String> ids) {
        List<RequisitionApplicationDTO.HandleListDTO> list = baseMapper.handleList(ids);
        long count = list.stream().filter(req -> !RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus().equals(req.getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.WAIT_HANDLE_HANDLE);
        }

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //字段映射处理
        for (RequisitionApplicationDTO.HandleListDTO handleListDTO : list) {
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(handleListDTO.getSkuId())).findFirst().orElse(new SkuVO());
            handleListDTO.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(handleListDTO.getSkuId())
                            && req.getBomVersion().equals(handleListDTO.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equalsIgnoreCase(req.getType())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                handleListDTO.setIsCombination(Boolean.TRUE);
            } else {
                handleListDTO.setIsCombination(Boolean.FALSE);
            }

            //如果没有仓位用空仓位
            handleListDTO.setFromWarehouseLocation(StringUtils.isBlank(handleListDTO.getFromWarehouseLocation()) ? "" : handleListDTO.getFromWarehouseLocation());
            handleListDTO.setToWarehouseLocation(StringUtils.isBlank(handleListDTO.getToWarehouseLocation()) ? "" : handleListDTO.getToWarehouseLocation());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleSave(List<RequisitionApplicationDTO.HandleListDTO> list) {
        RequisitionApplicationEntity requisitionApplication = getById(list.get(0).getSourceId());
        //根据调出调入仓id查询仓库信息
        List<String> warehouseIds = list.stream().map(req -> req.getFromWarehouseId()).collect(Collectors.toList());
        List<String> toWarehouseIds = list.stream().map(req -> req.getToWarehouseId()).collect(Collectors.toList());
        warehouseIds.addAll(toWarehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);

        //获取虚拟仓
        List<VirtualWarehouseEntity> vmList = new ArrayList<>();
        List<String> fromVmIds = list.stream().map(RequisitionApplicationDTO.HandleListDTO::getFromVirtualWarehouseId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(fromVmIds)) {
            vmList = virtualWarehouseService.listByIds(fromVmIds);
        }
        //查询产品信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDTO.HandleListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        for (RequisitionApplicationDTO.HandleListDTO handleListDTO : list) {
            Integer approveQty = handleListDTO.getApproveQty();

            //虚拟仓数量
            if (StringUtils.isNotBlank(handleListDTO.getFromVirtualWarehouseId())) {
                VirtualWarehouseEntity virtualWarehouseEntity = vmList.stream().filter(item -> Objects.equals(item.getId(), handleListDTO.getFromVirtualWarehouseId())).findFirst().orElse(new VirtualWarehouseEntity());
                handleListDTO.setFromVirtualWarehouseName(virtualWarehouseEntity.getName());
                //获取虚拟仓出库参数
                getStockParam(handleListDTO, approveQty, allocationParamList);
            }
            //调出仓
            WarehouseDTO.UpdateDTO fromWarehouse = warehouseList.stream().filter(req -> req.getId().equals(handleListDTO.getFromWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            handleListDTO.setOutOrgId(fromWarehouse.getOrgId());
            //调入仓
            WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream().filter(req -> req.getId().equals(handleListDTO.getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            handleListDTO.setInOrgId(toWarehouse.getOrgId());
        }
        //执行虚拟仓出库
        if (CollectionUtils.isNotEmpty(allocationParamList)) {
            VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
            allocationDto.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            allocationDto.setParamList(allocationParamList);
            virtualInventoryTransCoreService.approve(allocationDto);

            List<RequisitionApplicationDTO.HandleListDTO> haveFromVwList = list.stream().filter(item -> StringUtils.isNotBlank(item.getFromVirtualWarehouseId())).collect(Collectors.toList());
            List<RequisitionApplicationDetailEntity> detailEntityList = BeanMapperUtils.copyList(RequisitionApplicationDetailEntity.class, haveFromVwList);
            Map<String, List<RequisitionApplicationDetailEntity>> haveFromVwMap = detailEntityList.stream().collect(Collectors.groupingBy(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId));

            //创建旺店通虚拟仓订单
            //service.saveWdtOrder(fromVmIds, requisitionApplication, haveFromVwMap);

        }
        //调出仓库和调入仓库不一致的单据
        Map<String, List<RequisitionApplicationDTO.HandleListDTO>> map = list.stream().filter(req -> !req.getFromWarehouseId().equals(req.getToWarehouseId())).collect(Collectors.groupingBy(req -> req.getSourceCode().concat(req.getOutOrgId().concat(req.getInOrgId()))));
        for (Map.Entry<String, List<RequisitionApplicationDTO.HandleListDTO>> dto : map.entrySet()) {
            List<RequisitionApplicationDTO.HandleListDTO> value = dto.getValue();

            //生成调拨单
            generateHandleToTransferInfo(value, skuVOList, bomChildrenSkuList);
        }

        //修改处理信息
        List<String> raIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        Boolean flag = updateHandleDate(raIds, RequisitionApplicationStatusEnum.HANDLE_ING.getStatus());

        //保存处理选择的调出,调入,批准数量等信息
        updateHandleDetailDate(list, warehouseList);

        //新增日志
        List<RequisitionApplicationEntity> requisitionApplicationEntities = this.listByIds(raIds);
        for (RequisitionApplicationEntity entity : requisitionApplicationEntities) {
            String msg = StrUtil.format("用户【{}】处理了一个单号为【{}】的【{}】单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "处理保存");
        }
        return flag;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveWdtOrder(List<String> fromVmIds, RequisitionApplicationEntity requisitionApplication,
                             Map<String, List<RequisitionApplicationDetailEntity>> haveFromVwMap) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                List<String> fromVwId = fromVmIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                List<ThirdMappingEntity> fromThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromVwId);
                //保存要货申请主单
                VirtualWarehousePushHandleEntity pushHandleEntity = new VirtualWarehousePushHandleEntity(requisitionApplication.getId(),
                        requisitionApplication.getCode(), SourceTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplication.getStatus()
                        , VwAllocationDirectionEnum.REVERSE.getCode());
                virtualWarehousePushHandleService.save(pushHandleEntity);
                List<VirtualWarehousePushHandleDetailEntity> handleDetailList = new ArrayList<>();
                saveList(requisitionApplication, haveFromVwMap, fromThirdMappingList, pushHandleEntity, handleDetailList, VwAllocationDirectionEnum.REVERSE);
                if (CollectionUtils.isNotEmpty(handleDetailList)) {
                    //推送中台任务:保存任务+发送mq
                    List<DmpPushTaskEntity> dmpPushTaskEntityList = syncWdtVirtualWarehousePushOrderService.saveTaskList(handleDetailList,
                            requisitionApplication.getCode(), SyncOperateEnum.OPERATE_APPROVE.getCode(), SourceTypeEnum.REQUISITION_APPLICATION.getCode());
                    if (CollectionUtils.isNotEmpty(dmpPushTaskEntityList)) {
//                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                            @Override
//                            public void afterCommit() {
                        //发送mq
                        dmpMqFeign.sendTask(dmpPushTaskEntityList);
//                            }
//                        });
                    }
                }
            }
        });
    }

    private void saveList(RequisitionApplicationEntity requisitionApplication, Map<String, List<RequisitionApplicationDetailEntity>> haveFromVwMap,
                          List<ThirdMappingEntity> fromThirdMappingList, VirtualWarehousePushHandleEntity pushHandleEntity,
                          List<VirtualWarehousePushHandleDetailEntity> handleDetailList, VwAllocationDirectionEnum code) {
        haveFromVwMap.forEach((fromVmId, fromHandleList) -> {
            //获取调出仓绑定的旺店通虚拟仓
            if (CollectionUtils.isNotEmpty(fromThirdMappingList)) {
                ThirdMappingEntity thirdMapping = fromThirdMappingList.stream().filter(item -> Objects.equals(item.getSysId(), fromVmId)).findFirst().orElse(null);
                if (Objects.nonNull(thirdMapping)) {
//                fromThirdMappingList.forEach(thirdMapping -> {
                    //保存合单明细
                    VirtualWarehousePushHandleDetailEntity handleDetailEntity = new VirtualWarehousePushHandleDetailEntity();
                    handleDetailEntity.setSourceId(requisitionApplication.getId());
                    handleDetailEntity.setDirection(pushHandleEntity.getDirection());
                    handleDetailEntity.setType(requisitionApplication.getType());
                    handleDetailEntity.setStatus(pushHandleEntity.getStatus());
                    switch (code) {
                        case FORWARD:
                            handleDetailEntity.setToVirtualWarehouseId(fromVmId);
                            handleDetailEntity.setThirdToVirtualWarehouseId(thirdMapping.getThirdId());
                            handleDetailEntity.setThirdToVirtualWarehouseNo(thirdMapping.getThirdCode());
                            break;
                        default:
                            handleDetailEntity.setFromVirtualWarehouseId(fromVmId);
                            handleDetailEntity.setThirdFromVirtualWarehouseId(thirdMapping.getThirdId());
                            handleDetailEntity.setThirdFromVirtualWarehouseNo(thirdMapping.getThirdCode());
                            break;
                    }
                    handleDetailEntity.setSysType(thirdMapping.getThirdSysType());
                    handleDetailEntity.setMainId(pushHandleEntity.getId());
                    handleDetailEntity.setWarehouseId(fromHandleList.get(0).getFromWarehouseId());
                    handleDetailEntity.setThirdWarehouseId(thirdMapping.getRemark());
                    Integer sumQty = fromHandleList.stream().map(RequisitionApplicationDetailEntity::getApproveQty).reduce(0, Integer::sum);
                    handleDetailEntity.setQty(sumQty);
                    virtualWarehousePushHandledetailService.save(handleDetailEntity);
                    handleDetailList.add(handleDetailEntity);
                    fromHandleList.forEach(allocationDetail -> {
                        VirtualWarehousePushHandleRelationEntity vmAllocationHandleRelationEntity = new VirtualWarehousePushHandleRelationEntity();
                        vmAllocationHandleRelationEntity.setSourceId(requisitionApplication.getId());
                        switch (code) {
                            case FORWARD:
                                vmAllocationHandleRelationEntity.setSourceDetailId(allocationDetail.getId());
                                break;
                            default:
                                vmAllocationHandleRelationEntity.setSourceDetailId(allocationDetail.getSourceDetailId());
                                break;
                        }
                        vmAllocationHandleRelationEntity.setHandleId(pushHandleEntity.getId());
                        vmAllocationHandleRelationEntity.setHandleDetailId(handleDetailEntity.getId());
                        virtualWarehousePushHandleRelationService.save(vmAllocationHandleRelationEntity);
                    });
                }
            }
        });
    }

    /**
     * 获取虚拟仓出库参数
     *
     * @param handleListDTO
     * @param approveQty
     * @param allocationParamList
     */
    private static void getStockParam(RequisitionApplicationDTO.HandleListDTO handleListDTO, Integer
            approveQty, List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList) {
        if(approveQty == 0){
            return;
        }
        VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSourceId(handleListDTO.getSourceId());
        outInStockDTO.setSourceCode(handleListDTO.getSourceCode());
        outInStockDTO.setSourceType(InventorySourceTypeEnum.REQUISITION_APPLICATION);
        outInStockDTO.setSourceDetailId(handleListDTO.getSourceDetailId());
        outInStockDTO.setBillDate(LocalDate.now());
        outInStockDTO.setSkuId(handleListDTO.getSkuId());
        outInStockDTO.setSkuNo(handleListDTO.getSkuNo());
        outInStockDTO.setWarehouseId(handleListDTO.getFromWarehouseId());
        outInStockDTO.setVirtualWarehouseId(handleListDTO.getFromVirtualWarehouseId());
        outInStockDTO.setQty(approveQty);
        allocationParamList.add(outInStockDTO);
    }


    @Override
    public List<RequisitionApplicationDTO.FinishListDTO> finishList(List<String> ids) {
        List<RequisitionApplicationDTO.FinishListDTO> list = baseMapper.finishList(ids);

        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //字段映射处理
        for (RequisitionApplicationDTO.FinishListDTO finishListDTO : list) {
            if (!RequisitionApplicationStatusEnum.HANDLE_ING.getStatus().equals(finishListDTO.getStatus())) {
                throw new ServiceException(ApiError.HANDLE_ING_FINISH, finishListDTO.getSourceCode());
            }

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(finishListDTO.getSkuId())).findFirst().orElse(new SkuVO());
            finishListDTO.setProductName(skuVO.getSkuName());

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(finishListDTO.getSkuId())
                            && req.getBomVersion().equals(finishListDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                finishListDTO.setIsCombination(Boolean.TRUE);
            } else {
                finishListDTO.setIsCombination(Boolean.FALSE);
            }

            //仓位是推荐仓位，与库存无关
            finishListDTO.setPickingWarehouseLocation(skuVO.getWarehouseLocation());

            finishListDTO.setPickingQty(finishListDTO.getPickingQty());
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finishSave(List<RequisitionApplicationDTO.FinishListDTO> list) {
        long countQty = list.stream().mapToInt(RequisitionApplicationDTO.FinishListDTO::getPickingQty).sum();
        if (countQty <= 0) {
            throw new ServiceException(ApiError.ERROR_99106);
        }
        //根据调出调入仓id查询仓库信息
        List<String> toWarehouseIds = list.stream().map(req -> req.getToWarehouseId()).collect(Collectors.toList());
        List<String> requisitionWarehouseIds = list.stream().map(req -> req.getRequisitionWarehouseId()).collect(Collectors.toList());
        toWarehouseIds.addAll(requisitionWarehouseIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(toWarehouseIds);

        //查询产品信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDTO.FinishListDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        for (RequisitionApplicationDTO.FinishListDTO finishListDTO : list) {
            //调出仓
            WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream().filter(req -> req.getId().equals(finishListDTO.getToWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            finishListDTO.setOutOrgId(toWarehouse.getOrgId());
            //调入仓
            WarehouseDTO.UpdateDTO requisitionWarehouse = warehouseList.stream().filter(req -> req.getId().equals(finishListDTO.getRequisitionWarehouseId())).findFirst().orElse(new WarehouseDTO.UpdateDTO());
            finishListDTO.setInOrgId(requisitionWarehouse.getOrgId());
        }

        //调出仓库和调入仓库不一致的单据
        Map<String, List<RequisitionApplicationDTO.FinishListDTO>> map = list.stream().filter(req -> !req.getToWarehouseId().equals(req.getRequisitionWarehouseId())).collect(Collectors.groupingBy(req -> req.getSourceCode().concat(req.getOutOrgId().concat(req.getInOrgId()))));
        for (Map.Entry<String, List<RequisitionApplicationDTO.FinishListDTO>> dto : map.entrySet()) {
            List<RequisitionApplicationDTO.FinishListDTO> value = dto.getValue();

            //完成要货单生成调拨单
            generateFinishToTransferInfo(value, skuVOList, bomChildrenSkuList);
        }

        //修改处理信息
        List<String> raIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        Boolean flag = updateHandleDate(raIds, RequisitionApplicationStatusEnum.HANDLE.getStatus());

        //保存完成输入的拣货数量
        updateFinishDetailPickingQty(list);

        //针对拣货单进行库存的多退少补
        handleVirtualInventoryQty(list);

        //新增日志
        List<RequisitionApplicationEntity> requisitionApplicationEntities = this.listByIds(raIds);
        for (RequisitionApplicationEntity entity : requisitionApplicationEntities) {
            String msg = StrUtil.format("用户【{}】完成了一个单号为【{}】的【{}】单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "完成保存");
        }
        return flag;
    }

    /**
     * 处理虚拟仓数量
     * @author will
     * @date 2024/7/25 21:38
     * @param list
     */
    private void handleVirtualInventoryQty (List<RequisitionApplicationDTO.FinishListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> detailIdList = list.stream().map(RequisitionApplicationDTO.FinishListDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        List<RequisitionApplicationDetailEntity> applicationDetailList = requisitionApplicationDetailService.listByIds(detailIdList);
        if (CollectionUtils.isEmpty(applicationDetailList)) {
            throw new ServiceException("拣货单关联的要货申请明细未找到");
        }
        //添加
        List<VirtualInventoryStockDTO.OutInStockDTO> addList = new ArrayList<>();

        //减少
        List<VirtualInventoryStockDTO.OutInStockDTO> subList = new ArrayList<>();

        for (RequisitionApplicationDetailEntity applicationDetailEntity : applicationDetailList) {
            //选择数据不存在无需处理
            RequisitionApplicationDTO.FinishListDTO finishListDTO = list.stream().filter(obj -> StrUtil.equals(obj.getSourceDetailId(), applicationDetailEntity.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(finishListDTO)) {
                continue;
            }
            //调入仓不等于调入仓则无需库存扣减
            if (!StrUtil.equals(applicationDetailEntity.getFromWarehouseId(),applicationDetailEntity.getToWarehouseId())) {
                continue;
            }
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(finishListDTO.getSourceId());
            outInStockDTO.setSourceCode(finishListDTO.getSourceCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.REQUISITION_APPLICATION);
            outInStockDTO.setSourceDetailId(applicationDetailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(applicationDetailEntity.getSkuId());
            outInStockDTO.setSkuNo(applicationDetailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
            outInStockDTO.setBomVersion(applicationDetailEntity.getBomVersion());
            if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
            Integer virtualFrozenQty = applicationDetailEntity.getVirtualFrozenQty();
            Integer qty = applicationDetailEntity.getPickingQty();
            if (MathUtil.compareTo(qty, virtualFrozenQty) > MathUtil.ZERO) {
                outInStockDTO.setQty((qty - virtualFrozenQty) );
                addList.add(outInStockDTO);
            } else if (MathUtil.compareTo(virtualFrozenQty, qty) > MathUtil.ZERO) {
                outInStockDTO.setQty((virtualFrozenQty - qty) );
                subList.add(outInStockDTO);
            } else {
                log.info("无需要多退少补的库存需要变更");
            }
            applicationDetailEntity.setVirtualFrozenQty(applicationDetailEntity.getPickingQty());
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            stockParamDTO.setParamList(addList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }
        if (CollectionUtils.isNotEmpty(subList)) {
            VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
            stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_RETURN_HANDLE.getCode());
            stockParamDTO.setParamList(subList);
            virtualInventoryTransCoreService.approve(stockParamDTO);
        }

        //更新虚拟仓冻结数量
        requisitionApplicationDetailService.updateBatchById(applicationDetailList);
    }


    @Override
    public List<RequisitionApplicationDTO.printPickingViewDTO> printPickingView(List<String> ids) {
        List<RequisitionApplicationEntity> list = this.listByIds(ids);
        long count = list.stream()
                .filter(req -> !RequisitionApplicationStatusEnum.HANDLE_ING.getStatus().equals(req.getStatus())
                        && !RequisitionApplicationStatusEnum.HANDLE.getStatus().equals(req.getStatus()))
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.HANDLE_ING_OR_HANDLE_IS_PRINT_PICKING);
        }

        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailEntities = requisitionApplicationDetailService.listByMainIds(ids);

        //查询产品信息
        List<String> skuIds = requisitionApplicationDetailEntities.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());

        //查询第三方SKU信息
        List<ListingInfoWithSkuMappingDTO> listingWithSkuMappingDTOList = skuMappingFeign.listByErpSkuIdAndType(skuIds,"","","");
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);

        //只拆分销售套装BOM
        bomChildrenSkuList = bomChildrenSkuList.stream().filter(v->v.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());

        //只拆分销售套装BOM
        bomChildrenSkuList = bomChildrenSkuList.stream().filter(v->v.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());

        List<String> childSkuIds = bomChildrenSkuList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        skuIds.addAll(childSkuIds);
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);

        List<RequisitionApplicationDTO.printPickingViewDTO> printPickingViewList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : requisitionApplicationDetailEntities) {

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(requisitionApplicationDetailEntity.getSkuId())
                            && req.getBomVersion().equals(requisitionApplicationDetailEntity.getBomVersion()))
                    .collect(Collectors.toList());

            //根据类型设置第三方SKU信息
            RequisitionApplicationEntity mainEntity = list.stream().filter(v->v.getId().equals(requisitionApplicationDetailEntity.getMainId())).findFirst().get();
            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = null;
            if(mainEntity.getType().equals(RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getCode())){
                List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOList = overseasProviderWarehouseService.listByWarehouseIdList(Arrays.asList(mainEntity.getChannelId()));
                if(CollectionUtils.isNotEmpty(viewDTOList)){
                    String provideCode = viewDTOList.get(0).getProviderCode();
                    if(StringUtils.isNotBlank(provideCode)){
                        listingInfoWithSkuMappingDTO = listingWithSkuMappingDTOList.stream().filter(
                                v->v.getProductSkuId().equals(requisitionApplicationDetailEntity.getSkuId()) && v.getDictPlatform().equals(provideCode) && (v.getHasMappingAll() || v.getWarehouseId().equals(mainEntity.getChannelId()))
                        ).findFirst().orElse(null);
                    }
                }
            }
            String thirdSku = "";
            if(Objects.nonNull(listingInfoWithSkuMappingDTO)){
                thirdSku = listingInfoWithSkuMappingDTO.getPlatformSkuNo();
            }
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSkuDTO : sonSkuList) {
                    RequisitionApplicationDTO.printPickingViewDTO viewDTO = new RequisitionApplicationDTO.printPickingViewDTO();
                    BeanMapper.copy(requisitionApplicationDetailEntity, viewDTO);
                    viewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                    viewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                    if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                        viewDTO.setPickingQty(requisitionApplicationDetailEntity.getApproveQty());
                    }

                    //匹配sku信息
                    SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(bomChildrenSkuDTO.getParentSkuId())).distinct().findFirst().orElse(new SkuVO());
                    viewDTO.setProductName(skuVO.getSkuName());
                    //修改仓位设置为 推荐仓位(大货区)
                    viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocationLarge()) ? "" : skuVO.getWarehouseLocationLarge());
                    viewDTO.setThirdWarehouseSku(thirdSku);
                    printPickingViewList.add(viewDTO);
                }
            } else {
                RequisitionApplicationDTO.printPickingViewDTO viewDTO = new RequisitionApplicationDTO.printPickingViewDTO();
                BeanMapper.copy(requisitionApplicationDetailEntity, viewDTO);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(requisitionApplicationDetailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                viewDTO.setProductName(skuVO.getSkuName());
                //修改仓位设置为 推荐仓位(大货区)
                viewDTO.setWarehouseLocation(StringUtils.isBlank(skuVO.getWarehouseLocationLarge()) ? "" : skuVO.getWarehouseLocationLarge());
                if (viewDTO.getPickingQty() == null || viewDTO.getPickingQty() == 0) {
                    viewDTO.setPickingQty(requisitionApplicationDetailEntity.getApproveQty());
                }
                viewDTO.setThirdWarehouseSku(thirdSku);
                printPickingViewList.add(viewDTO);
            }
        }

        printPickingViewList.sort((s1, s2) -> {
            if (StringUtils.isBlank(s1.getWarehouseLocation()) && !StringUtils.isBlank(s2.getWarehouseLocation())) {
                return 1;
            } else if (!StringUtils.isBlank(s1.getWarehouseLocation()) && StringUtils.isBlank(s2.getWarehouseLocation())) {
                return -1;
            } else if (org.apache.commons.lang3.StringUtils.isBlank(s1.getWarehouseLocation()) && org.apache.commons.lang3.StringUtils.isBlank(s2.getWarehouseLocation())) {
                return 0;
            } else {
                return s1.getWarehouseLocation().compareTo(s2.getWarehouseLocation());
            }
        });
        return printPickingViewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO cancelProcess(String id) {
        RequisitionApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
/*        // 只有待处理的单据允许撤销
        if (!Objects.equals(entity.getStatus(), RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus())) {
            throw new ServiceException(ApiError.WAIT_HANDLE_IS_CANCEL_PROCESS);
        }*/
        //待处理撤销
        if (Objects.equals(entity.getStatus(), RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus())) {
            updateApproveStatus(id, RequisitionApplicationStatusEnum.WAIT_SUBMIT.getStatus());
        } else if (Objects.equals(entity.getStatus(), RequisitionApplicationStatusEnum.HANDLE_ING.getStatus())) {
            pickingListsService.exist(id);
            //处理中撤销
            transferInfoService.requisitionApplicationCancelProcess(entity.getCode(), SourceTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
            //获取明细
            List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Collections.singletonList(entity.getId()));

            List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = getOutInStockDTOS(entity, detailEntityList);
            //执行虚拟仓入库
            if (CollectionUtils.isNotEmpty(allocationParamList)) {
                VirtualInventoryStockDTO.StockParamDTO allocationDto = new VirtualInventoryStockDTO.StockParamDTO();
                allocationDto.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_RETURN_HANDLE.getCode());
                allocationDto.setParamList(allocationParamList);
                virtualInventoryTransCoreService.approve(allocationDto);
                List<RequisitionApplicationDetailEntity> haveFromVwList = detailEntityList.stream().filter(item -> StringUtils.isNotBlank(item.getFromVirtualWarehouseId())).collect(Collectors.toList());
                Map<String, List<RequisitionApplicationDetailEntity>> haveFromVwMap = haveFromVwList.stream().collect(Collectors.groupingBy(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId));
                List<String> fromVmIds = haveFromVwList.stream().map(RequisitionApplicationDetailEntity::getFromVirtualWarehouseId).collect(Collectors.toList());

                //创建旺店通虚拟仓订单
                //service.cancelWdtOrder(fromVmIds, entity, haveFromVwMap);
            }
            updateApproveStatus(id, RequisitionApplicationStatusEnum.WAIT_HANDLE.getStatus());
            //清空明细中的虚拟仓
//            requisitionApplicationDetailService.cleanVirtualWarehouseIdByMianId(id);
        } else if (Objects.equals(entity.getStatus(), RequisitionApplicationStatusEnum.HANDLE.getStatus())) {
            if (entity.getHandleTime().isBefore(LocalDateTime.of(2024,07,27,0,0))) {
                throw new ServiceException("系统升级，不支持撤销，请联系实施人员");
            }
            //已处理
            transferInfoService.requisitionApplicationCancelProcess(entity.getCode(), SourceTypeEnum.REQUISITION_APPLICATION_FINISH.getCode());
            updateApproveStatus(id, RequisitionApplicationStatusEnum.HANDLE_ING.getStatus());
        } else {
            throw new ServiceException(ApiError.WAIT_HANDLE_IS_CANCEL_PROCESS);
        }
        log.info("撤销 开始修改要货申请状态，id：【{}】", id);

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "头程发货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "取消流程操作");
/*        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.FBA_DELIVERY.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);*/
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelWdtOrder(List<String> fromVmIds, RequisitionApplicationEntity entity, Map<String, List<RequisitionApplicationDetailEntity>> haveFromVwMap) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                List<String> fromVwId = fromVmIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                List<ThirdMappingEntity> fromThirdMappingList = dmpThirdMappingFeign.getVwListBySysIds(fromVwId);
                //保存要货申请主单
                VirtualWarehousePushHandleEntity pushHandleEntity = new VirtualWarehousePushHandleEntity(entity.getId(),
                        entity.getCode(), SourceTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getStatus(), VwAllocationDirectionEnum.FORWARD.getCode());
                virtualWarehousePushHandleService.save(pushHandleEntity);
                List<VirtualWarehousePushHandleDetailEntity> handleDetailList = new ArrayList<>();
                saveList(entity, haveFromVwMap, fromThirdMappingList, pushHandleEntity, handleDetailList, VwAllocationDirectionEnum.FORWARD);
                if (CollectionUtils.isNotEmpty(handleDetailList)) {
                    //推送中台任务:保存任务+发送mq
                    List<DmpPushTaskEntity> dmpPushTaskEntityList = syncWdtVirtualWarehousePushOrderService.saveTaskList(handleDetailList,
                            entity.getCode(), SyncOperateEnum.OPERATE_DISAPPROVE.getCode(), SourceTypeEnum.REQUISITION_APPLICATION.getCode());
                    if (CollectionUtils.isNotEmpty(dmpPushTaskEntityList)) {
//                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                            @Override
//                            public void afterCommit() {
                        //发送mq
                        dmpMqFeign.sendTask(dmpPushTaskEntityList);
//                            }
//                        });
                    }
                }
            }
        });
    }

    /**
     * 获取出入库参数
     *
     * @param entity
     * @return
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> getOutInStockDTOS(RequisitionApplicationEntity
                                                                                   entity, List<RequisitionApplicationDetailEntity> detailEntityList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(detailEntityList)) {
            detailEntityList.forEach(detailEntity -> {
                if (StringUtils.isNotBlank(detailEntity.getFromVirtualWarehouseId()) && MathUtil.compareTo(detailEntity.getVirtualFrozenQty(),MathUtil.ZERO) > MathUtil.ZERO) {                    RequisitionApplicationDTO.HandleListDTO handleListDTO = new RequisitionApplicationDTO.HandleListDTO();
                    BeanUtils.copyProperties(detailEntity, handleListDTO);
                    handleListDTO.setSourceDetailId(detailEntity.getId());
                    handleListDTO.setSourceId(entity.getId());
                    handleListDTO.setSourceCode(entity.getCode());
                    getStockParam(handleListDTO, detailEntity.getVirtualFrozenQty(), allocationParamList);
                }
            });
        }
        return allocationParamList;
    }

    @Override
    public void exportExcel(RequisitionApplicationDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("要货申请单导出", EXPORT_WMS_REQUISITION_APPLICATION.getCode(), dto);
    }

    @Override
    public BatchResultDTO delete(String id) {
        RequisitionApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到要货申请单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(RequisitionApplicationStatusEnum.WAIT_SUBMIT.getCode(), entity.getStatus())) {
            throw new ServiceException(ApiError.ERROR_1043);
        }
        pickingListsService.exist(id);
        // 删除明细数据
        requisitionApplicationDetailService.removeByMainIds(Arrays.asList(id));
        // 删除主单数据
        log.info("删除 开始删除要货申请单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除要货申请单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "要货申请单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), entity.getId(), "删除要货申请单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<RequisitionApplicationEntity> listBySourceIds(List<String> sourceIds) {
        if(CollectionUtils.isEmpty(sourceIds)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(RequisitionApplicationEntity::getSourceId, sourceIds).list();
    }

    @Override
    public List<RequisitionApplicationDTO.ChildViewDTO> listChildBySku(RequisitionApplicationDTO.ChildParamDTO dto) {
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }

        //查询历史子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(Arrays.asList(dto.getSkuId()));
        //查询最新版本的sku子件信息
        List<BomChildrenSkuDTO> bomSonItemList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuNo().equals(skuVOList.get(0).getSkuNo()) && req.getBomVersion().equals(dto.getBomVersion())).collect(Collectors.toList());

        List<RequisitionApplicationDTO.ChildViewDTO> list = new ArrayList<>();
        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomSonItemList) {
            RequisitionApplicationDTO.ChildViewDTO childViewDTO = new RequisitionApplicationDTO.ChildViewDTO();
            childViewDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
            childViewDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
            childViewDTO.setQuantity(bomChildrenSkuDTO.getQuantity());
            childViewDTO.setUsableQty(inventoryService.getUsableInventoryTotal(dto.getWarehouseId(), bomChildrenSkuDTO.getSkuId()));
            list.add(childViewDTO);
        }
        return list;
    }

    @Override
    public List<BatchResultDTO> bindShipment(List<RequisitionApplicationDTO.BindShipment> dto) {
        List<String> ids = dto.stream().map(RequisitionApplicationDTO.BindShipment::getId).distinct().collect(Collectors.toList());
        List<String> shipmentIds = dto.stream().map(RequisitionApplicationDTO.BindShipment::getShipmentId).collect(Collectors.toList());
        List<RequisitionApplicationEntity> requisitionApplicationEntities = this.listByIds(ids);
        List<FbaShipmentEntity> fbaShipmentEntities = fbaShipmentService.listByIds(shipmentIds);
        List<String> shipmentCode = fbaShipmentEntities.stream().map(FbaShipmentEntity::getCode).collect(Collectors.toList());
        List<RequisitionApplicationEntity> existBinds = lambdaQuery().in(RequisitionApplicationEntity::getFbaShipmentCode, shipmentCode).list();
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<RequisitionApplicationEntity> updateList = new ArrayList<>();
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listBySourceIds(ids);
        List<String> deliveryIds = firstMileDeliveryEntityList.stream().map(v->v.getId()).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(deliveryIds);
        //判断dto里面的shipmentId有没有重复
        if(dto.stream().map(RequisitionApplicationDTO.BindShipment::getShipmentId).distinct().count() != dto.size()){
            throw new ServiceException("货件单号重复");
        }
        for (RequisitionApplicationDTO.BindShipment bindShipment : dto) {
            RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationEntities.stream().filter(req -> req.getId().equals(bindShipment.getId())).findFirst().orElse(null);
            if(Objects.isNull(requisitionApplicationEntity)){
                resultDTOList.add(BatchResultDTO.fail(bindShipment.getId(),bindShipment.getId(), "单据不存在"));
                continue;
            }
            if(!RequisitionApplicationTypeEnum.FBA.getCode().equals(requisitionApplicationEntity.getType())){
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(),requisitionApplicationEntity.getCode(), "不是FBA要货单，无法绑定货件"));
                continue;
            }
            if(!requisitionApplicationEntity.getStatus().equals(RequisitionApplicationStatusEnum.HANDLE.getCode())){
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(),requisitionApplicationEntity.getCode(), "要货申请必须已处理才能绑定货件单号"));
                continue;
            }
            FbaShipmentEntity fbaShipmentEntity = fbaShipmentEntities.stream().filter(req -> req.getId().equals(bindShipment.getShipmentId())).findFirst().orElse(null);
            if(Objects.isNull(fbaShipmentEntity)){
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(),requisitionApplicationEntity.getCode(), "货件在系统不存在"));
                continue;
            }
            if(fbaShipmentEntity.getCode().equals(requisitionApplicationEntity.getFbaShipmentCode())){
                continue;
            }
            RequisitionApplicationEntity existBind = existBinds.stream().filter(req -> req.getFbaShipmentCode().equals(fbaShipmentEntity.getCode())).findFirst().orElse(null);
            if(Objects.nonNull(existBind)) {
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(), requisitionApplicationEntity.getCode(), "货件单号已绑定"));
                continue;
            }
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getSourceId().equals(requisitionApplicationEntity.getId())).findFirst().orElse(null);
            if(Objects.isNull(firstMileDeliveryEntity) || !firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(), requisitionApplicationEntity.getCode(), "没有关联的发货单，或发货单未审核"));
                continue;
            }
            List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailEntityList.stream().filter(v->v.getMainId().equals(firstMileDeliveryEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(firstMileDeliveryDetailList) || (StringUtils.isNotBlank(firstMileDeliveryDetailList.get(0).getFbaShipmentCode()) && StringUtils.isBlank(requisitionApplicationEntity.getFbaShipmentCode()))){
                resultDTOList.add(BatchResultDTO.fail(requisitionApplicationEntity.getId(), requisitionApplicationEntity.getCode(), "通过下推发货单绑定的货件号不允许修改"));
                continue;
            }

            requisitionApplicationEntity.setFbaShipmentCode(fbaShipmentEntity.getCode());
            updateList.add(requisitionApplicationEntity);

            String msg = StrUtil.format("用户【{}】绑定单号为【{}】货件号为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), requisitionApplicationEntity.getCode(), fbaShipmentEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.REQUISITION_APPLICATION.getCode(), requisitionApplicationEntity.getId(), "绑定货件");

            //更新头程发货单中的FBA货件号
            firstMileDeliveryDetailList.forEach(v->{
                v.setFbaShipmentCode(requisitionApplicationEntity.getFbaShipmentCode());
            });
            firstMileDeliveryDetailService.updateBatchById(firstMileDeliveryDetailList);

            fbaShipmentService.deliveryStatus(firstMileDeliveryEntity);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            service.updateBatchById(updateList);
        }
        return resultDTOList;
    }

    @Override
    public void writeBackData(List<String> sourceDetailIds,Boolean isCheck) {
        List<RequisitionApplicationDetailEntity> detailEntities = requisitionApplicationDetailService.listByIds(sourceDetailIds);
        if (CollectionUtils.isEmpty(detailEntities)) {
            throw new ServiceException("未找到要货申请明细数据");
        }
        List<String> mainIdList = detailEntities.stream().map(RequisitionApplicationDetailEntity::getMainId).collect(Collectors.toList());
        List<RequisitionApplicationEntity> requisitionApplicationList = this.listByIds(mainIdList);
        if (CollectionUtils.isEmpty(requisitionApplicationList)) {
            throw new ServiceException("未找到要货申请主表数据");
        }
        String codes = requisitionApplicationList.stream().filter(obj -> StrUtil.equals(obj.getStatus(), RequisitionApplicationStatusEnum.HANDLE.getStatus())).map(RequisitionApplicationEntity::getCode).collect(Collectors.joining(","));
        if (StrUtil.isNotBlank(codes) && isCheck) {
            throw new ServiceException(StrUtil.format("要货申请【{}】已处理不支持修改或删除拣货单",codes));
        }

        List<PickingDetailEntity> pickingDetailEntities = pickingDetailService.list(Wrappers.<PickingDetailEntity>lambdaQuery().in(PickingDetailEntity::getSourceDetailId, sourceDetailIds));
        // 回写数量，处理组合数据
        List<String> skuIds = detailEntities.stream().map(RequisitionApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIds);
        for (RequisitionApplicationDetailEntity detailEntity : detailEntities) {
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).findFirst().orElse(null);
            if (!ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(bomChildrenSkuDTO.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty / Optional.ofNullable(bomChildrenSkuDTO.getQuantity()).orElse(1));
            }else {
                Integer qty = pickingDetailEntities.stream()
                        .filter(v -> v.getSourceDetailId().equals(detailEntity.getId()))
                        .filter(v -> v.getSkuId().equals(detailEntity.getSkuId()))
                        .map(PickingDetailEntity::getQty)
                        .reduce(0, Math::addExact);
                detailEntity.setPickingQty(qty);
            }
            if (detailEntity.getApproveQty() < detailEntity.getPickingQty()) {
                throw new ServiceException(ApiError.ERROR_99133, detailEntity.getSkuNo());
            }
        }
        requisitionApplicationDetailService.updateBatchById(detailEntities);
        Map<String, Integer> qtyMap = detailEntities.stream().collect(Collectors.toMap(v->v.getId(),v->v.getPickingQty()));
        packingTaskService.updateDetailQty(qtyMap);
    }

    @Override
    public List<RequisitionApplicationDTO.GenerateDeliverViewDTO> generateDeliverView(List<String> ids) {
        List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list = baseMapper.generateDeliverView(ids);
        long count = list.stream().filter(e -> !RequisitionApplicationStatusEnum.HANDLE.getCode().equals(e.getStatus())).count();
        if (count > 1) {
            throw new ServiceException(ApiError.ERROR_99104);
        }
        if(list.stream().anyMatch(v->RequisitionApplicationTypeEnum.FBA.getCode().equals(v.getType()))){
            throw new ServiceException("FBA要货申请不支持批量下推");
        }
        List<FirstMileDeliveryEntity> entities = firstMileDeliveryService.listBySourceIds(ids);
        boolean invalidStatus = entities.stream().anyMatch(FirstMileDeliveryEntity::getInvalidStatus);
        if (CollectionUtils.isNotEmpty(entities) && Boolean.FALSE.equals(invalidStatus)) {
            throw new ServiceException(ApiError.ERROR_99104);
        }

        //根据skuId查询拥有的子sku
        List<String> skuIds = list.stream().map(RequisitionApplicationDTO.GenerateDeliverViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        List<String> sourIds = list.stream()
                .map(RequisitionApplicationDTO.GenerateDeliverViewDTO::getDeliveryPlanId)
                .collect(Collectors.toList());
        List<WmsDeliveryPlanEntity> wmsDeliveryPlanEntities = wmsDeliveryPlanService.listByIds(sourIds);
        //查询skuId产品信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<String> shopIds = list.stream().map(RequisitionApplicationDTO.GenerateDeliverViewDTO::getToWarehouseId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(shopIds);
        for (RequisitionApplicationDTO.GenerateDeliverViewDTO viewDTO : list) {

            if (RequisitionApplicationTypeEnum.FBA.getCode().equals(viewDTO.getType())) {
                ShopInfoEntity shopInfo = shopInfoEntities.stream().filter(v -> v.getId().equals(viewDTO.getToWarehouseId())).findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_92058));
                viewDTO.setToWarehouseId(shopInfo.getWarehouseId());
                viewDTO.setToWarehouseName(shopInfo.getWarehouseName());
            }
            WmsDeliveryPlanEntity wmsDeliveryPlanEntity = wmsDeliveryPlanEntities.stream()
                    .filter(v -> v.getId().equals(viewDTO.getDeliveryPlanId()))
                    .findFirst()
                    .orElse(new WmsDeliveryPlanEntity());
            viewDTO.setShopId(wmsDeliveryPlanEntity.getShopId());
            viewDTO.setShopName(wmsDeliveryPlanEntity.getShopName());
            viewDTO.setCountry(wmsDeliveryPlanEntity.getCountry());
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                viewDTO.setIsCombination(Boolean.TRUE);
            } else {
                viewDTO.setIsCombination(Boolean.FALSE);
            }
            //设置产品编号
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).findFirst().orElse(new SkuVO());
            if (ObjectUtil.isNotEmpty(skuVO)) {
                viewDTO.setProductName(skuVO.getSkuName());
            }
        }
        return list;
    }

    @Override
    public Boolean generateDeliverSave(List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.FALSE);
    }

    @Override
    public Boolean generateDeliverSaveAndSubmit(List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list) {
        return generateDeliver(list, Boolean.TRUE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO handleData(String id,Boolean isFlag) {
        RequisitionApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
        }
        if (!StrUtil.equals(entity.getStatus(),RequisitionApplicationStatusEnum.HANDLE.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
        }
        List<RequisitionApplicationDetailEntity> detailList = requisitionApplicationDetailService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isEmpty(detailList)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
        }
        //处理申请单
        if (isFlag) {
            handleApplication(entity,detailList);
            handleTransferInfo(id,detailList);
        }
        //处理发货单生成直接调拨单库存
        handleFirstMileTransferInfo(entity,detailList);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public PagingVO<RequisitionApplicationDTO.ListDTO> exportRequisitionApplication(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto) {
        Page<RequisitionApplicationDTO.ListDTO> page1 = new Page<>(dto.getCurrPage(), dto.getPageSize());
        page1.setOptimizeCountSql(false);
        Page<RequisitionApplicationDTO.ListDTO> page = baseMapper.listExport(page1, dto.getParams());
        if(CollUtil.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 数据处理
        fillList(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> fbaBindShipmentView(String id) {
        RequisitionApplicationEntity entity = Optional.ofNullable(this.getById(id)).orElseThrow(()-> new ServiceException("要货申请不存在"));
        if(!entity.getType().equals(RequisitionApplicationTypeEnum.FBA.getCode())){
            throw new ServiceException("非FBA来源无法绑定货件");
        }

        PackingTaskEntity packingTaskEntity = Optional.ofNullable(packingTaskService.getBySourceCode(entity.getCode())).orElseThrow(()-> new ServiceException("未生成装箱任务"));
        if(!PackingTaskStatusEnum.PACKED.getCode().equals(packingTaskEntity.getPackingStatus())){
            throw new ServiceException("已装箱才能下推发货单");
        }
        List<WmsCartonEntity> cartonEntityList = Optional.ofNullable(cartonService.listByTaskIds(Arrays.asList(packingTaskEntity.getId()))).filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ServiceException("装箱数据为空"));

        List<String> cartonIds = cartonEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> resultList = baseMapper.fbaBindShipmentView(cartonIds);
        resultList.forEach(v->v.setId(id));
        if(resultList.stream().noneMatch(v->StringUtils.isBlank(v.getFbaShipmentId()))){
            throw new ServiceException("要货申请所有装箱已关联货件，无法再次绑定");
        }
        return resultList;
    }

    @Override
    public RequisitionApplicationDTO.FbaBindShipmentViewDTO fbaBindShipmentMatching(RequisitionApplicationDTO.FbaBindShipmentMatchingDTO dto) {
        List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> waitMatchList = dto.getFbaBindShipmentViewDTOList();
        waitMatchList = waitMatchList.stream().filter(v->StringUtils.isBlank(v.getFbaShipmentId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(waitMatchList)){
            throw new ServiceException("待匹配为空");
        }
        FbaShipmentEntity fbaShipmentEntity = Optional.ofNullable(fbaShipmentService.getById(dto.getFbaShipmentId())).orElseThrow(()-> new ServiceException("FBA货件不存在"));
        if(!fbaShipmentEntity.getIsPackingDownload()){
            throw new ServiceException("{}货件装箱还未下载，无法绑定",fbaShipmentEntity.getCode());
        }
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntityList = fbaShipmentPackingService.getByMainIdAndBoxNo(fbaShipmentEntity.getId(),null);
        if(CollectionUtils.isEmpty(fbaShipmentPackingEntityList)){
            throw new ServiceException("{}货件装箱信息为空",fbaShipmentEntity.getCode());
        }
        if(fbaShipmentPackingEntityList.stream().anyMatch(v->StringUtils.isNotBlank(v.getCartonId()))){
            throw new ServiceException("{}货件装箱已绑定，无法重复绑定",fbaShipmentEntity.getCode());
        }
        //过滤掉混装和单装分开的
        List<FbaShipmentPackingEntity> mixedSkuList = fbaShipmentPackingEntityList.stream().filter(v->StringUtils.isBlank(v.getMsku())).collect(Collectors.toList());
        fbaShipmentPackingEntityList = fbaShipmentPackingEntityList.stream().filter(v->StringUtils.isNotBlank(v.getMsku())).collect(Collectors.toList());
        //箱号分组，组成sku*qty 匹配
        Map<String,List<FbaShipmentPackingEntity>> fbaPackingMap = fbaShipmentPackingEntityList.stream().collect(Collectors.groupingBy(FbaShipmentPackingEntity::getBoxNo));
        for(Map.Entry<String, List<FbaShipmentPackingEntity>> entry : fbaPackingMap.entrySet()) {
            String fbaBoxNo = entry.getKey();
            List<FbaShipmentPackingEntity> entityList = entry.getValue();
            List<String> fbaPackingList = entityList.stream().map(v->v.getSkuNo()+"*"+v.getQty()).collect(Collectors.toList());
            List<String> fbaFnPackingList = entityList.stream().map(v->v.getFnSku()+"*"+v.getQty()).collect(Collectors.toList());
            boolean matchResult = false;
            for (RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO fbaBindShipmentViewDetailDTO : waitMatchList) {
                List<String> packingSkuArr = Arrays.asList(fbaBindShipmentViewDetailDTO.getPackingSku().split(";"));
                if(com.common.business.utils.CollectionUtils.areListsEqualWithFrequency(fbaPackingList,packingSkuArr)){
                    matchResult = true;
                    fbaBindShipmentViewDetailDTO.setFbaShipmentId(fbaShipmentEntity.getId());
                    fbaBindShipmentViewDetailDTO.setFbaShipmentCode(fbaShipmentEntity.getCode());
                    fbaBindShipmentViewDetailDTO.setFbaBoxNo(fbaBoxNo);
                    fbaBindShipmentViewDetailDTO.setFbaPackingSku(String.join(";", fbaPackingList));
                    fbaBindShipmentViewDetailDTO.setFbaPackingFnSku(String.join(";", fbaFnPackingList));
                }
            }
            if(!matchResult){
                throw new ServiceException("【{}】的装箱信息无法匹配系统装箱，请核对要货申请是否对应该货件发货",fbaBoxNo);
            }
        }
        RequisitionApplicationDTO.FbaBindShipmentViewDTO result = new RequisitionApplicationDTO.FbaBindShipmentViewDTO();
        result.setFbaBindShipmentViewDetailDTOList(waitMatchList);
        if(CollectionUtils.isNotEmpty(mixedSkuList)){
            List<String> fbaBoxList = mixedSkuList.stream().map(v->v.getBoxNo()).distinct().collect(Collectors.toList());
            RequisitionApplicationDTO.FbaRelationDTO fbaRelationDTO = new RequisitionApplicationDTO.FbaRelationDTO();
            fbaRelationDTO.setFbaShipmentId(fbaShipmentEntity.getId());
            fbaRelationDTO.setFbaShipmentCode(fbaShipmentEntity.getCode());
            fbaRelationDTO.setFbaBoxNo(fbaBoxList);
            result.setFbaRelationDTO(fbaRelationDTO);
        }
        return result;
    }

    @Override
    public List<RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO> fbaBindShipmentDetailView(RequisitionApplicationDTO.FbaBindShipmentDetailDTO dto) {
        List<RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO> fbaBindShipmentDetailViewDTOList = baseMapper.fbaBindShipmentDetailView(dto);
        //查询产品信息
        List<String> skuIdList = fbaBindShipmentDetailViewDTOList.stream().map(RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        fbaBindShipmentDetailViewDTOList.forEach(v->{
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(v.getSkuId())).findFirst().orElse(new SkuVO());
            v.setProductName(skuVO.getSkuName());
        });
        return fbaBindShipmentDetailViewDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDeliveryWithFba(RequisitionApplicationDTO.GenerateDeliveryWithFbaDTO dto) {
        List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> detailList = dto.getFbaBindShipmentViewDTOS();
        detailList = detailList.stream().filter(v->StringUtils.isBlank(v.getDeliveryCode()) && StringUtils.isNotBlank(v.getFbaShipmentId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(detailList)){
            throw new ServiceException("请先绑定货件再下推");
        }
        if(detailList.stream().anyMatch(v->StringUtils.isBlank(v.getFbaBoxNo()))){
            throw new ServiceException("箱号不能为空");
        }
        List<String> fbaShipmentIdList = detailList.stream().map(RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO::getFbaShipmentId).distinct().collect(Collectors.toList());

        RequisitionApplicationEntity entity = this.getById(detailList.get(0).getId());
        List<RequisitionApplicationDetailEntity> detailEntityList = requisitionApplicationDetailService.listByMainIds(Arrays.asList(entity.getId()));
        RequisitionApplicationDetailEntity detailEntity = detailEntityList.get(0);
        //根据货件生成发货单
        List<FbaShipmentEntity> fbaShipmentEntityList = fbaShipmentService.listByIds(fbaShipmentIdList);
        List<FbaShipmentDetailEntity> allFbaDetailList = fbaShipmentDetailService.listByMainIds(fbaShipmentIdList);

        List<FbaShipmentPackingEntity> allFbaPackingList = fbaShipmentPackingService.listByMains(fbaShipmentIdList);
        if(CollectionUtils.isNotEmpty(allFbaPackingList)){
            List<String> errorList = allFbaDetailList.stream().map(FbaShipmentDetailEntity::getMainId).distinct().collect(Collectors.toList());
            List<String> errorCodeList = fbaShipmentEntityList.stream().filter(v->errorList.contains(v.getId())).map(FbaShipmentEntity::getCode).collect(Collectors.toList());
            throw new ServiceException("{}已绑定下推发货单，无法重复下推",errorCodeList);
        }

        //校验箱号是否连续
        Map<String,List<String>> fbaBoxMap = detailList.stream()
                .collect(Collectors.groupingBy(
                        RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO::getFbaShipmentCode,
                        Collectors.mapping(RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO::getFbaBoxNo, Collectors.toList())
                ));
        for (Map.Entry<String, List<String>> entry : fbaBoxMap.entrySet()) {
            List<Integer> fbaBoxNos = entry.getValue().stream()
                    .map(Integer::parseInt)
                    .sorted()
                    .collect(Collectors.toList());
            //校验箱号必须从1开始并且不能重复，必须连续
            for (int i = 0; i < fbaBoxNos.size(); i++) {
                if (fbaBoxNos.get(i) != i + 1) {
                    throw new ServiceException("{},FBA箱号必须从1开始并且不能重复，必须连续",entry.getKey());
                }
            }
        }

        //校验装箱SKU在FBA货件里是否存在
        for (RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO fbaBindShipmentViewDetailDTO : detailList) {
            List<String> packingSkuArr = Arrays.asList(fbaBindShipmentViewDetailDTO.getPackingSku().split(";"));
            List<String> packingFnSkuArr = Arrays.asList(fbaBindShipmentViewDetailDTO.getPackingFnSku().split(";"));
            List<FbaShipmentDetailEntity> fbaShipmentDetailEntityList = allFbaDetailList.stream().filter(v->v.getMainId().equals(fbaBindShipmentViewDetailDTO.getFbaShipmentId())).collect(Collectors.toList());
            List<String> existSkuNoList = fbaShipmentDetailEntityList.stream().map(v->v.getSkuNo()).distinct().collect(Collectors.toList());
            List<String> existFnSkuNoList = fbaShipmentDetailEntityList.stream().map(v->v.getFnSku()).distinct().collect(Collectors.toList());
            for(String skuNo:packingSkuArr){
                boolean errorFlag = true;
                for(String existSkuNo:existSkuNoList){
                    if(skuNo.contains(existSkuNo)){
                        errorFlag = false;
                        break;
                    }
                }
                if(errorFlag){
                    throw new ServiceException("装箱sku:{}关联不到货件：{}",skuNo,fbaBindShipmentViewDetailDTO.getFbaShipmentCode());
                }
            }
            for(String fnSkuNo:packingFnSkuArr){
                boolean errorFlag = true;
                for(String existFnSkuNo:existFnSkuNoList){
                    if(fnSkuNo.contains(existFnSkuNo)){
                        errorFlag = false;
                        break;
                    }
                }
                if(errorFlag){
                    throw new ServiceException("装箱FnSku:{}关联不到货件：{}",fnSkuNo,fbaBindShipmentViewDetailDTO.getFbaShipmentCode());
                }
            }
        }

        List<String> skuIdList = allFbaDetailList.stream().map(FbaShipmentDetailEntity::getSkuId).collect(Collectors.toList());
        //获取sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        ShopInfoEntity shopInfo = Optional.ofNullable(FeignQuery.getById(ShopInfoEntity.class,entity.getChannelId())).orElseThrow(()->new ServiceException("查询不到店铺"));

        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                .filter(staging -> PickingBillTypeEnum.firstLegs().contains(staging.getBillType()))
                .filter(staging -> staging.getWarehouseId().equals(detailEntity.getToWarehouseId()))
                .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));

        for (FbaShipmentEntity fbaShipmentEntity : fbaShipmentEntityList) {
            //映射主表信息
            FirstMileDeliveryDTO.AddDTO addDTO = RequisitionApplicationConverter.INSTANCE.generateFbaDeliverFDD(fbaShipmentEntity,entity,shopInfo);
            List<FbaShipmentDetailEntity> fbaDetailList = allFbaDetailList.stream().filter(v->v.getMainId().equals(fbaShipmentEntity.getId())).collect(Collectors.toList());
            //查询关联发货数量
            List<String> cartonIds = detailList.stream().filter(v->v.getFbaShipmentId().equals(fbaShipmentEntity.getId())).map(v->v.getCartonId()).collect(Collectors.toList());
            RequisitionApplicationDTO.FbaBindShipmentDetailDTO fbaBindShipmentDetailDTO = new RequisitionApplicationDTO.FbaBindShipmentDetailDTO();
            fbaBindShipmentDetailDTO.setFbaShipmentId(fbaShipmentEntity.getId());
            fbaBindShipmentDetailDTO.setMatchedCartonIds(cartonIds);
            List<RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO> fbaBindShipmentDetailViewDTOList = baseMapper.fbaBindShipmentDetailView(fbaBindShipmentDetailDTO);

            //映射详情信息
            List<FirstMileDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (FbaShipmentDetailEntity fbaShipmentDetailEntity : fbaDetailList) {
                SkuVO skuVO = skuVOList.stream().filter(v->v.getSkuId().equals(fbaShipmentDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
                FirstMileDeliveryDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.generateFbaDeliverDetailFDD(fbaShipmentDetailEntity,skuVO);
                detailAddDto.setFbaShipmentCode(fbaShipmentEntity.getCode());
                RequisitionApplicationDTO.FbaBindShipmentDetailViewDTO fbaBindShipmentDetailViewDTO = fbaBindShipmentDetailViewDTOList.stream().filter(v->v.getFnSku().equals(fbaShipmentDetailEntity.getFnSku())).findFirst().orElse(null);
                if(Objects.isNull(fbaBindShipmentDetailViewDTO) || "0".equals(fbaBindShipmentDetailViewDTO.getAssociatedDeliveryQty())){
                    throw new ServiceException("货件明细关联不到装箱fnSku,货件号{}，FNSKU：{}",fbaShipmentEntity.getCode(),fbaShipmentDetailEntity.getFnSku());
                }
                detailAddDto.setDeliveryQty(Integer.valueOf(fbaBindShipmentDetailViewDTO.getAssociatedDeliveryQty()));
                detailAddDto.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                RequisitionApplicationDetailEntity requisitionApplicationDetail = detailEntityList.stream().filter(v->v.getSkuId().equals(fbaShipmentDetailEntity.getSkuId()) && v.getPlatformFnSku().equals(fbaShipmentDetailEntity.getFnSku())).findFirst().orElse(null);
                if(Objects.isNull(requisitionApplicationDetail) ){
                    throw new ServiceException("货件明细关联不到要货申请明细,货件号{}，FNSKU：{}",fbaShipmentEntity.getCode(),fbaShipmentDetailEntity.getFnSku());
                }
                detailAddList.add(detailAddDto);
            }
            addDTO.setDetailList(detailAddList);
            BaseResultDTO.AddDTO add = firstMileDeliveryService.add(addDTO);
            List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> updateList = detailList.stream().filter(v->v.getFbaShipmentId().equals(fbaShipmentEntity.getId())).collect(Collectors.toList());
            updateList.forEach(v->{
                fbaShipmentPackingService.updateCartonId(v.getCartonId(),v.getFbaShipmentId(),v.getFbaBoxNo());
            });
        }
        //生成FBA装箱数据
        fbaShipmentPackingService.generateByBindDTO(detailList);
    }

    @Override
    public List<RequisitionApplicationDTO.DeliverRecordView> listDeliverRecord(String id) {
        RequisitionApplicationEntity requisitionApplicationEntity = Optional.ofNullable(this.getById(id)).orElseThrow(()->new ServiceException("要货申请为空"));
        if(requisitionApplicationEntity.getType().equals(RequisitionApplicationTypeEnum.FBA.getCode())){
            return baseMapper.listFbaDeliverRecord(id);
        }else{
            return baseMapper.listWarehouseDeliverRecord(id);
        }
    }

    @Override
    public void assembleDownload(List<String> ids, HttpServletResponse response) {
        List<RequisitionApplicationEntity> entityList = Optional.ofNullable(this.listByIds(ids)).filter(list -> !list.isEmpty()).orElseThrow(() -> new ServiceException("要货申请为空"));
        List<RequisitionApplicationDetailEntity> detailEntityList = Optional.ofNullable(requisitionApplicationDetailService.listByMainIds(ids)).filter(list -> !list.isEmpty()).orElseThrow(() -> new ServiceException("要货申请明细为空"));
        List<String> skuNo = detailEntityList.stream().map(RequisitionApplicationDetailEntity::getSkuNo).collect(Collectors.toList());
        //根据SKU查询BOM判断是否是组合SKU
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuNos(skuNo);
        List<RequisitionApplicationAssembleExportDTO> datas = new ArrayList<>();
        for (RequisitionApplicationDetailEntity requisitionApplicationDetailEntity : detailEntityList) {
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomChildrenList.stream().filter(v->v.getParentSkuId().equals(requisitionApplicationDetailEntity.getSkuId()) && v.getBomVersion().equals(requisitionApplicationDetailEntity.getBomVersion())&& BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomChildrenSkuDTOS)){
                continue;
            }
            RequisitionApplicationEntity requisitionApplicationEntity = entityList.stream().filter(v->v.getId().equals(requisitionApplicationDetailEntity.getMainId())).findFirst().orElse(new RequisitionApplicationEntity());
            bomChildrenSkuDTOS.forEach(v->{
                RequisitionApplicationAssembleExportDTO dto = new RequisitionApplicationAssembleExportDTO();
                dto.setCode(requisitionApplicationEntity.getCode());
                dto.setSku(requisitionApplicationDetailEntity.getSkuNo());
                dto.setAssembleQty(requisitionApplicationDetailEntity.getPickingQty());
                dto.setChildSku(v.getSkuNo());
                dto.setBomQty(v.getQuantity());
                dto.setChildQty(v.getQuantity() * requisitionApplicationDetailEntity.getPickingQty());
                datas.add(dto);
            });
        }
        if(CollectionUtils.isEmpty(datas)){
            throw new ServiceException("要货申请无需要组装的产品");
        }
        ExcelUtil.export("要货申请组装清单", "要货申请组装清单", datas, RequisitionApplicationAssembleExportDTO.class, response,new ExcelExportFillCellMergeStrategy(2,Arrays.asList(0,1)));

    }

    /**
     * 处理申请单
     */
    private void handleApplication (RequisitionApplicationEntity entity,List<RequisitionApplicationDetailEntity> detailList) {
        List<VirtualInventoryStockDTO.OutInStockDTO> allocationParamList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity detailEntity : detailList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSourceId(entity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceType(InventorySourceTypeEnum.REQUISITION_APPLICATION);
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getSkuId());
            outInStockDTO.setSkuNo(detailEntity.getSkuNo());
            outInStockDTO.setWarehouseId(detailEntity.getFromWarehouseId());
            outInStockDTO.setVirtualWarehouseId(detailEntity.getFromVirtualWarehouseId());
            outInStockDTO.setQty(detailEntity.getPickingQty());
            allocationParamList.add(outInStockDTO);
        }
        //添加可用
        VirtualInventoryStockDTO.StockParamDTO usableDTO = new VirtualInventoryStockDTO.StockParamDTO();
        usableDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.IN_USABLE.getCode());
        usableDTO.setParamList(allocationParamList);
        virtualInventoryTransCoreService.approve(usableDTO);

        //转冻结
        VirtualInventoryStockDTO.StockParamDTO frozenDTO = new VirtualInventoryStockDTO.StockParamDTO();
        frozenDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
        frozenDTO.setParamList(allocationParamList);
        virtualInventoryTransCoreService.approve(frozenDTO);
    }

    /**
     * 处理发货单生成直接调拨单库存
     */
    private void handleFirstMileTransferInfo (RequisitionApplicationEntity entity,List<RequisitionApplicationDetailEntity> detailList) {
        //头程发货单
        List<FirstMileDeliveryEntity> firstMileDeliveryList = firstMileDeliveryService.listBySourceIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isEmpty(firstMileDeliveryList)) {
            return;
        }
        //头程发货单明细
        List<String> deliveryIdList = firstMileDeliveryList.stream().map(FirstMileDeliveryEntity::getId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailList = firstMileDeliveryDetailService.listByMainIds(deliveryIdList);
        if (CollectionUtils.isEmpty(firstMileDeliveryDetailList)) {
            return;
        }
        //直接调拨单
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(deliveryIdList);
        //处理发货单生成直接调拨单库存
        TransferInfoEntity transferInfoEntity = transferInfoList.stream().filter(obj ->
                (StrUtil.equals(obj.getSourceType(), SourceTypeEnum.FIRST_MILE_DELIVERY_TO_ULANZI.getCode())
                        || StrUtil.equals(obj.getSourceType(), SourceTypeEnum.FIRST_MILE_DELIVERY.getCode()))
                        && StrUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),obj.getApproveStatus())
        ).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(transferInfoEntity)) {
            List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listByMainId(transferInfoEntity.getId());
            if (CollectionUtils.isEmpty(transferInfoDetailList)) {
                throw new ServiceException(ApiError.ERROR_99047);
            }
            //出冻结库存
            List<VirtualInventoryStockDTO.OutInStockDTO> outList = new ArrayList<>();

            for (TransferInfoDetailEntity transferInfoDetailEntity : transferInfoDetailList) {

                FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = firstMileDeliveryDetailList.stream().filter(obj -> StrUtil.equals(obj.getId(), transferInfoDetailEntity.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(firstMileDeliveryDetailEntity)) {
                    throw new ServiceException("未找到头程发货单明细");
                }
                RequisitionApplicationDetailEntity applicationDetailEntity = detailList.stream().filter(obj -> StrUtil.equals(obj.getId(), firstMileDeliveryDetailEntity.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(applicationDetailEntity)) {
                    throw new ServiceException("未找到要货申请明细");
                }
                VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSourceId(transferInfoEntity.getId());
                outInStockDTO.setSourceCode(transferInfoEntity.getCode());
                outInStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
                outInStockDTO.setSourceDetailId(transferInfoDetailEntity.getId());
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSkuId(applicationDetailEntity.getSkuId());
                outInStockDTO.setSkuNo(applicationDetailEntity.getSkuNo());
                outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
                if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                    continue;
                }
                outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
                outInStockDTO.setQty(transferInfoDetailEntity.getQty());
                outList.add(outInStockDTO);
            }
            if (CollectionUtils.isNotEmpty(outList)) {
                VirtualInventoryStockDTO.StockParamDTO stockParamDTO = new VirtualInventoryStockDTO.StockParamDTO();
                stockParamDTO.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
                stockParamDTO.setParamList(outList);
                virtualInventoryTransCoreService.approve(stockParamDTO);
            }
        }
    }
    /**
     * 处理直接调拨单库存
     */
    private void handleTransferInfo (String id,List<RequisitionApplicationDetailEntity> detailList) {
        //查直接调拨单
        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(Arrays.asList(id));
        List<TransferInfoEntity> handleList = transferInfoList.stream().filter(obj ->
                (StrUtil.equals(obj.getSourceType(), SourceTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode())
                        || StrUtil.equals(obj.getSourceType(), SourceTypeEnum.REQUISITION_APPLICATION_FINISH.getCode()))
                        && StrUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),obj.getApproveStatus())
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(handleList)) {
            List<String> transferIdList = transferInfoList.stream().map(TransferInfoEntity::getId).distinct().collect(Collectors.toList());
            List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listByMainIds(transferIdList);

            //要货申请下推直接调拨单出冻结
            List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
            for (TransferInfoDetailEntity detailEntity : transferInfoDetailList) {

                RequisitionApplicationDetailEntity applicationDetailEntity = detailList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(applicationDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_NOT_REQUISITION_APPLICATION);
                }
                //虚拟仓库存随调出仓出
                if (!StrUtil.equals(applicationDetailEntity.getFromWarehouseId(),detailEntity.getOutWarehouseId())) {
                    continue;
                }
                TransferInfoEntity transferInfoEntity = handleList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(transferInfoEntity)) {
                    throw new ServiceException(ApiError.ERROR_99047);
                }
                //调拨操作请求实体
                VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                outInStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_INFO);
                outInStockDTO.setSourceId(detailEntity.getId());
                outInStockDTO.setSourceCode(transferInfoEntity.getCode());
                outInStockDTO.setSourceDetailId(detailEntity.getId());
                outInStockDTO.setBillDate(LocalDate.now());
                outInStockDTO.setSkuId(detailEntity.getSkuId());
                outInStockDTO.setSkuNo(detailEntity.getSkuNo());
                outInStockDTO.setQty(detailEntity.getQty());
                outInStockDTO.setWarehouseId(applicationDetailEntity.getFromWarehouseId());
                if (StrUtil.isBlank(applicationDetailEntity.getFromVirtualWarehouseId())) {
                    continue;
                }
                outInStockDTO.setVirtualWarehouseId(applicationDetailEntity.getFromVirtualWarehouseId());
                paramList.add(outInStockDTO);
            }
            if (CollectionUtils.isNotEmpty(paramList)) {
                VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
                dto.setParamList(paramList);
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getCode());
                virtualInventoryTransCoreService.approve(dto);
            }
        }
    }

    /**
     * 下推发货单处理
     * @Author Luo_WG
     * @Date 2023/11/23 16:33
     * @param list 数据集
     * @param isSubmit 是否提交
     * @return java.lang.Boolean
     **/
    private Boolean generateDeliver(List<RequisitionApplicationDTO.GenerateDeliverViewDTO> list, Boolean isSubmit) {
        if(CollectionUtils.isEmpty(list)){
            return false;
        }

        //已装箱才能审核
        PackingTaskEntity taskEntity = packingTaskService.getBySourceCode(list.get(0).getSourceCode());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException("未生成装箱任务，不允许下推发货单");
        }

        List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
        List<String> noInventorySkuIds = noInventorySku.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        list = list.stream().filter(v -> noInventorySkuIds.contains(v.getSkuId()) || v.getDeliveryQty() > 0).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(list)){
            throw new ServiceException("下推明细为空");
        }
        //一个发货计划单，生成一个要发货单
        Map<String, List<RequisitionApplicationDTO.GenerateDeliverViewDTO>> map = list.stream().collect(Collectors.groupingBy(RequisitionApplicationDTO.GenerateDeliverViewDTO::getSourceId));
        List<String> sourceDetailIds = list.stream().map(RequisitionApplicationDTO.GenerateDeliverViewDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        //查询子件信息
        List<String> skuIdList = list.stream().map(RequisitionApplicationDTO.GenerateDeliverViewDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //获取sku信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuPackByIds(skuIdList);
        List<RequisitionApplicationDetailEntity> detailEntities = requisitionApplicationDetailService.listByIds(sourceDetailIds);
        for (Map.Entry<String, List<RequisitionApplicationDTO.GenerateDeliverViewDTO>> entry : map.entrySet()) {
            List<RequisitionApplicationDTO.GenerateDeliverViewDTO> value = entry.getValue();
            RequisitionApplicationDTO.GenerateDeliverViewDTO view = value.get(MathUtil.ZERO);
            if(RequisitionApplicationTypeEnum.FBA.getCode().equals(view.getType()) && StringUtils.isBlank(view.getFbaShipmentCode())){
                throw new ServiceException(StrUtil.format("要货申请{}未绑定货件单号，无法下推发货单",view.getSourceCode()));
            }
            //映射主表信息
            FirstMileDeliveryDTO.AddDTO addDTO = RequisitionApplicationConverter.INSTANCE.generateDeliverFDD(view);

            //备货类型
            addDTO.setDemandType(RequisitionApplicationTypeEnum.FBA.getCode().equals(view.getType()) ? FbaDemandTypeEnum.DEMAND_PLATFORM_WAREHOUSE.getCode():FbaDemandTypeEnum.DEMAND_OVERSEAS_WAREHOUSE.getCode());
            //来源类型
            addDTO.setSourceType(SourceTypeEnum.REQUISITION_APPLICATION.getCode());
            List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
            //映射详情信息
            List<FirstMileDeliveryDetailDTO.AddDTO> detailAddList = new ArrayList<>();
            for (RequisitionApplicationDTO.GenerateDeliverViewDTO viewDTO : value) {
                FirstMileDeliveryDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.generateDeliverDetailFDD(viewDTO);
                //查询sku是否存在子SKU
                List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(viewDTO.getSkuId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(sonSkuList)) {
                    detailAddDto.setIsCombination(Boolean.TRUE);
                } else {
                    detailAddDto.setIsCombination(Boolean.FALSE);
                }
                //映射产品信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(viewDTO.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                if (ObjectUtil.isNotEmpty(skuVO)) {
                    detailAddDto.setNetWeight(skuVO.getNetWeight());
                    detailAddDto.setProductSizeLength(LengthConverterUtil.mmToCm(skuVO.getProductLength()));
                    detailAddDto.setProductSizeWidth(LengthConverterUtil.mmToCm(skuVO.getProductWidth()));
                    detailAddDto.setProductSizeHeight(LengthConverterUtil.mmToCm(skuVO.getProductHeight()));
                }
                if (noInventorySkuIds.contains(viewDTO.getSkuId())) {
                    detailAddDto.setDeliveryQty(viewDTO.getPlanQty());
                    detailAddDto.setPlanQty(viewDTO.getPlanQty());
                }else {
                    detailAddDto.setDeliveryQty(viewDTO.getDeliveryQty());
                    detailAddDto.setPlanQty(viewDTO.getDeliveryQty());
                }
                RequisitionApplicationDetailEntity detailEntity = detailEntities.stream()
                        .filter(v -> v.getId().equals(viewDTO.getSourceDetailId()))
                        .findFirst()
                        .orElse(new RequisitionApplicationDetailEntity());
                if (viewDTO.getDeliveryWarehouseId().equals(detailEntity.getToWarehouseId())) {
                    // 获取仓库暂存区默认配置
                    CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                            .filter(staging -> PickingBillTypeEnum.firstLegs().contains(staging.getBillType()))
                            .filter(staging -> staging.getWarehouseId().equals(detailEntity.getToWarehouseId()))
                            .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
                    detailAddDto.setWarehouseLocation(pickingStaging.getWarehouseLocation());
                } else {
                    detailAddDto.setWarehouseLocation("");
                }
                detailAddDto.setFbaShipmentCode(viewDTO.getFbaShipmentCode());
                detailAddList.add(detailAddDto);
            }

            addDTO.setDetailList(detailAddList);
            BaseResultDTO.AddDTO add = firstMileDeliveryService.add(addDTO);
            if (Boolean.TRUE.equals(isSubmit)) {
                firstMileDeliveryService.submit(add.getId());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<RequisitionApplicationDTO.PickingViewDTO> generatePickingView(PagingDTO<RequisitionApplicationDTO.GetPickingViewDTO> page) {
        //判断是否存在下游单据，已有下游单据就不能再生成拣货单
        FirstMileDeliveryEntity firstMileDelivery = firstMileDeliveryService.findBySourceId(page.getParams().getId());
        if (ObjectUtil.isNotEmpty(firstMileDelivery)) {
            throw new ServiceException(ApiError.ERROR_99110, "头程发货单");
        }
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkus = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        IPage<RequisitionApplicationDTO.PickingViewDTO> picking = baseMapper.pagingPicking(new Page<>(page.getCurrPage(), page.getPageSize()), page.getParams(), ignoreInventorySkus);
        return new PagingVO<>(picking);
    }

    @Override
    public void generatePickingList(RequisitionApplicationDTO.GeneratePickingDTO picking) {
        RequisitionApplicationEntity application = getById(picking.getId());
        if (ObjectUtil.isEmpty(application)) {
            throw new ServiceException(ApiError.ERROR_BILL_NOT_EXIST);
        }
        if (!RequisitionApplicationStatusEnum.getPickingList().contains(application.getStatus())) {
            throw new ServiceException(ApiError.HANDLE_ING_OR_HANDLE_IS_PRINT_PICKING);
        }
        List<RequisitionApplicationDetailEntity> details = requisitionApplicationDetailService.list(Wrappers.<RequisitionApplicationDetailEntity>lambdaQuery()
                .eq(RequisitionApplicationDetailEntity::getMainId, picking.getId())
                .in(RequisitionApplicationDetailEntity::getId, picking.getDetailIds())
        );
        //判断sku是否被他人生成了拣货单
        boolean checkUnpickedQty = details.stream().allMatch(detail -> (detail.getApproveQty() - detail.getPickingQty()) > 0);
        if (Boolean.FALSE.equals(checkUnpickedQty)) {
            throw new ServiceException(ApiError.UNPICKED_QUANTITY_SHORTAGE);
        }
        details = details.stream().filter(v->v.getApproveQty()>0).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(details)){
            throw new ServiceException("批准数量为0无法生成拣货明细");
        }
        PickingListsDTO.AddDTO addDTO = new PickingListsDTO.AddDTO();
        addDTO.setBillType(RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType()) ?
                PickingBillTypeEnum.FBA.getCode() : PickingBillTypeEnum.THIRD.getCode());
        if (RequisitionApplicationTypeEnum.FBA.getCode().equals(application.getType())) {
            ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(application.getChannelId());
            addDTO.setDeliveryWarehouseId(shopInfo.getWarehouseId());
            addDTO.setCountryCode(shopInfo.getDictCountryCode());
        }else {
            OverseasProviderWarehouseEntity overseasProviderWarehouseEntity = overseasProviderWarehouseService.getByWarehouseId(application.getChannelId());
            if(Objects.nonNull(overseasProviderWarehouseEntity)){
                addDTO.setCountryCode(overseasProviderWarehouseEntity.getCountry());
            }
            addDTO.setDeliveryWarehouseId(application.getChannelId());
        }
        addDTO.setSourceId(application.getId());
        addDTO.setSourceType(SourceTypeEnum.REQUISITION_APPLICATION.getCode());
        addDTO.setSourceCode(application.getCode());
        List<RequisitionApplicationDetailEntity> updateDetails = new ArrayList<>();
        List<PickingDetailDTO.AddDTO> detailList = new ArrayList<>();
        for (RequisitionApplicationDetailEntity detailEntity : details) {
            PickingDetailDTO.AddDTO detail = new PickingDetailDTO.AddDTO();
            detail.setWarehouseId(detailEntity.getToWarehouseId());
            detail.setWarehouseName(detailEntity.getToWarehouseName());
            detail.setSkuId(detailEntity.getSkuId());
            detail.setSkuNo(detailEntity.getSkuNo());
            detail.setQty(detailEntity.getApproveQty() - detailEntity.getPickingQty());
            detail.setSourceDetailId(detailEntity.getId());
            detail.setBomVersion(detailEntity.getBomVersion());
            detailList.add(detail);
            detailEntity.setPickingQty(detailEntity.getApproveQty());
            updateDetails.add(detailEntity);

        }
        addDTO.setDetails(detailList);
        pickingListsService.add(addDTO);
        requisitionApplicationDetailService.updateBatchById(updateDetails);
        //生成装箱任务
        packingTaskService.addPackingByRequisition(application);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationEntity requisitionApplicationEntity) {
        String requisitionWarehouseId = requisitionApplicationEntity.getRequisitionWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouse = warehouseService.listWarehouseByIds(Arrays.asList(requisitionWarehouseId));
        requisitionApplicationEntity.setRequisitionWarehouseName(warehouse.get(MathUtil.ZERO).getName());
        if(StringUtils.isBlank(requisitionApplicationEntity.getChannelName())){
            if(StringUtils.isBlank(requisitionApplicationEntity.getChannelId())){
                return;
            }
            String channelName = "";
            if(RequisitionApplicationTypeEnum.FBA.getCode().equals(requisitionApplicationEntity.getType())){
                ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(requisitionApplicationEntity.getChannelId());
                if(Objects.nonNull(shopInfoEntity)){
                    channelName = shopInfoEntity.getName();
                }
            }
            if(RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getCode().equals(requisitionApplicationEntity.getType())){
                WarehouseEntity warehouseEntity = warehouseService.getById(requisitionApplicationEntity.getChannelId());
                if(Objects.nonNull(warehouseEntity)){
                    channelName =  warehouseEntity.getName();
                }
            }
            requisitionApplicationEntity.setChannelName(channelName);
        }
    }

    /**
     * 详情字段处理
     */
    private void fillOne(RequisitionApplicationDTO.ViewDTO
                                 data, List<RequisitionApplicationDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);

        //来源类型中文
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        //要货类型中文
        data.setTypeName(RequisitionApplicationTypeEnum.getName(data.getType()));
        //单据状态中文
        data.setStatusName(RequisitionApplicationStatusEnum.getName(data.getStatus()));

        //详情字段设置
        List<RequisitionApplicationDetailDTO.ViewDTO> viewDetailList = new ArrayList<>();

        //查询第三方SKU信息
//        List<ListingInfoWithSkuMappingDTO> listingWithSkuMappingDTOList = skuMappingFeign.listByErpSkuIdAndType(skuIdList,"");

        for (RequisitionApplicationDetailEntity detailEntity : detailList) {
            RequisitionApplicationDetailDTO.ViewDTO detailView = RequisitionApplicationConverter.INSTANCE.radEntityToRadDto(detailEntity);

            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
            detailView.setProductName(skuVO.getSkuName());
            detailView.setImageUrl(skuVO.getSkuImagesUrl());
            detailView.setUsableQty(inventoryService.getUsableInventoryTotal(data.getRequisitionWarehouseId(), skuVO.getSkuId()));

            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                detailView.setIsCombination(Boolean.TRUE);
            } else {
                detailView.setIsCombination(Boolean.FALSE);
            }
            detailView.setAsin(detailEntity.getPlatformSpu());
            detailView.setPlatformSku(detailEntity.getPlatformSku());
            detailView.setThirdWarehouseSku(detailEntity.getPlatformSku());
            //根据类型设置第三方SKU信息
//            ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = null;
//            if(data.getType().equals(RequisitionApplicationTypeEnum.OVERSEAS_WAREHOUSE.getCode())){
//                List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOList = overseasProviderWarehouseService.listByWarehouseIdList(Arrays.asList(data.getChannelId()));
//                if(CollectionUtils.isNotEmpty(viewDTOList)){
//                    String provideCode = viewDTOList.get(0).getProviderCode();
//                    listingInfoWithSkuMappingDTO = listingWithSkuMappingDTOList.stream().filter(
//                            v->v.getProductSkuId().equals(detailEntity.getSkuId()) && v.getDictPlatform().equals(provideCode) && (v.getHasMappingAll() || v.getWarehouseId().equals(data.getChannelId()))
//                    ).findFirst().orElse(null);
//                }
//            }

//            if(data.getType().equals(RequisitionApplicationTypeEnum.SALES_PLATFORM.getCode())){
//                if(StringUtils.isNotBlank(data.getChannelId())){
//                    listingInfoWithSkuMappingDTO = listingWithSkuMappingDTOList.stream().filter(v->v.getProductSkuId().equals(detailEntity.getSkuId()) && v.getShopId().equals(data.getChannelId())).findFirst().orElse(null);
//                }
//            }

//            if(Objects.nonNull(listingInfoWithSkuMappingDTO)){
//                detailView.setThirdWarehouseSku(listingInfoWithSkuMappingDTO.getPlatformSkuNo());
//                detailView.setPlatformSkuNo(listingInfoWithSkuMappingDTO.getPlatformSkuNo());
//                detailView.setPlatformSkuName(listingInfoWithSkuMappingDTO.getPlatformSkuName());
//                detailView.setPlatformFnSku(listingInfoWithSkuMappingDTO.getPlatformFnSku());
//            }

            viewDetailList.add(detailView);
        }

        data.setDetailList(viewDetailList);
    }

    /**
     * 分页查询数据处理
     *
     * @param list
     */
    private void fillList(List<RequisitionApplicationDTO.ListDTO> list) {
        //查询产品信息
        List<String> skuIdList = list.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        //获取子SKU集合
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        List<String> ids = list.stream().map(RequisitionApplicationDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<FirstMileDeliveryEntity> firstMileDeliveryEntityList = firstMileDeliveryService.listBySourceIds(ids);
        List<String> deliveryIds = firstMileDeliveryEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(deliveryIds);
        List<OverseasWarehouseInboundEntity> overseasWarehouseInboundEntityList = overseasWarehouseInboundService.listBySourceIds(deliveryIds);
        Map<String,Integer> qtyMap = new HashMap<>();
        for (RequisitionApplicationDTO.ListDTO listDTO : list) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(listDTO.getPackingStatus())) {
                listDTO.setPackingStatusName(PackingTaskStatusEnum.getName(listDTO.getPackingStatus()));
            }else{
                listDTO.setPackingStatus(PackingTaskStatusEnum.WAIT.getCode());
                listDTO.setPackingStatusName(PackingTaskStatusEnum.WAIT.getName());
            }
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuDTOS.stream()
                    .filter(req -> req.getParentSkuId().equals(listDTO.getSkuId())
                            && req.getBomVersion().equals(listDTO.getBomVersion())
                    ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                listDTO.setIsCombination(Boolean.TRUE);
            } else {
                listDTO.setIsCombination(Boolean.FALSE);
            }
            //产品信息
            SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(listDTO.getSkuId())).findFirst().orElse(new SkuVO());
            listDTO.setProductName(skuVO.getSkuName());
            //状态中文
            listDTO.setStatusName(RequisitionApplicationStatusEnum.getName(listDTO.getStatus()));
            //要货类型中文
            listDTO.setTypeName(RequisitionApplicationTypeEnum.getName(listDTO.getType()));

            //如果装箱数量大于拣货数量，拆分处理
            if(Objects.nonNull(listDTO.getPickingQty()) && Objects.nonNull(listDTO.getPackingQty()) && listDTO.getPackingQty() > listDTO.getPickingQty()){
                String key = listDTO.getId() + listDTO.getSkuId();
                if(qtyMap.containsKey(key)){
                    Integer reduceQty = qtyMap.get(key);
                    if(reduceQty > listDTO.getPickingQty()){
                        listDTO.setPackingQty(listDTO.getPickingQty());
                        qtyMap.put(key,reduceQty - listDTO.getPickingQty());
                    }else{
                        listDTO.setPackingQty(reduceQty);
                        qtyMap.put(key,0);
                    }
                }else{
                    qtyMap.put(key,listDTO.getPackingQty() - listDTO.getPickingQty());
                    listDTO.setPackingQty(listDTO.getPickingQty());
                }
            }
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryEntityList.stream().filter(v->v.getSourceId().equals(listDTO.getId())).findFirst().orElse(new FirstMileDeliveryEntity());
            if(RequisitionApplicationTypeEnum.THIRD_WAREHOUSE.getCode().equals(listDTO.getType())){
                OverseasWarehouseInboundEntity overseasWarehouseInboundEntity = overseasWarehouseInboundEntityList.stream().filter(v->v.getSourceId().equals(firstMileDeliveryEntity.getId())).findFirst().orElse(new OverseasWarehouseInboundEntity());
                listDTO.setFbaShipmentCode(overseasWarehouseInboundEntity.getCode());
            }else{
                FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = firstMileDeliveryDetailEntityList.stream().filter(v->v.getMainId().equals(firstMileDeliveryEntity.getId())).findFirst().orElse(new FirstMileDeliveryDetailEntity());
                listDTO.setFbaShipmentCode(firstMileDeliveryDetailEntity.getFbaShipmentCode());
            }
        }
    }

    /**
     * 提交状态校验
     */
    private void validateSubmit(RequisitionApplicationEntity entity) {
        // 待提交允许提交
        if (!entity.getStatus().equals(RequisitionApplicationStatusEnum.WAIT_SUBMIT.getStatus())) {
            throw new ServiceException(ApiError.IS_SUBMIT_IN_SUBMIT);
        }
        return;
    }

    /**
     * 更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String status) {
        lambdaUpdate().eq(RequisitionApplicationEntity::getId, id)
                .set(RequisitionApplicationEntity::getStatus, status)
                .update(new RequisitionApplicationEntity());
    }

    /**
     * 修改处理信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param raIds 要货申请表id
     * @return java.lang.Boolean
     **/

    private Boolean updateHandleDate(List<String> raIds, String status) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        return lambdaUpdate().set(RequisitionApplicationEntity::getHandleUserId, userInfo.getUid())
                .set(RequisitionApplicationEntity::getHandleUserName, userInfo.getUserName())
                .set(RequisitionApplicationEntity::getHandleTime, LocalDateTime.now())
                .set(RequisitionApplicationEntity::getStatus, status)
                .in(RequisitionApplicationEntity::getId, raIds)
                .update();
    }

    /**
     * 修改处理详情信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param list 要货申请表id
     * @param warehouseList 要货申请表id
     * @return java.lang.Boolean
     **/
    private void updateHandleDetailDate(List<RequisitionApplicationDTO.HandleListDTO> list, List<WarehouseDTO.UpdateDTO> warehouseList) {
        for (RequisitionApplicationDTO.HandleListDTO handleListDTO : list) {
            //调入仓
            WarehouseDTO.UpdateDTO toWarehouse = warehouseList.stream()
                    .filter(req -> req.getId().equals(handleListDTO.getToWarehouseId()))
                    .findFirst()
                    .orElse(new WarehouseDTO.UpdateDTO());

            //调出仓
            WarehouseDTO.UpdateDTO fromWarehouse = warehouseList.stream()
                    .filter(req -> req.getId().equals(handleListDTO.getFromWarehouseId()))
                    .findFirst()
                    .orElse(new WarehouseDTO.UpdateDTO());
            requisitionApplicationDetailService.updateTransferWarehouse(handleListDTO.getFromWarehouseId(), fromWarehouse.getName(),
                    handleListDTO.getFromVirtualWarehouseId(), handleListDTO.getFromVirtualWarehouseName(),
                    handleListDTO.getToWarehouseId(), toWarehouse.getName(), handleListDTO.getApproveQty(), handleListDTO.getSourceDetailId());
        }
    }

    /**
     * 修改完成详情的拣货数量信息
     * @Author Luo_WG
     * @Date 2023/11/24 10:52
     * @param list 要货申请表id
     * @return java.lang.Boolean
     **/
    private void updateFinishDetailPickingQty(List<RequisitionApplicationDTO.FinishListDTO> list) {
        for (RequisitionApplicationDTO.FinishListDTO handleListDTO : list) {
            requisitionApplicationDetailService.updateFinishDetailPickingQty(handleListDTO.getPickingQty(), handleListDTO.getSourceDetailId());
        }
    }

    /**
     * 要货申请处理生成调拨单
     * @Author Luo_WG
     * @Date 2023/11/24 10:35
     * @param detailEntityList 调出仓库和调入仓库不一致的单据详情
     * @param skuVOList 单据的产品信息
     * @param bomChildrenSkuList 子件信息
     * @return java.lang.String
     **/
    private String generateHandleToTransferInfo(List<RequisitionApplicationDTO.HandleListDTO> detailEntityList,
                                                List<SkuVO> skuVOList,
                                                List<BomChildrenSkuDTO> bomChildrenSkuList) {
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();

        //默认来源类型：海外发货计划
        addDTO.setSourceType(SourceTypeEnum.REQUISITION_APPLICATION_HANDLE.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调出仓
        addDTO.setOutOrgId(detailEntityList.get(0).getOutOrgId());
        //调入仓
        addDTO.setInOrgId(detailEntityList.get(0).getInOrgId());
        //调拨类型
        if (addDTO.getInOrgId().equals(addDTO.getOutOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }
        addDTO.setSourceId(detailEntityList.get(0).getSourceId());
        addDTO.setSourceCode(detailEntityList.get(0).getSourceCode());
        addDTO.setRemark("");
        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        for (RequisitionApplicationDTO.HandleListDTO detailEntity : detailEntityList) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());

            // 子件需要拆分
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSku : sonSkuList) {
                    //映射信息
                    TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radHandleListToTransferInfoDetail(detailEntity);
                    detailAddDto.setSkuId(bomChildrenSku.getSkuId());
                    detailAddDto.setSkuNo(bomChildrenSku.getSkuNo());
                    detailAddDto.setQty(detailEntity.getApproveQty() * bomChildrenSku.getQuantity());
                    detailAddDtoList.add(detailAddDto);
                }
            } else {
                //映射信息
                TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radHandleListToTransferInfoDetail(detailEntity);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                detailAddDto.setSkuId(skuVO.getSkuId());
                detailAddDto.setSkuNo(skuVO.getSkuNo());
                detailAddDto.setQty(detailEntity.getApproveQty());
                detailAddDtoList.add(detailAddDto);
            }
        }

        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

    /**
     * 要货申请完成生成调拨单
     * @Author Luo_WG
     * @Date 2023/11/24 10:35
     * @param detailEntityList 调出仓库和调入仓库不一致的单据详情
     * @param skuVOList 单据的产品信息
     * @param bomChildrenSkuList 子件信息
     * @return java.lang.String
     **/
    private String generateFinishToTransferInfo(List<RequisitionApplicationDTO.FinishListDTO> detailEntityList,
                                                List<SkuVO> skuVOList,
                                                List<BomChildrenSkuDTO> bomChildrenSkuList
    ) {
        TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();

        //默认来源类型：要货申请
        addDTO.setSourceType(SourceTypeEnum.REQUISITION_APPLICATION_FINISH.getCode());
        //默认调出日期：当前日期
        addDTO.setBillDate(LocalDate.now());
        //默认调拨方向：普通
        addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
        //调出仓
        addDTO.setOutOrgId(detailEntityList.get(0).getOutOrgId());
        //调入仓
        addDTO.setInOrgId(detailEntityList.get(0).getInOrgId());
        //调拨类型
        if (addDTO.getInOrgId().equals(addDTO.getOutOrgId()))  {
            addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
        } else {
            addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
        }

        addDTO.setSourceId(detailEntityList.get(0).getSourceId());
        addDTO.setSourceCode(detailEntityList.get(0).getSourceCode());
        addDTO.setRemark("");

        //详情信息
        List<TransferInfoDetailDTO.AddDTO> detailAddDtoList = new ArrayList<>();
        List<CfgRulePickingStagingEntity> warehouseStagingList = cfgRulePickingStagingService.list();
        for (RequisitionApplicationDTO.FinishListDTO detailEntity : detailEntityList) {
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())
                            && req.getBomVersion().equals(detailEntity.getBomVersion())
                            && BomTypeEnum.COMBINATION.getType().equals(req.getType())
                    ).collect(Collectors.toList());
            // 获取仓库暂存区默认配置
            CfgRulePickingStagingEntity pickingStaging = warehouseStagingList.stream()
                    .filter(staging -> PickingBillTypeEnum.firstLegs().contains(staging.getBillType()))
                    .filter(staging -> staging.getWarehouseId().equals(detailEntity.getToWarehouseId()))
                    .findFirst().orElseThrow(() -> new ServiceException(ApiError.ERROR_99088));
            // 子件需要拆分
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                for (BomChildrenSkuDTO bomChildrenSku : sonSkuList) {
                    //映射信息
                    TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radFinishListToTransferInfoDetail(detailEntity);
                    detailAddDto.setSkuId(bomChildrenSku.getSkuId());
                    detailAddDto.setSkuNo(bomChildrenSku.getSkuNo());
                    detailAddDto.setQty(detailEntity.getPickingQty() * bomChildrenSku.getQuantity());

                    //虚拟仓暂无仓位
                    detailAddDto.setOutWarehouseLocation(pickingStaging.getWarehouseLocation());
                    detailAddDtoList.add(detailAddDto);
                }
            } else {
                //映射信息
                TransferInfoDetailDTO.AddDTO detailAddDto = RequisitionApplicationConverter.INSTANCE.radFinishListToTransferInfoDetail(detailEntity);
                //匹配sku信息
                SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).distinct().findFirst().orElse(new SkuVO());
                detailAddDto.setSkuId(skuVO.getSkuId());
                detailAddDto.setSkuNo(skuVO.getSkuNo());
                detailAddDto.setQty(detailEntity.getPickingQty());
                //虚拟仓暂无仓位
                detailAddDto.setOutWarehouseLocation(pickingStaging.getWarehouseLocation());
                detailAddDtoList.add(detailAddDto);
            }
        }
        addDTO.setDetailList(detailAddDtoList);
        return transferInfoService.add(addDTO);
    }

    /**
     * 仓库关联虚拟仓远程搜索
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<RequisitionApplicationDTO.WarehouseListDTO> pagingSelect
    (PagingDTO<RequisitionApplicationDTO.WarehouseSelectDTO> dto) {
        String virtualWarehouseId = dto.getParams().getVirtualWarehouseId();
        //判断当前虚拟仓是否为空，不为空获取所有关联的实体仓
        if (StringUtils.isNotBlank(virtualWarehouseId)) {
            List<VirtualWarehouseRelationEntity> vwRelationList = virtualWarehouseRelationService.getByVirtualWarehouseId(virtualWarehouseId);
            if (CollectionUtils.isEmpty(vwRelationList)) {
                return new PagingVO<>();
            } else {
                List<String> ids = vwRelationList.stream().map(VirtualWarehouseRelationEntity::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                dto.getParams().setIds(ids);
            }
        }

        PagingDTO<WarehouseDTO.SelectDTO> warehouseDto = new PagingDTO<>();
        warehouseDto.setPageSize(dto.getPageSize());
        warehouseDto.setCurrPage(dto.getCurrPage());
        WarehouseDTO.SelectDTO warehouseSelectDTO = new WarehouseDTO.SelectDTO();
        BeanUtils.copyProperties(dto.getParams(), warehouseSelectDTO);
        warehouseDto.setParams(warehouseSelectDTO);
        PagingVO<WarehouseDTO.ListDTO> warehouseList = warehouseService.selectPaging(warehouseDto);
        List<?> list = warehouseList.getList();
        List<RequisitionApplicationDTO.WarehouseListDTO> warehouseListDTOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(warehouse -> {
                RequisitionApplicationDTO.WarehouseListDTO warehouseListDTO = new RequisitionApplicationDTO.WarehouseListDTO();
                WarehouseDTO.ListDTO listDTO = (WarehouseDTO.ListDTO) warehouse;
                List<VirtualWarehouseRelationEntity> warehouseRelationEntities = virtualWarehouseRelationService.getByWarehouseId(Collections.singletonList(((WarehouseDTO.ListDTO) warehouse).getId()));
                BeanUtils.copyProperties(listDTO, warehouseListDTO);
                if (!Objects.equals(ApproveStatusEnum.APPROVE, listDTO.getApproveStatus())) {
                    warehouseListDTO.setDisabled(true);
                    warehouseListDTO.setCanCheck(false);
                }
                if (CollectionUtils.isNotEmpty(warehouseRelationEntities)) {
                    warehouseListDTO.setHasVw(true);
                }
                warehouseListDTOS.add(warehouseListDTO);
            });
        }
        PagingVO<RequisitionApplicationDTO.WarehouseListDTO> resultPage = new PagingVO<>();
        resultPage.setPageSize(dto.getPageSize());
        resultPage.setCurrPage(dto.getCurrPage());
        resultPage.setList(warehouseListDTOS);
        return resultPage;
    }
    @Override
    public BatchResultDTO generatePackingTask(RequisitionApplicationEntity entity) {
        packingTaskService.addPackingByRequisition(entity);
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"操作成功");
    }

    @Override
    public List<RequisitionApplicationEntity> listByCodes(List<String> codes) {
        if(CollectionUtils.isEmpty(codes)){
            return new ArrayList<>();
        }
        return lambdaQuery().in(RequisitionApplicationEntity::getCode,codes).list();
    }
}
