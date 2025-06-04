package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgQueryOptionExtEntity;
import com.erp.server.workflow.mapper.CfgQueryOptionExtMapper;
import com.erp.server.workflow.service.CfgQueryOptionExtService;
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
import com.erp.model.workflow.dto.CfgQueryOptionExtDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * cfg_query_option拓展表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@Service
public class CfgQueryOptionExtServiceImpl extends SuperServiceImpl<CfgQueryOptionExtMapper, CfgQueryOptionExtEntity> implements CfgQueryOptionExtService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgQueryOptionExtDTO.AddDTO addDTO) {
        CfgQueryOptionExtEntity cfgQueryOptionExtEntity = new CfgQueryOptionExtEntity();
        BeanMapperUtils.copy(addDTO, cfgQueryOptionExtEntity);

        // 数据处理
        handleData(cfgQueryOptionExtEntity);

        log.info("开始新增cfg_query_option拓展单");
        boolean save = super.save(cfgQueryOptionExtEntity);
        if(!save) {
            throw new ServiceException("cfg_query_option拓展单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "cfg_query_option拓展单" , cfgQueryOptionExtEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgQueryOptionExtEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgQueryOptionExtEntity.getId(), cfgQueryOptionExtEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQueryOptionExtDTO.UpdateDTO addOrUpdateDTO) {
        CfgQueryOptionExtEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "cfg_query_option拓展单"));
        CfgQueryOptionExtEntity cfgQueryOptionExtEntity =  BeanMapperUtils.map(CfgQueryOptionExtEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgQueryOptionExtEntity);
        log.info("编辑 开始修改cfg_query_option拓展单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgQueryOptionExtEntity);
        if(!save) {
            throw new ServiceException("cfg_query_option拓展单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录cfg_query_option拓展单日志数据，id：【{}】", cfgQueryOptionExtEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgQueryOptionExtEntity.getId(), "cfg_query_option拓展单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgQueryOptionExtEntity, null, cfgQueryOptionExtEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgQueryOptionExtEntity cfgQueryOptionExtEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
