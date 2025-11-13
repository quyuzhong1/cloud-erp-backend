package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.SampleDocumentAuditUtil;
import com.common.business.utils.SampleLedgerLockUtil;
import com.common.business.utils.SampleLedgerQtyValidator;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SampleInitialLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.model.wms.entity.SampleInitialLedgerDetailEntity;
import com.erp.model.wms.entity.SampleInitialLedgerEntity;
import com.erp.model.wms.entity.SampleRecipientEntity;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.rpc.workflow.feign.CfgQueryOptionFeign;
import com.erp.server.wms.listener.SampleInitialLedgerExcelListener;
import com.erp.server.wms.mapper.SampleInitialLedgerMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 样品期初台账 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleInitialLedgerServiceImpl extends SuperServiceImpl<SampleInitialLedgerMapper, SampleInitialLedgerEntity> implements SampleInitialLedgerService, SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Resource
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SampleInitialLedgerDetailService sampleInitialLedgerDetailService;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private FileFeign fileFeign;

    @Autowired
    private SampleDocumentAuditUtil sampleDocumentAuditUtil;

    @Autowired
    private SampleLedgerService sampleLedgerService;
    @Autowired
    private SampleLedgerLockUtil sampleLedgerLockUtil;
    @Autowired
    private SampleLedgerQtyValidator sampleLedgerQtyValidator;
    @Autowired
    private CfgQueryOptionFeign cfgQueryOptionFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleInitialLedgerDTO.AddDTO addDTO) {
        SampleInitialLedgerEntity sampleInitialLedgerEntity = new SampleInitialLedgerEntity();
        BeanMapperUtils.copy(addDTO, sampleInitialLedgerEntity);

        // 数据处理
        handleData(sampleInitialLedgerEntity);

        log.info("开始新增样品期初台账");
        // 校验明细不能为空
        if (CollUtil.isEmpty(addDTO.getDetailList())) {
            throw new ServiceException("样品期初台账明细不能为空");
        }
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QCTZ);
        sampleInitialLedgerEntity.setCode(code);
        boolean save = super.save(sampleInitialLedgerEntity);
        if(!save) {
            throw new ServiceException("样品期初台账保存失败");
        }

        // 处理明细数据
        handleDetailData(sampleInitialLedgerEntity.getId(), addDTO.getDetailList());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品期初台账" , sampleInitialLedgerEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), sampleInitialLedgerEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(sampleInitialLedgerEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleInitialLedgerDTO.UpdateDTO addOrUpdateDTO) {
        SampleInitialLedgerEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品期初台账"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleInitialLedgerEntity sampleInitialLedgerEntity =  BeanMapperUtils.map(SampleInitialLedgerEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleInitialLedgerEntity);
        log.info("编辑 开始修改样品期初台账数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleInitialLedgerEntity);
        if(!save) {
            throw new ServiceException("样品期初台账保存失败");
        }

        // 处理明细数据（先删除旧的，再新增新的）
        handleDetailDataForUpdate(sampleInitialLedgerEntity.getId(), addOrUpdateDTO.getDetailList());

        // 记录主单操作日志
        log.info("编辑 开始记录样品期初台账日志数据，单号：【{}】", sampleInitialLedgerEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleInitialLedgerEntity.getCode(), "样品期初台账");
        operateLogService.addModuleOperateLogByObj(old, sampleInitialLedgerEntity, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), sampleInitialLedgerEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SampleInitialLedgerDTO.ListDTO> paging(PagingDTO<SampleInitialLedgerDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleInitialLedgerDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleInitialLedgerDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleInitialLedgerDTO.PagingParamDTO searchParam = new SampleInitialLedgerDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SampleInitialLedgerDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleInitialLedgerDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
            list.add(new SampleInitialLedgerDTO.TabListDTO(status, "", 0));
        }
        });

        list.forEach(e ->{
            e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
        });
        
        // 按照指定顺序排序
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approved", "rejected");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        list.add(0,new SampleInitialLedgerDTO.TabListDTO("all", "全部", list.stream().mapToInt(SampleInitialLedgerDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(SampleInitialLedgerDTO.ExportDTO param, HttpServletResponse response) {
        // 对齐参考：改为异步导出任务
        downloadTaskFeign.saveDownloadTask("样品期初台账导出", FileTaskEventEnum.EXPORT_WMS_SAMPLE_INITIAL_LEDGER_REPORT.getCode(), param);
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        try {
            dto.setUserId(UserContext.getDefaultLoginUser().getUid());
            // 创建异步导入任务
            downloadTaskFeign.saveImportTask("样品期初台账导入", FileTaskEventEnum.IMPORT_WMS_SAMPLE_INITIAL_LEDGER.getCode(), dto);
            return true;
        } catch (Exception e) {
            log.error("创建样品期初台账导入任务失败", e);
            return false;
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleInitialLedgerTemplate.xlsx";
        String standardExcelName = "sampleInitialLedgerTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleInitialLedgerEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品期初台账数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品期初台账状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动样品期初台账流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品期初台账日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     *
     *
     * @param entity
     * @return
     */
    private Map<String,Object> getVariablesMap(SampleInitialLedgerEntity entity){
        CfgQueryOptionDTO.VariablesParamsDTO dto = new CfgQueryOptionDTO.VariablesParamsDTO();
        dto.setBusinessKey(CfgQueryOptionBussinessKeyEnum.SAMPLE_LEDGER_INIT.getCode());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        Map<String, Object> map = cfgQueryOptionFeign.getVariablesMapByBusinessKey(dto);
        return map;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleInitialLedgerDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleInitialLedgerDTO.UpdateDTO dto) {
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
        SampleInitialLedgerEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        // 期初台账单审核时需要校验负数数量的台账是否足够扣减
        validateSampleLedgerQtyWithLock(entity, approveType);

        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleInitialLedgerEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
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
        SampleInitialLedgerEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品期初台账单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 记录台账流水（反审核）
        try {
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (flowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                log.info("样品期初台账反审核台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品期初台账反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品期初台账反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleInitialLedgerEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE)) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleInitialLedgerEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品期初台账数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        List<SampleInitialLedgerDetailEntity> list = sampleInitialLedgerDetailService.list(new LambdaQueryWrapper<SampleInitialLedgerDetailEntity>().eq(SampleInitialLedgerDetailEntity::getMainId, id));
        sampleInitialLedgerDetailService.removeByIds(list.stream().map(SampleInitialLedgerDetailEntity::getId).collect(Collectors.toList()));

        // 删除主单数据
        log.info("删除 开始删除样品期初台账主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品期初台账日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getCode(), "删除样品期初台账数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleInitialLedgerEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品期初台账数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品期初台账状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleInitialLedgerEntity::getId, id)
            .set(SampleInitialLedgerEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleInitialLedgerEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleInitialLedgerEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品期初台账数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品期初台账状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品期初台账");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleInitialLedgerEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        // 只有审核通过和反审核才记录台账流水
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if (ApproveTypeEnum.PASS.equals(approveType) || ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            // 记录台账流水
            try {
                SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), approveType);
                if (flowDTO != null) {
                    sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                    log.info("期初台账单台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("期初台账单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("期初台账单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public SampleInitialLedgerDTO.ViewDTO view(String id) {
        SampleInitialLedgerEntity sampleInitialLedgerEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品期初台账数据"));
        SampleInitialLedgerDTO.ViewDTO data = new SampleInitialLedgerDTO.ViewDTO();
        BeanUtils.copyProperties(sampleInitialLedgerEntity, data);
        data.setApproveStatus(sampleInitialLedgerEntity.getApproveStatus().getCode());
        // 数据填充处理
        fillOne(data);
        // 查询明细数据
        List<SampleInitialLedgerDetailEntity> detailList = sampleInitialLedgerDetailService.lambdaQuery()
            .eq(SampleInitialLedgerDetailEntity::getMainId, id)
            .eq(SampleInitialLedgerDetailEntity::getIsDeleted, false)
            .list();
        // 转换为DTO
        List<SampleInitialLedgerDTO.DetailDTO> detailDTOList = detailList.stream()
            .map(detail -> {
                SampleInitialLedgerDTO.DetailDTO detailDTO = new SampleInitialLedgerDTO.DetailDTO();
                detailDTO.setSkuId(detail.getSkuId());
                detailDTO.setSkuNo(detail.getSkuNo());
                detailDTO.setProductName(detail.getProductName());
                detailDTO.setQty(detail.getQty());
                detailDTO.setRemark(detail.getRemark());
                return detailDTO;
            })
            .collect(Collectors.toList());
        // 设置明细数据到ViewDTO中（需要在ViewDTO中添加detailList字段）
         data.setDetailList(detailDTOList);
        
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SampleInitialLedgerEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleInitialLedgerDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        
        // 设置部门名称
        if (StrUtil.isNotBlank(data.getDeptId())) {
            Map<String, String> deptIdNameMap = getDeptNameByIds(Arrays.asList(data.getDeptId()));
            String deptName = deptIdNameMap.get(data.getDeptId());
            data.setDeptName(deptName);
        }
        
        // 设置用户名称
        if (StrUtil.isNotBlank(data.getUserId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(data.getUserId()));
            if (CollUtil.isNotEmpty(userList)) {
                FindUserDTO user = userList.get(0);
                data.setUserName(user.getUserName());
            }
        }
        
        //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode(), data.getId()));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (listApiResult.isSuccess() && CollectionUtils.isNotEmpty(listApiResult.getData())) {
            String curApprove = listApiResult.getData().stream()
                .filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName()))
                .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                .collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(curApprove)) {
                data.setApproveUserName(curApprove);
            }
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
        this.lambdaUpdate().eq(SampleInitialLedgerEntity::getId, id)
            .set(SampleInitialLedgerEntity::getApproveUserId, userInfo.getUid())
            .set(SampleInitialLedgerEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleInitialLedgerEntity::getApproveStatus, approveStatus)
            .set(SampleInitialLedgerEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleInitialLedgerEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleInitialLedgerEntity::getId, id)
            .set(SampleInitialLedgerEntity::getApproveUserId, "")
            .set(SampleInitialLedgerEntity::getApproveUserName, "")
            .set(SampleInitialLedgerEntity::getApproveStatus, approveStatus)
            .set(SampleInitialLedgerEntity::getApproveTime, null)
            .update(new SampleInitialLedgerEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleInitialLedgerEntity::getId, id)
                .set(SampleInitialLedgerEntity::getApproveUserId, "")
                .set(SampleInitialLedgerEntity::getApproveUserName, "")
                .set(SampleInitialLedgerEntity::getApproveStatus, approveStatus)
                .set(SampleInitialLedgerEntity::getApproveTime, null)
        .update(new SampleInitialLedgerEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleInitialLedgerDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 获取所有部门ID
        List<String> deptIds = list.stream()
            .map(SampleInitialLedgerDTO.ListDTO::getDeptId)
            .filter(StrUtil::isNotBlank)
            .distinct()
            .collect(Collectors.toList());

        // 通过feign获取部门信息
        Map<String, String> deptIdNameMap = getDeptNameByIds(deptIds);
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(list.stream().map(SampleInitialLedgerDTO.ListDTO::getUserId).collect(Collectors.toList()));

        Map<String, String> userNameMap = userList.stream()
                .collect(Collectors.toMap(FindUserDTO::getUserId,FindUserDTO::getUserName));

        // 属性赋值
        for(SampleInitialLedgerDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 设置部门名称
            if (StrUtil.isNotBlank(data.getDeptId())) {
                String deptName = deptIdNameMap.get(data.getDeptId());
                data.setDeptName(deptName);
            }
            // 设置部门名称
            if (StrUtil.isNotBlank(data.getUserId())) {
                String userName = userNameMap.get(data.getUserId());
                data.setUserName(userName);
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
    private void validateSubmit(SampleInitialLedgerEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleInitialLedgerEntity sampleInitialLedgerEntity) {
        // 主表不再处理SKU相关字段，这些字段已移至明细表
        
        // 设置部门名称
        if (StrUtil.isNotBlank(sampleInitialLedgerEntity.getDeptId())) {
            Map<String, String> deptIdNameMap = getDeptNameByIds(Arrays.asList(sampleInitialLedgerEntity.getDeptId()));
            String deptName = deptIdNameMap.get(sampleInitialLedgerEntity.getDeptId());
            sampleInitialLedgerEntity.setDeptName(deptName);
        }
        
        // 设置用户名称
        if (StrUtil.isNotBlank(sampleInitialLedgerEntity.getUserId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(sampleInitialLedgerEntity.getUserId()));
            if (CollUtil.isNotEmpty(userList)) {
                FindUserDTO user = userList.get(0);
                sampleInitialLedgerEntity.setUserName(user.getUserName());
            }
        }
    }

    // ==================== 台账流水构建器实现 ====================

    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_LEDGER_INIT.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取期初台账单信息
            SampleInitialLedgerEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取期初台账单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取明细数据
            List<SampleInitialLedgerDetailEntity> detailList = sampleInitialLedgerDetailService.lambdaQuery()
                .eq(SampleInitialLedgerDetailEntity::getMainId, sourceId)
                .eq(SampleInitialLedgerDetailEntity::getIsDeleted, false)
                .list();
            
            if (CollUtil.isEmpty(detailList)) {
                log.warn("期初台账单没有明细数据，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleInitialLedgerDetailEntity detail : detailList) {
                // 计算数量：审核为+X（若导入填写的为负数，则为-X）
                Integer qty = calculateQty(detail.getQty(), approveType);
                
                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getBillDate());
            flowDTO.setSourceName("期初台账单");
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
            log.error("构建期初台账单台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算数量：审核为+X（若导入填写的为负数，则为-X）
     */
    @Override
    public Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            // 审核：保持原始数量（若导入填写的为负数，则为-X；若为正数，则为+X）
            return originalQty;
        } else if (ApproveTypeEnum.DIS_APPROVE.equals(approveType)) {
            // 反审核：数量取反
            return -originalQty;
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

    /**
     * 批量根据部门ID查询部门名称
     */
    private Map<String, String> getDeptNameByIds(List<String> deptIds) {
        Map<String, String> deptIdNameMap = new HashMap<>();
        
        if (CollUtil.isEmpty(deptIds)) {
            return deptIdNameMap;
        }

        try {
            // 调用部门服务批量查询部门信息
            List<SysDepartmentEntity> departments = sysUserFeign.getDeptByIds(deptIds);
            
            if (CollUtil.isNotEmpty(departments)) {
                for (SysDepartmentEntity department : departments) {
                    if (department != null && StrUtil.isNotBlank(department.getId()) && StrUtil.isNotBlank(department.getName())) {
                        deptIdNameMap.put(department.getId(), department.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("批量查询部门名称失败，deptIds：{}，错误：{}", deptIds, e.getMessage(), e);
        }
        
        return deptIdNameMap;
    }

    /**
     * 通过sku_no查询sku_id
     */
    private String getSkuIdBySkuNo(String skuNo) {
        try {
            // 调用商品服务根据sku_no查询sku_id
            List<SkuVO> skuVOS = plmTaskFeign.listBySkuNoList(Collections.singletonList(skuNo));
            if (CollectionUtils.isNotEmpty(skuVOS)) {
                return skuVOS.get(0).getSkuId();
            }
            log.warn("未找到sku_no：{} 对应的sku_id", skuNo);
            return null;
        } catch (Exception e) {
            log.error("查询sku_id失败，sku_no：{}，错误：{}", skuNo, e.getMessage(), e);
            return null;
        }
    }

    /**
    * 处理明细数据
    */
    private void handleDetailData(String mainId, List<SampleInitialLedgerDTO.DetailDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }

        // 获取所有SKU编码
        List<String> skuNoList = detailList.stream()
            .map(SampleInitialLedgerDTO.DetailDTO::getSkuNo)
            .distinct()
            .collect(Collectors.toList());

        // 通过feign获取产品信息
        Map<String, String> skuNoProductNameMap = getProductNameBySkuNoList(skuNoList);

        // 构建明细实体列表
        List<SampleInitialLedgerDetailEntity> detailEntities = new ArrayList<>();
        for (SampleInitialLedgerDTO.DetailDTO detailDTO : detailList) {
            SampleInitialLedgerDetailEntity detailEntity = new SampleInitialLedgerDetailEntity();
            detailEntity.setMainId(mainId);
            detailEntity.setSkuId(detailDTO.getSkuId());
            detailEntity.setSkuNo(detailDTO.getSkuNo());
            detailEntity.setQty(detailDTO.getQty());
            detailEntity.setRemark(detailDTO.getRemark());
            
            // 设置产品名称
            String productName = skuNoProductNameMap.get(detailDTO.getSkuNo());
            detailEntity.setProductName(productName);
            
            detailEntities.add(detailEntity);
        }

        // 批量保存明细
        boolean saveResult = sampleInitialLedgerDetailService.saveBatch(detailEntities);
        if (!saveResult) {
            throw new ServiceException("样品期初台账明细保存失败");
        }
    }

    /**
    * 处理明细数据（更新时使用）
    */
    private void handleDetailDataForUpdate(String mainId, List<SampleInitialLedgerDTO.DetailDTO> detailList) {
        // 获取现有的明细数据
        List<SampleInitialLedgerDetailEntity> existingDetails = sampleInitialLedgerDetailService.lambdaQuery()
            .eq(SampleInitialLedgerDetailEntity::getMainId, mainId)
            .eq(SampleInitialLedgerDetailEntity::getIsDeleted, false)
            .list();
        
        // 构建现有明细的Map，以SKU编码为key
        Map<String, SampleInitialLedgerDetailEntity> existingDetailMap = existingDetails.stream()
            .collect(Collectors.toMap(SampleInitialLedgerDetailEntity::getSkuNo, detail -> detail));
        
        // 构建新明细的Map，以SKU编码为key
        Map<String, SampleInitialLedgerDTO.DetailDTO> newDetailMap = detailList.stream()
            .collect(Collectors.toMap(SampleInitialLedgerDTO.DetailDTO::getSkuNo, detail -> detail));
        
        // 需要删除的明细（存在于现有数据中，但不在新数据中）
        List<String> toDeleteSkuNos = existingDetails.stream()
            .map(SampleInitialLedgerDetailEntity::getSkuNo)
            .filter(skuNo -> !newDetailMap.containsKey(skuNo))
            .collect(Collectors.toList());
        
        // 需要新增的明细（不存在于现有数据中，但在新数据中）
        List<SampleInitialLedgerDTO.DetailDTO> toAddDetails = detailList.stream()
            .filter(detail -> !existingDetailMap.containsKey(detail.getSkuNo()))
            .collect(Collectors.toList());
        
        // 需要更新的明细（既存在于现有数据中，也存在于新数据中）
        List<SampleInitialLedgerDetailEntity> toUpdateDetails = new ArrayList<>();
        List<SampleInitialLedgerDTO.DetailDTO> toUpdateDetailDTOs = new ArrayList<>();
        
        for (SampleInitialLedgerDTO.DetailDTO detailDTO : detailList) {
            if (existingDetailMap.containsKey(detailDTO.getSkuNo())) {
                SampleInitialLedgerDetailEntity existingDetail = existingDetailMap.get(detailDTO.getSkuNo());
                toUpdateDetails.add(existingDetail);
                toUpdateDetailDTOs.add(detailDTO);
            }
        }
        
        // 执行删除操作
        if (!toDeleteSkuNos.isEmpty()) {
            // 获取要删除的明细实体用于日志记录
            List<SampleInitialLedgerDetailEntity> toDeleteDetails = existingDetails.stream()
                .filter(detail -> toDeleteSkuNos.contains(detail.getSkuNo()))
                .collect(Collectors.toList());
            
            sampleInitialLedgerDetailService.lambdaUpdate()
                .eq(SampleInitialLedgerDetailEntity::getMainId, mainId)
                .in(SampleInitialLedgerDetailEntity::getSkuNo, toDeleteSkuNos)
                .remove();
            
            // 记录删除操作日志
            if (CollUtil.isNotEmpty(toDeleteDetails)) {
                List<Pair<String, String>> deletePairList = toDeleteDetails.stream()
                    .map(obj -> new Pair<>(mainId, obj.getSkuNo()))
                    .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), deletePairList, "编辑操作");
            }
            
            log.info("删除明细数据，mainId：{}，删除的SKU编码：{}", mainId, toDeleteSkuNos);
        }
        
        // 执行新增操作
        if (!toAddDetails.isEmpty()) {
            handleDetailData(mainId, toAddDetails);
            
            // 记录新增操作日志
            List<Pair<String, String>> addPairList = toAddDetails.stream()
                .map(obj -> new Pair<>(mainId, obj.getSkuNo()))
                .collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), addPairList, "编辑操作");
            
            log.info("新增明细数据，mainId：{}，新增数量：{}", mainId, toAddDetails.size());
        }
        
        // 执行更新操作
        if (!toUpdateDetails.isEmpty()) {
            // 获取产品名称映射
            List<String> skuNoList = toUpdateDetailDTOs.stream()
                .map(SampleInitialLedgerDTO.DetailDTO::getSkuNo)
                .distinct()
                .collect(Collectors.toList());
            Map<String, String> skuNoProductNameMap = getProductNameBySkuNoList(skuNoList);
            
            // 更新明细数据
            for (int i = 0; i < toUpdateDetails.size(); i++) {
                SampleInitialLedgerDetailEntity existingDetail = toUpdateDetails.get(i);
                SampleInitialLedgerDTO.DetailDTO detailDTO = toUpdateDetailDTOs.get(i);
                
                // 记录更新操作日志
                SampleInitialLedgerDetailEntity oldDetail = existingDetailMap.get(detailDTO.getSkuNo());
                if (Objects.nonNull(oldDetail)) {
                    operateLogService.addModuleOperateLogByObj(oldDetail, existingDetail, ModuleTypeEnum.SAMPLE_LEDGER_INIT.getCode(), mainId, String.format("编辑SKU【%s】", oldDetail.getSkuNo()));
                }
                
                existingDetail.setSkuId(detailDTO.getSkuId());
                existingDetail.setQty(detailDTO.getQty());
                existingDetail.setRemark(detailDTO.getRemark());
                
                // 更新产品名称
                String productName = skuNoProductNameMap.get(detailDTO.getSkuNo());
                if (StrUtil.isNotBlank(productName)) {
                    existingDetail.setProductName(productName);
                }
            }
            
            sampleInitialLedgerDetailService.updateBatchById(toUpdateDetails);
            log.info("更新明细数据，mainId：{}，更新数量：{}", mainId, toUpdateDetails.size());
        }
    }

    /**
    * 通过SKU编码列表获取产品名称
    */
    private Map<String, String> getProductNameBySkuNoList(List<String> skuNoList) {
        Map<String, String> skuNoProductNameMap = new HashMap<>();
        
        try {
            // 调用PLM服务获取产品信息
            List<ProductDetailEntity> productList = plmTaskFeign.listBySkuNos(skuNoList);
            
            if (CollUtil.isNotEmpty(productList)) {
                for (ProductDetailEntity product : productList) {
                    if (StrUtil.isNotBlank(product.getSkuNo()) && StrUtil.isNotBlank(product.getName())) {
                        skuNoProductNameMap.put(product.getSkuNo(), product.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取产品信息失败，skuNoList：{}，错误：{}", skuNoList, e.getMessage(), e);
        }
        
        return skuNoProductNameMap;
    }

    /**
     * 导入样品期初台账
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSampleInitialLedger(BaseDTO.ImportDTO dto) {
        // SKU信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e, (o1, o2) -> o1));
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        // 部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        SampleInitialLedgerExcelListener excelListenerUtil = new SampleInitialLedgerExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount(), deptList, map, userList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "样品期初台账错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO.class);
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> successList, List<String> errorNoList, List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        if (StringUtils.isBlank(importType)){
            //给个默认值
            importType=ImportTypeEnum.ADD.getCode();
        }

        if (CollUtil.isNotEmpty(errorNoList)) {
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            // 全部返回到错误列表
            List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        SampleInitialLedgerServiceImpl bean = ApplicationContextUtils.getBean(SampleInitialLedgerServiceImpl.class);

        // 按序号分组
        Map<String, List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO::getNo));
        for (Map.Entry<String, List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO>> entry : collect.entrySet()) {
            List<com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO> value = entry.getValue();
            com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO importMainDTO = value.get(0);
            SampleInitialLedgerDTO.AddDTO addDTO = new SampleInitialLedgerDTO.AddDTO();
            BeanMapperUtils.copy(importMainDTO, addDTO);
            
            List<SampleInitialLedgerDTO.DetailDTO> detailList = new ArrayList<>();
            for (com.erp.model.wms.dto.excel.SampleInitialLedgerImportExcelDTO importDTO : value) {
                SampleInitialLedgerDTO.DetailDTO detailDTO = new SampleInitialLedgerDTO.DetailDTO();
                detailDTO.setSkuId(importDTO.getSkuId());
                detailDTO.setSkuNo(importDTO.getSkuNo());
                detailDTO.setQty(Integer.valueOf(importDTO.getQty()));
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);

            if (ImportTypeEnum.ADD.getCode().equals(importType)) {
                bean.add(addDTO);
            }
        }
    }

    /**
     * 使用分布式锁校验样品台账数量
     * 期初台账单特殊逻辑：
     * - qty 本身可以是正数或负数
     * - 正数（+X）：审核时增加台账，不需要校验
     * - 负数（-X）：审核时扣减台账，需要校验是否足够扣减
     * - 没有反审核功能
     */
    private void validateSampleLedgerQtyWithLock(SampleInitialLedgerEntity entity, ApproveTypeEnum approveType) {
        // 获取样品期初台账单明细
        List<SampleInitialLedgerDetailEntity> detailList = sampleInitialLedgerDetailService.list(
            new LambdaQueryWrapper<SampleInitialLedgerDetailEntity>()
                .eq(SampleInitialLedgerDetailEntity::getMainId, entity.getId())
        );
        
        if (CollUtil.isEmpty(detailList)) {
            log.info("样品期初台账单明细为空，跳过数量校验，单据编号：{}", entity.getCode());
            return;
        }
        
        // 只筛选负数数量的明细（扣减操作）
        List<SampleInitialLedgerDetailEntity> negativeQtyDetails = detailList.stream()
                .filter(detail -> detail.getQty() != null && detail.getQty() < 0)
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(negativeQtyDetails)) {
            log.info("没有需要校验的负数数量明细，跳过数量校验，单据编号：{}", entity.getCode());
            return;
        }
        
        // 批量查询台账：收集所有需要查询的SKU ID
        List<String> skuIds = negativeQtyDetails.stream()
                .map(SampleInitialLedgerDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
        
        // 一次性批量查询所有台账
        SampleLedgerDTO.SearchDTO searchDTO = new SampleLedgerDTO.SearchDTO();
        searchDTO.setUserId(entity.getUserId());
        searchDTO.setUseUserId(entity.getUserId());
        searchDTO.setSkuIds(skuIds);
        
        List<SampleLedgerDTO.SkuAvailableQtyDTO> ledgerList = sampleLedgerService.listLedgerByUserId(searchDTO);
        
        // 构建 skuId -> ledgerId 的映射
        Map<String, String> skuIdToLedgerIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(ledgerList)) {
            skuIdToLedgerIdMap = ledgerList.stream()
                    .collect(Collectors.toMap(
                            SampleLedgerDTO.SkuAvailableQtyDTO::getSkuId,
                            SampleLedgerDTO.SkuAvailableQtyDTO::getSampleLedgerId,
                            (existing, replacement) -> existing
                    ));
        }
        
        // 收集需要校验的台账ID和数量
        List<String> sampleLedgerIds = new ArrayList<>();
        List<Integer> qtys = new ArrayList<>();
        List<String> skuNos = new ArrayList<>();
        
        for (SampleInitialLedgerDetailEntity detail : negativeQtyDetails) {
            String ledgerId = skuIdToLedgerIdMap.get(detail.getSkuId());
            if (StrUtil.isNotBlank(ledgerId)) {
                sampleLedgerIds.add(ledgerId);
                qtys.add(detail.getQty()); // 直接使用负数数量
                skuNos.add(detail.getSkuNo());
            } else {
                log.warn("未找到台账，SKU：{}，使用方：{}，单据编号：{}", 
                    detail.getSkuNo(), entity.getUserName(), entity.getCode());
                throw new ServiceException(StrUtil.format("SKU【{}】的样品台账不存在，无法审核负数数量", detail.getSkuNo()));
            }
        }
        
        if (CollUtil.isEmpty(sampleLedgerIds)) {
            log.info("没有需要校验的样品台账，跳过数量校验，单据编号：{}", entity.getCode());
            return;
        }
        
        log.info("开始校验样品期初台账单台账数量（负数明细），单据编号：{}，台账数量：{}", entity.getCode(), sampleLedgerIds.size());
        
        // 使用分布式锁进行数量校验
        sampleLedgerLockUtil.executeWithLock(sampleLedgerIds, () -> {
            sampleLedgerQtyValidator.validateQty(sampleLedgerIds, qtys, approveType, skuNos, sampleLedgerService::getLedgerQtyMap);
            log.info("样品期初台账单台账数量校验通过，单据编号：{}", entity.getCode());
            return null;
        });
    }

}
