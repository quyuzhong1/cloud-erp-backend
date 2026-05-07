package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseLocationMappingDTO;
import com.erp.model.wms.dto.excel.WarehouseLocationMappingExcelDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationMappingEntity;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.WarehouseLocationMappingMapper;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.service.WarehouseLocationMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 第三方仓位映射业务类
 * @date 2024-08-14
 * @author tanmujin
 */
@Service
public class WarehouseLocationMappingServiceImpl extends SuperServiceImpl<WarehouseLocationMappingMapper, WarehouseLocationMappingEntity> implements WarehouseLocationMappingService {

    private static final String IMPORT_EVENT = "IMPORT_WMS_WAREHOUSE_LOCATION_MAPPING";

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private WarehouseMapper warehouseMapper;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<WarehouseLocationMappingDTO.ViewDTO> paging(PagingDTO<WarehouseLocationMappingDTO.SearchDTO> dto) {
        WarehouseLocationMappingDTO.SearchDTO params = dto.getParams() == null ? new WarehouseLocationMappingDTO.SearchDTO() : dto.getParams();
        Page<WarehouseLocationMappingEntity> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        LambdaQueryWrapper<WarehouseLocationMappingEntity> wrapper = buildQueryWrapper(params);
        wrapper.orderByDesc(WarehouseLocationMappingEntity::getCreateTime);
        IPage<WarehouseLocationMappingEntity> pageData = this.page(query, wrapper);
        List<WarehouseLocationMappingDTO.ViewDTO> viewList = buildViewList(pageData.getRecords());
        return new PagingVO<>(viewList, (int) pageData.getTotal(), (int) pageData.getSize(), (int) pageData.getCurrent());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(WarehouseLocationMappingDTO.AddDTO dto) {
        LoginUser user = UserContext.getNonLoginUser();
        List<Candidate> candidates = buildCandidates(dto);
        Map<Candidate, List<String>> errorMap = new LinkedHashMap<>();
        validateDuplicate(candidates, null, errorMap);
        throwIfError(errorMap);
        List<WarehouseLocationMappingEntity> addList = candidates.stream().map(Candidate::getEntity).collect(Collectors.toList());
        this.saveBatch(addList);
        addList.forEach(entity -> operateLogService.addModuleOperateLog(buildLogContent("新增", entity),
                ModuleTypeEnum.WAREHOUSE_LOCATION_MAPPING.getCode(), entity.getId(), "新增操作", user.getUid(), user.getUserName()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(WarehouseLocationMappingDTO.UpdateDTO dto) {
        LoginUser user = UserContext.getNonLoginUser();
        WarehouseLocationMappingEntity oldEntity = this.getById(dto.getId());
        if (oldEntity == null || Boolean.TRUE.equals(oldEntity.getIsDeleted())) {
            throw new ServiceException("仓位绑定不存在");
        }
        WarehouseLocationMappingDTO.AddDTO addDTO = new WarehouseLocationMappingDTO.AddDTO();
        addDTO.setSysWarehouseId(dto.getSysWarehouseId());
        addDTO.setDictPlatform(dto.getDictPlatform());
        WarehouseLocationMappingDTO.DetailDTO detailDTO = new WarehouseLocationMappingDTO.DetailDTO();
        detailDTO.setSysWarehouseLocation(dto.getSysWarehouseLocation());
        detailDTO.setThirdWarehouseLocation(dto.getThirdWarehouseLocation());
        addDTO.setDetailList(Collections.singletonList(detailDTO));

        List<Candidate> candidates = buildCandidates(addDTO);
        Candidate candidate = candidates.get(0);
        candidate.getEntity().setId(dto.getId());
        Map<Candidate, List<String>> errorMap = new LinkedHashMap<>();
        validateDuplicate(candidates, dto.getId(), errorMap);
        throwIfError(errorMap);

        this.updateById(candidate.getEntity());
        operateLogService.addModuleOperateLog(buildLogContent("编辑", candidate.getEntity()),
                ModuleTypeEnum.WAREHOUSE_LOCATION_MAPPING.getCode(), dto.getId(), "编辑操作", user.getUid(), user.getUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(WarehouseLocationMappingDTO.IdsDTO dto) {
        LoginUser user = UserContext.getNonLoginUser();
        List<WarehouseLocationMappingEntity> list = this.listByIds(dto.getIds());
        this.removeByIds(dto.getIds());
        list.forEach(entity -> operateLogService.addModuleOperateLog(buildLogContent("删除", entity),
                ModuleTypeEnum.WAREHOUSE_LOCATION_MAPPING.getCode(), entity.getId(), "删除操作", user.getUid(), user.getUserName()));
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("仓位绑定导入", IMPORT_EVENT, dto);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importWarehouseLocationMapping(BaseDTO.ImportDTO dto) {
        setImportUser(dto.getUserId());
        List<WarehouseLocationMappingExcelDTO> errorList;
        int count;
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            List<WarehouseLocationMappingExcelDTO> rowList = EasyExcel.read(new ByteArrayInputStream(bytes))
                    .head(WarehouseLocationMappingExcelDTO.class)
                    .sheet(0)
                    .doReadSync();
            count = CollectionUtils.isEmpty(rowList) ? 0 : rowList.size();
            errorList = processImportRows(rowList);
        } catch (ExcelCommonException e) {
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(count);
        String errorUrl = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "仓位绑定导入错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, WarehouseLocationMappingExcelDTO.class);
            if (!file.isDirectory()) {
                errorUrl = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(errorUrl);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        ExcelUtil.downloadTemplate("excel/warehouseLocationMappingTemplate.xlsx", "warehouseLocationMappingTemplate.xlsx", response);
    }

    @Override
    public WarehouseLocationMappingDTO.BindWarehouseDTO getBindWarehouse(String sysWarehouseId, String dictPlatform) {
        String platform = validatePlatform(dictPlatform);
        ThirdWarehouseEntity thirdWarehouseEntity = dmpThirdMappingFeign.getBySysId(sysWarehouseId, platform);
        WarehouseLocationMappingDTO.BindWarehouseDTO dto = new WarehouseLocationMappingDTO.BindWarehouseDTO();
        dto.setSysWarehouseId(sysWarehouseId);
        dto.setDictPlatform(platform);
        dto.setDictPlatformName(getPlatformName(platform));
        WarehouseEntity warehouse = warehouseMapper.selectById(sysWarehouseId);
        if (warehouse != null) {
            dto.setSysWarehouseName(warehouse.getName());
        }
        if (thirdWarehouseEntity != null) {
            dto.setBindWarehouseId(thirdWarehouseEntity.getId());
            dto.setBindWarehouseCode(thirdWarehouseEntity.getCode());
            dto.setBindWarehouseName(thirdWarehouseEntity.getName());
        }
        return dto;
    }

    private List<WarehouseLocationMappingExcelDTO> processImportRows(List<WarehouseLocationMappingExcelDTO> rowList) {
        if (CollectionUtils.isEmpty(rowList)) {
            return Collections.emptyList();
        }
        List<WarehouseLocationMappingExcelDTO> dataList = rowList.stream().filter(this::notBlankRow).collect(Collectors.toList());

        List<Candidate> candidates = new ArrayList<>();
        Map<Candidate, List<String>> errorMap = new LinkedHashMap<>();
        for (WarehouseLocationMappingExcelDTO row : dataList) {
            Candidate candidate = buildImportCandidate(row);
            if (CharSequenceUtil.isNotBlank(row.getErrorMsg())) {
                continue;
            }
            candidates.add(candidate);
            errorMap.put(candidate, new ArrayList<>());
        }
        validateDuplicate(candidates, null, errorMap);

        List<WarehouseLocationMappingExcelDTO> errorList = new ArrayList<>();
        for (Map.Entry<Candidate, List<String>> entry : errorMap.entrySet()) {
            if (CollectionUtils.isNotEmpty(entry.getValue())) {
                WarehouseLocationMappingExcelDTO row = entry.getKey().getImportRow();
                row.setErrorMsg(String.join("；", entry.getValue()));
            }
        }
        for (WarehouseLocationMappingExcelDTO row : dataList) {
            if (CharSequenceUtil.isNotBlank(row.getErrorMsg())) {
                errorList.add(row);
            }
        }
        List<WarehouseLocationMappingEntity> addList = candidates.stream()
                .filter(candidate -> CharSequenceUtil.isBlank(candidate.getImportRow().getErrorMsg()))
                .map(Candidate::getEntity)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
            LoginUser user = UserContext.getNonLoginUser();
            addList.forEach(entity -> operateLogService.addModuleOperateLog(buildLogContent("导入", entity),
                    ModuleTypeEnum.WAREHOUSE_LOCATION_MAPPING.getCode(), entity.getId(), "导入操作", user.getUid(), user.getUserName()));
        }
        return errorList;
    }

    private void setImportUser(String userId) {
        if (CharSequenceUtil.isBlank(userId)) {
            return;
        }
        FindUserDTO findUserDTO = sysUserFeign.getUserList().stream()
                .filter(user -> Objects.equals(user.getUserId(), userId))
                .findFirst()
                .orElse(null);
        if (findUserDTO == null) {
            return;
        }
        LoginUser user = new LoginUser();
        user.setUid(findUserDTO.getUserId());
        user.setUserName(findUserDTO.getUserName());
        user.setRealName(findUserDTO.getRealName());
        user.setUserAccount(findUserDTO.getMobile());
        user.setMobile(findUserDTO.getMobile());
        UserContext.setLoginUser(user);
    }

    private LambdaQueryWrapper<WarehouseLocationMappingEntity> buildQueryWrapper(WarehouseLocationMappingDTO.SearchDTO params) {
        LambdaQueryWrapper<WarehouseLocationMappingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CharSequenceUtil.isNotBlank(params.getSysWarehouseId()), WarehouseLocationMappingEntity::getSysWarehouseId, params.getSysWarehouseId())
                .eq(CharSequenceUtil.isNotBlank(params.getSysWarehouseLocation()), WarehouseLocationMappingEntity::getSysWarehouseLocation, params.getSysWarehouseLocation())
                .eq(CharSequenceUtil.isNotBlank(params.getDictPlatform()), WarehouseLocationMappingEntity::getDictPlatform, params.getDictPlatform())
                .like(CharSequenceUtil.isNotBlank(params.getThirdWarehouseLocation()), WarehouseLocationMappingEntity::getThirdWarehouseLocation, params.getThirdWarehouseLocation());
        if (params.getSqlMap() != null && CharSequenceUtil.isNotBlank(params.getSqlMap().get("default"))) {
            wrapper.apply(params.getSqlMap().get("default"));
        }
        return wrapper;
    }

    private List<WarehouseLocationMappingDTO.ViewDTO> buildViewList(List<WarehouseLocationMappingEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<String> warehouseIds = records.stream().map(WarehouseLocationMappingEntity::getSysWarehouseId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toSet());
        Map<String, WarehouseEntity> warehouseMap = warehouseIds.isEmpty() ? Collections.emptyMap() : warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(WarehouseEntity::getId, Function.identity(), (v1, v2) -> v1));
        List<String> locationCodes = records.stream().map(WarehouseLocationMappingEntity::getSysWarehouseLocation).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, WarehouseLocationEntity> locationMap = warehouseIds.isEmpty() || locationCodes.isEmpty() ? Collections.emptyMap()
                : warehouseLocationService.listByWarehouseIdsAndCodeList(new ArrayList<>(warehouseIds), locationCodes).stream()
                .collect(Collectors.toMap(item -> buildKey(item.getWarehouseId(), item.getCode()), Function.identity(), (v1, v2) -> v1));
        Map<String, ThirdWarehouseEntity> bindWarehouseMap = buildBindWarehouseMap(records);

        List<WarehouseLocationMappingDTO.ViewDTO> resultList = new ArrayList<>(records.size());
        for (WarehouseLocationMappingEntity entity : records) {
            WarehouseLocationMappingDTO.ViewDTO viewDTO = new WarehouseLocationMappingDTO.ViewDTO();
            viewDTO.setId(entity.getId());
            viewDTO.setSysWarehouseId(entity.getSysWarehouseId());
            viewDTO.setSysWarehouseCode(entity.getSysWarehouseCode());
            viewDTO.setSysWarehouseLocation(entity.getSysWarehouseLocation());
            viewDTO.setDictPlatform(entity.getDictPlatform());
            viewDTO.setDictPlatformName(getPlatformName(entity.getDictPlatform()));
            ThirdWarehouseEntity bindWarehouse = bindWarehouseMap.get(buildKey(entity.getSysWarehouseId(), entity.getDictPlatform()));
            if (bindWarehouse != null) {
                viewDTO.setBindWarehouseId(bindWarehouse.getId());
                viewDTO.setBindWarehouseCode(bindWarehouse.getCode());
                viewDTO.setBindWarehouseName(bindWarehouse.getName());
            }
            viewDTO.setThirdWarehouseLocation(entity.getThirdWarehouseLocation());
            viewDTO.setCreateUserName(entity.getCreateUserName());
            viewDTO.setCreateTime(entity.getCreateTime());
            viewDTO.setUpdateUserName(entity.getUpdateUserName());
            viewDTO.setUpdateTime(entity.getUpdateTime());
            WarehouseEntity warehouse = warehouseMap.get(entity.getSysWarehouseId());
            if (warehouse != null) {
                viewDTO.setSysWarehouseName(warehouse.getName());
            }
            WarehouseLocationEntity location = locationMap.get(buildKey(entity.getSysWarehouseId(), entity.getSysWarehouseLocation()));
            if (location != null) {
                viewDTO.setSysWarehouseLocationName(location.getName());
            }
            resultList.add(viewDTO);
        }
        return resultList;
    }

    private Map<String, ThirdWarehouseEntity> buildBindWarehouseMap(List<WarehouseLocationMappingEntity> records) {
        Map<String, ThirdWarehouseEntity> bindWarehouseMap = new HashMap<>();
        records.stream()
                .filter(entity -> CharSequenceUtil.isNotBlank(entity.getSysWarehouseId()) && CharSequenceUtil.isNotBlank(entity.getDictPlatform()))
                .map(entity -> buildKey(entity.getSysWarehouseId(), entity.getDictPlatform()))
                .distinct()
                .forEach(key -> {
                    String[] split = key.split("#", 2);
                    ThirdWarehouseEntity thirdWarehouse = dmpThirdMappingFeign.getBySysId(split[0], split[1]);
                    if (thirdWarehouse != null) {
                        bindWarehouseMap.put(key, thirdWarehouse);
                    }
                });
        return bindWarehouseMap;
    }

    private List<Candidate> buildCandidates(WarehouseLocationMappingDTO.AddDTO dto) {
        String platform = validatePlatform(dto.getDictPlatform());
        WarehouseEntity warehouse = validateWarehouse(dto.getSysWarehouseId());
        List<Candidate> candidates = new ArrayList<>(dto.getDetailList().size());
        for (WarehouseLocationMappingDTO.DetailDTO detailDTO : dto.getDetailList()) {
            WarehouseLocationEntity location = validateLocation(warehouse.getId(), detailDTO.getSysWarehouseLocation());
            WarehouseLocationMappingEntity entity = new WarehouseLocationMappingEntity();
            entity.setDictPlatform(platform);
            entity.setSysWarehouseId(warehouse.getId());
            entity.setSysWarehouseCode(warehouse.getKingdeeWarehouseCode());
            entity.setSysWarehouseLocation(location.getCode());
            entity.setThirdWarehouseLocation(detailDTO.getThirdWarehouseLocation());
            candidates.add(new Candidate(entity, null));
        }
        validateRequired(candidates);
        return candidates;
    }

    private Candidate buildImportCandidate(WarehouseLocationMappingExcelDTO row) {
        List<String> errorList = new ArrayList<>();
        if (CharSequenceUtil.isBlank(row.getSysWarehouseName())) {
            errorList.add("仓库名称不能为空");
        }
        String platform = validateImportPlatform(row.getDictPlatformName(), errorList);
        WarehouseEntity warehouse = validateImportWarehouse(row.getSysWarehouseName(), errorList);
        WarehouseLocationEntity location = warehouse == null ? null : validateImportLocation(warehouse.getId(), row.getSysWarehouseLocation(), errorList);
        if (CharSequenceUtil.isBlank(row.getThirdWarehouseLocation())) {
            errorList.add("绑定仓位编码不能为空");
        } else if (row.getThirdWarehouseLocation().length() > 50) {
            errorList.add("绑定仓位编码最大长度不能超过50位");
        }
        if (CollectionUtils.isNotEmpty(errorList)) {
            row.setErrorMsg(String.join("；", errorList));
            return new Candidate(new WarehouseLocationMappingEntity(), row);
        }
        if (warehouse == null || location == null) {
            row.setErrorMsg("导入数据校验失败");
            return new Candidate(new WarehouseLocationMappingEntity(), row);
        }
        WarehouseLocationMappingEntity entity = new WarehouseLocationMappingEntity();
        entity.setDictPlatform(platform);
        entity.setSysWarehouseId(warehouse.getId());
        entity.setSysWarehouseCode(warehouse.getKingdeeWarehouseCode());
        entity.setSysWarehouseLocation(location.getCode());
        entity.setThirdWarehouseLocation(row.getThirdWarehouseLocation());
        return new Candidate(entity, row);
    }

    private void validateRequired(List<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            WarehouseLocationMappingEntity entity = candidate.getEntity();
            if (CharSequenceUtil.isBlank(entity.getThirdWarehouseLocation())) {
                throw new ServiceException("绑定仓位编码不能为空");
            }
            if (entity.getThirdWarehouseLocation().length() > 50) {
                throw new ServiceException("绑定仓位编码最大长度不能超过50位");
            }
        }
    }

    private String validatePlatform(String dictPlatform) {
        String platform = parsePlatform(dictPlatform);
        if (platform == null) {
            throw new ServiceException("第三方系统仅支持领星、旺店通");
        }
        return platform;
    }

    private String validateImportPlatform(String dictPlatform, List<String> errorList) {
        if (CharSequenceUtil.isBlank(dictPlatform)) {
            errorList.add("第三方系统不能为空");
            return null;
        }
        String platform = parsePlatform(dictPlatform);
        if (platform == null) {
            errorList.add("第三方系统仅支持领星、旺店通");
        }
        return platform;
    }

    private String parsePlatform(String dictPlatform) {
        if (CharSequenceUtil.isBlank(dictPlatform)) {
            return null;
        }
        String value = dictPlatform.trim();
        if (PlatformDictEnum.WDT.getCode().equalsIgnoreCase(value) || PlatformDictEnum.WDT.getName().equals(value)) {
            return PlatformDictEnum.WDT.getCode();
        }
        if (PlatformDictEnum.LING_XING.getCode().equalsIgnoreCase(value) || PlatformDictEnum.LING_XING.getName().equals(value)) {
            return PlatformDictEnum.LING_XING.getCode();
        }
        return null;
    }

    private WarehouseEntity validateWarehouse(String warehouseId) {
        WarehouseEntity warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null || Boolean.TRUE.equals(warehouse.getIsDeleted())) {
            throw new ServiceException("仓库不存在");
        }
        if (Boolean.TRUE.equals(warehouse.getDisabled())) {
            throw new ServiceException("仓库被禁用");
        }
        if (!ApproveStatusEnum.APPROVE.equals(warehouse.getApproveStatus())) {
            throw new ServiceException("仓库未审核");
        }
        return warehouse;
    }

    private WarehouseEntity validateImportWarehouse(String warehouseName, List<String> errorList) {
        if (CharSequenceUtil.isBlank(warehouseName)) {
            return null;
        }
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(Collections.singletonList(warehouseName));
        if (CollectionUtils.isEmpty(warehouseList)) {
            errorList.add("仓库不存在");
            return null;
        }
        WarehouseEntity warehouse = warehouseMapper.selectById(warehouseList.get(0).getId());
        if (warehouse == null || Boolean.TRUE.equals(warehouse.getIsDeleted())) {
            errorList.add("仓库不存在");
            return null;
        }
        if (Boolean.TRUE.equals(warehouse.getDisabled())) {
            errorList.add("仓库被禁用");
        }
        if (!ApproveStatusEnum.APPROVE.equals(warehouse.getApproveStatus())) {
            errorList.add("仓库未审核");
        }
        return warehouse;
    }

    private WarehouseLocationEntity validateLocation(String warehouseId, String sysWarehouseLocation) {
        if (CharSequenceUtil.isBlank(sysWarehouseLocation)) {
            throw new ServiceException("仓位编码不能为空");
        }
        WarehouseLocationEntity location = warehouseLocationService.findByWarehouseIdAndCode(warehouseId, sysWarehouseLocation);
        if (location == null || Boolean.TRUE.equals(location.getIsDeleted())) {
            throw new ServiceException(CharSequenceUtil.format("仓位【{}】不存在", sysWarehouseLocation));
        }
        return location;
    }

    private WarehouseLocationEntity validateImportLocation(String warehouseId, String sysWarehouseLocation, List<String> errorList) {
        if (CharSequenceUtil.isBlank(sysWarehouseLocation)) {
            errorList.add("仓位编码不能为空");
            return null;
        }
        WarehouseLocationEntity location = warehouseLocationService.findByWarehouseIdAndCode(warehouseId, sysWarehouseLocation);
        if (location == null || Boolean.TRUE.equals(location.getIsDeleted())) {
            errorList.add(CharSequenceUtil.format("仓位【{}】不存在", sysWarehouseLocation));
        }
        return location;
    }

    private void validateDuplicate(List<Candidate> candidates, String excludeId, Map<Candidate, List<String>> errorMap) {
        if (CollectionUtils.isEmpty(candidates)) {
            return;
        }
        List<WarehouseLocationMappingEntity> existList = listExisting(candidates, excludeId);
        for (Candidate candidate : candidates) {
            errorMap.computeIfAbsent(candidate, key -> new ArrayList<>());
            validateDuplicateWithList(candidate, candidates, errorMap.get(candidate), true);
            validateDuplicateWithList(candidate, wrapExisting(existList), errorMap.get(candidate), false);
        }
    }

    private List<WarehouseLocationMappingEntity> listExisting(List<Candidate> candidates, String excludeId) {
        Set<String> warehouseIds = candidates.stream().map(item -> item.getEntity().getSysWarehouseId()).collect(Collectors.toSet());
        Set<String> platforms = candidates.stream().map(item -> item.getEntity().getDictPlatform()).collect(Collectors.toSet());
        return this.lambdaQuery()
                .in(WarehouseLocationMappingEntity::getSysWarehouseId, warehouseIds)
                .in(WarehouseLocationMappingEntity::getDictPlatform, platforms)
                .ne(CharSequenceUtil.isNotBlank(excludeId), WarehouseLocationMappingEntity::getId, excludeId)
                .list();
    }

    private List<Candidate> wrapExisting(List<WarehouseLocationMappingEntity> existList) {
        if (CollectionUtils.isEmpty(existList)) {
            return Collections.emptyList();
        }
        return existList.stream().map(entity -> new Candidate(entity, null)).collect(Collectors.toList());
    }

    private void validateDuplicateWithList(Candidate current, List<Candidate> candidateList, List<String> errorList, boolean sameBatch) {
        WarehouseLocationMappingEntity currentEntity = current.getEntity();
        Set<String> messageSet = new HashSet<>(errorList);
        for (Candidate candidate : candidateList) {
            WarehouseLocationMappingEntity other = candidate.getEntity();
            if (sameBatch && current == candidate) {
                continue;
            }
            if (!sameSysWarehouseAndPlatform(currentEntity, other)) {
                continue;
            }
            boolean sameSysLocation = Objects.equals(currentEntity.getSysWarehouseLocation(), other.getSysWarehouseLocation());
            boolean sameThirdLocation = Objects.equals(currentEntity.getThirdWarehouseLocation(), other.getThirdWarehouseLocation());
            if (sameSysLocation && sameThirdLocation) {
                addMessage(errorList, messageSet, CharSequenceUtil.format("仓位【{}】不允许同时绑定相同第三方系统【{}】相同仓位【{}】",
                        currentEntity.getSysWarehouseLocation(), getPlatformName(currentEntity.getDictPlatform()), currentEntity.getThirdWarehouseLocation()));
            } else if (!sameSysLocation && sameThirdLocation) {
                addMessage(errorList, messageSet, CharSequenceUtil.format("仓位【{}】已绑定第三方系统【{}】仓位【{}】，请绑定其他仓位",
                        currentEntity.getThirdWarehouseLocation(), getPlatformName(currentEntity.getDictPlatform()), currentEntity.getThirdWarehouseLocation()));
            } else if (sameSysLocation) {
                addMessage(errorList, messageSet, CharSequenceUtil.format("仓位【{}】已绑定第三方系统【{}】仓位【{}】，请解绑后再绑定",
                        currentEntity.getSysWarehouseLocation(), getPlatformName(currentEntity.getDictPlatform()), other.getThirdWarehouseLocation()));
            }
        }
    }

    private boolean sameSysWarehouseAndPlatform(WarehouseLocationMappingEntity current, WarehouseLocationMappingEntity other) {
        return Objects.equals(current.getSysWarehouseId(), other.getSysWarehouseId())
                && Objects.equals(current.getDictPlatform(), other.getDictPlatform());
    }

    private void addMessage(List<String> errorList, Set<String> messageSet, String message) {
        if (messageSet.add(message)) {
            errorList.add(message);
        }
    }

    private void throwIfError(Map<Candidate, List<String>> errorMap) {
        List<String> errorList = errorMap.values().stream().flatMap(List::stream).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(errorList)) {
            throw new ServiceException(String.join("；", errorList));
        }
    }

    private boolean notBlankRow(WarehouseLocationMappingExcelDTO row) {
        return CharSequenceUtil.isNotBlank(row.getSysWarehouseName())
                || CharSequenceUtil.isNotBlank(row.getDictPlatformName())
                || CharSequenceUtil.isNotBlank(row.getSysWarehouseLocation())
                || CharSequenceUtil.isNotBlank(row.getThirdWarehouseLocation());
    }

    private String buildLogContent(String operate, WarehouseLocationMappingEntity entity) {
        return CharSequenceUtil.format("{}仓位绑定：仓位【{}】第三方系统【{}】仓位【{}】",
                operate, entity.getSysWarehouseLocation(), getPlatformName(entity.getDictPlatform()), entity.getThirdWarehouseLocation());
    }

    private String getPlatformName(String dictPlatform) {
        PlatformDictEnum platform = PlatformDictEnum.getByCode(dictPlatform);
        return platform == null ? dictPlatform : platform.getName();
    }

    private String buildKey(String first, String second) {
        return (first == null ? "" : first).concat("#").concat(second == null ? "" : second);
    }

    private static class Candidate {
        private final WarehouseLocationMappingEntity entity;
        private final WarehouseLocationMappingExcelDTO importRow;

        private Candidate(WarehouseLocationMappingEntity entity, WarehouseLocationMappingExcelDTO importRow) {
            this.entity = entity;
            this.importRow = importRow;
        }

        public WarehouseLocationMappingEntity getEntity() {
            return entity;
        }

        public WarehouseLocationMappingExcelDTO getImportRow() {
            return importRow;
        }
    }
}
