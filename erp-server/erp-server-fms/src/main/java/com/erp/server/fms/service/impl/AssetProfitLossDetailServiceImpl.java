package com.erp.server.fms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.fms.dto.AssetProfitLossDetailDTO;
import com.erp.model.fms.entity.AssetProfitLossDetailEntity;
import com.erp.server.fms.mapper.AssetProfitLossDetailMapper;
import com.erp.server.fms.service.AssetProfitLossDetailService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 盘盈盘亏单明细表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetProfitLossDetailServiceImpl extends SuperServiceImpl<AssetProfitLossDetailMapper, AssetProfitLossDetailEntity> implements AssetProfitLossDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetProfitLossDetailDTO.AddDTO addDTO) {
        AssetProfitLossDetailEntity assetProfitLossDetailEntity = new AssetProfitLossDetailEntity();
        BeanMapperUtils.copy(addDTO, assetProfitLossDetailEntity);

        // 数据处理
        handleData(assetProfitLossDetailEntity);

        log.info("开始新增盘盈盘亏单明细单");
        boolean save = super.save(assetProfitLossDetailEntity);
        if(!save) {
            throw new ServiceException("盘盈盘亏单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "盘盈盘亏单明细单" , assetProfitLossDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, assetProfitLossDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetProfitLossDetailEntity.getId(), assetProfitLossDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetProfitLossDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetProfitLossDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "盘盈盘亏单明细单"));
        AssetProfitLossDetailEntity assetProfitLossDetailEntity =  BeanMapperUtils.map(AssetProfitLossDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetProfitLossDetailEntity);
        log.info("编辑 开始修改盘盈盘亏单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetProfitLossDetailEntity);
        if(!save) {
            throw new ServiceException("盘盈盘亏单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录盘盈盘亏单明细单日志数据，id：【{}】", assetProfitLossDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetProfitLossDetailEntity.getId(), "盘盈盘亏单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, assetProfitLossDetailEntity, null, assetProfitLossDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetProfitLossDetailEntity assetProfitLossDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
