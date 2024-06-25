package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.CfgRuleConditionEntity;
import com.erp.model.wms.entity.CfgRuleWaveEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.ExecutionTypeEnum;
import com.erp.model.wms.enums.RuleTypeEnum;
import com.erp.server.wms.mapper.CfgRuleWaveMapper;
import com.erp.server.wms.service.CfgRuleConditionService;
import com.erp.server.wms.service.CfgRuleWaveService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 波次规则 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class CfgRuleWaveServiceImpl extends SuperServiceImpl<CfgRuleWaveMapper, CfgRuleWaveEntity> implements CfgRuleWaveService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleConditionService cfgRuleConditionService;

    @Autowired
    private SoB2cDeliveryService soB2cDeliveryService;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO addDTO) {
        CfgRuleWaveEntity cfgRuleWaveEntity = new CfgRuleWaveEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWaveEntity);

        // 数据处理
        handleData(addDTO.getExecutionTimeList(),cfgRuleWaveEntity);

        log.info("开始新增波次规则");
        boolean save = super.save(cfgRuleWaveEntity);
        if(!save) {
            throw new ServiceException("波次规则保存失败");
        }
        //保存规则条件
        cfgRuleConditionService.saveRuleCondition(cfgRuleWaveEntity.getId(), addDTO.getConditionList(), RuleTypeEnum.CFG_RULE_WAVE.getCode());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "波次规则" , cfgRuleWaveEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgRuleWaveEntity.getId(), cfgRuleWaveEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWaveDTO.UpdateDTO updateDTO) {
        CfgRuleWaveEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "波次规则"));
        CfgRuleWaveEntity cfgRuleWaveEntity =  BeanMapperUtils.map(CfgRuleWaveEntity.class, updateDTO);

        // 数据处理
        handleData(updateDTO.getExecutionTimeList(),cfgRuleWaveEntity);
        log.info("编辑 开始修改波次规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWaveEntity);
        if(!save) {
            throw new ServiceException("波次规则保存失败");
        }
        cfgRuleConditionService.updateRuleCondition(updateDTO.getId(), updateDTO.getConditionList(), ModuleTypeEnum.CFG_RULE_WAVE.getCode(), RuleTypeEnum.PICKING_STRATEGY.getCode());

        // 记录主单操作日志
        log.info("编辑 开始记录波次规则日志数据，id：【{}】", cfgRuleWaveEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWaveEntity.getId(), "波次规则");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWaveEntity, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        CfgRuleWaveDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleWaveDTO.ListDTO> pageData = baseMapper.paging(query, params);
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));

        String disabledName = disabled ? "禁用" : "启用";
        if (disabled.equals(entity.getDisabled())) {
            throw new ServiceException(StrUtil.format("波次规则已【{}】，不支持再次【{}】",disabledName,disabledName));
        }
         lambdaUpdate().eq(CfgRuleWaveEntity::getId,id)
                .set(CfgRuleWaveEntity::getDisabled,disabled)
                .update(new CfgRuleWaveEntity());
        String msg = StrUtil.format("【{}】波次规则【{}】", disabledName,entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), id,StrUtil.format("{}操作",disabledName));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));

        // 删除主单数据
        log.info("删除 开始删除波次规则主单数据，id：【{}】", id);
        super.removeById(id);

        //删除规则
        cfgRuleConditionService.removeByRuleIds(Arrays.asList(id));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public CfgRuleWaveDTO.ViewDTO view(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到波次规则数据"));
        CfgRuleWaveDTO.ViewDTO view = BeanMapperUtils.map(CfgRuleWaveDTO.ViewDTO.class, entity);

        //执行时间
        if (ObjectUtil.isNotEmpty(entity.getExecutionTimeJson())) {
            List<LocalTime> executionTimeList = JSONUtil.parseArray(entity.getExecutionTimeJson()).stream().filter(obj -> ObjectUtil.isNotEmpty(obj))
                    .map(obj -> LocalTime.parse(obj.toString(),  DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList());
            view.setExecutionTimeList(executionTimeList);
        }

        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        return view;
    }

    @Override
    public BatchResultDTO executeRule(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到波次规则数据"));

        //查询所有待处理的发货单进行生成波次
        List<SoB2cDeliveryEntity> soB2cDeliveryList = soB2cDeliveryService.listWaitHandle();


        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.EXECUTE);
    }

    /**
     * 根据名称查询波次规则
     * @author will
     * @date 2024/6/25 10:19
     * @param name
     * @return CfgRuleWaveEntity
     */
    private CfgRuleWaveEntity getByWaveName (String name) {
       return lambdaQuery().eq(CfgRuleWaveEntity::getName,name).last("limit 1").one();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<LocalTime> executionTimeList, CfgRuleWaveEntity cfgRuleWaveEntity) {
        //数量验证
        if (MathUtil.compareTo(cfgRuleWaveEntity.getMinOrderQty(),cfgRuleWaveEntity.getMaxOrderQty()) > MathUtil.ZERO) {
            throw new ServiceException(ApiError.CFG_RULE_WAVE_ORDER_QTY_COMPARE);
        }
        if (MathUtil.compareTo(cfgRuleWaveEntity.getMinQty(),cfgRuleWaveEntity.getMaxQty()) > MathUtil.ZERO) {
            throw new ServiceException(ApiError.CFG_RULE_WAVE_QTY_COMPARE);
        }
        //波次名称重复验证
        CfgRuleWaveEntity ruleWaveEntity = getByWaveName(cfgRuleWaveEntity.getName());
        if (ObjectUtil.isNotEmpty(ruleWaveEntity) && !StrUtil.equals(cfgRuleWaveEntity.getId(),ruleWaveEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }

        //自动执行
        if (StrUtil.equals(cfgRuleWaveEntity.getExecutionType(), ExecutionTypeEnum.AUTO.getCode())) {
            if (ObjectUtil.isEmpty(executionTimeList)) {
                throw new ServiceException("自动执行时执行时间不能为空");
            }
            cfgRuleWaveEntity.setExecutionTimeJson(JSONUtil.parseObj(executionTimeList));
        }
    }

    /**
     *  分页数据处理
     */
    private void fillList (List<CfgRuleWaveDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
    }
}
