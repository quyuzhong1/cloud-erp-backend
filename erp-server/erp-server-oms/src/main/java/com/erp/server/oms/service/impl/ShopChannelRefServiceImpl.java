package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.ShopChannelRefEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.oms.mapper.ShopChannelRefMapper;
import com.erp.server.oms.service.ShopChannelRefService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.oms.service.ShopInfoService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ShopChannelRefDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 店铺渠道关联表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2025-02-14
 */
@Slf4j
@Service
public class ShopChannelRefServiceImpl extends SuperServiceImpl<ShopChannelRefMapper, ShopChannelRefEntity> implements ShopChannelRefService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private LogisticsFeign logisticsFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShopChannelRefDTO.AddDTO addDTO) {
        ShopChannelRefEntity shopChannelRefEntity = new ShopChannelRefEntity();
        BeanMapperUtils.copy(addDTO, shopChannelRefEntity);

        // 数据处理
        handleData(shopChannelRefEntity);

        log.info("开始新增店铺渠道关联单");
        boolean save = super.save(shopChannelRefEntity);
        if(!save) {
            throw new ServiceException("店铺渠道关联单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "店铺渠道关联单" , shopChannelRefEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shopChannelRefEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shopChannelRefEntity.getId(), shopChannelRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShopChannelRefDTO.UpdateDTO addOrUpdateDTO) {
        ShopChannelRefEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "店铺渠道关联单"));
        ShopChannelRefEntity shopChannelRefEntity =  BeanMapperUtils.map(ShopChannelRefEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(shopChannelRefEntity);
        log.info("编辑 开始修改店铺渠道关联单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shopChannelRefEntity);
        if(!save) {
            throw new ServiceException("店铺渠道关联单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录店铺渠道关联单日志数据，id：【{}】", shopChannelRefEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), shopChannelRefEntity.getId(), "店铺渠道关联单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shopChannelRefEntity, null, shopChannelRefEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(ShopInfoEntity shop, List<String> channelIdList) {
        if(Objects.isNull(shop) || StringUtils.isBlank(shop.getId())) {
            return;
        }
        if(Objects.isNull(channelIdList)){
            channelIdList = new ArrayList<>();
        }
        channelIdList = channelIdList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ShopChannelRefEntity> existsList = this.lambdaQuery().eq(ShopChannelRefEntity::getShopId, shop.getId()).list();
        //新增
        List<String> existsChannelIdList = existsList.stream().map(ShopChannelRefEntity::getLogisticsChannelId).collect(Collectors.toList());
        List<String> addChannelIdList = channelIdList.stream().filter(v->!existsChannelIdList.contains(v)).collect(Collectors.toList());
        List<ShopChannelRefEntity> addList =new ArrayList<>();
        addChannelIdList.forEach(v->{
            ShopChannelRefEntity entity = new ShopChannelRefEntity();
            entity.setShopId(shop.getId());
            entity.setLogisticsChannelId(v);
            addList.add(entity);
        });
        //删除
        List<String> finalChannelIdList = channelIdList;
        List<ShopChannelRefEntity> deleteList = existsList.stream().filter(v->!finalChannelIdList.contains(v.getLogisticsChannelId())).collect(Collectors.toList());

        if(!addList.isEmpty()) {
            this.saveBatch(addList);
        }
        if(!deleteList.isEmpty()) {
            this.removeByIds(deleteList.stream().map(ShopChannelRefEntity::getId).collect(Collectors.toList()));
        }

    }

    @Override
    public List<ShopChannelRefDTO.ViewDTO> getViewByShopId(String id) {
        if(StringUtils.isBlank(id)){
            return new ArrayList<>();
        }
        List<ShopChannelRefEntity> list = this.lambdaQuery().eq(ShopChannelRefEntity::getShopId, id).list();
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        List<String> channelIdList = list.stream().map(ShopChannelRefEntity::getLogisticsChannelId).collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntities = FeignQuery.getByIds(LogisticsChannelEntity.class,channelIdList);
        List<ShopChannelRefDTO.ViewDTO> viewDTOS = new ArrayList<>();
        list.forEach(v->{
            ShopChannelRefDTO.ViewDTO viewDTO = new ShopChannelRefDTO.ViewDTO();
            viewDTO.setLogisticsChannelId(v.getLogisticsChannelId());
            viewDTO.setLogisticsChannelName(logisticsChannelEntities.stream().filter(l->l.getId().equals(v.getLogisticsChannelId())).findFirst().map(LogisticsChannelEntity::getName).orElse(""));
            viewDTOS.add(viewDTO);
        });
        return viewDTOS;
    }

    @Override
    public void checkChannel(String shopId, String newChannelId) {
        if(StringUtils.isBlank(shopId)){
            throw new ServiceException("店铺id不能为空");
        }
        List<ShopChannelRefEntity> list = this.lambdaQuery().eq(ShopChannelRefEntity::getShopId, shopId).list();
        if(CollectionUtils.isNotEmpty(list)){
            if(list.stream().noneMatch(v->v.getLogisticsChannelId().equals(newChannelId))){
                ShopInfoEntity shopInfoEntity = shopInfoService.getById(shopId);
                List<String> channelIds = list.stream().map(ShopChannelRefEntity::getLogisticsChannelId).collect(Collectors.toList());
                //截取前三个
                if(channelIds.size()>3){
                    channelIds = channelIds.subList(0,3);
                }
                List<LogisticsChannelDTO.BaseDTO>  baseDTOS = logisticsFeign.listChannelInfoById(channelIds);
                //封装成物流商名称+渠道名称
                String channelNames = baseDTOS.stream().map(v->v.getLogisticsSupplierName()+"-"+v.getName()).collect(Collectors.joining(","));
                throw new ServiceException("{}可用渠道为{}，详情请查看店铺管理-海外仓交运渠道配置",shopInfoEntity.getName(),channelNames);
            }
        }else{
            throw new ServiceException("该店铺未绑定任何渠道");
        }
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShopChannelRefEntity shopChannelRefEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
