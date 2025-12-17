package com.erp.server.plm.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.PlmPushMsgDTO;
import com.erp.model.plm.entity.PlmPushMsgEntity;
import com.erp.server.plm.mapper.PlmPushMsgMapper;
import com.erp.server.plm.service.PlmPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-27
 */
@Slf4j
@Service
public class PlmPushMsgServiceImpl extends SuperServiceImpl<PlmPushMsgMapper, PlmPushMsgEntity> implements PlmPushMsgService {

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PlmPushMsgDTO.AddDTO addDTO) {
        PlmPushMsgEntity plmPushMsgEntity = new PlmPushMsgEntity();
        BeanMapperUtils.copy(addDTO, plmPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(plmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(plmPushMsgEntity.getId(), plmPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PlmPushMsgDTO.UpdateDTO updateDTO) {
        PlmPushMsgEntity old = super.getById(updateDTO.getId());
        PlmPushMsgEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单"));
        PlmPushMsgEntity plmPushMsgEntity =  BeanMapperUtils.map(PlmPushMsgEntity.class, updateDTO);

        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(plmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", plmPushMsgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }

}
