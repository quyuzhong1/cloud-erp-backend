package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.SkuMappingRuleMapper;
import com.erp.server.oms.service.SkuMappingRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * sku对照表匹配规则 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
 */
@Slf4j
@Service
public class SkuMappingRuleServiceImpl extends SuperServiceImpl<SkuMappingRuleMapper, SkuMappingRuleEntity> implements SkuMappingRuleService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SkuMappingRuleDTO.AddDTO addDTO) {
        SkuMappingRuleEntity skuMappingRuleEntity = new SkuMappingRuleEntity();
        BeanMapperUtils.copy(addDTO, skuMappingRuleEntity);

        // 数据处理
        handleData(skuMappingRuleEntity);

        log.info("开始新增sku对照表匹配规则");
        boolean save = super.save(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】id为【{}】", commonService.getUserInfo().getUserName(), "sku对照表匹配规则" , skuMappingRuleEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(skuMappingRuleEntity.getId(), skuMappingRuleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SkuMappingRuleDTO.UpdateDTO updateDTO) {
        SkuMappingRuleEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku对照表匹配规则"));
        SkuMappingRuleEntity skuMappingRuleEntity =  BeanMapperUtils.map(SkuMappingRuleEntity.class, updateDTO);

        // 数据处理
        handleData(skuMappingRuleEntity);
        log.info("编辑 开始修改sku对照表匹配规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }

        // 记录主单操作日志
        log.info("编辑 开始记录sku对照表匹配规则日志数据，id：【{}】", skuMappingRuleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), skuMappingRuleEntity.getId(), "sku对照表匹配规则");
        operateLogService.addModuleOperateLogByObj(old, skuMappingRuleEntity, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SkuMappingRuleEntity> listOrderByPriority() {
        return lambdaQuery().orderByAsc(SkuMappingRuleEntity::getPriority).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SkuMappingRuleEntity skuMappingRuleEntity) {
    }
}
