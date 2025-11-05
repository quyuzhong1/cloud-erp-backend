package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.erp.model.fms.dto.AssetStocktakingDetailDTO;
import com.erp.model.fms.entity.AssetStocktakingPlanEntity;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.erp.model.fms.entity.AssetStocktakingDetailEntity;
import com.erp.model.fms.entity.AssetCardEntity;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.mapper.AssetStocktakingPlanMapper;
import com.erp.server.fms.service.*;
import com.erp.server.fms.listener.AssetStocktakingPlanExcelListener;
import com.erp.model.fms.dto.excel.AssetStocktakingPlanImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.business.enums.FileTaskStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * <p>
 * 资产盘点方案表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetStocktakingPlanServiceImpl extends SuperServiceImpl<AssetStocktakingPlanMapper, AssetStocktakingPlanEntity> implements AssetStocktakingPlanService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private AssetStocktakingService assetStocktakingService;
    @Autowired
    private AssetCardService assetCardService;
    @Autowired
    private AssetCardDetailService assetCardDetailService;
    @Autowired
    private AssetStocktakingDetailService assetStocktakingDetailService;
    @Autowired
    private com.erp.server.fms.mapper.AssetCardDetailMapper assetCardDetailMapper;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private FileFeign fileFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private AssetLocationService assetLocationService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetStocktakingPlanDTO.AddDTO addDTO) {
        AssetStocktakingPlanEntity assetStocktakingPlanEntity = new AssetStocktakingPlanEntity();
        BeanMapperUtils.copy(addDTO, assetStocktakingPlanEntity);

        // 数据处理
        handleData(assetStocktakingPlanEntity);

        log.info("开始新增资产盘点方案单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_PDFA);
        assetStocktakingPlanEntity.setCode(code);
        boolean save = super.save(assetStocktakingPlanEntity);
        if(!save) {
            throw new ServiceException("资产盘点方案单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产盘点方案单" , assetStocktakingPlanEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), assetStocktakingPlanEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetStocktakingPlanEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetStocktakingPlanDTO.UpdateDTO addOrUpdateDTO) {
        AssetStocktakingPlanEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产盘点方案单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetStocktakingPlanEntity assetStocktakingPlanEntity =  BeanMapperUtils.map(AssetStocktakingPlanEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetStocktakingPlanEntity);
        log.info("编辑 开始修改资产盘点方案单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetStocktakingPlanEntity);
        if(!save) {
            throw new ServiceException("资产盘点方案单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录资产盘点方案单日志数据，单号：【{}】", assetStocktakingPlanEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetStocktakingPlanEntity.getCode(), "资产盘点方案单");
        operateLogService.addModuleOperateLogByObj(old, assetStocktakingPlanEntity,  ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), assetStocktakingPlanEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<AssetStocktakingPlanDTO.ListDTO> paging(PagingDTO<AssetStocktakingPlanDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetStocktakingPlanDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetStocktakingPlanDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetStocktakingPlanDTO.PagingParamDTO searchParam = new AssetStocktakingPlanDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetStocktakingPlanDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        }); 
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetStocktakingPlanDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetStocktakingPlanDTO.TabListDTO newTab = new AssetStocktakingPlanDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
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
        int totalCount = list.stream().mapToInt(AssetStocktakingPlanDTO.TabListDTO::getCount).sum();
        AssetStocktakingPlanDTO.TabListDTO allTab = new AssetStocktakingPlanDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }


    /**
     * 获取资产盘点方案分页数据（用于异步导出）
     * @param dto
     * @return
     */
    @Override
    public PagingVO<AssetStocktakingPlanDTO.ListDTO> getAssetStocktakingPlanPageData(PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto) {
        // 调用现有的分页查询方法
        IPage<AssetStocktakingPlanDTO.ListDTO> pageData = this.baseMapper.listExport(dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetStocktakingPlanEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产盘点方案单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产盘点方案单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动资产盘点方案单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产盘点方案单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单");
        operateLogService.addModuleOperateLog(msg,ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetStocktakingPlanDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetStocktakingPlanDTO.UpdateDTO dto) {
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
        AssetStocktakingPlanEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetStocktakingPlanEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.INVENTORY_PLAN.getCode());
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
        AssetStocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点方案单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetStocktakingPlanEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        
        // 校验是否存在资产盘点表(未删除或未作废)
        long count = assetStocktakingService.lambdaQuery()
                .eq(AssetStocktakingEntity::getSourceId, entity.getId())
                .eq(AssetStocktakingEntity::getSourceType, SourceTypeEnum.STOCKTAKING_PLAN.getCode())
                .eq(AssetStocktakingEntity::getIsDeleted, false)
                .eq(AssetStocktakingEntity::getInvalidStatus, InvalidStatusEnum.NOT_VOIDED.getStatus())
                .count();
        
        if (count > 0) {
            throw new ServiceException("存在关联的资产盘点表（未删除或未作废），不允许反审核");
        }
        
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetStocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点方案单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除资产盘点方案单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除资产盘点方案单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除资产盘点方案单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetStocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点方案单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产盘点方案单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetStocktakingPlanEntity::getId, id)
            .set(AssetStocktakingPlanEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetStocktakingPlanEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetStocktakingPlanEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产盘点方案单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改资产盘点方案单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产盘点方案单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.STOCKTAKING_PLAN.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(SourceTypeEnum.STOCKTAKING_PLAN.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetStocktakingPlanEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        
        // 审核通过后不自动生成资产盘点表，需要手动执行下推操作

        return Boolean.TRUE;
    }

    /**
     * 根据盘点方案生成资产盘点表
     * @param planEntity 盘点方案实体
     */
    private void generateAssetStocktakingTable(AssetStocktakingPlanEntity planEntity) {
        log.info("开始根据盘点方案生成资产盘点表，方案编号：{}", planEntity.getCode());
        
        // 根据资产范围查询符合条件的资产卡片明细
        List<AssetCardDetailEntity> cardDetailList = queryCardDetailsByScope(planEntity);
        
        if (CollUtil.isEmpty(cardDetailList)) {
            log.warn("根据盘点方案未查询到符合条件的资产卡片明细，方案编号：{}", planEntity.getCode());
            return;
        }
        
        // 获取资产卡片主表信息
        List<String> cardIds = cardDetailList.stream()
                .map(AssetCardDetailEntity::getMainId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<String, AssetCardEntity> cardMap = new HashMap<>();
        if (CollUtil.isNotEmpty(cardIds)) {
            List<AssetCardEntity> cardList = assetCardService.lambdaQuery()
                    .in(AssetCardEntity::getId, cardIds)
                    .list();
            cardMap = cardList.stream()
                    .collect(Collectors.toMap(AssetCardEntity::getId, card -> card));
        }
        
        // 构建盘点明细 DTO 列表
        List<AssetStocktakingDetailDTO.UpdateDTO> detailDTOList = new ArrayList<>(cardDetailList.size());
        
        for (AssetCardDetailEntity detail : cardDetailList) {
            AssetCardEntity card = cardMap.get(detail.getMainId());
            if (card == null) {
                continue;
            }
            
            AssetStocktakingDetailDTO.UpdateDTO detailDTO = new AssetStocktakingDetailDTO.UpdateDTO();
            detailDTO.setAssetCategory(card.getType());
            detailDTO.setCardId(card.getId());
            detailDTO.setCardDetailId(detail.getId());  // 关键字段：标识从资产卡片关联的明细
            detailDTO.setCardCode(card.getCode());
            detailDTO.setAssetId(card.getId());
            detailDTO.setAssetName(card.getName());
            detailDTO.setUnit(card.getUnit());
            detailDTO.setAssetStatus(card.getStatus());
            detailDTO.setAssetCode(detail.getAssetCode());
            detailDTO.setBookQty(detail.getQty());
            detailDTO.setBookLocation(detail.getAssetLocationId());
            detailDTO.setFirstCountQty(0);  // 初盘数量默认为0，等待用户填写
            detailDTO.setIsRecount(false);  // 默认不需要复盘
            
            detailDTOList.add(detailDTO);
        }
        
        // 创建资产盘点表主单（包含明细）
        AssetStocktakingDTO.AddDTO addDTO = new AssetStocktakingDTO.AddDTO();
        addDTO.setSourceCode(planEntity.getCode());
        addDTO.setSourceType(SourceTypeEnum.STOCKTAKING_PLAN.getCode());
        addDTO.setSourceId(planEntity.getId());
        addDTO.setAssetOrgId(planEntity.getAssetOrgId());
        addDTO.setAssetOrgName(planEntity.getAssetOrgName());
        addDTO.setRemark(planEntity.getRemark());
        addDTO.setDetailList(detailDTOList);  // 设置明细列表
        
        // 创建资产盘点表（add 方法会自动保存主表和明细）
        BaseResultDTO.AddDTO addResult = assetStocktakingService.add(addDTO);
        
        log.info("成功生成资产盘点表，方案编号：{}，盘点表编号：{}，明细数量：{}", 
                planEntity.getCode(), addResult.getCode(), detailDTOList.size());
    }

    /**
     * 根据盘点方案的资产范围查询符合条件的资产卡片明细
     * @param planEntity 盘点方案实体
     * @return 资产卡片明细列表
     */
    private List<AssetCardDetailEntity> queryCardDetailsByScope(AssetStocktakingPlanEntity planEntity) {
        // 使用JOIN关联查询，避免查询大量ID导致的内存和性能问题
        return assetCardDetailMapper.queryCardDetailsByScopeWithJoin(
                planEntity.getCardCodeStart(),
                planEntity.getCardCodeEnd()
        );
    }

    @Override
    public AssetStocktakingPlanDTO.ViewDTO view(String id) {
        AssetStocktakingPlanEntity assetStocktakingPlanEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到资产盘点方案单数据"));
        AssetStocktakingPlanDTO.ViewDTO data = BeanMapperUtils.map(AssetStocktakingPlanDTO.ViewDTO.class, assetStocktakingPlanEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetStocktakingPlanEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        startDTO.setBusinessKey(SourceTypeEnum.INVENTORY_PLAN.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetStocktakingPlanDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(AssetStocktakingPlanEntity::getId, id)
            .set(AssetStocktakingPlanEntity::getApproveUserId, userInfo.getUid())
            .set(AssetStocktakingPlanEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetStocktakingPlanEntity::getApproveStatus, approveStatus)
            .set(AssetStocktakingPlanEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetStocktakingPlanEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetStocktakingPlanEntity::getId, id)
            .set(AssetStocktakingPlanEntity::getApproveUserId, "")
            .set(AssetStocktakingPlanEntity::getApproveUserName, "")
            .set(AssetStocktakingPlanEntity::getApproveStatus, approveStatus)
            .set(AssetStocktakingPlanEntity::getApproveTime, null)
            .update(new AssetStocktakingPlanEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetStocktakingPlanEntity::getId, id)
        .set(AssetStocktakingPlanEntity::getApproveUserId, "")
        .set(AssetStocktakingPlanEntity::getApproveUserName, "")
        .set(AssetStocktakingPlanEntity::getApproveStatus, approveStatus)
        .set(AssetStocktakingPlanEntity::getApproveTime, null)
        .update(new AssetStocktakingPlanEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetStocktakingPlanDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.INVENTORY_PLAN.getCode(), obj.getId()));
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
        for(AssetStocktakingPlanDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));

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
    private void validateSubmit(AssetStocktakingPlanEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetStocktakingPlanEntity assetStocktakingPlanEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
    * 下推操作（校验资产盘点表中是否存在未审核的资产卡片）
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO pushDown(String id) {
        AssetStocktakingPlanEntity planEntity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException("未找到资产盘点方案单数据"));
        
        // 只有已审核的盘点方案才能下推
        if (!ApproveStatusEnum.APPROVE.equals(planEntity.getApproveStatus())) {
            throw new ServiceException("只有已审核的盘点方案才能下推");
        }
        
        // 根据盘点方案配置的卡片编码范围查询未审核的资产卡片
        LambdaQueryChainWrapper<AssetCardEntity> query = assetCardService.lambdaQuery()
                .ne(AssetCardEntity::getApproveStatus, ApproveStatusEnum.APPROVE)
                .eq(AssetCardEntity::getIsDeleted, false);
        
        // 按卡片编码范围筛选
        if (StringUtils.isNotBlank(planEntity.getCardCodeStart())) {
            query.ge(AssetCardEntity::getCode, planEntity.getCardCodeStart());
        }
        if (StringUtils.isNotBlank(planEntity.getCardCodeEnd())) {
            query.le(AssetCardEntity::getCode, planEntity.getCardCodeEnd());
        }
        
        List<AssetCardEntity> unapprovedCards = query.list();
        
        // 如果存在未审核的资产卡片，则不允许下推
        if (CollUtil.isNotEmpty(unapprovedCards)) {
            // 收集未审核的卡片编码
            String unapprovedCodes = unapprovedCards.stream()
                    .map(AssetCardEntity::getCode)
                    .limit(3) // 最多显示3个
                    .collect(Collectors.joining(","));
            
            if (unapprovedCards.size() > 3) {
                unapprovedCodes += "等" + unapprovedCards.size() + "个";
            }
            
            String errorMsg = StrUtil.format("盘点方案\"{}\"存在未审核的资产卡片{},不允许盘点",
                    planEntity.getCode(), unapprovedCodes);
            throw new ServiceException(errorMsg);
        }
        
        // 下推操作：生成资产盘点表并提交审核
        log.info("盘点方案下推校验通过，开始生成资产盘点表，方案编号：{}", planEntity.getCode());
        
        // 检查是否已经生成过资产盘点表
        long existCount = assetStocktakingService.lambdaQuery()
                .eq(AssetStocktakingEntity::getSourceId, planEntity.getId())
                .eq(AssetStocktakingEntity::getSourceType, SourceTypeEnum.INVENTORY_PLAN.getCode())
                .eq(AssetStocktakingEntity::getIsDeleted, false)
                .count();
        
        if (existCount > 0) {
            throw new ServiceException("该盘点方案已生成资产盘点表，不能重复下推");
        }
        
        // 生成资产盘点表
        generateAssetStocktakingTable(planEntity);
        
        // 查询刚生成的资产盘点表并提交
        List<AssetStocktakingEntity> stocktakingList = assetStocktakingService.lambdaQuery()
                .eq(AssetStocktakingEntity::getSourceId, planEntity.getId())
                .eq(AssetStocktakingEntity::getSourceType, SourceTypeEnum.INVENTORY_PLAN.getCode())
                .eq(AssetStocktakingEntity::getIsDeleted, false)
                .list();
        
        if (CollUtil.isNotEmpty(stocktakingList)) {
            // 批量提交资产盘点表
            int submitCount = 0;
            for (AssetStocktakingEntity stocktaking : stocktakingList) {
                try {
                    assetStocktakingService.submit(stocktaking.getId());
                    submitCount++;
                    log.info("成功提交资产盘点表，编号：{}", stocktaking.getCode());
                } catch (Exception e) {
                    log.error("提交资产盘点表失败，编号：{}，原因：{}", stocktaking.getCode(), e.getMessage());
                }
            }
            log.info("盘点方案下推完成，方案编号：{}，成功提交{}个资产盘点表", 
                    planEntity.getCode(), submitCount);
        }
        
        // 操作日志
        String msg = StrUtil.format("用户【{}】对盘点方案【{}】执行下推操作，生成并提交{}个资产盘点表", 
                UserContext.getDefaultLoginUser().getUserName(), 
                planEntity.getCode(), 
                stocktakingList != null ? stocktakingList.size() : 0);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVENTORY_PLAN.getCode(), 
                planEntity.getId(), "下推操作");
        
        return BatchResultDTO.success(planEntity.getId(), planEntity.getCode());
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入资产盘点方案", FileTaskEventEnum.IMPORT_FMS_ASSET_STOCKTAKING_PLAN.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetStocktakingPlan(BaseDTO.ImportDTO dto) {
        AssetStocktakingPlanExcelListener excelListenerUtil = new AssetStocktakingPlanExcelListener(
            this, assetLocationService, sysUserFeign, 
            dto.getTaskId(), dto.getImportType(), dto.getImportCount()
        );
        
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetStocktakingPlanImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetStocktakingPlanImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "资产盘点方案错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetStocktakingPlanImportExcelDTO.class);
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
    public List<AssetStocktakingPlanDTO.DropDownDTO> dropDownList(String keyword) {
        // 构建查询条件
        LambdaQueryChainWrapper<AssetStocktakingPlanEntity> queryWrapper = this.lambdaQuery()
                .eq(AssetStocktakingPlanEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                .eq(AssetStocktakingPlanEntity::getInvalidStatus, false)
                .eq(AssetStocktakingPlanEntity::getIsDeleted, false);

        // 如果有关键字，添加模糊查询条件
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(AssetStocktakingPlanEntity::getCode, keyword)
                    .or()
                    .like(AssetStocktakingPlanEntity::getPlanName, keyword)
            );
        }

        // 按编码升序排列
        queryWrapper.orderByAsc(AssetStocktakingPlanEntity::getCode);

        List<AssetStocktakingPlanEntity> entityList = queryWrapper.list();

        if (CollUtil.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        return entityList.stream().map(entity -> {
            AssetStocktakingPlanDTO.DropDownDTO dto = new AssetStocktakingPlanDTO.DropDownDTO();
            dto.setId(entity.getId());
            dto.setPlanName(entity.getPlanName());
            dto.setDisabled(false);
            return dto;
        }).collect(Collectors.toList());
    }
}
