package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleBackDetailDTO;
import com.erp.model.wms.dto.SampleBackInfoDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.model.wms.entity.SampleBackDetailEntity;
import com.erp.model.wms.entity.SampleBackInfoEntity;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SampleBackInfoMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.dto.SampleLedgerDTO;

/**
 * <p>
 * 样品退回单 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleBackInfoServiceImpl extends SuperServiceImpl<SampleBackInfoMapper, SampleBackInfoEntity> implements SampleBackInfoService,SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private SampleBackDetailService sampleBackDetailService;
    @Autowired
    private SampleLedgerService sampleLedgerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleBackInfoDTO.AddDTO addDTO) {
        SampleBackInfoEntity sampleBackInfoEntity = new SampleBackInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleBackInfoEntity);

        // 数据处理
        handleData(sampleBackInfoEntity);

        log.info("开始新增样品退回单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPTH);
        sampleBackInfoEntity.setCode(code);
        
        // 设置默认审批状态为待提交
        sampleBackInfoEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        
        boolean save = super.save(sampleBackInfoEntity);
        if(!save) {
            throw new ServiceException("样品退回单保存失败");
        }

        // 保存明细
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            for (SampleBackDetailDTO.AddDTO detailDTO : addDTO.getDetailList()) {
                SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                BeanMapperUtils.copy(detailDTO, detailEntity);
                detailEntity.setMainId(sampleBackInfoEntity.getId());
                
                boolean detailSave = sampleBackDetailService.save(detailEntity);
                if (!detailSave) {
                    throw new ServiceException("样品退回单明细保存失败");
                }
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品退回单" , sampleBackInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), sampleBackInfoEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(sampleBackInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleBackInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleBackInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品退回单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleBackInfoEntity sampleBackInfoEntity =  BeanMapperUtils.map(SampleBackInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleBackInfoEntity);
        log.info("编辑 开始修改样品退回单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleBackInfoEntity);
        if(!save) {
            throw new ServiceException("样品退回单保存失败");
        }
        // 处理明细数据（包含增删改）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            // 获取原有明细列表
            LambdaQueryWrapper<SampleBackDetailEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SampleBackDetailEntity::getMainId, addOrUpdateDTO.getId());
            List<SampleBackDetailEntity> existingDetails = sampleBackDetailService.list(wrapper);
            
            // 创建原有明细的ID集合，用于判断哪些需要删除
            Set<String> existingDetailIds = existingDetails.stream()
                .map(SampleBackDetailEntity::getId)
                .collect(Collectors.toSet());
            
            // 创建新明细的ID集合，用于判断哪些需要新增
            Set<String> newDetailIds = addOrUpdateDTO.getDetailList().stream()
                .map(SampleBackDetailDTO.UpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            // 删除不再存在的明细
            Set<String> toDeleteIds = existingDetailIds.stream()
                .filter(id -> !newDetailIds.contains(id))
                .collect(Collectors.toSet());
            if (!toDeleteIds.isEmpty()) {
                sampleBackDetailService.removeByIds(toDeleteIds);
            }
            
            // 处理新增和更新
            for (SampleBackDetailDTO.UpdateDTO detailDTO : addOrUpdateDTO.getDetailList()) {
                if (StrUtil.isBlank(detailDTO.getId())) {
                    // 新增明细
                    SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                    BeanMapperUtils.copy(detailDTO, detailEntity);
                    detailEntity.setMainId(addOrUpdateDTO.getId());
                    
                    boolean detailSave = sampleBackDetailService.save(detailEntity);
                    if (!detailSave) {
                        throw new ServiceException("样品退回单明细保存失败");
                    }
                } else {
                    // 更新明细
                    SampleBackDetailEntity detailEntity = new SampleBackDetailEntity();
                    BeanMapperUtils.copy(detailDTO, detailEntity);
                    detailEntity.setMainId(addOrUpdateDTO.getId());
                    
                    boolean detailUpdate = sampleBackDetailService.updateById(detailEntity);
                    if (!detailUpdate) {
                        throw new ServiceException("样品退回单明细更新失败");
                    }
                }
            }
        }

        // 记录主单操作日志
            log.info("编辑 开始记录样品退回单日志数据，单号：【{}】", sampleBackInfoEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleBackInfoEntity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLogByObj(old, sampleBackInfoEntity, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), sampleBackInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SampleBackInfoDTO.ListDTO> paging(PagingDTO<SampleBackInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleBackInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleBackInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleBackInfoDTO.PagingParamDTO searchParam = new SampleBackInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleBackInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleBackInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SampleBackInfoDTO.TabListDTO(status, 0));
        }
        });
        list.add(new SampleBackInfoDTO.TabListDTO("all", list.stream().mapToInt(SampleBackInfoDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public SampleBackInfoDTO.ViewDTO view(String id) {
        SampleBackInfoDTO.ViewDTO viewDTO = new SampleBackInfoDTO.ViewDTO();
        SampleBackInfoEntity entity = super.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "样品退回单");
        }
        BeanMapperUtils.copy(entity, viewDTO);
        // 设置明细列表到ViewDTO中
        List<SampleBackDetailDTO.ViewDTO> detailList = sampleBackDetailService.list(new LambdaQueryWrapper<SampleBackDetailEntity>().eq(SampleBackDetailEntity::getMainId, id)).stream()
            .map(detail -> {
                SampleBackDetailDTO.ViewDTO detailDTO = new SampleBackDetailDTO.ViewDTO();
                BeanMapperUtils.copy(detail, detailDTO);
                
                // 查询可退回数量
                SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
                searchDTO.setUserId(detail.getUseUserId());
                searchDTO.setSkuNo(detail.getSkuNo());
                searchDTO.setType("back");
                List<SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(searchDTO);
                
                // 设置可退回数量
                if (CollUtil.isNotEmpty(skuAvailableQtyDTOS)) {
                    Integer availableQty = skuAvailableQtyDTOS.stream()
                        .mapToInt(SampleLedgerDTO.SkuAvailableQtyDTO::getAvailableQty)
                        .sum();
                    detailDTO.setAvailableQty(availableQty);
                } else {
                    detailDTO.setAvailableQty(0);
                }
                
                return detailDTO;
            })
            .collect(Collectors.toList());
        viewDTO.setDetailList(detailList);
        
        return viewDTO;
    }

    @Override
    public void exportList(SampleBackInfoDTO.ExportDTO param, HttpServletResponse response) {
        List<SampleBackInfoDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/sampleBackInfo.xlsx";
        String name = "样品退回单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleBackInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品退回单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品退回单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动样品退回单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品退回单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }
    /**
     * 启动流程
     *
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(SampleBackInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleBackInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleBackInfoDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleBackInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleBackInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleBackInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除样品退回单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品退回单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getCode(), "删除样品退回单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品退回单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
            .set(SampleBackInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleBackInfoEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleBackInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品退回单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品退回单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品退回单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_BACK_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_BACK_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleBackInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        // 记录台账流水
        try {
            ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), approveType);
            if (flowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                log.info("样品退回单台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
            }
        } catch (Exception e) {
            log.error("样品退回单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品退回单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        return Boolean.TRUE;
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
            .set(SampleBackInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleBackInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBackInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleBackInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
            .set(SampleBackInfoEntity::getApproveUserId, "")
            .set(SampleBackInfoEntity::getApproveUserName, "")
            .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
            .set(SampleBackInfoEntity::getApproveTime, null)
            .update(new SampleBackInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleBackInfoEntity::getId, id)
        .set(SampleBackInfoEntity::getApproveStatus, approveStatus)
        .update(new SampleBackInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleBackInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(SampleBackInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleBackInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleBackInfoEntity sampleBackInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

    // ==================== 台账流水构建器实现 ====================

    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_BACK_INFO.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品退回单主表信息
            SampleBackInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品退回单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品退回单明细
            List<SampleBackDetailEntity> detailList = sampleBackDetailService.list(new LambdaQueryWrapper<SampleBackDetailEntity>().eq(SampleBackDetailEntity::getMainId, sourceId));

            if (detailList.isEmpty()) {
                log.warn("样品退回单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleBackDetailEntity detail : detailList) {
                // 计算数量：审核为-X，反审核为+X
                Integer qty = calculateQty(detail.getQty(), approveType);
                
                // 通过sku_no查询sku_id

                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID（如果有的话）
                // flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getBackDate());
            flowDTO.setSourceName("样品退回单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            flowDTO.setUseUserId(entity.getUserId());
            flowDTO.setUseUserName(entity.getUserName());
            flowDTO.setUserId(entity.getUserId());
            flowDTO.setUserName(entity.getUserName());
            flowDTO.setDeptId(entity.getDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品退回单台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算数量：审核为-X，反审核为+X
     */
    @Override
    public Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            return -originalQty; // 审核：-X（减少库存）
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            return originalQty; // 反审核：+X（增加库存）
        }
        
        return 0;
    }


    /**
     * 根据部门ID查询部门名称
     */
    private String getDeptNameById(String deptId) {
        if (StrUtil.isBlank(deptId)) {
            return null;
        }

        try {
            // 调用部门服务根据ID查询部门信息
            SysDepartmentDTO department = sysUserFeign.getUserDeptById(deptId);

            if (department != null && StrUtil.isNotBlank(department.getName())) {
                return department.getName();
            }

            log.warn("未找到部门ID：{}", deptId);
            return null;
        } catch (Exception e) {
            log.error("查询部门名称失败，部门ID：{}，错误：{}", deptId, e.getMessage(), e);
            return null;
        }
    }

}
