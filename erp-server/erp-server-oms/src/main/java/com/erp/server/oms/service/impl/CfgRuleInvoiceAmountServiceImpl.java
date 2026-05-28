package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.CfgRuleInvoiceAmountDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.erp.model.oms.entity.CfgRuleInvoiceAmountEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.InvoiceRuleEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.InvoiceSettingConverter;
import com.erp.server.oms.mapper.CfgRuleInvoiceAmountMapper;
import com.erp.server.oms.service.CfgInvoiceSettingDetailService;
import com.erp.server.oms.service.CfgRuleInvoiceAmountService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.RuleConditionService;
import com.erp.server.oms.service.ShopInfoService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

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
public class CfgRuleInvoiceAmountServiceImpl extends SuperServiceImpl<CfgRuleInvoiceAmountMapper, CfgRuleInvoiceAmountEntity> implements CfgRuleInvoiceAmountService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private RuleConditionService ruleConditionService;
    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;
    @Resource
    private ShopInfoService shopInfoService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleInvoiceAmountDTO.AddDTO addDTO) {
        CfgRuleInvoiceAmountEntity cfgRuleInvoiceAmountEntity = new CfgRuleInvoiceAmountEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleInvoiceAmountEntity);
        validateShopeeBrazilRule(cfgRuleInvoiceAmountEntity.getCfgId(), cfgRuleInvoiceAmountEntity.getDictInvoiceRule());

        // 数据处理
        handleData(cfgRuleInvoiceAmountEntity);

        log.info("开始新增发票产品总价计算规则");
        boolean save = super.save(cfgRuleInvoiceAmountEntity);
        if(!save) {
            throw new ServiceException("发票产品总价计算规则保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票产品总价计算规则" , cfgRuleInvoiceAmountEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_INVOICE_PRODUCT_AMOUNT.getCode(), cfgRuleInvoiceAmountEntity.getId(), "新增操作");
        //新增明细（如果有明细的话）
        if (CollUtil.isNotEmpty(addDTO.getConditionList())){
            List<RuleConditionDTO.AddDTO> conditionList = InvoiceSettingConverter.INSTANCE.conditionViewToAddDTO(addDTO.getConditionList());
            ruleConditionService.saveRuleCondition(cfgRuleInvoiceAmountEntity.getId(),conditionList);
        }

        return new BaseResultDTO.AddDTO(cfgRuleInvoiceAmountEntity.getId(), cfgRuleInvoiceAmountEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleInvoiceAmountDTO.UpdateDTO addOrUpdateDTO) {
        CfgRuleInvoiceAmountEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "发票产品总价计算规则"));
        CfgRuleInvoiceAmountEntity cfgRuleInvoiceAmountEntity =  BeanMapperUtils.map(CfgRuleInvoiceAmountEntity.class, addOrUpdateDTO);
        validateShopeeBrazilRule(cfgRuleInvoiceAmountEntity.getCfgId(), cfgRuleInvoiceAmountEntity.getDictInvoiceRule());

        // 数据处理
        handleData(cfgRuleInvoiceAmountEntity);
        log.info("编辑 开始修改发票产品总价计算规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleInvoiceAmountEntity);
        if(!save) {
            throw new ServiceException("发票产品总价计算规则保存失败");
        }
        //修改明细数据（包含增删改）（如果有明细的话）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getConditionList())){
            List<RuleConditionDTO.UpdateDTO> conditionList = InvoiceSettingConverter.INSTANCE.conditionViewToUpdateDTO(addOrUpdateDTO.getConditionList());
            ruleConditionService.updateRuleCondition(cfgRuleInvoiceAmountEntity.getId(),conditionList);
        }
        // 记录主单操作日志
        log.info("编辑 开始记录发票产品总价计算规则日志数据，id：【{}】", cfgRuleInvoiceAmountEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleInvoiceAmountEntity.getId(), "发票产品总价计算规则");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleInvoiceAmountEntity, ModuleTypeEnum.CFG_RULE_INVOICE_PRODUCT_AMOUNT.getCode(), cfgRuleInvoiceAmountEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleInvoiceAmountDTO.ViewDTO> listByCfgId(String id) {
        return baseMapper.listByCfgId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddOrUpdate(List<CfgRuleInvoiceAmountDTO.ViewDTO> productAmountDTOList, String mainId) {
        //获取历史数据
        List<CfgRuleInvoiceAmountEntity> oldList = baseMapper.selectList(
                Wrappers.<CfgRuleInvoiceAmountEntity>lambdaQuery().eq(CfgRuleInvoiceAmountEntity::getCfgId, mainId)
        );
        //获取要删除的数据
        List<String> ruleIds = productAmountDTOList.stream().map(CfgRuleInvoiceAmountDTO.ViewDTO::getId).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        List<CfgRuleInvoiceAmountEntity> delList = oldList.stream().filter(old -> !ruleIds.contains(old.getId())).collect(Collectors.toList());
        this.removeRule(delList,mainId);
        //需要新增更新的数据
        for (CfgRuleInvoiceAmountDTO.ViewDTO viewDTO : productAmountDTOList) {
            if (CharSequenceUtil.isBlank(viewDTO.getId())){
                CfgRuleInvoiceAmountDTO.AddDTO addDTO = InvoiceSettingConverter.INSTANCE.toAddDTO(viewDTO);
                addDTO.setCfgId(mainId);
                addDTO.setName(InvoiceRuleEnum.getName(viewDTO.getDictInvoiceRule()));
                addDTO.setConditionList(viewDTO.getConditionList());
                this.add(addDTO);
            }else {
                CfgRuleInvoiceAmountDTO.UpdateDTO updateDTO = InvoiceSettingConverter.INSTANCE.toUpdateDTO(viewDTO);
                updateDTO.setCfgId(mainId);
                updateDTO.setName(InvoiceRuleEnum.getName(viewDTO.getDictInvoiceRule()));
                updateDTO.setConditionList(viewDTO.getConditionList());
                this.update(updateDTO);
            }
        }
    }

    @Override
    public List<CfgRuleInvoiceAmountEntity> listRuleByPriority(List<String> cfgIds) {
        if (CollUtil.isEmpty(cfgIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(CfgRuleInvoiceAmountEntity::getDisabled, Boolean.FALSE).
                in(CfgRuleInvoiceAmountEntity::getCfgId, cfgIds).
                orderByAsc(CfgRuleInvoiceAmountEntity::getPriority).
                orderByDesc(CfgRuleInvoiceAmountEntity::getUpdateTime).
                list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeRule(List<CfgRuleInvoiceAmountEntity> delList, String mainId) {
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
    private void handleData(CfgRuleInvoiceAmountEntity cfgRuleInvoiceAmountEntity) {
        if (CharSequenceUtil.isNotBlank(cfgRuleInvoiceAmountEntity.getName())){
            cfgRuleInvoiceAmountEntity.setName(InvoiceRuleEnum.getName(cfgRuleInvoiceAmountEntity.getDictInvoiceRule()));
        }
    }

    private void validateShopeeBrazilRule(String cfgId, String dictInvoiceRule) {
        if (!CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.DEDUCT.getCode()) || CharSequenceUtil.isBlank(cfgId)) {
            return;
        }
        List<CfgInvoiceSettingDetailEntity> detailEntityList = cfgInvoiceSettingDetailService.lambdaQuery()
                .eq(CfgInvoiceSettingDetailEntity::getMainId, cfgId)
                .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false)
                .list();
        if (CollUtil.isEmpty(detailEntityList)) {
            return;
        }
        List<String> shopIdList = detailEntityList.stream()
                .map(CfgInvoiceSettingDetailEntity::getShopId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(shopIdList)) {
            return;
        }
        Map<String, ShopInfoEntity> shopInfoMap = shopInfoService.listByIds(shopIdList).stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, shop -> shop, (left, right) -> left));
        boolean containsShopeeBrazil = detailEntityList.stream()
                .map(detail -> shopInfoMap.get(detail.getShopId()))
                .filter(Objects::nonNull)
                .anyMatch(this::isShopeeBrazilShop);
        if (containsShopeeBrazil) {
            throw new ServiceException("虾皮巴西店铺不支持按佣金开票，请选择产品全额或自定义比例");
        }
    }

    private boolean isShopeeBrazilShop(ShopInfoEntity shopInfoEntity) {
        return Objects.nonNull(shopInfoEntity)
                && CharSequenceUtil.equalsIgnoreCase(shopInfoEntity.getDictPlatform(), PlatformDictEnum.SHOPEE.getCode())
                && CharSequenceUtil.equalsIgnoreCase(shopInfoEntity.getDictCountryCode(), "BR");
    }
}
