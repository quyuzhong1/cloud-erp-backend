package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.tms.dto.CfgDeclareRuleConditionDTO;
import com.erp.model.tms.dto.CfgDeclareRuleDTO;
import com.erp.model.tms.entity.CfgDeclareRuleConditionEntity;
import com.erp.model.tms.entity.CfgDeclareRuleEntity;
import com.erp.model.tms.enums.CfgDeclareRuleReceiverTypeEnum;
import com.erp.model.tms.enums.CfgDeclareRuleSenderTypeEnum;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.server.tms.mapper.CfgDeclareRuleMapper;
import com.erp.server.tms.service.CfgDeclareRuleConditionService;
import com.erp.server.tms.service.CfgDeclareRuleService;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CfgDeclareRuleServiceImpl extends SuperServiceImpl<CfgDeclareRuleMapper, CfgDeclareRuleEntity> implements CfgDeclareRuleService {

    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String RULE_NOT_FOUND_MESSAGE = "CfgDeclareRule not found";
    private static final String DUPLICATE_RULE_MESSAGE = "Duplicate ruleType/senderId/receiverId combination";
    private static final String SAVE_FAILED_MESSAGE = "CfgDeclareRule save failed";

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CfgDeclareRuleConditionService cfgDeclareRuleConditionService;
    @Resource
    private CommonService commonService;
    @Resource
    private SysFeign sysFeign;

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(CfgDeclareRuleDTO.AddDTO addDTO) {
        CfgDeclareRuleEntity entity = BeanMapperUtils.map(CfgDeclareRuleEntity.class, addDTO);
        validateUniqueWithDb(entity);
        fillCompanyNames(Collections.singletonList(entity));

        boolean saved = super.save(entity);
        if (!saved) {
            throw new ServiceException(SAVE_FAILED_MESSAGE);
        }

        saveRuleConditions(entity.getId(), addDTO.getDetailList());
        addCreateLog(entity);
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(CfgDeclareRuleDTO.SaveListDTO dto) {
        List<CfgDeclareRuleDTO.SaveDTO> saveList = Optional.ofNullable(dto.getList()).orElse(Collections.emptyList());
        List<CfgDeclareRuleEntity> existingRules = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, dto.getRuleType())
                .list();
        Map<String, CfgDeclareRuleEntity> existingRuleMap = existingRules.stream()
                .collect(Collectors.toMap(CfgDeclareRuleEntity::getId, item -> item, (left, right) -> left));

        validateBatchSaveRequest(dto.getRuleType(), saveList, existingRuleMap);

        List<CfgDeclareRuleEntity> saveEntities = saveList.stream()
                .map(item -> {
                    CfgDeclareRuleEntity entity = BeanMapperUtils.map(CfgDeclareRuleEntity.class, item);
                    entity.setRuleType(dto.getRuleType());
                    return entity;
                })
                .collect(Collectors.toList());
        fillCompanyNames(saveEntities);

        List<String> deleteRuleIds = getDeleteRuleIds(existingRules, saveList);
        removeRules(deleteRuleIds, existingRuleMap);

        for (int i = 0; i < saveList.size(); i++) {
            persistRule(saveEntities.get(i), saveList.get(i), existingRuleMap);
        }
        return Boolean.TRUE;
    }

    @Override
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgDeclareRuleDTO.UpdateDTO addOrUpdateDTO) {
        CfgDeclareRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "CfgDeclareRule"));

        CfgDeclareRuleEntity entity = BeanMapperUtils.map(CfgDeclareRuleEntity.class, addOrUpdateDTO);
        validateUniqueWithDb(entity);
        fillCompanyNames(Collections.singletonList(entity));

        boolean updated = super.updateById(entity);
        if (!updated) {
            throw new ServiceException(SAVE_FAILED_MESSAGE);
        }

        List<CfgDeclareRuleConditionEntity> oldDetails = cfgDeclareRuleConditionService.lambdaQuery()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, entity.getId())
                .list();
        List<CfgDeclareRuleConditionEntity> newDetails = buildConditionEntities(entity.getId(), addOrUpdateDTO.getDetailList(), true);
        commonService.updateDetail(entity.getId(), ModuleTypeEnum.CFG_DECLARE_RULE.getCode(),
                cfgDeclareRuleConditionService, newDetails, oldDetails, Collections.singletonList("id"));

        addUpdateLog(old, entity);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgDeclareRuleDTO.ListDTO> paging(CfgDeclareRuleDTO.ListParamDTO dto) {
        List<CfgDeclareRuleDTO.ListDTO> list = this.baseMapper.paging(dto);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        fillList(list);
        return list;
    }

    @Override
    public void exportList(CfgDeclareRuleDTO.ExportDTO param, HttpServletResponse response) {
        List<CfgDeclareRuleDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        fillList(list);

        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/cfgDeclareRule.xlsx";
        String name = "\u62a5\u5173\u89c4\u5219\u4e3b\u5355\u5bfc\u51fa";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public CfgDeclareRuleDTO.ViewDTO view(String id) {
        CfgDeclareRuleEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(RULE_NOT_FOUND_MESSAGE));
        CfgDeclareRuleDTO.ViewDTO data = BeanMapperUtils.map(CfgDeclareRuleDTO.ViewDTO.class, entity);
        List<CfgDeclareRuleConditionEntity> conditions = cfgDeclareRuleConditionService.lambdaQuery()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgDeclareRuleConditionEntity::getIndex)
                .list();
        if (CollUtil.isNotEmpty(conditions)) {
            data.setDetailList(BeanMapperUtils.copyList(CfgDeclareRuleConditionDTO.ListDTO.class, conditions));
        }
        return data;
    }

    @Override
    public List<BaseDropDownDTO.Tree> dropDownList(String type, String name) {
        if (SENDER.equals(type)) {
            return Collections.singletonList(buildDropDown(
                    type,
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
        }

        if (RECEIVER.equals(type)) {
            List<BaseDropDownDTO.Tree> result = new ArrayList<>(2);
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getName(),
                    accountingCompanyChildList(name)));
            result.add(buildDropDown(
                    type,
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getCode(),
                    CfgDeclareRuleReceiverTypeEnum.BY_CUSTOMER.getName(),
                    Collections.emptyList()));
            return result;
        }

        throw new ServiceException("type must be sender or receiver");
    }

    void validateBatchSaveRequest(String ruleType,
                                  List<CfgDeclareRuleDTO.SaveDTO> saveList,
                                  Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgDeclareRuleDTO.SaveDTO item : saveList) {
            if (!StrUtil.equals(ruleType, item.getRuleType())) {
                throw new ServiceException("request ruleType does not match item ruleType");
            }
            if (CollUtil.isEmpty(item.getDetailList())) {
                throw new ServiceException("detailList can not be empty");
            }
            if (StrUtil.isNotBlank(item.getId())) {
                CfgDeclareRuleEntity existingRule = existingRuleMap.get(item.getId());
                if (existingRule == null) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "CfgDeclareRule");
                }
                if (!StrUtil.equals(ruleType, existingRule.getRuleType())) {
                    throw new ServiceException("cross-ruleType update is not allowed");
                }
            }

            String uniqueKey = buildUniqueKey(item.getRuleType(), item.getSenderId(), item.getReceiverId());
            if (!uniqueKeySet.add(uniqueKey)) {
                throw new ServiceException(ApiError.BILL_ALREADY_EXIST, DUPLICATE_RULE_MESSAGE);
            }
        }
    }

    List<String> getDeleteRuleIds(Collection<CfgDeclareRuleEntity> existingRules, List<CfgDeclareRuleDTO.SaveDTO> saveList) {
        Set<String> keepIdSet = saveList.stream()
                .map(CfgDeclareRuleDTO.SaveDTO::getId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        return existingRules.stream()
                .map(CfgDeclareRuleEntity::getId)
                .filter(id -> !keepIdSet.contains(id))
                .collect(Collectors.toList());
    }

    List<CfgDeclareRuleConditionEntity> buildConditionEntities(String ruleId,
                                                               List<? extends CfgDeclareRuleConditionDTO.CommonDTO> detailList,
                                                               boolean keepId) {
        if (CollUtil.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        List<CfgDeclareRuleConditionEntity> entities = new ArrayList<>(detailList.size());
        for (int i = 0; i < detailList.size(); i++) {
            CfgDeclareRuleConditionEntity entity = BeanMapperUtils.map(CfgDeclareRuleConditionEntity.class, detailList.get(i));
            entity.setRuleId(ruleId);
            entity.setIndex(i);
            if (!keepId) {
                entity.setId(null);
            }
            entities.add(entity);
        }
        return entities;
    }

    void applyCompanyNames(List<CfgDeclareRuleEntity> ruleList, Map<String, String> companyMap) {
        if (CollUtil.isEmpty(ruleList) || CollUtil.isEmpty(companyMap)) {
            return;
        }
        for (CfgDeclareRuleEntity entity : ruleList) {
            if (StrUtil.equals(entity.getSenderType(), CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode())
                    && companyMap.containsKey(entity.getSenderId())) {
                entity.setSenderName(companyMap.get(entity.getSenderId()));
            }
            if (StrUtil.equals(entity.getReceiverType(), CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode())
                    && companyMap.containsKey(entity.getReceiverId())) {
                entity.setReceiverName(companyMap.get(entity.getReceiverId()));
            }
        }
    }

    private void persistRule(CfgDeclareRuleEntity entity,
                             CfgDeclareRuleDTO.SaveDTO saveDTO,
                             Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        CfgDeclareRuleEntity old = StrUtil.isNotBlank(entity.getId()) ? existingRuleMap.get(entity.getId()) : null;
        if (old != null) {
            boolean updated = super.updateById(entity);
            if (!updated) {
                throw new ServiceException(SAVE_FAILED_MESSAGE);
            }
            refreshRuleConditions(entity.getId(), saveDTO.getDetailList());
            addUpdateLog(old, entity);
            return;
        }

        boolean saved = super.save(entity);
        if (!saved) {
            throw new ServiceException(SAVE_FAILED_MESSAGE);
        }
        refreshRuleConditions(entity.getId(), saveDTO.getDetailList());
        addCreateLog(entity);
    }

    private void refreshRuleConditions(String ruleId, List<CfgDeclareRuleConditionDTO.SaveDTO> detailList) {
        cfgDeclareRuleConditionService.lambdaUpdate()
                .eq(CfgDeclareRuleConditionEntity::getRuleId, ruleId)
                .remove();
        List<CfgDeclareRuleConditionEntity> conditions = buildConditionEntities(ruleId, detailList, false);
        if (CollUtil.isNotEmpty(conditions)) {
            cfgDeclareRuleConditionService.saveBatch(conditions);
        }
    }

    private void saveRuleConditions(String ruleId, List<CfgDeclareRuleConditionDTO.AddDTO> detailList) {
        List<CfgDeclareRuleConditionEntity> conditions = buildConditionEntities(ruleId, detailList, false);
        if (CollUtil.isNotEmpty(conditions)) {
            cfgDeclareRuleConditionService.saveBatch(conditions);
        }
    }

    private void removeRules(List<String> deleteRuleIds, Map<String, CfgDeclareRuleEntity> existingRuleMap) {
        if (CollUtil.isEmpty(deleteRuleIds)) {
            return;
        }
        cfgDeclareRuleConditionService.lambdaUpdate()
                .in(CfgDeclareRuleConditionEntity::getRuleId, deleteRuleIds)
                .remove();
        super.removeByIds(deleteRuleIds);
        deleteRuleIds.stream()
                .map(existingRuleMap::get)
                .filter(ObjectUtil::isNotNull)
                .forEach(this::addDeleteLog);
    }

    private void validateUniqueWithDb(CfgDeclareRuleEntity entity) {
        if (!ObjectUtil.isAllNotEmpty(entity.getRuleType(), entity.getSenderId(), entity.getReceiverId())) {
            return;
        }
        CfgDeclareRuleEntity exist = this.lambdaQuery()
                .eq(CfgDeclareRuleEntity::getRuleType, entity.getRuleType())
                .eq(CfgDeclareRuleEntity::getSenderId, entity.getSenderId())
                .eq(CfgDeclareRuleEntity::getReceiverId, entity.getReceiverId())
                .ne(StrUtil.isNotBlank(entity.getId()), CfgDeclareRuleEntity::getId, entity.getId())
                .last("LIMIT 1")
                .one();
        if (exist != null) {
            throw new ServiceException(ApiError.BILL_ALREADY_EXIST, DUPLICATE_RULE_MESSAGE);
        }
    }

    private void fillCompanyNames(List<CfgDeclareRuleEntity> ruleList) {
        if (CollUtil.isEmpty(ruleList)) {
            return;
        }
        Set<String> companyIds = new HashSet<>();
        for (CfgDeclareRuleEntity entity : ruleList) {
            if (StrUtil.isNotBlank(entity.getSenderId())
                    && StrUtil.equals(entity.getSenderType(), CfgDeclareRuleSenderTypeEnum.BY_COMPANY.getCode())) {
                companyIds.add(entity.getSenderId());
            }
            if (StrUtil.isNotBlank(entity.getReceiverId())
                    && StrUtil.equals(entity.getReceiverType(), CfgDeclareRuleReceiverTypeEnum.BY_COMPANY.getCode())) {
                companyIds.add(entity.getReceiverId());
            }
        }
        if (CollUtil.isEmpty(companyIds)) {
            return;
        }

        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList("");
        if (companyResult == null || !companyResult.isSuccess() || CollUtil.isEmpty(companyResult.getData())) {
            return;
        }

        Map<String, String> companyMap = companyResult.getData().stream()
                .filter(company -> companyIds.contains(company.getId()))
                .collect(Collectors.toMap(SysAccountingCompanyDTO.ListDTO::getId,
                        SysAccountingCompanyDTO.ListDTO::getCompanyName,
                        (left, right) -> left,
                        HashMap::new));
        applyCompanyNames(ruleList, companyMap);
    }

    private String buildUniqueKey(String ruleType, String senderId, String receiverId) {
        return StrUtil.join("|", ruleType, senderId, receiverId);
    }

    private void addCreateLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("User [{}] created cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "create");
    }

    private void addUpdateLog(CfgDeclareRuleEntity oldEntity, CfgDeclareRuleEntity newEntity) {
        String msg = StrUtil.format("User [{}] updated cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), newEntity.getId());
        operateLogService.addModuleOperateLogByObj(oldEntity, newEntity,
                ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), newEntity.getId(), msg);
    }

    private void addDeleteLog(CfgDeclareRuleEntity entity) {
        String msg = StrUtil.format("User [{}] deleted cfg declare rule [{}]",
                UserContext.getDefaultLoginUser().getUserName(), entity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_DECLARE_RULE.getCode(), entity.getId(), "delete");
    }

    private void fillList(List<CfgDeclareRuleDTO.ListDTO> list) {
        for (CfgDeclareRuleDTO.ListDTO data : list) {
            if (StrUtil.isNotEmpty(data.getRuleType())) {
                if ("fmDeclareBill".equals(data.getRuleType())) {
                    data.setRuleTypeName("\u5934\u7a0b\u62a5\u5173\u5355");
                } else if ("b2bDeclareBill".equals(data.getRuleType())) {
                    data.setRuleTypeName("B2B\u62a5\u5173\u5355");
                }
            }
            data.setSenderTypeName(CfgDeclareRuleSenderTypeEnum.getName(data.getSenderType()));
            data.setReceiverTypeName(CfgDeclareRuleReceiverTypeEnum.getName(data.getReceiverType()));
        }
    }

    private BaseDropDownDTO.Tree buildDropDown(String type,
                                               String code,
                                               String value,
                                               List<BaseDropDownDTO.ChildTree> childTreeList) {
        BaseDropDownDTO.Tree tree = new BaseDropDownDTO.Tree();
        tree.setType(type);
        tree.setCode(code);
        tree.setValue(value);
        tree.setDisabled(Boolean.FALSE);
        tree.setChildTreeList(childTreeList);
        return tree;
    }

    private List<BaseDropDownDTO.ChildTree> accountingCompanyChildList(String name) {
        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList(name);
        if (companyResult == null || !companyResult.isSuccess()) {
            throw new ServiceException("load accounting company list failed");
        }
        return Optional.ofNullable(companyResult.getData())
                .orElse(Collections.emptyList())
                .stream()
                .map(company -> BaseDropDownDTO.ChildTree.builder()
                        .code(company.getId())
                        .value(company.getCompanyName())
                        .disabled(company.getDisabled())
                        .build())
                .collect(Collectors.toList());
    }
}
