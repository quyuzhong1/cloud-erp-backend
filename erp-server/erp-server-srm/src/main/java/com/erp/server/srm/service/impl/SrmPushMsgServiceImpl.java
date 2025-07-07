package com.erp.server.srm.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.srm.dto.SrmPushMsgDTO;
import com.erp.model.srm.entity.SrmPushMsgEntity;
import com.erp.server.srm.mapper.SrmPushMsgMapper;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.SrmPushMsgService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

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
public class SrmPushMsgServiceImpl extends SuperServiceImpl<SrmPushMsgMapper, SrmPushMsgEntity> implements SrmPushMsgService {
    @Resource
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SrmPushMsgDTO.AddDTO addDTO) {
        SrmPushMsgEntity srmPushMsgEntity = new SrmPushMsgEntity();
        BeanMapperUtils.copy(addDTO, srmPushMsgEntity);

        // 数据处理
        handleData(srmPushMsgEntity);

        log.info("开始新增本地推送消息单");
        boolean save = super.save(srmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "本地推送消息单" , srmPushMsgEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, srmPushMsgEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(srmPushMsgEntity.getId(), srmPushMsgEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SrmPushMsgDTO.UpdateDTO updateDTO) {
        SrmPushMsgEntity old = super.getById(updateDTO.getId());
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "本地推送消息单");
        }
        SrmPushMsgEntity srmPushMsgEntity =  BeanMapperUtils.map(SrmPushMsgEntity.class, updateDTO);

        // 数据处理
        handleData(srmPushMsgEntity);
        log.info("编辑 开始修改本地推送消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(srmPushMsgEntity);
        if(!save) {
            throw new ServiceException("本地推送消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地推送消息单日志数据，id：【{}】", srmPushMsgEntity.getId());
            String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), srmPushMsgEntity.getId(), "本地推送消息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, srmPushMsgEntity, null, srmPushMsgEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SrmPushMsgEntity srmPushMsgEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
