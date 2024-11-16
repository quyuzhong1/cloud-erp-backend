package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.server.wms.mapper.WmsPushMsgMapper;
import com.erp.server.wms.service.WmsPushMsgService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.WmsPushMsgDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 本地推送消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-22
 */
@Slf4j
@Service
public class WmsPushMsgServiceImpl extends SuperServiceImpl<WmsPushMsgMapper, WmsPushMsgEntity> implements WmsPushMsgService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WmsPushMsgDTO.AddDTO addDTO) {
        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        BeanMapperUtils.copy(addDTO, wmsPushMsgEntity);

        // 数据处理
        handleData(wmsPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(wmsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "本地推送消息单" , wmsPushMsgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, wmsPushMsgEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(wmsPushMsgEntity.getId(), wmsPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WmsPushMsgDTO.UpdateDTO updateDTO) {
        WmsPushMsgEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单");
        }
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单"));
        WmsPushMsgEntity wmsPushMsgEntity =  BeanMapperUtils.map(WmsPushMsgEntity.class, updateDTO);

        // 数据处理
        handleData(wmsPushMsgEntity);
        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(wmsPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", wmsPushMsgEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), wmsPushMsgEntity.getId(), "本地推送消息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, wmsPushMsgEntity, null, wmsPushMsgEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(WmsPushMsgEntity wmsPushMsgEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
