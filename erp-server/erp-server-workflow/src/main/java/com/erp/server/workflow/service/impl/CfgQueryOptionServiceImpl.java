package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.server.workflow.mapper.CfgQueryOptionMapper;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgQueryOptionDTO.AddDTO addDTO) {
        CfgQueryOptionEntity cfgQueryOptionEntity = new CfgQueryOptionEntity();
        BeanMapperUtils.copy(addDTO, cfgQueryOptionEntity);

        // 数据处理
        handleData(cfgQueryOptionEntity);

        log.info("开始新增查询option配置表(数大臣单据字段)");
        boolean save = super.save(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("查询option配置表(数大臣单据字段)保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "查询option配置表(数大臣单据字段)" , cfgQueryOptionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgQueryOptionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgQueryOptionEntity.getId(), cfgQueryOptionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQueryOptionDTO.UpdateDTO addOrUpdateDTO) {
        CfgQueryOptionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "查询option配置表(数大臣单据字段)"));
        CfgQueryOptionEntity cfgQueryOptionEntity =  BeanMapperUtils.map(CfgQueryOptionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgQueryOptionEntity);
        log.info("编辑 开始修改查询option配置表(数大臣单据字段)数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("查询option配置表(数大臣单据字段)保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录查询option配置表(数大臣单据字段)日志数据，id：【{}】", cfgQueryOptionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgQueryOptionEntity.getId(), "查询option配置表(数大臣单据字段)");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgQueryOptionEntity, null, cfgQueryOptionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgQueryOptionEntity cfgQueryOptionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
