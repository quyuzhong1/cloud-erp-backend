package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.server.tms.mapper.ShippingTemplateRuleMapper;
import com.erp.server.tms.service.ShippingTemplateRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateRuleDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 运费模板渠道关联表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateRuleServiceImpl extends SuperServiceImpl<ShippingTemplateRuleMapper, ShippingTemplateRuleEntity> implements ShippingTemplateRuleService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateRuleDTO.AddDTO addDTO) {
        ShippingTemplateRuleEntity shippingTemplateRuleEntity = new ShippingTemplateRuleEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateRuleEntity);

        // 数据处理
        handleData(shippingTemplateRuleEntity);

        log.info("开始新增运费模板渠道关联单");
        boolean save = super.save(shippingTemplateRuleEntity);
        if(!save) {
            throw new ServiceException("运费模板渠道关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "运费模板渠道关联单" , shippingTemplateRuleEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shippingTemplateRuleEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shippingTemplateRuleEntity.getId(), shippingTemplateRuleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateRuleDTO.UpdateDTO updateDTO) {
        ShippingTemplateRuleEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板渠道关联单"));
        ShippingTemplateRuleEntity shippingTemplateRuleEntity =  BeanMapperUtils.map(ShippingTemplateRuleEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateRuleEntity);
        log.info("编辑 开始修改运费模板渠道关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateRuleEntity);
        if(!save) {
            throw new ServiceException("运费模板渠道关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录运费模板渠道关联单日志数据，id：【{}】", shippingTemplateRuleEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateRuleEntity.getId(), "运费模板渠道关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shippingTemplateRuleEntity, null, shippingTemplateRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateRuleEntity shippingTemplateRuleEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
