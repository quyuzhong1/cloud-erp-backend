package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PdaTabFlagPcEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.model.wms.enums.VirtualWarehouseAllocationTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.VirtualWarehouseAllocationMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
    private VirtualWarehouseAllocationHandleService virtualWarehouseAllocationHandleService;
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
    @Lazy
    private VirtualWarehouseAllocationServiceImpl service;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(VirtualWarehouseAllocationDTO.AddDTO addDTO) {
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = new VirtualWarehouseAllocationEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseAllocationEntity);
        // 数据处理
        handleData(virtualWarehouseAllocationEntity, addDTO.getDetailList());
        log.info("开始新增虚拟仓分货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FH);
        virtualWarehouseAllocationEntity.setCode(code);
        boolean save = super.save(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓分货单", virtualWarehouseAllocationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), virtualWarehouseAllocationEntity.getId(), "新增操作");
        // 新增明细
        virtualWarehouseAllocationDetailService.batchAdd(addDTO, virtualWarehouseAllocationEntity.getId());
        //保存附件
        wmsAttachmentService.batchSave(addDTO.getAttachUrlList(), addDTO.getAttachNameList(), WmsConstant.QC_PRODUCT, virtualWarehouseAllocationEntity.getId());

        return new BaseResultDTO.AddDTO(virtualWarehouseAllocationEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseAllocationDTO.UpdateDTO updateDTO) {
        VirtualWarehouseAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓分货单"));
        VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity = BeanMapperUtils.map(VirtualWarehouseAllocationEntity.class, updateDTO);
        // 数据处理
        handleData(virtualWarehouseAllocationEntity, updateDTO.getDetailList());
        log.info("编辑 开始修改虚拟仓分货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseAllocationEntity);
        if (!save) {
            throw new ServiceException("虚拟仓分货单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓分货单日志数据，单号：【{}】", virtualWarehouseAllocationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), virtualWarehouseAllocationEntity.getCode(), "虚拟仓分货单");
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseAllocationEntity, ModuleTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(), virtualWarehouseAllocationEntity.getId(), msg);
        // 新增明细
        virtualWarehouseAllocationDetailService.batchUpdate(updateDTO, virtualWarehouseAllocationEntity.getId());
        //保存附件
        wmsAttachmentService.batchSave(updateDTO.getAttachUrlList(), updateDTO.getAttachNameList(), WmsConstant.QC_PRODUCT, virtualWarehouseAllocationEntity.getId());
        return Boolean.TRUE;
    }

    /**
     * 列表查询
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
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Arrays.asList(id));
        List<String> attachmentUrlList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> attachmentNameList = attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        viewDTO.setAttachUrlList(attachmentUrlList);
        viewDTO.setAttachNameList(attachmentNameList);

        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, id));
        //获取所有的sku信息
        List<String> skuIds = detailList.stream().map(VirtualWarehouseAllocationDetailEntity::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);
        List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailList);
        detailDtos.forEach(detailDto -> {
            SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())).findFirst().orElse(null);
            if (Objects.nonNull(skuVO)) {
                detailDto.setProductName(skuVO.getBrandName());
                detailDto.setImageUrl(skuVO.getSkuImagesUrl());
            }
        });
        viewDTO.setDetailList(detailDtos);
        return viewDTO;
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
        setSkuInfo(records);
        return new PagingVO(pageData);
    }

    private void setSkuInfo(List<VirtualWarehouseAllocationDTO.ListDTO> records) {
        List<String> skuIds = records.stream().map(record -> record.getSkuId()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isNotEmpty(skuVOList)) {
            records.forEach(record -> {
                SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), record.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(skuVO)) {
                    record.setSkuName(skuVO.getSkuName());
                    record.setImageUrl(skuVO.getSkuImagesUrl());
                    record.setProductName(skuVO.getBrandName());
                }
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
        //进行拆单并创建中台任务数据进行同步
        virtualWarehouseAllocationHandleService.handleData(allocationEntity);
        return BatchResultDTO.success(allocationEntity.getId(), allocationEntity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @Override
    public void export(VirtualWarehouseAllocationDTO.ExportDTO dto, HttpServletResponse response) {
        List<VirtualWarehouseAllocationDTO.ListDTO> list = baseMapper.listExport(dto);
        if (CollectionUtils.isNotEmpty(list)) {
            //填充数据
            setSkuInfo(list);
        }
        StringBuffer stringBuffer = new StringBuffer();
        String excelPath = "excel/VirtualWarehouseAllocation.xlsx";
        String name = "分货单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        stringBuffer.append(date);
        stringBuffer.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, stringBuffer.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Override
    public VirtualWarehouseAllocationDTO.DetailDto importFile(String type, MultipartFile excelFile, HttpServletResponse response) {
//        VirtualWarehouseAllocationExcelListener excelListenerUtil = new VirtualWarehouseAllocationExcelListener(this, warehouseService, warehouseLocationService, plmTaskFeign, inventoryService);
//        try {
//            EasyExcel.read(excelFile.getInputStream(), MoveInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
//        } catch (IOException e) {
//            log.error("导入错误！", e);  throw new ServiceException(ApiError.ERROR_95124);
//        } catch (ExcelCommonException e) {
//            log.error("导入格式错误！",e);
//            throw new ServiceException(ApiError.ERROR_1016);
//        }
//        //验证导入数据是否为空
//        List<MoveInfoExcelDTO> allList = excelListenerUtil.getAllList();
//        if (CollectionUtils.isEmpty(allList)) {
//            throw new ServiceException(ApiError.ERROR_95123);
//        }
//        VirtualWarehouseAllocationDTO.DetailDto importDTO = new VirtualWarehouseAllocationDTO.DetailDto;
//        List<VirtualWarehouseAllocationDTO.DetailDto> successList = excelListenerUtil.getSuccessList();
//        String url = "";
//        List<MoveInfoExcelDTO> errorList = excelListenerUtil.getErrorList();
//        if (errorList.size() > 0) {
//            String fileName = "分货单导入错误信息.xlsx";
//            File file = ExcelUtil.exportFile(fileName, "virtualWarehouseAllocationError", errorList, MoveInfoExcelDTO.class);
//            if (file != null && !file.isDirectory()) {
//                url = FastDFSClientUtil.uploadFile(file, fileName);
//            }
//        }
//        importDTO.setSuccessList(successList);
//        importDTO.setErrorUrl(url);
//        return importDTO;
        return null;
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
    public BatchResultDTO addAndSubmit(VirtualWarehouseAllocationDTO.AddDTO dto) {
        BaseResultDTO.AddDTO add = service.add(dto);
        VirtualWarehouseAllocationEntity allocationEntity = this.getById(add.getId());
        return service.submit(allocationEntity);
    }


    private void checkDetail(VirtualWarehouseAllocationEntity allocationEntity) {
        //获取明细
        List<VirtualWarehouseAllocationDetailEntity> detailList = virtualWarehouseAllocationDetailService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationDetailEntity>()
                .eq(VirtualWarehouseAllocationDetailEntity::getMainId, allocationEntity.getId()));
        List<VirtualWarehouseAllocationDTO.DetailDto> detailDtos = BeanMapperUtils.copyList(VirtualWarehouseAllocationDTO.DetailDto.class, detailList);
        checkInfoAndQty(allocationEntity, detailDtos);
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
            virtualWarehouseAllocationEntity.setDirection(-1);
        }
        checkInfoAndQty(virtualWarehouseAllocationEntity, detailList);
    }

    private void checkInfoAndQty(VirtualWarehouseAllocationEntity virtualWarehouseAllocationEntity, List<VirtualWarehouseAllocationDTO.DetailDto> detailList) {
        String type = virtualWarehouseAllocationEntity.getType();
        List<String> skuIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> warehouseIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> vmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getToVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<String> fromVmIds = detailList.stream().map(VirtualWarehouseAllocationDTO.DetailDto::getFromVirtualWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        vmIds.addAll(fromVmIds);
        //获取所有的sku、仓库、虚拟仓信息
        List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIds);
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(warehouseIds);
        List<VirtualWarehouseEntity> virtualWarehouseList = virtualWarehouseService.listByIds(vmIds);
        //获取库存
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = getQty(detailList, type);
        detailList.forEach(detailDto -> {
            //校验sku、仓库、虚拟仓是否存在
            checkInfo(detailDto, skuVOList, warehouseList, virtualWarehouseList);
            //校验库存
            checkQty(detailDto, type, virtualInventoryQtyList);
        });
    }

    private List<VirtualInventoryDTO.ViewQtyDTO> getQty(List<VirtualWarehouseAllocationDTO.DetailDto> detailList, String type) {
        VirtualInventoryDTO.QtyTypeDTO qtyTypeDTO = new VirtualInventoryDTO.QtyTypeDTO();
        qtyTypeDTO.setType(type);
        List<VirtualInventoryDTO.QtySearchDTO> qtySearchDTOS = BeanMapperUtils.copyList(VirtualInventoryDTO.QtySearchDTO.class, detailList);
        qtyTypeDTO.setQtySearchList(qtySearchDTOS);
        List<VirtualInventoryDTO.ViewQtyDTO> virtualInventoryQtyList = virtualInventoryService.getQty(qtyTypeDTO);
        return virtualInventoryQtyList;
    }

    private static void checkInfo(VirtualWarehouseAllocationDTO.DetailDto detailDto, List<SkuVO> skuVOList, List<WarehouseDTO.UpdateDTO> warehouseList, List<VirtualWarehouseEntity> virtualWarehouseList) {
        SkuVO skuVO = skuVOList.stream().filter(item -> Objects.equals(item.getSkuId(), detailDto.getSkuId())).findFirst().orElse(null);
        if (Objects.isNull(skuVO)) {
            throw new ServiceException(ApiError.ERROR_SKU_NOTFOUND, detailDto.getSkuId());
        }
        detailDto.setSkuNo(skuVO.getSkuNo());
        WarehouseDTO.UpdateDTO updateDTO = warehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getWarehouseId())).findFirst().orElse(null);
        if (Objects.isNull(updateDTO)) {
            throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOTFOUND, detailDto.getWarehouseId());
        } else {
            if (updateDTO.getDisabled()) {
                throw new ServiceException(ApiError.ERROR_WAREHOUSE_NOTACTIVE, detailDto.getWarehouseId());
            }
        }
        detailDto.setWarehouseName(updateDTO.getName());
        if (StringUtils.isBlank(detailDto.getToVirtualWarehouseId()) && StringUtils.isBlank(detailDto.getFromVirtualWarehouseId())) {
            throw new ServiceException(ApiError.ERROR_FROM_TO_VM_BOTHEMPTY);
        }
        if (StringUtils.isNotBlank(detailDto.getToVirtualWarehouseId())) {
            VirtualWarehouseEntity toVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getToVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(toVmWarehouse)) {
                throw new ServiceException(ApiError.ERROR_TOVM_NOTFOUND, detailDto.getToVirtualWarehouseId());
            } else {
                if (toVmWarehouse.getDisabled()) {
                    throw new ServiceException(ApiError.ERROR_TOVM_NOTACTIVE, detailDto.getToVirtualWarehouseId());
                }
            }
            detailDto.setToVirtualWarehouseName(toVmWarehouse.getName());
            detailDto.setToVirtualWarehouseCode(toVmWarehouse.getCode());
        }
        if (StringUtils.isNotBlank(detailDto.getFromVirtualWarehouseId())) {
            VirtualWarehouseEntity fromVmWarehouse = virtualWarehouseList.stream().filter(item -> Objects.equals(item.getId(), detailDto.getFromVirtualWarehouseId())).findFirst().orElse(null);
            if (Objects.isNull(fromVmWarehouse)) {
                throw new ServiceException(ApiError.ERROR_FROMVM_NOTFOUND, detailDto.getFromVirtualWarehouseId());
            } else {
                if (fromVmWarehouse.getDisabled()) {
                    throw new ServiceException(ApiError.ERROR_FROMVM_NOTACTIVE, detailDto.getFromVirtualWarehouseId());
                }
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
}
