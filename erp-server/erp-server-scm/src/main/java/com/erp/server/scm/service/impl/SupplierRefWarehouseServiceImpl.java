package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
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

    

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SupplierRefWarehouseDTO.AddDTO addDTO) {
        SupplierRefWarehouseEntity supplierRefWarehouseEntity = new SupplierRefWarehouseEntity();
        BeanMapperUtils.copy(addDTO, supplierRefWarehouseEntity);

        // 数据处理
        handleData(supplierRefWarehouseEntity);

        log.info("开始新增供应商关联仓库单");
        boolean save = super.save(supplierRefWarehouseEntity);
        if(!save) {
            throw new ServiceException("供应商关联仓库单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "供应商关联仓库单" , supplierRefWarehouseEntity.getId());
        operateLogService.addModuleOperateLog(msg,  ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), supplierRefWarehouseEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(supplierRefWarehouseEntity.getId(), supplierRefWarehouseEntity.getId());
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
        handleData(supplierRefWarehouseEntity);
        log.info("编辑 开始修改供应商关联仓库单数据，id：【{}】", old.getId());
        boolean save = super.updateById(supplierRefWarehouseEntity);
        if(!save) {
            throw new ServiceException("供应商关联仓库单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录供应商关联仓库单日志数据，id：【{}】", supplierRefWarehouseEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), supplierRefWarehouseEntity.getId(), "供应商关联仓库单");
        operateLogService.addModuleOperateLogByObj(old, supplierRefWarehouseEntity, ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), supplierRefWarehouseEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    public SupplierRefWarehouseDTO.ViewDTO view(String id) {
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到合同管理单数据"));
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
        return new PagingVO(pageData);
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
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到合同管理单数据"));
        boolean flag = super.removeById(entity.getId());
        if (!flag) {
            throw new ServiceException(ApiError.ERROR_DATA_DELETE);
        }
        // 记录操作日志
        log.info("删除 开始记录合同管理单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】删除单据 ", UserContext.getDefaultLoginUser().getUserName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), entity.getId(), "删除操作");
        return BatchResultDTO.success(entity.getId(), entity.getSupplierCode(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO enable(String id) {
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到合同管理单数据"));
        if (!entity.getDisabled()) {
            throw new ServiceException(ApiError.ERROR_ENABLE_FAIL);
        }
        entity.setDisabled(Boolean.FALSE);
        super.updateById(entity);
        // 记录操作日志
        log.info("启用 开始记录合同管理单日志数据，id：【{}】", id);
        operateLogService.addModuleOperateLog("启用单据 ", ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), entity.getId(), "启用操作");
        return BatchResultDTO.success(entity.getId(), entity.getSupplierCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disable(String id) {
        SupplierRefWarehouseEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到合同管理单数据"));
        if (!entity.getDisabled()) {
            throw new ServiceException(ApiError.ERROR_DISABLE_FAIL);
        }
        entity.setDisabled(Boolean.TRUE);
        super.updateById(entity);
        // 记录操作日志
        log.info("禁用 开始记录合同管理单日志数据，id：【{}】", id);
        operateLogService.addModuleOperateLog("禁用单据 ", ModuleTypeEnum.SUPPLIER_REF_WAREHOUSE.getCode(), entity.getId(), "禁用操作");
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
            if (ObjUtil.isEmpty(warehouseLocationEntity)) {
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
            entity.setWarehouseLocationCode(warehouseLocationEntity.getCode());
            entity.setDisabled(SupplierRefWarehouseTabEnum.getCodeByName(excelDTO.getDisabledName()));
            SupplierRefWarehouseEntity oldEntity = getByOne(entity.getSupplierId(), entity.getWarehouseId(), entity.getWarehouseLocationCode());
            if (ObjUtil.isNotEmpty(oldEntity)) {
                entity.setId(oldEntity.getId());
            }
            super.saveOrUpdate(entity);
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SupplierRefWarehouseEntity entity) {
        //更新供应商编码
        if (CharSequenceUtil.isBlank(entity.getSupplierCode())) {
            SupplierEntity supplierEntity = supplierService.getById(entity.getSupplierId());
            entity.setSupplierCode(supplierEntity.getCode());
        }
        SupplierRefWarehouseEntity oldEntity = getByOne(entity.getSupplierId(), entity.getWarehouseId(), entity.getWarehouseLocationCode());
        if (ObjUtil.isNotEmpty(oldEntity) && !CharSequenceUtil.equals(entity.getId(),oldEntity.getId())) {
            //仓库信息
            WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class, oldEntity.getWarehouseId());
            throw new ServiceException(ApiError.ERROR_SUPPLIER_REF_WAREHOUSE_EXIST,oldEntity.getSupplierCode(),warehouseEntity.getName(),oldEntity.getWarehouseLocationCode());
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
