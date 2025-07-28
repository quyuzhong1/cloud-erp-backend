package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
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
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.entity.SupplierGradeEntity;
import com.erp.model.scm.entity.SupplierPhaseEntity;
import com.erp.model.scm.enums.SupplierPhaseEnum;
import com.erp.model.scm.enums.SupplierPhaseTabFlagEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.SupplierPhaseMapper;
import com.erp.server.scm.query.SupplierPhaseQueryHandler;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.SupplierGradeService;
import com.erp.server.scm.service.SupplierPhaseService;
import com.erp.server.scm.service.SupplierService;
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

/**
 * <p>
 * 供应商升降级 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
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
    public Boolean addAndSubmit(SupplierPhaseDTO.AddDTO dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(id));
        return result;
    }


    /**
     * 供应商阶段 提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-23 16:50
     */
    @Override
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        //启动流程 todo
        Boolean result = updateApproveStatus(list, ApproveStatusEnum.APPROVE_ING.getStatus());
        return result;
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
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<SupplierPhaseEntity> list = this.listByIds(ids);
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        /**
         *
         * 还需要检查是否是自己能否审核
         */
        //审核通过
        if (dto.getType().equals(ApproveTypeEnum.PASS.getStatus())) {
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            Boolean result = this.updateApproveStatus(list, approveStatus);
            //通过后更改供应商的阶段
            supplierService.updatePhase(list);

            return result;
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            Boolean result = this.updateApproveStatus(list, rejectStatus);
            return result;
        }
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
            String currentGradeName = supplierGradeList.stream().filter(d -> d.getId().equals(item.getCurrentGradeId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setCurrentGradeName(currentGradeName);
            //目标等级
            String targetGradeName = supplierGradeList.stream().filter(d -> d.getId().equals(item.getTargetGradeId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setTargetGradeName(targetGradeName);

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
    public Boolean updateAndSubmit(SupplierPhaseDTO.UpdateDTO dto) {
        String id = this.updateSupplierPhase(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));
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

}
