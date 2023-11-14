package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.server.tms.mapper.LogisticsTrackMapper;
import com.erp.server.tms.service.LogisticsTrackService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流轨迹表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
 */
@Slf4j
@Service
public class LogisticsTrackServiceImpl extends SuperServiceImpl<LogisticsTrackMapper, LogisticsTrackEntity> implements LogisticsTrackService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsTrackDTO.AddDTO addDTO) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        BeanMapperUtils.copy(addDTO, logisticsTrackEntity);

        // 数据处理
        handleData(logisticsTrackEntity);

        log.info("开始新增物流轨迹单");
        boolean save = super.save(logisticsTrackEntity);
        if(!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流轨迹单" , logisticsTrackEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsTrackEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsTrackEntity.getId(), logisticsTrackEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsTrackDTO.UpdateDTO updateDTO) {
        LogisticsTrackEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流轨迹单"));
        LogisticsTrackEntity logisticsTrackEntity =  BeanMapperUtils.map(LogisticsTrackEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsTrackEntity);
        log.info("编辑 开始修改物流轨迹单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsTrackEntity);
        if(!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流轨迹单日志数据，id：【{}】", logisticsTrackEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsTrackEntity.getId(), "物流轨迹单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsTrackEntity, null, logisticsTrackEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsTrackEntity logisticsTrackEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
