package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DictRuleConditionEntity;
import com.erp.server.dmp.mapper.DictRuleConditionMapper;
import com.erp.server.dmp.service.DictRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DictRuleConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 条件字典表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-01-20
 */
@Slf4j
@Service
public class DictRuleConditionServiceImpl extends SuperServiceImpl<DictRuleConditionMapper, DictRuleConditionEntity> implements DictRuleConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictRuleConditionDTO.AddDTO addDTO) {
        DictRuleConditionEntity dictRuleConditionEntity = new DictRuleConditionEntity();
        BeanMapperUtils.copy(addDTO, dictRuleConditionEntity);

        // 数据处理
        handleData(dictRuleConditionEntity);

        log.info("开始新增条件字典单");
        boolean save = super.save(dictRuleConditionEntity);
        if(!save) {
            throw new ServiceException("条件字典单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "条件字典单" , dictRuleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictRuleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dictRuleConditionEntity.getId(), dictRuleConditionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictRuleConditionDTO.UpdateDTO addOrUpdateDTO) {
        DictRuleConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "条件字典单"));
        DictRuleConditionEntity dictRuleConditionEntity =  BeanMapperUtils.map(DictRuleConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dictRuleConditionEntity);
        log.info("编辑 开始修改条件字典单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictRuleConditionEntity);
        if(!save) {
            throw new ServiceException("条件字典单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录条件字典单日志数据，id：【{}】", dictRuleConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictRuleConditionEntity.getId(), "条件字典单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictRuleConditionEntity, null, dictRuleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<DictRuleConditionEntity> listDbByTypes(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictRuleConditionEntity::getType, list).list();
    }

    @Override
    public List<BaseDropDownDTO.CommonDTO> listRuleField() {
        return baseMapper.listRuleField();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictRuleConditionEntity dictRuleConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
