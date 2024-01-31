package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.convert.CustomerInfoConverter;
import com.erp.server.oms.mapper.CustomerB2bSellerChangeMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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
    private CustomerB2bSellerChangeService service;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private CustomerSellerService customerSellerService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO add(CustomerB2bSellerChangeDTO.AddDTO addDTO) {

        CustomerInfoEntity customerInfoEntity = customerInfoService.getById(addDTO.getMainId());
        if(Objects.isNull(customerInfoEntity)){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "客户信息为空");
        }
        List<CustomerB2bSellerChangeEntity> entityList = this.listByMainId(addDTO.getMainId());
        if(entityList.stream().anyMatch(v->v.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING)||v.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT))){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "已经有待提交，审核中的变更单，无法新增");
        }
        //销售员信息
        List<SellerDTO.ViewDTO> sellerList = customerSellerService.listByMainId(addDTO.getMainId());
        SellerDTO.ViewDTO maxStartDateEntity = sellerList.stream().max(Comparator.comparing(SellerDTO.ViewDTO::getStartDate)).orElse(null);
        if(Objects.nonNull(maxStartDateEntity) && !addDTO.getStartDate().isAfter(maxStartDateEntity.getStartDate())){
            return BatchResultDTO.fail(addDTO.getMainId(), addDTO.getCode(), "启用时间必须晚于当前销售员开始时间");
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
    public List<CustomerB2bSellerChangeDTO.ListDTO> paging(CustomerB2bSellerChangeDTO.ParamDTO dto) {
        List<CustomerB2bSellerChangeDTO.ListDTO> listDTOList = baseMapper.paging(dto);
        listDTOList.forEach(v->v.setApproveStatusName(ApproveStatusEnum.getName(v.getApproveStatus())));
        return listDTOList;
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
        CustomerB2bSellerChangeEntity customerB2bSellerChangeEntity =  BeanMapperUtils.map(CustomerB2bSellerChangeEntity.class, updateDTO);

        // 数据处理
        handleData(customerB2bSellerChangeEntity);
        log.info("编辑 开始修改b2b客户销售员变更单数据，id：【{}】", old.getId());
        boolean save = super.updateById(customerB2bSellerChangeEntity);
        if(!save) {
            throw new ServiceException("b2b客户销售员变更单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2b客户销售员变更单日志数据，id：【{}】", customerB2bSellerChangeEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), customerB2bSellerChangeEntity.getId(), "b2b客户销售员变更单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, customerB2bSellerChangeEntity, null, customerB2bSellerChangeEntity.getId(), msg);
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
