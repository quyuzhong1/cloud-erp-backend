package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.RuleConditionDTO;
import com.erp.model.oms.dto.RuleLogisticsDTO;
import com.erp.model.oms.entity.RuleConditionEntity;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelWarehouseEntity;
import com.erp.model.tms.enums.LogisticsChannelWarehouseTypeEnum;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.mapper.RuleLogisticsMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.RuleConditionService;
import com.erp.server.oms.service.RuleLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流规则表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class RuleLogisticsServiceImpl extends SuperServiceImpl<RuleLogisticsMapper, RuleLogisticsEntity> implements RuleLogisticsService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private RuleConditionService ruleConditionService;

    @Resource
    private SpElServer spElServer;

    @Resource
    private LogisticsFeign logisticsFeign;
    @Lazy
    @Resource
    private SoB2cService soB2cService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleLogisticsDTO.AddDTO addDTO) {
        List<RuleConditionDTO.AddDTO> conditionList = addDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        RuleLogisticsEntity ruleLogisticsEntity = new RuleLogisticsEntity();
        BeanMapperUtils.copy(addDTO, ruleLogisticsEntity);
        handleData(ruleLogisticsEntity);
        if(Boolean.TRUE.equals(ruleLogisticsEntity.getAutoGetTrackNo()) && Boolean.TRUE.equals(ruleLogisticsEntity.getAutoGetTrackNotOfRangeDelivery())){
            throw new ServiceException(ApiError.ERROR_92163);
        }
        boolean save = super.save(ruleLogisticsEntity);
        if (!save) {
            throw new ServiceException("物流规则单保存失败");
        }

        String id = ruleLogisticsEntity.getId();
        //保存规则条件
        ruleConditionService.saveRuleCondition(id, conditionList);
        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流规则单", id);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.RULE_LOGISTICS.getCode(), id, "新增操作");
        return ruleLogisticsEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleLogisticsDTO.UpdateDTO updateDTO) {
        String id = updateDTO.getId();
        RuleLogisticsEntity old = super.getById(id);
        isExist(old);

        List<RuleConditionDTO.UpdateDTO> conditionList = updateDTO.getConditionList();
        List<ConditionElement> conditionElementList = conditionList.stream().
                map(c -> new ConditionElement(c.getLeftBracket(), c.getField(),
                        c.getCompare(), c.getValue(),
                        c.getRightBracket(), c.getLogic(), "")).collect(Collectors.toList());
        SpElExpressionDTO sqElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = sqElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (Boolean.FALSE.equals(checkResult)) {
            throw new ServiceException(ApiError.ERROR_RULE_EXPRESSION_ERROR);
        }
        RuleLogisticsEntity ruleLogisticsEntity = BeanMapperUtils.map(RuleLogisticsEntity.class, updateDTO);
        // 数据处理
        handleData(ruleLogisticsEntity);
        if(Boolean.TRUE.equals(ruleLogisticsEntity.getAutoGetTrackNo()) && Boolean.TRUE.equals(ruleLogisticsEntity.getAutoGetTrackNotOfRangeDelivery())){
            throw new ServiceException(ApiError.ERROR_92163);
        }
        boolean save = super.updateById(ruleLogisticsEntity);
        if (!save) {
            throw new ServiceException("物流规则单保存失败");
        }
        ruleConditionService.updateRuleCondition(id, conditionList);
        // 记录主单操作日志
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), ruleLogisticsEntity.getId(), "物流规则单");
        operateLogService.addModuleOperateLogByObj(old, ruleLogisticsEntity, ModuleTypeEnum.RULE_LOGISTICS.getCode(), ruleLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }

    private static void isExist(RuleLogisticsEntity old) {
        if(null == old){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流规则单");
        }
    }


    /**
     * 物流规则分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.RuleLogisticsDTO.PagingViewDTO>
     * @author yl
     * @date 2023-09-01 9:07
     */
    @Override
    public PagingVO<RuleLogisticsDTO.PagingViewDTO> paging(PagingDTO<RuleLogisticsDTO.PagingParamDTO> dto) {
        RuleLogisticsDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<RuleLogisticsDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        return new PagingVO<>(pageData);

    }


    /**
     * 物流规则详情
     *
     * @param id
     * @return
     */
    @Override
    public RuleLogisticsDTO.ViewDTO view(String id) {
        RuleLogisticsEntity ruleLogistics = this.getById(id);
        isExist(ruleLogistics);
        RuleLogisticsDTO.ViewDTO view = new RuleLogisticsDTO.ViewDTO();
        BeanMapper.copy(ruleLogistics, view);
        String type = DictBasicTypeEnum.FIELD.getType();
        List<RuleConditionDTO.ViewDTO> conditionList = ruleConditionService.listByRuleId(id, type);
        view.setConditionList(conditionList);
        String modeType = view.getModeType();
        String modeTypeName = RuleTypeEnum.getName(modeType);
        view.setModeTypeName(modeTypeName);
        return view;
    }

    /**
     * 更改启用禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 14:15
     */
    @Override
    public Boolean updateStatus(UpdateStateDTO dto) {
        RuleLogisticsEntity ruleLogistics = this.getById(dto.getId());
        isExist(ruleLogistics);
        Boolean disabled = ruleLogistics.getDisabled();
        if (disabled.equals(dto.getState())) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        String content = String.format("启用状态[%s]变更为[%s]", Boolean.TRUE.equals(disabled) ? "停用" : "启用", Boolean.TRUE.equals(disabled) ? "启用" : "停用");
        ruleLogistics.setDisabled(dto.getState());
        operateLogService.addModuleOperateLog(content, ModuleTypeEnum.RULE_ORDER_APPROVAL.getCode(), dto.getId(), "状态变更");
        return this.updateById(ruleLogistics);

    }


    /**
     * 获取到物流匹配结果
     *
     * @param map
     * @return
     */
    @Override
    public RuleLogisticsDTO.RuleMatchResultDTO getRuleOrderMatchResult(Map<String, Object> map) {
        if (Objects.isNull(map)) {
            return null;
        }
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get("detailList");
        //要匹配渠道id 是空的 如果有就 不用匹配了返回成功
        String logisticsChannelIdKey="logisticsChannelId";
        mapList = mapList.stream().filter(m -> m.get(logisticsChannelIdKey)==null ||
                StringUtils.isBlank(m.getOrDefault(logisticsChannelIdKey,"").toString())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(mapList)){
            return new RuleLogisticsDTO.RuleMatchResultDTO();
        }
        map.put("detailList",mapList);
        List<RuleLogisticsEntity> ruleLogisticsList = this.listOrderByPriority();
        List<String> ruleIdList = ruleLogisticsList.stream().map(RuleLogisticsEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = ruleConditionService.listDbRuleIds(ruleIdList);
        for (RuleLogisticsEntity item : ruleLogisticsList) {
            String ruleId = item.getId();
            List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                    filter(r -> r.getRuleId().equals(ruleId)).
                    sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());

            List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
            //获取到表达式,判断表达式是否匹配
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map,"");
            if (Boolean.TRUE.equals(matchResult)) {
                //验证渠道下是否设置了仓库
                List<LogisticsChannelWarehouseEntity> list = FeignQuery.create(LogisticsChannelWarehouseEntity.class)
                        .eq(LogisticsChannelWarehouseEntity::getLogisticsChannelId, item.getLogisticsChannelId())
                        .list();
                if (CollectionUtils.isEmpty(list)) {
                    throw new ServiceException( CharSequenceUtil.format("渠道【{}】未设置仓库，请先设置仓库",item.getLogisticsChannelName()));
                }
                //校验渠道限制
                try {
                    soB2cService.checkLogisticsChannelBlacklist(map.get("id").toString(),item.getLogisticsChannelId(),"");
                }catch (Exception e){
                    log.error("渠道限制校验失败:{}",e.getMessage());
                    continue;
                }
                //全部指定直接过，部分指定校验仓库是否一致
                if (CharSequenceUtil.equals(list.get(0).getType(),LogisticsChannelWarehouseTypeEnum.ENUM_PART.getCode())) {
                    List<String> warehouseIdList = mapList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj.get("deliveryWarehouseId")) && CharSequenceUtil.isNotBlank(obj.get("deliveryWarehouseId").toString())).map(obj -> obj.get("deliveryWarehouseId").toString()).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(warehouseIdList)) {
                        throw new ServiceException("B2C销售订单仓库不能为空");
                    }
                    List<String> channelWarehouseIdList = list.stream().map(LogisticsChannelWarehouseEntity::getWarehouseId).collect(Collectors.toList());
                   Boolean isMatch =  Boolean.TRUE;
                    for (String warehouseId : warehouseIdList) {
                       if (!channelWarehouseIdList.contains(warehouseId)) {
                           isMatch = Boolean.FALSE;
                           break;
                       }
                   }
                    //如果仓库没匹配上则进行下一条规则的匹配
                   if (Boolean.FALSE.equals(isMatch)) {
                       continue;
                   }
                }
                RuleLogisticsDTO.RuleMatchResultDTO ruleMatchResult = new RuleLogisticsDTO.RuleMatchResultDTO();
                ruleMatchResult.setLogisticsSupplierId(item.getLogisticsSupplierId());
                ruleMatchResult.setAutoGetTrackNo(item.getAutoGetTrackNo());
                ruleMatchResult.setAutoGetTrackNotOfRangeDelivery(item.getAutoGetTrackNotOfRangeDelivery());
                ruleMatchResult.setLogisticsChannelId(item.getLogisticsChannelId());
                ruleMatchResult.setLogisticsChannelName(item.getLogisticsChannelName());
                ruleMatchResult.setName(item.getName());
                return ruleMatchResult;
            }

        }
        return null;
    }

    @Override
    public List<RuleLogisticsEntity> getChannelListByAutoSubmitDelivery() {
        return baseMapper.getChannelListByAutoSubmitDelivery();
    }

    /**
     * 根据优先级 获取到对应物流的信息
     *
     * @return
     */
    private List<RuleLogisticsEntity> listOrderByPriority() {
        return this.lambdaQuery().eq(RuleLogisticsEntity::getDisabled, Boolean.FALSE).
                orderByAsc(RuleLogisticsEntity::getPriority).
                orderByDesc(RuleLogisticsEntity::getUpdateTime).
                list();

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(RuleLogisticsEntity ruleLogisticsEntity) {
        String logisticsChannelId = ruleLogisticsEntity.getLogisticsChannelId();
        if (StringUtils.isNotBlank(logisticsChannelId)) {
            LogisticsChannelDTO.BaseDTO baseDTO = logisticsFeign.getChannelInfoById(logisticsChannelId);
            ruleLogisticsEntity.setLogisticsChannelName(baseDTO.getName());
            ruleLogisticsEntity.setLogisticsSupplierName(baseDTO.getLogisticsSupplierName());
        }

    }


}
