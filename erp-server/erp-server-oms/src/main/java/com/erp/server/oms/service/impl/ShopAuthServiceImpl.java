package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.server.oms.mapper.ShopAuthMapper;
import com.erp.server.oms.service.ShopAuthService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ShopAuthDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 店铺授权表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@Service
public class ShopAuthServiceImpl extends SuperServiceImpl<ShopAuthMapper, ShopAuthEntity> implements ShopAuthService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ShopAuthDTO.AddDTO addDTO) {
        ShopAuthEntity shopAuthEntity = new ShopAuthEntity();
        BeanMapperUtils.copy(addDTO, shopAuthEntity);

        // 数据处理
        handleData(shopAuthEntity);

        log.info("开始新增店铺授权单");
        boolean save = super.save(shopAuthEntity);
        if(!save) {
            throw new ServiceException("店铺授权单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "店铺授权单" , shopAuthEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shopAuthEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return shopAuthEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShopAuthDTO.UpdateDTO updateDTO) {
        ShopAuthEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "店铺授权单"));
        ShopAuthEntity shopAuthEntity =  BeanMapperUtils.map(ShopAuthEntity.class, updateDTO);

        // 数据处理
        handleData(shopAuthEntity);
        log.info("编辑 开始修改店铺授权单数据，id：【{}】", old.getId());
        boolean save = super.updateById(shopAuthEntity);
        if(!save) {
            throw new ServiceException("店铺授权单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录店铺授权单日志数据，id：【{}】", shopAuthEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shopAuthEntity.getId(), "店铺授权单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shopAuthEntity, null, shopAuthEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShopAuthEntity shopAuthEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
