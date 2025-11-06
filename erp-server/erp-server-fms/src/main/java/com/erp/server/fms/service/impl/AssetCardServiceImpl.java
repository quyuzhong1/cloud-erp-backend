package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.fms.dto.AssetCardDTO;
import com.erp.model.fms.dto.AssetCardDetailDTO;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.erp.model.fms.entity.AssetCardEntity;
import com.erp.model.fms.entity.AssetDisposalDetailEntity;
import com.erp.model.fms.entity.AssetDisposalEntity;
import com.erp.model.fms.entity.AssetStocktakingDetailEntity;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.erp.model.fms.enums.CardSourceEnum;
import com.erp.model.fms.enums.DepreciationChargeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.mapper.AssetCardMapper;
import com.erp.server.fms.service.AssetCardService;
import com.erp.server.fms.service.OperateLogService;
import com.erp.server.fms.listener.AssetCardExcelListener;
import com.erp.model.fms.dto.excel.AssetCardImportExcelDTO;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 资产卡片主表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetCardServiceImpl extends SuperServiceImpl<AssetCardMapper, AssetCardEntity> implements AssetCardService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private com.erp.server.fms.service.AssetCardDetailService assetCardDetailService;
    @Autowired
    private com.erp.server.fms.service.AssetStocktakingService assetStocktakingService;
    @Autowired
    private com.erp.server.fms.service.AssetStocktakingDetailService assetStocktakingDetailService;
    @Autowired
    private com.erp.server.fms.service.AssetDisposalService assetDisposalService;
    @Autowired
    private com.erp.server.fms.service.AssetDisposalDetailService assetDisposalDetailService;
    @Autowired
    private com.erp.server.fms.service.AssetLocationService assetLocationService;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private FileFeign fileFeign;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetCardDTO.AddDTO addDTO) {
        // 验证明细不能为空
        if (CollUtil.isEmpty(addDTO.getDetailList())) {
            throw new ServiceException("资产卡片明细不能为空，至少需要一条明细数据");
        }
        
        AssetCardEntity assetCardEntity = new AssetCardEntity();
        BeanMapperUtils.copy(addDTO, assetCardEntity);

        // 数据处理
        handleData(assetCardEntity);

        log.info("开始新增资产卡片主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZCKP);
        assetCardEntity.setCode(code);
        boolean save = super.save(assetCardEntity);
        if(!save) {
            throw new ServiceException("资产卡片主单保存失败");
        }

        // 新增明细
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            List<AssetCardDetailEntity> detailEntities = new ArrayList<>();
            for (AssetCardDetailDTO.AddDTO detailDTO : addDTO.getDetailList()) {
                AssetCardDetailEntity detailEntity = new AssetCardDetailEntity();
                detailEntity.setMainId(assetCardEntity.getId());
                detailEntity.setSourceDetailId(detailDTO.getSourceDetailId());
                
                // 资产编码：如果DTO中已有编码则使用，否则自动生成
                String assetCode = detailDTO.getAssetCode();
                if (StringUtils.isBlank(assetCode)) {
                    // 自动生成资产编码：ZC + 年月日(YYMMDD) + 6位流水号
                    assetCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZC);
                }
                detailEntity.setAssetCode(assetCode);
                
                detailEntity.setAssetLocationId(detailDTO.getAssetLocationId());
                detailEntity.setQty(detailDTO.getQty());
                detailEntity.setSupplierId(detailDTO.getSupplierId());
                detailEntity.setSupplierName(detailDTO.getSupplierName());
                detailEntity.setUseDeptName(detailDTO.getUseDeptName());
                detailEntity.setUseDeptId(detailDTO.getUseDeptId());
                detailEntity.setCostType(detailDTO.getCostType());
                detailEntity.setRemark(detailDTO.getRemark());
                detailEntities.add(detailEntity);
            }
            
            if (CollUtil.isNotEmpty(detailEntities)) {
                boolean detailSaveResult = assetCardDetailService.saveBatch(detailEntities);
                if (!detailSaveResult) {
                    throw new ServiceException("资产卡片明细保存失败");
                }
                log.info("资产卡片明细保存成功，共保存{}条明细", detailEntities.size());
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产卡片主单" , assetCardEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), assetCardEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetCardEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetCardDTO.UpdateDTO addOrUpdateDTO) {
        // 验证明细不能为空
        if (CollUtil.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("资产卡片明细不能为空，至少需要一条明细数据");
        }
        
        AssetCardEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产卡片主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetCardEntity assetCardEntity =  BeanMapperUtils.map(AssetCardEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetCardEntity);
        log.info("编辑 开始修改资产卡片主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetCardEntity);
        if(!save) {
            throw new ServiceException("资产卡片主单保存失败");
        }
        
        // 修改明细数据（增量更新：有ID的更新，没有ID的新增，之前存在现在不在的删除）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            // 查询已存在的明细数据
            List<AssetCardDetailEntity> existingDetails = assetCardDetailService.lambdaQuery()
                .eq(AssetCardDetailEntity::getMainId, addOrUpdateDTO.getId())
                .list();

            // 构建已存在明细的Map，key为id，value为明细实体
            Map<String, AssetCardDetailEntity> existingDetailMap = existingDetails.stream()
                .filter(detail -> StringUtils.isNotBlank(detail.getId()))
                .collect(Collectors.toMap(AssetCardDetailEntity::getId, item -> item));

            // 处理明细数据：新增、更新、删除
            List<AssetCardDetailEntity> toSaveDetails = new ArrayList<>();
            List<String> toDeleteDetails = new ArrayList<>();
            Set<String> processedDetailIds = new HashSet<>();

            for (AssetCardDetailDTO.AddDTO detailDTO : addOrUpdateDTO.getDetailList()) {
                String detailId = detailDTO.getId();

                if (StringUtils.isNotBlank(detailId)) {
                    // 有ID，更新已存在的明细
                    processedDetailIds.add(detailId);
                    AssetCardDetailEntity existingDetail = existingDetailMap.get(detailId);

                    if (existingDetail != null) {
                        // 更新已存在的明细
                        existingDetail.setSourceDetailId(detailDTO.getSourceDetailId());
                        existingDetail.setAssetCode(detailDTO.getAssetCode());
                        existingDetail.setAssetLocationId(detailDTO.getAssetLocationId());
                        existingDetail.setQty(detailDTO.getQty());
                        existingDetail.setSupplierId(detailDTO.getSupplierId());
                        existingDetail.setSupplierName(detailDTO.getSupplierName());
                        existingDetail.setUseDeptName(detailDTO.getUseDeptName());
                        existingDetail.setUseDeptId(detailDTO.getUseDeptId());
                        existingDetail.setCostType(detailDTO.getCostType());
                        existingDetail.setRemark(detailDTO.getRemark());
                        toSaveDetails.add(existingDetail);
                    }
                } else {
                    // 没有ID，新增明细
                    AssetCardDetailEntity newDetail = new AssetCardDetailEntity();
                    newDetail.setMainId(addOrUpdateDTO.getId());
                    newDetail.setSourceDetailId(detailDTO.getSourceDetailId());
                    
                    // 自动生成资产编码：ZC + 年月日(YYMMDD) + 6位流水号
                    String assetCode = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZC);
                    newDetail.setAssetCode(assetCode);
                    
                    newDetail.setAssetLocationId(detailDTO.getAssetLocationId());
                    newDetail.setQty(detailDTO.getQty());
                    newDetail.setSupplierId(detailDTO.getSupplierId());
                    newDetail.setSupplierName(detailDTO.getSupplierName());
                    newDetail.setUseDeptName(detailDTO.getUseDeptName());
                    newDetail.setUseDeptId(detailDTO.getUseDeptId());
                    newDetail.setCostType(detailDTO.getCostType());
                    newDetail.setRemark(detailDTO.getRemark());
                    toSaveDetails.add(newDetail);
                }
            }

            // 找出需要删除的明细（在新列表中不存在的）
            for (AssetCardDetailEntity existingDetail : existingDetails) {
                if (!processedDetailIds.contains(existingDetail.getId())) {
                    toDeleteDetails.add(existingDetail.getId());
                }
            }

            // 执行删除操作
            if (!toDeleteDetails.isEmpty()) {
                // 查询要删除的明细信息用于日志记录
                List<AssetCardDetailEntity> deleteDetails = existingDetails.stream()
                    .filter(detail -> toDeleteDetails.contains(detail.getId()))
                    .collect(Collectors.toList());

                boolean deleteResult = assetCardDetailService.lambdaUpdate()
                    .in(AssetCardDetailEntity::getId, toDeleteDetails)
                    .remove();

                if (!deleteResult) {
                    log.warn("删除资产卡片明细数据失败，ids：{}", toDeleteDetails);
                } else {
                    // 添加删除日志
                    List<Pair<String, String>> deletePairList = deleteDetails.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getAssetCode()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("删除资产卡片明细【%s】", ModuleTypeEnum.ASSET_CARD.getCode(), deletePairList, "编辑操作");
                    log.info("删除资产卡片明细数据成功，共删除{}条明细", toDeleteDetails.size());
                }
            }

            // 执行保存/更新操作
            if (!toSaveDetails.isEmpty()) {
                // 分离新增和更新的明细
                List<AssetCardDetailEntity> addDetailList = toSaveDetails.stream()
                    .filter(e -> StringUtils.isBlank(e.getId()))
                    .collect(Collectors.toList());
                List<AssetCardDetailEntity> updateDetailList = toSaveDetails.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getId()))
                    .collect(Collectors.toList());

                boolean saveResult = assetCardDetailService.saveOrUpdateBatch(toSaveDetails);
                if (!saveResult) {
                    throw new ServiceException("资产卡片明细保存失败");
                }

                // 添加新增日志
                if (CollUtil.isNotEmpty(addDetailList)) {
                    List<Pair<String, String>> addPairList = addDetailList.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getAssetCode()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("添加资产卡片明细【%s】", ModuleTypeEnum.ASSET_CARD.getCode(), addPairList, "编辑操作");
                }

                // 添加更新日志
                if (CollUtil.isNotEmpty(updateDetailList)) {
                    for (AssetCardDetailEntity updateDetail : updateDetailList) {
                        AssetCardDetailEntity oldDetail = existingDetails.stream()
                            .filter(e -> Objects.equals(e.getId(), updateDetail.getId()))
                            .findFirst()
                            .orElse(null);
                        if (Objects.nonNull(oldDetail)) {
                            operateLogService.addModuleOperateLogByObj(oldDetail, updateDetail, ModuleTypeEnum.ASSET_CARD.getCode(), addOrUpdateDTO.getId(), String.format("编辑资产卡片明细【%s】", oldDetail.getAssetCode()));
                        }
                    }
                }

                log.info("资产卡片明细保存成功，共保存{}条明细", toSaveDetails.size());
            }
        }

        // 记录主单操作日志
            log.info("编辑 开始记录资产卡片主单日志数据，单号：【{}】", assetCardEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetCardEntity.getCode(), "资产卡片主单");
        operateLogService.addModuleOperateLogByObj(old, assetCardEntity, ModuleTypeEnum.ASSET_CARD.getCode(), assetCardEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetCardDTO.ListDTO> paging(PagingDTO<AssetCardDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetCardDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetCardDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetCardDTO.PagingParamDTO searchParam = new AssetCardDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetCardDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        }); 
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetCardDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetCardDTO.TabListDTO newTab = new AssetCardDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
                list.add(newTab);
            }
        });
        
        // 按照指定顺序排序：待提交、审核中、已审核、不通过
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approve", "reject");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(AssetCardDTO.TabListDTO::getCount).sum();
        AssetCardDTO.TabListDTO allTab = new AssetCardDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }


    /**
     * 获取资产卡片分页数据（用于异步导出）
     * @param dto
     * @return
     */
    @Override
    public PagingVO<AssetCardDTO.ListDTO> getAssetCardPageData(PagingDTO<AssetCardDTO.ExportDTO> dto) {
        // 调用现有的分页查询方法
        Page<AssetAcceptDTO.ListDTO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<AssetCardDTO.ListDTO> pageData = this.baseMapper.listExport(page,dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
     * 获取已审核资产卡片列表（用于盘点方案）
     * @param dto
     * @return
     */
    @Override
    public List<AssetCardDTO.ApprovedCardDTO> getApprovedCardList(AssetCardDTO.QueryApprovedDTO dto) {
        List<AssetCardDTO.ApprovedCardDTO> list = this.baseMapper.getApprovedCardList(dto);
        if(CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        // 填充资产类型名称
        for(AssetCardDTO.ApprovedCardDTO card : list) {
            if (StringUtils.isNotBlank(card.getType())) {
                card.setTypeName(com.erp.model.fms.enums.AssetCategoryEnum.getName(card.getType()));
                // 资产状态枚举转换
                card.setStatusName(com.erp.model.fms.enums.AssetStatusEnum.getName(card.getStatus()));
                // 变动方式枚举转换
                card.setChangeMethodName(com.erp.model.fms.enums.ChangeMethodEnum.getName(card.getChangeMethod()));

                card.setUnitName(com.erp.model.fms.enums.UnitEnum.getName(card.getUnit()));
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetCardEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产卡片主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产卡片主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动资产卡片主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产卡片主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetCardDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetCardDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetCardEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetCardEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_CARD.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        AssetCardEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产卡片主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetCardEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        
        if (Boolean.TRUE.equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的单据不能反审核");
        }
        
        // 检查是否存在关联的资产盘点表（未删除且未作废）
        // 先查询关联的盘点明细，再查询对应的主表
        List<String> stocktakingMainIds = assetStocktakingDetailService.lambdaQuery()
            .select(AssetStocktakingDetailEntity::getMainId)
            .eq(AssetStocktakingDetailEntity::getCardId, entity.getId())
            .eq(AssetStocktakingDetailEntity::getIsDeleted, false)
            .list()
            .stream()
            .map(AssetStocktakingDetailEntity::getMainId)
            .distinct()
            .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(stocktakingMainIds)) {
            List<AssetStocktakingEntity> stocktakingList = assetStocktakingService.lambdaQuery()
                .in(AssetStocktakingEntity::getId, stocktakingMainIds)
                .eq(AssetStocktakingEntity::getIsDeleted, false)
                .eq(AssetStocktakingEntity::getInvalidStatus, false)
                .list();
            
            if (CollUtil.isNotEmpty(stocktakingList)) {
                String stocktakingCodes = stocktakingList.stream()
                    .map(AssetStocktakingEntity::getCode)
                    .collect(Collectors.joining(", "));
                throw new ServiceException(StrUtil.format("卡片{}存在关联的资产盘点表{},不允许反审核", entity.getCode(), stocktakingCodes));
            }
        }
        
        // 检查是否存在关联的资产处置单（未删除且未作废）
        // 先查询关联的处置明细，再查询对应的主表
        List<String> disposalMainIds = assetDisposalDetailService.lambdaQuery()
            .select(AssetDisposalDetailEntity::getMainId)
            .eq(AssetDisposalDetailEntity::getSourceId, entity.getId())
            .eq(AssetDisposalDetailEntity::getIsDeleted, false)
            .list()
            .stream()
            .map(AssetDisposalDetailEntity::getMainId)
            .distinct()
            .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(disposalMainIds)) {
            List<AssetDisposalEntity> disposalList = assetDisposalService.lambdaQuery()
                .in(AssetDisposalEntity::getId, disposalMainIds)
                .eq(AssetDisposalEntity::getIsDeleted, false)
                .eq(AssetDisposalEntity::getInvalidStatus, false)
                .list();
            
            if (CollUtil.isNotEmpty(disposalList)) {
                String disposalCodes = disposalList.stream()
                    .map(AssetDisposalEntity::getCode)
                    .collect(Collectors.joining(", "));
                throw new ServiceException(StrUtil.format("卡片{}存在关联的资产处置单{},不允许反审核", entity.getCode(), disposalCodes));
            }
        }
        
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetCardEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产卡片主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除资产卡片主单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除资产卡片主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除资产卡片主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetCardEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产卡片主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产卡片主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetCardEntity::getId, id)
            .set(AssetCardEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetCardEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetCardEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产卡片主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改资产卡片主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产卡片主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_CARD.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_CARD.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetCardEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public AssetCardDTO.ViewDTO view(String id) {
        AssetCardEntity assetCardEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到资产卡片主单数据"));
        AssetCardDTO.ViewDTO data = new AssetCardDTO.ViewDTO();
        BeanUtil.copyProperties(assetCardEntity,data);
        data.setApproveStatus(assetCardEntity.getApproveStatus().getCode());
        // 数据填充处理
        fillOne(data);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetCardEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_CARD.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetCardDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        String id = data.getId();

        // 查询明细数据
        List<AssetCardDetailEntity> detailList = assetCardDetailService.lambdaQuery()
                .eq(AssetCardDetailEntity::getMainId, id)
                .list();

        if (CollUtil.isNotEmpty(detailList)) {
            // 收集所有需要查询的资产位置ID和部门ID
            List<String> assetLocationIds = detailList.stream()
                    .map(AssetCardDetailEntity::getAssetLocationId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<String> useDeptIds = detailList.stream()
                    .map(AssetCardDetailEntity::getUseDeptId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询资产位置信息
            Map<String, String> assetLocationMap = new HashMap<>();
            if (CollUtil.isNotEmpty(assetLocationIds)) {
                try {
                    List<com.erp.model.fms.entity.AssetLocationEntity> locationList = assetLocationService.listByIds(assetLocationIds);
                    if (CollUtil.isNotEmpty(locationList)) {
                        assetLocationMap = locationList.stream()
                                .filter(loc -> StringUtils.isNotBlank(loc.getId()) && StringUtils.isNotBlank(loc.getAddress()))
                                .collect(Collectors.toMap(
                                        com.erp.model.fms.entity.AssetLocationEntity::getId,
                                        com.erp.model.fms.entity.AssetLocationEntity::getAddress,
                                        (v1, v2) -> v1
                                ));
                    }
                } catch (Exception e) {
                    log.error("批量查询资产位置信息失败", e);
                }
            }
            
            // 批量查询部门信息
            Map<String, String> deptMap = new HashMap<>();
            if (CollUtil.isNotEmpty(useDeptIds)) {
                try {
                    List<com.erp.model.sys.entity.SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(useDeptIds);
                    if (CollUtil.isNotEmpty(deptList)) {
                        deptMap = deptList.stream()
                                .filter(dept -> StringUtils.isNotBlank(dept.getId()) && StringUtils.isNotBlank(dept.getName()))
                                .collect(Collectors.toMap(
                                        com.erp.model.sys.entity.SysDepartmentEntity::getId,
                                        com.erp.model.sys.entity.SysDepartmentEntity::getName,
                                        (v1, v2) -> v1
                                ));
                    }
                } catch (Exception e) {
                    log.error("批量查询部门信息失败", e);
                }
            }
            
            // 创建副本用于后续查询
            final Map<String, String> finalAssetLocationMap = assetLocationMap;
            final Map<String, String> finalDeptMap = deptMap;
            
            List<AssetCardDetailDTO.ViewDTO> detailViewList = detailList.stream()
                    .map(detail -> {
                        AssetCardDetailDTO.ViewDTO detailView = new AssetCardDetailDTO.ViewDTO();
                        BeanMapperUtils.copy(detail, detailView);
                        
                        // 填充资产位置名称
                        if (StringUtils.isNotBlank(detail.getAssetLocationId())) {
                            detailView.setAssetLocationName(finalAssetLocationMap.get(detail.getAssetLocationId()));
                        }
                        
                        // 填充部门名称
                        if (StringUtils.isNotBlank(detail.getUseDeptId())) {
                            detailView.setUseDeptName(finalDeptMap.get(detail.getUseDeptId()));
                        }
                        
                        // 填充费用项目名称（枚举转换）
                        if (StringUtils.isNotBlank(detail.getCostType())) {
                            detailView.setCostTypeName(DepreciationChargeEnum.getName(detail.getCostType()));
                        }
                        
                        return detailView;
                    })
                    .collect(Collectors.toList());
            data.setDetailList(detailViewList);
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(AssetCardEntity::getId, id)
            .set(AssetCardEntity::getApproveUserId, userInfo.getUid())
            .set(AssetCardEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetCardEntity::getApproveStatus, approveStatus)
            .set(AssetCardEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetCardEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetCardEntity::getId, id)
            .set(AssetCardEntity::getApproveUserId, "")
            .set(AssetCardEntity::getApproveUserName, "")
            .set(AssetCardEntity::getApproveStatus, approveStatus)
            .set(AssetCardEntity::getApproveTime, null)
            .update(new AssetCardEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetCardEntity::getId, id)
        .set(AssetCardEntity::getApproveUserId, "")
        .set(AssetCardEntity::getApproveUserName, "")
        .set(AssetCardEntity::getApproveStatus, approveStatus)
        .set(AssetCardEntity::getApproveTime, null)
        .update(new AssetCardEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetCardDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.ASSET_CARD.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 属性赋值
        for(AssetCardDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 主表字段的枚举值转换
            // 卡片来源枚举转换
            data.setSourceTypeName(com.erp.model.fms.enums.CardSourceEnum.getNameFromSourceType(data.getSourceType()));
            // 资产类别枚举转换
            if (StringUtils.isNotBlank(data.getType())) {
                data.setTypeName(com.erp.model.fms.enums.AssetCategoryEnum.getName(data.getType()));
            }
            // 计量单位枚举转换
            if (StringUtils.isNotBlank(data.getUnit())) {
                data.setUnitName(com.erp.model.fms.enums.UnitEnum.getName(data.getUnit()));
            }
            // 资产状态枚举转换
            if (StringUtils.isNotBlank(data.getStatus())) {
                data.setStatusName(com.erp.model.fms.enums.AssetStatusEnum.getName(data.getStatus()));
            }
            // 变动方式枚举转换
            if (StringUtils.isNotBlank(data.getChangeMethod())) {
                data.setChangeMethodName(com.erp.model.fms.enums.ChangeMethodEnum.getName(data.getChangeMethod()));
            }
            
            // 明细字段的枚举值转换
            // 费用项目枚举转换
            if (StringUtils.isNotBlank(data.getCostType())) {
                data.setCostTypeName(com.erp.model.fms.enums.DepreciationChargeEnum.getName(data.getCostType()));
            }

            //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(curApprove)) {
                    data.setApproveUserName(curApprove);
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetCardEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetCardEntity assetCardEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入资产卡片", FileTaskEventEnum.IMPORT_FMS_ASSET_CARD.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetCard(BaseDTO.ImportDTO dto) {
        AssetCardExcelListener excelListenerUtil = new AssetCardExcelListener(
            dto.getTaskId(), dto.getImportType(), dto.getImportCount()
        );
        
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetCardImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetCardImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "资产卡片错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetCardImportExcelDTO.class);
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
     * 批量处理导入成功的数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleImportSuccessList(List<AssetCardImportExcelDTO> successList,
                                       List<String> errorNoList,
                                       List<AssetCardImportExcelDTO> errorList2,
                                       String importType) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        
        // 过滤掉错误编号的数据
        if (CollUtil.isNotEmpty(errorNoList)) {
            List<AssetCardImportExcelDTO> filteredList = successList.stream()
                .filter(e -> e.getNo() != null && !errorNoList.contains(String.valueOf(e.getNo())))
                .collect(Collectors.toList());
            
            // 被过滤掉的数据添加到错误列表
            List<AssetCardImportExcelDTO> errorData = successList.stream()
                .filter(e -> e.getNo() == null || errorNoList.contains(String.valueOf(e.getNo())))
                .collect(Collectors.toList());
            errorList2.addAll(errorData);
            
            successList = filteredList;
        }
        
        // 批量保存数据
        for (AssetCardImportExcelDTO excelDTO : successList) {
            try {
                AssetCardDTO.AddDTO addDTO = new AssetCardDTO.AddDTO();
                
                // 主表数据（枚举字段已经在Listener中转换为code）
                addDTO.setOrgName(excelDTO.getOrgName());
                addDTO.setType(excelDTO.getType());
                addDTO.setName(excelDTO.getName());
                addDTO.setUnit(excelDTO.getUnit());
                addDTO.setQty(excelDTO.getQty());
                addDTO.setStartUseDate(excelDTO.getStartUseDate());
                addDTO.setRemark(excelDTO.getRemark());
                addDTO.setStatus(excelDTO.getStatus());
                addDTO.setChangeMethod(excelDTO.getChangeMethod());
                addDTO.setSourceType(CardSourceEnum.MANUAL_CREATE.getCode());
                
                // 明细数据
                AssetCardDetailDTO.AddDTO detailDTO = new AssetCardDetailDTO.AddDTO();
                detailDTO.setAssetLocationId(excelDTO.getAssetLocationId());
                detailDTO.setQty(excelDTO.getQty());
                detailDTO.setUseDeptName(excelDTO.getUseDeptName());
                detailDTO.setCostType(excelDTO.getCostType());
                detailDTO.setRemark(excelDTO.getDetailRemark());
                
                addDTO.setDetailList(Collections.singletonList(detailDTO));
                
                // 保存
                this.add(addDTO);
                
            } catch (Exception e) {
                log.error("保存资产卡片失败：{}", e.getMessage(), e);
                excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                errorList2.add(excelDTO);
            }
        }
    }

    /**
     * 下载导入模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/assetCardTemplate.xlsx";
        String excelName = "资产卡片导入模板.xlsx";
        ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public List<AssetCardDTO.ApprovedCardDTO> searchApprovedCard(AssetCardDTO.SearchParamDTO dto) {
        List<String> codes = dto.getCodes();
        if(CollUtil.isNotEmpty(codes)){
            if(codes.size() == 1){
                dto.setSearchKeyword(codes.get(0));
                dto.setCodes(new ArrayList<>());
            }else {
                dto.setSearchKeyword("");
            }
        }
        List<AssetCardDTO.ApprovedCardDTO> list = this.baseMapper.searchApprovedCard(dto);
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 填充资产类型名称
        for(AssetCardDTO.ApprovedCardDTO card : list) {
            if (StringUtils.isNotBlank(card.getType())) {
                card.setTypeName(com.erp.model.fms.enums.AssetCategoryEnum.getName(card.getType()));
                // 资产状态枚举转换
                card.setStatusName(com.erp.model.fms.enums.AssetStatusEnum.getName(card.getStatus()));
                // 变动方式枚举转换
                card.setChangeMethodName(com.erp.model.fms.enums.ChangeMethodEnum.getName(card.getChangeMethod()));

                card.setUnitName(com.erp.model.fms.enums.UnitEnum.getName(card.getUnit()));
            }
        }
        return list;
    }
}
