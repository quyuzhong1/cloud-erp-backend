package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.DictRuleConditionEntity;
import com.erp.server.oms.mapper.DictRuleConditionMapper;
import com.erp.server.oms.service.DictRuleConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.DictRuleConditionDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 条件字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@Service
public class DictRuleConditionServiceImpl extends SuperServiceImpl<DictRuleConditionMapper, DictRuleConditionEntity> implements DictRuleConditionService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(DictRuleConditionDTO.AddDTO addDTO) {
        DictRuleConditionEntity dictRuleConditionEntity = new DictRuleConditionEntity();
        BeanMapperUtils.copy(addDTO, dictRuleConditionEntity);

        // 数据处理
        handleData(dictRuleConditionEntity);

        log.info("开始新增条件字典单");
        boolean save = super.save(dictRuleConditionEntity);
        if (!save) {
            throw new ServiceException("条件字典单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "条件字典单", dictRuleConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictRuleConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return dictRuleConditionEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictRuleConditionDTO.UpdateDTO updateDTO) {
        DictRuleConditionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "条件字典单"));
        DictRuleConditionEntity dictRuleConditionEntity = BeanMapperUtils.map(DictRuleConditionEntity.class, updateDTO);

        // 数据处理
        handleData(dictRuleConditionEntity);
        log.info("编辑 开始修改条件字典单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictRuleConditionEntity);
        if (!save) {
            throw new ServiceException("条件字典单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录条件字典单日志数据，id：【{}】", dictRuleConditionEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), dictRuleConditionEntity.getId(), "条件字典单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictRuleConditionEntity, null, dictRuleConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 批量保存或者修改
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-30 16:04
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchSaveOrUpdate(List<DictRuleConditionDTO.UpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictRuleConditionEntity> addList = BeanMapper.copyList(list, DictRuleConditionEntity.class);
        return this.saveOrUpdateBatch(addList);
    }


    /**
     * 根据key
     *
     * @param key
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-08-31 11:48
     */
    @Override
    public List<BaseDropDownDTO.CommonDTO> listByType(String key) {
        return baseMapper.listByType(key);
    }

    /**
     * 根据typeList 获取对应数据
     *
     * @param typeList
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-08-31 11:48
     */
    @Override
    public List<DictRuleConditionEntity> listDbByTypes(List<String> typeList) {
        if (CollectionUtils.isEmpty(typeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictRuleConditionEntity::getType, typeList).list();
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
