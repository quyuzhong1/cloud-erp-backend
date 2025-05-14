package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.DictCfgSysFieldEntity;
import com.erp.server.workflow.mapper.DictCfgSysFieldMapper;
import com.erp.server.workflow.service.DictCfgSysFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
//import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.DictCfgSysFieldDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 数大臣单据字段 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class DictCfgSysFieldServiceImpl extends SuperServiceImpl<DictCfgSysFieldMapper, DictCfgSysFieldEntity> implements DictCfgSysFieldService {
    @Autowired
//    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictCfgSysFieldDTO.AddDTO addDTO) {
        DictCfgSysFieldEntity dictCfgSysFieldEntity = new DictCfgSysFieldEntity();
        BeanMapperUtils.copy(addDTO, dictCfgSysFieldEntity);

        // 数据处理
        handleData(dictCfgSysFieldEntity);

        log.info("开始新增数大臣单据字段");
        boolean save = super.save(dictCfgSysFieldEntity);
        if(!save) {
            throw new ServiceException("数大臣单据字段保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "数大臣单据字段" , dictCfgSysFieldEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, dictCfgSysFieldEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dictCfgSysFieldEntity.getId(), dictCfgSysFieldEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictCfgSysFieldDTO.UpdateDTO addOrUpdateDTO) {
        DictCfgSysFieldEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "数大臣单据字段"));
        DictCfgSysFieldEntity dictCfgSysFieldEntity =  BeanMapperUtils.map(DictCfgSysFieldEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dictCfgSysFieldEntity);
        log.info("编辑 开始修改数大臣单据字段数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictCfgSysFieldEntity);
        if(!save) {
            throw new ServiceException("数大臣单据字段保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录数大臣单据字段日志数据，id：【{}】", dictCfgSysFieldEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictCfgSysFieldEntity.getId(), "数大臣单据字段");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, dictCfgSysFieldEntity, null, dictCfgSysFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictCfgSysFieldEntity dictCfgSysFieldEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
