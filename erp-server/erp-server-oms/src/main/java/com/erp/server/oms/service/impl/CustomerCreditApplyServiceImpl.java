package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CustomerCreditStatusEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.CustomerCreditApplyMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 客户授信 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
 */
@Slf4j
@Service
public class CustomerCreditApplyServiceImpl extends SuperServiceImpl<CustomerCreditApplyMapper, CustomerCreditApplyEntity> implements CustomerCreditApplyService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CustomerCreditApplyDTO.AddDTO addDTO) {
        CustomerCreditApplyEntity customerCreditApplyEntity = new CustomerCreditApplyEntity();
        BeanMapperUtils.copy(addDTO, customerCreditApplyEntity);
        //校验客户授信是否已存在
        List<CustomerCreditApplyEntity> existingCredits = this.lambdaQuery()
                .eq(CustomerCreditApplyEntity::getCustomerId, customerCreditApplyEntity.getCustomerId())
                .ne(CustomerCreditApplyEntity::getCreditStatus, CustomerCreditStatusEnum.CANCEL.getCode())
                .list();
        if (CollectionUtils.isNotEmpty(existingCredits)) {
            throw new ServiceException("该客户已有未作废的授信记录，不能重复新增");
        }
        // 数据处理
        handleData(customerCreditApplyEntity);

        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_KHSX);
        customerCreditApplyEntity.setCode(code);
        boolean save = super.save(customerCreditApplyEntity);
        if(!save) {
            throw new ServiceException("客户授信保存失败");
        }
        // 保存附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<AttachDTO> list = addDTO.getEvaluationAttachmentList();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(v->v.setBusinessId(customerCreditApplyEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(list, tableName.value() + "_evaluation");
        }
        List<AttachDTO> otherAttachmentList = addDTO.getOtherAttachmentList();
        if(CollectionUtils.isNotEmpty(otherAttachmentList)){
            otherAttachmentList.forEach(v->v.setBusinessId(customerCreditApplyEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(otherAttachmentList, tableName.value() + "_other");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户授信" , customerCreditApplyEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), customerCreditApplyEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(customerCreditApplyEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CustomerCreditApplyDTO.UpdateDTO addOrUpdateDTO) {
        CustomerCreditApplyEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "客户授信"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        CustomerCreditApplyEntity customerCreditApplyEntity =  BeanMapperUtils.map(CustomerCreditApplyEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(customerCreditApplyEntity);
        log.info("编辑 开始修改客户授信数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(customerCreditApplyEntity);
        if(!save) {
            throw new ServiceException("客户授信保存失败");
        }

        // 保存附件
        TableName tableName = SoReceiptEntity.class.getDeclaredAnnotation(TableName.class);
        List<AttachDTO> list = addOrUpdateDTO.getEvaluationAttachmentList();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(v->v.setBusinessId(customerCreditApplyEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(list, tableName.value() + "_evaluation");
        }
        List<AttachDTO> otherAttachmentList = addOrUpdateDTO.getOtherAttachmentList();
        if(CollectionUtils.isNotEmpty(otherAttachmentList)){
            otherAttachmentList.forEach(v->v.setBusinessId(customerCreditApplyEntity.getId()));
            omsAttachmentService.batchSaveOrUpdate(otherAttachmentList, tableName.value() + "_other");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录客户授信日志数据，单号：【{}】", customerCreditApplyEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), customerCreditApplyEntity.getCode(), "客户授信");

        operateLogService.addModuleOperateLogByObj(old, customerCreditApplyEntity, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), customerCreditApplyEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<CustomerCreditApplyDTO.ListDTO> paging(PagingDTO<CustomerCreditApplyDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CustomerCreditApplyDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<CustomerCreditApplyDTO.TabListDTO> tabList(PermissionsDTO param) {
        CustomerCreditApplyDTO.PagingParamDTO searchParam = new CustomerCreditApplyDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<CustomerCreditApplyDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = ApproveStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<CustomerCreditApplyDTO.TabListDTO> result = new ArrayList<>();
        statusList.parallelStream().forEach(status -> {
            CustomerCreditApplyDTO.TabListDTO tabListDTO = list.stream().filter(v -> Objects.equals(v.getTabFlag(), status)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(tabListDTO)) {
                tabListDTO = new CustomerCreditApplyDTO.TabListDTO();
                tabListDTO.setTabFlag(status);
                tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
                tabListDTO.setCount(0);
            }else{
                tabListDTO.setTabFlagName(ApproveStatusEnum.getName(status));
            }
            result.add(tabListDTO);
        });
        return result;
    }

    @Override
    public void exportList(CustomerCreditApplyDTO.ExportDTO param, HttpServletResponse response) {
        List<CustomerCreditApplyDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/customerCreditApply.xlsx";
        String name = "客户授信导出";
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
        CustomerCreditApplyEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到客户授信数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        startProcess(entity);
        // 记录操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "客户授信");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(CustomerCreditApplyDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(CustomerCreditApplyDTO.UpdateDTO dto) {
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
        CustomerCreditApplyEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "客户授信", approveType.getName(), dto.getComment());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(CustomerCreditApplyEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为流程模块类型，BusinessKey查看SourceTypeEnum枚举类
        approveDTO.setBusinessKey(null);
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
        CustomerCreditApplyEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到客户授信单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "客户授信");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(CustomerCreditApplyEntity entity) {
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
        CustomerCreditApplyEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到客户授信数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }

        // 删除主单数据
        super.removeById(id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "客户授信");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), entity.getCode(), "删除客户授信数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        CustomerCreditApplyEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到客户授信数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改客户授信状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "客户授信");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_CREDIT_APPLY.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        // TODO 此处的null需修改为日志模块类型，BusinessKey查看SourceTypeEnum枚举类
        revokeDTO.setBusinessKey(null);
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, CustomerCreditApplyEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());

        return Boolean.TRUE;
    }

    @Override
    public CustomerCreditApplyDTO.ViewDTO view(String id) {
        CustomerCreditApplyEntity customerCreditApplyEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到客户授信数据"));
        CustomerCreditApplyDTO.ViewDTO data = BeanMapperUtils.map(CustomerCreditApplyDTO.ViewDTO.class, customerCreditApplyEntity);
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

    public void startProcess(CustomerCreditApplyEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_CREDIT_APPLY.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(CustomerCreditApplyDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.CREDIT_PERIOD.getType(),DictBasicTypeEnum.CREDIT_TYPE.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        List<DictBasicEntity> creditPeriodDictList = dictBasicMap.get(DictBasicTypeEnum.CREDIT_PERIOD.getType());
        List<DictBasicEntity> creditTypeList = dictBasicMap.get(DictBasicTypeEnum.CREDIT_TYPE.getType());

        String creditPeriodName = creditPeriodDictList.stream().filter(v->Objects.equals(v.getValue(), data.getPeriod())).map(DictBasicEntity::getName).findFirst().orElse("");
        String creditTypeName = creditTypeList.stream().filter(v->Objects.equals(v.getValue(), data.getCreditType())).map(DictBasicEntity::getName).findFirst().orElse("");
        data.setPeriodName(creditPeriodName);
        data.setCreditTypeName(creditTypeName);
        //查询测评表附件
        TableName tableName = CustomerCreditApplyEntity.class.getDeclaredAnnotation(TableName.class);
        List<OmsAttachmentEntity> omsAttachmentEntities = omsAttachmentService.listByBusinessIdsAndType(Arrays.asList(data.getId()),tableName.value() + "_evaluation");
        List<AttachDTO> attachDTOList = BeanMapper.copyList(omsAttachmentEntities, AttachDTO.class);
        data.setEvaluationAttachmentList(attachDTOList);

        //查询其他附件
        List<OmsAttachmentEntity> otherAttachment = omsAttachmentService.listByBusinessIdsAndType(Arrays.asList(data.getId()),tableName.value() + "_other");
        List<AttachDTO> otherAttachDTOList = BeanMapper.copyList(otherAttachment, AttachDTO.class);
        data.setOtherAttachmentList(otherAttachDTOList);
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(CustomerCreditApplyEntity::getId, id)
            .set(CustomerCreditApplyEntity::getApproveUserId, userInfo.getUid())
            .set(CustomerCreditApplyEntity::getApproveUserName, userInfo.getUserName())
            .set(CustomerCreditApplyEntity::getApproveStatus, approveStatus)
            .set(CustomerCreditApplyEntity::getApproveTime, LocalDateTime.now())
            .update(new CustomerCreditApplyEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(CustomerCreditApplyEntity::getId, id)
            .set(CustomerCreditApplyEntity::getApproveUserId, "")
            .set(CustomerCreditApplyEntity::getApproveUserName, "")
            .set(CustomerCreditApplyEntity::getApproveStatus, approveStatus)
            .set(CustomerCreditApplyEntity::getApproveTime, null)
            .update(new CustomerCreditApplyEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(CustomerCreditApplyEntity::getId, id)
        .set(CustomerCreditApplyEntity::getApproveStatus, approveStatus)
        .update(new CustomerCreditApplyEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<CustomerCreditApplyDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        // 字典值获取
        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.CREDIT_TYPE.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        List<DictBasicEntity> creditTypeList = dictBasicMap.get(DictBasicTypeEnum.CREDIT_TYPE.getType());

        List<String> saleUserIds = list.stream().map(CustomerCreditApplyDTO.ListDTO::getSaleUserId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<FindUserDTO> findUserDTOS = sysUserFeign.getUserListByUserIds(saleUserIds);

        List<String> orgIds = list.stream().map(CustomerCreditApplyDTO.ListDTO::getSaleOrgId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        // 属性赋值
        for(CustomerCreditApplyDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));

            String creditTypeName = creditTypeList.stream().filter(v->Objects.equals(v.getValue(), data.getCreditType())).map(DictBasicEntity::getName).findFirst().orElse("");
            data.setCreditTypeName(creditTypeName);
            data.setCreditStatusName(CustomerCreditStatusEnum.getName(data.getCreditStatus()));

            FindUserDTO user = findUserDTOS.stream().filter(v -> Objects.equals(v.getUserId(), data.getSaleUserId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(user)) {
                data.setSaleUserName(user.getRealName());
            }
            BaseIdDTO.CodeDTO org = accountingCompanyList.stream().filter(v -> Objects.equals(v.getId(), data.getSaleOrgId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(org)) {
                data.setSaleOrgName(org.getCode());
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(CustomerCreditApplyEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CustomerCreditApplyEntity customerCreditApplyEntity) {
        //设置销售员，销售部门
        CustomerInfoEntity customerInfo = customerInfoService.getById(customerCreditApplyEntity.getCustomerId());
        if(ObjectUtil.isEmpty(customerInfo)) {
            throw new ServiceException("未找到客户信息");
        }
        customerCreditApplyEntity.setSaleUserId(customerInfo.getSellerId());
        customerCreditApplyEntity.setSaleDeptId(customerInfo.getSalesDeptId());

    }
}
