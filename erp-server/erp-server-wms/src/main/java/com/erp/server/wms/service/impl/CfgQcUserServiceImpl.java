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
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.model.wms.entity.WarehouseEntity;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_CFG_QC_USER;
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
        // 公共校验
        HandleDataResult ctx = handleData(addDTO, null);
        SupplierEntity supplier = ctx.getSupplier();

        // 保存主表
        CfgQcUserEntity cfgQcUserEntity = new CfgQcUserEntity();
        BeanMapper.copy(addDTO, cfgQcUserEntity);
        cfgQcUserEntity.setStockinQcUserId(addDTO.getStockInQcUserId());
        cfgQcUserEntity.setStockinQcUserName(addDTO.getStockInQcUserName());
        cfgQcUserEntity.setStockoutQcUserId(addDTO.getStockOutQcUserId());
        cfgQcUserEntity.setStockoutQcUserName(addDTO.getStockOutQcUserName());
        cfgQcUserEntity.setNewProductStockinQcUserId(addDTO.getNewProductStockInQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserName(addDTO.getNewProductStockInQcUserName());

        boolean save = super.save(cfgQcUserEntity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_SAVE_FAILED);
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
    @DistributeLocker(keyName = "updateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQcUserDTO.UpdateDTO updateDTO) {
        CfgQcUserEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.CFG_QC_USER_NOT_EXIST));

        // 公共校验
        HandleDataResult ctx = handleData(updateDTO, old);
        String supplierId = ctx.getSupplierId();
        String warehouseId = ctx.getWarehouseId();
        SupplierEntity supplier = ctx.getSupplier();
        String supplierName = supplier.getName();

        // 更新主表
        CfgQcUserEntity cfgQcUserEntity = new CfgQcUserEntity();
        BeanMapper.copy(updateDTO, cfgQcUserEntity);
        cfgQcUserEntity.setSupplierId(supplierId);
        cfgQcUserEntity.setWarehouseId(warehouseId);
        cfgQcUserEntity.setStockinQcUserId(updateDTO.getStockInQcUserId());
        cfgQcUserEntity.setStockinQcUserName(updateDTO.getStockInQcUserName());
        cfgQcUserEntity.setStockoutQcUserId(updateDTO.getStockOutQcUserId());
        cfgQcUserEntity.setStockoutQcUserName(updateDTO.getStockOutQcUserName());
        cfgQcUserEntity.setNewProductStockinQcUserId(updateDTO.getNewProductStockInQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserName(updateDTO.getNewProductStockInQcUserName());

        boolean update = super.updateById(cfgQcUserEntity);
        if (!update) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }

        // 记录操作日志
        String msg = StrUtil.format("用户【{}】编辑质检员配置，供应商【{}】",
                UserContext.getDefaultLoginUser().getUserName(), supplierName);
        operateLogService.addModuleOperateLog(msg, null, updateDTO.getId(), "编辑质检员配置");

        return Boolean.TRUE;
    }

    /**
    * 唯一性校验：同一供应商 + 同一仓库不允许重复配置
    *
    * @param supplierId   供应商ID
    * @param warehouseId  仓库ID（可空，空表示全仓库通用配置）
    * @param supplierName 供应商名称（用于错误提示，可为 null）
    * @param excludeId    需要排除的记录主键（编辑场景排除自身；新增时传 null）
    */
    private void checkSupplierWarehouseUnique(String supplierId, String warehouseId,
                                              String supplierName, String warehouseName, String excludeId) {
        CfgQcUserEntity exists = baseMapper.selectBySupplierIdAndWarehouseId(supplierId, warehouseId);
        if (ObjectUtil.isEmpty(exists)) {
            return;
        }
        if (StrUtil.isNotBlank(excludeId) && Objects.equals(exists.getId(), excludeId)) {
            return;
        }
        throw new ServiceException(ApiError.CFG_QC_USER_SUPPLIER_DUPLICATE,
                StrUtil.isNotBlank(supplierName) ? supplierName : supplierId,
                StrUtil.isNotBlank(warehouseName) ? warehouseName : warehouseId);
    }

    /**
     * 新增/修改公共校验：供应商、仓库、唯一性、质检员数量及组织归属
     *
     * @param dto  入参（新增为 AddDTO，修改为 UpdateDTO）
     * @param old  原记录；新增时传 null
     */
    private HandleDataResult handleData(CfgQcUserDTO.CommonDTO dto, CfgQcUserEntity old) {
        boolean isUpdate = old != null;
        String supplierId;
        String warehouseId;
        SupplierEntity supplier;

        if (isUpdate) {
            // 修改时不允许变更供应商或仓库，入参与原记录不一致则抛异常
            validateUpdateKeyNotModifiable(old, (CfgQcUserDTO.UpdateDTO) dto);
            // 修改时供应商、仓库不允许编辑，直接使用原值
            supplierId = old.getSupplierId();
            warehouseId = old.getWarehouseId();
            supplier = supplierFeign.getSupplierById(supplierId);
        } else {
            supplierId = dto.getSupplierId();
            warehouseId = dto.getWarehouseId();
            supplier = supplierFeign.getSupplierById(supplierId);
        }
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.SUPPLIER_NOT_FOUND);
        }

        // 校验仓库是否存在且已审核启用
        WarehouseEntity warehouse = validateWarehouse(warehouseId);

        if (!isUpdate) {
            // 唯一性校验：同一供应商 + 同一仓库不允许重复配置
            checkSupplierWarehouseUnique(supplierId, warehouseId, supplier.getName(), warehouse.getName(), null);
        }

        // 入库/出库/外验/在库/新品入库/B2B外检/退货质检员至少配置一名
        validateAtLeastOneQcUser(dto);
        // 校验已选质检员均在仓库对应组织的业务员管理（ZJY）中
        validateQcUsersInOrg(warehouse, dto);

        return new HandleDataResult(supplier, warehouse, supplierId, warehouseId);
    }

    @Getter
    @AllArgsConstructor
    private static class HandleDataResult {
        private final SupplierEntity supplier;
        private final WarehouseEntity warehouse;
        private final String supplierId;
        private final String warehouseId;
    }

    /**
     * 修改时不允许变更供应商或仓库
     */
    private void validateUpdateKeyNotModifiable(CfgQcUserEntity old, CfgQcUserDTO.UpdateDTO updateDTO) {
        if (StrUtil.isNotBlank(updateDTO.getSupplierId())
                && !Objects.equals(old.getSupplierId(), updateDTO.getSupplierId())) {
            throw new ServiceException(ApiError.CFG_QC_USER_UPDATE_KEY_NOT_MODIFIABLE);
        }
        if (StrUtil.isNotBlank(updateDTO.getWarehouseId())
                && !Objects.equals(old.getWarehouseId(), updateDTO.getWarehouseId())) {
            throw new ServiceException(ApiError.CFG_QC_USER_UPDATE_KEY_NOT_MODIFIABLE);
        }
    }

    private WarehouseEntity validateWarehouse(String warehouseId) {
        if (StrUtil.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.CFG_QC_USER_WAREHOUSE_REQUIRED);
        }
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (ObjectUtil.isEmpty(warehouse) || Boolean.TRUE.equals(warehouse.getIsDeleted())) {
            throw new ServiceException(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND, warehouseId);
        }
        if (Boolean.TRUE.equals(warehouse.getDisabled())
                || warehouse.getApproveStatus() == null
                || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouse.getApproveStatus().getStatus())) {
            throw new ServiceException(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND, warehouse.getName());
        }
        return warehouse;
    }

    /**
     * 入库/出库/外验/在库/新品入库/B2B外检/退货质检员至少配置一名
     */
    private void validateAtLeastOneQcUser(CfgQcUserDTO.CommonDTO dto) {
        if (StrUtil.isNotBlank(dto.getStockInQcUserId())
                || StrUtil.isNotBlank(dto.getStockOutQcUserId())
                || StrUtil.isNotBlank(dto.getOutsideQcUserId())
                || StrUtil.isNotBlank(dto.getInsideQcUserId())
                || StrUtil.isNotBlank(dto.getNewProductStockInQcUserId())
                || StrUtil.isNotBlank(dto.getB2bOutsideQcUserId())
                || StrUtil.isNotBlank(dto.getReturnQcUserId())) {
            return;
        }
        throw new ServiceException(ApiError.CFG_QC_USER_QC_USER_AT_LEAST_ONE);
    }

    /**
     * 校验已选质检员均在业务员管理（type=ZJY）中，且属于质检仓库对应组织
     */
    private void validateQcUsersInOrg(WarehouseEntity warehouse, CfgQcUserDTO.CommonDTO dto) {
        Set<String> validUserIds = loadQcUserIdsByOrgId(warehouse.getOrgId());
        Set<String> invalidUsers = new LinkedHashSet<>();
        collectInvalidQcUser(validUserIds, dto.getStockInQcUserId(), dto.getStockInQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getStockOutQcUserId(), dto.getStockOutQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getOutsideQcUserId(), dto.getOutsideQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getInsideQcUserId(), dto.getInsideQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getNewProductStockInQcUserId(), dto.getNewProductStockInQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getB2bOutsideQcUserId(), dto.getB2bOutsideQcUserName(), invalidUsers);
        collectInvalidQcUser(validUserIds, dto.getReturnQcUserId(), dto.getReturnQcUserName(), invalidUsers);
        if (CollUtil.isNotEmpty(invalidUsers)) {
            throw new ServiceException(ApiError.CFG_QC_USER_IMPORT_USER_NOT_IN_ORG, String.join(",", invalidUsers));
        }
    }

    private void collectInvalidQcUser(Set<String> validUserIds, String userId, String userName, Set<String> invalidUsers) {
        if (StrUtil.isBlank(userId)) {
            return;
        }
        if (validUserIds.contains(userId)) {
            return;
        }
        invalidUsers.add(StrUtil.isNotBlank(userName) ? userName : userId);
    }

    private Set<String> loadQcUserIdsByOrgId(String orgId) {
        KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO listDTO = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
        listDTO.setOrgId(orgId);
        listDTO.setType("ZJY");
        ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> apiResult = kingdeeFeign.listKingdeeUser(listDTO);
        if (apiResult == null || !apiResult.isSuccess() || CollUtil.isEmpty(apiResult.getData())) {
            return Collections.emptySet();
        }
        return apiResult.getData().stream()
                .map(UserInfoDTO.BusinessOperationUserDTO::getUserId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    @Override
    public PagingVO<CfgQcUserDTO.ListDTO> paging(PagingDTO<? extends CfgQcUserDTO.PagingParamDTO> pagingParamDTO) {
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
        downloadTaskFeign.saveDownloadTask("质检员配置导出", EXPORT_WMS_CFG_QC_USER.getCode(), param);
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
        CfgQcUserEntity cfgQcUserEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.CFG_QC_USER_NOT_EXIST));

        CfgQcUserDTO.ViewDTO data = new CfgQcUserDTO.ViewDTO();
        BeanMapper.copy(cfgQcUserEntity, data);
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

        if (StrUtil.isNotBlank(cfgQcUserEntity.getWarehouseId())) {
            WarehouseEntity warehouse = warehouseService.getById(cfgQcUserEntity.getWarehouseId());
            if (ObjectUtil.isNotEmpty(warehouse)) {
                data.setWarehouseName(warehouse.getName());
            }
        }

        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        CfgQcUserEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.CFG_QC_USER_NOT_EXIST));
        String supplierId = entity.getSupplierId();
        String supplierCode = supplierId;
        SupplierEntity supplier = supplierFeign.getSupplierById(supplierId);
        if (ObjectUtil.isNotEmpty(supplier)) {
            supplierCode = supplier.getCode();
        }
        
        // 删除主表数据
        super.removeById(id);
        
        // 操作日志
        String msg = StrUtil.format("用户【{}】删除质检员配置，ID【{}】", 
                UserContext.getDefaultLoginUser().getUserName(), id);
        operateLogService.addModuleOperateLog(msg, null, id, "删除质检员配置");
        
        return BatchResultDTO.success(id, supplierCode);
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

    @Override
    public CfgQcUserEntity getBySupplierIdAndWarehouseId(String supplierId, String warehouseId) {
        if (StrUtil.isBlank(supplierId)) {
            return null;
        }
        return baseMapper.selectBySupplierIdAndWarehouseId(supplierId, warehouseId);
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
        List<String> warehouseIds = list.stream()
                .map(CfgQcUserDTO.ListDTO::getWarehouseId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量获取供应商信息
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap = supplierFeign.getSupplierSimpleInfo(supplierIds);
        Map<String, String> warehouseNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(warehouseIds)) {
            List<com.erp.model.wms.dto.WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseNameByIds(warehouseIds);
            if (CollUtil.isNotEmpty(warehouseList)) {
                warehouseNameMap = warehouseList.stream()
                        .collect(Collectors.toMap(com.erp.model.wms.dto.WarehouseDTO.UpdateDTO::getId,
                                com.erp.model.wms.dto.WarehouseDTO.UpdateDTO::getName, (a, b) -> a));
            }
        }
        
        for (CfgQcUserDTO.ListDTO data : list) {
            // 填充供应商信息
            SupplierDTO.SupplierSimpleDTO supplier = supplierMap.get(data.getSupplierId());
            if (ObjectUtil.isNotEmpty(supplier)) {
                data.setSupplierCode(supplier.getCode());
                data.setSupplierName(supplier.getName());
            }
            if (StrUtil.isNotBlank(data.getWarehouseId())) {
                data.setWarehouseName(warehouseNameMap.get(data.getWarehouseId()));
            }
        }
    }
}
