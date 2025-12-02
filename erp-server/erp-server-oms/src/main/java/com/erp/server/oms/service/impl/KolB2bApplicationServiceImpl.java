package com.erp.server.oms.service.impl;

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
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.KolB2bApplicationDTO;
import com.erp.model.oms.entity.KolB2bApplicationEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.mapper.KolB2bApplicationMapper;
import com.erp.server.oms.service.KolB2bApplicationDetailService;
import com.erp.server.oms.service.KolB2bApplicationService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_WMS_KOL_B2B_APPLICATION_REPORT;

/**
 * <p>
 * B2B寄养申请主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolB2bApplicationServiceImpl extends SuperServiceImpl<KolB2bApplicationMapper, KolB2bApplicationEntity> implements KolB2bApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private KolB2bApplicationDetailService kolB2bApplicationDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolB2bApplicationDTO.AddDTO addDTO) {
        KolB2bApplicationEntity kolB2bApplicationEntity = new KolB2bApplicationEntity();
        BeanMapperUtils.copy(addDTO, kolB2bApplicationEntity);

        // 数据处理
        handleData(kolB2bApplicationEntity);

        log.info("开始新增B2B寄养申请主单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KOLB);
        kolB2bApplicationEntity.setCode(code);
        boolean save = super.save(kolB2bApplicationEntity);
        if(!save) {
            throw new ServiceException("B2B寄养申请主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B寄养申请主单" , kolB2bApplicationEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), kolB2bApplicationEntity.getId(), "新增操作");

        //添加明细
        kolB2bApplicationDetailService.add(addDTO.getDetailList(), kolB2bApplicationEntity.getId());

        return new BaseResultDTO.AddDTO(kolB2bApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolB2bApplicationDTO.UpdateDTO addOrUpdateDTO) {
        KolB2bApplicationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2B寄养申请主单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        KolB2bApplicationEntity kolB2bApplicationEntity =  BeanMapperUtils.map(KolB2bApplicationEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolB2bApplicationEntity);
        log.info("编辑 开始修改B2B寄养申请主单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kolB2bApplicationEntity);
        if(!save) {
            throw new ServiceException("B2B寄养申请主单保存失败");
        }
        //更新明细信息
        kolB2bApplicationDetailService.update(addOrUpdateDTO.getDetailList(), kolB2bApplicationEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录B2B寄养申请主单日志数据，单号：【{}】", kolB2bApplicationEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolB2bApplicationEntity.getCode(), "B2B寄养申请主单");
        operateLogService.addModuleOperateLogByObj(old, kolB2bApplicationEntity, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), kolB2bApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<KolB2bApplicationDTO.ListDTO> paging(PagingDTO<KolB2bApplicationDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolB2bApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<KolB2bApplicationDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolB2bApplicationDTO.PagingParamDTO searchParam = new KolB2bApplicationDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());

        return Collections.emptyList();
    }

    @Override
    public void exportList(KolB2bApplicationDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("B2B寄样申请导出", IMPORT_WMS_KOL_B2B_APPLICATION_REPORT.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        KolB2bApplicationEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到B2B寄养申请主单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改B2B寄养申请主单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动B2B寄养申请主单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录B2B寄养申请主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(KolB2bApplicationDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(KolB2bApplicationDTO.UpdateDTO dto) {
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
        KolB2bApplicationEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(KolB2bApplicationEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
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
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄养申请主单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(KolB2bApplicationEntity entity) {
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
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄养申请主单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除B2B寄养申请主单主单数据，id：【{}】", id);
        super.removeById(id);

        //删除明细数据
        kolB2bApplicationDetailService.deleteByMainId(id);

        // 删除日志数据
        log.info("删除 开始删除B2B寄养申请主单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除B2B寄养申请主单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄养申请主单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getCode()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getCode()))
                || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改B2B寄养申请主单状态数据，id：【{}】", id);
        lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(KolB2bApplicationEntity::getInvalidRemark, remark)
            .set(KolB2bApplicationEntity::getInvalidTime, LocalDateTime.now())
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        KolB2bApplicationEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2B寄养申请主单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改B2B寄养申请主单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "B2B寄养申请主单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_B2B_APPLICATION.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, KolB2bApplicationEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    @Override
    public KolB2bApplicationDTO.RefBillDTO listRefBill(String id) {
        return null;
    }

    @Override
    public Boolean generateSoInfo(KolB2bApplicationDTO.GenerateSoInfoDTO dto) {
        return null;
    }

    @Override
    public Boolean generateFeedback(KolB2bApplicationDTO.GenerateFeedbackDTO dto) {
        return null;
    }

    @Override
    public KolB2bApplicationDTO.ViewDTO view(String id) {
        KolB2bApplicationEntity kolB2bApplicationEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到B2B寄养申请主单数据"));
        KolB2bApplicationDTO.ViewDTO data = BeanMapperUtils.map(KolB2bApplicationDTO.ViewDTO.class, kolB2bApplicationEntity);
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

    public void startProcess(KolB2bApplicationEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(KolB2bApplicationDTO.ViewDTO data) {
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
        this.lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getApproveUserId, userInfo.getUid())
            .set(KolB2bApplicationEntity::getApproveUserName, userInfo.getUserName())
            .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2bApplicationEntity::getApproveTime, LocalDateTime.now())
            .update(new KolB2bApplicationEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
            .set(KolB2bApplicationEntity::getApproveUserId, "")
            .set(KolB2bApplicationEntity::getApproveUserName, "")
            .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
            .set(KolB2bApplicationEntity::getApproveTime, null)
            .update(new KolB2bApplicationEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(KolB2bApplicationEntity::getId, id)
        .set(KolB2bApplicationEntity::getApproveStatus, approveStatus)
        .update(new KolB2bApplicationEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<KolB2bApplicationDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 属性赋值
        for(KolB2bApplicationDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            // TODO 其他如需要显示名称的字段赋值
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(KolB2bApplicationEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolB2bApplicationEntity kolB2bApplicationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
