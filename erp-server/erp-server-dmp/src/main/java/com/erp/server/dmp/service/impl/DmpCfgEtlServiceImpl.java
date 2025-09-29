package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.server.dmp.mapper.DmpCfgEtlMapper;
import com.erp.server.dmp.service.DmpCfgEtlService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * etl配置信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
 */
@Slf4j
@Service
public class DmpCfgEtlServiceImpl extends SuperServiceImpl<DmpCfgEtlMapper, DmpCfgEtlEntity> implements DmpCfgEtlService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgEtlDTO.AddDTO addDTO) {
        DmpCfgEtlEntity dmpCfgEtlEntity = new DmpCfgEtlEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgEtlEntity);

        // 数据处理
        handleData(dmpCfgEtlEntity);

        log.info("开始新增etl配置信息");
        boolean save = super.save(dmpCfgEtlEntity);
        if(!save) {
            throw new ServiceException("etl配置信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "etl配置信息" , dmpCfgEtlEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgEtlEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgEtlEntity.getId(), dmpCfgEtlEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgEtlDTO.UpdateDTO addOrUpdateDTO) {
        DmpCfgEtlEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "etl配置信息"));
        DmpCfgEtlEntity dmpCfgEtlEntity =  BeanMapperUtils.map(DmpCfgEtlEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpCfgEtlEntity);
        log.info("编辑 开始修改etl配置信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgEtlEntity);
        if(!save) {
            throw new ServiceException("etl配置信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录etl配置信息日志数据，id：【{}】", dmpCfgEtlEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgEtlEntity.getId(), "etl配置信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgEtlEntity, null, dmpCfgEtlEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgEtlEntity dmpCfgEtlEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
