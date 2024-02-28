package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.CustomerSellerEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.wms.dto.OverseasDeliveryPlanDTO;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.CustomerInfoConverter;
import com.erp.server.oms.mapper.CustomerB2bSellerChangeMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO add(CustomerB2bSellerChangeDTO.AddDTO addDTO) {

        if(StringUtils.isBlank(addDTO.getChangeSellerId())){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "变更后的销售员id不能为空");
        }
        List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorEntityList = kingdeeFeign.listBusinessOperatorByUserIdList(Collections.singletonList(addDTO.getChangeSellerId()));
        if(CollectionUtils.isNotEmpty(kingdeeBusinessOperatorEntityList)){
            addDTO.setChangeSellerName(kingdeeBusinessOperatorEntityList.get(0).getKingdeeUserName());
        }else{
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "查询不到销售员");
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
        // 数据处理
        handleData(customerB2bSellerChangeEntity);
        log.info("开始新增b2b客户销售员变更单");
        boolean save = super.save(customerB2bSellerChangeEntity);
        if(!save) {
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "b2b客户销售员变更单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "b2b客户销售员变更单" , customerB2bSellerChangeEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), customerB2bSellerChangeEntity.getId(), "新增操作");
        return BatchResultDTO.success(customerB2bSellerChangeEntity.getId(),addDTO.getCode(),"新增成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO addAndSubmit(CustomerB2bSellerChangeDTO.AddDTO addDTO) {
        BatchResultDTO batchResultDTO = service.add(addDTO);
        if(!batchResultDTO.getSuccess()){
            return batchResultDTO;
        }
        //提交流程
        CustomerB2bSellerChangeEntity entity = this.getById(batchResultDTO.getId());
        batchResultDTO = this.startProcess(entity);
        if(!batchResultDTO.getSuccess()){
            return batchResultDTO;
        }
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
        boolean result = this.updateById(entity);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
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
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CustomerB2bSellerChangeDTO.ListDTO> listDTOList = baseMapper.paging(query,dto.getParams());
        listDTOList.getRecords().forEach(v->v.setApproveStatusName(ApproveStatusEnum.getName(v.getApproveStatus())));
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
        BatchResultDTO batchResultDTO = new BatchResultDTO();
        if(StringUtils.isBlank(dto.getChangeSellerId())){
            throw new ServiceException("变更后的销售员id不能为空");
        }
        List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorEntityList = kingdeeFeign.listBusinessOperatorByUserIdList(Collections.singletonList(dto.getChangeSellerId()));
        if(CollectionUtils.isNotEmpty(kingdeeBusinessOperatorEntityList)){
            dto.setChangeSellerName(kingdeeBusinessOperatorEntityList.get(0).getKingdeeUserName());
        }else{
            throw new ServiceException("查询不到销售员");
        }
        this.update(dto);
        //提交流程
        CustomerB2bSellerChangeEntity entity = this.getById(dto.getId());
        batchResultDTO = this.startProcess(entity);
        if(!batchResultDTO.getSuccess()){
            throw new ServiceException("提交流程失败");
        }
        entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
        boolean result = this.updateById(entity);
        if (result) {
            //添加日志
            String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
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
                batchResultDTO.setMsg("单据不存在");
                resultDTOList.add(batchResultDTO);
                continue;
            }
            CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
            batchResultDTO.setId(entity.getId());
            batchResultDTO.setCode(customerInfoEntity.getCode());
            if(!entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT)){
                batchResultDTO.setMsg("只有待提交状态可以提交审核");
                batchResultDTO.setSuccess(false);
                resultDTOList.add(batchResultDTO);
                continue;
            }
            batchResultDTO = this.startProcess(entity);
            if(!batchResultDTO.getSuccess()){
                resultDTOList.add(batchResultDTO);
                continue;
            }
            entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            boolean result = this.updateById(entity);
            if (result) {
                //添加日志
                String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
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
                batchResultDTO.setMsg("单据不存在");
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
            boolean result = this.removeById(id);
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
                batchResultDTO.setMsg("单据不存在");
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
            LoginUser userInfo = commonService.getUserInfo();
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(id);
            revokeDTO.setBusinessKey(SourceTypeEnum.CUSTOMER_B2B_CHANGE_SELLER.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);

            entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
            entity.setApproveTime(null);
            entity.setApproveUserName(null);
            boolean result = this.updateById(entity);
            if (result) {
                //添加日志
                String content = String.format("状态由[%s]变更为[%s]", ApproveStatusEnum.WAIT_SUBMIT.getName(), ApproveStatusEnum.APPROVE_ING.getName());
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
    public List<BatchResultDTO> batchApprove(BaseApproveParamDTO baseApproveParamDTO) {
        List<CustomerB2bSellerChangeEntity> entityList = listByIds(baseApproveParamDTO.getIds());
        List<String> mainIds =entityList.stream().map(CustomerB2bSellerChangeEntity::getMainId).collect(Collectors.toList());
        Map<String,CustomerInfoEntity> customerInfoEntityMap = customerInfoService.listByIds(mainIds).stream().collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for(CustomerB2bSellerChangeEntity entity : entityList){
            CustomerInfoEntity customerInfoEntity = customerInfoEntityMap.get(entity.getMainId());
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            resultDTOList.add(batchResultDTO);
            if(Objects.isNull(customerInfoEntity)){
                batchResultDTO = BatchResultDTO.fail(entity.getId(),"","客户信息已经删除");
                batchResultDTO.setId(entity.getId());
                batchResultDTO.setMsg("客户信息已经删除");
                batchResultDTO.setSuccess(false);
                continue;
            }
            batchResultDTO.setId(entity.getId());
            batchResultDTO.setCode(customerInfoEntity.getCode());
            batchResultDTO.setSuccess(true);
            batchResultDTO.setMsg("审核成功");
            if(!entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)){
                batchResultDTO.setMsg(ApiError.ERROR_98006.msg);
                batchResultDTO.setSuccess(false);
                continue;
            }
            //调用审核流程
            service.approveProcess(entity, baseApproveParamDTO,batchResultDTO,customerInfoEntity);
        }
        return resultDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveProcess(CustomerB2bSellerChangeEntity entity, BaseApproveParamDTO dto,BatchResultDTO batchResultDTO,CustomerInfoEntity customerInfoEntity ) {
        //无需流程则直接更新状态
        if (ObjectUtil.isNotEmpty(dto.getIsNeedProcess()) && !dto.getIsNeedProcess()) {
            this.approveEnd(dto, entity,batchResultDTO,customerInfoEntity);
            return;
        }

        LoginUser userInfo = commonService.getUserInfo();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
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
        if(ObjectUtils.isEmpty(approveResult.getIsExistProcess()) || !approveResult.getIsExistProcess()){
            this.approveEnd(dto, entity,batchResultDTO,customerInfoEntity);
        }
        CustomerB2bSellerChangeEntity againEntity = this.getById(entity.getId());
        if(againEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)){
            againEntity.setRemark(dto.getComment());
            againEntity.setApproveTime(LocalDateTime.now());
            againEntity.setApproveUserId(userInfo.getUid());
            againEntity.setApproveUserName(userInfo.getUserName());
            this.updateById(againEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(BaseApproveParamDTO dto, CustomerB2bSellerChangeEntity entity,BatchResultDTO batchResultDTO,CustomerInfoEntity customerInfoEntity ) {
        LoginUser user = commonService.getUserInfo();
        if (dto.getType().equals(ApproveType.PASS)) {
            //审核通过
            entity.setApproveStatus(ApproveStatusEnum.APPROVE);
        } else {
            //审核不通过
            entity.setApproveStatus(ApproveStatusEnum.REJECT);
        }
        entity.setRemark(dto.getComment());
        entity.setApproveTime(LocalDateTime.now());
        entity.setApproveUserId(user.getUid());
        entity.setApproveUserName(user.getUserName());
        Boolean result = this.updateById(entity);
        if (!result) {
            batchResultDTO.setSuccess(false);
            batchResultDTO.setMsg("更新状态失败");
            return false;
        }
        //审核通过更新客户表销售员信息，更新历史销售员信息
        customerInfoEntity.setSellerId(entity.getChangeSellerId());
        customerInfoEntity.setSellerName(entity.getChangeSellerName());
        customerInfoService.updateById(customerInfoEntity);
        customerSellerService.batchSellerHistory(Collections.singletonList(customerInfoEntity),entity.getStartDate());
        return true;
    }

    @Override
    public void export(CustomerB2bSellerChangeDTO.ParamDTO dto, HttpServletResponse response) {
        List<CustomerB2bSellerExcelDTO> list = this.baseMapper.export(dto);
        if(CollUtil.isEmpty(list)) {
            return;
        }
        list.forEach(v->v.setApproveStatusName(ApproveStatusEnum.getName(v.getApproveStatus())));
        // 数据处理
        ExcelUtil.export("客户b2b销售变更单","客户b2b销售变更单",list,CustomerB2bSellerExcelDTO.class,response);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CustomerB2bSellerChangeDTO.UpdateDTO updateDTO) {
        CustomerB2bSellerChangeEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2b客户销售员变更单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        if(StringUtils.isBlank(updateDTO.getChangeSellerId())){
            throw new ServiceException("变更后的销售员id不能为空");
        }
        List<KingdeeBusinessOperatorEntity> kingdeeBusinessOperatorEntityList = kingdeeFeign.listBusinessOperatorByUserIdList(Collections.singletonList(updateDTO.getChangeSellerId()));
        if(CollectionUtils.isNotEmpty(kingdeeBusinessOperatorEntityList)){
            updateDTO.setChangeSellerName(kingdeeBusinessOperatorEntityList.get(0).getKingdeeUserName());
        }else{
            throw new ServiceException("查询不到销售员");
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

        // 数据处理
        handleData(customerB2bSellerChangeEntity);
        log.info("编辑 开始修改b2b客户销售员变更单数据，id：【{}】", old.getId());
        boolean save = super.updateById(customerB2bSellerChangeEntity);
        if(!save) {
            throw new ServiceException("b2b客户销售员变更单保存失败");
        }
        log.info("编辑 开始记录b2b客户销售员变更单日志数据，id：【{}】", customerB2bSellerChangeEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), customerB2bSellerChangeEntity.getId(), "b2b客户销售员变更单");
        operateLogService.addModuleOperateLogByObj(old, customerB2bSellerChangeEntity, ModuleTypeEnum.CUSTOMER_B2B_SELLER_CHANGE.getCode(), customerB2bSellerChangeEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<BatchResultDTO> batchAdd(List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList) {
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
        LoginUser userInfo = commonService.getUserInfo();
        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(entity.getMainId());
        ProcessManagementDTO.StartDTO dto = new ProcessManagementDTO.StartDTO();
        dto.setBusinessId(entity.getId());
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


    /**
    * 新增修改处理数据
    */
    private void handleData(CustomerB2bSellerChangeEntity customerB2bSellerChangeEntity) {
    // TODO 验证数据 & 数据赋值
    }

}
