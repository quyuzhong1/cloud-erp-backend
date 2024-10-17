package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpCfgOutputBlackDTO;
import com.erp.model.dmp.entity.DmpCfgOutputBlackEntity;
import com.erp.server.dmp.mapper.DmpCfgOutputBlackMapper;
import com.erp.server.dmp.service.DmpCfgOutputBlackService;
import com.erp.server.dmp.service.OperateLogService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 输出黑名单 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-03
 */
@Slf4j
@Service
public class DmpCfgOutputBlackServiceImpl extends SuperServiceImpl<DmpCfgOutputBlackMapper, DmpCfgOutputBlackEntity> implements DmpCfgOutputBlackService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputBlackDTO.AddDTO addDTO) {
        DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity = new DmpCfgOutputBlackEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputBlackEntity);

        // 数据处理
        handleData(dmpCfgOutputBlackEntity);

        dmpCfgOutputBlackEntity.setIsWebAdd(true);
        log.info("开始新增输出黑名单");
        boolean save = super.save(dmpCfgOutputBlackEntity);
        if(!save) {
            throw new ServiceException("输出黑名单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输出黑名单" , dmpCfgOutputBlackEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgOutputBlackEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputBlackEntity.getId(), dmpCfgOutputBlackEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputBlackDTO.UpdateDTO updateDTO) {
        DmpCfgOutputBlackEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输出黑名单"));
        DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity =  BeanMapperUtils.map(DmpCfgOutputBlackEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputBlackEntity);
        log.info("编辑 开始修改输出黑名单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputBlackEntity);
        if(!save) {
            throw new ServiceException("输出黑名单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输出黑名单日志数据，id：【{}】", dmpCfgOutputBlackEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputBlackEntity.getId(), "输出黑名单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgOutputBlackEntity, null, dmpCfgOutputBlackEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputBlackEntity dmpCfgOutputBlackEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
