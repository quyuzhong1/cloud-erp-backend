package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.dto.CfgRuleInvoiceAmountDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgInvoiceSettingDetailMapper;
import com.erp.server.oms.service.*;

import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <p>
 * 发票设置明细 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Slf4j
@Service
public class CfgInvoiceSettingDetailServiceImpl extends SuperServiceImpl<CfgInvoiceSettingDetailMapper, CfgInvoiceSettingDetailEntity> implements CfgInvoiceSettingDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    DictBasicService dictBasicService;

    @Resource
    CfgInvoiceSettingService cfgInvoiceSettingService;

    @Resource
    ShopInfoService shopInfoService;
    @Lazy
    @Resource
    private InvoiceInfoService invoiceInfoService;
    @Resource
    private CfgRuleInvoiceAmountService cfgRuleInvoiceAmountService;
    @Resource
    private RuleConditionService ruleConditionService;

    @Override
    public CfgInvoiceSettingDetailDTO.ViewDTO view(CfgInvoiceSettingDetailDTO.ViewParamsDTO dto) {
        CfgInvoiceSettingDetailDTO.ViewDTO viewDTOS = baseMapper.selectDetailsByMainIdGroupByPlatformWithRatioAdjusted(
                dto.getId(),
                dto.getKey(),
                dto.getNames()
        );
        if (ObjectUtil.isEmpty(viewDTOS)) {
            return viewDTOS;
        }
        //填充规则列表
        List<CfgRuleInvoiceAmountDTO.ViewDTO> productAmountDTOList = cfgRuleInvoiceAmountService.listByCfgId(dto.getId());
        if (CollUtil.isEmpty(productAmountDTOList)){
            return viewDTOS;
        }
        List<String> ruleIds = productAmountDTOList.stream().map(CfgRuleInvoiceAmountDTO.ViewDTO::getId).distinct().collect(Collectors.toList());
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleIds(ruleIds, DictBasicTypeEnum.FIELD.getType());
        productAmountDTOList.forEach(e -> {
            e.setDictInvoiceRuleName(InvoiceRuleEnum.getName(e.getDictInvoiceRule()));
            List<RuleConditionDTO.ViewDTO> collect = conditionList.stream().filter(f -> e.getId().equals(f.getRuleId())).collect(Collectors.toList());
            e.setConditionList(collect);
        });
        viewDTOS.setProductAmountDTOList(productAmountDTOList);
        return viewDTOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(CfgInvoiceSettingDetailDTO.AddOrUpdateDTO dto) {
        //发票设置id
        String mainId = dto.getMainId();
        // 参数校验
        if (ObjectUtil.isEmpty(dto)) {
            throw new ServiceException("请求参数不能为空");
        }
        // 校验主表是否存在
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = validateInvoiceSetting(mainId);
        if(ObjectUtil.isEmpty(cfgInvoiceSettingEntity)){
            throw new ServiceException("发票设置不存在！");
        }
        //处理清空逻辑
        if (CollUtil.isEmpty(dto.getDetailDTOList())){
            this.lambdaUpdate().in(CfgInvoiceSettingDetailEntity::getMainId, mainId).remove();
            //清空成功
            return new BaseResultDTO.AddDTO(mainId, mainId);
        }
        // 处理更新中的删除逻辑
        List<CfgInvoiceSettingDetailEntity> relatedToAddAndUpdateList= handleInvoiceSettingDetails(dto.getDetailDTOList(), mainId);
        //处理产品总价计算规则
        cfgRuleInvoiceAmountService.batchAddOrUpdate(dto.getProductAmountDTOList(), mainId);
        // 操作日志
        if (ObjectUtil.isNotEmpty(relatedToAddAndUpdateList)) {
            List<Pair<String, String>> pairList = relatedToAddAndUpdateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            String msg = StrUtil.format("用户【{}】修改【{}】绑定店铺id为【{}】",
                    UserContext.getDefaultLoginUser().getUserName(), "发票设置明细", relatedToAddAndUpdateList.stream().map(CfgInvoiceSettingDetailEntity::getId).collect(Collectors.toList()));
            operateLogService.batchAddModuleOperateLog(msg,
                    ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(), pairList, "新增操作");
        }
        return new BaseResultDTO.AddDTO(mainId, mainId);
    }

    @Override
    public List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList) {
        return baseMapper.listByShopIdList(shopIdList);
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewShopDTO> listShopSelect(String dictplatform) {
        List<ShopInfoEntity> shoplist = shopInfoService.list(new LambdaQueryWrapper<ShopInfoEntity>().eq(ShopInfoEntity::getDictPlatform, dictplatform)
                .eq(ShopInfoEntity::getIsDeleted, false).select(ShopInfoEntity::getId, ShopInfoEntity::getName,ShopInfoEntity::getDictPlatform, ShopInfoEntity::getDisabled));
        //记录已绑定的店铺id
        Set<String> usedShopidList = this.list(new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>().eq(CfgInvoiceSettingDetailEntity::getDictPlatform, dictplatform)
                        .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false).select(CfgInvoiceSettingDetailEntity::getShopId)).stream()
                .map(CfgInvoiceSettingDetailEntity::getShopId).collect(Collectors.toSet());
        // 转换为 ViewShopDTO，并设置 disabled 字段
        List<CfgInvoiceSettingDetailDTO.ViewShopDTO> viewShopDTOList = shoplist.stream()
                .map(shop -> {
                    CfgInvoiceSettingDetailDTO.ViewShopDTO dto = new CfgInvoiceSettingDetailDTO.ViewShopDTO();
                    dto.setId(shop.getId());
                    dto.setName(shop.getDictPlatform());
                    dto.setValue(shop.getName());
                    //场景
                    dto.setDisabled(Boolean.TRUE.equals(shop.getDisabled()) || usedShopidList.contains(shop.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return viewShopDTOList;
    }

    @Override
    public CfgInvoiceSettingDetailEntity getInvoiceSettingDetail(String dictPlatform, String shopId) {
        List<CfgInvoiceSettingDetailEntity> invoiceSettingDetail = baseMapper.getInvoiceSettingDetail(dictPlatform, shopId);
        if (CollUtil.isEmpty(invoiceSettingDetail)){
            return null;
        }else {
            return invoiceSettingDetail.get(0);
        }
    }

    @Override
    public List<CfgInvoiceSettingDetailEntity> listInvoiceSettingDetail(String dictPlatform, String shopId) {
        return baseMapper.getInvoiceSettingDetail(dictPlatform, shopId);
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO> listDictSelect(CfgInvoiceSettingDetailDTO.ParamsDictPlatformDTO dto) {
        List<DictBasicDTO.ViewDTO> platformList = dictBasicService.getByKey(dto.getKey());
        List<DictBasicDTO.ViewDTO> filtrationDictList = platformList.stream()
                .filter(p -> dto.getNames().contains(p.getValue()))
                .collect(Collectors.toList());
        List<CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO> viewDictPlatformDTOList = BeanUtil.copyToList(filtrationDictList, CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO.class);
        return viewDictPlatformDTOList;
    }

    @Override
    public void delateByMainIds(List<String> ids, Boolean aTrue) {
        boolean remove = this.lambdaUpdate().in(CfgInvoiceSettingDetailEntity::getMainId, ids).remove();
        return;
    }

    @Override
    public void generateNfeInvoice(SoB2cEntity soB2cEntity, String type) {
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = this.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        if (ObjUtil.isEmpty(invoiceSettingDetail)) {
            return;
        }
        if (CharSequenceUtil.equals(invoiceSettingDetail.getInvoiceNode(), InvoiceNodeEnum.NO_AUTO.getCode()) || !SoB2cNfeStatusEnum.PENDING.getCode().equals(soB2cEntity.getNfeInvoiceStatus())
                || CharSequenceUtil.equals(soB2cEntity.getNfeInvoiceStatus(), SoB2cNfeStatusEnum.UPLOAD_SUCCESS.getCode())) {
            return;
        }
        if (CharSequenceUtil.equals(type, invoiceSettingDetail.getInvoiceNode())) {
            try {
                invoiceInfoService.batchGenerateNfeInvoice(soB2cEntity.getId(),Boolean.FALSE);
            }catch (Exception e){
                log.error("生成nfe发票失败，{}",e.getMessage());
            }
        }
    }

    /**
     * @description: 校验发票设置是否存在
     * @author: hcg
     * @date: 2025/4/15 12:08
     * @param: mainId
     * @return: null
     **/
    private CfgInvoiceSettingEntity validateInvoiceSetting(String mainId) {
        CfgInvoiceSettingEntity entity = cfgInvoiceSettingService.getById(mainId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("发票设置不存在");
        }
        return entity;
    }

    /**
     * @description:add、update、add AND update过程中的delete
     * @author: hcg
     * @date: 2025/4/15 12:09
     * @param: dtoList
     * @return: null
     **/
    @Transactional(rollbackFor = Exception.class)
    public List<CfgInvoiceSettingDetailEntity> handleInvoiceSettingDetails(List<CfgInvoiceSettingDetailDTO.DetailListDTO> dtoList, String mainId) {
        // 1. 获取现有记录
        List<CfgInvoiceSettingDetailEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>()
                        .eq(CfgInvoiceSettingDetailEntity::getMainId, mainId)
                        .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false)
        );
        // 构建现有记录的 Map<id, entity>
        Map<String, CfgInvoiceSettingDetailEntity> existingMap = existingEntities.stream()
                .filter(item -> ObjectUtil.isNotEmpty(item.getId()))
                .collect(Collectors.toMap(
                        CfgInvoiceSettingDetailEntity::getId,
                        Function.identity(),
                        (a, b) -> a // 保留第一个
                ));
        // 2. 获取传入的ID集合
        Set<String> existingIds = existingMap.keySet();
        Set<String> incomingIds = dtoList.stream()
                .flatMap(dto -> dto.getDetailDTOList().stream())
                .map(CfgInvoiceSettingDetailDTO.CommonDTO::getId)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toSet());
        // 3. 处理需要删除的记录
        Set<String> idsToDelete = new HashSet<>(existingIds);
        idsToDelete.removeAll(incomingIds);
        // 4. 找出shopId变更的记录
        Set<String> idsWithChangedShopId = dtoList.stream()
                .flatMap(dto -> dto.getDetailDTOList().stream())
                .filter(detailDTO -> StrUtil.isNotEmpty(detailDTO.getId()))
                .filter(detailDTO -> {
                    CfgInvoiceSettingDetailEntity existing = existingMap.get(detailDTO.getId());
                    return existing != null && !Objects.equals(existing.getShopId(), detailDTO.getShopId());
                })
                .map(CfgInvoiceSettingDetailDTO.CommonDTO::getId)
                .collect(Collectors.toSet());

        // 将shopId变更的记录也加入删除集合
        idsToDelete.addAll(idsWithChangedShopId);
        // 5. 执行删除操作
        int deleteCount = 0;
        if (CollUtil.isNotEmpty(idsToDelete)) {
            log.info("删除发票设置明细: {}", idsToDelete);
            boolean removed = this.lambdaUpdate()
                    .in(CfgInvoiceSettingDetailEntity::getId, idsToDelete)
                    .remove();
            if (!removed) {
                throw new ServiceException("删除发票设置明细失败");
            }
            deleteCount = idsToDelete.size();
        }
        // 6. 准备新增和更新的实体列表
        List<CfgInvoiceSettingDetailEntity> createList = new ArrayList<>();
        List<CfgInvoiceSettingDetailEntity> updateList = new ArrayList<>();
        Iterator<CfgInvoiceSettingDetailDTO.DetailListDTO> dtoIterator = dtoList.iterator();
        while (dtoIterator.hasNext()) {
            CfgInvoiceSettingDetailDTO.DetailListDTO item = dtoIterator.next();
            String platformValue = item.getPlatformValue();
            Iterator<CfgInvoiceSettingDetailDTO.CommonDTO> detailIterator = item.getDetailDTOList().iterator();
            while (detailIterator.hasNext()) {
                CfgInvoiceSettingDetailDTO.CommonDTO detail = detailIterator.next();
                CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(detail, CfgInvoiceSettingDetailEntity.class);

                // 处理比率
                BigDecimal ratio = entity.getRatio();
                entity.setRatio((ratio == null ? BigDecimal.ZERO : ratio).divide(new BigDecimal("100")));
                entity.setDictPlatform(platformValue);
                entity.setMainId(mainId); // 确保设置了mainId
                if (Objects.nonNull(detail.getIsCheckIe()) && detail.getIsCheckIe()){
                    entity.setDictVerifyType(InvoiceVerifyTypeEnum.IE.getCode());
                }else {
                    entity.setDictVerifyType(InvoiceVerifyTypeEnum.NONE.getCode());
                }
                // 判断是新增还是更新
                String id = detail.getId();
                if (ObjectUtil.isEmpty(id) || idsWithChangedShopId.contains(id)) {
                    // 如果ID为空或者是shopId变更的记录，则为新增
                    if (idsWithChangedShopId.contains(id)) {
                        entity.setId(null); // 清空ID，让数据库生成新ID
                    }
                    createList.add(entity);
                } else if (existingMap.containsKey(id)) {
                    // 存在且未变更shopId的记录，为更新
                    updateList.add(entity);
                }
            }
        }
        // 7. 执行新增操作
        if (CollUtil.isNotEmpty(createList)) {
            if (!super.saveBatch(createList)) {
                throw new ServiceException("新增发票设置明细失败");
            }
        }
        // 8. 执行更新操作
        if (CollUtil.isNotEmpty(updateList)) {
            if (!super.updateBatchById(updateList)) {
                throw new ServiceException("更新发票设置明细失败");
            }
        }
        // 9. 返回处理结果统计
        updateList.addAll(createList);
        return updateList;
    }
}
