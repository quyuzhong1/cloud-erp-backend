package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.CfgVirtualTransRulesEntity;
import com.erp.server.wms.mapper.CfgVirtualTransRulesMapper;
import com.erp.server.wms.service.CfgVirtualTransRulesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgVirtualTransRulesDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 虚拟库存交易规则表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class CfgVirtualTransRulesServiceImpl extends SuperServiceImpl<CfgVirtualTransRulesMapper, CfgVirtualTransRulesEntity> implements CfgVirtualTransRulesService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgVirtualTransRulesDTO.AddDTO addDTO) {
        CfgVirtualTransRulesEntity cfgVirtualTransRulesEntity = new CfgVirtualTransRulesEntity();
        BeanMapperUtils.copy(addDTO, cfgVirtualTransRulesEntity);

        // 数据处理
        handleData(cfgVirtualTransRulesEntity);

        log.info("开始新增虚拟库存交易规则单");
        boolean save = super.save(cfgVirtualTransRulesEntity);
        if(!save) {
            throw new ServiceException("虚拟库存交易规则单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟库存交易规则单" , cfgVirtualTransRulesEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgVirtualTransRulesEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgVirtualTransRulesEntity.getId(), cfgVirtualTransRulesEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgVirtualTransRulesDTO.UpdateDTO updateDTO) {
        CfgVirtualTransRulesEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟库存交易规则单"));
        CfgVirtualTransRulesEntity cfgVirtualTransRulesEntity =  BeanMapperUtils.map(CfgVirtualTransRulesEntity.class, updateDTO);

        // 数据处理
        handleData(cfgVirtualTransRulesEntity);
        log.info("编辑 开始修改虚拟库存交易规则单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgVirtualTransRulesEntity);
        if(!save) {
            throw new ServiceException("虚拟库存交易规则单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录虚拟库存交易规则单日志数据，id：【{}】", cfgVirtualTransRulesEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgVirtualTransRulesEntity.getId(), "虚拟库存交易规则单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgVirtualTransRulesEntity, null, cfgVirtualTransRulesEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgVirtualTransRulesEntity cfgVirtualTransRulesEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
