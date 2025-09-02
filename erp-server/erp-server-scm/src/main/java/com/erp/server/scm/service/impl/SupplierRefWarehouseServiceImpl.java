package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.SupplierRefWarehouseDTO;
import com.erp.model.scm.dto.excel.SupplierRefWarehouseExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierRefWarehouseEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierRefWarehouseTabEnum;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.scm.listener.SupplierRefWarehouseExcelListener;
import com.erp.server.scm.mapper.SupplierRefWarehouseMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierRefWarehouseService;
import com.erp.server.scm.service.SupplierService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alibaba.excel.EasyExcelFactory.read;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_REF_WAREHOUSE;

/**
 * <p>
 * 供应商关联仓库表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-06-18
 */
@Slf4j
@Service
public class SupplierRefWarehouseServiceImpl extends SuperServiceImpl<SupplierRefWarehouseMapper, SupplierRefWarehouseEntity> implements SupplierRefWarehouseService {
    @Resource
    private ModuleOperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SupplierService supplierService;

    

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierRefWarehouseDTO.AddDTO addDTO) {
        // 数据处理
       List<SupplierRefWarehouseEntity> list = handleAddData(addDTO);

        log.info("开始新增供应商关联仓库单");
        boolean save = super.saveBatch(list);
        if(!save) {
            throw new ServiceException("供应商关联仓库单保存失败");
        }
        return new BaseResultDTO.AddDTO(list.get(0).getId(), list.get(0).getId());
    }

    /**
     * 新增数据处理
     * @author will
     * @date 2025/6/20 10:52
     * @param addDTO
     * @return List<SupplierRefWarehouseEntity>
     */
    private List<SupplierRefWarehouseEntity> handleAddData (SupplierRefWarehouseDTO.AddDTO addDTO) {
        // 更新供应商编码
        if (CharSequenceUtil.isBlank(addDTO.getSupplierCode())) {
            SupplierEntity supplierEntity = supplierService.getById(addDTO.getSupplierId());
            addDTO.setSupplierCode(supplierEntity.getCode());
        }
        //空仓位处理
        if (CollUtil.isEmpty(addDTO.getWarehouseLocationCodeList())){
            SupplierRefWarehouseEntity supplierRefWarehouseEntity = new SupplierRefWarehouseEntity();
            BeanMapperUtils.copy(addDTO, supplierRefWarehouseEntity);
            // 数据处理
            handleData(supplierRefWarehouseEntity);
            return Collections.singletonList(supplierRefWarehouseEntity);
        }
        List<String> warehouseLocationCodeList = addDTO.getWarehouseLocationCodeList();

        //验证新增是否全局仓位和正常仓位同时存在
        long count = warehouseLocationCodeList.stream().filter(CharSequenceUtil::isBlank).count();
        if (warehouseLocationCodeList.size() > MathUtil.ONE && count > MathUtil.ZERO) {
            WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, addDTO.getWarehouseId());
            throw new ServiceException(
                    ApiError.ERROR_SUPPLIER_REF_WAREHOUSE_GLOBAL_CONFLICT,
                    addDTO.getSupplierCode(),
                    warehouse.getName()
            );
        }
        List<SupplierRefWarehouseEntity> list = new ArrayList<>();
        for (String warehouseLocationCode : warehouseLocationCodeList) {
            SupplierRefWarehouseEntity supplierRefWarehouseEntity = new SupplierRefWarehouseEntity();
            BeanMapperUtils.copy(addDTO, supplierRefWarehouseEntity);
            supplierRefWarehouseEntity.setWarehouseLocationCode(warehouseLocationCode);
            // 数据处理
            handleData(supplierRefWarehouseEntity);
            list.add(supplierRefWarehouseEntity);
        }
        return list;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SupplierRefWarehouseDTO.UpdateDTO addOrUpdateDTO) {
        SupplierRefWarehouseEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "供应商关联仓库单"));
        SupplierRefWarehouseEntity supplierRefWarehouseEntity =  BeanMapperUtils.map(SupplierRefWarehouseEntity.class, addOrUpdateDTO);

        // 数据处理
        supplierRefWarehouseEntity.setWarehouseLocationCode(ObjectUtil.isEmpty(supplierRefWarehouseEntity.getWarehouseLocationCode()) ? "" : supplierRefWarehouseEntity.getWarehouseLocationCode());
        handleData(supplierRefWarehouseEntity);
        log.info("编辑 开始修改供应商关联仓库单数据，id：【{}】", old.getId());
        boolean save = super.updateById(supplierRefWarehouseEntity);
        if(!save) {
            throw new ServiceException("供应商关联仓库单保存失败");
        }
        handleLogData(old,supplierRefWarehouseEntity);
        // 记录主单操作日志
        log.info("编辑 开始记录供应商关联仓库单日志数据，id：【{}】", supplierRefWarehouseEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierRefWarehouseEntity.getId(), "供应商关联仓库单");
        operateLogService.addModuleOperateLogByObj(old, supplierRefWarehouseEntity, ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), supplierRefWarehouseEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    /**
     * 日志数据处理
     * @author will
     * @date 2025/6/23 16:55
     * @param old
     * @param entity
     * @return void
     */
    private void handleLogData (SupplierRefWarehouseEntity old, SupplierRefWarehouseEntity entity) {
        //供应商名称
        if (!CharSequenceUtil.equals(old.getSupplierId(), entity.getSupplierId())) {
            List<SupplierEntity> supplierList =supplierService.listByIds( Arrays.asList(old.getWarehouseId(), entity.getWarehouseId()));
            Map<String, String> supplierMap = supplierList.stream().collect(Collectors.toMap(SupplierEntity::getId, SupplierEntity::getName));
            old.setSupplierName(supplierMap.get(old.getSupplierId()));
            entity.setSupplierName(supplierMap.get(entity.getSupplierId()));
        }
        //仓库名称
        if (!CharSequenceUtil.equals(old.getWarehouseId(), entity.getWarehouseId())) {
            List<WarehouseEntity> warehosueList = FeignQuery.getByIds(WarehouseEntity.class, Arrays.asList(old.getWarehouseId(), entity.getWarehouseId()));
            Map<String, String> warehouseMap = warehosueList.stream().collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
            old.setWarehouseName(warehouseMap.get(old.getWarehouseId()));
            entity.setWarehouseName(warehouseMap.get(entity.getWarehouseId()));
        }
    }

    @Override
    public SupplierRefWarehouseDTO.ViewDTO view(String id) {
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到供应商关联仓库单数据"));
        SupplierRefWarehouseDTO.ViewDTO data = BeanMapperUtils.map(SupplierRefWarehouseDTO.ViewDTO.class, entity);
        return data;
    }

    @Override
    public PagingVO<SupplierRefWarehouseDTO.ListDTO> paging(PagingDTO<SupplierRefWarehouseDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SupplierRefWarehouseDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        handlePaging(pageData.getRecords());
        return new PagingVO(pageData);
    }
    /**
     * 分页数据处理
     * @author will
     * @date 2025/6/19 16:39
     * @param list
     * @return void
     */
    private void handlePaging (List<SupplierRefWarehouseDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (SupplierRefWarehouseDTO.ListDTO listDTO : list) {
            listDTO.setDisabledName(SupplierRefWarehouseTabEnum.getNameByCode(listDTO.getDisabled()));
            listDTO.setWarehouseLocationName(CharSequenceUtil.isBlank(listDTO.getWarehouseLocationCode()) ? "" : listDTO.getWarehouseLocationName());
        }
    }

    @Override
    public List<SupplierRefWarehouseDTO.TabListDTO> tabList(PermissionsDTO param) {
        List<SupplierRefWarehouseDTO.TabListDTO> tabList = this.baseMapper.tabList(param);
        Map<Boolean, Integer> map = CollUtil.isEmpty(tabList) ? new HashMap<>() : tabList.stream().collect(Collectors.toMap(SupplierRefWarehouseDTO.TabListDTO::getTabFlag, SupplierRefWarehouseDTO.TabListDTO::getCount));
        SupplierRefWarehouseTabEnum[] values = SupplierRefWarehouseTabEnum.values();
        List<SupplierRefWarehouseDTO.TabListDTO> list = new ArrayList<>();
        for (SupplierRefWarehouseTabEnum item : values) {
            SupplierRefWarehouseDTO.TabListDTO resultDTO = new SupplierRefWarehouseDTO.TabListDTO();
            Integer count = map.get(item.getCode());
            resultDTO.setCount(ObjUtil.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到供应商关联仓库单数据"));
        boolean flag = super.removeById(entity.getId());
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE);
        }
        // 记录操作日志
        log.info("删除 开始记录供应商关联仓库单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】删除单据 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), entity.getId(), "删除操作");
        return BatchResultDTO.success(entity.getId(), entity.getSupplierCode(), OperationTypeEnum.DELETE);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateDisabled(String id,Boolean disabled) {
        SupplierRefWarehouseEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST,"仓库绑定数据");
        }
        Boolean oldDisabled = entity.getDisabled();
        if (oldDisabled.equals(disabled)) {
            return BatchResultDTO.fail(entity.getId(), entity.getSupplierCode(), "状态未变更，无需更新");
        }
        entity.setDisabled(disabled);
        super.updateById(entity);

        // 操作日志
        String msg = StrUtil.format("状态由【{}】更新为【{}】",SupplierRefWarehouseTabEnum.getNameByCode(oldDisabled),SupplierRefWarehouseTabEnum.getNameByCode(disabled));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), entity.getId(), "状态更新");

        return BatchResultDTO.success(entity.getId(), entity.getSupplierCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/supplierRefWarehouseTemplate.xlsx";
        String excelName = "仓库绑定.xlsx";
        ExcelUtil.downloadTemplate(path,excelName,response);
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        SupplierRefWarehouseExcelListener excelListenerUtil = new SupplierRefWarehouseExcelListener();
        try {
            read(excelFile.getInputStream(), SupplierRefWarehouseExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入物流场频错误！{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        List<SupplierRefWarehouseExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<SupplierRefWarehouseExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<SupplierRefWarehouseExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            String excelPath = "excel/supplierRefWarehouseError.xlsx";
            String name = "supplierRefWarehouse";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(SupplierRefWarehouseDTO.PagingParamDTO params) {
        downloadTaskFeign.saveDownloadTask("仓库绑定", EXPORT_SCM_SUPPLIER_REF_WAREHOUSE.getCode(), params);
        return Boolean.TRUE;
    }


    /**
     * 导入数据处理
     * @author will
     * @date 2025/6/19 10:14
     * @param successList
     * @param errorList
     * @return void
     */
    private void handleImportSuccessList (List<SupplierRefWarehouseExcelDTO> successList,List<SupplierRefWarehouseExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        //供应商信息
        List<String> supplierNameList = successList.stream().distinct().map(SupplierRefWarehouseExcelDTO::getSupplierName).collect(Collectors.toList());
        List<SupplierEntity> supplierList = supplierService.listBySupplierByNames(supplierNameList);
        Map<String, SupplierEntity> supplierMap = CollUtil.isEmpty(supplierList) ? new HashMap<>() : supplierList.stream().collect(Collectors.toMap(SupplierEntity::getName, Function.identity()));


        //仓库信息
        List<String> warehosueNameList = successList.stream().distinct().map(SupplierRefWarehouseExcelDTO::getWarehouseName).collect(Collectors.toList());
        List<WarehouseEntity> warehouseList = FeignQuery.create(WarehouseEntity.class).in(WarehouseEntity::getName, warehosueNameList).list();
        List<String> warehouseIdList = CollUtil.isEmpty(warehouseList) ? Collections.emptyList() : warehouseList.stream().map(WarehouseEntity::getId).distinct().collect(Collectors.toList());
        Map<String, WarehouseEntity> warehouseMap = CollUtil.isEmpty(warehouseList) ? new HashMap<>() : warehouseList.stream().collect(Collectors.toMap(WarehouseEntity::getName, Function.identity()));

        //仓位信息
        List<String> warehouseLocationNameList = successList.stream().distinct().map(SupplierRefWarehouseExcelDTO::getWarehouseLocationName).collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationList = FeignQuery.create(WarehouseLocationEntity.class).in(WarehouseLocationEntity::getWarehouseId, warehouseIdList).in(WarehouseLocationEntity::getName, warehouseLocationNameList).list();
        Map<String, WarehouseLocationEntity> warehouseLocationMap = CollUtil.isEmpty(warehouseLocationList) ? new HashMap<>() : warehouseLocationList.stream().collect(Collectors.toMap(obj -> StrUtil.format("{}-{}",obj.getWarehouseId(),obj.getName()), Function.identity()));

        for (SupplierRefWarehouseExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            //供应商
            SupplierEntity supplierEntity = supplierMap.get(excelDTO.getSupplierName());
            if (ObjUtil.isEmpty(supplierEntity)) {
                errorMsgList.add("供应商信息未找到");
            } else {
                if (!CharSequenceUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),supplierEntity.getApproveStatus().getCode())) {
                    errorMsgList.add("供应商信息未审核");
                }
            }
            //仓库
            WarehouseEntity warehouseEntity = warehouseMap.get(excelDTO.getWarehouseName());
            if (ObjUtil.isEmpty(warehouseEntity)) {
                errorMsgList.add("仓库信息未找到");
            } else {
                if (!CharSequenceUtil.equals(ApproveStatusEnum.APPROVE.getStatus(),warehouseEntity.getApproveStatus().getCode())) {
                    errorMsgList.add("仓库信息未审核");
                }
            }
            //仓位
            warehouseEntity = ObjUtil.isEmpty(warehouseEntity) ? new WarehouseEntity() :warehouseEntity;
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationMap.get(StrUtil.format("{}-{}", warehouseEntity.getId(), excelDTO.getWarehouseLocationName()));
            if (ObjUtil.isEmpty(warehouseLocationEntity) && CharSequenceUtil.isNotBlank(excelDTO.getWarehouseLocationName())) {
                errorMsgList.add("仓位信息未找到");
            }
            if (CollectionUtils.isNotEmpty(errorMsgList)) {
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            SupplierRefWarehouseEntity entity = new SupplierRefWarehouseEntity();
            entity.setSupplierId(supplierEntity.getId());
            entity.setSupplierCode(supplierEntity.getCode());
            entity.setWarehouseId(warehouseEntity.getId());
            entity.setWarehouseLocationCode(ObjectUtil.isEmpty(warehouseLocationEntity) ? "" : warehouseLocationEntity.getCode());
            entity.setDisabled(SupplierRefWarehouseTabEnum.getCodeByName(excelDTO.getDisabledName()));
            SupplierRefWarehouseEntity oldEntity = getByOne(entity.getSupplierId(), entity.getWarehouseId(), entity.getWarehouseLocationCode());
            if (ObjUtil.isNotEmpty(oldEntity)) {
                entity.setId(oldEntity.getId());
            }
            try {
                handleData(entity);
            } catch (Exception e) {
                log.error("导入数据异常，供应商名称：{}，仓库名称：{}，仓位名称：{}，错误信息：{}", excelDTO.getSupplierName(), excelDTO.getWarehouseName(), excelDTO.getWarehouseLocationName(), e.getMessage());
                errorMsgList.add(e.getMessage());
                excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                errorList.add(excelDTO);
                continue;
            }
            super.saveOrUpdate(entity);
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(SupplierRefWarehouseEntity entity) {
        // 更新供应商编码
        SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
        entity.setSupplierCode(supplierEntity.getCode());
        if (supplierEntity.getSrmDisabled()) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_SRM_DISABLE);
        }
        // 检查是否存在冲突记录
        checkConflictRecords(entity);
    }

    /**
     * 检查冲突记录
     */
    private void checkConflictRecords(SupplierRefWarehouseEntity entity) {
        String warehouseLocationCode = entity.getWarehouseLocationCode();
        boolean isGlobalLocation = CharSequenceUtil.isBlank(warehouseLocationCode);

        // 情况1：当前记录是全局仓位（仓位为空）
        if (isGlobalLocation) {
            checkGlobalLocationConflict(entity);
        }
        // 情况2：当前记录是具体仓位
        else {
            checkSpecificLocationConflict(entity);
        }
    }

    /**
     * 检查全局仓位冲突
     */
    private void checkGlobalLocationConflict(SupplierRefWarehouseEntity entity) {
        // 查找该供应商在该仓库下的所有记录
        List<SupplierRefWarehouseEntity> existingRecords = lambdaQuery()
                .eq(SupplierRefWarehouseEntity::getSupplierId, entity.getSupplierId())
                .eq(SupplierRefWarehouseEntity::getWarehouseId, entity.getWarehouseId())
                .list();

        // 排除当前记录自身（更新时）
        List<SupplierRefWarehouseEntity> conflictRecords = existingRecords.stream()
                .filter(record -> !record.getId().equals(entity.getId()))
                .collect(Collectors.toList());

        // 如果存在其他记录（无论是全局还是具体仓位）
        if (!conflictRecords.isEmpty()) {
            WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, entity.getWarehouseId());
            throw new ServiceException(
                    ApiError.ERROR_SUPPLIER_REF_WAREHOUSE_GLOBAL_CONFLICT,
                    entity.getSupplierCode(),
                    warehouse.getName()
            );
        }
    }

    /**
     * 检查具体仓位冲突
     */
    private void checkSpecificLocationConflict(SupplierRefWarehouseEntity entity) {
        // 1. 检查是否已存在全局仓位记录
        SupplierRefWarehouseEntity globalRecord = lambdaQuery()
                .eq(SupplierRefWarehouseEntity::getSupplierId, entity.getSupplierId())
                .eq(SupplierRefWarehouseEntity::getWarehouseId, entity.getWarehouseId())
                .eq(SupplierRefWarehouseEntity::getWarehouseLocationCode,"")
                .ne(entity.getId() != null, SupplierRefWarehouseEntity::getId, entity.getId())
                .one();

        if (globalRecord != null) {
            WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, entity.getWarehouseId());
            throw new ServiceException(
                    ApiError.ERROR_SUPPLIER_REF_WAREHOUSE_GLOBAL_EXISTS,
                    entity.getSupplierCode(),
                    warehouse.getName()
            );
        }

        // 2. 检查是否已存在相同仓位的记录
        SupplierRefWarehouseEntity sameLocationRecord = lambdaQuery()
                .eq(SupplierRefWarehouseEntity::getSupplierId, entity.getSupplierId())
                .eq(SupplierRefWarehouseEntity::getWarehouseId, entity.getWarehouseId())
                .eq(SupplierRefWarehouseEntity::getWarehouseLocationCode, entity.getWarehouseLocationCode())
                .ne(entity.getId() != null, SupplierRefWarehouseEntity::getId, entity.getId())
                .one();

        if (sameLocationRecord != null) {
            WarehouseEntity warehouse = FeignQuery.getById(WarehouseEntity.class, entity.getWarehouseId());
            throw new ServiceException(
                    ApiError.ERROR_SUPPLIER_REF_WAREHOUSE_EXIST,
                    entity.getSupplierCode(),
                    warehouse.getName(),
                    entity.getWarehouseLocationCode()
            );
        }
    }

    /**
     * 查询单个数据
     * @author will
     * @date 2025/6/19 10:36
     * @param supplierId
     * @param warehouseId
     * @param warehouseLocationCode
     * @return SupplierRefWarehouseEntity
     */
    private SupplierRefWarehouseEntity getByOne(String supplierId, String warehouseId, String warehouseLocationCode) {
        return lambdaQuery().eq(SupplierRefWarehouseEntity::getSupplierId,supplierId)
                .eq(SupplierRefWarehouseEntity::getWarehouseId,warehouseId)
                .eq(SupplierRefWarehouseEntity::getWarehouseLocationCode,warehouseLocationCode)
                .last("limit 1").one();
    }
}
