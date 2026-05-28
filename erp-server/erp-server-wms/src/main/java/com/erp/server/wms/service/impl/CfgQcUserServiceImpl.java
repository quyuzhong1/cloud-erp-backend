package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.utils.BeanMapper;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.server.wms.listener.CfgQcUserExcelListener;
import com.erp.server.wms.mapper.CfgQcUserMapper;
import com.erp.server.wms.service.CfgQcUserService;
import com.erp.server.wms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_CFG_QC_USER;

/**
 * <p>
 * 质检员配置服务实现类
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
 */
@Slf4j
@Service
public class CfgQcUserServiceImpl extends SuperServiceImpl<CfgQcUserMapper, CfgQcUserEntity> implements CfgQcUserService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private com.erp.server.wms.service.WarehouseService warehouseService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgQcUserDTO.AddDTO addDTO) {
        String supplierId = addDTO.getSupplierId();
        
        // 校验供应商是否已存在
        checkSupplierExists(supplierId);
        
        // 获取供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException("供应商不存在");
        }

        // 保存主表
        CfgQcUserEntity cfgQcUserEntity = new CfgQcUserEntity();
        BeanMapper.copy(addDTO, cfgQcUserEntity);
        // DTO字段名是stockIn，数据库是stockin，需要手动设置
        cfgQcUserEntity.setStockinQcUserId(addDTO.getStockInQcUserId());
        cfgQcUserEntity.setStockinQcUserName(addDTO.getStockInQcUserName());
        cfgQcUserEntity.setStockoutQcUserId(addDTO.getStockOutQcUserId());
        cfgQcUserEntity.setStockoutQcUserName(addDTO.getStockOutQcUserName());
        cfgQcUserEntity.setNewProductStockinQcUserId(addDTO.getNewProductStockInQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserName(addDTO.getNewProductStockInQcUserName());
        
        boolean save = super.save(cfgQcUserEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增质检员配置，供应商【{}】", 
                UserContext.getDefaultLoginUser().getUserName(), supplier.getName());
        operateLogService.addModuleOperateLog(msg, null, cfgQcUserEntity.getId(), "新增质检员配置");

        return new BaseResultDTO.AddDTO(cfgQcUserEntity.getId(), supplier.getCode());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQcUserDTO.UpdateDTO updateDTO) {
        CfgQcUserEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "质检员配置"));

        // 修改时供应商信息不允许编辑，直接使用原供应商ID
        String supplierId = old.getSupplierId();

        // 更新主表
        CfgQcUserEntity cfgQcUserEntity = new CfgQcUserEntity();
        BeanMapper.copy(updateDTO, cfgQcUserEntity);
        cfgQcUserEntity.setId(updateDTO.getId());
        cfgQcUserEntity.setSupplierId(supplierId);
        // DTO字段名是stockIn，数据库是stockin，需要手动设置
        cfgQcUserEntity.setStockinQcUserId(updateDTO.getStockInQcUserId());
        cfgQcUserEntity.setStockinQcUserName(updateDTO.getStockInQcUserName());
        cfgQcUserEntity.setStockoutQcUserId(updateDTO.getStockOutQcUserId());
        cfgQcUserEntity.setStockoutQcUserName(updateDTO.getStockOutQcUserName());
        cfgQcUserEntity.setNewProductStockinQcUserId(updateDTO.getNewProductStockInQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserName(updateDTO.getNewProductStockInQcUserName());

        boolean update = super.updateById(cfgQcUserEntity);
        if (!update) {
            throw new ServiceException("更新失败");
        }

        // 记录操作日志
        SupplierEntity supplier = supplierFeign.getSupplierById(supplierId);
        String supplierName = supplier != null ? supplier.getName() : "";
        String msg = StrUtil.format("用户【{}】编辑质检员配置，供应商【{}】", 
                UserContext.getDefaultLoginUser().getUserName(), supplierName);
        operateLogService.addModuleOperateLog(msg, null, updateDTO.getId(), "编辑质检员配置");
        
        return Boolean.TRUE;
    }

    /**
    * 校验供应商是否已存在配置
    */
    private void checkSupplierExists(String supplierId) {
        CfgQcUserEntity exists = baseMapper.selectBySupplierId(supplierId);
        if (ObjectUtil.isNotEmpty(exists)) {
            throw new ServiceException("该供应商已配置质检员，不允许重复添加");
        }
    }

    @Override
    public PagingVO<CfgQcUserDTO.ListDTO> paging(PagingDTO<CfgQcUserDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgQcUserDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(CfgQcUserDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgQcUserDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/cfgQcUser.xlsx";
        String name = "质检员配置";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入质检员配置", IMPORT_WMS_CFG_QC_USER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importCfgQcUser(BaseDTO.ImportDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        FindUserDTO findUserDTO = userList.stream().filter(e -> StrUtil.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if (Objects.nonNull(findUserDTO)) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        CfgQcUserExcelListener excelListenerUtil = new CfgQcUserExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), CfgQcUserDTO.ImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<CfgQcUserDTO.ImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "质检员配置错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, CfgQcUserDTO.ImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public CfgQcUserDTO.ViewDTO view(String id) {
        CfgQcUserEntity cfgQcUserEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检员配置"));
        
        CfgQcUserDTO.ViewDTO data = new CfgQcUserDTO.ViewDTO();
        BeanMapper.copy(cfgQcUserEntity, data);
        // 数据库字段是stockin，DTO是stockIn，需要手动设置
        data.setStockInQcUserId(cfgQcUserEntity.getStockinQcUserId());
        data.setStockInQcUserName(cfgQcUserEntity.getStockinQcUserName());
        data.setStockOutQcUserId(cfgQcUserEntity.getStockoutQcUserId());
        data.setStockOutQcUserName(cfgQcUserEntity.getStockoutQcUserName());
        data.setNewProductStockInQcUserId(cfgQcUserEntity.getNewProductStockinQcUserId());
        data.setNewProductStockInQcUserName(cfgQcUserEntity.getNewProductStockinQcUserName());
        
        // 获取供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierById(cfgQcUserEntity.getSupplierId());
        if (ObjectUtil.isNotEmpty(supplier)) {
            data.setSupplierCode(supplier.getCode());
            data.setSupplierName(supplier.getName());
        }

        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        CfgQcUserEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到质检员配置"));
        String supplierId = entity.getSupplierId();
        
        // 删除主表数据
        super.removeById(id);
        
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除质检员配置，ID【{}】", 
                UserContext.getDefaultLoginUser().getUserName(), id);
        operateLogService.addModuleOperateLog(msg, null, id, "删除质检员配置");
        
        return BatchResultDTO.success(id, supplierId);
    }

    @Override
    public List<CfgQcUserDTO.QcUserSelectDTO> qcUserList(String warehouseId) {
        List<CfgQcUserDTO.QcUserSelectDTO> result = new ArrayList<>();
        
        // 如果传入仓库ID，需要根据仓库组织过滤
        String orgId = null;
        if (StrUtil.isNotBlank(warehouseId)) {
            // 根据warehouseId获取仓库信息
            com.erp.model.wms.entity.WarehouseEntity warehouse = warehouseService.getById(warehouseId);
            if (ObjectUtil.isNotEmpty(warehouse)) {
                orgId = warehouse.getOrgId();
            }
        }
        
        // 构建查询质检员的参数
        KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO listDTO = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
        listDTO.setOrgId(orgId);
        // 质检员类型是 ZJY
        listDTO.setType("ZJY");
        
        // 获取质检员列表
        ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> apiResult = kingdeeFeign.listKingdeeUser(listDTO);
        if (apiResult == null || !apiResult.isSuccess() || CollUtil.isEmpty(apiResult.getData())) {
            return result;
        }

        final List<UserInfoDTO.BusinessOperationUserDTO> qcUserList = apiResult.getData();
        for (UserInfoDTO.BusinessOperationUserDTO user : qcUserList) {
            CfgQcUserDTO.QcUserSelectDTO dto = new CfgQcUserDTO.QcUserSelectDTO();
            dto.setId(user.getUserId());
            // 使用realName或者userName作为名称
            dto.setName(StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUserName());
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public CfgQcUserEntity getBySupplierId(String supplierId) {
        return this.lambdaQuery()
                .eq(CfgQcUserEntity::getSupplierId, supplierId)
                .eq(CfgQcUserEntity::getIsDeleted, false)
                .one();
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<CfgQcUserDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        
        // 获取所有供应商ID
        List<String> supplierIds = list.stream().map(CfgQcUserDTO.ListDTO::getSupplierId).collect(Collectors.toList());
        
        // 批量获取供应商信息
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
        
        for (CfgQcUserDTO.ListDTO data : list) {
            // 填充供应商信息
            SupplierDTO.SupplierSimpleDTO supplier = supplierMap.get(data.getSupplierId());
            if (ObjectUtil.isNotEmpty(supplier)) {
                data.setSupplierCode(supplier.getCode());
                data.setSupplierName(supplier.getName());
            }
        }
    }
}
