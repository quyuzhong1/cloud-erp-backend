package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.MqConsumerRecordEntity;
import com.erp.server.sys.mapper.MqConsumerRecordMapper;
import com.erp.server.sys.service.MqConsumerRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * mq消费记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-05-29
 */
@Slf4j
@Service
public class MqConsumerRecordServiceImpl extends SuperServiceImpl<MqConsumerRecordMapper, MqConsumerRecordEntity> implements MqConsumerRecordService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(MqConsumerRecordDTO.AddDTO addDTO) {
        MqConsumerRecordEntity mqConsumerRecordEntity = new MqConsumerRecordEntity();
        BeanMapperUtils.copy(addDTO, mqConsumerRecordEntity);

        // 数据处理
        handleData(mqConsumerRecordEntity);

        log.info("开始新增mq消费记录");
        boolean save = super.save(mqConsumerRecordEntity);
        if(!save) {
            throw new ServiceException("mq消费记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "mq消费记录" , mqConsumerRecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, mqConsumerRecordEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(mqConsumerRecordEntity.getId(), mqConsumerRecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(MqConsumerRecordDTO.UpdateDTO addOrUpdateDTO) {
        MqConsumerRecordEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "mq消费记录"));
        MqConsumerRecordEntity mqConsumerRecordEntity =  BeanMapperUtils.map(MqConsumerRecordEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(mqConsumerRecordEntity);
        log.info("编辑 开始修改mq消费记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(mqConsumerRecordEntity);
        if(!save) {
            throw new ServiceException("mq消费记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录mq消费记录日志数据，id：【{}】", mqConsumerRecordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), mqConsumerRecordEntity.getId(), "mq消费记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, mqConsumerRecordEntity, null, mqConsumerRecordEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(MqConsumerRecordEntity mqConsumerRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
