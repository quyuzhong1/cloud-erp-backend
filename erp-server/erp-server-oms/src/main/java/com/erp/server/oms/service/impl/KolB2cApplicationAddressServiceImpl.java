package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolB2cApplicationAddressEntity;
import com.erp.server.oms.mapper.KolB2cApplicationAddressMapper;
import com.erp.server.oms.service.KolB2cApplicationAddressService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolB2cApplicationAddressDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * B2C寄样申请单地址信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolB2cApplicationAddressServiceImpl extends SuperServiceImpl<KolB2cApplicationAddressMapper, KolB2cApplicationAddressEntity> implements KolB2cApplicationAddressService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolB2cApplicationAddressDTO.AddDTO addDTO) {
        KolB2cApplicationAddressEntity kolB2cApplicationAddressEntity = new KolB2cApplicationAddressEntity();
        BeanMapperUtils.copy(addDTO, kolB2cApplicationAddressEntity);

        // 数据处理
        handleData(kolB2cApplicationAddressEntity);

        log.info("开始新增B2C寄样申请单地址信息");
        boolean save = super.save(kolB2cApplicationAddressEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单地址信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C寄样申请单地址信息" , kolB2cApplicationAddressEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolB2cApplicationAddressEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolB2cApplicationAddressEntity.getId(), kolB2cApplicationAddressEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolB2cApplicationAddressDTO.UpdateDTO addOrUpdateDTO) {
        KolB2cApplicationAddressEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2C寄样申请单地址信息"));
        KolB2cApplicationAddressEntity kolB2cApplicationAddressEntity =  BeanMapperUtils.map(KolB2cApplicationAddressEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolB2cApplicationAddressEntity);
        log.info("编辑 开始修改B2C寄样申请单地址信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolB2cApplicationAddressEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单地址信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C寄样申请单地址信息日志数据，id：【{}】", kolB2cApplicationAddressEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolB2cApplicationAddressEntity.getId(), "B2C寄样申请单地址信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolB2cApplicationAddressEntity, null, kolB2cApplicationAddressEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolB2cApplicationAddressEntity kolB2cApplicationAddressEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
