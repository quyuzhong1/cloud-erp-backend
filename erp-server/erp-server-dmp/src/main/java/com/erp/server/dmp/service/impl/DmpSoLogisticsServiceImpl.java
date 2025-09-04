package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSoLogisticsEntity;
import com.erp.server.dmp.mapper.DmpSoLogisticsMapper;
import com.erp.server.dmp.service.DmpSoLogisticsService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSoLogisticsDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中台物流单主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-04-07
 */
@Slf4j
@Service
public class DmpSoLogisticsServiceImpl extends SuperServiceImpl<DmpSoLogisticsMapper, DmpSoLogisticsEntity> implements DmpSoLogisticsService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoLogisticsDTO.AddDTO addDTO) {
        DmpSoLogisticsEntity dmpSoLogisticsEntity = new DmpSoLogisticsEntity();
        BeanMapperUtils.copy(addDTO, dmpSoLogisticsEntity);

        // 数据处理
        handleData(dmpSoLogisticsEntity);

        log.info("开始新增中台物流单主单");
        boolean save = super.save(dmpSoLogisticsEntity);
        if(!save) {
            throw new ServiceException("中台物流单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台物流单主单" , dmpSoLogisticsEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoLogisticsEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoLogisticsEntity.getId(), dmpSoLogisticsEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoLogisticsDTO.UpdateDTO addOrUpdateDTO) {
        DmpSoLogisticsEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台物流单主单"));
        DmpSoLogisticsEntity dmpSoLogisticsEntity =  BeanMapperUtils.map(DmpSoLogisticsEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpSoLogisticsEntity);
        log.info("编辑 开始修改中台物流单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoLogisticsEntity);
        if(!save) {
            throw new ServiceException("中台物流单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台物流单主单日志数据，id：【{}】", dmpSoLogisticsEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoLogisticsEntity.getId(), "中台物流单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoLogisticsEntity, null, dmpSoLogisticsEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoLogisticsEntity dmpSoLogisticsEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
