package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.server.tms.mapper.LogisticsBillDetailMapper;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流单明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillDetailServiceImpl extends SuperServiceImpl<LogisticsBillDetailMapper, LogisticsBillDetailEntity> implements LogisticsBillDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillDetailDTO.AddDTO addDTO) {
        LogisticsBillDetailEntity logisticsBillDetailEntity = new LogisticsBillDetailEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillDetailEntity);

        // 数据处理
        handleData(logisticsBillDetailEntity);

        log.info("开始新增物流单明细单");
        boolean save = super.save(logisticsBillDetailEntity);
        if(!save) {
            throw new ServiceException("物流单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流单明细单" , logisticsBillDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsBillDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsBillDetailEntity.getId(), logisticsBillDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDetailDTO.UpdateDTO updateDTO) {
        LogisticsBillDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流单明细单"));
        LogisticsBillDetailEntity logisticsBillDetailEntity =  BeanMapperUtils.map(LogisticsBillDetailEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsBillDetailEntity);
        log.info("编辑 开始修改物流单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillDetailEntity);
        if(!save) {
            throw new ServiceException("物流单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流单明细单日志数据，id：【{}】", logisticsBillDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillDetailEntity.getId(), "物流单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsBillDetailEntity, null, logisticsBillDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillDetailEntity logisticsBillDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
