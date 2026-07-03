package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.entity.DmpBasicSystemEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.dmp.dto.CfgFileParseDTO;
import com.erp.model.dmp.dto.CfgFileParseFileDTO;
import com.erp.model.dmp.dto.CfgFileParseFolderDTO;
import com.erp.model.dmp.entity.CfgFileParseEntity;
import com.erp.model.dmp.entity.CfgFileParseFileEntity;
import com.erp.model.dmp.entity.CfgFileParseFolderEntity;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.dmp.enums.CfgFileParseFileTypeEnum;
import com.erp.model.dmp.enums.CfgFileParseFolderAccountTypeEnum;
import com.erp.model.dmp.enums.CfgFileParseFolderTypeEnum;
import com.erp.model.dmp.enums.CfgFileParsePeriodTypeEnum;
import com.erp.rpc.dmp.feign.DmpBasicSystemFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.mapper.CfgFileParseMapper;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.service.CfgFileParseFileService;
import com.erp.server.dmp.service.CfgFileParseFolderService;
import com.erp.server.dmp.service.CfgFileParseService;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.ThirdWarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 月结文件解析配置服务实现。
 *
 * @author jack
 * @since 2026-06-29
 */
@Slf4j
@Service
public class CfgFileParseServiceImpl extends SuperServiceImpl<CfgFileParseMapper, CfgFileParseEntity> implements CfgFileParseService {
    private static final String CFG_FILE_PARSE_FILE_BUSINESS_TYPE = "cfgFileParseFileBusinessType";

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private CfgFileParseFolderService cfgFileParseFolderService;
    @Resource
    private CfgFileParseFileService cfgFileParseFileService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private DmpBasicSystemFeign dmpBasicSystemFeign;
    @Resource
    private CommonService commonService;
    @Resource
    private ThirdWarehouseService thirdWarehouseService;
    @Resource
    private DictBasicService dictBasicService;

