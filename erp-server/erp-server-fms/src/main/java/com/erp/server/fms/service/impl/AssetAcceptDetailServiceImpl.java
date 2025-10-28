package com.erp.server.fms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.erp.server.fms.mapper.AssetAcceptDetailMapper;
import com.erp.server.fms.service.AssetAcceptDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.fms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.fms.dto.AssetAcceptDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 资产验收表明细表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetAcceptDetailServiceImpl extends SuperServiceImpl<AssetAcceptDetailMapper, AssetAcceptDetailEntity> implements AssetAcceptDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetAcceptDetailDTO.AddDTO addDTO) {
        AssetAcceptDetailEntity assetAcceptDetailEntity = new AssetAcceptDetailEntity();
        BeanMapperUtils.copy(addDTO, assetAcceptDetailEntity);

        // 数据处理
        handleData(assetAcceptDetailEntity);

        log.info("开始新增资产验收表明细单");
        boolean save = super.save(assetAcceptDetailEntity);
        if(!save) {
            throw new ServiceException("资产验收表明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产验收表明细单" , assetAcceptDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, assetAcceptDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(assetAcceptDetailEntity.getId(), assetAcceptDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetAcceptDetailDTO.UpdateDTO addOrUpdateDTO) {
        AssetAcceptDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产验收表明细单"));
        AssetAcceptDetailEntity assetAcceptDetailEntity =  BeanMapperUtils.map(AssetAcceptDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetAcceptDetailEntity);
        log.info("编辑 开始修改资产验收表明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(assetAcceptDetailEntity);
        if(!save) {
            throw new ServiceException("资产验收表明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录资产验收表明细单日志数据，id：【{}】", assetAcceptDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetAcceptDetailEntity.getId(), "资产验收表明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, assetAcceptDetailEntity, null, assetAcceptDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Integer getAcceptQtyByDetailId(String detailId) {
        LambdaQueryWrapper<AssetAcceptDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetAcceptDetailEntity::getSourceDetailId, detailId)
                .eq(AssetAcceptDetailEntity::getIsDeleted, Boolean.FALSE);
        List<AssetAcceptDetailEntity> list = this.list(queryWrapper);
        if (!list.isEmpty()) {
            return list.stream().mapToInt(AssetAcceptDetailEntity::getAcceptedQty).sum();
        }
        return null;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetAcceptDetailEntity assetAcceptDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
