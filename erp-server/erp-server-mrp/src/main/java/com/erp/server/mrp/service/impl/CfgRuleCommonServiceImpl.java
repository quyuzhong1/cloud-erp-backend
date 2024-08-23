package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;
import com.erp.server.mrp.mapper.CfgRuleCommonMapper;
import com.erp.server.mrp.service.CfgRuleCommonService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 公共配置（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleCommonServiceImpl extends SuperServiceImpl<CfgRuleCommonMapper, CfgRuleCommonEntity> implements CfgRuleCommonService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleCommonDTO.AddDTO addDTO) {
        CfgRuleCommonEntity cfgRuleCommonEntity = new CfgRuleCommonEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleCommonEntity);

        // 数据处理
        handleData(cfgRuleCommonEntity);

        log.info("开始新增公共配置（规则设置）");
        boolean save = super.save(cfgRuleCommonEntity);
        if(!save) {
            throw new ServiceException("公共配置（规则设置）保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "公共配置（规则设置）" , cfgRuleCommonEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleCommonEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleCommonEntity.getId(), cfgRuleCommonEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleCommonDTO.UpdateDTO updateDTO) {
        CfgRuleCommonEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "公共配置（规则设置）"));
        CfgRuleCommonEntity cfgRuleCommonEntity =  BeanMapperUtils.map(CfgRuleCommonEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleCommonEntity);
        log.info("编辑 开始修改公共配置（规则设置）数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleCommonEntity);
        if(!save) {
            throw new ServiceException("公共配置（规则设置）保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录公共配置（规则设置）日志数据，id：【{}】", cfgRuleCommonEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleCommonEntity.getId(), "公共配置（规则设置）");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleCommonEntity, null, cfgRuleCommonEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleCommonEntity cfgRuleCommonEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