    /**
     * 新增月结文件解析配置。
     * @param dto 新增参数
     * @return 新增结果
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgFileParseDTO.AddDTO dto) {
        // 数据处理
        normalizeAndValidate(dto, null);
        CfgFileParseEntity entity = new CfgFileParseEntity();
        BeanMapperUtils.copy(dto, entity);
        log.info("开始新增月结文件解析配置");
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WJPZ);
        entity.setCode(code);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException("月结文件解析配置保存失败");
        }
        saveChildren(entity.getId(), dto.getFolderList(), dto.getFileList());
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", getUserName(), "月结文件解析配置", entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_FILE_PARSE.getCode(), entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), code);
    }

    /**
     * 修改月结文件解析配置。
     * @param dto 修改参数
     * @return 是否成功
     */
    @DistributeLocker(keyName = "dto.getId()")
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgFileParseDTO.UpdateDTO dto) {
        CfgFileParseEntity old = getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "月结文件解析配置"));
        CfgFileParseEntity entity = BeanMapperUtils.map(CfgFileParseEntity.class, dto);

        // 数据处理
        normalizeAndValidate(dto, dto.getId());
        BeanMapperUtils.copy(dto, entity);
        entity.setCode(old.getCode());
        log.info("编辑 开始修改月结文件解析配置数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException("月结文件解析配置保存失败");
        }
        updateChildren(entity.getId(), dto.getFolderList(), dto.getFileList());
        log.info("编辑 开始记录月结文件解析配置日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", getUserName(), old.getCode(), "月结文件解析配置");
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.CFG_FILE_PARSE.getCode(), entity.getId(),
                msg);
        return Boolean.TRUE;
    }

    /**
     * 分页查询月结文件解析配置。
     * @param pagingParamDTO 分页参数
     * @return 分页数据
     */
    @Override
    public PagingVO<CfgFileParseDTO.ListDTO> paging(PagingDTO<CfgFileParseDTO.PagingParamDTO> pagingParamDTO) {
        if (pagingParamDTO.getParams() == null) {
            pagingParamDTO.setParams(new CfgFileParseDTO.PagingParamDTO());
        }
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<CfgFileParseDTO.ListDTO> page = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgFileParseDTO.ListDTO> pageData = this.baseMapper.paging(page, pagingParamDTO.getParams());
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 查询启用状态页签数量。
     * @param param 权限参数
     * @return 页签数量
     */
    @Override
    public List<CfgFileParseDTO.TabListDTO> tabList(PermissionsDTO param) {
        CfgFileParseDTO.PagingParamDTO searchParam = new CfgFileParseDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CfgFileParseDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<CfgFileParseDTO.TabListDTO> result = new ArrayList<>();
        result.add(new CfgFileParseDTO.TabListDTO("all","全部", 0));
        result.add(list.stream().filter(e -> e.getTabFlag().equals("enabled")).findFirst().orElse(new CfgFileParseDTO.TabListDTO("enabled","启用", 0)));
        result.add(list.stream().filter(e -> e.getTabFlag().equals("disabled")).findFirst().orElse(new CfgFileParseDTO.TabListDTO("disabled","停用", 0)));
        return result;
    }

    /**
     * 创建异步导出任务。
     * @param dto 导出参数
     * @return 是否创建成功
     */
    @Override
    public Boolean exportList(CfgFileParseDTO.ExportDTO dto) {
        try {
            downloadTaskFeign.saveDownloadTask("月结文件解析配置导出", FileTaskEventEnum.EXPORT_DMP_CFG_FILE_PARSE.getCode(), dto);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("创建月结文件解析配置导出任务失败", e);
            throw new ServiceException(ApiError.CFG_FILE_PARSE_EXPORT_TASK_CREATE_FAILED);
        }
    }

    /**
     * 分页查询月结文件解析配置导出数据。
     * @param pagingParamDTO 导出分页参数
     * @return 导出分页数据
     */
    @Override
    public PagingVO<CfgFileParseDTO.ListDTO> exportPaging(PagingDTO<CfgFileParseDTO.ExportDTO> pagingParamDTO) {
        if (pagingParamDTO.getParams() == null) {
            pagingParamDTO.setParams(new CfgFileParseDTO.ExportDTO());
        }
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<CfgFileParseDTO.ListDTO> page = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgFileParseDTO.ListDTO> pageData = this.baseMapper.exportPaging(page, pagingParamDTO.getParams());
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 删除单条月结文件解析配置。
     * @param id 配置 ID
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgFileParseEntity entity = getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "月结文件解析配置"));
        if (hasDownstreamTask(entity)) {
            throw new ServiceException(ApiError.CFG_FILE_PARSE_DOWNSTREAM_EXISTS, entity.getCode());
        }
        cfgFileParseFolderService.lambdaUpdate().eq(CfgFileParseFolderEntity::getMainId, id).remove();
        cfgFileParseFileService.lambdaUpdate().eq(CfgFileParseFileEntity::getMainId, id).remove();
        removeById(id);
        addLog(entity, OperationTypeEnum.DELETE, "删除月结文件解析配置");
        return BatchResultDTO.success(id, entity.getCode(), "删除成功");
    }

    /**
     * 更新单条启用状态。
     * @param id 配置 ID
     * @param disabled 是否停用
     * @return 更新结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateDisabled(String id, Boolean disabled) {
        CfgFileParseEntity entity = getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "月结文件解析配置"));
        CfgFileParseEntity update = new CfgFileParseEntity();
        update.setId(id);
        update.setDisabled(Boolean.TRUE.equals(disabled));
        updateById(update);
        addLog(entity, OperationTypeEnum.DISABLED, Boolean.TRUE.equals(disabled) ? "停用月结文件解析配置" : "启用月结文件解析配置");
        return BatchResultDTO.success(id, entity.getCode(), Boolean.TRUE.equals(disabled) ? "停用成功" : "启用成功");
    }

    /**
     * 查询详情。
     * @param id 配置 ID
     * @return 配置详情
     */
    @Override
    public CfgFileParseDTO.ViewDTO view(String id) {
        CfgFileParseEntity entity = getByIdOpt(id).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "月结文件解析配置"));
        CfgFileParseDTO.ViewDTO data = new CfgFileParseDTO.ViewDTO();
        BeanMapperUtils.copy(entity, data);
        fillOne(data);
        data.setFolderList(queryFolderList(id));
        data.setFileList(queryFileList(id));
        return data;
    }


    /**
     * 保存子表数据，采用先删后增保持页面提交结果与数据库一致。
     * @param mainId 主表 ID
     * @param folderList 配置文件夹
     * @param fileList 文件清洗规则
     */
    private void saveChildren(String mainId, List<CfgFileParseFolderDTO.UpdateDTO> folderList, List<CfgFileParseFileDTO.UpdateDTO> fileList) {
        List<CfgFileParseFolderEntity> folderEntityList = BeanMapper.copyList(folderList, CfgFileParseFolderEntity.class);
        folderEntityList.forEach(item -> {
            item.setMainId(mainId);
        });
        cfgFileParseFolderService.saveBatch(folderEntityList);

        List<CfgFileParseFileEntity> fileEntityList = BeanMapper.copyList(fileList, CfgFileParseFileEntity.class);
        fileEntityList.forEach(item -> {
            item.setMainId(mainId);
        });
        cfgFileParseFileService.saveBatch(fileEntityList);
    }

    /**
     * 更新子表数据，按项目规范记录明细增删改和字段级日志。
     *
     * @param mainId 主表 ID
     * @param folderList 配置文件夹
     * @param fileList 文件清洗规则
     */
    private void updateChildren(String mainId, List<CfgFileParseFolderDTO.UpdateDTO> folderList, List<CfgFileParseFileDTO.UpdateDTO> fileList) {
        updateFolderList(mainId, folderList);
        updateFileList(mainId, fileList);
    }

    /**
     * 更新配置文件夹明细，并记录字段级操作日志。
     *
     * @param mainId 主表 ID
     * @param folderList 配置文件夹
     */
    private void updateFolderList(String mainId, List<CfgFileParseFolderDTO.UpdateDTO> folderList) {
        List<CfgFileParseFolderEntity> newFolderList = BeanMapper.copyList(folderList, CfgFileParseFolderEntity.class);
        newFolderList.forEach(item -> item.setMainId(mainId));
        List<CfgFileParseFolderEntity> oldFolderList = cfgFileParseFolderService.lambdaQuery()
                .eq(CfgFileParseFolderEntity::getMainId, mainId)
                .list();
        commonService.updateDetail(mainId, ModuleTypeEnum.CFG_FILE_PARSE.getCode(), cfgFileParseFolderService, newFolderList, oldFolderList,
                Arrays.asList("accountType", "accountCode", "accountName"));
    }

    /**
     * 更新文件清洗规则明细，并记录字段级操作日志。
     *
     * @param mainId 主表 ID
     * @param fileList 文件清洗规则
     */
    private void updateFileList(String mainId, List<CfgFileParseFileDTO.UpdateDTO> fileList) {
        List<CfgFileParseFileEntity> newFileList = BeanMapper.copyList(fileList, CfgFileParseFileEntity.class);
        newFileList.forEach(item -> item.setMainId(mainId));
        List<CfgFileParseFileEntity> oldFileList = cfgFileParseFileService.lambdaQuery()
                .eq(CfgFileParseFileEntity::getMainId, mainId)
                .list();
        commonService.updateDetail(mainId, ModuleTypeEnum.CFG_FILE_PARSE.getCode(), cfgFileParseFileService, newFileList, oldFileList,
                Arrays.asList("businessType", "type", "fileKeyword", "sheetName", "headerRow"));
    }

    /**
     * 标准化并校验主子表数据。
     * @param dto 入参
     * @param excludeId 编辑时排除的当前 ID
     */
    private void normalizeAndValidate(CfgFileParseDTO.CommonDTO dto, String excludeId) {
        if (StringUtils.isBlank(dto.getPeriodType())) {
            dto.setPeriodType(CfgFileParsePeriodTypeEnum.MONTHLY.getCode());
        }
        if (!CfgFileParsePeriodTypeEnum.MONTHLY.getCode().equals(dto.getPeriodType())) {
            throw new ServiceException(ApiError.CFG_FILE_PARSE_NOT_MONTHLY);
        }
        fillPlatformName(dto);
        fillFolderAccountInfo(dto);
        validateDuplicate(dto, excludeId);
        validateFolderList(dto.getFolderList());
        validateFileList(dto.getFileList());
    }

    /**
     * 根据 DMP 基础系统 ID 补齐平台名称，避免前端传入的展示名称与编码不一致。
     * @param dto 入参
     */
    private void fillPlatformName(CfgFileParseDTO.CommonDTO dto) {
        DmpBasicSystemEntity dmpBasicSystem = dmpBasicSystemFeign.getById(dto.getDictPlatform());
        dto.setDictPlatformName(Objects.isNull(dmpBasicSystem) ? dto.getDictPlatform() : dmpBasicSystem.getName());
    }

    /**
     * 根据账号 ID 补齐文件夹行的账号编码和账号名称。
     * @param dto 入参
     */
    private void fillFolderAccountInfo(CfgFileParseDTO.CommonDTO dto) {
        List<CfgFileParseFolderDTO.UpdateDTO> folderList = dto.getFolderList();
        if (CollUtil.isEmpty(folderList)) {
            return;
        }
        Map<String, ShopInfoEntity> shopMap = buildShopMap(folderList);
        Map<String, ThirdWarehouseEntity> thirdWarehouseMap = buildThirdWarehouseMap(folderList);
        for (CfgFileParseFolderDTO.UpdateDTO item : folderList) {
            if (CfgFileParseFolderAccountTypeEnum.PLATFORM_SHOP.getCode().equals(item.getAccountType())) {
                fillShopAccountInfo(item, shopMap);
            } else if (CfgFileParseFolderAccountTypeEnum.PLATFORM_THIRD_WAREHOUSE.getCode().equals(item.getAccountType())) {
                fillThirdWarehouseAccountInfo(item, thirdWarehouseMap);
            }
        }
    }

    /**
     * 批量查询店铺信息，用于补齐店铺账号编码和名称。
     * @param folderList 文件夹配置行
     * @return 店铺信息 Map
     */
    private Map<String, ShopInfoEntity> buildShopMap(List<CfgFileParseFolderDTO.UpdateDTO> folderList) {
        List<String> shopIds = folderList.stream()
                .filter(item -> CfgFileParseFolderAccountTypeEnum.PLATFORM_SHOP.getCode().equals(item.getAccountType()))
                .map(CfgFileParseFolderDTO.UpdateDTO::getAccountId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        List<ShopInfoEntity> shopList = shopInfoFeign.listShopInfoByIds(shopIds);
        if (CollUtil.isEmpty(shopList)) {
            return Collections.emptyMap();
        }
        return shopList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity(), (first, second) -> first));
    }

    /**
     * 使用店铺主数据补齐账号编码和名称。
     * @param item 文件夹配置行
     * @param shopMap 店铺信息 Map
     */
    private void fillShopAccountInfo(CfgFileParseFolderDTO.UpdateDTO item, Map<String, ShopInfoEntity> shopMap) {
        ShopInfoEntity shopInfo = shopMap.get(item.getAccountId());
        if (Objects.isNull(shopInfo)) {
            item.setAccountCode(null);
            item.setAccountName(null);
            return;
        }
        item.setAccountCode(shopInfo.getAccount());
        item.setAccountName(shopInfo.getName());
    }

    /**
     * 批量查询三方仓账号信息，用于补齐三方仓账号编码和名称。
     * @param folderList 文件夹配置行
     * @return 三方仓账号信息 Map
     */
    private Map<String, ThirdWarehouseEntity> buildThirdWarehouseMap(List<CfgFileParseFolderDTO.UpdateDTO> folderList) {
        List<String> thirdWarehouseIds = folderList.stream()
                .filter(item -> CfgFileParseFolderAccountTypeEnum.PLATFORM_THIRD_WAREHOUSE.getCode().equals(item.getAccountType()))
                .map(CfgFileParseFolderDTO.UpdateDTO::getAccountId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(thirdWarehouseIds)) {
            return Collections.emptyMap();
        }
        List<ThirdWarehouseEntity> thirdWarehouseList = thirdWarehouseService.listByIds(thirdWarehouseIds);
        if (CollUtil.isEmpty(thirdWarehouseList)) {
            return Collections.emptyMap();
        }
        return thirdWarehouseList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(ThirdWarehouseEntity::getId, Function.identity(), (first, second) -> first));
    }

    /**
     * 使用三方仓主数据补齐账号编码和名称。
     * @param item 文件夹配置行
     * @param thirdWarehouseMap 三方仓账号信息 Map
     */
    private void fillThirdWarehouseAccountInfo(CfgFileParseFolderDTO.UpdateDTO item, Map<String, ThirdWarehouseEntity> thirdWarehouseMap) {
        ThirdWarehouseEntity thirdWarehouse = thirdWarehouseMap.get(item.getAccountId());
        if (Objects.isNull(thirdWarehouse)) {
            item.setAccountCode(null);
            item.setAccountName(null);
            return;
        }
        item.setAccountCode(thirdWarehouse.getCode());
        item.setAccountName(thirdWarehouse.getName());
    }

    /**
     * 校验清洗仓库和清洗时间是否重复。
     * @param dto 入参
     * @param excludeId 编辑时排除的当前 ID
     */
    private void validateDuplicate(CfgFileParseDTO.CommonDTO dto, String excludeId) {
        boolean exists = lambdaQuery()
                .eq(CfgFileParseEntity::getDictPlatform, dto.getDictPlatform())
                .eq(CfgFileParseEntity::getPeriodType, CfgFileParsePeriodTypeEnum.MONTHLY.getCode())
                .ne(StringUtils.isNotBlank(excludeId), CfgFileParseEntity::getId, excludeId)
                .count() > 0;
        if (exists) {
            throw new ServiceException(ApiError.CFG_FILE_PARSE_DUPLICATE, dto.getDictPlatformName(), CfgFileParsePeriodTypeEnum.MONTHLY.getName());
        }
    }

    /**
     * 校验配置文件夹行。
     * @param folderList 配置文件夹
     */
    private void validateFolderList(List<CfgFileParseFolderDTO.UpdateDTO> folderList) {
        if (CollUtil.isEmpty(folderList)) {
            throw new ServiceException(ApiError.CFG_FILE_PARSE_FOLDER_REQUIRED);
        }
        for (int i = 0; i < folderList.size(); i++) {
            CfgFileParseFolderDTO.UpdateDTO item = folderList.get(i);
            int rowNum = i + 1;
            if (StringUtils.isBlank(CfgFileParseFolderAccountTypeEnum.getName(item.getAccountType()))) {
                throw new ServiceException(ApiError.CFG_FILE_PARSE_FOLDER_RULE_INVALID, rowNum, "账号类型录入有误");
            }
            if (StringUtils.isAnyBlank(item.getAccountId(), item.getAccountCode(), item.getAccountName())) {
                throw new ServiceException(ApiError.CFG_FILE_PARSE_FOLDER_RULE_INVALID, rowNum, "账号信息不能为空");
            }
            if (item.getSort() == null) {
                item.setSort(rowNum);
            }
        }
    }

    /**
     * 校验文件清洗规则行。
     * @param fileList 文件清洗规则
     */
    private void validateFileList(List<CfgFileParseFileDTO.UpdateDTO> fileList) {
        if (CollUtil.isEmpty(fileList)) {
            throw new ServiceException(ApiError.CFG_FILE_PARSE_FILE_REQUIRED);
        }
        for (int i = 0; i < fileList.size(); i++) {
            CfgFileParseFileDTO.UpdateDTO item = fileList.get(i);
            int rowNum = i + 1;
            if (StringUtils.isBlank(item.getBusinessType())) {
                throw new ServiceException(ApiError.CFG_FILE_PARSE_FILE_RULE_INVALID, rowNum, "单据类型不能为空");
            }
            if (StringUtils.isBlank(CfgFileParseFileTypeEnum.getName(item.getType()))) {
                throw new ServiceException(ApiError.CFG_FILE_PARSE_FILE_RULE_INVALID, rowNum, "数据来源录入有误");
            }
            if (StringUtils.isBlank(item.getFileKeyword())) {
                throw new ServiceException(ApiError.CFG_FILE_PARSE_FILE_RULE_INVALID, rowNum, "识别名称不能为空");
            }
            if (item.getHeaderRow() == null) {
                item.setHeaderRow(CfgFileParseFileTypeEnum.API.getCode().equals(item.getType()) ? 0 : 1);
            }
            if (item.getSort() == null) {
                item.setSort(rowNum);
            }
        }
    }

    /**
     * 判断配置是否已产生下游任务。
     * @param entity 配置主表
     * @return 是否存在下游任务
     */
    private boolean hasDownstreamTask(CfgFileParseEntity entity) {
        // TODO 当前变更未明确下游清洗/解析任务表或服务，定位后只需在此处补充存在性查询。
        return false;
    }

    /**
     * 查询配置文件夹子表。
     * @param mainId 主表 ID
     * @return 配置文件夹
     */
    private List<CfgFileParseFolderDTO.UpdateDTO> queryFolderList(String mainId) {
        List<CfgFileParseFolderEntity> list = cfgFileParseFolderService.lambdaQuery()
                .eq(CfgFileParseFolderEntity::getMainId, mainId)
                .orderByAsc(CfgFileParseFolderEntity::getSort)
                .list();
        return BeanMapper.copyList(list, CfgFileParseFolderDTO.UpdateDTO.class);
    }

    /**
     * 查询文件清洗规则子表。
     * @param mainId 主表 ID
     * @return 文件清洗规则
     */
    private List<CfgFileParseFileDTO.UpdateDTO> queryFileList(String mainId) {
        List<CfgFileParseFileEntity> list = cfgFileParseFileService.lambdaQuery()
                .eq(CfgFileParseFileEntity::getMainId, mainId)
                .orderByAsc(CfgFileParseFileEntity::getSort)
                .list();
        return BeanMapper.copyList(list, CfgFileParseFileDTO.UpdateDTO.class);
    }

    /**
     * 详情字段填充。
     * @param data 详情
     */
    private void fillOne(CfgFileParseDTO.ViewDTO data) {
        if (data == null) {
            return;
        }
        data.setPeriodTypeName(CfgFileParsePeriodTypeEnum.getName(data.getPeriodType()));
        data.setFolderTypeName(CfgFileParseFolderTypeEnum.getName(data.getFolderType()));
    }

    /**
     * 列表字段填充。
     * @param list 列表数据
     */
    private void fillList(List<CfgFileParseDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> businessTypeNameMap = dictBasicService.getByKey(CFG_FILE_PARSE_FILE_BUSINESS_TYPE)
                .stream()
                .filter(item -> StringUtils.isNotBlank(item.getValue()))
                .collect(Collectors.toMap(DictBasicEntity::getValue, DictBasicEntity::getName, (first, second) -> first));
        list.forEach(item -> {
            item.setPeriodTypeName(CfgFileParsePeriodTypeEnum.getName(item.getPeriodType()));
            item.setFolderTypeName(CfgFileParseFolderTypeEnum.getName(item.getFolderType()));
            item.setDisabledName(Boolean.TRUE.equals(item.getDisabled()) ? "停用" : "启用");
            item.setTypeName(CfgFileParseFileTypeEnum.getName(item.getType()));
            item.setBusinessTypeName(businessTypeNameMap.get(item.getBusinessType()));
        });
    }

    /**
     * 记录操作日志。
     * @param entity 配置主表
     * @param operationType 操作类型
     * @param action 操作文案
     */
    private void addLog(CfgFileParseEntity entity, OperationTypeEnum operationType, String action) {
        String msg = StrUtil.format("用户【{}】{}【{}】", getUserName(), action, entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_FILE_PARSE.getCode(), entity.getId(), operationType.getName());
    }

    /**
     * 获取当前登录用户名。
     * @return 用户名
     */
    private String getUserName() {
        return Objects.isNull(UserContext.getDefaultLoginUser()) ? "" : UserContext.getDefaultLoginUser().getUserName();
    }
}
