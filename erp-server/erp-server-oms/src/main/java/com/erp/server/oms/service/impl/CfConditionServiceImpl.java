package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.CfConditionEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.mapper.CfConditionMapper;
import com.erp.server.oms.service.CfConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.DictRuleConditionService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.CfConditionDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 条件配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@Service
public class CfConditionServiceImpl extends SuperServiceImpl<CfConditionMapper, CfConditionEntity> implements CfConditionService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Autowired
    private DictRuleConditionService dictRuleConditionService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(CfConditionDTO.AddDTO addDTO) {
        CfConditionEntity cfConditionEntity = new CfConditionEntity();
        BeanMapperUtils.copy(addDTO, cfConditionEntity);

        // 数据处理
        handleData(cfConditionEntity);

        log.info("开始新增条件配置单");
        boolean save = super.save(cfConditionEntity);
        if (!save) {
            throw new ServiceException("条件配置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "条件配置单", cfConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return cfConditionEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfConditionDTO.UpdateDTO updateDTO) {
        CfConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "条件配置单"));
        CfConditionEntity cfConditionEntity = BeanMapperUtils.map(CfConditionEntity.class, updateDTO);

        // 数据处理
        handleData(cfConditionEntity);
        log.info("编辑 开始修改条件配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfConditionEntity);
        if (!save) {
            throw new ServiceException("条件配置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录条件配置单日志数据，id：【{}】", cfConditionEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfConditionEntity.getId(), "条件配置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfConditionEntity, null, cfConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 根据添加code 获取到逻辑关系
     *
     * @param conditionCode
     * @return
     */
    @Override
    public List<CfConditionDTO.CommonDTO> listByConditionCode(String conditionCode) {
        List<CfConditionDTO.CommonDTO> list = baseMapper.listByConditionCode(conditionCode);
        String type = DictBasicTypeEnum.COMPARE.getType();
        List<BaseDropDownDTO.CommonDTO> dictRuleConditionList = dictRuleConditionService.listByType(type);
        for (CfConditionDTO.CommonDTO item : list) {
            String logic = item.getLogic();
            String logicName = dictRuleConditionList.stream().filter(d -> d.getCode().equals(logic)).
                    findFirst().map(BaseDropDownDTO.CommonDTO::getValue).orElse("");
            item.setLogicName(logicName);
        }
        return list;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfConditionEntity cfConditionEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
