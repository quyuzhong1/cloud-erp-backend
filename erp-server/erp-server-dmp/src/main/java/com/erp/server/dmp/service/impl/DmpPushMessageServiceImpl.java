package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpPushMessageEntity;
import com.erp.server.dmp.mapper.DmpPushMessageMapper;
import com.erp.server.dmp.service.DmpPushMessageService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpPushMessageDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 本地消息表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-21
 */
@Slf4j
@Service
public class DmpPushMessageServiceImpl extends SuperServiceImpl<DmpPushMessageMapper, DmpPushMessageEntity> implements DmpPushMessageService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpPushMessageDTO.AddDTO addDTO) {
        DmpPushMessageEntity dmpPushMessageEntity = new DmpPushMessageEntity();
        BeanMapperUtils.copy(addDTO, dmpPushMessageEntity);

        // 数据处理
        handleData(dmpPushMessageEntity);

        log.info("开始新增本地消息单");
        boolean save = super.save(dmpPushMessageEntity);
        if(!save) {
            throw new ServiceException("本地消息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "本地消息单" , dmpPushMessageEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpPushMessageEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpPushMessageEntity.getId(), dmpPushMessageEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpPushMessageDTO.UpdateDTO updateDTO) {
        DmpPushMessageEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "本地消息单"));
        DmpPushMessageEntity dmpPushMessageEntity =  BeanMapperUtils.map(DmpPushMessageEntity.class, updateDTO);

        // 数据处理
        handleData(dmpPushMessageEntity);
        log.info("编辑 开始修改本地消息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpPushMessageEntity);
        if(!save) {
            throw new ServiceException("本地消息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录本地消息单日志数据，id：【{}】", dmpPushMessageEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpPushMessageEntity.getId(), "本地消息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpPushMessageEntity, null, dmpPushMessageEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpPushMessageEntity dmpPushMessageEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
