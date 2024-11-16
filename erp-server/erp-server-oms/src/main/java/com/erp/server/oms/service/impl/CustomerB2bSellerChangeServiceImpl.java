package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.ApproveType;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.CustomerInfoConverter;
import com.erp.server.oms.mapper.CustomerB2bSellerChangeMapper;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.CustomerSellerService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER_B2B_SELLER_CHANGE;

/**
 * <p>
 * b2b客户销售员变更单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
 */
@Slf4j
@Service
public class CustomerB2bSellerChangeServiceImpl extends SuperServiceImpl<CustomerB2bSellerChangeMapper, CustomerB2bSellerChangeEntity> implements CustomerB2bSellerChangeService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CustomerB2bSellerChangeServiceImpl service;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerSellerService customerSellerService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO add(CustomerB2bSellerChangeDTO.AddDTO addDTO) {
        String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();
        if(StringUtils.isBlank(addDTO.getChangeSellerId())){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), ApiError.ERROR_92158.msg);
        }

        KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO businessOperatorDTO = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
        businessOperatorDTO.setOrgCode("100");
        businessOperatorDTO.setBusinessOperatorType(xsyCode);
        businessOperatorDTO.setUserId(addDTO.getChangeSellerId());
        KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeFeign.getBusinessOperator(businessOperatorDTO);
        if (Objects.isNull(businessOperator)) {
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), ApiError.ERROR_92157.msg);
        }else{
            addDTO.setChangeSellerName(businessOperator.getUserName());
        }

        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(addDTO.getMainId());
        if(Objects.isNull(customerInfoEntity)){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "客户信息为空");
        }
        if(!customerInfoEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE)){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "客户信息未审核，无法进行销售员变更");
        }
        List<CustomerB2bSellerChangeEntity> entityList = this.listByMainId(addDTO.getMainId());
        if(entityList.stream().anyMatch(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)||v.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT))){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "已经有待提交，审核中的变更单，无法新增");
        }
        //销售员信息
        CustomerSellerEntity currentSellerEntity = customerSellerService.getCurrentInfo(customerInfoEntity.getId());
        if(Objects.nonNull(currentSellerEntity) && !addDTO.getStartDate().isAfter(currentSellerEntity.getStartDate())){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "启用时间必须晚于当前销售员开始时间");
        }

        if(Objects.nonNull(currentSellerEntity) && addDTO.getChangeSellerId().equals(currentSellerEntity.getSellerId())){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "变更后的销售员与当前销售员一致");
        }

        CustomerB2bSellerChangeEntity customerB2bSellerChangeEntity = CustomerInfoConverter.INSTANCE.toCustomerB2bSellerChangeConvert(customerInfoEntity,addDTO);
        log.info("开始新增b2b客户销售员变更单");
        boolean save = super.save(customerB2bSellerChangeEntity);
        if(!save) {
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "b2b客户销售员变更单保存失败");
        }
        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), ApiError.ERROR_92155.msg , customerB2bSellerChangeEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), customerB2bSellerChangeEntity.getId(), "新增操作");
        return BatchResultDTO.success(customerB2bSellerChangeEntity.getId(),addDTO.getCode(),"新增成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(CustomerB2bSellerChangeDTO.AddDTO addDTO) {
        BatchResultDTO batchResultDTO = service.add(addDTO);
        if(Boolean.FALSE.equals(batchResultDTO.getSuccess())){
            return batchResultDTO;
        }
        //提交流程
        CustomerB2bSellerChangeEntity entity = this.getById(batchResultDTO.getId());
        batchResultDTO = this.startProcess(entity);
        if(Boolean.FALSE.equals(batchResultDTO.getSuccess())){
            return batchResultDTO;
        }
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
        boolean result = this.updateById(entity);
        if (result) {
            //添加日志
            String content = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), entity.getId(), "状态变更");
            batchResultDTO.setMsg("提交审核成功");
        }else{
            batchResultDTO.setSuccess(false);
            batchResultDTO.setMsg("提交审核失败");
        }
        return batchResultDTO;
    }

    @Override
    public List<CustomerB2bSellerChangeEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(CustomerB2bSellerChangeEntity::getMainId,mainId).list();
    }

    @Override
    public PagingVO<CustomerB2bSellerChangeDTO.ListDTO> paging(PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CustomerB2bSellerChangeDTO.ListDTO> listDTOList = baseMapper.paging(query,dto.getParams());
        List<String> ids = listDTOList.getRecords().stream().map(CustomerB2bSellerChangeDTO.ListDTO::getCustomerId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode(), obj)));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult<Object>(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        for (CustomerB2bSellerChangeDTO.ListDTO re : listDTOList.getRecords()) {
            re.setApproveStatusName(ApproveStatusEnum.getName(re.getApproveStatus()));
            //最新待审核人
            if (listApiResult != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(re.getCustomerId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if(StringUtils.isNotBlank(curApprove)){
                    re.setApproveUserName(curApprove);
                }
            }
        }
        return new PagingVO<>(listDTOList);
    }

    @Override
    public List<CustomerB2bSellerChangeDTO.TabFlagDTO> tabFlag() {
        List<CustomerB2bSellerChangeDTO.TabFlagDTO> tabFlagDTOList = baseMapper.countByTabFlag();
        ApproveStatusEnum[] approveStatusEnums = ApproveStatusEnum.values();
        for(ApproveStatusEnum approveStatusEnum : approveStatusEnums){
            CustomerB2bSellerChangeDTO.TabFlagDTO tabFlagDTO = tabFlagDTOList.stream().filter(v->v.getTabFlag().equals(approveStatusEnum.getStatus())).findFirst().orElse(null);
            if(Objects.isNull(tabFlagDTO)){
                CustomerB2bSellerChangeDTO.TabFlagDTO tempTabFlagDTO = new CustomerB2bSellerChangeDTO.TabFlagDTO();
                tempTabFlagDTO.setCount(0);
                tempTabFlagDTO.setTabFlag(approveStatusEnum.getStatus());
                tempTabFlagDTO.setTabFlagName(approveStatusEnum.getName());
                tabFlagDTOList.add(tempTabFlagDTO);
            }else{
                tabFlagDTO.setTabFlagName(approveStatusEnum.getName());
            }
        }
        for(CustomerB2bSellerChangeDTO.TabFlagDTO tabFlagDTO : tabFlagDTOList){
            if(tabFlagDTO.getTabFlag().equals(ApproveStatusEnum.APPROVE_ING.getCode())){
                tabFlagDTO.setTabFlagName("待审核");
            }
            if(tabFlagDTO.getTabFlag().equals(ApproveStatusEnum.REJECT.getCode())){
                tabFlagDTO.setTabFlagName("不通过");
            }

        }
        return tabFlagDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(CustomerB2bSellerChangeDTO.UpdateDTO dto) {
        if(StringUtils.isBlank(dto.getChangeSellerId())){
            throw new ServiceException(ApiError.ERROR_92158.msg);
        }

        String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();
        KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO businessOperatorDTO = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
        businessOperatorDTO.setOrgCode("100");
        businessOperatorDTO.setBusinessOperatorType(xsyCode);
        businessOperatorDTO.setUserId(dto.getChangeSellerId());
        KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeFeign.getBusinessOperator(businessOperatorDTO);
        if (Objects.isNull(businessOperator)) {
            throw new ServiceException(ApiError.ERROR_92157.msg);
        }else{
            dto.setChangeSellerName(businessOperator.getUserName());
        }
        CustomerB2bSellerChangeServiceImpl bean = ApplicationContextUtils.getBean(CustomerB2bSellerChangeServiceImpl.class);
        bean.update(dto);
        //提交流程
        CustomerB2bSellerChangeEntity entity = this.getById(dto.getId());
        BatchResultDTO batchResultDTO = this.startProcess(entity);
        if(Boolean.FALSE.equals(batchResultDTO.getSuccess())){
            throw new ServiceException("提交流程失败");
        }
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
        boolean result = this.updateById(entity);
        if (result) {
            //添加日志
            String content = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
            operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), entity.getId(), "状态变更");
        }else{
            throw new ServiceException("更新审核状态失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> batchSubmit(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(String id : ids){
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            //提交流程
            CustomerB2bSellerChangeEntity entity = this.getById(id);
            if(Objects.isNull(entity)){
                batchResultDTO.setSuccess(false);
                batchResultDTO.setId(id);
                batchResultDTO.setCode(id);
                batchResultDTO.setMsg(ApiError.ERROR_92159.msg);
                resultDTOList.add(batchResultDTO);
                continue;
            }
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
            batchResultDTO.setId(entity.getId());
            batchResultDTO.setCode(customerInfoEntity.getCode());
            if(!(entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT) || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT))){
                batchResultDTO.setMsg("只有待提交或审核失败状态可以提交审核");
                batchResultDTO.setSuccess(false);
                resultDTOList.add(batchResultDTO);
                continue;
            }
            batchResultDTO = this.startProcess(entity);
            if(Boolean.FALSE.equals(batchResultDTO.getSuccess())){
                resultDTOList.add(batchResultDTO);
                continue;
            }
            entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            boolean result = this.updateApproveInfo(entity,"",null);
            if (result) {
                //添加日志
                String content = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
                operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), entity.getId(), "状态变更");
                batchResultDTO.setSuccess(true);
                batchResultDTO.setMsg("提交审核成功");
            }else{
                batchResultDTO.setSuccess(false);
                batchResultDTO.setMsg("提交审核失败");
            }
            resultDTOList.add(batchResultDTO);
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> batchDelete(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(String id : ids){
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            resultDTOList.add(batchResultDTO);
            CustomerB2bSellerChangeEntity entity = this.getById(id);
            if(Objects.isNull(entity)){
                batchResultDTO.setSuccess(false);
                batchResultDTO.setId(id);
                batchResultDTO.setCode(id);
                batchResultDTO.setMsg(ApiError.ERROR_92159.msg);
                continue;
            }
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
            batchResultDTO.setId(entity.getId());
            batchResultDTO.setCode(customerInfoEntity.getCode());
            if(!(entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT) || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT)) ){
                batchResultDTO.setMsg("只有待提交或不通过状态可以删除");
                batchResultDTO.setSuccess(false);
                continue;
            }
            CustomerB2bSellerChangeServiceImpl bean = ApplicationContextUtils.getBean(CustomerB2bSellerChangeServiceImpl.class);
            boolean result = bean.removeById(id);
            if (result) {
                batchResultDTO.setSuccess(true);
                batchResultDTO.setMsg("删除成功");
            }else{
                batchResultDTO.setSuccess(false);
                batchResultDTO.setMsg("删除失败");
            }
        }
        return resultDTOList;
    }

    @Override
    public List<BatchResultDTO> batchCancel(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(String id : ids){
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            resultDTOList.add(batchResultDTO);
            CustomerB2bSellerChangeEntity entity = this.getById(id);
            if(Objects.isNull(entity)){
                batchResultDTO.setSuccess(false);
                batchResultDTO.setId(id);
                batchResultDTO.setCode(id);
                batchResultDTO.setMsg(ApiError.ERROR_92159.msg);
                continue;
            }
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
            batchResultDTO.setId(entity.getId());
            batchResultDTO.setCode(customerInfoEntity.getCode());
            if(!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING) ){
                batchResultDTO.setMsg("只有审核中可以撤销");
                batchResultDTO.setSuccess(false);
                continue;
            }
            //撤销现有流程
            LoginUser userInfo = UserContext.getDefaultLoginUser();
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(entity.getMainId());
            revokeDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);

            entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
            entity.setApproveTime(null);
            entity.setApproveUserName(null);
            boolean result = this.updateById(entity);
            if (result) {
                //添加日志
                String content = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
                operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), entity.getId(), "状态变更");
                batchResultDTO.setSuccess(true);
                batchResultDTO.setMsg("撤销成功");
            }else{
                batchResultDTO.setSuccess(false);
                batchResultDTO.setMsg("撤销失败");
            }
        }
        return resultDTOList;
    }

    @Override
    public BatchResultDTO approve(BaseApproveParamDTO baseApproveParamDTO,CustomerB2bSellerChangeEntity entity,CustomerInfoEntity customerInfo) {
        if(!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)){
            return BatchResultDTO.fail(entity.getId(),customerInfo.getCode(),ApiError.ERROR_98006.msg);
        }
        BatchResultDTO batchResultDTO = new BatchResultDTO();
        batchResultDTO.setId(entity.getId());
        batchResultDTO.setCode(customerInfo.getCode());
        batchResultDTO.setSuccess(true);
        batchResultDTO.setMsg("审核成功");
        //调用审核流程
        service.approveProcess(entity, baseApproveParamDTO,batchResultDTO,customerInfo);
        return BatchResultDTO.success(entity.getId(),customerInfo.getCode(),"操作成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveProcess(CustomerB2bSellerChangeEntity entity, BaseApproveParamDTO dto,BatchResultDTO batchResultDTO,CustomerInfoEntity customerInfoEntity ) {
        //无需流程则直接更新状态
        if (ObjectUtil.isNotEmpty(dto.getIsNeedProcess()) && Boolean.TRUE.equals(!dto.getIsNeedProcess())) {
            CustomerB2bSellerChangeServiceImpl bean = ApplicationContextUtils.getBean(CustomerB2bSellerChangeServiceImpl.class);
            bean.approveEnd(dto, entity,batchResultDTO,customerInfoEntity);
            return;
        }

        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getMainId());
        approveDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));

        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResultApi = workflowFeign.approve(approveDTO);
        Integer code = approveResultApi.getCode();
        if (200 != code) {
            batchResultDTO.setSuccess(false);
            batchResultDTO.setMsg("审核失败");
            return;
        }
        ProcessManagementDTO.ApproveResultDTO approveResult = approveResultApi.getData();
        CustomerB2bSellerChangeServiceImpl bean = ApplicationContextUtils.getBean(CustomerB2bSellerChangeServiceImpl.class);
        if(ObjectUtils.isEmpty(approveResult.getIsExistProcess()) || Boolean.TRUE.equals(!approveResult.getIsExistProcess())){
            bean.approveEnd(dto, entity,batchResultDTO,customerInfoEntity);
        }
        CustomerB2bSellerChangeEntity againEntity = this.getById(entity.getId());
        if(againEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)){
            bean.updateApproveInfo(againEntity,dto.getComment(),LocalDateTime.now());
        }
    }

    public Boolean updateApproveInfo(CustomerB2bSellerChangeEntity entity,String comment,LocalDateTime approveTime){
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode(), entity.getId()));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.curApprover(dtoList);
        if (listApiResult != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(listApiResult.getData())) {
            List<ProcessManagementDTO.CurApproveInfoDTO> curApproveInfoDTOList = listApiResult.getData();
            String curApproveId = curApproveInfoDTOList.stream().filter(v->StringUtils.isNotBlank(v.getCurApproveId())).findFirst().orElse(new ProcessManagementDTO.CurApproveInfoDTO()).getCurApproveId();
            String curApproveName = curApproveInfoDTOList.stream().filter(v->StringUtils.isNotBlank(v.getCurApproveName())).findFirst().orElse(new ProcessManagementDTO.CurApproveInfoDTO()).getCurApproveName();
            if(StringUtils.isNotBlank(curApproveId)){
                entity.setApproveUserId(curApproveId);
            }
            if(StringUtils.isNotBlank(curApproveName)){
                entity.setApproveUserName(curApproveName);
            }
            if(approveTime != null){
                entity.setApproveTime(approveTime);
            }
            entity.setRemark(comment);
        }
        return this.updateById(entity);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, CustomerB2bSellerChangeEntity entity,BatchResultDTO batchResultDTO,CustomerInfoEntity customerInfoEntity ) {
        LoginUser user = UserContext.getDefaultLoginUser();
        ApproveStatusEnum approve;
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            approve = ApproveStatusEnum.APPROVE;
            entity.setApproveStatus(ApproveStatusEnum.APPROVE);
        } else {
            //审核不通过
            approve = ApproveStatusEnum.REJECT;
            entity.setApproveStatus(ApproveStatusEnum.REJECT);
        }
        entity.setRemark(dto.getComment());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveUserId(user.getUid());
        entity.setApproveUserName(user.getUserName());
        Boolean result = this.updateById(entity);
        if (Boolean.FALSE.equals(result)) {
            batchResultDTO.setSuccess(false);
            batchResultDTO.setMsg("更新状态失败");
            return false;
        }
        //审核通过更新客户表销售员信息，更新历史销售员信息
        customerInfoEntity.setSellerId(entity.getChangeSellerId());
        customerInfoEntity.setSellerName(entity.getChangeSellerName());
        customerInfoService.updateById(customerInfoEntity);
        customerSellerService.batchSellerHistory(Collections.singletonList(customerInfoEntity),entity.getStartDate());

        String approveContent = String.format(ApiError.ERROR_92156.msg, ApproveStatusEnum.APPROVE_ING.getName(), approve.getName());
        operateLogService.addModuleOperateLog(approveContent, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), entity.getId(), "状态变更");
        //记录操作日志
        String content = String.format("销售员变更单[%s]审核通过自动修改销售员从[%s]为[%s]",customerInfoEntity.getCode(),entity.getOriginSellerName(),entity.getChangeSellerName());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CUSTOMER.getCode(), customerInfoEntity.getId(), "编辑操作");
        return true;
    }

    @Override
    public void export(CustomerB2bSellerChangeDTO.ParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("客户b2b销售变更单", EXPORT_OMS_CUSTOMER_B2B_SELLER_CHANGE.getCode(), dto);
    }

    @Override
    public CustomerB2bSellerChangeEntity getByMainId(String businessId) {
        if(StringUtils.isBlank(businessId)){
            return new CustomerB2bSellerChangeEntity();
        }
        return lambdaQuery()
                .eq(CustomerB2bSellerChangeEntity::getMainId, businessId)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        Page<CustomerB2bSellerExcelDTO> page = this.baseMapper.export(new Page<>(dto.getCurrPage(), dto.getPageSize()) ,dto.getParams());
        if(CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        List<String> ids = page.getRecords().stream().map(CustomerB2bSellerExcelDTO::getMainId).collect(Collectors.toList());
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        ids.forEach(obj -> dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode(), obj)));
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult<Object>(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        for (CustomerB2bSellerExcelDTO customerB2bSellerExcelDTO : page.getRecords()) {
            customerB2bSellerExcelDTO.setApproveStatusName(ApproveStatusEnum.getName(customerB2bSellerExcelDTO.getApproveStatus()));
            //最新待审核人
            if (listApiResult != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(obj -> obj.getBusinessId().equals(customerB2bSellerExcelDTO.getMainId()) && StringUtils.isNotBlank(obj.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if(StringUtils.isNotBlank(curApprove)){
                    customerB2bSellerExcelDTO.setApproveUserName(curApprove);
                }
            }
        }
        return new PagingVO<>(page);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CustomerB2bSellerChangeDTO.UpdateDTO updateDTO) {
        CustomerB2bSellerChangeEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, ApiError.ERROR_92155.msg);
        }
        // 待提交和审核不通过允许修改
        if (Boolean.FALSE.equals(ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus()))) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        if(StringUtils.isBlank(updateDTO.getChangeSellerId())){
            throw new ServiceException(ApiError.ERROR_92158.msg);
        }
        String xsyCode = KingdeeBusinessOperatorTypeEnum.XSY.getCode();
        KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO businessOperatorDTO = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
        businessOperatorDTO.setOrgCode("100");
        businessOperatorDTO.setBusinessOperatorType(xsyCode);
        businessOperatorDTO.setUserId(updateDTO.getChangeSellerId());
        KingdeeOperatorRefPostDTO.OperatorDTO businessOperator = kingdeeFeign.getBusinessOperator(businessOperatorDTO);
        if (Objects.isNull(businessOperator)) {
            throw new ServiceException(ApiError.ERROR_92157.msg);
        }else{
            updateDTO.setChangeSellerName(businessOperator.getUserName());

        }
        //销售员信息
        CustomerSellerEntity currentSellerEntity = customerSellerService.getCurrentInfo(old.getMainId());
        if(Objects.nonNull(currentSellerEntity) && !updateDTO.getStartDate().isAfter(currentSellerEntity.getStartDate())){
            throw new ServiceException("启用时间必须晚于当前销售员开始时间");
        }
        if(Objects.nonNull(currentSellerEntity) && updateDTO.getChangeSellerId().equals(currentSellerEntity.getSellerId())){
            throw new ServiceException("变更后的销售员与当前销售员一致");
        }

        CustomerB2bSellerChangeEntity customerB2bSellerChangeEntity =  BeanMapperUtils.map(CustomerB2bSellerChangeEntity.class, updateDTO);
        log.info("编辑 开始修改b2b客户销售员变更单数据，id：【{}】", old.getId());
        boolean save = super.updateById(customerB2bSellerChangeEntity);
        if(!save) {
            throw new ServiceException("b2b客户销售员变更单保存失败");
        }
        log.info("编辑 开始记录b2b客户销售员变更单日志数据，id：【{}】", customerB2bSellerChangeEntity.getId());
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), customerB2bSellerChangeEntity.getId(), ApiError.ERROR_92155.msg);
        operateLogService.addModuleOperateLogByObj(old, customerB2bSellerChangeEntity, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), customerB2bSellerChangeEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<BatchResultDTO> batchAdd(List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList) {
        List<String> mainIdList = addDTOList.stream().map(CustomerB2bSellerChangeDTO.AddDTO::getMainId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = CollectionUtils.isNotEmpty(mainIdList) ? customerInfoService.listByIds(mainIdList) : Collections.emptyList();
        List<String> useOrgIdList = customerInfoList.stream().map(CustomerInfoEntity::getUseOrgId).distinct().collect(Collectors.toList());
        if(useOrgIdList.size()> 1){
             throw new ServiceException("只能选择同一个使用组织的客户进行销售员变更");
        }
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        for(CustomerB2bSellerChangeDTO.AddDTO addDTO : addDTOList){
            batchResultDTOList.add(service.add(addDTO));
        }
        return batchResultDTOList;
    }

    @Override
    public List<BatchResultDTO> batchAddAndSubmit(List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        for(CustomerB2bSellerChangeDTO.AddDTO addDTO : addDTOList){
            batchResultDTOList.add(service.addAndSubmit(addDTO));
        }
        return batchResultDTOList;
    }

    /**
     * @param entity
     * @description: 提交流程
     */
    private BatchResultDTO startProcess(CustomerB2bSellerChangeEntity entity) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
        ProcessManagementDTO.StartDTO dto = new ProcessManagementDTO.StartDTO();
        dto.setBusinessId(entity.getMainId());
        dto.setBusinessCode(customerInfoEntity.getCode());
        dto.setBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode());
        dto.setBusinessName(customerInfoEntity.getCode());
        dto.setUserId(userInfo.getUid());
        dto.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> listApiResult = workflowFeign.start(dto);
        if (!listApiResult.isSuccess()) {
            return BatchResultDTO.fail(entity.getId(), customerInfoEntity.getCode(), "提交流程失败");
        }
        return BatchResultDTO.success(entity.getId(), customerInfoEntity.getCode(), "提交流程成功");
    }

}
