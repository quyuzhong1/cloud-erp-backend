package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.erp.server.oms.mapper.RuleLogisticsMapper;
import com.erp.server.oms.service.RuleLogisticsService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.RuleLogisticsDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(RuleLogisticsDTO.AddDTO addDTO) {
        RuleLogisticsEntity ruleLogisticsEntity = new RuleLogisticsEntity();
        BeanMapperUtils.copy(addDTO, ruleLogisticsEntity);

        // 数据处理
        handleData(ruleLogisticsEntity);

        log.info("开始新增物流规则单");
        boolean save = super.save(ruleLogisticsEntity);
        if(!save) {
            throw new ServiceException("物流规则单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流规则单" , ruleLogisticsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, ruleLogisticsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return ruleLogisticsEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RuleLogisticsDTO.UpdateDTO updateDTO) {
        RuleLogisticsEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流规则单"));
        RuleLogisticsEntity ruleLogisticsEntity =  BeanMapperUtils.map(RuleLogisticsEntity.class, updateDTO);

        // 数据处理
        handleData(ruleLogisticsEntity);
        log.info("编辑 开始修改物流规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(ruleLogisticsEntity);
        if(!save) {
            throw new ServiceException("物流规则单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流规则单日志数据，id：【{}】", ruleLogisticsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), ruleLogisticsEntity.getId(), "物流规则单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, ruleLogisticsEntity, null, ruleLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RuleLogisticsEntity ruleLogisticsEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
