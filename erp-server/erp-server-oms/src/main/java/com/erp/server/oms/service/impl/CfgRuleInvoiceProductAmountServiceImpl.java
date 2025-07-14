package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceProductAmountEntity;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.erp.model.oms.enums.InvoiceRuleEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.InvoiceSettingConverter;
import com.erp.server.oms.mapper.CfgRuleInvoiceProductAmountMapper;
import com.erp.server.oms.service.CfgRuleInvoiceProductAmountService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.RuleConditionService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfgRuleInvoiceProductAmountDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 发票产品总价计算规则 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-07-14
 */
@Slf4j
@Service
public class CfgRuleInvoiceProductAmountServiceImpl extends SuperServiceImpl<CfgRuleInvoiceProductAmountMapper, CfgRuleInvoiceProductAmountEntity> implements CfgRuleInvoiceProductAmountService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private RuleConditionService ruleConditionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleInvoiceProductAmountDTO.AddDTO addDTO) {
        CfgRuleInvoiceProductAmountEntity cfgRuleInvoiceProductAmountEntity = new CfgRuleInvoiceProductAmountEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleInvoiceProductAmountEntity);

        // 数据处理
        handleData(cfgRuleInvoiceProductAmountEntity);

        log.info("开始新增发票产品总价计算规则");
        boolean save = super.save(cfgRuleInvoiceProductAmountEntity);
        if(!save) {
            throw new ServiceException("发票产品总价计算规则保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票产品总价计算规则" , cfgRuleInvoiceProductAmountEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_INVOICE_PRODUCT_AMOUNT.getCode(), cfgRuleInvoiceProductAmountEntity.getId(), "新增操作");
        //新增明细（如果有明细的话）
        if (CollUtil.isNotEmpty(addDTO.getConditionList())){
            List<RuleConditionDTO.AddDTO> conditionList = InvoiceSettingConverter.INSTANCE.conditionViewToAddDTO(addDTO.getConditionList());
            ruleConditionService.saveRuleCondition(cfgRuleInvoiceProductAmountEntity.getId(),conditionList);
        }

        return new BaseResultDTO.AddDTO(cfgRuleInvoiceProductAmountEntity.getId(), cfgRuleInvoiceProductAmountEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleInvoiceProductAmountDTO.UpdateDTO addOrUpdateDTO) {
        CfgRuleInvoiceProductAmountEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "发票产品总价计算规则"));
        CfgRuleInvoiceProductAmountEntity cfgRuleInvoiceProductAmountEntity =  BeanMapperUtils.map(CfgRuleInvoiceProductAmountEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgRuleInvoiceProductAmountEntity);
        log.info("编辑 开始修改发票产品总价计算规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleInvoiceProductAmountEntity);
        if(!save) {
            throw new ServiceException("发票产品总价计算规则保存失败");
        }
        //修改明细数据（包含增删改）（如果有明细的话）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getConditionList())){
            List<RuleConditionDTO.UpdateDTO> conditionList = InvoiceSettingConverter.INSTANCE.conditionViewToUpdateDTO(addOrUpdateDTO.getConditionList());
            ruleConditionService.updateRuleCondition(cfgRuleInvoiceProductAmountEntity.getId(),conditionList);
        }
        // 记录主单操作日志
        log.info("编辑 开始记录发票产品总价计算规则日志数据，id：【{}】", cfgRuleInvoiceProductAmountEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleInvoiceProductAmountEntity.getId(), "发票产品总价计算规则");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleInvoiceProductAmountEntity, ModuleTypeEnum.CFG_RULE_INVOICE_PRODUCT_AMOUNT.getCode(), cfgRuleInvoiceProductAmountEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleInvoiceProductAmountDTO.ViewDTO> listByCfgId(String id) {
        return baseMapper.listByCfgId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddOrUpdate(List<CfgRuleInvoiceProductAmountDTO.ViewDTO> productAmountDTOList, String mainId) {
        //获取历史数据
        List<CfgRuleInvoiceProductAmountEntity> oldList = baseMapper.selectList(
                Wrappers.<CfgRuleInvoiceProductAmountEntity>lambdaQuery().eq(CfgRuleInvoiceProductAmountEntity::getCfgId, mainId)
        );
        //获取要删除的数据
        List<String> ruleIds = productAmountDTOList.stream().map(CfgRuleInvoiceProductAmountDTO.ViewDTO::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<CfgRuleInvoiceProductAmountEntity> delList = oldList.stream().filter(old -> !ruleIds.contains(old.getId())).collect(Collectors.toList());
        this.removeRule(delList,mainId);
        //需要新增更新的数据
        for (CfgRuleInvoiceProductAmountDTO.ViewDTO viewDTO : productAmountDTOList) {
            if (CharSequenceUtil.isBlank(viewDTO.getId())){
                CfgRuleInvoiceProductAmountDTO.AddDTO addDTO = InvoiceSettingConverter.INSTANCE.toAddDTO(viewDTO);
                addDTO.setConditionList(viewDTO.getConditionList());
                this.add(addDTO);
            }else {
                CfgRuleInvoiceProductAmountDTO.UpdateDTO updateDTO = InvoiceSettingConverter.INSTANCE.toUpdateDTO(viewDTO);
                updateDTO.setConditionList(viewDTO.getConditionList());
                this.update(updateDTO);
            }
        }
    }

    @Override
    public List<CfgRuleInvoiceProductAmountEntity> listRuleByPriority(List<String> cfgIds) {
        if (CollUtil.isEmpty(cfgIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(CfgRuleInvoiceProductAmountEntity::getDisabled, Boolean.FALSE).
                in(CfgRuleInvoiceProductAmountEntity::getCfgId, cfgIds).
                orderByAsc(CfgRuleInvoiceProductAmountEntity::getPriority).
                orderByDesc(CfgRuleInvoiceProductAmountEntity::getUpdateTime).
                list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeRule(List<CfgRuleInvoiceProductAmountEntity> delList, String mainId) {
        if(CollUtil.isEmpty(delList)) {
            return;
        }
        delList.forEach(e -> {
            boolean remove = this.removeById(e.getId());
            if(!remove) {
                throw new ServiceException("删除规则失败");
            }
            ruleConditionService.removeByRuleId(e.getId());
            // 记录日志
            String msg = StrUtil.format("用户【{}】删除名称为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), e.getName(), "发票产品总价计算规则");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_INVOICE_PRODUCT_AMOUNT.getCode(), mainId, "删除规则");
        });
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleInvoiceProductAmountEntity cfgRuleInvoiceProductAmountEntity) {
        if (CharSequenceUtil.isNotBlank(cfgRuleInvoiceProductAmountEntity.getName())){
            cfgRuleInvoiceProductAmountEntity.setName(InvoiceRuleEnum.getName(cfgRuleInvoiceProductAmountEntity.getDictInvoiceRule()));
        }
    }
}
