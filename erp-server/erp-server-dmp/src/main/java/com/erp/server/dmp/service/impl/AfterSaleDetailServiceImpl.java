package com.erp.server.dmp.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.AfterSaleDetailDTO;
import com.erp.model.dmp.entity.AfterSaleDetailEntity;
import com.erp.server.dmp.mapper.AfterSaleDetailMapper;
import com.erp.server.dmp.service.AfterSaleDetailService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 售后申请明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-04-06
 */
@Slf4j
@Service
public class AfterSaleDetailServiceImpl extends SuperServiceImpl<AfterSaleDetailMapper, AfterSaleDetailEntity> implements AfterSaleDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSaleDetailDTO.AddDTO addDTO) {
        AfterSaleDetailEntity afterSaleDetailEntity = new AfterSaleDetailEntity();
        BeanMapperUtils.copy(addDTO, afterSaleDetailEntity);

        // 数据处理
        handleData(afterSaleDetailEntity);

        log.info("开始新增售后申请明细单");
        boolean save = super.save(afterSaleDetailEntity);
        if(!save) {
            throw new ServiceException("售后申请明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后申请明细单" , afterSaleDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, afterSaleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(afterSaleDetailEntity.getId(), afterSaleDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSaleDetailDTO.UpdateDTO addOrUpdateDTO) {
        AfterSaleDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "售后申请明细单"));
        AfterSaleDetailEntity afterSaleDetailEntity =  BeanMapperUtils.map(AfterSaleDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(afterSaleDetailEntity);
        log.info("编辑 开始修改售后申请明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(afterSaleDetailEntity);
        if(!save) {
            throw new ServiceException("售后申请明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录售后申请明细单日志数据，id：【{}】", afterSaleDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSaleDetailEntity.getId(), "售后申请明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, afterSaleDetailEntity, null, afterSaleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<AfterSaleDetailEntity> listByMainIds(List<String> ids) {
        if(CollUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(AfterSaleDetailEntity::getMainId, ids).list();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AfterSaleDetailEntity afterSaleDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
