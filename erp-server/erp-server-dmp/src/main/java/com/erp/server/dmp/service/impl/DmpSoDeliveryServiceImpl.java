package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSoDeliveryEntity;
import com.erp.server.dmp.mapper.DmpSoDeliveryMapper;
import com.erp.server.dmp.service.DmpSoDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSoDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中台配货单主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2025-04-17
 */
@Slf4j
@Service
public class DmpSoDeliveryServiceImpl extends SuperServiceImpl<DmpSoDeliveryMapper, DmpSoDeliveryEntity> implements DmpSoDeliveryService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoDeliveryDTO.AddDTO addDTO) {
        DmpSoDeliveryEntity dmpSoDeliveryEntity = new DmpSoDeliveryEntity();
        BeanMapperUtils.copy(addDTO, dmpSoDeliveryEntity);

        // 数据处理
        handleData(dmpSoDeliveryEntity);

        log.info("开始新增中台配货单主单");
        boolean save = super.save(dmpSoDeliveryEntity);
        if(!save) {
            throw new ServiceException("中台配货单主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台配货单主单" , dmpSoDeliveryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoDeliveryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoDeliveryEntity.getId(), dmpSoDeliveryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoDeliveryDTO.UpdateDTO addOrUpdateDTO) {
        DmpSoDeliveryEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台配货单主单"));
        DmpSoDeliveryEntity dmpSoDeliveryEntity =  BeanMapperUtils.map(DmpSoDeliveryEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpSoDeliveryEntity);
        log.info("编辑 开始修改中台配货单主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoDeliveryEntity);
        if(!save) {
            throw new ServiceException("中台配货单主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台配货单主单日志数据，id：【{}】", dmpSoDeliveryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoDeliveryEntity.getId(), "中台配货单主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoDeliveryEntity, null, dmpSoDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoDeliveryEntity dmpSoDeliveryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
