package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
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
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationCancelExcelDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationExcelDTO;
import com.erp.model.wms.dto.excel.VwAllocationAllocationTransferExcelDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.model.wms.enums.VwAllocationDirectionEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.listener.VirtualWarehouseAllocationCancelExcelListener;
import com.erp.server.wms.listener.VirtualWarehouseAllocationExcelListener;
import com.erp.server.wms.listener.VirtualWarehouseAllocationTransferExcelListener;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    private InventoryService inventoryService;


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
    @GlobalTransactional(rollbackFor = Exception.class)
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "分货单", virtualWarehouseAllocationEntity.getCode());
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
                new ServiceException(ApiError.NOT_EXIST_BILL, "分货单"));
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
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "分货单");
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
            throw new ServiceException(ApiError.ERROR_VMALLOCATION_NOTFOUND, id);
        }
        VirtualWarehouseAllocationDTO.ViewDTO viewDTO = new VirtualWarehouseAllocationDTO.ViewDTO();
        BeanUtils.copyProperties(vmAllocation, viewDTO);
        //获取附件
        getAttachment(id, viewDTO);

        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, id).orderByAsc(VirtualWarehouseAllocationDetailEntity::getId));

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
        dto.getParams().setPermissionSql(dto.getPermissionSql());
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
        if (CollectionUtils.isNotEmpty(skuVOList)) {
            records.forEach(record -> {
                SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), record.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(skuVO)) {
                    record.setSkuName(skuVO.getSkuName());
                    record.setImageUrl(skuVO.getSkuImagesUrl());
                    record.setProductName(skuVO.getSkuName());
                }
                record.setSyncStatusName(VirtualWarehouseAllocationSyncStatusEnum.getNameByCode(record.getSyncStatus()));
            });
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(VirtualWarehouseAllocationEntity allocationEntity) {
        String existStatus = allocationEntity.getStatus();
        String code = VirtualWarehouseAllocationStatusEnum.HANDLE.getCode();
        if (Objects.equals(existStatus, code)) {
            throw new ServiceException("存在相同的状态");
        }
        //校验明细数据
        checkDetail(allocationEntity);
        allocationEntity.setStatus(code);
        this.updateById(allocationEntity);
        //变更明细同步状态
        virtualWarehouseAllocationDetailService.updateByMainId(allocationEntity.getId(), VirtualWarehouseAllocationSyncStatusEnum.IN_SYNC.getCode());
        //执行扣减库存
        virtualWarehouseAllocationDetailService.submit(allocationEntity);

        // 记录操作日志
        log.info("提交 开始记录分货单主单日志数据，id：【{}】", allocationEntity.getId());
        String msg = StrUtil.format("用户【{}】提交了单号【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), allocationEntity.getCode(), "分货单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), allocationEntity.getId(), "提交操作");
        //进行合单并创建中台任务数据进行同步
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                virtualWarehouseAllocationHandleService.handleData(allocationEntity);
//                CompletableFuture.runAsync(() -> virtualWarehousePushHandleService.handleData(allocationEntity));
//            }
//        });
        virtualWarehousePushHandleService.handleData(allocationEntity);
        return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.SUBMIT);
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
        if (StringUtils.isBlank(type) || Objects.isNull(VirtualWarehouseAllocationTypeEnum.getEnum(type))) {
            throw new ServiceException(ApiError.ERROR_99999);
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
                throw new ServiceException(ApiError.ERROR_99999);
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
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.ERROR_IMPORT_SIZE_ERROR, size);
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
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationTransferExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.ERROR_IMPORT_SIZE_ERROR, size);
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
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<VwAllocationAllocationCancelExcelDTO> allList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(allList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
        if (successList.size() > size) {
            throw new ServiceException(ApiError.ERROR_IMPORT_SIZE_ERROR, size);
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
    public BatchResultDTO saveAndSubmit(VirtualWarehouseAllocationDTO.UpdateDTO dto) {
        String id = dto.getId();
        if (StringUtils.isBlank(id)) {
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
            throw new ServiceException(ApiError.ERROR_VMALLOCATION_NOTFOUND, id);
        }
        VirtualWarehouseAllocationDTO.ManualFinishViewDTO manualFinishViewDTO=new VirtualWarehouseAllocationDTO.ManualFinishViewDTO();
        BeanUtils.copyProperties(vmAllocation,manualFinishViewDTO);
        return manualFinishViewDTO;
    }


    private void checkDetail(VirtualWarehouseAllocationEntity allocationEntity) {
        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailList);
        checkInfoAndQty(allocationEntity, detailDtos,Boolean.TRUE);
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO invalid(VirtualWarehouseAllocationEntity allocationEntity, String status, String invalidDescription) {
        String existStatus = allocationEntity.getStatus();
        if (existStatus.equals(status)) {
            throw new ServiceException("存在相同的状态");
        }
        allocationEntity.setStatus(status);
        allocationEntity.setInvalidDescription(invalidDescription);
        this.updateById(allocationEntity);
        log.info("作废 开始记录分货单主单日志数据，id：【{}】", allocationEntity.getId());
        String msg = StrUtil.format("用户【{}】作废了【{}】单据【{}】，作废说明（{}）", UserContext.getDefaultLoginUser().getUserName(), "分货单", allocationEntity.getCode(), invalidDescription);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), allocationEntity.getId(), "作废操作");

        return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.INVALID);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity, List<VirtualWarehouseAllocationDTO.DetailDto> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_600);
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
        List<String> warehouseIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> vmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> fromVmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        vmIds.addAll(fromVmIds);
        //获取所有的sku、仓库、虚拟仓信息
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(vmIds);
        String type = virtualWarehouseAllocationEntity.getType();
        //校验总库存数量
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = getQty(detailList, type);

        //根据实体仓获取虚拟仓
        List<VirtualWarehouseRelationEntity> vwRelationList = virtualWarehouseRelationService.getByWarehouseId(warehouseIds);
        if (CollUtil.isEmpty(vwRelationList) || Objects.isNull(vwRelationList.get(0))) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NORELATION_ERROR);
        }
        //获取库存
        detailList.forEach(detailDto -> {
            //校验sku、仓库、虚拟仓是否存在
            checkInfo(detailDto, skuVOList, warehouseList, virtualWarehouseList, vwRelationList);
            if (isChekInventoryQty) {
                //校验库存
                checkQty(detailDto, type, virtualInventoryQtyList);
            }
        });
        if (isChekInventoryQty) {
            //校验所有的库存总量
            checkTotalQty(detailList, type, virtualInventoryQtyList);
        }

        //校验数据唯一
        checkUniqueInfo(type, detailList);
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
                            throw new ServiceException(ApiError.ERROR_WAREHOUSE_INVENTORY_ALLOCATION_ERROR, list.get(0).getSkuNo(), list.get(0).getWarehouseName(), warehouseAllocationQty);
                        }
                    } else {
                        throw new ServiceException(ApiError.ERROR_WAREHOUSE_INVENTORY_ALLOCATION_ERROR, list.get(0).getSkuNo(), list.get(0).getWarehouseName(), 0);
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
                            throw new ServiceException(ApiError.ERROR_FROMVM_INVENTORY_INSUFFICIENT, list.get(0).getSkuNo(), list.get(0).getFromVirtualWarehouseName(), fromVmQty.getFromVirtualWarehouseUsableQty());
                        }
                    } else {
                        throw new ServiceException(ApiError.ERROR_VW_INVENTORY_ERROR, list.get(0).getSkuNo(), list.get(0).getFromVirtualWarehouseName(), 0);
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
                            msg.append(StrUtil.format(ApiError.ERROR_ALLOCATION_UNIQUE_ERROR.msg, split[0], split[1], split[3]));
                            break;
                        case TRANSFER:
                            msg.append(StrUtil.format(ApiError.ERROR_ALLOCATION_TRANSFER_UNIQUE_ERROR.msg, split[0], split[1], split[2], split[3]));
                            break;
                        case CANCEL:
                            msg.append(StrUtil.format(ApiError.ERROR_ALLOCATION_CANCEL_UNIQUE_ERROR.msg, split[0], split[1], split[2]));
                            break;
                        default:
                            throw new ServiceException(ApiError.ERROR_ALLOCATION_UNIQUE_ERROR);
                    }
                }
            }
        });
        if (StringUtils.isNotBlank(msg.toString())) {
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
            throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND, detailDto.getSkuId());
        }
        detailDto.setSkuNo(skuVO.getSkuNo());
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getWarehouseId())).findFirst().orElse(null);
        if (Objects.isNull(updateDTO)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOTFOUND, detailDto.getWarehouseId());
        } else {
            if (Boolean.TRUE.equals(updateDTO.getDisabled())) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOTACTIVE, detailDto.getWarehouseId());
            }
        }


        detailDto.setWarehouseName(updateDTO.getName());
        if (StringUtils.isBlank(detailDto.getToVirtualWarehouseId()) && StringUtils.isBlank(detailDto.getFromVirtualWarehouseId())) {
            throw new ServiceException(ApiError.ERROR_FROM_TO_VM_BOTHEMPTY);
        }

        if (StringUtils.isNotBlank(detailDto.getToVirtualWarehouseId()) && StringUtils.isNotBlank(detailDto.getFromVirtualWarehouseId()) &&
                Objects.equals(detailDto.getToVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())) {
            throw new ServiceException(ApiError.ERROR_FROM_TO_VM_SAME);
        }
        if (StringUtils.isNotBlank(detailDto.getToVirtualWarehouseId())) {
            VirtualWarehouseEntity toVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getToVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(toVmWarehouse)) {
                throw new ServiceException(ApiError.ERROR_TOVM_NOTFOUND, detailDto.getToVirtualWarehouseId());
            } else {
                if (Boolean.TRUE.equals(toVmWarehouse.getDisabled())) {
                    throw new ServiceException(ApiError.ERROR_TOVM_NOTACTIVE, toVmWarehouse.getName());
                }
            }

            if (StringUtils.isNotBlank(detailDto.getToVirtualWarehouseId())) {
                VirtualWarehouseRelationEntity toVmRelation = vwRelationList.stream().filter(item ->
                        Objects.equals(item.getVirtualWarehouseId(), detailDto.getToVirtualWarehouseId()) && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(toVmRelation)) {
                    throw new ServiceException(ApiError.ERROR_VW_RELATION_ERROR, updateDTO.getName(), toVmWarehouse.getName());
                }
                detailDto.setToVirtualWarehouseName(toVmWarehouse.getName());
                detailDto.setToVirtualWarehouseCode(toVmWarehouse.getCode());
            }
            if (StringUtils.isNotBlank(detailDto.getFromVirtualWarehouseId())) {
                VirtualWarehouseRelationEntity fromVmRelation = vwRelationList.stream().filter(item ->
                        Objects.equals(item.getVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId()) && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(fromVmRelation)) {
                    throw new ServiceException(ApiError.ERROR_VW_RELATION_ERROR, updateDTO.getName(), toVmWarehouse.getName());
                }
                detailDto.setFromVirtualWarehouseName(toVmWarehouse.getName());
                detailDto.setFromVirtualWarehouseCode(toVmWarehouse.getCode());
            }

        }
        if (StringUtils.isNotBlank(detailDto.getFromVirtualWarehouseId())) {
            VirtualWarehouseEntity fromVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(fromVmWarehouse)) {
                throw new ServiceException(ApiError.ERROR_FROMVM_NOTFOUND, detailDto.getFromVirtualWarehouseId());
            } else {
                if (Boolean.TRUE.equals(fromVmWarehouse.getDisabled())) {
                    throw new ServiceException(ApiError.ERROR_FROMVM_NOTACTIVE, detailDto.getFromVirtualWarehouseId());
                }
            }
            VirtualWarehouseRelationEntity fromVmRelation = vwRelationList.stream().filter(item ->
                    Objects.equals(item.getVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(fromVmRelation)) {
                throw new ServiceException(ApiError.ERROR_VW_RELATION_ERROR, updateDTO.getName(), fromVmWarehouse.getName());
            }
            detailDto.setFromVirtualWarehouseName(fromVmWarehouse.getName());
            detailDto.setFromVirtualWarehouseCode(fromVmWarehouse.getCode());
        }
    }

    private static void checkQty(VirtualWarehouseAllocationDTO.DetailDto detailDto, String type, List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList) {
        Integer qty = detailDto.getQty();
        switch (VirtualWarehouseAllocationTypeEnum.getEnum(type)) {
            case ALLOCATION:
                //获取实体仓可用库存
                VirtualInventoryDTO.ViewQtyDTO warehouseQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())
                        && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(warehouseQty) || (Objects.nonNull(warehouseQty) && warehouseQty.getWarehouseAllocationQty() < qty)) {
                    throw new ServiceException(ApiError.ERROR_INVENTORY_INSUFFICIENT, detailDto.getSkuNo(), detailDto.getWarehouseName(), warehouseQty.getWarehouseAllocationQty());
                }
                break;
            default:
                VirtualInventoryDTO.ViewQtyDTO fromVmQty = virtualInventoryQtyList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())
                        && Objects.equals(item.getWarehouseId(), detailDto.getWarehouseId()) && Objects.equals(item.getFromVirtualWarehouseId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
                if (Objects.isNull(fromVmQty) || (Objects.nonNull(fromVmQty) && fromVmQty.getFromVirtualWarehouseUsableQty() < qty)) {
                    throw new ServiceException(ApiError.ERROR_FROMVM_INVENTORY_INSUFFICIENT, detailDto.getSkuNo(), detailDto.getFromVirtualWarehouseName(), fromVmQty.getFromVirtualWarehouseUsableQty());
                }
                break;
        }
    }

    @Override
    public Boolean updateRemark(VirtualWarehouseAllocationDTO.UpdateRemarkDTO updateRemarkDTO) {
        return lambdaUpdate()
                .set(VirtualWarehouseAllocationEntity::getRemark, updateRemarkDTO.getRemark())
                .eq(VirtualWarehouseAllocationEntity::getId, updateRemarkDTO.getId())
                .update();
    }

    @Override
    public PagingVO<VirtualWarehouseAllocationDTO.ListDTO> exportVirtualWarehouseAllocation(PagingDTO<VirtualWarehouseAllocationDTO.ExportDTO> dto) {
        Page<VirtualWarehouseAllocationDTO.ListDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            //填充数据
            setInfo(page.getRecords());
        }
        return new PagingVO<>(page);
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
        List<String> toVirtualWarehouseIdList = list.stream().filter(obj -> StrUtil.isNotBlank(obj.getToVirtualWarehouseId()))
                .map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getToVirtualWarehouseId).distinct().collect(Collectors.toList());
        virtualWarehouseIdList.addAll(toVirtualWarehouseIdList);
        List<String> fromVirtualWarehouseIdList = list.stream().filter(obj -> StrUtil.isNotBlank(obj.getFromVirtualWarehouseId()))
                .map(VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO::getFromVirtualWarehouseId).distinct().collect(Collectors.toList());
        virtualWarehouseIdList.addAll(fromVirtualWarehouseIdList);

        //实际可用库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO dto = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        dto.setWarehouseIdList(warehouseIdList);
        dto.setSkuIdList(skuIdList);
        dto.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryTotalList = inventoryService.listSkuInventory(dto);

        //实体仓可分配库存
        List<VirtualInventoryDTO.WarehouseInventoryQtyDTO> warehouseInventoryQtyList = virtualInventoryService.listInventoryQtyByWarehouseId(warehouseIdList, skuIdList);

        //虚拟仓可用库存
        VirtualInventoryDTO.VirtualInventoryParamDTO inventoryParamDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        inventoryParamDTO.setWarehouseIdList(warehouseIdList);
        inventoryParamDTO.setSkuIdList(skuIdList);
        inventoryParamDTO.setVirtualWarehouseIdList(virtualWarehouseIdList);
        inventoryParamDTO.setDictInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyList = CollectionUtils.isEmpty(virtualWarehouseIdList) ?
                new ArrayList<>() : virtualInventoryService.listInventoryQty(inventoryParamDTO);

        for (VirtualWarehouseAllocationDTO.VirtualInventoryQtyParamDTO paramDTO : list) {
            VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO resultDTO = new VirtualWarehouseAllocationDTO.VirtualInventoryQtyDTO();
            BeanMapperUtils.copy(paramDTO,resultDTO);
            //实体仓可用库存
            Integer warehouseUsableQty = skuInventoryTotalList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && StrUtil.equals(obj.getInventoryStatus(),InventoryStatusEnum.USABLE.getCode())
                    )
                    .map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO,Integer::sum);
            resultDTO.setWarehouseUsableQty(warehouseUsableQty);
            //实体仓已分配库存
            Integer distributionQty = warehouseInventoryQtyList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId())
                            && StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId()))
                    .map(VirtualInventoryDTO.WarehouseInventoryQtyDTO::getQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setDistributionQty(ObjectUtil.isEmpty(distributionQty) ? MathUtil.ZERO : distributionQty);

            //实体仓实际库存
            Integer warehouseRealQty = skuInventoryTotalList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId())
                            && StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()))
                    .map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal).reduce(MathUtil.ZERO,Integer::sum);
            resultDTO.setUnDistributionQty(warehouseRealQty - resultDTO.getDistributionQty());

            Integer toVirtualWarehouseUsableQty = virtualInventoryQtyList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()) && StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId()) && StrUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getToVirtualWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setToVirtualWarehouseUsableQty(toVirtualWarehouseUsableQty);
            Integer fromVirtualWarehouseUsableQty = virtualInventoryQtyList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), paramDTO.getWarehouseId()) && StrUtil.equals(obj.getSkuId(), paramDTO.getSkuId()) && StrUtil.equals(obj.getVirtualWarehouseId(), paramDTO.getFromVirtualWarehouseId()))
                    .map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).findFirst().orElse(MathUtil.ZERO);
            resultDTO.setFromVirtualWarehouseUsableQty(fromVirtualWarehouseUsableQty);
            resultList.add(resultDTO);
        }
        return resultList;
    }

}
