package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.MoldMonitorRefOrderEntity;
import com.erp.server.plm.mapper.MoldMonitorRefOrderMapper;
import com.erp.server.plm.service.MoldMonitorRefOrderService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.MoldMonitorRefOrderDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 模具监控关联单据 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-10-24
 */
@Slf4j
@Service
public class MoldMonitorRefOrderServiceImpl extends SuperServiceImpl<MoldMonitorRefOrderMapper, MoldMonitorRefOrderEntity> implements MoldMonitorRefOrderService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MoldMonitorRefOrderDTO.AddDTO addDTO) {
        MoldMonitorRefOrderEntity moldMonitorRefOrderEntity = new MoldMonitorRefOrderEntity();
        BeanMapperUtils.copy(addDTO, moldMonitorRefOrderEntity);

        // 数据处理
        handleData(moldMonitorRefOrderEntity);

        log.info("开始新增模具监控关联单据");
        boolean save = super.save(moldMonitorRefOrderEntity);
        if(!save) {
            throw new ServiceException("模具监控关联单据保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "模具监控关联单据" , moldMonitorRefOrderEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(moldMonitorRefOrderEntity.getId(), moldMonitorRefOrderEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MoldMonitorRefOrderDTO.UpdateDTO addOrUpdateDTO) {
        MoldMonitorRefOrderEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "模具监控关联单据"));
        MoldMonitorRefOrderEntity moldMonitorRefOrderEntity =  BeanMapperUtils.map(MoldMonitorRefOrderEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(moldMonitorRefOrderEntity);
        log.info("编辑 开始修改模具监控关联单据数据，id：【{}】", old.getId());
        boolean save = super.updateById(moldMonitorRefOrderEntity);
        if(!save) {
            throw new ServiceException("模具监控关联单据保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录模具监控关联单据日志数据，id：【{}】", moldMonitorRefOrderEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), moldMonitorRefOrderEntity.getId(), "模具监控关联单据");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(MoldMonitorRefOrderEntity moldMonitorRefOrderEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
