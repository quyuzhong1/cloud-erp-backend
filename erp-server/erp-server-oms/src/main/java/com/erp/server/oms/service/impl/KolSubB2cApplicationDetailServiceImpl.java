package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolSubB2cApplicationDetailEntity;
import com.erp.server.oms.mapper.KolSubB2cApplicationDetailMapper;
import com.erp.server.oms.service.KolSubB2cApplicationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolSubB2cApplicationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * B2C寄样申请单拆分单明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolSubB2cApplicationDetailServiceImpl extends SuperServiceImpl<KolSubB2cApplicationDetailMapper, KolSubB2cApplicationDetailEntity> implements KolSubB2cApplicationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSubB2cApplicationDetailDTO.AddDTO addDTO) {
        KolSubB2cApplicationDetailEntity kolSubB2cApplicationDetailEntity = new KolSubB2cApplicationDetailEntity();
        BeanMapperUtils.copy(addDTO, kolSubB2cApplicationDetailEntity);

        // 数据处理
        handleData(kolSubB2cApplicationDetailEntity);

        log.info("开始新增B2C寄样申请单拆分单明细");
        boolean save = super.save(kolSubB2cApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单拆分单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C寄样申请单拆分单明细" , kolSubB2cApplicationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolSubB2cApplicationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolSubB2cApplicationDetailEntity.getId(), kolSubB2cApplicationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSubB2cApplicationDetailDTO.UpdateDTO addOrUpdateDTO) {
        KolSubB2cApplicationDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "B2C寄样申请单拆分单明细"));
        KolSubB2cApplicationDetailEntity kolSubB2cApplicationDetailEntity =  BeanMapperUtils.map(KolSubB2cApplicationDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSubB2cApplicationDetailEntity);
        log.info("编辑 开始修改B2C寄样申请单拆分单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSubB2cApplicationDetailEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单拆分单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C寄样申请单拆分单明细日志数据，id：【{}】", kolSubB2cApplicationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolSubB2cApplicationDetailEntity.getId(), "B2C寄样申请单拆分单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolSubB2cApplicationDetailEntity, null, kolSubB2cApplicationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolSubB2cApplicationDetailEntity kolSubB2cApplicationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
