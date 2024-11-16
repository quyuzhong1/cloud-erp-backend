package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.ReceiverDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.entity.CfgRuleOrderHandleEntity;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleOrderHandleEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.vo.request.LogisticsOrderRuleVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.wms.dto.third.ThirdWarehouseCreateOutboundReq;
import com.erp.server.oms.mapper.CfgRuleOrderHandleMapper;
import com.erp.server.oms.service.CfgRuleOrderHandleService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RuleConditionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单处理规则表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-05-09
 */
@Slf4j
@Service
public class CfgRuleOrderHandleServiceImpl extends SuperServiceImpl<CfgRuleOrderHandleMapper, CfgRuleOrderHandleEntity> implements CfgRuleOrderHandleService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private RuleConditionService ruleConditionService;

    @Resource
    private SpElServer spElServer;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleOrderHandleDTO.AddDTO addDTO) {
        CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity = new CfgRuleOrderHandleEntity();
        //处理规则详情
        Map<String, Object> ruleMap = BeanUtil.beanToMap(addDTO.getRuleContent());
        cfgRuleOrderHandleEntity.setRuleContent(ruleMap);
        BeanMapperUtils.copy(addDTO, cfgRuleOrderHandleEntity);

        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        //数据校验
        checkData(cfgRuleOrderHandleEntity,conditionElementList);

        log.info("开始新增订单处理规则单");
        boolean save = super.save(cfgRuleOrderHandleEntity);
        if(!save) {
            throw new ServiceException("订单处理规则单保存失败");
        }
        //保存规则条件
        ruleConditionService.saveRuleCondition(cfgRuleOrderHandleEntity.getId(), conditionList);

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "订单处理规则单" , cfgRuleOrderHandleEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), cfgRuleOrderHandleEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgRuleOrderHandleEntity.getId(), cfgRuleOrderHandleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleOrderHandleDTO.UpdateDTO updateDTO) {
        CfgRuleOrderHandleEntity old = super.getById(updateDTO.getId());
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "订单处理规则单");
        }
        CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity =  BeanMapperUtils.map(CfgRuleOrderHandleEntity.class, updateDTO);
        Map<String, Object> ruleMap = BeanUtil.beanToMap(updateDTO.getRuleContent());
        cfgRuleOrderHandleEntity.setRuleContent(ruleMap);
        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        //数据校验
        checkData(cfgRuleOrderHandleEntity,conditionElementList);

        log.info("编辑 开始修改订单处理规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleOrderHandleEntity);
        if(!save) {
            throw new ServiceException("订单处理规则单保存失败");
        }
        //保存规则条件
        ruleConditionService.updateRuleCondition(cfgRuleOrderHandleEntity.getId(), updateDTO.getConditionList());

        // 记录主单操作日志
        log.info("编辑 开始记录订单处理规则单日志数据，id：【{}】", cfgRuleOrderHandleEntity.getId());
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleOrderHandleEntity.getId(), "订单处理规则单");
        CfgRuleOrderHandleDTO.LogDTO oldView = this.buildLogDTO(old);
        CfgRuleOrderHandleDTO.LogDTO newView = this.buildLogDTO(cfgRuleOrderHandleEntity);
        operateLogService.addModuleOperateLogByObj(oldView, newView, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), cfgRuleOrderHandleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    private CfgRuleOrderHandleDTO.LogDTO buildLogDTO(CfgRuleOrderHandleEntity old) {
        CfgRuleOrderHandleDTO.LogDTO logDTO = BeanUtil.copyProperties(old,CfgRuleOrderHandleDTO.LogDTO.class,"ruleContent");
        JSONObject jsonObject = new JSONObject(old.getRuleContent());
        CfgRuleOrderHandleDTO.RuleContent ruleDTO = JSON.parseObject(jsonObject.toJSONString(),new TypeReference<CfgRuleOrderHandleDTO.RuleContent>() {}.getType());
        logDTO.setReceiveHandleContent(ruleDTO.getReceiveHandleContent());
        logDTO.setAddressHandlerContent(ruleDTO.getAddressHandlerContent());
        logDTO.setPhoneHandleContent(ruleDTO.getPhoneHandleContent());
        logDTO.setZipCodeHandleContent(ruleDTO.getZipCodeHandleContent());
        //日志组件不支持嵌套List,特殊处理，将List挪到外层
        if(Objects.nonNull(logDTO.getAddressHandlerContent()) && Objects.nonNull(logDTO.getAddressHandlerContent().getFilterAddress1TextList())){
            logDTO.setFilterAddressOneTextList(logDTO.getAddressHandlerContent().getFilterAddress1TextList());
            logDTO.getAddressHandlerContent().setFilterAddress1TextList(null);
            logDTO.getAddressHandlerContent().setFilterAddress1TextNameList(null);
        }
        if(Objects.nonNull(logDTO.getPhoneHandleContent()) && Objects.nonNull(logDTO.getPhoneHandleContent().getFilterPhoneTextList())){
            logDTO.setFilterPhoneTextList(logDTO.getPhoneHandleContent().getFilterPhoneTextList());
            logDTO.getPhoneHandleContent().setFilterPhoneTextList(null);
            logDTO.getPhoneHandleContent().setFilterPhoneTextNameList(null);
        }
        if(Objects.nonNull(logDTO.getZipCodeHandleContent()) && Objects.nonNull(logDTO.getZipCodeHandleContent().getFilterZipCodeTextList())){
            logDTO.setFilterZipCodeTextList(logDTO.getZipCodeHandleContent().getFilterZipCodeTextList());
            logDTO.getZipCodeHandleContent().setFilterZipCodeTextList(null);
            logDTO.getZipCodeHandleContent().setFilterZipCodeTextNameList(null);
        }
        if(Objects.nonNull(logDTO.getReceiveHandleContent()) && Objects.nonNull(logDTO.getReceiveHandleContent().getFilterReceiveTextList())){
            logDTO.setFilterReceiveTextList(logDTO.getReceiveHandleContent().getFilterReceiveTextList());
            logDTO.getReceiveHandleContent().setFilterReceiveTextList(null);
            logDTO.getReceiveHandleContent().setFilterReceiveTextNameList(null);
        }
        return logDTO;
    }

    @Override
    public PagingVO<CfgRuleOrderHandleDTO.ListDTO> paging(PagingDTO<CfgRuleOrderHandleDTO.PagingParamDTO> dto) {
        CfgRuleOrderHandleDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleOrderHandleDTO.ListDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);
    }

    @Override
    public CfgRuleOrderHandleDTO.ViewDTO view(String id) {
        CfgRuleOrderHandleEntity ruleOrderHandle = this.getById(id);
        if(null == ruleOrderHandle){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "订单处理规则");
        }
        CfgRuleOrderHandleDTO.ViewDTO view = new CfgRuleOrderHandleDTO.ViewDTO();
        BeanMapper.copy(ruleOrderHandle, view);
        JSONObject jsonObject = new JSONObject(ruleOrderHandle.getRuleContent());
        CfgRuleOrderHandleDTO.RuleContent ruleDTO = JSON.parseObject(jsonObject.toJSONString(),new TypeReference<CfgRuleOrderHandleDTO.RuleContent>() {}.getType());
        ruleDTO.getAddressHandlerContent().setFilterAddress1TextNameList(EnumMessage.listNameByCodes(RuleOrderHandleEnum.Address1FilterEnum.class,ruleDTO.getAddressHandlerContent().getFilterAddress1TextList()));
        ruleDTO.getPhoneHandleContent().setFilterPhoneTextNameList(EnumMessage.listNameByCodes(RuleOrderHandleEnum.PhoneFilterEnum.class,ruleDTO.getPhoneHandleContent().getFilterPhoneTextList()));
        ruleDTO.getReceiveHandleContent().setFilterReceiveTextNameList(EnumMessage.listNameByCodes(RuleOrderHandleEnum.ReceiveFilterEnum.class,ruleDTO.getReceiveHandleContent().getFilterReceiveTextList()));
        ruleDTO.getZipCodeHandleContent().setFilterZipCodeTextNameList(EnumMessage.listNameByCodes(RuleOrderHandleEnum.ZipCodeFilterEnum.class,ruleDTO.getZipCodeHandleContent().getFilterZipCodeTextList()));

        view.setRuleContent(ruleDTO);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        return view;
    }

    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        CfgRuleOrderHandleEntity ruleOrderHandle = this.getById(dto.getId());
        if(null == ruleOrderHandle){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流规则单");
        }
        Boolean disabled = ruleOrderHandle.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", Boolean.TRUE.equals(disabled) ? "启用" : "停用", Boolean.TRUE.equals(disabled) ? "停用" : "启用");
        ruleOrderHandle.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.CFG_RULE_ORDER_HANDLE.getCode(), dto.getId(), "状态更新");
        return this.updateById(ruleOrderHandle);
    }

    @Override
    public CfgRuleOrderHandleDTO.RuleMatchDTO getRuleOrderHandleMatchResult(Map<String, Object> map) {
        CfgRuleOrderHandleDTO.RuleMatchDTO ruleMatch = new CfgRuleOrderHandleDTO.RuleMatchDTO();
        ruleMatch.setApproveSuccess(Boolean.FALSE);
        if (Objects.isNull(map)) {
            return ruleMatch;
        }
        log.info("参数为=========={}", map);
        List<CfgRuleOrderHandleEntity> ruleOrderHandleList = this.listRuleOrderHandleByPriority();
        List<String> ruleIdList = ruleOrderHandleList.stream().map(CfgRuleOrderHandleEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (CfgRuleOrderHandleEntity item : ruleOrderHandleList) {
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            if (Boolean.TRUE.equals(matchResult)) {
                ruleMatch.setApproveSuccess(Boolean.TRUE);
                ruleMatch.setRuleName(item.getName());
                JSONObject jsonObject = new JSONObject(item.getRuleContent());
                CfgRuleOrderHandleDTO.RuleContent ruleDTO = JSON.parseObject(jsonObject.toJSONString(),new TypeReference<CfgRuleOrderHandleDTO.RuleContent>() {}.getType());
                ruleMatch.setRuleContent(ruleDTO);
                return ruleMatch;
            }
        }
        return ruleMatch;
    }

    @Override
    public LogisticsOrderVO handleRuleOrderLogistic(LogisticsOrderRuleVO logisticsOrderRuleVO) {
        CfgRuleOrderHandleDTO.RuleMatchDTO ruleMatchDTO = this.getRuleOrderHandleMatchResult(logisticsOrderRuleVO.getMap());
        LogisticsOrderVO logisticsOrderVO = logisticsOrderRuleVO.getLogisticsOrderVO();
        if(Boolean.TRUE.equals(ruleMatchDTO.getApproveSuccess())){
            //处理地址
            this.handleAddressRule(logisticsOrderVO.getReceiverInfoVO(),ruleMatchDTO.getRuleContent());
            //处理电话
            this.handlePhoneRule(logisticsOrderVO.getReceiverInfoVO(),ruleMatchDTO.getRuleContent());
            //处理邮编
            this.handleZipCodeRule(logisticsOrderVO.getReceiverInfoVO(),ruleMatchDTO.getRuleContent());
            //处理收货人
            this.handleReceiveRule(logisticsOrderVO.getReceiverInfoVO(),ruleMatchDTO.getRuleContent());
        }
        return logisticsOrderVO;
    }

    @Override
    public ThirdWarehouseCreateOutboundReq handleRuleOrderThirdWarehouse(ThirdWarehouseCreateOutboundReq createOutboundReq, Map<String, Object> map) {
        CfgRuleOrderHandleDTO.RuleMatchDTO ruleMatchDTO = this.getRuleOrderHandleMatchResult(map);
        if(Boolean.TRUE.equals(ruleMatchDTO.getApproveSuccess())){
            //处理地址
            this.handleAddressRule(createOutboundReq.getReceiverInfo(),ruleMatchDTO.getRuleContent());
            //处理电话
            this.handlePhoneRule(createOutboundReq.getReceiverInfo(),ruleMatchDTO.getRuleContent());
            //处理邮编
            this.handleZipCodeRule(createOutboundReq.getReceiverInfo(),ruleMatchDTO.getRuleContent());
            //处理收货人
            this.handleReceiveRule(createOutboundReq.getReceiverInfo(),ruleMatchDTO.getRuleContent());
        }
        return createOutboundReq;
    }



    /**
     * 根据规则名称查询
     */
    private CfgRuleOrderHandleEntity getByName(String name) {
        return lambdaQuery().eq(CfgRuleOrderHandleEntity::getName, name).one();
    }

    /**
     * @description: 数据校验
     * @author Will
     * @date: 2024/5/9 10:43
     * @param cfgRuleOrderHandleEntity
     */
    private void checkData(CfgRuleOrderHandleEntity cfgRuleOrderHandleEntity,List<ConditionElement> conditionElementList) {
        //校验名称是否存在
        CfgRuleOrderHandleEntity old = this.getByName(cfgRuleOrderHandleEntity.getName());
        if (ObjectUtil.isNotEmpty(old) && !CharSequenceUtil.equals(cfgRuleOrderHandleEntity.getId(),old.getId())) {
            throw new ServiceException(ApiError.ERROR_NAME_EXIST,cfgRuleOrderHandleEntity.getName());
        }
        //校验规则表达式是否有效
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
    }

    /**
     * 根据有限级获取到订单审核
     *
     * @return
     */
    private List<CfgRuleOrderHandleEntity> listRuleOrderHandleByPriority() {
        return this.lambdaQuery().eq(CfgRuleOrderHandleEntity::getDisabled, Boolean.FALSE).
                orderByAsc(CfgRuleOrderHandleEntity::getPriority).
                orderByDesc(CfgRuleOrderHandleEntity::getUpdateTime).
                list();
    }

    /**
     * 地址处理
     * @param ruleContent
     */
    private <T extends ReceiverDTO> void handleAddressRule(T receiverInfoVO,CfgRuleOrderHandleDTO.RuleContent ruleContent){
        CfgRuleOrderHandleDTO.AddressHandleContent addressHandleContent = ruleContent.getAddressHandlerContent();
        if(Objects.isNull(addressHandleContent)){
            return;
        }
        //处理州/省
        if(addressHandleContent.isProvinceSwitch()){
            RuleOrderHandleEnum.ProvinceRuleContentEnum provinceRuleContentEnum = EnumMessage.getByCode(RuleOrderHandleEnum.ProvinceRuleContentEnum.class,addressHandleContent.getHandleProvinceRule());
            if(Objects.nonNull(provinceRuleContentEnum)) {
                switch (provinceRuleContentEnum) {
                    case REPLACE_WITH_CITY:
                        if (StringUtils.isBlank(receiverInfoVO.getProvince())) {
                            receiverInfoVO.setProvince(receiverInfoVO.getCity());
                        }
                        break;
                    case REPLACE_BLANK:
                        receiverInfoVO.setProvince(null);
                        break;
                    case CUSTOM_REPLACE:
                        if (StringUtils.isNotBlank(receiverInfoVO.getProvince()) && StringUtils.isNotBlank(addressHandleContent.getProvinceWaitReplaceText()) && receiverInfoVO.getProvince().equals(addressHandleContent.getProvinceWaitReplaceText())) {
                            receiverInfoVO.setProvince(addressHandleContent.getProvinceReplaceText());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if(addressHandleContent.isCitySwitch()){
            RuleOrderHandleEnum.CityRuleContentEnum cityRuleContentEnum = EnumMessage.getByCode(RuleOrderHandleEnum.CityRuleContentEnum.class,addressHandleContent.getHandleCityRule());
            if(Objects.nonNull(cityRuleContentEnum)){
                switch (cityRuleContentEnum) {
                    case REPLACE_WITH_PROVINCE:
                        if (StringUtils.isBlank(receiverInfoVO.getCity())) {
                            receiverInfoVO.setCity(receiverInfoVO.getProvince());
                        }
                        break;
                    case REPLACE_BLANK:
                        receiverInfoVO.setCity(null);
                        break;
                    case CUSTOM_REPLACE:
                        if (StringUtils.isNotBlank(receiverInfoVO.getCity()) && StringUtils.isNotBlank(addressHandleContent.getCityWaitReplaceText()) && receiverInfoVO.getCity().equals(addressHandleContent.getCityWaitReplaceText())) {
                            receiverInfoVO.setCity(addressHandleContent.getCityReplaceText());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if(addressHandleContent.isAddress1FilterSwitch() && StringUtils.isNotBlank(receiverInfoVO.getAddressFirst()) && CollectionUtils.isNotEmpty((addressHandleContent.getFilterAddress1TextList()))){
                for (String filterStr : addressHandleContent.getFilterAddress1TextList()) {
                    receiverInfoVO.setAddressFirst(receiverInfoVO.getAddressFirst().replace(filterStr, ""));
                }
            }

        if(addressHandleContent.isAddress1ReplaceSwitch() && StringUtils.isNotBlank(receiverInfoVO.getAddressFirst()) && StringUtils.isNotBlank(addressHandleContent.getAddress1WaitReplaceText())&& StringUtils.isNotBlank(addressHandleContent.getAddress1ReplaceText())){
                receiverInfoVO.setAddressFirst(receiverInfoVO.getAddressFirst().replace(addressHandleContent.getAddress1WaitReplaceText(), addressHandleContent.getAddress1ReplaceText()));
            }

    }

    /**
     * 电话处理
     * @param receiverInfoVO
     * @param ruleContent
     */
    private <T extends ReceiverDTO> void handlePhoneRule(T receiverInfoVO,CfgRuleOrderHandleDTO.RuleContent ruleContent){
        CfgRuleOrderHandleDTO.PhoneHandleContent phoneHandleContent = ruleContent.getPhoneHandleContent();
        if(Objects.isNull(phoneHandleContent)){
            return;
        }
        //过滤指定字符
        if(phoneHandleContent.isPhoneFilterSwitch() && StringUtils.isNotBlank(receiverInfoVO.getTelNumber()) && CollectionUtils.isNotEmpty(phoneHandleContent.getFilterPhoneTextList())){
                for (String filterStr : phoneHandleContent.getFilterPhoneTextList()) {
                    receiverInfoVO.setTelNumber(receiverInfoVO.getTelNumber().replace(filterStr, ""));
                }
            }

        //截取
        if(phoneHandleContent.isPhoneInterceptSwitch()){
            Integer startIndex = phoneHandleContent.getPhoneInterceptStartIndex();
            if(Objects.nonNull(startIndex)){
                startIndex = startIndex - 1;
                if(StringUtils.isNotBlank(receiverInfoVO.getTelNumber())  && startIndex > 0 && startIndex <= receiverInfoVO.getTelNumber().length()){
                    receiverInfoVO.setTelNumber(receiverInfoVO.getTelNumber().substring(startIndex));
                }
            }

        }
        //为空填充
        if(phoneHandleContent.isPhoneEmptyFillSwitch() && StringUtils.isBlank(receiverInfoVO.getTelNumber())){
                receiverInfoVO.setTelNumber(phoneHandleContent.getPhoneEmptyFillText());
            }

    }

    /**
     * 邮编处理
     * @param receiverInfoVO
     * @param ruleContent
     */
    private <T extends ReceiverDTO> void handleZipCodeRule(T receiverInfoVO,CfgRuleOrderHandleDTO.RuleContent ruleContent){
        CfgRuleOrderHandleDTO.ZipCodeHandleContent zipCodeHandleContent = ruleContent.getZipCodeHandleContent();
        if(Objects.isNull(zipCodeHandleContent)){
            return;
        }
        //过滤特殊字符
        if(zipCodeHandleContent.isZipCodeFilterSwitch() && StringUtils.isNotBlank(receiverInfoVO.getZipCode()) && CollectionUtils.isNotEmpty(zipCodeHandleContent.getFilterZipCodeTextList())){
                for (String filterStr : zipCodeHandleContent.getFilterZipCodeTextList()) {
                    receiverInfoVO.setZipCode(receiverInfoVO.getZipCode().replace(filterStr, ""));
                }
            }

        //为空填充
        if(zipCodeHandleContent.isZipCodeEmptyFillSwitch() && StringUtils.isBlank(receiverInfoVO.getZipCode())){
                receiverInfoVO.setZipCode(zipCodeHandleContent.getZipCodeEmptyFillText());
            }

    }
    /**
     * 收货人处理
     * @param receiverInfoVO
     * @param ruleContent
     */
    private <T extends ReceiverDTO> void handleReceiveRule(T receiverInfoVO,CfgRuleOrderHandleDTO.RuleContent ruleContent){
        CfgRuleOrderHandleDTO.ReceiveHandleContent receiveHandleContent = ruleContent.getReceiveHandleContent();
        if(Objects.isNull(receiveHandleContent)){
            return;
        }
        //为空填充
        if(receiveHandleContent.isReceiveEmptyFillSwitch()){
            RuleOrderHandleEnum.ReceiveFillRuleContentEnum receiveFillRuleContentEnum = EnumMessage.getByCode(RuleOrderHandleEnum.ReceiveFillRuleContentEnum.class,receiveHandleContent.getHandleReceiveEmptyFillRule());
            if(Objects.nonNull(receiveFillRuleContentEnum) && StringUtils.isBlank(receiverInfoVO.getContact())){
                switch (receiveFillRuleContentEnum){
                    case FILL_WITH_CUSTOMER_NAME:
                        receiverInfoVO.setContact(receiverInfoVO.getBuyerName());
                        break;
                    case CUSTOMIZE:
                        receiverInfoVO.setContact(receiveHandleContent.getReceiveFillText());
                        break;
                    default:
                        break;
                }
            }
        }
        //过滤特殊符号
        if(receiveHandleContent.isReceiveFilterSwitch() && StringUtils.isNotBlank(receiverInfoVO.getContact()) && CollectionUtils.isNotEmpty(receiveHandleContent.getFilterReceiveTextList())){
                for (String filterStr : receiveHandleContent.getFilterReceiveTextList()) {
                    receiverInfoVO.setContact(receiverInfoVO.getContact().replace(filterStr, ""));
                }
            }

    }
}
