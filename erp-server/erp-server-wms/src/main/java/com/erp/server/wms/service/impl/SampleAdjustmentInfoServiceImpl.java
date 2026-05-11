package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.utils.SampleDocumentAuditUtil;
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
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.SampleAdjustmentDetailDTO;
import com.erp.model.wms.dto.SampleAdjustmentInfoDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.dto.excel.SampleAdjustmentImportExcelDTO;
import com.erp.model.wms.entity.SampleAdjustmentDetailEntity;
import com.erp.model.wms.entity.SampleAdjustmentInfoEntity;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import com.erp.model.wms.enums.SampleAdjustmentTypeEnum;
import com.erp.model.wms.enums.SampleLedgerTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.listener.SampleAdjustmentExcelListener;
import com.erp.server.wms.mapper.SampleAdjustmentInfoMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_ADJUSTMENT_INFO_REPORT;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_SAMPLE_ADJUSTMENT_INFO;
/**
 * <p>
 * 样品调整单 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-11-14
 */
@Slf4j
@Service
public class SampleAdjustmentInfoServiceImpl extends SuperServiceImpl<SampleAdjustmentInfoMapper, SampleAdjustmentInfoEntity> implements SampleAdjustmentInfoService, SampleLedgerFlowBuilder {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SampleAdjustmentDetailService sampleAdjustmentDetailService;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private WmsAttachmentService attachmentService;
    @Autowired
    private SampleLedgerFlowService sampleLedgerFlowService;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private SampleDocumentAuditUtil sampleDocumentAuditUtil;
    @Autowired
    private SampleLedgerService sampleLedgerService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleAdjustmentInfoDTO.AddDTO addDTO) {
        SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity = new SampleAdjustmentInfoEntity();
        BeanMapperUtils.copy(addDTO, sampleAdjustmentInfoEntity);

        // 数据处理
        handleData(sampleAdjustmentInfoEntity);

        log.info("开始新增样品调整单");

        // 校验明细不能为空
        if (CollUtil.isEmpty(addDTO.getDetailList())) {
            throw new ServiceException("样品调整单明细不能为空");
        }

        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPTZ);
        sampleAdjustmentInfoEntity.setCode(code);
        boolean save = super.save(sampleAdjustmentInfoEntity);
        if(!save) {
            throw new ServiceException("样品调整单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品调整单" , sampleAdjustmentInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), "新增操作");
        
        // 处理明细数据
        List<SampleAdjustmentDetailDTO.AddDTO> detailList = addDTO.getDetailList();

        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(SampleAdjustmentDetailDTO.AddDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(com.common.core.enums.ApiError.PRODUCT_SKU_DUPLICATE);
        }

        List<SampleAdjustmentDetailEntity> sampleAdjustmentDetailEntities = BeanMapperUtils.copyList(SampleAdjustmentDetailEntity.class, detailList);
        List<String> skuIds = sampleAdjustmentDetailEntities.stream().map(SampleAdjustmentDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        
        for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : sampleAdjustmentDetailEntities) {
            sampleAdjustmentDetailEntity.setMainId(sampleAdjustmentInfoEntity.getId());

            // 计算差异数量 = 实际数量 - 台账数量
            Integer differenceQty = sampleAdjustmentDetailEntity.getActualQty() - sampleAdjustmentDetailEntity.getLedgerQty();
            sampleAdjustmentDetailEntity.setDifferenceQty(differenceQty);

            SkuVO skuVO = skuMap.getOrDefault(sampleAdjustmentDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleAdjustmentDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleAdjustmentDetailEntity.setProductName(skuVO.getSkuName());
            }

            // 校验调整类型与差异数量
            validateAdjustmentTypeAndDifferenceQty(sampleAdjustmentInfoEntity.getAdjustmentType(), differenceQty, sampleAdjustmentDetailEntity.getSkuNo());
        }

        sampleAdjustmentDetailService.saveBatch(sampleAdjustmentDetailEntities);
        
        //附件
        addAttachment(addDTO, sampleAdjustmentInfoEntity);

        return new BaseResultDTO.AddDTO(sampleAdjustmentInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO) {
        SampleAdjustmentInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "样品调整单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_UPDATE_STATUS_NOT_ALLOWED);
        }
        // 校验明细不能为空
        if (CollUtil.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("样品调整单明细不能为空");
        }
        SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity =  BeanMapperUtils.map(SampleAdjustmentInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleAdjustmentInfoEntity);
        log.info("编辑 开始修改样品调整单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleAdjustmentInfoEntity);
        if(!save) {
            throw new ServiceException("样品调整单保存失败");
        }
        
        // 记录主单操作日志
        log.info("编辑 开始记录样品调整单日志数据，单号：【{}】", old.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "样品调整单");
        operateLogService.addModuleOperateLogByObj(old, sampleAdjustmentInfoEntity, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), msg);

        //明细
        updateDetail(addOrUpdateDTO, sampleAdjustmentInfoEntity);

        //附件
        updateAttachment(addOrUpdateDTO, old);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SampleAdjustmentInfoDTO.ListDTO> paging(PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleAdjustmentInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleAdjustmentInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleAdjustmentInfoDTO.PagingParamDTO searchParam = new SampleAdjustmentInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        //待我审核
        //根据单据id查询审核流程
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            searchParam.setIds(ids);
        } else {
            searchParam.setIds(Arrays.asList("-1"));
        }

        List<SampleAdjustmentInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SampleAdjustmentInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new SampleAdjustmentInfoDTO.TabListDTO(status, "", 0));
            }
        });

        list.stream().forEach(e -> {
            if(Objects.equals(ApproveStatusEnum.APPROVE_ING.getCode(), e.getTabFlag())){
                e.setTabFlagName("待我审核");
            } else {
                e.setTabFlagName(ApproveStatusEnum.getName(e.getTabFlag()));
            }
        });
        // 修改为按照 ApproveStatusEnum 枚举声明顺序排序
        list.sort(Comparator.comparingInt(tabDto -> {
            ApproveStatusEnum statusEnum = ApproveStatusEnum.getByStatus(tabDto.getTabFlag());
            return statusEnum != null ? statusEnum.ordinal() : Integer.MAX_VALUE;
        }));
        // 在列表开头添加"全部"统计
        list.add(0, new SampleAdjustmentInfoDTO.TabListDTO("all","全部", 0));
        return list;
    }

    @Override
    public void exportList(SampleAdjustmentInfoDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品调整单导出", EXPORT_WMS_SAMPLE_ADJUSTMENT_INFO_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleAdjustmentInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品调整单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品调整单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动样品调整单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品调整单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleAdjustmentInfoDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleAdjustmentInfoDTO.UpdateDTO dto) {
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
            throw new ServiceException(ApiError.WF_REJECT_COMMENT_REQUIRED);
        }
        SampleAdjustmentInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.WF_APPROVE_ALLOWED_STATUS_ONLY);
        }
        // 使用分布式锁进行数量校验
        validateSampleLedgerQtyWithLock(entity, approveType);
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleAdjustmentInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.WF_APPROVE_FAILED);
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
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));

        // 检查单据是否已作废
        if (InvalidStatusEnum.VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException("已作废的样品调整单不支持反审核操作");
        }

        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 使用分布式锁进行数量校验（反审核时也需要校验）
        validateSampleLedgerQtyWithLock(entity, ApproveTypeEnum.DIS_APPROVE);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 记录台账流水（反审核）
        try {
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = buildFlow(entity.getId(), entity.getCode(), ApproveTypeEnum.DIS_APPROVE);
            if (flowDTO != null) {
                sampleLedgerFlowService.addSampleLedgerFlow(flowDTO);
                log.info("样品调整单反审核台账流水记录成功，单据编号：{}", entity.getCode());
            }
        } catch (Exception e) {
            log.error("样品调整单反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
            throw new ServiceException("样品调整单反审核台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleAdjustmentInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.BILL_REVERSE_APPROVAL_ALLOWED_APPROVED_ONLY);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        // TODO 删除明细数据（如果有明细数据的话）

        // 删除主单数据
        log.info("删除 开始删除样品调整单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品调整单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除样品调整单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.BILL_VOID_ALLOWED_STATUS_ONLY);
        }
        log.info("作废 开始修改样品调整单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleAdjustmentInfoEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单", remark);
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(ApproveDTO.CancelProcessDTO dto) {
        String id = dto.getId();
        SampleAdjustmentInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品调整单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.WF_REVOKE_PROCESS_ALLOWED_STATUS_ONLY);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品调整单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品调整单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setExecuteSystem(dto.getExecuteSystem());
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleAdjustmentInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        //审核完成也做台账数量校验
        validateSampleLedgerQtyWithLock(entity, ApproveTypeEnum.getByCode(dto.getType()));

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
                    log.info("样品调整单台账流水记录成功，单据编号：{}，审核类型：{}", entity.getCode(), approveType.getName());
                }
            } catch (Exception e) {
                log.error("样品调整单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage(), e);
                throw new ServiceException("样品调整单台账流水记录失败，单据编号：{}，错误：{}", entity.getCode(), e.getMessage());
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public SampleAdjustmentInfoDTO.ViewDTO view(String id) {
        SampleAdjustmentInfoDTO.ViewDTO viewDTO = new SampleAdjustmentInfoDTO.ViewDTO();
        SampleAdjustmentInfoEntity entity = super.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "样品调整单");
        }
        BeanMapperUtils.copy(entity, viewDTO);

        viewDTO.setApproveStatus(entity.getApproveStatus().getCode());
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setAdjustmentTypeName(SampleAdjustmentTypeEnum.getName(viewDTO.getAdjustmentType()));

        // 设置明细列表到ViewDTO中
        List<SampleAdjustmentDetailEntity> detailEntities = sampleAdjustmentDetailService.list(
            new LambdaQueryWrapper<SampleAdjustmentDetailEntity>().eq(SampleAdjustmentDetailEntity::getMainId, id)
        );
        
        // 提取所有样品台账ID，用于查询使用方信息
        List<String> sampleLedgerIds = detailEntities.stream()
                .map(SampleAdjustmentDetailEntity::getSampleLedgerId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询台账信息，获取使用方
        Map<String, String> ledgerIdToUserSideMap = new HashMap<>();
        if (CollUtil.isNotEmpty(sampleLedgerIds)) {
            try {
                List<SampleLedgerEntity> ledgerList = sampleLedgerService.listByIds(sampleLedgerIds);
                if (CollUtil.isNotEmpty(ledgerList)) {
                    ledgerIdToUserSideMap = ledgerList.stream()
                            .filter(ledger -> StrUtil.isNotBlank(ledger.getUseUserName()))
                            .collect(Collectors.toMap(
                                    SampleLedgerEntity::getId,
                                    SampleLedgerEntity::getUseUserName,
                                    (existing, replacement) -> existing
                            ));
                }
            } catch (Exception e) {
                log.warn("查询样品台账使用方信息失败：{}", e.getMessage());
            }
        }
        
        // 设置明细列表，并填充使用方信息
        Map<String, String> finalLedgerIdToUserSideMap = ledgerIdToUserSideMap;
        List<SampleAdjustmentDetailDTO.ViewDTO> detailList = detailEntities.stream()
            .map(detail -> {
                SampleAdjustmentDetailDTO.ViewDTO detailDTO = new SampleAdjustmentDetailDTO.ViewDTO();
                BeanMapperUtils.copy(detail, detailDTO);
                // 从台账中获取使用方信息
                String sampleLedgerId = detail.getSampleLedgerId();
                if (StrUtil.isNotBlank(sampleLedgerId)) {
                    String userSide = finalLedgerIdToUserSideMap.get(sampleLedgerId);
                    if (StrUtil.isNotBlank(userSide)) {
                        detailDTO.setUserSide(userSide);
                    }
                }
                return detailDTO;
            })
            .collect(Collectors.toList());
        viewDTO.setDetailList(detailList);

        // 查询相关的附件信息
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            // 分别提取附件名称和URL列表设置到返回对象中
            viewDTO.setAttachmentNameList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            viewDTO.setAttachmentUrlList(attachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
        }

        // 数据填充处理
        fillOne(viewDTO);
        return viewDTO;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SampleAdjustmentInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleAdjustmentInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        
        // 设置调整人名称
        if (StrUtil.isNotBlank(data.getAdjustmentUserId())) {
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(data.getAdjustmentUserId()));
            if (CollUtil.isNotEmpty(userList)) {
                FindUserDTO user = userList.get(0);
                data.setAdjustmentUserName(user.getUserName());
            }
        }
        
        // 设置调整部门名称
        if (StrUtil.isNotBlank(data.getAdjustmentDeptId())) {
            List<SysDepartmentEntity> departments = sysUserFeign.getDeptByIds(Arrays.asList(data.getAdjustmentDeptId()));
            if (CollUtil.isNotEmpty(departments)) {
                SysDepartmentEntity department = departments.get(0);
                if (department != null && StrUtil.isNotBlank(department.getName())) {
                    data.setAdjustmentDeptName(department.getName());
                }
            }
        }
        
        //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), data.getId()));
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
        this.lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getApproveUserId, userInfo.getUid())
            .set(SampleAdjustmentInfoEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
            .set(SampleAdjustmentInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleAdjustmentInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
            .set(SampleAdjustmentInfoEntity::getApproveUserId, "")
            .set(SampleAdjustmentInfoEntity::getApproveUserName, "")
            .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
            .set(SampleAdjustmentInfoEntity::getApproveTime, null)
            .update(new SampleAdjustmentInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleAdjustmentInfoEntity::getId, id)
                .set(SampleAdjustmentInfoEntity::getApproveUserId, "")
                .set(SampleAdjustmentInfoEntity::getApproveUserName, "")
                .set(SampleAdjustmentInfoEntity::getApproveStatus, approveStatus)
                .set(SampleAdjustmentInfoEntity::getApproveTime, null)
                .update(new SampleAdjustmentInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleAdjustmentInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.HTTP_UNKNOWN.getCode(), listApiResult.getMsg()));
            }
        }

        // 批量获取调整人ID和部门ID
        List<String> adjustmentUserIds = list.stream()
                .map(SampleAdjustmentInfoDTO.ListDTO::getAdjustmentUserId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        List<String> adjustmentDeptIds = list.stream()
                .map(SampleAdjustmentInfoDTO.ListDTO::getAdjustmentDeptId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<String, String> userIdNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(adjustmentUserIds)) {
            try {
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(adjustmentUserIds);
                if (CollUtil.isNotEmpty(userList)) {
                    userIdNameMap = userList.stream()
                            .filter(user -> StrUtil.isNotBlank(user.getUserId()) && StrUtil.isNotBlank(user.getUserName()))
                            .collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName, (v1, v2) -> v1));
                }
            } catch (Exception e) {
                log.warn("批量查询调整人信息失败，错误：{}", e.getMessage());
            }
        }
        
        // 批量查询部门信息
        Map<String, String> deptIdNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(adjustmentDeptIds)) {
            try {
                List<SysDepartmentEntity> departments = sysUserFeign.getDeptByIds(adjustmentDeptIds);
                if (CollUtil.isNotEmpty(departments)) {
                    deptIdNameMap = departments.stream()
                            .filter(dept -> dept != null && StrUtil.isNotBlank(dept.getId()) && StrUtil.isNotBlank(dept.getName()))
                            .collect(Collectors.toMap(SysDepartmentEntity::getId, SysDepartmentEntity::getName, (v1, v2) -> v1));
                }
            } catch (Exception e) {
                log.warn("批量查询调整部门信息失败，错误：{}", e.getMessage());
            }
        }

        // 属性赋值
        for(SampleAdjustmentInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setAdjustmentTypeName(SampleAdjustmentTypeEnum.getName(data.getAdjustmentType()));
            
            // 设置调整人名称
            if (StrUtil.isNotBlank(data.getAdjustmentUserId()) && userIdNameMap.containsKey(data.getAdjustmentUserId())) {
                data.setAdjustmentUserName(userIdNameMap.get(data.getAdjustmentUserId()));
            }
            
            // 设置调整部门名称
            if (StrUtil.isNotBlank(data.getAdjustmentDeptId()) && deptIdNameMap.containsKey(data.getAdjustmentDeptId())) {
                data.setAdjustmentDeptName(deptIdNameMap.get(data.getAdjustmentDeptId()));
            }
            
            //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                List<ProcessManagementDTO.CurApproveInfoDTO> curApproveList = listApiResult.getData().stream()
                    .filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName()))
                    .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(curApproveList)) {
                    String curApproveName = curApproveList.stream()
                        .map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName)
                        .collect(Collectors.joining(","));
                    if (StringUtils.isNotBlank(curApproveName)) {
                        data.setApproveUserName(curApproveName);
                    }
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleAdjustmentInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.BILL_SUBMIT_ALLOWED_STATUS_ONLY);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
        // 根据调整人ID查询并设置调整人名称，确保ID和名称的一致性
        if (StrUtil.isNotBlank(sampleAdjustmentInfoEntity.getAdjustmentUserId())) {
            try {
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(sampleAdjustmentInfoEntity.getAdjustmentUserId()));
                if (CollUtil.isNotEmpty(userList)) {
                    FindUserDTO user = userList.get(0);
                    if (user != null && StrUtil.isNotBlank(user.getUserName())) {
                        sampleAdjustmentInfoEntity.setAdjustmentUserName(user.getUserName());
                    }
                }
            } catch (Exception e) {
                log.warn("查询调整人信息失败，adjustmentUserId：{}，错误：{}", sampleAdjustmentInfoEntity.getAdjustmentUserId(), e.getMessage());
            }
        }
        
        // 根据调整部门ID查询并设置调整部门名称，确保ID和名称的一致性
        if (StrUtil.isNotBlank(sampleAdjustmentInfoEntity.getAdjustmentDeptId())) {
            try {
                List<SysDepartmentEntity> departments = sysUserFeign.getDeptByIds(Arrays.asList(sampleAdjustmentInfoEntity.getAdjustmentDeptId()));
                if (CollUtil.isNotEmpty(departments)) {
                    SysDepartmentEntity department = departments.get(0);
                    if (department != null && StrUtil.isNotBlank(department.getName())) {
                        sampleAdjustmentInfoEntity.setAdjustmentDeptName(department.getName());
                    }
                }
            } catch (Exception e) {
                log.warn("查询调整部门信息失败，adjustmentDeptId：{}，错误：{}", sampleAdjustmentInfoEntity.getAdjustmentDeptId(), e.getMessage());
            }
        }
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param sampleAdjustmentInfoEntity 样品调整单实体
     */
    private void addAttachment(SampleAdjustmentInfoDTO.AddDTO addDTO, SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<SampleAdjustmentInfoEntity> credentialClass = SampleAdjustmentInfoEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                WmsAttachmentEntity attachment = new WmsAttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(sampleAdjustmentInfoEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
    }

    /**
     * 更新附件信息
     * @param addOrUpdateDTO 包含附件URL和名称列表的更新数据传输对象
     * @param old 原样品调整单实体，用于获取业务ID
     */
    private void updateAttachment(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO, SampleAdjustmentInfoEntity old) {
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<WmsAttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessIds(Arrays.asList(old.getId()));
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<WmsAttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream().map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> add = attachmentUrlList.stream()
                    .filter(url -> !oldUrlList.contains(url))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(add)){
                Class<SampleAdjustmentInfoEntity> credentialClass = SampleAdjustmentInfoEntity.class;
                TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
                String type = tableName.value();
                List<WmsAttachmentEntity> batchAttachmentList = new ArrayList<>(10);
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!add.contains(attachmentUrlList.get(i))){
                        continue;
                    }
                    WmsAttachmentEntity addAttachment = new WmsAttachmentEntity();
                    addAttachment.setAttachUrl(attachmentUrlList.get(i));
                    addAttachment.setAttachName(attachmentNameList.get(i));
                    addAttachment.setBusinessId(old.getId());
                    addAttachment.setType(type);
                    batchAttachmentList.add(addAttachment);
                }

                if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                    attachmentService.saveBatch(batchAttachmentList);
                }
            }
        }
    }

    /**
     * 更新样品调整单的明细数据
     * <p>
     * 该方法根据传入的更新DTO对象，对样品调整单明细进行增删改操作，并记录相应的操作日志。
     * 具体包括：
     * - 删除旧明细中存在但新数据中不存在的记录；
     * - 新增新数据中ID为空的明细记录；
     * - 更新新数据中ID不为空的明细记录；
     * 同时为上述操作添加对应的操作日志。
     *
     * @param addOrUpdateDTO        包含待更新明细数据的DTO对象
     * @param sampleAdjustmentInfoEntity 主表实体对象
     */
    private void updateDetail(SampleAdjustmentInfoDTO.UpdateDTO addOrUpdateDTO, SampleAdjustmentInfoEntity sampleAdjustmentInfoEntity) {
        List<SampleAdjustmentDetailDTO.UpdateDTO> detailList = addOrUpdateDTO.getDetailList();

        //不允许重复添加
        long sampleLedgerIdCount = detailList.stream().map(SampleAdjustmentDetailDTO.UpdateDTO::getSampleLedgerId).distinct().count();
        if(sampleLedgerIdCount != detailList.size()){
            throw new ServiceException(com.common.core.enums.ApiError.PRODUCT_SKU_DUPLICATE);
        }

        List<SampleAdjustmentDetailEntity> oldList = sampleAdjustmentDetailService.listByMainId(sampleAdjustmentInfoEntity.getId());

        List<SampleAdjustmentDetailEntity> sampleAdjustmentDetailEntities = BeanMapperUtils.copyList(SampleAdjustmentDetailEntity.class, detailList);
        // 提取所有SKU编号，用于后续查询可用数量
        List<String> skuIds = sampleAdjustmentDetailEntities.stream().map(SampleAdjustmentDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        //sku信息
        List<SkuVO> skuVOS = plmTaskFeign.listSkuProductByIds(skuIds);
        Map<String, SkuVO> skuMap = skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(), (o1, o2) -> o1));
        for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : sampleAdjustmentDetailEntities) {
            sampleAdjustmentDetailEntity.setMainId(sampleAdjustmentInfoEntity.getId());

            // 计算差异数量 = 实际数量 - 台账数量
            Integer differenceQty = sampleAdjustmentDetailEntity.getActualQty() - sampleAdjustmentDetailEntity.getLedgerQty();
            sampleAdjustmentDetailEntity.setDifferenceQty(differenceQty);

            SkuVO skuVO = skuMap.getOrDefault(sampleAdjustmentDetailEntity.getSkuId(), null);
            if(Objects.nonNull(skuVO)){
                sampleAdjustmentDetailEntity.setSkuNo(skuVO.getSkuNo());
                sampleAdjustmentDetailEntity.setProductName(skuVO.getSkuName());
            }

            // 校验调整类型与差异数量
            validateAdjustmentTypeAndDifferenceQty(sampleAdjustmentInfoEntity.getAdjustmentType(), differenceQty, sampleAdjustmentDetailEntity.getSkuNo());
        }

        if(CollUtil.isNotEmpty(oldList)){
            List<String> detailIds = detailList.stream().map(SampleAdjustmentDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            // 处理删除的数据
            List<SampleAdjustmentDetailEntity> remove = oldList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());
            if(CollUtil.isNotEmpty(remove)){
                sampleAdjustmentDetailService.removeByIds(remove.stream().map(SampleAdjustmentDetailEntity::getId).collect(Collectors.toList()));
                //添加日志
                List<Pair<String, String>> removePairList = remove.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog("删除SKU【%s】", ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), removePairList, "编辑操作");
            }
        }
        //处理需要新增的数据
        List<SampleAdjustmentDetailEntity> addList = sampleAdjustmentDetailEntities.stream().filter(e -> StringUtils.isBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addList)){
            sampleAdjustmentDetailService.saveBatch(addList);

            //添加日志
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加SKU【%s】", ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), addPairList, "编辑操作");
        }
        //处理需要更新的数据
        List<SampleAdjustmentDetailEntity> updateList = sampleAdjustmentDetailEntities.stream().filter(e -> StringUtils.isNotBlank(e.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(updateList)){
            sampleAdjustmentDetailService.updateBatchById(updateList);
            //添加日志
            for (SampleAdjustmentDetailEntity sampleAdjustmentDetailEntity : updateList) {
                SampleAdjustmentDetailEntity oldDetail = oldList.stream().filter(e -> Objects.equals(e.getId(), sampleAdjustmentDetailEntity.getId())).findFirst().orElse(null);
                if(Objects.nonNull(oldDetail)){
                    operateLogService.addModuleOperateLogByObj(oldDetail, sampleAdjustmentDetailEntity, ModuleTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode(), sampleAdjustmentInfoEntity.getId(), String.format("编辑SKU【%s】",oldDetail.getSkuNo()));
                }
            }
        }
    }

    // ==================== 台账流水构建器实现 ====================

    @Override
    public String getSupportedSourceType() {
        return SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode();
    }

    @Override
    public SampleLedgerFlowDTO.AddFlowDTO buildFlow(String sourceId, String sourceCode, ApproveTypeEnum approveType) {
        try {
            // 获取样品调整单主表信息
            SampleAdjustmentInfoEntity entity = this.getById(sourceId);
            if (entity == null) {
                log.error("获取样品调整单失败，sourceId：{}", sourceId);
                return null;
            }

            // 获取样品调整单明细
            List<SampleAdjustmentDetailEntity> detailList = sampleAdjustmentDetailService.list(
                new LambdaQueryWrapper<SampleAdjustmentDetailEntity>()
                    .eq(SampleAdjustmentDetailEntity::getMainId, sourceId)
            );

            if (detailList.isEmpty()) {
                log.warn("样品调整单明细为空，sourceId：{}", sourceId);
                return null;
            }

            // 构建流水明细
            List<SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO> flowDetails = new ArrayList<>();
            for (SampleAdjustmentDetailEntity detail : detailList) {
                // 计算数量：审核为差异数量，反审核为-差异数量
                Integer qty = calculateQty(detail.getDifferenceQty(), approveType);
                
                SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO flowDetail = new SampleLedgerFlowDTO.AddFlowDTO.FlowDetailDTO();
                flowDetail.setSourceDetailId(detail.getId());
                flowDetail.setSkuNo(detail.getSkuNo());
                flowDetail.setSkuId(detail.getSkuId());
                flowDetail.setProductName(detail.getProductName());
                flowDetail.setQty(qty);
                // 设置样品台账ID，用于查询使用方信息
                flowDetail.setSampleLedgerId(detail.getSampleLedgerId());
                flowDetails.add(flowDetail);
            }

            // 构建流水主表数据
            SampleLedgerFlowDTO.AddFlowDTO flowDTO = new SampleLedgerFlowDTO.AddFlowDTO();
            flowDTO.setSourceType(getSupportedSourceType());
            flowDTO.setApproveType(approveType.getStatus());
            flowDTO.setOperateTime(LocalDateTime.now());
            flowDTO.setBillDate(entity.getAdjustmentDate());
            flowDTO.setSourceName("样品调整单");
            flowDTO.setSourceCode(sourceCode);
            flowDTO.setSourceId(sourceId);
            // 使用方信息将通过每个明细的sampleLedgerId在SampleLedgerFlowServiceImpl中查询获取
            flowDTO.setUserId(entity.getAdjustmentUserId());
            flowDTO.setUserName(entity.getAdjustmentUserName());
            flowDTO.setDeptId(entity.getAdjustmentDeptId());
            // 根据部门ID查询部门名称
            flowDTO.setDeptName(getDeptNameById(entity.getAdjustmentDeptId()));
            flowDTO.setDetailList(flowDetails);

            return flowDTO;
        } catch (Exception e) {
            log.error("构建样品调整单台账流水失败，sourceId：{}，错误：{}", sourceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 计算数量：审核为差异数量，反审核为-差异数量
     * 调整单使用差异数量（实际数量 - 台账数量）来更新台账
     */
    @Override
    public Integer calculateQty(Integer originalQty, ApproveTypeEnum approveType) {
        if (originalQty == null) {
            return 0;
        }
        
        if (ApproveTypeEnum.PASS.equals(approveType)) {
            // 审核：使用差异数量（正数表示增加，负数表示减少）
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
     * 使用分布式锁进行样品台账数量校验
     * 实现一锁二判三放行的逻辑
     *
     * @param entity 样品调整单实体
     * @param approveType 审核类型
     */
    private void validateSampleLedgerQtyWithLock(SampleAdjustmentInfoEntity entity, ApproveTypeEnum approveType) {
        // 获取样品调整单明细
        List<SampleAdjustmentDetailEntity> detailList = sampleAdjustmentDetailService.list(
            new LambdaQueryWrapper<SampleAdjustmentDetailEntity>()
                .eq(SampleAdjustmentDetailEntity::getMainId, entity.getId())
        );

        // 使用通用工具类进行数量校验
        // 调整单：审核时使用差异数量（正数增加，负数减少），反审核时取反
        sampleDocumentAuditUtil.validateSampleDocumentQty(
            entity.getCode(),
            "样品调整单",
            detailList,
            approveType,
            sampleLedgerService::getLedgerQtyMap
        );
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("导入样品调整单", IMPORT_WMS_SAMPLE_ADJUSTMENT_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSampleAdjustment(BaseDTO.ImportDTO dto) {
        //sku信息
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        Map<String, SkuVO> map = skuList.stream().collect(Collectors.toMap(SkuVO::getSkuNo, e -> e, (o1, o2) -> o1));
        //用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if (Objects.nonNull(findUserDTO)) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }
        SampleAdjustmentExcelListener excelListenerUtil = new SampleAdjustmentExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount(), deptList, map, userList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SampleAdjustmentImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<SampleAdjustmentImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            //排序
            List<SampleAdjustmentImportExcelDTO> sortedErrorList = errorList.stream()
                    .filter(e -> e.getNo() != null && !e.getNo().isEmpty()) // 过滤掉 null 或空字符串
                    .sorted(Comparator.comparingInt(e -> {
                        try {
                            return Integer.parseInt(e.getNo());
                        } catch (Exception ex) {
                            // 处理非数字字符串，可以返回一个默认值
                            return 0;
                        }
                    }))
                    .collect(Collectors.toList());
            String fileName = "样品调整单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", sortedErrorList, SampleAdjustmentImportExcelDTO.class);
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

    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<SampleAdjustmentImportExcelDTO> successList, List<String> errorNoList, List<SampleAdjustmentImportExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        if (CollUtil.isNotEmpty(errorNoList)) {
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getNo()) && !errorNoList.contains(e.getNo())).collect(Collectors.toList());

            //全部返回到错误列表
            List<SampleAdjustmentImportExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getNo()) || errorNoList.contains(e.getNo())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        SampleAdjustmentInfoServiceImpl bean = ApplicationContextUtils.getBean(SampleAdjustmentInfoServiceImpl.class);

        // 批量收集所有调整人ID和调整部门ID（去重）
        List<String> adjustmentUserIds = successList.stream()
                .map(SampleAdjustmentImportExcelDTO::getAdjustmentUserId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        List<String> adjustmentDeptIds = successList.stream()
                .map(SampleAdjustmentImportExcelDTO::getAdjustmentDeptId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询所有用户信息并构建Map
        Map<String, String> userIdNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(adjustmentUserIds)) {
            try {
                List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(adjustmentUserIds);
                if (CollUtil.isNotEmpty(userList)) {
                    userIdNameMap = userList.stream()
                            .filter(user -> user != null && StrUtil.isNotBlank(user.getUserId()) && StrUtil.isNotBlank(user.getUserName()))
                            .collect(Collectors.toMap(FindUserDTO::getUserId, FindUserDTO::getUserName, (v1, v2) -> v1));
                }
            } catch (Exception e) {
                log.warn("导入时批量查询调整人信息失败，错误：{}", e.getMessage());
            }
        }

        // 批量查询所有部门信息并构建Map
        Map<String, String> deptIdNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(adjustmentDeptIds)) {
            try {
                List<SysDepartmentEntity> departments = sysUserFeign.getDeptByIds(adjustmentDeptIds);
                if (CollUtil.isNotEmpty(departments)) {
                    deptIdNameMap = departments.stream()
                            .filter(dept -> dept != null && StrUtil.isNotBlank(dept.getId()) && StrUtil.isNotBlank(dept.getName()))
                            .collect(Collectors.toMap(SysDepartmentEntity::getId, SysDepartmentEntity::getName, (v1, v2) -> v1));
                }
            } catch (Exception e) {
                log.warn("导入时批量查询调整部门信息失败，错误：{}", e.getMessage());
            }
        }

        //按序号分组
        Map<String, List<SampleAdjustmentImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(SampleAdjustmentImportExcelDTO::getNo));
        for (Map.Entry<String, List<SampleAdjustmentImportExcelDTO>> entry : collect.entrySet()) {
            List<SampleAdjustmentImportExcelDTO> value = entry.getValue();
            SampleAdjustmentImportExcelDTO importMainDTO = value.get(0);

            List<String> skuIds = value.stream().map(SampleAdjustmentImportExcelDTO::getSkuId).collect(Collectors.toList());
            // 构造查询条件：根据调整人ID、使用方ID和SKU列表查询样品台账
            com.erp.model.wms.dto.SampleLedgerDTO.SearchDTO searchDTO = new com.erp.model.wms.dto.SampleLedgerDTO.SearchDTO();
            searchDTO.setUserId(importMainDTO.getAdjustmentUserId());
            searchDTO.setUseUserId(importMainDTO.getUseUserId());
            searchDTO.setSkuIds(skuIds);
            searchDTO.setType( SampleLedgerTypeEnum.ADJUSTMENT.getCode());
            List<com.erp.model.wms.dto.SampleLedgerDTO.SkuAvailableQtyDTO> skuAvailableQtyDTOS = sampleLedgerService.listLedgerByUserId(searchDTO);

            Boolean isAdd = Boolean.TRUE;
            List<SampleAdjustmentDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SampleAdjustmentImportExcelDTO importDTO : value) {
                String errorMsg = importDTO.getErrorMsg();
                int indexTemp = 1;
                if (StringUtils.isNotBlank(errorMsg)) {
                    String[] split = errorMsg.split("；");
                    indexTemp = split.length + 1;
                }

                //关联台账：通过调整人 + 使用方 + SKU 查询
                if (CollUtil.isEmpty(skuAvailableQtyDTOS)) {
                    errorMsg = errorMsg + indexTemp + "、" + ApiError.SAMPLE_LEDGER_NOT_EXIST.getMsg() + "；";
                } else {
                    com.erp.model.wms.dto.SampleLedgerDTO.SkuAvailableQtyDTO skuAvailableQtyDTO = skuAvailableQtyDTOS.stream()
                            .filter(e -> e.getSkuId().equals(importDTO.getSkuId()) && 
                                    (StringUtils.isNotBlank(importDTO.getUseUserId()) && e.getUseUserId() != null && e.getUseUserId().equals(importDTO.getUseUserId())))
                            .findFirst().orElse(null);
                    if (Objects.isNull(skuAvailableQtyDTO)) {
                        errorMsg = errorMsg + indexTemp + "、" + ApiError.SAMPLE_LEDGER_NOT_EXIST.getMsg() + "；";
                    } else {
                        importDTO.setSampleLedgerId(skuAvailableQtyDTO.getSampleLedgerId());
                        // 设置台账数量
                        importDTO.setLedgerQtyInt(skuAvailableQtyDTO.getLedgerQty() != null ? skuAvailableQtyDTO.getLedgerQty() : 0);
                        // 计算差异数量：实际数量 - 台账数量
                        if (importDTO.getActualQtyInt() != null && importDTO.getLedgerQtyInt() != null) {
                            importDTO.setDifferenceQtyInt(importDTO.getActualQtyInt() - importDTO.getLedgerQtyInt());
                        }
                    }
                }
                if (StringUtils.isNotBlank(errorMsg)) {
                    isAdd = Boolean.FALSE;
                    importDTO.setErrorMsg(errorMsg);
                } else {
                    SampleAdjustmentDetailDTO.AddDTO detailDTO = new SampleAdjustmentDetailDTO.AddDTO();
                    BeanMapperUtils.copy(importDTO, detailDTO);
                    detailDTO.setMainId(""); // 将在保存主单后设置
                    detailDTO.setSkuId(importDTO.getSkuId());
                    detailDTO.setSkuNo(importDTO.getSkuNo());
                    detailDTO.setProductName(importDTO.getProductName());
                    detailDTO.setLedgerQty(importDTO.getLedgerQtyInt());
                    detailDTO.setActualQty(importDTO.getActualQtyInt());
                    detailDTO.setDifferenceQty(importDTO.getDifferenceQtyInt());
                    detailDTO.setSampleLedgerId(importDTO.getSampleLedgerId());
                    //明细备注
                    detailDTO.setRemark(importDTO.getDetailRemark());
                    detailList.add(detailDTO);
                }
            }
            if (!isAdd) {
                errorList2.addAll(value);
            } else {
                SampleAdjustmentInfoDTO.AddDTO addDTO = new SampleAdjustmentInfoDTO.AddDTO();
                BeanMapperUtils.copy(importMainDTO, addDTO);
                addDTO.setAdjustmentUserId(importMainDTO.getAdjustmentUserId());
                // 从Map中获取调整人名称，确保ID和名称的一致性（不直接使用导入数据中的名称）
                if (StrUtil.isNotBlank(importMainDTO.getAdjustmentUserId()) && userIdNameMap.containsKey(importMainDTO.getAdjustmentUserId())) {
                    addDTO.setAdjustmentUserName(userIdNameMap.get(importMainDTO.getAdjustmentUserId()));
                }
                addDTO.setAdjustmentDeptId(importMainDTO.getAdjustmentDeptId());
                // 从Map中获取调整部门名称，确保ID和名称的一致性（不直接使用导入数据中的名称）
                if (StrUtil.isNotBlank(importMainDTO.getAdjustmentDeptId()) && deptIdNameMap.containsKey(importMainDTO.getAdjustmentDeptId())) {
                    addDTO.setAdjustmentDeptName(deptIdNameMap.get(importMainDTO.getAdjustmentDeptId()));
                }
                addDTO.setAdjustmentDate(importMainDTO.getAdjustmentDate());
                addDTO.setAdjustmentType(importMainDTO.getAdjustmentType());
                addDTO.setRemark(importMainDTO.getRemark());
                addDTO.setDetailList(detailList);

                bean.add(addDTO);
            }
        }
    }

    /**
     * 下载模板
     * @author wuhaotian
     * @date: 2025-11-14
     * @param response
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String standardPath = "classpath:excel/sampleAdjustmentInfoTemplate.xlsx";
        String standardExcelName = "sampleAdjustmentInfoTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
    }

    /**
     * 校验调整类型与差异数量
     * 实际数量不能等于台账数量（差异数量不能为0）
     * 调整类型=盘盈，差异数量必须大于0
     * 调整类型=盘亏，差异数量必须小于0
     * 调整类型=其他，不对差异数量进行校验
     *
     * @param adjustmentType 调整类型
     * @param differenceQty 差异数量
     * @param skuNo SKU编号（用于错误提示）
     */
    private void validateAdjustmentTypeAndDifferenceQty(String adjustmentType, Integer differenceQty, String skuNo) {
        if (differenceQty == null) {
            return;
        }

        String skuInfo = StrUtil.isNotBlank(skuNo) ? String.format("SKU【%s】", skuNo) : "";

        // 实际数量不能等于台账数量（差异数量不能为0）
        if (differenceQty == 0) {
            throw new ServiceException(String.format("%s实际数量不能等于台账数量", skuInfo));
        }

        if (StrUtil.isBlank(adjustmentType)) {
            return;
        }

        SampleAdjustmentTypeEnum adjustmentTypeEnum = SampleAdjustmentTypeEnum.getByCode(adjustmentType);
        if (adjustmentTypeEnum == null) {
            return;
        }

        if (SampleAdjustmentTypeEnum.INVENTORY_PROFIT.equals(adjustmentTypeEnum)) {
            // 盘盈：差异数量必须大于0
            if (differenceQty <= 0) {
                throw new ServiceException(String.format("%s调整类型为盘盈时，差异数量必须大于0", skuInfo));
            }
        } else if (SampleAdjustmentTypeEnum.INVENTORY_LOSS.equals(adjustmentTypeEnum)) {
            // 盘亏：差异数量必须小于0
            if (differenceQty >= 0) {
                throw new ServiceException(String.format("%s调整类型为盘亏时，差异数量必须小于0", skuInfo));
            }
        }
        // 其他类型：不进行校验
    }

    @Override
    public List<SampleAdjustmentInfoDTO.TabListDTO> tabListApp(PermissionsDTO param) {
        SampleAdjustmentInfoDTO.PagingParamDTO searchParam = new SampleAdjustmentInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        //待我审核
        //根据单据id查询审核流程
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.SAMPLE_ADJUSTMENT_INFO.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(UserContext.getNonLoginUser().getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            searchParam.setIds(ids);
        } else {
            searchParam.setIds(Arrays.asList("-1"));
        }

        List<SampleAdjustmentInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 移动端特殊处理：合并待提交和不通过
        List<SampleAdjustmentInfoDTO.TabListDTO> appList = new ArrayList<>();
        
        // 计算待提交/不通过的总数
        int waitSubmitCount = 0;
        int rejectCount = 0;
        SampleAdjustmentInfoDTO.TabListDTO waitSubmitItem = null;
        SampleAdjustmentInfoDTO.TabListDTO rejectItem = null;
        
        for (SampleAdjustmentInfoDTO.TabListDTO item : list) {
            if (ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(item.getTabFlag())) {
                waitSubmitCount = item.getCount();
                waitSubmitItem = item;
            } else if (ApproveStatusEnum.REJECT.getCode().equals(item.getTabFlag())) {
                rejectCount = item.getCount();
                rejectItem = item;
            }
        }
        
        // 创建合并后的待提交/不通过标签
        if (waitSubmitItem != null || rejectItem != null) {
            SampleAdjustmentInfoDTO.TabListDTO mergedItem = new SampleAdjustmentInfoDTO.TabListDTO();
            mergedItem.setTabFlag("waitSubmitOrReject");
            mergedItem.setTabFlagName("待提交/不通过");
            mergedItem.setCount(waitSubmitCount + rejectCount);
            appList.add(mergedItem);
        }
        
        // 添加其他标签（待我审核、已审核）
        for (SampleAdjustmentInfoDTO.TabListDTO item : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(item.getTabFlag()) 
                && !ApproveStatusEnum.REJECT.getCode().equals(item.getTabFlag())) {
                if (Objects.equals(ApproveStatusEnum.APPROVE_ING.getCode(), item.getTabFlag())) {
                    item.setTabFlagName("待我审核");
                } else if (Objects.equals(ApproveStatusEnum.APPROVE.getCode(), item.getTabFlag())) {
                    item.setTabFlagName("已审核");
                }
                appList.add(item);
            }
        }

        // 按照移动端指定顺序排序：待提交/不通过、审核中、已审核
        List<String> orderList = Arrays.asList("waitSubmitOrReject", "approveIng", "approve");
        appList.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });

        return appList;
    }

    @Override
    public PagingVO<SampleAdjustmentInfoDTO.ListDTO> pagingApp(PagingDTO<SampleAdjustmentInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleAdjustmentInfoDTO.ListDTO> pageData = this.baseMapper.pagingApp(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }
}
