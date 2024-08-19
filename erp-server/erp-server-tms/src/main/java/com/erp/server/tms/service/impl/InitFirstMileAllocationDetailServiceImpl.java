package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import com.erp.model.tms.entity.InitFirstMileAllocationDetailEntity;
import com.erp.server.tms.mapper.InitFirstMileAllocationDetailMapper;
import com.erp.server.tms.service.InitFirstMileAllocationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.InitFirstMileAllocationDetailDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * 期初头程分摊明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-08-13
 */
@Slf4j
@Service
public class InitFirstMileAllocationDetailServiceImpl extends SuperServiceImpl<InitFirstMileAllocationDetailMapper, InitFirstMileAllocationDetailEntity> implements InitFirstMileAllocationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InitFirstMileAllocationDetailDTO.AddDTO addDTO) {
        InitFirstMileAllocationDetailEntity initFirstMileAllocationDetailEntity = new InitFirstMileAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, initFirstMileAllocationDetailEntity);

        // 数据处理
        handleData(initFirstMileAllocationDetailEntity);

        log.info("开始新增期初头程分摊明细");
        boolean save = super.save(initFirstMileAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("期初头程分摊明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "期初头程分摊明细", initFirstMileAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, initFirstMileAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(initFirstMileAllocationDetailEntity.getId(), initFirstMileAllocationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InitFirstMileAllocationDetailDTO.UpdateDTO updateDTO) {
        InitFirstMileAllocationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "期初头程分摊明细"));
        InitFirstMileAllocationDetailEntity initFirstMileAllocationDetailEntity = BeanMapperUtils.map(InitFirstMileAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(initFirstMileAllocationDetailEntity);
        log.info("编辑 开始修改期初头程分摊明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(initFirstMileAllocationDetailEntity);
        if (!save) {
            throw new ServiceException("期初头程分摊明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录期初头程分摊明细日志数据，id：【{}】", initFirstMileAllocationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), initFirstMileAllocationDetailEntity.getId(), "期初头程分摊明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, initFirstMileAllocationDetailEntity, null, initFirstMileAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buildDetail(List<InitFirstMileAllocationDetailEntity> detailEntityList, String id) {
        if (CollectionUtils.isEmpty(detailEntityList)) {
            //明细为空则清空
            lambdaUpdate().eq(InitFirstMileAllocationDetailEntity::getMainId, id).remove();
            return;
        }
        List<String> newDetailIds = detailEntityList.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getId())).map(InitFirstMileAllocationDetailEntity::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newDetailIds)) {
            //明细为空则清空
            lambdaUpdate().eq(InitFirstMileAllocationDetailEntity::getMainId, id).remove();
        }
        //检查数据是否已存在
        List<String> sourceIds = detailEntityList.stream().map(InitFirstMileAllocationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        List<InitFirstMileAllocationDetailEntity> existDetailEntityList = this.listBySourceIds(sourceIds);
        detailEntityList.forEach(detailEntity -> {
            if (!CollectionUtils.isEmpty(existDetailEntityList)){
                InitFirstMileAllocationDetailEntity entity = existDetailEntityList.stream().filter(e -> Objects.nonNull(e)
                        && !Objects.equals(id, e.getMainId())
                        && Objects.equals(e.getSourceId(), detailEntity.getSourceId())
                        && Objects.equals(e.getSourceDetailId(), detailEntity.getSourceDetailId())
                        && Objects.equals(e.getSkuId(), detailEntity.getSkuId())).findFirst().orElse(null);
                if (Objects.nonNull(entity)){
                    throw new ServiceException(StrUtil.format("发货单【{}】SKU【{}】已存在", entity.getSourceCode(),entity.getSkuNo()));
                }
            }
        });

        List<InitFirstMileAllocationDetailEntity> oldDetailEntityList = this.listByMainIds(Collections.singletonList(id));
        if (!CollectionUtils.isEmpty(oldDetailEntityList)) {
            List<String> oldDetailIds = oldDetailEntityList.stream().map(InitFirstMileAllocationDetailEntity::getId).distinct().collect(Collectors.toList());
            List<String> notExistDetailIds = oldDetailIds.stream().filter(e -> !newDetailIds.contains(e)).distinct().collect(Collectors.toList());
            //清空不存在的明细记录
            this.removeByIds(notExistDetailIds);
        }
        detailEntityList.forEach(detailEntity -> {
            detailEntity.setMainId(id);
            if (Objects.isNull(detailEntity.getExchangeRate())){
                detailEntity.setExchangeRate(BigDecimal.ONE);
            }
            if (StrUtil.isBlank(detailEntity.getCurrency())){
                detailEntity.setCurrency(CurrencyEnum.CNY.getCurrencyCode());
                detailEntity.setCurrencySymbol(CurrencyEnum.CNY.getCurrencySymbol());
            }
            if (StrUtil.isBlank(detailEntity.getWeightUnit())){
                detailEntity.setWeightUnit(UnitEnum.WeightUnitEnum.KG.code);
            }
        });
        this.saveOrUpdateBatch(detailEntityList);
    }
    @Override
    public List<InitFirstMileAllocationDetailEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(InitFirstMileAllocationDetailEntity::getSourceId,sourceIds).list();
    }

    /**
     * 根据主表id获取明细记录
     *
     * @param mainIds
     * @return
     */
    @Override
    public List<InitFirstMileAllocationDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(InitFirstMileAllocationDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public void removeByMainId(String id) {
        if (!StrUtil.isBlank(id)) {
            this.lambdaUpdate().eq(InitFirstMileAllocationDetailEntity::getMainId, id).remove();
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(InitFirstMileAllocationDetailEntity initFirstMileAllocationDetailEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
