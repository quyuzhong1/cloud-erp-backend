package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsChannelAddressEntity;
import com.erp.server.tms.mapper.LogisticsChannelAddressMapper;
import com.erp.server.tms.service.LogisticsChannelAddressService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsChannelAddressDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 渠道地址表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@Service
public class LogisticsChannelAddressServiceImpl extends SuperServiceImpl<LogisticsChannelAddressMapper, LogisticsChannelAddressEntity> implements LogisticsChannelAddressService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(String channelId, List<LogisticsChannelAddressDTO.AddDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.FALSE;
        }
        List<LogisticsChannelAddressEntity> saveList = BeanMapperUtils.copyList(LogisticsChannelAddressEntity.class, list);
        // 数据处理
        handleData(saveList);
        return Boolean.TRUE;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsChannelAddressDTO.UpdateDTO updateDTO) {
        LogisticsChannelAddressEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "渠道地址单"));
        LogisticsChannelAddressEntity logisticsChannelAddressEntity = BeanMapperUtils.map(LogisticsChannelAddressEntity.class, updateDTO);


        log.info("编辑 开始修改渠道地址单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsChannelAddressEntity);
        if (!save) {
            throw new ServiceException("渠道地址单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录渠道地址单日志数据，id：【{}】", logisticsChannelAddressEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsChannelAddressEntity.getId(), "渠道地址单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsChannelAddressEntity, null, logisticsChannelAddressEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(List<LogisticsChannelAddressEntity> list) {
        // TODO 验证数据 & 数据赋值
        List<String> shopIdList=list.stream().map(LogisticsChannelAddressEntity::getShopId).collect(Collectors.toList());
    }
}
