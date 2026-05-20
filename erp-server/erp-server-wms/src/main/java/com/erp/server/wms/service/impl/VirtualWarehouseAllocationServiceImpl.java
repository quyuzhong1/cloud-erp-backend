package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.VwAllocationAllocationCancelExcelDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationExcelDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationTransferExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.listener.VirtualWarehouseAllocationCancelExcelListener;
import com.erp.server.wms.listener.VirtualWarehouseAllocationExcelListener;
import com.erp.server.wms.listener.VirtualWarehouseAllocationTransferExcelListener;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_STATISTICS;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_WAREHOUSE_ALLOCATION;
import static java.util.stream.Collectors.groupingBy;

/**
 * <p>
 * 虚拟仓分货单 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-05
 */
@Slf4j
@Service
public class VirtualWarehouseAllocationServiceImpl extends SuperServiceImpl<VirtualWarehouseAllocationMapper, VirtualWarehouseAllocationEntity> implements VirtualWarehouseAllocationService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private VirtualWarehouseService virtualWarehouseService;
    @Resource
    private VirtualInventoryService virtualInventoryService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;
    @Resource
    @Lazy
    private VirtualWarehouseAllocationServiceImpl service;

    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private InventoryService inventoryService;
    @Resource
    private TransferInfoService transferInfoService;
    @Resource
    private CfgRulePickingService cfgRulePickingService;
    @Resource
    private VirtualWarehousePushHandleDetailService virtualWarehousePushHandleDetailService;

    @Resource
    private CfgSettingVirtualService cfgSettingVirtualService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SyncWdtVirtualWarehousePushOrderService syncWdtVirtualWarehousePushOrderService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    private static final int size = 2000;
    private static final String splitStr = "_&_";


    /**
     * 新增
     *
     * @param addDTO
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = new VirtualWarehouseAllocationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationEntity);
        // 数据处理
        handleData(virtualWarehouseAllocationEntity, addDTO.getDetailList());
        log.info("开始新增分货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FH);
        virtualWarehouseAllocationEntity.setCode(code);
        boolean save = super.save(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("分货单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单", virtualWarehouseAllocationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), virtualWarehouseAllocationEntity.getId(), "新增操作");
        // 新增明细
        virtualWarehouseAllocationDetailService.batchAdd(addDTO, virtualWarehouseAllocationEntity.getId());
        //保存附件
        wmsAttachmentService.batchSaveNotDel(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), WmsConstant.QC_PRODUCT, virtualWarehouseAllocationEntity.getId());
        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationEntity old = Optional.ofNullable(super.getById(updateDTO.getId())).orElseThrow(() ->
                new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "分货单"));
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = BeanMapperUtils.map(VirtualWarehouseAllocationEntity.class, updateDTO);
        // 数据处理
        handleData(virtualWarehouseAllocationEntity, updateDTO.getDetailList());
        log.info("编辑 开始修改分货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("分货单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录分货单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "分货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), old.getId(), "编辑操作");

        // 新增明细
        virtualWarehouseAllocationDetailService.batchUpdate(updateDTO, virtualWarehouseAllocationEntity.getId());
        //保存附件
        wmsAttachmentService.batchSaveNotDel(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), WmsConstant.QC_PRODUCT, virtualWarehouseAllocationEntity.getId());
        return Boolean.TRUE;
    }

    /**
     * 预览
     *
     * @param id
     * @author hyj
     * @date: 2024-06-07
     */
    @Override
    public VirtualWarehouseAllocationDTO.ViewDTO view(String id) {
        VirtualWarehouseAllocationEntity vmAllocation = this.getById(id);
        if (Objects.isNull(vmAllocation)) {
            throw new ServiceException(ApiError.VM_ALLOCATION_NOT_FOUND, id);
        }
        VirtualWarehouseAllocationDTO.ViewDTO viewDTO = new VirtualWarehouseAllocationDTO.ViewDTO();
        BeanUtils.copyProperties(vmAllocation, viewDTO);
        //获取附件
        getAttachment(id, viewDTO);

        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, id).orderByAsc(VirtualWarehouseAllocationDetailEntity::getId));

        List<String> detailIdList = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getId).distinct().collect(Collectors.toList());
        List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> thirdList =  virtualWarehousePushHandleDetailService.listThirdDataByDetailIdList(detailIdList);

        //实体仓信息
        List<String> toWarehouseIdList = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getToWarehouseId).distinct().collect(Collectors.toList());
        List<WarehouseEntity> toWarehouseList = CollUtil.isEmpty(toWarehouseIdList) ? Collections.emptyList() : warehouseService.listByIds(toWarehouseIdList);
        Map<String, String> toWarehouseNameMap = CollUtil.isEmpty(toWarehouseList) ? new HashMap<>() : toWarehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));

        //获取数量
        VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO = new VirtualInventoryDTO.QtyTypeDTO();
        qtyTypeDTO.setType(vmAllocation.getType());
        List<VirtualInventoryDTO.QtySearchDTO> qtySearchList = BeanMapperUtils.copyList(VirtualInventoryDTO.QtySearchDTO.class, detailList);
        qtyTypeDTO.setQtySearchList(qtySearchList);
        List<VirtualInventoryDTO.ViewQtyDTO> qtyDTOList = virtualInventoryService.getQty(qtyTypeDTO);
        //获取所有的sku信息
        List<String> skuIds = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailList);
        detailDtos.forEach(detailDto -> {
            SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                detailDto.setProductName(skuVO.getSkuName());
                detailDto.setImageUrl(skuVO.getSkuImagesUrl());
            }
            //借调实体仓名称
            detailDto.setToWarehouseName(toWarehouseNameMap.get(detailDto.getToWarehouseId()));
            qtyDTOList.forEach(qtyDTO -> {
                if (Objects.equals(qtyDTO.getWarehouseId(), detailDto.getWarehouseId()) && Objects.equals(qtyDTO.getSkuId(), detailDto.getSkuId())) {
                    detailDto.setWarehouseUsableQty(qtyDTO.getWarehouseAllocationQty());
                    if (Objects.equals(qtyDTO.getFromVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())) {
                        detailDto.setFromVirtualWarehouseUsableQty(qtyDTO.getFromVirtualWarehouseUsableQty());
                    }
                    if (Objects.equals(qtyDTO.getToVirtualWarehouseId(), detailDto.getToVirtualWarehouseId())) {
                        detailDto.setToVirtualWarehouseUsableQty(qtyDTO.getToVirtualWarehouseUsableQty());
                    }
                }
            });

            detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.TO_BE_SYNC.getCode());
            List<VirtualWarehousePushHandleDetailDTO.ThirdDataDTO> thisThirdList = thirdList.stream().filter(obj -> CharSequenceUtil.equals(obj.getDetailId(), detailDto.getId())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(thisThirdList)) {
                //同步平台单号
                String thirdCodes = thisThirdList.stream().filter(obj-> CharSequenceUtil.isNotBlank(obj.getThirdCode())).map(VirtualWarehousePushHandleDetailDTO.ThirdDataDTO::getThirdCode).collect(Collectors.joining(","));
                detailDto.setThirdCode(thirdCodes);
                detailDto.setSysType(thisThirdList.get(0).getSysType());
                detailDto.setSysTypeName(ThirdSysTypeEnum.getNameByCode(detailDto.getSysType()));
                //处理同步状态
                List<String> syncStatusList = thisThirdList.stream().map(VirtualWarehousePushHandleDetailDTO.ThirdDataDTO::getSyncStatus).collect(Collectors.toList());
                if (syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.NO_NEED_SYNC.getCode())) {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.NO_NEED_SYNC.getCode());
                } else if (syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.TO_BE_SYNC.getCode())) {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.TO_BE_SYNC.getCode());
                } else if (syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode())) {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode());
                } else if (syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode())) {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
                } else if (syncStatusList.contains(VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode())) {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.MANUAL_COMPLETION_SYNC.getCode());
                } else {
                    detailDto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode());
                }
            }
            detailDto.setSyncStatusName(VirtualWarehouseAllocationSyncStatusEnum.getNameByCode(detailDto.getSyncStatus()));
        });
        viewDTO.setDetailList(detailDtos);
        return viewDTO;
    }

    /**
     * 获取附件
     *
     * @param id
     * @param viewDTO
     */
    private void getAttachment(String id, VirtualWarehouseAllocationDTO.ViewDTO viewDTO) {
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(id));
        List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachUrlList(attachmentUrlList);
        viewDTO.setAttachNameList(attachmentNameList);
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return PagingVO
     * @author hyj
     * @date: 2024-06-05
     */
    @Override
    public PagingVO<VirtualWarehouseAllocationDTO.ListDTO> paging(PagingDTO<VirtualWarehouseAllocationDTO.PagingParamDTO> dto) {
        VirtualWarehouseAllocationDTO.PagingParamDTO params = dto.getParams();
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        if(dynamicDataSourceTypeEnum == null) {
            dynamicDataSourceTypeEnum = DynamicDataSourceTypeEnum.POSTGRES;
        }
        params.setDynamicDataSource(dynamicDataSourceTypeEnum.getCode());
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseAllocationDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        List<VirtualWarehouseAllocationDTO.ListDTO> records = pageData.getRecords();
        setInfo(records);
        return new PagingVO(pageData);
    }

    /**
     * 设置明细
     *
     * @param records
     */
    private void setInfo(List<VirtualWarehouseAllocationDTO.ListDTO> records) {
        List<String> skuIds = records.stream().map(record -> record.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        if (CollectionUtils.isEmpty(skuVOList)) {
            return;
        }

        for (VirtualWarehouseAllocationDTO.ListDTO record : records) {
            SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), record.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                record.setSkuName(skuVO.getSkuName());
                record.setImageUrl(skuVO.getSkuImagesUrl());
                record.setProductName(skuVO.getSkuName());
            }
            //是否统计名称
            record.setIsStatisticsName(record.getIsStatistics() ? "是" : "否");
            record.setIsVirtualScarceStr(record.getIsVirtualScarce() ? "是" : "否");
            record.setSysTypeName(ThirdSysTypeEnum.getNameByCode(record.getSysType()));
            record.setSyncStatusName(VirtualWarehouseAllocationSyncStatusEnum.getNameByCode(record.getSyncStatus()));
        }
    }

    /**
     * 变更状态
     *
     * @param allocationEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submit(VirtualWarehouseAllocationEntity allocationEntity) {
        String existStatus = allocationEntity.getStatus();
        String code = VirtualWarehouseAllocationStatusEnum.HANDLE.getCode();
        if (Objects.equals(existStatus, code)) {
            throw new ServiceException("存在相同的状态");
        }
        //查询明细信息
        List<VirtualWarehouseAllocationDetailEntity> detailEntityList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        if (CollUtil.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.VM_ALLOCATION_NOT_FOUND);
        }
        //实体仓信息
        List<WarehouseEntity> list = warehouseService.list();
        Map<String, String> warehouseMap = CollUtil.isEmpty(list) ? new HashMap<>() :
                list.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getOrgId));

        //校验明细库存数据，并且生成借调信息
        List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList = transferAndCheckVirtualInventoryQty(allocationEntity,detailEntityList,warehouseMap);
        allocationEntity.setStatus(code);
        allocationEntity.setHandleDate(LocalDate.now());
        this.updateById(allocationEntity);
        //处理分货推送
        this.submitHandlePush(allocationEntity,detailEntityList,transferWarehouseList,warehouseMap);

        // 记录操作日志
        log.info("提交 开始记录分货单主单日志数据，id：【{}】", allocationEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】提交了单号【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), allocationEntity.getCode(), "分货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), allocationEntity.getId(), "提交操作");
        return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.SUBMIT);
    }


    /**
     * 处理分货推送
     * @author will
     * @date 2026/1/27 16:44
     * @param allocationEntity
     * @param detailEntityList
     * @param transferWarehouseList
     * @param warehouseMap
     * @return void
     */
    private void submitHandlePush (VirtualWarehouseAllocationEntity allocationEntity,List<VirtualWarehouseAllocationDetailEntity> detailEntityList,
                                   List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList,Map<String, String> warehouseMap) {
        //分货处理
        if (VirtualWarehouseAllocationTypeEnum.ALLOCATION.getCode().equals(allocationEntity.getType())) {

            //生成自动借调直接调拨单
            List<String> transferIdList = generateAutoTransferInfo(allocationEntity, transferWarehouseList);

            List<String> parentId = new ArrayList<>();
            if(!CollectionUtils.isEmpty(transferIdList)){
                List<TransferInfoEntity> transferInfoEntities = transferInfoService.listByIds(transferIdList);
                List<String> sourceCodes = transferInfoEntities.stream().map(TransferInfoEntity::getCode).collect(Collectors.toList());
                //查询直接调拨单是否生成推送
                WmsPushMsgDTO.SearchDTO searchDTO = new WmsPushMsgDTO.SearchDTO();
                searchDTO.setSourceCodeList(sourceCodes);
                searchDTO.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
                searchDTO.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
                List<WmsPushMsgEntity> wmsPushMsgEntityList = wmsPushMsgService.searchByDTO(searchDTO);
                if(!CollectionUtils.isEmpty(wmsPushMsgEntityList)){
                    parentId.addAll(transferIdList);
                }
            }
            //生成旺店通同步库存比对任务
            String taskId = syncWdtVirtualWarehousePushOrderService.saveWdtInventoryTask(allocationEntity, detailEntityList);
            if(StringUtils.isNotBlank(taskId)){
                parentId.add(taskId);
            }


            //校验总库存
            submitCheckQty(detailEntityList,allocationEntity);

            //生成平台新增分货同步单
            virtualWarehousePushHandleService.addAllocationPush(allocationEntity,parentId);

        } else if (VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode().equals(allocationEntity.getType())) {
            //调拨分货
            submitCheckQty(detailEntityList,allocationEntity);

            //生成平台取消分货同步单
            virtualWarehousePushHandleService.cancelAllocationPush(allocationEntity);

            //调拨分货生成直接调拨单
            List<String> transferIdList = generateDirectTransferInfo(allocationEntity, detailEntityList, warehouseMap);

            List<String> parentId = new ArrayList<>();
            if(!CollectionUtils.isEmpty(transferIdList)){
                List<TransferInfoEntity> transferInfoEntities = transferInfoService.listByIds(transferIdList);
                List<String> sourceCodes = transferInfoEntities.stream().map(TransferInfoEntity::getCode).collect(Collectors.toList());
                //查询直接调拨单是否生成推送
                WmsPushMsgDTO.SearchDTO searchDTO = new WmsPushMsgDTO.SearchDTO();
                searchDTO.setSourceCodeList(sourceCodes);
                searchDTO.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
                searchDTO.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
                List<WmsPushMsgEntity> wmsPushMsgEntityList = wmsPushMsgService.searchByDTO(searchDTO);
                if(!CollectionUtils.isEmpty(wmsPushMsgEntityList)){
                    parentId.addAll(transferIdList);
                }
            }
            //生成旺店通同步库存比对任务
            String taskId = syncWdtVirtualWarehousePushOrderService.saveWdtInventoryTask(allocationEntity, detailEntityList);
            if(StringUtils.isNotBlank(taskId)){
                parentId.add(taskId);
            }

            //生成平台新增分货同步单
            virtualWarehousePushHandleService.addAllocationPush(allocationEntity, parentId);
        } else {
            //取消分货
            submitCheckQty(detailEntityList,allocationEntity);

            //生成平台取消分货同步单
            virtualWarehousePushHandleService.cancelAllocationPush(allocationEntity);
        }

        // 更新明细推送状态
        virtualWarehousePushHandleService.deleteVirtualWarehousePushHandle(allocationEntity.getId());
    }

    /**
     * 提交时校验库存并扣减
     * @author will
     * @date 2026/1/21 10:11
     * @param detailEntityList
     * @param allocationEntity
     * @return void
     */
    private void submitCheckQty(List<VirtualWarehouseAllocationDetailEntity> detailEntityList, VirtualWarehouseAllocationEntity allocationEntity) {
        //校验总库存
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailEntityList);
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = getQty(detailList, allocationEntity.getType());
        checkTotalQty(detailList, allocationEntity.getType(), virtualInventoryQtyList);
        //执行扣减库存
        virtualWarehouseAllocationDetailService.submit(allocationEntity);
    }

    /**
     * 导出
     *
     * @param dto
     */
    @Override
    public void export(VirtualWarehouseAllocationDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("分货单导出", EXPORT_WMS_VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), dto);
    }

    /**
     * 导入
     *
     * @param type
     * @param excelFile
     * @param response
     * @return
     */
    @Override
    public VirtualWarehouseAllocationDTO.DetailViewDto importFile(String type, MultipartFile excelFile, HttpServletResponse response) {
        if (CharSequenceUtil.isBlank(type) || Objects.isNull(VirtualWarehouseAllocationTypeEnum.getEnum(type))) {
            throw new ServiceException(ApiError.HTTP_BAD_REQUEST);
        }
        VirtualWarehouseAllocationDTO.DetailViewDto importDTO = new VirtualWarehouseAllocationDTO.DetailViewDto();
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                getAllocationImport(excelFile, importDTO);
                break;
            case TRANSFER:
                getTransferImport(excelFile, importDTO);
                break;
            case CANCEL:
                getCancelImport(excelFile, importDTO);
                break;
            default:
                throw new ServiceException(ApiError.HTTP_BAD_REQUEST);
        }
        return importDTO;
    }

    private void getAllocationImport(MultipartFile excelFile, VirtualWarehouseAllocationDTO.DetailViewDto importDTO) {
        VirtualWarehouseAllocationExcelListener excelListenerUtil = new VirtualWarehouseAllocationExcelListener(
                virtualWarehouseRelationService, warehouseService, virtualWarehouseService, plmTaskFeign, virtualInventoryService);
        try {
            EasyExcel.read(excelFile.getInputStream(), VwAllocationAllocationExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.COMMON_IMPORT_SIZE_EXCEED_LIMIT, size);
        }
        String url = "";
        List<VwAllocationAllocationExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "分货单导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "virtualWarehouseAllocationError", errorList, VwAllocationAllocationExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
    }

    private void getTransferImport(MultipartFile excelFile, VirtualWarehouseAllocationDTO.DetailViewDto importDTO) {
        VirtualWarehouseAllocationTransferExcelListener excelListenerUtil = new VirtualWarehouseAllocationTransferExcelListener(
                virtualWarehouseRelationService, warehouseService, virtualWarehouseService, plmTaskFeign, virtualInventoryService);
        try {
            EasyExcel.read(excelFile.getInputStream(), VwAllocationAllocationTransferExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationTransferExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.COMMON_IMPORT_SIZE_EXCEED_LIMIT, size);
        }
        String url = "";
        List<VwAllocationAllocationTransferExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "分货单导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "virtualWarehouseAllocationError", errorList, VwAllocationAllocationTransferExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
    }

    private void getCancelImport(MultipartFile excelFile, VirtualWarehouseAllocationDTO.DetailViewDto importDTO) {
        VirtualWarehouseAllocationCancelExcelListener excelListenerUtil = new VirtualWarehouseAllocationCancelExcelListener(
                virtualWarehouseRelationService, warehouseService, virtualWarehouseService, plmTaskFeign, virtualInventoryService);
        try {
            EasyExcel.read(excelFile.getInputStream(), VwAllocationAllocationCancelExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.FILE_DATA_IMPORT_FAILED);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationCancelExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.COMMON_IMPORT_SIZE_EXCEED_LIMIT, size);
        }
        String url = "";
        List<VwAllocationAllocationCancelExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "分货单导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "virtualWarehouseAllocationError", errorList, VwAllocationAllocationCancelExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
    }

    @Override
    public List<VirtualWarehouseAllocationDTO.TabListDTO> tabList(PermissionsDTO dto) {
        VirtualWarehouseAllocationStatusEnum[] values = VirtualWarehouseAllocationStatusEnum.values();
        List<VirtualWarehouseAllocationDTO.TabListDTO> list = new ArrayList<>();
        for (VirtualWarehouseAllocationStatusEnum item : values) {
            VirtualWarehouseAllocationDTO.PagingParamDTO pagingParamDTO = new VirtualWarehouseAllocationDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            VirtualWarehouseAllocationDTO.TabListDTO resultDTO = new VirtualWarehouseAllocationDTO.TabListDTO();
            Integer count = MathUtil.ZERO;
            if (VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode().equals(item.getCode())) {
                pagingParamDTO.setStatus(VirtualWarehouseAllocationStatusEnum.WAIT_SUBMIT.getCode());
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (VirtualWarehouseAllocationStatusEnum.HANDLE.getCode().equals(item.getCode())) {
                pagingParamDTO.setStatus(VirtualWarehouseAllocationStatusEnum.HANDLE.getCode());
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            if (VirtualWarehouseAllocationStatusEnum.INVALID.getCode().equals(item.getCode())) {
                pagingParamDTO.setStatus(VirtualWarehouseAllocationStatusEnum.INVALID.getCode());
                count = this.baseMapper.listCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }

        //查询同步失败数量
        VirtualWarehouseAllocationDTO.PagingParamDTO pagingParamDTO = new VirtualWarehouseAllocationDTO.PagingParamDTO();
        pagingParamDTO.setPermissionSql(dto.getPermissionSql());
        pagingParamDTO.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
        Integer count = this.baseMapper.listCountBySyncStatus(pagingParamDTO);
        VirtualWarehouseAllocationDTO.TabListDTO resultDTO = new VirtualWarehouseAllocationDTO.TabListDTO();
        resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
        resultDTO.setTabFlag(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
        list.add(resultDTO);
        //查询所有的数量
        VirtualWarehouseAllocationDTO.PagingParamDTO paramDTO = new VirtualWarehouseAllocationDTO.PagingParamDTO();
        pagingParamDTO.setPermissionSql(dto.getPermissionSql());
        Integer totalCount = this.baseMapper.listCount(paramDTO);
        VirtualWarehouseAllocationDTO.TabListDTO listDTO = new VirtualWarehouseAllocationDTO.TabListDTO();
        listDTO.setTabFlag("all");
        listDTO.setCount(totalCount);
        list.add(listDTO);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO saveAndSubmit(VirtualWarehouseAllocationDTO.UpdateDTO dto) {
        String id = dto.getId();
        if (CharSequenceUtil.isBlank(id)) {
            VirtualWarehouseAllocationDTO.AddDTO addDTO = new VirtualWarehouseAllocationDTO.AddDTO();
            BeanUtils.copyProperties(dto, addDTO);
            BaseResultDTO.AddDTO add = service.add(addDTO);
            id = add.getId();
        } else {
            service.update(dto);
        }
        VirtualWarehouseAllocationEntity allocationEntity = this.getById(id);
        return service.submit(allocationEntity);
    }

    /**
     * 展示作废信息
     *
     * @param id
     * @return
     */
    @Override
    public VirtualWarehouseAllocationDTO.ManualFinishViewDTO viewInvalid(String id) {
        VirtualWarehouseAllocationEntity vmAllocation = getById(id);
        if (Objects.isNull(vmAllocation)) {
            throw new ServiceException(ApiError.VM_ALLOCATION_NOT_FOUND, id);
        }
        VirtualWarehouseAllocationDTO.ManualFinishViewDTO manualFinishViewDTO=new VirtualWarehouseAllocationDTO.ManualFinishViewDTO();
        BeanUtils.copyProperties(vmAllocation,manualFinishViewDTO);
        return manualFinishViewDTO;
    }


    /**
     * 变更状态
     *
     * @param allocationEntity
     * @param status
     * @param invalidDescription
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO invalid(VirtualWarehouseAllocationEntity allocationEntity, String status, String invalidDescription) {
        String existStatus = allocationEntity.getStatus();
        if (existStatus.equals(status)) {
            throw new ServiceException("存在相同的状态");
        }
        allocationEntity.setStatus(status);
        allocationEntity.setInvalidDescription(invalidDescription);
        this.updateById(allocationEntity);
        log.info("作废 开始记录分货单主单日志数据，id：【{}】", allocationEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】作废了【{}】单据【{}】，作废说明（{}）", UserContext.getDefaultLoginUser().getUserName(), "分货单", allocationEntity.getCode(), invalidDescription);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), allocationEntity.getId(), "作废操作");

        return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity, List<VirtualWarehouseAllocationDTO.DetailDto> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.HTTP_BAD_REQUEST);
        }
        //获取调转方向：调拨方向-1
        if (VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode().equals(virtualWarehouseAllocationEntity.getType())) {
            virtualWarehouseAllocationEntity.setDirection(VwAllocationDirectionEnum.REVERSE.getCode());
        }
        checkInfoAndQty(virtualWarehouseAllocationEntity, detailList,Boolean.FALSE);
    }

    /**
     * 校验信息
     *
     * @param virtualWarehouseAllocationEntity
     * @param detailList
     */
    private void checkInfoAndQty(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity, List<VirtualWarehouseAllocationDTO.DetailDto> detailList
                                , Boolean isChekInventoryQty) {
        List<String> skuIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> warehouseIds = detailList.stream().flatMap(obj -> Stream.of(obj.getWarehouseId(),obj.getToWarehouseId())).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> vmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> fromVmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        vmIds.addAll(fromVmIds);
        //获取所有的sku、仓库、虚拟仓信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(vmIds);
        String type = virtualWarehouseAllocationEntity.getType();
        //校验总库存数量
        //List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = getQty(detailList, type);

        //根据实体仓获取虚拟仓
        List<VirtualWarehouseRelationEntity> vwRelationList = virtualWarehouseRelationService.getByWarehouseId(warehouseIds);
        if (CollUtil.isEmpty(vwRelationList) || Objects.isNull(vwRelationList.get(0))) {
            throw new ServiceException(ApiError.WH_ENTITY_NO_VIRTUAL_RELATION);
        }
        //获取库存
        detailList.forEach(detailDto -> {
            //校验sku、仓库、虚拟仓是否存在
            checkInfo(detailDto, skuVOList, warehouseList, virtualWarehouseList, vwRelationList);
           /* if (isChekInventoryQty) {
                //校验库存
                checkQty(detailDto, type, virtualInventoryQtyList);
            }*/
        });
       /* if (isChekInventoryQty) {
            //校验所有的库存总量
            checkTotalQty(detailList, type, virtualInventoryQtyList);
        }*/

        //校验数据唯一
        checkUniqueInfo(type, detailList);
    }

    private List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferAndCheckVirtualInventoryQty (VirtualWarehouseAllocationEntity entity, List<VirtualWarehouseAllocationDetailEntity> detailEntityList,Map<String, String> warehouseMap) {
        //明细转换
        List<VirtualWarehouseAllocationDTO.DetailDto> detailList = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailEntityList);
        //校验总库存数量
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = getQty(detailList, entity.getType());

        //查询虚拟仓下的借调仓
        List<String> vmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<VirtualWarehouseEntity> virtualWarehouseList = CollUtil.isEmpty(vmIds) ? new ArrayList<>() : virtualWarehouseService.listByIds(vmIds);
        Map<String,VirtualWarehouseEntity> virtualWarehouseMap = CollUtil.isEmpty(virtualWarehouseList) ?
                new HashMap<>() :
                virtualWarehouseList.stream().collect(Collectors.toMap(VirtualWarehouseEntity::getId, e -> e));

        List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferList = new ArrayList<>();
        for (VirtualWarehouseAllocationDTO.DetailDto detailDto :detailList) {
            //校验数据唯一
            checkUniqueInfo(entity.getType(), detailList);
            //虚拟仓信息
            VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseMap.get(detailDto.getToVirtualWarehouseId());
            //校验库存、生成借调数据
            VirtualWarehouseAllocationDTO.TransferWarehouseDTO transferWarehouseDTO = generateAndCheckQty(detailDto, entity.getType(), virtualInventoryQtyList, virtualWarehouseEntity);
            if (ObjectUtil.isEmpty(transferWarehouseDTO)){
                //无需借调
                continue;
            }
            transferWarehouseDTO.setSourceDetailId(detailDto.getId());
            transferWarehouseDTO.setFromOrgId(warehouseMap.get(transferWarehouseDTO.getFromWarehouseId()));
            transferWarehouseDTO.setToOrgId(warehouseMap.get(transferWarehouseDTO.getToWarehouseId()));
            //需要借调
            transferList.add(transferWarehouseDTO);
        }
        return transferList;

    }
    /**
     * 自动生成直接调拨单信息
     * @author will
     * @date 2025/12/18 19:25
     * @param allocationEntity
     * @param transferWarehouseList
     * @return void
     */
    private List<String> generateAutoTransferInfo(VirtualWarehouseAllocationEntity allocationEntity,List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList) {
        if (CollUtil.isEmpty(transferWarehouseList)) {
            return Collections.emptyList();
        }
        //查询拣货策略
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(PickingBillTypeEnum.TRANSFER.getCode());
        executionData.setSourceCode(allocationEntity.getCode());
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> details = transferWarehouseList.stream()
                .map(v -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(v.getFromWarehouseId(), v.getSkuId(), v.getSkuNo(),v.getSkuNo(), v.getQty(), v.getSourceDetailId())).collect(Collectors.toList());
        executionData.setDetails(details);
        Pair<List<CfgRulePickingDTO.CfgRulePickingInventoryDTO>, List<WarehouseLocationEntity>> pickPair = cfgRulePickingService.matchRuleActionList(executionData, "gt");
        Pair<List<LocationInventoryResultDTO>, Map<String, Integer>> ruleOrderMatchResult = cfgRulePickingService.getSoB2CRuleOrderMatchResult(executionData, pickPair);

        List<String> resultList = new ArrayList<>();
        Map<String, List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO>> map = transferWarehouseList.stream().collect(groupingBy(obj -> obj.getFromWarehouseId() + splitStr + obj.getToWarehouseId()));
        for (Map.Entry<String, List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO>> entry : map.entrySet()) {
            List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> value = entry.getValue();
            TransferInfoDTO.AddDTO addDTO = new TransferInfoDTO.AddDTO();
            addDTO.setBillDate(LocalDate.now());
            //判断组织类型
            boolean orgEquals = CharSequenceUtil.equals(value.get(0).getFromOrgId(), value.get(0).getToOrgId());
            addDTO.setType(orgEquals ? TransferTypeEnum.IN_ORG.getCode() : TransferTypeEnum.CROSS_ORG.getCode());
            CfgSettingVirtualDTO.MatchVirtualTransferRuleDTO matchVirtualTransferRuleDTO = new  CfgSettingVirtualDTO.MatchVirtualTransferRuleDTO();
            matchVirtualTransferRuleDTO.setInWarehouseCode(value.get(0).getToWarehouseId());
            matchVirtualTransferRuleDTO.setOutWarehouseCode(value.get(0).getFromWarehouseId());
            CfgSettingVirtualDTO.MatchVirtualTransferResultDTO matchTransferRule = cfgSettingVirtualService.matchTransferRule(matchVirtualTransferRuleDTO);
            String transferDirection = TransferDirectionEnum.ORDINARY.getCode();
            if(matchTransferRule.getIsMatch() && StringUtils.isNotBlank(matchTransferRule.getTransferDirection())){
                transferDirection = matchTransferRule.getTransferDirection();
            }
            addDTO.setTransferDirection(transferDirection);
            addDTO.setInOrgId(value.get(0).getToOrgId());
            addDTO.setOutOrgId(value.get(0).getFromOrgId());
            addDTO.setSourceType(SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode());
            addDTO.setSourceId(allocationEntity.getId());
            addDTO.setSourceCode(allocationEntity.getCode());
            addDTO.setRemark("分货单自动生成调拨单");
            addDTO.setIsUserSystem(Boolean.TRUE);
            List<TransferInfoDetailDTO.AddDTO> detailDTOList = new ArrayList<>();
            for (VirtualWarehouseAllocationDTO.TransferWarehouseDTO transferWarehouseDTO :value) {
                TransferInfoDetailDTO.AddDTO detailAddDTO = new TransferInfoDetailDTO.AddDTO();
                detailAddDTO.setSkuId(transferWarehouseDTO.getSkuId());
                detailAddDTO.setQty(transferWarehouseDTO.getQty());
                detailAddDTO.setOutWarehouseId(transferWarehouseDTO.getFromWarehouseId());
                detailAddDTO.setInWarehouseId(transferWarehouseDTO.getToWarehouseId());
                detailAddDTO.setIsUserSystem(Boolean.TRUE);
                detailAddDTO.setInWarehouseLocation("");
                detailAddDTO.setOutWarehouseLocation("");
                detailAddDTO.setSourceDetailId(transferWarehouseDTO.getSourceDetailId());
                //查询拣货策略
                List<LocationInventoryResultDTO> inventoryResultDTOList = ruleOrderMatchResult.getFirst().stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), detailAddDTO.getOutWarehouseId()) && CharSequenceUtil.equals(obj.getSkuId(), detailAddDTO.getSkuId())).collect(Collectors.toList());
                if (CollUtil.isEmpty(inventoryResultDTOList)) {
                    detailDTOList.add(detailAddDTO);
                }
                for (LocationInventoryResultDTO resultDTO : inventoryResultDTOList) {
                    TransferInfoDetailDTO.AddDTO detailResultDTO = new TransferInfoDetailDTO.AddDTO();
                    BeanUtil.copyProperties(detailAddDTO,detailResultDTO);
                    detailResultDTO.setOutWarehouseLocation(resultDTO.getWarehouseLocation());
                    detailResultDTO.setInWarehouseLocation(resultDTO.getWarehouseLocation());
                    detailResultDTO.setQty(resultDTO.getQuantity());
                    detailDTOList.add(detailResultDTO);
                }
            }
            addDTO.setDetailList(detailDTOList);
            //新增并审核调拨单
            String transferId = transferInfoService.addAndApprove(addDTO);
            resultList.add(transferId);
        }
        return resultList;
    }

    /**
     * 调拨分货生成直接调拨单信息
     * @author will
     * @date 2025/12/18 19:50
     * @param allocationEntity
     * @param detailEntityList
     * @param warehouseMap
     * @return void
     */
    private List<String> generateDirectTransferInfo (VirtualWarehouseAllocationEntity allocationEntity,List<VirtualWarehouseAllocationDetailEntity> detailEntityList,Map<String, String> warehouseMap) {
        //非调拨类型无需再添加调拨单
        if (!CharSequenceUtil.equals(allocationEntity.getType(),VirtualWarehouseAllocationTypeEnum.TRANSFER.getCode())) {
            return Collections.emptyList();
        }
        List<VirtualWarehouseAllocationDTO.TransferWarehouseDTO> transferWarehouseList = new ArrayList<>();
        for (VirtualWarehouseAllocationDetailEntity detailEntity : detailEntityList) {
            //出入库仓库一致则无需调拨
            if (CharSequenceUtil.equals(detailEntity.getWarehouseId(),detailEntity.getToWarehouseId())) {
                continue;
            }
            VirtualWarehouseAllocationDTO.TransferWarehouseDTO transferWarehouseDTO = new VirtualWarehouseAllocationDTO.TransferWarehouseDTO();
            transferWarehouseDTO.setSkuId(detailEntity.getSkuId());
            transferWarehouseDTO.setSkuNo(detailEntity.getSkuNo());
            transferWarehouseDTO.setQty(detailEntity.getQty());
            transferWarehouseDTO.setFromWarehouseId(detailEntity.getWarehouseId());
            transferWarehouseDTO.setToWarehouseId(detailEntity.getToWarehouseId());
            transferWarehouseDTO.setFromOrgId(warehouseMap.get(detailEntity.getWarehouseId()));
            transferWarehouseDTO.setToOrgId(warehouseMap.get(detailEntity.getToWarehouseId()));
            transferWarehouseList.add(transferWarehouseDTO);
        }
        if (CollUtil.isEmpty(transferWarehouseList)) {
            return Collections.emptyList();
        }
        //生成调拨单
       return generateAutoTransferInfo(allocationEntity,transferWarehouseList);
    }

    /**
     * 校验所有的库存总量
     *
     * @param detailList
     * @param type
     * @param virtualInventoryQtyList
     */
    private static void checkTotalQty(List<VirtualWarehouseAllocationDTO.DetailDto> detailList, String type, List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList) {
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                //获取根据sku和实体仓获取需要一共要分配的数量
                Map<String, List<VirtualWarehouseAllocationDTO.DetailDto>> skuWarehouseGroupMap = detailList.stream().collect(groupingBy(detail -> detail.getSkuId() + splitStr + detail.getWarehouseId()));
                skuWarehouseGroupMap.forEach((key, list) -> {
                    String[] split = key.split(splitStr);
                    //获取实体仓
                    VirtualInventoryDTO.ViewQtyDTO viewQtyDTO = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), split[0]) && Objects.equals(item.getWarehouseId(), split[1])).findFirst().orElse(null);
                    if (Objects.nonNull(viewQtyDTO)) {
                        Integer warehouseAllocationQty = viewQtyDTO.getWarehouseAllocationQty();
                        Integer reduce = list.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getQty).reduce(0, Integer::sum);
                        if (warehouseAllocationQty < reduce) {
                            throw new ServiceException(ApiError.WH_ENTITY_ALLOCATION_STOCK_INSUFFICIENT, list.get(0).getSkuNo(), list.get(0).getWarehouseName(), warehouseAllocationQty);
                        }
                    } else {
                        throw new ServiceException(ApiError.WH_ENTITY_ALLOCATION_STOCK_INSUFFICIENT, list.get(0).getSkuNo(), list.get(0).getWarehouseName(), 0);
                    }
                });
                break;
            default:
                //获取根据sku和实体仓获取需要一共要分配的数量
                Map<String, List<VirtualWarehouseAllocationDTO.DetailDto>> skuVwGroupMap = detailList.stream().collect(groupingBy(detail ->
                        detail.getSkuId() + splitStr + detail.getWarehouseId() + splitStr + detail.getFromVirtualWarehouseId()));
                skuVwGroupMap.forEach((key, list) -> {
                    String[] split = key.split(splitStr);
                    VirtualInventoryDTO.ViewQtyDTO fromVmQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), split[0])
                            && Objects.equals(item.getWarehouseId(), split[1]) && Objects.equals(item.getFromVirtualWarehouseId(), split[2])).findFirst().orElse(null);
                    if (Objects.nonNull(fromVmQty)) {
                        Integer vwUsableQty = fromVmQty.getFromVirtualWarehouseUsableQty();
                        Integer reduce = list.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getQty).reduce(0, Integer::sum);
                        if (vwUsableQty < reduce) {
                            throw new ServiceException(ApiError.VM_SOURCE_INVENTORY_INSUFFICIENT, list.get(0).getSkuNo(), list.get(0).getFromVirtualWarehouseName(), fromVmQty.getFromVirtualWarehouseUsableQty());
                        }
                    } else {
                        throw new ServiceException(ApiError.VM_INVENTORY_INSUFFICIENT_FOR_TRANSFER, list.get(0).getSkuNo(), list.get(0).getFromVirtualWarehouseName(), 0);
                    }
                });
                break;
        }
    }

    /**
     * 校验数据唯一
     *
     * @param type
     * @param detailList
     */
    private static void checkUniqueInfo(String type, List<VirtualWarehouseAllocationDTO.DetailDto> detailList) {
        Map<String, Integer> keyMap = new HashMap<>();

        detailList.forEach(detail -> {
            String key = detail.getSkuNo() + splitStr + detail.getWarehouseName() + splitStr
                    + detail.getFromVirtualWarehouseName() + splitStr + detail.getToVirtualWarehouseName();
            Integer value = keyMap.get(key);
            if (Objects.isNull(value)) {
                value = 1;
            } else {
                value += 1;
            }
            keyMap.put(key, value);
        });
        StringBuilder msg = new StringBuilder();
        keyMap.forEach((k, v) -> {
            if (v > 1) {
                String[] split = k.split(splitStr);
                if (v > 1) {
                    switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
                        case ALLOCATION:
                            msg.append(CharSequenceUtil.format(ApiError.VM_ALLOCATION_UNIQUE_ERROR.getMsg(), split[0], split[1], split[3]));
                            break;
                        case TRANSFER:
                            msg.append(CharSequenceUtil.format(ApiError.VM_ALLOCATION_TRANSFER_UNIQUE_ERROR.getMsg(), split[0], split[1], split[2], split[3]));
                            break;
                        case CANCEL:
                            msg.append(CharSequenceUtil.format(ApiError.VM_ALLOCATION_CANCEL_UNIQUE_ERROR.getMsg(), split[0], split[1], split[2]));
                            break;
                        default:
                            throw new ServiceException(ApiError.VM_ALLOCATION_UNIQUE_ERROR);
                    }
                }
            }
        });
        if (CharSequenceUtil.isNotBlank(msg.toString())) {
            throw new ServiceException(msg.toString());
        }

    }

    private List<VirtualInventoryDTO.ViewQtyDTO> getQty(List<VirtualWarehouseAllocationDTO.DetailDto> detailList, String type) {
        VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO = new VirtualInventoryDTO.QtyTypeDTO();
        qtyTypeDTO.setType(type);
        List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS = BeanMapperUtils.copyList(VirtualInventoryDTO.QtySearchDTO.class, detailList);
        qtyTypeDTO.setQtySearchList(qtySearchDTOS);
        return virtualInventoryService.getQty(qtyTypeDTO);
    }

    private void checkInfo(VirtualWarehouseAllocationDTO.DetailDto detailDto, List<SkuVO> skuVOList, List<WarehouseDTO.UpdateDTO> warehouseList,
                           List<VirtualWarehouseEntity> virtualWarehouseList, List<VirtualWarehouseRelationEntity> vwRelationList) {
        SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())).findFirst().orElse(null);
        if (Objects.isNull(skuVO)) {
            throw new ServiceException(ApiError.PRODUCT_SKU_PARAM_NOT_FOUND, detailDto.getSkuId());
        }
        detailDto.setSkuNo(skuVO.getSkuNo());
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getWarehouseId())).findFirst().orElse(null);
        if (Objects.isNull(updateDTO)) {
            throw new ServiceException(ApiError.WH_ENTITY_NOT_FOUND, detailDto.getWarehouseId());
        } else {
            if (Boolean.TRUE.equals(updateDTO.getDisabled())) {
                throw new ServiceException(ApiError.WH_ENTITY_NOT_ACTIVE, detailDto.getWarehouseId());
            }
        }


        detailDto.setWarehouseName(updateDTO.getName());
        if (CharSequenceUtil.isBlank(detailDto.getToVirtualWarehouseId()) && CharSequenceUtil.isBlank(detailDto.getFromVirtualWarehouseId())) {
            throw new ServiceException(ApiError.VM_FROM_TO_BOTH_EMPTY);
        }

        if (CharSequenceUtil.isNotBlank(detailDto.getToVirtualWarehouseId()) && CharSequenceUtil.isNotBlank(detailDto.getFromVirtualWarehouseId()) &&
                Objects.equals(detailDto.getToVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())) {
            throw new ServiceException(ApiError.VM_FROM_TO_SAME);
        }
        if (CharSequenceUtil.isNotBlank(detailDto.getToVirtualWarehouseId())) {
            VirtualWarehouseEntity toVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getToVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(toVmWarehouse)) {
                throw new ServiceException(ApiError.VM_TARGET_NOT_FOUND, detailDto.getToVirtualWarehouseId());
            } else {
                if (Boolean.TRUE.equals(toVmWarehouse.getDisabled())) {
                    throw new ServiceException(ApiError.VM_TARGET_NOT_ACTIVE, toVmWarehouse.getName());
                }
            }

            if (CharSequenceUtil.isNotBlank(detailDto.getToVirtualWarehouseId())) {
                VirtualWarehouseRelationEntity toVmRelation = vwRelationList.stream().filter(item ->
                        Objects.equals(item.getVirtualWarehouseId(), detailDto.getToVirtualWarehouseId()) && Objects.equals(item.getWarehouseId(), StrUtil.blankToDefault(detailDto.getToWarehouseId(),detailDto.getWarehouseId()))).findFirst().orElse(null);
                if (Objects.isNull(toVmRelation)) {
                    throw new ServiceException(ApiError.VM_RELATION_ERROR, updateDTO.getName(), toVmWarehouse.getName());
                }
                detailDto.setToVirtualWarehouseName(toVmWarehouse.getName());
                detailDto.setToVirtualWarehouseCode(toVmWarehouse.getCode());
            }
            if (CharSequenceUtil.isNotBlank(detailDto.getFromVirtualWarehouseId())) {
                VirtualWarehouseRelationEntity fromVmRelation = vwRelationList.stream().filter(item ->
                        Objects.equals(item.getVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId()) && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(fromVmRelation)) {
                    throw new ServiceException(ApiError.VM_RELATION_ERROR, updateDTO.getName(), toVmWarehouse.getName());
                }
                detailDto.setFromVirtualWarehouseName(toVmWarehouse.getName());
                detailDto.setFromVirtualWarehouseCode(toVmWarehouse.getCode());
            }

        }
        if (CharSequenceUtil.isNotBlank(detailDto.getFromVirtualWarehouseId())) {
            VirtualWarehouseEntity fromVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(fromVmWarehouse)) {
                throw new ServiceException(ApiError.VM_SOURCE_NOT_FOUND, detailDto.getFromVirtualWarehouseId());
            } else {
                if (Boolean.TRUE.equals(fromVmWarehouse.getDisabled())) {
                    throw new ServiceException(ApiError.VM_SOURCE_NOT_ACTIVE, detailDto.getFromVirtualWarehouseId());
                }
            }
            VirtualWarehouseRelationEntity fromVmRelation = vwRelationList.stream().filter(item ->
                    Objects.equals(item.getVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(fromVmRelation)) {
                throw new ServiceException(ApiError.VM_RELATION_ERROR, updateDTO.getName(), fromVmWarehouse.getName());
            }
            detailDto.setFromVirtualWarehouseName(fromVmWarehouse.getName());
            detailDto.setFromVirtualWarehouseCode(fromVmWarehouse.getCode());
        }
    }

    /**
     * 校验库存数量，并且生成借调对象
     * @author will
     * @date 2025/12/18 18:16
     * @param detailDto
     * @param type
     * @param virtualInventoryQtyList
     * @param virtualWarehouseEntity
     * @return TransferWarehouseDTO
     */
    private VirtualWarehouseAllocationDTO.TransferWarehouseDTO generateAndCheckQty(VirtualWarehouseAllocationDTO.DetailDto detailDto, String type, List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList, VirtualWarehouseEntity virtualWarehouseEntity) {
        //是否启用自动调拨
        Boolean isAutoTransferEnabled = ObjUtil.isEmpty(virtualWarehouseEntity) ? Boolean.FALSE : virtualWarehouseEntity.getIsAutoTransferEnabled();
        Integer qty = detailDto.getQty();
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                //获取实体仓可用库存
                VirtualInventoryDTO.ViewQtyDTO warehouseQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())
                        && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId())).findFirst().orElse(new VirtualInventoryDTO.ViewQtyDTO());
                if (warehouseQty.getWarehouseAllocationQty() < qty) {
                    if (!isAutoTransferEnabled) {
                        throw new ServiceException(ApiError.WH_ENTITY_INVENTORY_INSUFFICIENT, detailDto.getSkuNo(), detailDto.getWarehouseName(), warehouseQty.getWarehouseAllocationQty());
                    }
                    //生成借调对象
                    return new VirtualWarehouseAllocationDTO.TransferWarehouseDTO(virtualWarehouseEntity.getFromWarehouseId(),detailDto.getWarehouseId(),detailDto.getSkuId(),detailDto.getSkuNo(),qty - warehouseQty.getWarehouseAllocationQty());
                }
                break;
            case TRANSFER:
                VirtualInventoryDTO.ViewQtyDTO fromVmQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())
                        && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId()) && Objects.equals(item.getFromVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(new VirtualInventoryDTO.ViewQtyDTO());
                if (fromVmQty.getFromVirtualWarehouseUsableQty() < qty) {
                        throw new ServiceException(ApiError.VM_SOURCE_INVENTORY_INSUFFICIENT, detailDto.getSkuNo(), detailDto.getFromVirtualWarehouseName(), fromVmQty.getFromVirtualWarehouseUsableQty());
                }
                break;
            default:
                VirtualInventoryDTO.ViewQtyDTO cancelVmQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())
                        && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId()) && Objects.equals(item.getFromVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(new VirtualInventoryDTO.ViewQtyDTO());
                if (cancelVmQty.getFromVirtualWarehouseUsableQty() < qty) {
                        throw new ServiceException(ApiError.VM_SOURCE_INVENTORY_INSUFFICIENT, detailDto.getSkuNo(), detailDto.getFromVirtualWarehouseName(), cancelVmQty.getFromVirtualWarehouseUsableQty());
                }
                break;
        }
        return null;
    }

    @Override
    public Boolean updateRemark(VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO) {
        return lambdaUpdate()
                .set(VirtualWarehouseAllocationEntity::getRemark, updateRemarkDTO.getRemark())
                .eq(VirtualWarehouseAllocationEntity::getId, updateRemarkDTO.getId())
                .update();
    }

    @Override
    public List<VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO> listVirtualInventory(List<VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO> list) {
        List<VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO> resultList = new FastArrayList();
        //仓库Id集合
        List<String> warehouseIdList = list.stream().map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //skuId集合
        List<String> skuIdList = list.stream().map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> virtualWarehouseIdList = new ArrayList<>();
        //虚拟仓Id集合
        List<String> toVirtualWarehouseIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getToVirtualWarehouseId()))
                .map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getToVirtualWarehouseId).distinct().collect(Collectors.toList());
        virtualWarehouseIdList.addAll(toVirtualWarehouseIdList);
        List<String> fromVirtualWarehouseIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getFromVirtualWarehouseId()))
                .map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getFromVirtualWarehouseId).distinct().collect(Collectors.toList());
        virtualWarehouseIdList.addAll(fromVirtualWarehouseIdList);

        //实际可用库存
        InventoryDTO.RedisInventoryParamDTO redisParamDTO = new InventoryDTO.RedisInventoryParamDTO();
        redisParamDTO.setWarehouseIdList(warehouseIdList);
        redisParamDTO.setSkuIdList(skuIdList);
        redisParamDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryDTO.RedisInventoryReturnDTO> redisInventoryList = inventoryService.getRedisInventory(redisParamDTO);

        //实体仓可分配库存
        List<VirtualInventoryDTO.WarehouseInventoryQtyDTO> warehouseInventoryQtyList = virtualInventoryService.listInventoryQtyByWarehouseId(warehouseIdList, skuIdList);

        //虚拟仓可用库存
        VirtualInventoryDTO.RedisVirtualInventoryParamDTO dto = new VirtualInventoryDTO.RedisVirtualInventoryParamDTO();
        dto.setWarehouseIdList(warehouseIdList);
        dto.setSkuIdList(skuIdList);
        dto.setVirtualWarehouseIdList(virtualWarehouseIdList);
        dto.setDictInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
        List<VirtualInventoryDTO.RedisVirtualInventoryReturnDTO> redisVirtualInventoryList = CollectionUtils.isEmpty(virtualWarehouseIdList) ?
                new ArrayList<>() : virtualInventoryService.getRedisVirtualInventory(dto);

        for (VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO paramDTO : list) {
            VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO resultDTO = new VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO();
            BeanMapperUtils.copy(paramDTO,resultDTO);
            //实体仓可用库存
            Integer warehouseUsableQty = redisInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getInventoryStatus(),InventoryStatusEnum.USABLE.getCode())
                    )
                    .map(InventoryDTO.RedisInventoryReturnDTO::getQty).reduce(MathUtil.ZERO,Integer::sum);
            resultDTO.setWarehouseUsableQty(warehouseUsableQty);
            //实体仓已分配库存
            Integer distributionQty = warehouseInventoryQtyList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId()))
                    .map(VirtualInventoryDTO.WarehouseInventoryQtyDTO::getQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setDistributionQty(ObjectUtil.isEmpty(distributionQty) ? MathUtil.ZERO : distributionQty);

            //实体仓实际库存
            Integer warehouseRealQty = redisInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()))
                    .map(InventoryDTO.RedisInventoryReturnDTO::getQty).reduce(MathUtil.ZERO,Integer::sum);
            resultDTO.setUnDistributionQty(warehouseRealQty - resultDTO.getDistributionQty());

            Integer toVirtualWarehouseUsableQty = redisVirtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()) && CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId()) && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getToVirtualWarehouseId()))
                    .map(VirtualInventoryDTO.RedisVirtualInventoryReturnDTO::getQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setToVirtualWarehouseUsableQty(toVirtualWarehouseUsableQty);
            Integer fromVirtualWarehouseUsableQty = redisVirtualInventoryList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()) && CharSequenceUtil.equals(obj.getSkuId(), paramDTO.getSkuId()) && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getFromVirtualWarehouseId()))
                    .map(VirtualInventoryDTO.RedisVirtualInventoryReturnDTO::getQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setFromVirtualWarehouseUsableQty(fromVirtualWarehouseUsableQty);
            resultList.add(resultDTO);
        }
        return resultList;
    }

    @Override
    public void exportStatistics(VirtualWarehouseAllocationDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("分货统计导出", EXPORT_WMS_VIRTUAL_STATISTICS.getCode(), dto);
    }

    @Override
    public PagingVO<VirtualWarehouseAllocationDTO.ExportStatisticsDTO> exportVirtualStatistics(PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<VirtualWarehouseAllocationDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseAllocationDTO.ExportStatisticsDTO> pageData = this.baseMapper.exportVirtualStatistics(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            throw new ServiceException("未找到分货统计数据导出");
        }
        return new PagingVO(pageData);
    }

    @Override
    public void updateIsStatistics(VirtualWarehouseAllocationDTO.UpdateIsStatisticsDTO dto) {
        VirtualWarehouseAllocationEntity old = Optional.ofNullable(super.getById(dto.getId())).orElseThrow(() ->
                new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "分货单"));
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = BeanMapperUtils.map(VirtualWarehouseAllocationEntity.class, dto);
        //字段值一致无需修改
        if (dto.getIsStatistics().equals(old.getIsStatistics())) {
            return;
        }

        log.info("编辑 开始修改分货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("分货单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录分货单日志数据，单号：【{}】", old.getCode());
        String msg = CharSequenceUtil.format("是否统计由【{}】变更为【{}】", old.getIsStatistics(),dto.getIsStatistics());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), old.getId(), "编辑操作");
    }

    @Override
    public List<VirtualWarehouseAllocationEntity> rebuildVirtualWarehouseAllocationFlow() {
        return lambdaQuery().eq(VirtualWarehouseAllocationEntity::getStatus,VirtualWarehouseAllocationStatusEnum.HANDLE.getCode())
                .orderByAsc(VirtualWarehouseAllocationEntity::getHandleDate)
                .list();
    }

}
