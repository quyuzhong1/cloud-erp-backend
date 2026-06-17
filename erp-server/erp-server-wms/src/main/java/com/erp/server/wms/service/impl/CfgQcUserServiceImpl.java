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
import com.common.business.utils.ApplicationContextUtils;
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
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.CfgQcUserEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.server.wms.listener.CfgQcUserExcelListener;
import com.erp.server.wms.mapper.CfgQcUserMapper;
import com.erp.server.wms.service.CfgQcUserService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.MessageFormat;
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
    private WarehouseService warehouseService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Override
    public BaseResultDTO.AddDTO add(CfgQcUserDTO.AddDTO addDTO) {
        HandleDataResult ctx = handleData(addDTO, null);
        CfgQcUserEntity entity = ApplicationContextUtils.getBean(CfgQcUserServiceImpl.class).doAdd(addDTO, ctx);
        return new BaseResultDTO.AddDTO(entity.getId(), ctx.getSupplier().getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    public CfgQcUserEntity doAdd(CfgQcUserDTO.AddDTO addDTO, HandleDataResult ctx) {
        SupplierEntity supplier = ctx.getSupplier();

        // 保存主表
        CfgQcUserEntity cfgQcUserEntity = new CfgQcUserEntity();
        BeanMapper.copy(addDTO, cfgQcUserEntity);
        cfgQcUserEntity.setStockinQcUserId(addDTO.getStockInQcUserId());
        cfgQcUserEntity.setStockoutQcUserId(addDTO.getStockOutQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserId(addDTO.getNewProductStockInQcUserId());
        // 前端只传 id，name 由后端根据 id 在金蝶业务员（ZJY）列表中反查后写入，防止与前端传值不一致或为空
        fillQcUserNames(cfgQcUserEntity, ctx.getQcUserNameMap());

        boolean save = super.save(cfgQcUserEntity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_SAVE_FAILED);
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增质检员配置，供应商【{}】",
                UserContext.getDefaultLoginUser().getUserName(), supplier.getName());
        operateLogService.addModuleOperateLog(msg, null, cfgQcUserEntity.getId(), "新增质检员配置");

        return cfgQcUserEntity;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "updateDTO.getId()")
    @Override
    public Boolean update(CfgQcUserDTO.UpdateDTO updateDTO) {
        CfgQcUserEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.CFG_QC_USER_NOT_EXIST));

        HandleDataResult ctx = handleData(updateDTO, old);
        ApplicationContextUtils.getBean(CfgQcUserServiceImpl.class).doUpdate(updateDTO, ctx);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public CfgQcUserEntity doUpdate(CfgQcUserDTO.UpdateDTO updateDTO, HandleDataResult ctx) {
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
        cfgQcUserEntity.setStockoutQcUserId(updateDTO.getStockOutQcUserId());
        cfgQcUserEntity.setNewProductStockinQcUserId(updateDTO.getNewProductStockInQcUserId());
        // 前端只传 id，name 由后端根据 id 在金蝶业务员（ZJY）列表中反查后写入，防止与前端传值不一致或为空
        fillQcUserNames(cfgQcUserEntity, ctx.getQcUserNameMap());

        boolean update = super.updateById(cfgQcUserEntity);
        if (!update) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }

        // 记录操作日志
        String msg = StrUtil.format("用户【{}】编辑质检员配置，供应商【{}】",
                UserContext.getDefaultLoginUser().getUserName(), supplierName);
        operateLogService.addModuleOperateLog(msg, null, updateDTO.getId(), "编辑质检员配置");

        return cfgQcUserEntity;
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
        // 校验已选质检员均在仓库对应组织的业务员管理（ZJY）中，并返回 id -> name 映射供后续回填使用
        Map<String, String> qcUserNameMap = validateQcUsersInOrg(warehouse, dto);

        return new HandleDataResult(supplier, warehouse, supplierId, warehouseId, qcUserNameMap);
    }

    @Getter
    @AllArgsConstructor
    private static class HandleDataResult {
        private final SupplierEntity supplier;
        private final WarehouseEntity warehouse;
        private final String supplierId;
        private final String warehouseId;
        /** 仓库所属组织下 ZJY 业务员的 id -> name 映射，用于回填质检员姓名 */
        private final Map<String, String> qcUserNameMap;
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
     *
     * @return 仓库所属组织下 ZJY 业务员的 id -> name 映射，供调用方回填质检员姓名复用
     */
    private Map<String, String> validateQcUsersInOrg(WarehouseEntity warehouse, CfgQcUserDTO.CommonDTO dto) {
        Map<String, String> userNameMap = loadQcUserMapByOrgId(warehouse.getOrgId());
        Set<String> validUserIds = userNameMap.keySet();
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
        return userNameMap;
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

    /**
     * 查询仓库所属组织下 ZJY 业务员列表，返回 id -> name 映射
     * <p>
     * 优先取 realName，没有则回退到 userName，与下拉接口 {@link #qcUserList(String)} 保持一致
     */
    private Map<String, String> loadQcUserMapByOrgId(String orgId) {
        KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO listDTO = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
        listDTO.setOrgId(orgId);
        listDTO.setType(KingdeeBusinessOperatorTypeEnum.ZJY.getCode());
        ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> apiResult;
        try {
            apiResult = kingdeeFeign.listKingdeeUser(listDTO);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载业务员管理质检员列表失败，orgId={}", orgId, e);
            throw new ServiceException(ApiError.CFG_QC_USER_LOAD_QC_USER_LIST_FAILED);
        }
        if (apiResult == null || !apiResult.isSuccess()) {
            throw new ServiceException(ApiError.CFG_QC_USER_LOAD_QC_USER_LIST_FAILED);
        }
        if (CollUtil.isEmpty(apiResult.getData())) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (UserInfoDTO.BusinessOperationUserDTO user : apiResult.getData()) {
            if (StrUtil.isBlank(user.getUserId())) {
                continue;
            }
            String name = StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUserName();
            result.put(user.getUserId(), StrUtil.nullToEmpty(name));
        }
        return result;
    }

    /**
     * 根据 entity 上已设置的质检员 id，从金蝶业务员列表反查姓名并回填到 entity 对应的 name 字段
     * <p>
     * id 为空则 name 也置空；id 不为空时姓名通过 {@link #validateQcUsersInOrg} 已校验存在
     */
    private void fillQcUserNames(CfgQcUserEntity entity, Map<String, String> userNameMap) {
        entity.setStockinQcUserName(resolveQcUserName(entity.getStockinQcUserId(), userNameMap));
        entity.setStockoutQcUserName(resolveQcUserName(entity.getStockoutQcUserId(), userNameMap));
        entity.setOutsideQcUserName(resolveQcUserName(entity.getOutsideQcUserId(), userNameMap));
        entity.setInsideQcUserName(resolveQcUserName(entity.getInsideQcUserId(), userNameMap));
        entity.setNewProductStockinQcUserName(resolveQcUserName(entity.getNewProductStockinQcUserId(), userNameMap));
        entity.setB2bOutsideQcUserName(resolveQcUserName(entity.getB2bOutsideQcUserId(), userNameMap));
        entity.setReturnQcUserName(resolveQcUserName(entity.getReturnQcUserId(), userNameMap));
    }

    private String resolveQcUserName(String userId, Map<String, String> userNameMap) {
        if (StrUtil.isBlank(userId)) {
            return "";
        }
        return StrUtil.nullToEmpty(userNameMap.get(userId));
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
    public void importCfgQcUser(BaseDTO.ImportDTO dto) {
        if (StrUtil.isNotBlank(dto.getUserId())) {
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getUserId());
            if (Objects.nonNull(findUserDTO)) {
                LoginUser user = new LoginUser();
                user.setUid(findUserDTO.getUserId());
                user.setUserName(findUserDTO.getUserName());
                user.setRealName(findUserDTO.getRealName());
                user.setUserAccount(findUserDTO.getMobile());
                user.setMobile(findUserDTO.getMobile());
                UserContext.setLoginUser(user);
            }
        }

        CfgQcUserExcelListener excelListenerUtil = new CfgQcUserExcelListener(dto.getTaskId(), dto.getImportCount());
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

    /**
     * 处理导入校验通过的数据：批量预加载后落库，跳过 add/update 中的重复 Feign 校验
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleImportSuccessList(List<CfgQcUserDTO.ImportExcelDTO> successList,
                                        List<CfgQcUserDTO.ImportExcelDTO> errorList) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }

        List<String> warehouseNames = successList.stream()
                .map(CfgQcUserDTO.ImportExcelDTO::getWarehouseName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<WarehouseDTO.ListDTO>> warehouseMap = new HashMap<>();
        if (CollUtil.isNotEmpty(warehouseNames)) {
            List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(warehouseNames);
            if (CollUtil.isNotEmpty(warehouseList)) {
                warehouseMap = warehouseList.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getName));
            }
        }

        List<String> supplierCodes = successList.stream()
                .map(CfgQcUserDTO.ImportExcelDTO::getSupplierCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, SupplierEntity> supplierByCode = new HashMap<>();
        if (CollUtil.isNotEmpty(supplierCodes)) {
            List<SupplierEntity> suppliers = supplierFeign.listByCodes(supplierCodes);
            if (CollUtil.isNotEmpty(suppliers)) {
                for (SupplierEntity supplier : suppliers) {
                    if (StrUtil.isNotBlank(supplier.getCode())) {
                        supplierByCode.putIfAbsent(supplier.getCode(), supplier);
                    }
                }
            }
        }

        Map<String, CfgQcUserEntity> existsByKey = loadImportExistsByKey(successList, supplierByCode, warehouseMap);
        Map<String, Map<String, String>> qcUserNameToIdMapByOrgId = new HashMap<>();
        Map<String, Map<String, String>> qcUserIdToNameMapByOrgId = new HashMap<>();
        Set<String> qcUserLoadFailedOrgIds = new HashSet<>();

        CfgQcUserServiceImpl bean = ApplicationContextUtils.getBean(CfgQcUserServiceImpl.class);
        for (CfgQcUserDTO.ImportExcelDTO dto : new ArrayList<>(successList)) {
            try {
                SupplierEntity supplier = supplierByCode.get(dto.getSupplierCode());
                if (Objects.isNull(supplier)) {
                    dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_SUPPLIER_NOT_FOUND.getMsg(), dto.getSupplierCode()));
                    errorList.add(dto);
                    continue;
                }

                WarehouseDTO.ListDTO warehouse = resolveImportWarehouse(dto, warehouseMap);
                if (warehouse == null) {
                    errorList.add(dto);
                    continue;
                }

                CfgQcUserEntity exists = existsByKey.get(buildSupplierWarehouseKey(supplier.getId(), warehouse.getId()));
                String orgId = warehouse.getOrgId();

                Map<String, String> qcUserMap = getImportQcUserNameToIdMap(orgId, qcUserNameToIdMapByOrgId,
                        qcUserIdToNameMapByOrgId, qcUserLoadFailedOrgIds);
                List<String> notFoundUsers = new ArrayList<>();
                String stockInId = resolveImportQcUserId(qcUserMap, dto.getStockInQcUserName(), notFoundUsers);
                String stockOutId = resolveImportQcUserId(qcUserMap, dto.getStockOutQcUserName(), notFoundUsers);
                String outsideId = resolveImportQcUserId(qcUserMap, dto.getOutsideQcUserName(), notFoundUsers);
                String insideId = resolveImportQcUserId(qcUserMap, dto.getInsideQcUserName(), notFoundUsers);
                String newProductStockInId = resolveImportQcUserId(qcUserMap, dto.getNewProductStockInQcUserName(), notFoundUsers);
                String b2bOutsideId = resolveImportQcUserId(qcUserMap, dto.getB2bOutsideQcUserName(), notFoundUsers);
                String returnId = resolveImportQcUserId(qcUserMap, dto.getReturnQcUserName(), notFoundUsers);
                if (!notFoundUsers.isEmpty()) {
                    Set<String> uniq = new LinkedHashSet<>(notFoundUsers);
                    dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_IMPORT_USER_NOT_IN_ORG.getMsg(),
                            String.join(",", uniq)));
                    errorList.add(dto);
                    continue;
                }

                if (!hasAnyImportQcUserId(stockInId, stockOutId, outsideId, insideId, newProductStockInId, b2bOutsideId, returnId)) {
                    dto.setErrorMsg(ApiError.CFG_QC_USER_QC_USER_AT_LEAST_ONE.getMsg());
                    errorList.add(dto);
                    continue;
                }

                Map<String, String> qcUserIdToNameMap = getImportQcUserIdToNameMap(orgId, qcUserNameToIdMapByOrgId,
                        qcUserIdToNameMapByOrgId, qcUserLoadFailedOrgIds);
                String pairKey = buildSupplierWarehouseKey(supplier.getId(), warehouse.getId());
                CfgQcUserEntity saved;
                if (Objects.nonNull(exists)) {
                    CfgQcUserDTO.UpdateDTO updateDTO = new CfgQcUserDTO.UpdateDTO();
                    copyImportToCommonDTO(dto, updateDTO, stockInId, stockOutId, outsideId, insideId,
                            newProductStockInId, b2bOutsideId, returnId);
                    saved = saveImportRow(bean, updateDTO, exists, supplier, warehouse.getId(), qcUserIdToNameMap);
                } else {
                    CfgQcUserDTO.AddDTO addDTO = new CfgQcUserDTO.AddDTO();
                    copyImportToCommonDTO(dto, addDTO, stockInId, stockOutId, outsideId, insideId,
                            newProductStockInId, b2bOutsideId, returnId);
                    saved = saveImportRow(bean, addDTO, null, supplier, warehouse.getId(), qcUserIdToNameMap);
                }
                existsByKey.put(pairKey, saved);
            } catch (Exception e) {
                log.error("质检员配置导入处理失败，supplierCode={}", dto.getSupplierCode(), e);
                String message = BatchResultDTO.resolveFailMsg(e);
                dto.setErrorMsg(message.length() > 200 ? message.substring(0, 200) : message);
                errorList.add(dto);
            }
        }
        successList.clear();
    }

    /**
     * 导入单行落库：复用批量校验/缓存数据，直接 doAdd/doUpdate
     */
    private CfgQcUserEntity saveImportRow(CfgQcUserServiceImpl bean, CfgQcUserDTO.CommonDTO dto,
                                          CfgQcUserEntity exists, SupplierEntity supplier,
                                          String warehouseId, Map<String, String> qcUserIdToNameMap) {
        if (supplier == null || StrUtil.isBlank(supplier.getId())) {
            throw new ServiceException(ApiError.SUPPLIER_NOT_FOUND);
        }
        if (StrUtil.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.CFG_QC_USER_WAREHOUSE_REQUIRED);
        }
        Map<String, String> userNameMap = qcUserIdToNameMap != null ? qcUserIdToNameMap : Collections.emptyMap();
        HandleDataResult ctx = new HandleDataResult(supplier, null, supplier.getId(), warehouseId, userNameMap);
        if (exists != null) {
            CfgQcUserDTO.UpdateDTO updateDTO = new CfgQcUserDTO.UpdateDTO();
            updateDTO.setId(exists.getId());
            copyQcUserFields(dto, updateDTO);
            CfgQcUserEntity updated = bean.doUpdate(updateDTO, ctx);
            updated.setId(exists.getId());
            updated.setSupplierId(supplier.getId());
            updated.setWarehouseId(warehouseId);
            if (exists.getVersion() != null) {
                updated.setVersion(exists.getVersion() + 1);
            }
            return updated;
        }
        CfgQcUserDTO.AddDTO addDTO = new CfgQcUserDTO.AddDTO();
        addDTO.setSupplierId(supplier.getId());
        addDTO.setWarehouseId(warehouseId);
        copyQcUserFields(dto, addDTO);
        return bean.doAdd(addDTO, ctx);
    }

    private Map<String, CfgQcUserEntity> loadImportExistsByKey(List<CfgQcUserDTO.ImportExcelDTO> rows,
                                                               Map<String, SupplierEntity> supplierByCode,
                                                               Map<String, List<WarehouseDTO.ListDTO>> warehouseMap) {
        Map<String, CfgQcUserEntity> existsByKey = new HashMap<>();
        if (supplierByCode.isEmpty() || warehouseMap.isEmpty()) {
            return existsByKey;
        }
        Set<String> seenPairKeys = new LinkedHashSet<>();
        List<CfgQcUserDTO.SupplierWarehousePair> pairs = new ArrayList<>();
        for (CfgQcUserDTO.ImportExcelDTO dto : rows) {
            if (StrUtil.isBlank(dto.getSupplierCode()) || StrUtil.isBlank(dto.getWarehouseName())) {
                continue;
            }
            SupplierEntity supplier = supplierByCode.get(dto.getSupplierCode());
            if (supplier == null || StrUtil.isBlank(supplier.getId())) {
                continue;
            }
            List<WarehouseDTO.ListDTO> warehouses = warehouseMap.get(dto.getWarehouseName());
            if (CollUtil.isEmpty(warehouses) || warehouses.size() != 1) {
                continue;
            }
            String warehouseId = warehouses.get(0).getId();
            if (StrUtil.isBlank(warehouseId)) {
                continue;
            }
            String pairKey = buildSupplierWarehouseKey(supplier.getId(), warehouseId);
            if (seenPairKeys.add(pairKey)) {
                pairs.add(new CfgQcUserDTO.SupplierWarehousePair(supplier.getId(), warehouseId));
            }
        }
        if (pairs.isEmpty()) {
            return existsByKey;
        }
        List<CfgQcUserEntity> existsList = baseMapper.selectBySupplierWarehousePairs(pairs);
        if (CollUtil.isEmpty(existsList)) {
            return existsByKey;
        }
        for (CfgQcUserEntity entity : existsList) {
            existsByKey.putIfAbsent(buildSupplierWarehouseKey(entity.getSupplierId(), entity.getWarehouseId()), entity);
        }
        return existsByKey;
    }

    private WarehouseDTO.ListDTO resolveImportWarehouse(CfgQcUserDTO.ImportExcelDTO dto,
                                                        Map<String, List<WarehouseDTO.ListDTO>> warehouseMap) {
        String warehouseName = dto.getWarehouseName();
        if (StrUtil.isBlank(warehouseName)) {
            dto.setErrorMsg(ApiError.CFG_QC_USER_WAREHOUSE_REQUIRED.getMsg());
            return null;
        }
        List<WarehouseDTO.ListDTO> warehouses = warehouseMap.get(warehouseName.trim());
        if (CollUtil.isEmpty(warehouses)) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND.getMsg(), warehouseName));
            return null;
        }
        if (warehouses.size() > 1) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NAME_DUPLICATE.getMsg(), warehouseName));
            return null;
        }
        WarehouseDTO.ListDTO warehouse = warehouses.get(0);
        if (Boolean.TRUE.equals(warehouse.getDisabled())
                || warehouse.getApproveStatus() == null
                || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouse.getApproveStatus().getStatus())) {
            dto.setErrorMsg(MessageFormat.format(ApiError.CFG_QC_USER_WAREHOUSE_NOT_FOUND.getMsg(), warehouseName));
            return null;
        }
        return warehouse;
    }

    private String buildSupplierWarehouseKey(String supplierId, String warehouseId) {
        return StrUtil.blankToDefault(supplierId, "") + "::" + StrUtil.blankToDefault(warehouseId, "");
    }

    private Map<String, String> getImportQcUserNameToIdMap(String orgId,
                                                           Map<String, Map<String, String>> qcUserNameToIdMapByOrgId,
                                                           Map<String, Map<String, String>> qcUserIdToNameMapByOrgId,
                                                           Set<String> qcUserLoadFailedOrgIds) {
        loadImportQcUserMapsByOrgId(orgId, qcUserNameToIdMapByOrgId, qcUserIdToNameMapByOrgId, qcUserLoadFailedOrgIds);
        return qcUserNameToIdMapByOrgId.getOrDefault(StrUtil.blankToDefault(orgId, ""), Collections.emptyMap());
    }

    private Map<String, String> getImportQcUserIdToNameMap(String orgId,
                                                           Map<String, Map<String, String>> qcUserNameToIdMapByOrgId,
                                                           Map<String, Map<String, String>> qcUserIdToNameMapByOrgId,
                                                           Set<String> qcUserLoadFailedOrgIds) {
        loadImportQcUserMapsByOrgId(orgId, qcUserNameToIdMapByOrgId, qcUserIdToNameMapByOrgId, qcUserLoadFailedOrgIds);
        return qcUserIdToNameMapByOrgId.getOrDefault(StrUtil.blankToDefault(orgId, ""), Collections.emptyMap());
    }

    private void loadImportQcUserMapsByOrgId(String orgId,
                                           Map<String, Map<String, String>> qcUserNameToIdMapByOrgId,
                                           Map<String, Map<String, String>> qcUserIdToNameMapByOrgId,
                                           Set<String> qcUserLoadFailedOrgIds) {
        String cacheKey = StrUtil.blankToDefault(orgId, "");
        if (qcUserLoadFailedOrgIds.contains(cacheKey)) {
            throw new ServiceException(ApiError.CFG_QC_USER_LOAD_QC_USER_LIST_FAILED);
        }
        if (qcUserNameToIdMapByOrgId.containsKey(cacheKey)) {
            return;
        }
        Map<String, String> nameToIdMap = new HashMap<>();
        Map<String, String> idToNameMap = new HashMap<>();
        KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO param = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
        param.setType(KingdeeBusinessOperatorTypeEnum.ZJY.getCode());
        if (StrUtil.isNotBlank(orgId)) {
            param.setOrgId(orgId);
        }
        ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> apiResult;
        try {
            apiResult = kingdeeFeign.listKingdeeUser(param);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("加载业务员管理质检员列表失败，orgId={}", orgId, e);
            qcUserLoadFailedOrgIds.add(cacheKey);
            throw new ServiceException(ApiError.CFG_QC_USER_LOAD_QC_USER_LIST_FAILED);
        }
        if (apiResult == null || !apiResult.isSuccess()) {
            qcUserLoadFailedOrgIds.add(cacheKey);
            throw new ServiceException(ApiError.CFG_QC_USER_LOAD_QC_USER_LIST_FAILED);
        }
        if (CollUtil.isNotEmpty(apiResult.getData())) {
            for (UserInfoDTO.BusinessOperationUserDTO user : apiResult.getData()) {
                if (StrUtil.isBlank(user.getUserId())) {
                    continue;
                }
                String displayName = StrUtil.isNotBlank(user.getRealName()) ? user.getRealName() : user.getUserName();
                idToNameMap.putIfAbsent(user.getUserId(), StrUtil.nullToEmpty(displayName));
                if (StrUtil.isNotBlank(user.getRealName())) {
                    nameToIdMap.putIfAbsent(user.getRealName(), user.getUserId());
                }
                if (StrUtil.isNotBlank(user.getUserName())) {
                    nameToIdMap.putIfAbsent(user.getUserName(), user.getUserId());
                }
            }
        }
        qcUserNameToIdMapByOrgId.put(cacheKey, nameToIdMap);
        qcUserIdToNameMapByOrgId.put(cacheKey, idToNameMap);
    }

    private String resolveImportQcUserId(Map<String, String> qcUserMap, String name, List<String> notFoundUsers) {
        if (StrUtil.isBlank(name)) {
            return null;
        }
        String userId = qcUserMap.get(name.trim());
        if (StrUtil.isBlank(userId)) {
            notFoundUsers.add(name.trim());
            return null;
        }
        return userId;
    }

    private boolean hasAnyImportQcUserId(String... userIds) {
        if (userIds == null) {
            return false;
        }
        for (String userId : userIds) {
            if (StrUtil.isNotBlank(userId)) {
                return true;
            }
        }
        return false;
    }

    private void copyImportToCommonDTO(CfgQcUserDTO.ImportExcelDTO importDTO, CfgQcUserDTO.CommonDTO commonDTO,
                                       String stockInId, String stockOutId, String outsideId, String insideId,
                                       String newProductStockInId, String b2bOutsideId, String returnId) {
        commonDTO.setStockInQcUserId(stockInId);
        commonDTO.setStockInQcUserName(importDTO.getStockInQcUserName());
        commonDTO.setStockOutQcUserId(stockOutId);
        commonDTO.setStockOutQcUserName(importDTO.getStockOutQcUserName());
        commonDTO.setOutsideQcUserId(outsideId);
        commonDTO.setOutsideQcUserName(importDTO.getOutsideQcUserName());
        commonDTO.setInsideQcUserId(insideId);
        commonDTO.setInsideQcUserName(importDTO.getInsideQcUserName());
        commonDTO.setNewProductStockInQcUserId(newProductStockInId);
        commonDTO.setNewProductStockInQcUserName(importDTO.getNewProductStockInQcUserName());
        commonDTO.setB2bOutsideQcUserId(b2bOutsideId);
        commonDTO.setB2bOutsideQcUserName(importDTO.getB2bOutsideQcUserName());
        commonDTO.setReturnQcUserId(returnId);
        commonDTO.setReturnQcUserName(importDTO.getReturnQcUserName());
    }

    private void copyQcUserFields(CfgQcUserDTO.CommonDTO source, CfgQcUserDTO.CommonDTO target) {
        target.setStockInQcUserId(source.getStockInQcUserId());
        target.setStockInQcUserName(source.getStockInQcUserName());
        target.setStockOutQcUserId(source.getStockOutQcUserId());
        target.setStockOutQcUserName(source.getStockOutQcUserName());
        target.setOutsideQcUserId(source.getOutsideQcUserId());
        target.setOutsideQcUserName(source.getOutsideQcUserName());
        target.setInsideQcUserId(source.getInsideQcUserId());
        target.setInsideQcUserName(source.getInsideQcUserName());
        target.setNewProductStockInQcUserId(source.getNewProductStockInQcUserId());
        target.setNewProductStockInQcUserName(source.getNewProductStockInQcUserName());
        target.setB2bOutsideQcUserId(source.getB2bOutsideQcUserId());
        target.setB2bOutsideQcUserName(source.getB2bOutsideQcUserName());
        target.setReturnQcUserId(source.getReturnQcUserId());
        target.setReturnQcUserName(source.getReturnQcUserName());
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
    @Transactional(rollbackFor = Exception.class)
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
            WarehouseEntity warehouse = warehouseService.getById(warehouseId);
            if (ObjectUtil.isNotEmpty(warehouse)) {
                orgId = warehouse.getOrgId();
            }
        }
        
        // 构建查询质检员的参数
        KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO listDTO = new KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO();
        listDTO.setOrgId(orgId);
        // 质检员类型是 ZJY
        listDTO.setType(KingdeeBusinessOperatorTypeEnum.ZJY.getCode());
        
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
