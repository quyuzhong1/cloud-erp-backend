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
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.erp.model.fms.dto.AssetStocktakingDetailDTO;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.erp.model.fms.entity.AssetStocktakingDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.mapper.AssetStocktakingMapper;
import com.erp.server.fms.service.AssetStocktakingService;
import com.erp.server.fms.service.AssetStocktakingDetailService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
/**
 * <p>
 * 资产盘点表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetStocktakingServiceImpl extends SuperServiceImpl<AssetStocktakingMapper, AssetStocktakingEntity> implements AssetStocktakingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Resource
    private AssetStocktakingDetailService assetStocktakingDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetStocktakingDTO.AddDTO addDTO) {
        AssetStocktakingEntity assetStocktakingEntity = new AssetStocktakingEntity();
        BeanMapperUtils.copy(addDTO, assetStocktakingEntity);

        // 数据处理
        handleData(assetStocktakingEntity);

        log.info("开始新增资产盘点单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        assetStocktakingEntity.setCode(code);
        boolean save = super.save(assetStocktakingEntity);
        if(!save) {
            throw new ServiceException("资产盘点单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产盘点单" , assetStocktakingEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), assetStocktakingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetStocktakingEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetStocktakingDTO.UpdateDTO addOrUpdateDTO) {
        AssetStocktakingEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产盘点单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        // 主单只允许修改remark
        AssetStocktakingEntity assetStocktakingEntity = new AssetStocktakingEntity();
        assetStocktakingEntity.setId(addOrUpdateDTO.getId());
        assetStocktakingEntity.setRemark(addOrUpdateDTO.getRemark());

        log.info("编辑 开始修改资产盘点单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetStocktakingEntity);
        if(!save) {
            throw new ServiceException("资产盘点单保存失败");
        }
        
        // 处理明细数据
        updateDetail(addOrUpdateDTO, old);

        // 记录主单操作日志
        log.info("编辑 开始记录资产盘点单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "资产盘点单");
        operateLogService.addModuleOperateLogByObj(old, assetStocktakingEntity, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), assetStocktakingEntity.getId(), msg);
        return Boolean.TRUE;
    }
    
    /**
     * 更新资产盘点单明细数据
     * 处理明细的增删改，并根据规则计算差异
     *
     * @param updateDTO 包含待更新明细数据的DTO对象
     * @param mainEntity 主表实体对象
     */
    private void updateDetail(AssetStocktakingDTO.UpdateDTO updateDTO, AssetStocktakingEntity mainEntity) {
        List<AssetStocktakingDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        
        // 查询旧的明细列表
        List<AssetStocktakingDetailEntity> oldList = assetStocktakingDetailService.lambdaQuery()
                .eq(AssetStocktakingDetailEntity::getMainId, mainEntity.getId())
                .list();
        
        List<AssetStocktakingDetailEntity> detailEntities = new ArrayList<>();
        LoginUser currentUser = UserContext.getDefaultLoginUser();
        LocalDate currentDate = LocalDate.now();
        
        for (AssetStocktakingDetailDTO.UpdateDTO detailDTO : detailList) {
            AssetStocktakingDetailEntity detailEntity = BeanMapperUtils.map(AssetStocktakingDetailEntity.class, detailDTO);
            detailEntity.setMainId(mainEntity.getId());
            
            // 计算初盘差异：初盘数量 - 账存数量
            if (detailDTO.getFirstCountQty() != null && detailDTO.getBookQty() != null) {
                detailEntity.setFirstDiffQty(detailDTO.getFirstCountQty() - detailDTO.getBookQty());
            }
            
            // 设置初盘人和初盘日期
            detailEntity.setFirstCountUserId(currentUser.getUid());
            detailEntity.setFirstCountUserName(currentUser.getUserName());
            detailEntity.setFirstCountDate(currentDate);
            
            // 如果需要复盘
            if (Boolean.TRUE.equals(detailDTO.getIsRecount())) {
                // 计算复盘差异：复盘数量 - 账存数量
                if (detailDTO.getRecountQty() != null && detailDTO.getBookQty() != null) {
                    detailEntity.setRecountDiffQty(detailDTO.getRecountQty() - detailDTO.getBookQty());
                }
                
                // 设置复盘人和复盘日期
                detailEntity.setRecountUserId(currentUser.getUid());
                detailEntity.setRecountUserName(currentUser.getUserName());
                detailEntity.setRecountDate(currentDate);
                
                // 最终差异 = 复盘差异
                detailEntity.setFinalDiffQty(detailEntity.getRecountDiffQty());
            } else {
                // 最终差异 = 初盘差异
                detailEntity.setFinalDiffQty(detailEntity.getFirstDiffQty());
            }
            
            detailEntities.add(detailEntity);
        }
        
        // 处理删除的明细数据
        if (CollUtil.isNotEmpty(oldList)) {
            List<String> detailIds = detailEntities.stream()
                    .map(AssetStocktakingDetailEntity::getId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            
            List<AssetStocktakingDetailEntity> removeList = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(removeList)) {
                assetStocktakingDetailService.removeByIds(removeList.stream()
                        .map(AssetStocktakingDetailEntity::getId)
                        .collect(Collectors.toList()));
                log.info("删除资产盘点单明细，数量：{}", removeList.size());
            }
        }
        
        // 处理需要新增的明细数据（ID为空）
        List<AssetStocktakingDetailEntity> addList = detailEntities.stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(addList)) {
            assetStocktakingDetailService.saveBatch(addList);
            log.info("新增资产盘点单明细，数量：{}", addList.size());
        }
        
        // 处理需要更新的明细数据（ID不为空）
        List<AssetStocktakingDetailEntity> updateList = detailEntities.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());
        
        if (CollUtil.isNotEmpty(updateList)) {
            assetStocktakingDetailService.updateBatchById(updateList);
            log.info("更新资产盘点单明细，数量：{}", updateList.size());
        }
    }


    @Override
    public PagingVO<AssetStocktakingDTO.ListDTO> paging(PagingDTO<AssetStocktakingDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetStocktakingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetStocktakingDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetStocktakingDTO.PagingParamDTO searchParam = new AssetStocktakingDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetStocktakingDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        }); 
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetStocktakingDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetStocktakingDTO.TabListDTO newTab = new AssetStocktakingDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
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
        int totalCount = list.stream().mapToInt(AssetStocktakingDTO.TabListDTO::getCount).sum();
        AssetStocktakingDTO.TabListDTO allTab = new AssetStocktakingDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }

    @Override
    public void exportList(AssetStocktakingDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("资产盘点单导出", FileTaskEventEnum.EXPORT_FMS_ASSET_STOCKTAKING.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetStocktakingEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产盘点单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产盘点单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动资产盘点单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产盘点单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetStocktakingDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetStocktakingDTO.UpdateDTO dto) {
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
        AssetStocktakingEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetStocktakingEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_INVENTORY_SHEET.getCode());
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
        AssetStocktakingEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetStocktakingEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetStocktakingEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        
        // 删除明细数据
        log.info("删除 开始删除资产盘点单明细数据，mainId：【{}】", id);
        assetStocktakingDetailService.lambdaUpdate()
                .set(AssetStocktakingDetailEntity::getIsDeleted, Boolean.TRUE)
                .eq(AssetStocktakingDetailEntity::getMainId, id)
                .update();

        // 删除主单数据
        log.info("删除 开始删除资产盘点单主单数据，id：【{}】", id);
        super.removeById(id);
        
        // 删除日志数据
        log.info("删除 开始删除资产盘点单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "删除资产盘点单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetStocktakingEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产盘点单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetStocktakingEntity::getId, id)
            .set(AssetStocktakingEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetStocktakingEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetStocktakingEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改资产盘点单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_INVENTORY_SHEET.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_INVENTORY_SHEET.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetStocktakingEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public AssetStocktakingDTO.ViewDTO view(String id) {
        AssetStocktakingEntity assetStocktakingEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到资产盘点单数据"));
        AssetStocktakingDTO.ViewDTO data = BeanMapperUtils.map(AssetStocktakingDTO.ViewDTO.class, assetStocktakingEntity);
        data.setApproveStatus(assetStocktakingEntity.getApproveStatus().getStatus());
        // 数据填充处理
        fillOne(data);
        
        // 查询明细数据
        List<AssetStocktakingDetailEntity> detailEntities = assetStocktakingDetailService.lambdaQuery()
                .eq(AssetStocktakingDetailEntity::getMainId, id)
                .list();
        
        if (CollUtil.isNotEmpty(detailEntities)) {
            List<AssetStocktakingDetailDTO.ViewDTO> detailList = BeanMapperUtils.copyList(AssetStocktakingDetailDTO.ViewDTO.class, detailEntities);
            data.setDetailList(detailList);
        }
        
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetStocktakingEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_INVENTORY_SHEET.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetStocktakingDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
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
        this.lambdaUpdate().eq(AssetStocktakingEntity::getId, id)
            .set(AssetStocktakingEntity::getApproveUserId, userInfo.getUid())
            .set(AssetStocktakingEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetStocktakingEntity::getApproveStatus, approveStatus)
            .set(AssetStocktakingEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetStocktakingEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetStocktakingEntity::getId, id)
            .set(AssetStocktakingEntity::getApproveUserId, "")
            .set(AssetStocktakingEntity::getApproveUserName, "")
            .set(AssetStocktakingEntity::getApproveStatus, approveStatus)
            .set(AssetStocktakingEntity::getApproveTime, null)
            .update(new AssetStocktakingEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetStocktakingEntity::getId, id)
        .set(AssetStocktakingEntity::getApproveStatus, approveStatus)
        .update(new AssetStocktakingEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetStocktakingDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(AssetStocktakingDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetStocktakingEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetStocktakingEntity assetStocktakingEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public String generateCardCode() {
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZCKP);
        return code;
    }

    /**
     * 获取资产盘点单分页数据（用于异步导出）
     * @author wuht
     * @date: 2025-10-31
     * @param dto 分页参数
     * @return
     */
    @Override
    public PagingVO<AssetStocktakingDTO.ListDTO> getAssetStocktakingPageData(PagingDTO<AssetStocktakingDTO.ExportDTO> dto) {
        // 创建分页对象
        Page<AssetAcceptDTO.ListDTO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        // 调用现有的分页查询方法
        IPage<AssetStocktakingDTO.ListDTO> pageData = this.baseMapper.listExport(page,dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }
}
