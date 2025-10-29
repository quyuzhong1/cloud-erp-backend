package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.server.scm.mapper.AssetPurchaseChangeDetailMapper;
import com.erp.server.scm.service.AssetPurchaseChangeDetailService;
import com.erp.server.scm.service.ModuleOperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.AssetPurchaseChangeDetailEntity;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.AssetPurchaseChangeDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@Service
public class AssetPurchaseChangeDetailServiceImpl extends SuperServiceImpl<AssetPurchaseChangeDetailMapper, AssetPurchaseChangeDetailEntity> implements AssetPurchaseChangeDetailService {

    @Autowired
    private ModuleOperateLogService moduleOperateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetPurchaseChangeDetailDTO.AddDTO addDTO) {
        AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity = new AssetPurchaseChangeDetailEntity();
        BeanMapperUtils.copy(addDTO, assetPurchaseChangeDetailEntity);

        // 数据处理
        handleData(assetPurchaseChangeDetailEntity);

        log.info("开始新增");
        boolean save = super.save(assetPurchaseChangeDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , assetPurchaseChangeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        moduleOperateLogService.addModuleOperateLog(msg, null, assetPurchaseChangeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetPurchaseChangeDetailEntity.getId(), assetPurchaseChangeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetPurchaseChangeDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetPurchaseChangeDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity =  BeanMapperUtils.map(AssetPurchaseChangeDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetPurchaseChangeDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetPurchaseChangeDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", assetPurchaseChangeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetPurchaseChangeDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        moduleOperateLogService.addModuleOperateLogByObj(old, assetPurchaseChangeDetailEntity, null, assetPurchaseChangeDetailEntity.getId(),"", msg);
        return Boolean.TRUE;
    }

    @Override
    public void update(List<AssetPurchaseChangeDetailEntity> assetPurchaseChangeDetailEntity) {

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(AssetPurchaseChangeDetailEntity assetPurchaseChangeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
