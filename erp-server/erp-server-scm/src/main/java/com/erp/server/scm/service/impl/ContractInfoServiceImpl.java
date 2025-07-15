package com.erp.server.scm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
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
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.dto.AttachmentDTO;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.AttachmentEntity;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ContractInfoStatusEnum;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.model.workflow.entity.ProcessTaskManagementEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.ContractInfoMapper;
import com.erp.server.scm.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_CONTRACT_INFO_REPORT;

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
    @Resource
    private ModuleOperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private AttachmentService attachmentService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SupplierService supplierService;

    @Resource
    @Qualifier("contractInfoExecutorPool")
    private ExecutorService contractInfoExecutorPool;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ContractInfoDTO.AddDTO addDTO) {
        ContractInfoEntity contractInfoEntity = new ContractInfoEntity();
        BeanMapperUtils.copy(addDTO, contractInfoEntity);

        // 数据处理
        handleData(contractInfoEntity);

        //默认未生效
        contractInfoEntity.setStatus(ContractInfoStatusEnum.NOT_EFFECTIVE.getCode());

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

        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        batchSaveAttachment(attachmentUrlList, attachmentNameList, contractInfoEntity);
        return new BaseResultDTO.AddDTO(contractInfoEntity.getId(), code);
    }

    private void batchSaveAttachment(List<String> attachmentUrlList, List<String> attachmentNameList, ContractInfoEntity contractInfoEntity) {
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<ContractInfoEntity> clazz = ContractInfoEntity.class;
            TableName tableName = clazz.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(contractInfoEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if(CollectionUtils.isNotEmpty(batchAttachmentList)){
                attachmentService.saveBatch(batchAttachmentList);
            }
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(ContractInfoEntity contractInfoEntity) {
        //供应商
        if(StringUtils.isNotBlank(contractInfoEntity.getServiceProviderId())){
            SupplierEntity supplierEntity = supplierService.getById(contractInfoEntity.getServiceProviderId());
            if(Objects.nonNull(supplierEntity)){
                contractInfoEntity.setServiceProviderName(supplierEntity.getName());
            }
        }

        //校验有效期
        LocalDate effectiveDate = contractInfoEntity.getEffectiveDate();
        LocalDate expireDate = contractInfoEntity.getExpireDate();
        if (Objects.nonNull(effectiveDate) && Objects.nonNull(expireDate) && expireDate.compareTo(effectiveDate) < 0) {
            throw new ServiceException(ApiError.ERROR_98125);
        }


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

        //审核通过的，才需要判断生效状态
        if(Objects.equals(contractInfoEntity.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())){
            LocalDate now = LocalDate.now();
            LocalDate effectiveDate = contractInfoEntity.getEffectiveDate();
            LocalDate expireDate = contractInfoEntity.getExpireDate();
            //状态处理
            ContractInfoStatusEnum status = ContractInfoStatusEnum.NOT_EFFECTIVE;
            if (now.compareTo(effectiveDate) >= 0 && now.compareTo(expireDate) <= 0) {
                status = ContractInfoStatusEnum.EFFECTIVE;
            } else if(now.compareTo(expireDate) > 0){
                status = ContractInfoStatusEnum.EXPIRED;
            }
            contractInfoEntity.setStatus(status.getCode());
        }else {
            contractInfoEntity.setStatus(ContractInfoStatusEnum.NOT_EFFECTIVE.getCode());
        }

        log.info("编辑 开始修改合同管理单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(contractInfoEntity);
        if(!save) {
            throw new ServiceException("合同管理单保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录合同管理单日志数据，单号：【{}】", contractInfoEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "合同管理单");
        operateLogService.addModuleOperateLogByObj(old, contractInfoEntity, ModuleTypeEnum.CONTRACT_INFO.getCode(), contractInfoEntity.getId(),"", msg);


        //附件集合
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();

        List<AttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessId(contractInfoEntity.getId());
        if(CollUtil.isEmpty(oldAttachmentList)){
            batchSaveAttachment(attachmentUrlList, attachmentNameList, contractInfoEntity);
            // 操作日志
            String attachMsg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据新增附件[{}] ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "合同管理单",attachmentNameList.get(0));
            operateLogService.addModuleOperateLog(attachMsg, ModuleTypeEnum.CONTRACT_INFO.getCode(), contractInfoEntity.getId(), "新增操作");
        }else {
            String oldAttachName = oldAttachmentList.get(0).getAttachName();
            String newAttachName = attachmentNameList.get(0);
            String oldAttachUrl = oldAttachmentList.get(0).getAttachUrl();
            String newAttachUrl = attachmentUrlList.get(0);
            if(!Objects.equals(oldAttachUrl, newAttachUrl)){
                //删除附件
                attachmentService.deleteByBusinessIds(Arrays.asList(contractInfoEntity.getId()));

                batchSaveAttachment(attachmentUrlList, attachmentNameList, contractInfoEntity);

                // 操作日志
                String attachMsg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据编辑了附件由[{}]变更为[{}] ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "合同管理单",oldAttachName,newAttachName);
                operateLogService.addModuleOperateLog(attachMsg, ModuleTypeEnum.CONTRACT_INFO.getCode(), contractInfoEntity.getId(), "新增操作");
            }
        }

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

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<ContractInfoDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        List<DictBasicDTO> typeList = dictBasicService.getByKey(DictBasicEnum.CONTRACT_TYPE.getType());
        Map<String, String> typeMap = CollUtil.isEmpty(typeList) ? new HashMap<>() : typeList.stream().collect(Collectors.toMap(DictBasicDTO::getValue, DictBasicDTO::getName));

        //获取到附件信息
        List<String> ids = list.stream().map(ContractInfoDTO.ListDTO::getId).collect(Collectors.toList());
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(ids);

        LocalDate now = LocalDate.now();

        // 属性赋值
        for(ContractInfoDTO.ListDTO data : list) {
            //审核通过的，才需要判断生效状态
            if(Objects.equals(data.getApproveStatus(),ApproveStatusEnum.APPROVE.getCode())){
                //校验有效期
                LocalDate effectiveDate = data.getEffectiveDate();
                LocalDate expireDate = data.getExpireDate();
                if (Objects.nonNull(effectiveDate) && Objects.nonNull(expireDate) && expireDate.compareTo(effectiveDate) < 0) {
                    data.setStatus("");

                    //更新状态值
                    lambdaUpdate()
                            .set(ContractInfoEntity::getStatus,"")
                            .eq(ContractInfoEntity::getId,data.getId())
                            .update();
                }else {
                    //状态处理
                    ContractInfoStatusEnum status = ContractInfoStatusEnum.NOT_EFFECTIVE;
                    if (now.compareTo(effectiveDate) >= 0 && now.compareTo(expireDate) <= 0) {
                        status = ContractInfoStatusEnum.EFFECTIVE;
                    } else if(now.compareTo(expireDate) > 0){
                        status = ContractInfoStatusEnum.EXPIRED;
                    }
                    if(!Objects.equals(data.getStatus(),status.getCode())){
                        data.setStatus(status.getCode());
                        //更新状态值
                        lambdaUpdate()
                                .set(ContractInfoEntity::getStatus,status.getCode())
                                .eq(ContractInfoEntity::getId,data.getId())
                                .update();

                    }
                }
            }

            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setStatusName(ContractInfoStatusEnum.getName(data.getStatus()));
            data.setTypeName(typeMap.get(data.getType()));
            data.setDisableName(Objects.equals(data.getDisable(),true) ? "停用" : "启用");

            //附件地址
            List<String> attachmentUrlList = attachmentList.stream().filter(a -> a.getBusinessId().equals(data.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());

            //附件地址
            List<String> attachmentNameList = attachmentList.stream().filter(a -> a.getBusinessId().equals(data.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            data.setAttachmentUrlList(attachmentUrlList);
            data.setAttachmentNameList(attachmentNameList);
        }
    }

    @Override
    public List<ContractInfoDTO.TabListDTO> tabList(PermissionsDTO param) {
        ContractInfoDTO.PagingParamDTO searchParam = new ContractInfoDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<ContractInfoDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        //待我审核
        //根据单据id查询审核流程
        LoginUser user = UserContext.getNonLoginUser();
        int waitMeApproveCount = 0;
        ProcessManagementDTO.TaskKeyInfoDTO dto = new ProcessManagementDTO.TaskKeyInfoDTO();
        dto.setBusinessKey(SourceTypeEnum.CONTRACT_INFO.getCode());
        dto.setTaskStatus(ApproveStatusEnum.APPROVE_ING.getCode());
        dto.setCurApproveId(user.getUid());
        List<ProcessTaskManagementEntity> processTaskManagementList = workflowFeign.listProcessByBusinessKey(dto);
        if (CollectionUtils.isNotEmpty(processTaskManagementList)) {
            List<String> ids = processTaskManagementList.stream().map(ProcessTaskManagementEntity::getBusinessId).collect(Collectors.toList());
            waitMeApproveCount = lambdaQuery().eq(ContractInfoEntity::getId, ids).count();
        }
        //不通过
        Integer rejectCount = lambdaQuery().eq(ContractInfoEntity::getApproveStatus, ApproveStatusEnum.REJECT).count();

        //生效状态
        Map<String, ContractInfoDTO.TabListDTO> map = list.stream().collect(Collectors.toMap(ContractInfoDTO.TabListDTO::getTabFlag, t -> t));
        List<ContractInfoDTO.TabListDTO> result = new ArrayList<>();

        result.add(new ContractInfoDTO.TabListDTO( ApproveStatusEnum.APPROVE_ING.getCode(), ApproveStatusEnum.APPROVE_ING.getName() , waitMeApproveCount));
        result.add(new ContractInfoDTO.TabListDTO( ApproveStatusEnum.REJECT.getCode(), ApproveStatusEnum.REJECT.getName() , rejectCount ));
        result.add(new ContractInfoDTO.TabListDTO( ContractInfoStatusEnum.NOT_EFFECTIVE.getCode(), ContractInfoStatusEnum.NOT_EFFECTIVE.getName() , map.containsKey(ContractInfoStatusEnum.NOT_EFFECTIVE.getCode()) ? map.get(ContractInfoStatusEnum.NOT_EFFECTIVE.getCode()).getCount() : 0   ));
        result.add(new ContractInfoDTO.TabListDTO( ContractInfoStatusEnum.EFFECTIVE.getCode(), ContractInfoStatusEnum.EFFECTIVE.getName() , map.containsKey(ContractInfoStatusEnum.EFFECTIVE.getCode()) ? map.get(ContractInfoStatusEnum.EFFECTIVE.getCode()).getCount() : 0   ));
        result.add(new ContractInfoDTO.TabListDTO( ContractInfoStatusEnum.EXPIRED.getCode(), ContractInfoStatusEnum.EXPIRED.getName() , map.containsKey(ContractInfoStatusEnum.EXPIRED.getCode()) ? map.get(ContractInfoStatusEnum.EXPIRED.getCode()).getCount() : 0   ));
        return result;
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
     * 查看详情处理
     * @author will
     * @date 2025/6/16 15:42
     * @param data
     * @return void
     */
    private void fillOne(ContractInfoDTO.ViewDTO data) {
        if (ObjectUtil.isNull(data)) {
            return;
        }

        List<DictBasicDTO> typeList = dictBasicService.getByKey(DictBasicEnum.CONTRACT_TYPE.getType());
        Map<String, String> typeMap = CollUtil.isEmpty(typeList) ? new HashMap<>() : typeList.stream().collect(Collectors.toMap(DictBasicDTO::getValue, DictBasicDTO::getName));

        data.setStatusName(ContractInfoStatusEnum.getName(data.getStatus()));
        data.setTypeName(typeMap.get(data.getType()));

        //获取到附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessId(data.getId());
        if(CollUtil.isNotEmpty(attachmentList)){
            //附件地址
            List<String> attachmentUrlList = attachmentList.stream().filter(a -> a.getBusinessId().equals(data.getId())).map(AttachmentDTO.UpdateDTO::getAttachUrl).
                    collect(Collectors.toList());

            //附件地址
            List<String> attachmentNameList = attachmentList.stream().filter(a -> a.getBusinessId().equals(data.getId())).map(AttachmentDTO.UpdateDTO::getAttachName).
                    collect(Collectors.toList());
            data.setAttachmentUrlList(attachmentUrlList);
            data.setAttachmentNameList(attachmentNameList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        ContractInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到合同管理单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT.getCode(), entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98009);
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

    @Override
    public void exportList(ContractInfoDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("合同管理单导出", EXPORT_SCM_CONTRACT_INFO_REPORT.getCode(), param);
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
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getCode())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "合同管理单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                try {
                    //睡眠一秒
                    Thread.sleep(1000);

                    LocalDate effectiveDate = entity.getEffectiveDate();
                    LocalDate expireDate = entity.getExpireDate();
                    //状态处理
                    ContractInfoStatusEnum status = ContractInfoStatusEnum.NOT_EFFECTIVE;
                    LocalDate now = LocalDate.now();
                    if (now.compareTo(effectiveDate) >= 0 && now.compareTo(expireDate) <= 0) {
                        status = ContractInfoStatusEnum.EFFECTIVE;
                    } else if(now.compareTo(expireDate) > 0){
                        status = ContractInfoStatusEnum.EXPIRED;
                    }
                    //只更新审核通过的数据
                    lambdaUpdate()
                            .set(ContractInfoEntity::getStatus, status.getCode())
                            .eq(ContractInfoEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                            .eq(ContractInfoEntity::getId, dto.getId())
                            .update();

                } catch (InterruptedException e) {
                    log.error("审核后数据状态更新异常", e);
                    Thread.currentThread().interrupt();
                    throw new ServiceException("审核后数据状态更新异常", e);
                }
            }
        });

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

        //只更新未生效
        lambdaUpdate()
                .set(ContractInfoEntity::getStatus, ContractInfoStatusEnum.NOT_EFFECTIVE.getCode())
                .eq(ContractInfoEntity::getId, id)
                .update();

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
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //登陆人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(ContractInfoEntity::getId, id)
            .set(ContractInfoEntity::getApproveUserId, userInfo.getUid())
            .set(ContractInfoEntity::getApproveUserName, userInfo.getUserName())
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
            .set(ContractInfoEntity::getApproveUserName, "")
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

    @Override
    public BatchResultDTO enable(String id, Boolean disabled) {
        ContractInfoEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到合同管理数据"));
        if(!entity.getDisable().equals(disabled)){
            lambdaUpdate()
                    .set(ContractInfoEntity::getDisable, disabled)
                    .eq(ContractInfoEntity::getId, id)
                    .update();
            // 日志
            String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据启用状态由【{}】改为【{}】 ",UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), Objects.equals(entity.getDisable(), Boolean.FALSE) ? "启用" : "停用",Objects.equals(disabled, Boolean.FALSE) ? "启用" : "停用");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CONTRACT_INFO.getCode(), entity.getId(), "更新合同管理");
        }
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    @Override
    public ExportZipResultDTO exportZip(ContractInfoDTO.PagingParamDTO pagingParamDTO) {
        List<ContractInfoDTO.ListAttachDTO> list = this.baseMapper.listAttachByIds(pagingParamDTO);
        if(CollUtil.isEmpty(list)){
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        // 动态生成文件名
        String fileName = SourceTypeEnum.CONTRACT_INFO.getName()+"_"+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + ".zip";
        StreamingResponseBody streamingResponseBody = downloadZip(list);
        ExportZipResultDTO resultDTO = new ExportZipResultDTO();
        resultDTO.setFileName(fileName);
        resultDTO.setResponseBody(streamingResponseBody);
        return resultDTO;
    }

    private StreamingResponseBody downloadZip(List<ContractInfoDTO.ListAttachDTO> list) {
        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                // 并发下载所有文件内容
                List<CompletableFuture<Triple<String, String, byte[]>>> downloadFutures = list.stream()
                        .map(attachDTO -> CompletableFuture.supplyAsync(() -> {
                            String fileName = attachDTO.getServiceProviderName()+"_"+attachDTO.getAttachName();//文件名
                            String typeName = attachDTO.getTypeName(); // 分类文件夹名称
                            try {
                                byte[] content = FastDFSClientUtil.getFileByte(attachDTO.getAttachUrl());
                                return Triple.of(typeName, fileName, content); // 使用 commons-lang3 的 Triple
                            } catch (Exception e) {
                                throw new ServiceException("文件处理失败: " + attachDTO.getAttachName(), e);
                            }
                        }, contractInfoExecutorPool))
                        .collect(Collectors.toList());

                // 等待所有下载任务完成
                CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0])).join();

                // 用于记录每个文件名出现的次数
                Map<String, Integer> fileNameCountMap = new HashMap<>();
                // 单线程按顺序写入 ZIP（带子目录）
                for (CompletableFuture<Triple<String, String, byte[]>> future : downloadFutures) {
                    Triple<String, String, byte[]> fileData = future.get();
                    String type = fileData.getLeft(); // 文件夹名
                    String fileName = fileData.getMiddle();
                    byte[] content = fileData.getRight();

                    // 处理重复文件名
                    String uniqueFileName = generateUniqueFileName(fileName, fileNameCountMap);

                    String entryPath = (type != null ? type + "/" : "") + uniqueFileName;

                    zipOut.putNextEntry(new ZipEntry(entryPath));
                    zipOut.write(content);
                    zipOut.closeEntry();
                }
            } catch (Exception e) {
                throw new ServiceException("压缩包生成失败", e);
            }
        };
    }

    /**
     * 生成唯一的文件名，避免重复
     *
     * @param baseFileName 原始文件名
     * @param fileNameCountMap 记录已出现的文件名及其次数
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String baseFileName, Map<String, Integer> fileNameCountMap) {
        int count = fileNameCountMap.getOrDefault(baseFileName, 0);
        fileNameCountMap.put(baseFileName, count + 1);

        if (count == 0) {
            return baseFileName;
        }

        // 插入随机后缀或者使用递增序号
        String nameWithoutExt = "";
        String ext = "";

        int dotIndex = baseFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            nameWithoutExt = baseFileName.substring(0, dotIndex);
            ext = baseFileName.substring(dotIndex);
        } else {
            nameWithoutExt = baseFileName;
        }

        // 使用递增序号防止重复，也可以用 UUID 随机数
        return nameWithoutExt + "_" + count + ext;
    }

}
