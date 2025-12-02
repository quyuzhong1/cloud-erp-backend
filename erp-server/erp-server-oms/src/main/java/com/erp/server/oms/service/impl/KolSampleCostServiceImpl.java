package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolSampleCostEntity;
import com.erp.server.oms.mapper.KolSampleCostMapper;
import com.erp.server.oms.service.KolSampleCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolSampleCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 寄样费用表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolSampleCostServiceImpl extends SuperServiceImpl<KolSampleCostMapper, KolSampleCostEntity> implements KolSampleCostService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSampleCostDTO.AddDTO addDTO) {
        KolSampleCostEntity kolSampleCostEntity = new KolSampleCostEntity();
        BeanMapperUtils.copy(addDTO, kolSampleCostEntity);

        // 数据处理
        handleData(kolSampleCostEntity);

        log.info("开始新增寄样费用单");
        boolean save = super.save(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "寄样费用单" , kolSampleCostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolSampleCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolSampleCostEntity.getId(), kolSampleCostEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSampleCostDTO.UpdateDTO addOrUpdateDTO) {
        KolSampleCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "寄样费用单"));
        KolSampleCostEntity kolSampleCostEntity =  BeanMapperUtils.map(KolSampleCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSampleCostEntity);
        log.info("编辑 开始修改寄样费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录寄样费用单日志数据，id：【{}】", kolSampleCostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolSampleCostEntity.getId(), "寄样费用单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolSampleCostEntity, null, kolSampleCostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolSampleCostEntity kolSampleCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
