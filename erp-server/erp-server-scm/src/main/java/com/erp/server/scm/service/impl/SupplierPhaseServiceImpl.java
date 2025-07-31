package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.SupplierPhaseDTO;
import com.erp.model.scm.dto.excel.SupplierPhaseExportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.scm.enums.SupplierPhaseTabFlagEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.SupplierPhaseMapper;
import com.erp.server.scm.query.SupplierPhaseQueryHandler;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.SupplierGradeService;
import com.erp.server.scm.service.SupplierPhaseService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_DYNAMIC_SUPPLIER_PHASE;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_PHASE;

/**
 * <p>
 * 供应商升降级 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Slf4j
@Service
public class SupplierPhaseServiceImpl extends SuperServiceImpl<SupplierPhaseMapper, SupplierPhaseEntity> implements SupplierPhaseService {


    @Resource
    private SupplierService supplierService;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private SupplierGradeService supplierGradeService;

    @Resource
    private SupplierPhaseQueryHandler supplierPhaseQueryHandler;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    @Qualifier("tabExecutorPool")
    private ExecutorService tabExecutorPool;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    /**
     * 添加供应商阶段
     *
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     * @author yl
     * @date 2023-03-23 12:20
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SupplierPhaseDTO.AddDTO dto) {
        SupplierPhaseEntity entity = new SupplierPhaseEntity();
        String supplierId = dto.getSupplierId();
        SupplierEntity supplier = supplierService.getById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        //供应商的现阶段
        String phase = supplier.getPhase().getPhase();
        if (!phase.equals(dto.getCurrentPhase())) {
            throw new ServiceException(ApiError.ERROR_98020);
        }
        BeanUtil.copyProperties(dto, entity, dto.getCurrentPhase(), dto.getTargetPhase());
        String id = IdWorker.getIdStr();
        entity.setId(id);
        Boolean result = this.save(entity);
        if (result) {
            Class<SupplierPhaseEntity> credentialClass = SupplierPhaseEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            //保存附件信息
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), type, id);
            return id;
        }
        return "";
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 16:34
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SupplierPhaseDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        BatchResultDTO submit = this.submit(id);
        return submit.getSuccess();
    }


    /**
     * 供应商阶段 提交审核
     *
     * @param id
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 16:50
     */
    @Override
    public BatchResultDTO submit(String id) {
        SupplierPhaseEntity entity = this.getById(id);
        log.info("提交 开始启动试产申请流程，id=：【{}】", entity.getId());

        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        // 待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getCode().equals(entity.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("提交 开始修改供应商阶段单状态数据，id：【{}】", id);
        //更新审核状态
        updateApproveStatus(Collections.singletonList(entity), ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动供应商阶段单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        return BatchResultDTO.success(entity.getId(), entity.getTargetPhase(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 流程启动
     * @author will
     * @date 2025/7/31 16:57
     * @param entity
     * @return void
     */
    public void startProcess(SupplierPhaseEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getTargetPhase());
        startDTO.setBusinessKey(SourceTypeEnum.SUPPLIER_PHASE.getCode());
        startDTO.setBusinessName(entity.getTargetPhase());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 供应商详情阶段
     *
     * @param supplierPhaseId
     * @return com.erp.model.scm.dto.SupplierPhaseDTO.UpdateDTO
     * @author yl
     * @date 2023-03-23 17:12
     */
    @Override
    public SupplierPhaseDTO.UpdateDTO view(String supplierPhaseId) {
        SupplierPhaseEntity phase = this.getById(supplierPhaseId);
        if (Objects.isNull(phase)) {
            throw new ServiceException(ApiError.ERROR_98018);
        }
        SupplierPhaseDTO.UpdateDTO dto = new SupplierPhaseDTO.UpdateDTO();
        BeanMapper.copy(phase, dto);
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(supplierPhaseId));
        List<String> urlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
        List<String> nameList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
        dto.setAttachmentUrlList(urlList);
        dto.setAttachmentNameList(nameList);
        return dto;
    }


    /**
     * 更改供应商阶段
     *
     * @param dto
     * @return com.erp.model.scm.entity.SupplierPhaseEntity
     * @author yl
     * @date 2023-03-23 17:23
     */
    @Override
    public String updateSupplierPhase(SupplierPhaseDTO.UpdateDTO dto) {
        String id = dto.getId();
        SupplierPhaseEntity phase = this.getById(id);
        if (Objects.isNull(phase)) {
            throw new ServiceException(ApiError.ERROR_98018);
        }

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        //只有待提交 和审核不通过 才能编辑
        if (!statusList.contains(phase.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98021);
        }

        //目标阶段
        String targetPhase = dto.getTargetPhase();

        phase.setTargetPhase(targetPhase);
        phase.setDescription(dto.getDescription());
        phase.setTargetGradeId(dto.getTargetGradeId());
        Boolean result = this.updateById(phase);
        if (result) {
            Class<SupplierPhaseEntity> credentialClass = SupplierPhaseEntity.class;
            TableName tableName = credentialClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String attachmentType = tableName.value();
            attachmentService.batchSave(dto.getAttachmentUrlList(), dto.getAttachmentNameList(), attachmentType, id);
            return id;
        }
        return "";
    }

    /**
     * 审核供应商阶段
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:43
     */
    @Override
    public BatchResultDTO approve(SupplierPhaseEntity entity,ApproveOneDTO dto) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        approveProcess(entity, dto);
        log.info("其他出库单【{}】，ids=【{}】", ApproveTypeEnum.getName(dto.getType()), JSONUtil.toJsonStr(dto.getId()));
        return BatchResultDTO.success(entity.getId(),entity.getTargetPhase(),"操作成功");
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(SupplierPhaseEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SUPPLIER_PHASE.getCode());
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SupplierPhaseEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //更新单据状态
        updateApproveStatus(Collections.singletonList(entity), approveStatus.getCode());

        //通过后更改供应商的阶段
        supplierService.updatePhase(Collections.singletonList(entity));
        return Boolean.TRUE;
    }


    /**
     * 删除供应商阶段
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:54
     */
    @Override
    public Boolean deleteByIds(List<String> ids) {
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(waitSubmitStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        return this.removeByIds(ids);
    }


    /**
     * 取消流程
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:58
     */
    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String approveIngStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !s.getApproveStatus().equals(approveIngStatus)).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //TODO 这里要调用工作流服务取消流程

        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        Boolean result = this.updateApproveStatus(list, waitSubmitStatus);

        return result;
    }


    /**
     * 分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierPhaseDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-23 18:15
     */
    @Override
    public PagingVO<SupplierPhaseDTO.PagingViewDTO> paging(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        SupplierPhaseDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params, null);
        List<SupplierPhaseDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //获取供应商等级
        List<SupplierGradeEntity> supplierGradeList = supplierGradeService.list();
        Map<String, String> gradeMap = CollUtil.isEmpty(supplierGradeList) ? new HashMap<>() : supplierGradeList.stream().collect(Collectors.toMap(SupplierGradeEntity::getId, SupplierGradeEntity::getName));

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.PURCHASE_ORDER.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        for (SupplierPhaseDTO.PagingViewDTO item : list) {
            //当前阶段
            String currentPhase = item.getCurrentPhase();
            String currentPhaseName = SupplierPhaseEnum.getPhaseName(currentPhase);
            item.setCurrentPhaseName(currentPhaseName);

            //目标阶段
            String targetPhase = item.getTargetPhase();
            String targetPhaseName = SupplierPhaseEnum.getPhaseName(targetPhase);
            item.setTargetPhaseName(targetPhaseName);
            String approveStatus = item.getApproveStatus();
            item.setApproveStatusName(ApproveStatusEnum.getName(approveStatus));

            //当前等级
            item.setCurrentGradeName(gradeMap.get(item.getCurrentGradeId()));
            //目标等级
            item.setTargetGradeName(gradeMap.get(item.getTargetGradeId()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(item.getId()) && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                item.setApproveUserName(curApprove);
            }

        }
        return new PagingVO(pageData);
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-29 16:20
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(SupplierPhaseDTO.UpdateDTO dto) {
        String id = this.updateSupplierPhase(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        BatchResultDTO submit = this.submit(id);
        return submit.getSuccess();
    }


    /**
     * 更改状态
     *
     * @param list
     * @param approveStatus
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 17:50
     */
    private Boolean updateApproveStatus(List<SupplierPhaseEntity> list, String approveStatus) {
        if (CollUtil.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<String> ids = list.stream().map(SupplierPhaseEntity::getId).distinct().collect(Collectors.toList());
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        return this.lambdaUpdate().in(SupplierPhaseEntity::getId, ids)
                .set(SupplierPhaseEntity::getApproveUserId, userInfo.getUid())
                .set(SupplierPhaseEntity::getApproveUserName, userInfo.getUserName())
                .set(SupplierPhaseEntity::getApproveStatus, approveStatus)
                .set(SupplierPhaseEntity::getApproveTime, LocalDateTime.now())
                .update();
    }


    @Override
    public List<SupplierPhaseDTO.TabFlagDTO> tabList(PermissionsDTO dto) {
        SupplierPhaseTabFlagEnum[] values = SupplierPhaseTabFlagEnum.values();
        List<Future<SupplierPhaseDTO.TabFlagDTO>> futureList = new ArrayList<>();
        List<SupplierPhaseDTO.TabFlagDTO> list = new ArrayList<>();
        for (SupplierPhaseTabFlagEnum item : values) {
            Future<SupplierPhaseDTO.TabFlagDTO> submit =  tabExecutorPool.submit(() -> {
                SupplierPhaseDTO.PagingParamDTO searchParamDTO = new SupplierPhaseDTO.PagingParamDTO();
                searchParamDTO.setPermissionSql(dto.getPermissionSql());
                SupplierPhaseDTO.TabFlagDTO resultDTO = new SupplierPhaseDTO.TabFlagDTO();
                String tabSql = supplierPhaseQueryHandler.getTabSql(item.getCode());
                HashMap<String, String> map = new HashMap<>();
                map.put("default", tabSql);
                searchParamDTO.setSqlMap(map);
                Integer count = this.baseMapper.tabList(searchParamDTO);
                resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
                resultDTO.setTabFlag(item.getCode());
                resultDTO.setTabFlagName(item.getName());
                return resultDTO;
            });
            futureList.add(submit);
        }
        for(Future<SupplierPhaseDTO.TabFlagDTO> f : futureList) {
            try {
                list.add(f.get());
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return list;
    }

    @Override
    public void export(SupplierPhaseDTO.PagingParamDTO dto) {
        if (CollUtil.isEmpty(dto.getFieldList())) {
            //正常导出
            downloadTaskFeign.saveDownloadTask("供应商阶段数据", EXPORT_SCM_SUPPLIER_PHASE.getCode(), dto);
        } else {
            //按字段导出
            downloadTaskFeign.saveDownloadTask("供应商阶段数据", EXPORT_SCM_DYNAMIC_SUPPLIER_PHASE.getCode(), dto);
        }
    }

    @Override
    public PagingVO<SupplierPhaseExportExcelDTO> exportSupplierPhase(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseDTO.PagingViewDTO> paging = this.paging(dto);
        if (CollUtil.isEmpty(paging.getList())) {
            return new PagingVO<>();
        }
        List<SupplierPhaseExportExcelDTO> supplierPhaseExportExcelList = BeanUtil.copyToList(paging.getList(), SupplierPhaseExportExcelDTO.class);
        return new PagingVO<>(supplierPhaseExportExcelList, paging.getTotalCount(), paging.getPageSize(), paging.getCurrPage());
    }

    @Override
    public PagingVO<DynamicExcelDTO> exportDynamicSupplierPhase(PagingDTO<SupplierPhaseDTO.PagingParamDTO> dto) {
        PagingVO<SupplierPhaseExportExcelDTO> excelList = this.exportSupplierPhase(dto);
        DynamicExcelDTO dynamicExcelDTO = new DynamicExcelDTO();

        List<SupplierPhaseDTO.ExportField> fieldList = dto.getParams().getFieldList();
        List<String> fieldCodeList = fieldList.stream().map(SupplierPhaseDTO.ExportField::getField).distinct().collect(Collectors.toList());
        LinkedHashMap<String, String> fieldMap =  fieldList.stream().collect(Collectors.toMap(SupplierPhaseDTO.ExportField::getField, SupplierPhaseDTO.ExportField::getFieldName, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        dynamicExcelDTO.setHeaders(fieldMap);

        List<LinkedHashMap<String, Object>> data = new ArrayList<>();
        List<SupplierPhaseExportExcelDTO> list = excelList.getList();
        for (SupplierPhaseExportExcelDTO exportExcelDTO : list) {
            LinkedHashMap<String, Object> excelMap = (LinkedHashMap<String, Object>)BeanUtil.beanToMap(exportExcelDTO);
            LinkedHashMap<String, Object> exportMap = excelMap.entrySet().stream().filter(obj -> fieldCodeList.contains(obj.getKey())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
            data.add(exportMap);
        }
        dynamicExcelDTO.setData(data);
        dynamicExcelDTO.setSheetName("供应商阶段数据");
        return new PagingVO<>(Collections.singletonList(dynamicExcelDTO), excelList.getTotalCount(), excelList.getPageSize(), excelList.getCurrPage());
    }
}
