package com.erp.server.fms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.fms.dto.AssetCardDetailDTO;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.erp.server.fms.mapper.AssetCardDetailMapper;
import com.erp.server.fms.service.AssetCardDetailService;
import com.erp.server.fms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 资产卡片明细表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetCardDetailServiceImpl extends SuperServiceImpl<AssetCardDetailMapper, AssetCardDetailEntity> implements AssetCardDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetCardDetailDTO.AddDTO addDTO) {
        AssetCardDetailEntity assetCardDetailEntity = new AssetCardDetailEntity();
        BeanMapperUtils.copy(addDTO, assetCardDetailEntity);

        // 数据处理
        handleData(assetCardDetailEntity);

        log.info("开始新增资产卡片明细单");
        boolean save = super.save(assetCardDetailEntity);
        if(!save) {
            throw new ServiceException("资产卡片明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产卡片明细单" , assetCardDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, assetCardDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetCardDetailEntity.getId(), assetCardDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetCardDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetCardDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产卡片明细单"));
        AssetCardDetailEntity assetCardDetailEntity =  BeanMapperUtils.map(AssetCardDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetCardDetailEntity);
        log.info("编辑 开始修改资产卡片明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetCardDetailEntity);
        if(!save) {
            throw new ServiceException("资产卡片明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录资产卡片明细单日志数据，id：【{}】", assetCardDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetCardDetailEntity.getId(), "资产卡片明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, assetCardDetailEntity, null, assetCardDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetCardDetailEntity assetCardDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
