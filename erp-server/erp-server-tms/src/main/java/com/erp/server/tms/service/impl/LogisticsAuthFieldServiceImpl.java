package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.LogisticsAuthFieldMapper;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsAuthFieldDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流授权字段值表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsAuthFieldServiceImpl extends SuperServiceImpl<LogisticsAuthFieldMapper, LogisticsAuthFieldEntity> implements LogisticsAuthFieldService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsAuthFieldDTO.AddDTO addDTO) {
        LogisticsAuthFieldEntity logisticsAuthFieldEntity = new LogisticsAuthFieldEntity();
        BeanMapperUtils.copy(addDTO, logisticsAuthFieldEntity);

        // 数据处理
        handleData(logisticsAuthFieldEntity);

        log.info("开始新增物流授权字段值单");
        boolean save = super.save(logisticsAuthFieldEntity);
        if(!save) {
            throw new ServiceException("物流授权字段值单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流授权字段值单" , logisticsAuthFieldEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsAuthFieldEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsAuthFieldEntity.getId(), logisticsAuthFieldEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsAuthFieldDTO.UpdateDTO updateDTO) {
        LogisticsAuthFieldEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流授权字段值单"));
        LogisticsAuthFieldEntity logisticsAuthFieldEntity =  BeanMapperUtils.map(LogisticsAuthFieldEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsAuthFieldEntity);
        log.info("编辑 开始修改物流授权字段值单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsAuthFieldEntity);
        if(!save) {
            throw new ServiceException("物流授权字段值单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流授权字段值单日志数据，id：【{}】", logisticsAuthFieldEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsAuthFieldEntity.getId(), "物流授权字段值单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsAuthFieldEntity, null, logisticsAuthFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsAuthFieldEntity logisticsAuthFieldEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
