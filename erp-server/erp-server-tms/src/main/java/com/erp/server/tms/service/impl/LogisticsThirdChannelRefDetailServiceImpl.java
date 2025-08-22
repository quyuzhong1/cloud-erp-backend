package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.tms.entity.LogisticsThirdChannelRefDetailEntity;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.server.tms.mapper.LogisticsThirdChannelRefDetailMapper;
import com.erp.server.tms.service.LogisticsThirdChannelRefDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDetailDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流-第三方渠道关系明细表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-07-30
 */
@Slf4j
@Service
public class LogisticsThirdChannelRefDetailServiceImpl extends SuperServiceImpl<LogisticsThirdChannelRefDetailMapper, LogisticsThirdChannelRefDetailEntity> implements LogisticsThirdChannelRefDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDetailDTO.AddDTO addDTO) {
        LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity = new LogisticsThirdChannelRefDetailEntity();
        BeanMapperUtils.copy(addDTO, logisticsThirdChannelRefDetailEntity);

        // 数据处理
        handleData(logisticsThirdChannelRefDetailEntity);

        log.info("开始新增物流-第三方渠道关系明细单");
        boolean save = super.save(logisticsThirdChannelRefDetailEntity);
        if(!save) {
            throw new ServiceException("物流-第三方渠道关系明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流-第三方渠道关系明细单" , logisticsThirdChannelRefDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsThirdChannelRefDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsThirdChannelRefDetailEntity.getId(), logisticsThirdChannelRefDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsThirdChannelRefDetailDTO.UpdateDTO addOrUpdateDTO) {
        LogisticsThirdChannelRefDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流-第三方渠道关系明细单"));
        LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity =  BeanMapperUtils.map(LogisticsThirdChannelRefDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(logisticsThirdChannelRefDetailEntity);
        log.info("编辑 开始修改物流-第三方渠道关系明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsThirdChannelRefDetailEntity);
        if(!save) {
            throw new ServiceException("物流-第三方渠道关系明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流-第三方渠道关系明细单日志数据，id：【{}】", logisticsThirdChannelRefDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsThirdChannelRefDetailEntity.getId(), "物流-第三方渠道关系明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsThirdChannelRefDetailEntity, null, logisticsThirdChannelRefDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDetail(LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity, List<LogisticsThirdChannelRefDetailEntity> detailList) {
        List<String> detailIds = detailList.stream().map(LogisticsThirdChannelRefDetailEntity::getId).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        // 先删除旧明细
        super.remove(new LambdaQueryWrapper<LogisticsThirdChannelRefDetailEntity>().eq(LogisticsThirdChannelRefDetailEntity::getMainId, logisticsThirdChannelRefEntity.getId())
                .notIn(CollUtil.isNotEmpty(detailIds), LogisticsThirdChannelRefDetailEntity::getId, detailIds));
        // 再新增明细
        if(CollectionUtil.isNotEmpty(detailList)) {
            if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode())) {
                //增加店铺是否重复校验
                List<String> shopIds = detailList.stream().map(LogisticsThirdChannelRefDetailEntity::getShopId).distinct().collect(Collectors.toList());
                if(shopIds.size() != detailList.size()){
                    throw new ServiceException("明细中店铺不能重复配置");
                }
                List<ShopInfoEntity> shopInfoEntities = FeignQuery.getByIds(ShopInfoEntity.class, shopIds);
                detailList.forEach(detail -> {
                    ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(shop -> shop.getId().equals(detail.getShopId())).findFirst().orElseThrow(()->new ServiceException("店铺不存在"));
                    detail.setShopName(shopInfoEntity.getName());
                });
            }else if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode())) {
                //增加平台是否重复校验
                List<String> platformIds = detailList.stream().map(LogisticsThirdChannelRefDetailEntity::getDictPlatform).distinct().collect(Collectors.toList());
                if(platformIds.size() != detailList.size()){
                    throw new ServiceException("明细中平台不能重复配置");
                }
            }
            detailList.forEach(detail -> detail.setMainId(logisticsThirdChannelRefEntity.getId()));
            super.saveOrUpdateBatch(detailList);
        }
    }

    @Override
    public List<LogisticsThirdChannelRefDetailEntity> listByMainIdList(List<String> mainIds) {
        if (CollectionUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return super.list(new LambdaQueryWrapper<LogisticsThirdChannelRefDetailEntity>().in(LogisticsThirdChannelRefDetailEntity::getMainId, mainIds));
    }

    @Override
    public void removeByMainId(String mainId) {
        super.remove(new LambdaQueryWrapper<LogisticsThirdChannelRefDetailEntity>().eq(LogisticsThirdChannelRefDetailEntity::getMainId, mainId));
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsThirdChannelRefDetailEntity logisticsThirdChannelRefDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
