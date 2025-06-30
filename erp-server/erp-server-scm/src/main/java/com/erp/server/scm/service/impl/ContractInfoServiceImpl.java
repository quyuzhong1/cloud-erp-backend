package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.ContractInfoMapper;
import com.erp.server.scm.service.AttachmentService;
import com.erp.server.scm.service.ContractInfoService;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.ModuleOperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
/**
 * <p>
 * 合同管理表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-06-16
 */
@Slf4j
@Service
public class ContractInfoServiceImpl extends SuperServiceImpl<ContractInfoMapper, ContractInfoEntity> implements ContractInfoService {
    @Autowired
    private ModuleOperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private AttachmentService attachmentService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ContractInfoDTO.AddDTO addDTO) {
        ContractInfoEntity contractInfoEntity = new ContractInfoEntity();
        BeanMapperUtils.copy(addDTO, contractInfoEntity);

        // 数据处理
        handleData(contractInfoEntity);

        log.info("开始新增合同管理单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_GYSHT);
        contractInfoEntity.setCode(code);
        boolean save = super.save(contractInfoEntity);
        if(!save) {
            throw new ServiceException("合同管理单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "合同管理单" , contractInfoEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), contractInfoEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(contractInfoEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ContractInfoDTO.UpdateDTO addOrUpdateDTO) {
        ContractInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "合同管理单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        ContractInfoEntity contractInfoEntity =  BeanMapperUtils.map(ContractInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(contractInfoEntity);
        log.info("编辑 开始修改合同管理单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(contractInfoEntity);
        if(!save) {
            throw new ServiceException("合同管理单保存失败");
        }

        //上传文件

        // 记录主单操作日志
        log.info("编辑 开始记录合同管理单日志数据，单号：【{}】", contractInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), contractInfoEntity.getCode(), "合同管理单");
        operateLogService.addModuleOperateLogByObj(old, contractInfoEntity, ModuleTypeEnum.CONTRACT_INFO.getCode(), contractInfoEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<ContractInfoDTO.ListDTO> paging(PagingDTO<ContractInfoDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<ContractInfoDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<ContractInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        ContractInfoDTO.PagingParamDTO searchParam = new ContractInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ContractInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(ContractInfoDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        return list;
    }

    @Override
    public void exportList(ContractInfoDTO.PagingParamDTO param, HttpServletResponse response) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        ContractInfoEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到合同管理单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改合同管理单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动合同管理单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录合同管理单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        ContractInfoEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(ContractInfoEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.CONTRACT_INFO.getCode());
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
        ContractInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到合同管理单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(ContractInfoEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ContractInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到合同管理单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除主单数据
        log.info("删除 开始删除合同管理单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除合同管理单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getCode(), "删除合同管理单数据");

        //删除附件
        attachmentService.deleteByBusinessIds(Collections.singletonList(id));

        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        ContractInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到合同管理单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("撤销 开始修改合同管理单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "取消流程操作");

        //撤销流程
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.CONTRACT_INFO.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, ContractInfoEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        return Boolean.TRUE;
    }

    @Override
    public ContractInfoDTO.ViewDTO view(String id) {
        ContractInfoEntity contractInfoEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到合同管理单数据"));
        ContractInfoDTO.ViewDTO data = BeanMapperUtils.map(ContractInfoDTO.ViewDTO.class, contractInfoEntity);
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

    public void startProcess(ContractInfoEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.CONTRACT_INFO.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    /**
     * 查看详情处理
     * @author will
     * @date 2025/6/16 15:42
     * @param data
     * @return void
     */
    private void fillOne(ContractInfoDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(data.getId());
        if (CollUtil.isNotEmpty(attachmentList)) {
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(ContractInfoEntity::getId, id)
            .set(ContractInfoEntity::getApproveUserId, userInfo.getUid())
            .set(ContractInfoEntity::getApproveStatus, approveStatus)
            .set(ContractInfoEntity::getApproveTime, LocalDateTime.now())
            .update(new ContractInfoEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(ContractInfoEntity::getId, id)
            .set(ContractInfoEntity::getApproveUserId, "")
            .set(ContractInfoEntity::getApproveStatus, approveStatus)
            .set(ContractInfoEntity::getApproveTime, null)
            .update(new ContractInfoEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(ContractInfoEntity::getId, id)
        .set(ContractInfoEntity::getApproveStatus, approveStatus)
        .update(new ContractInfoEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<ContractInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        List<DictBasicDTO> typeList = dictBasicService.getByKey(DictBasicEnum.CONTRACT_INFO.getType());
        Map<String, String> typeMap = CollUtil.isEmpty(typeList) ? new HashMap<>() : typeList.stream().collect(Collectors.toMap(DictBasicDTO::getValue, DictBasicDTO::getName));

        // 属性赋值
        for(ContractInfoDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            LocalDate now = LocalDate.now();
            String statusName = now.isBefore(data.getEffectiveDate()) ? "未生效" : now.isAfter(data.getExpireDate()) ? "已失效" : "生效中";
            data.setStatusName(statusName);
            data.setTypeName(typeMap.get(data.getType()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(ContractInfoEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(ContractInfoEntity contractInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }

}
