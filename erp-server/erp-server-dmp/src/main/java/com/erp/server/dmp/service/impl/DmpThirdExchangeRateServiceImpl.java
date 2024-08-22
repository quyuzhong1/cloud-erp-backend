package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpThirdExchangeRateEntity;
import com.erp.server.dmp.mapper.DmpThirdExchangeRateMapper;
import com.erp.server.dmp.service.DmpThirdExchangeRateService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpThirdExchangeRateDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 第三方汇率 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-19
 */
@Slf4j
@Service
public class DmpThirdExchangeRateServiceImpl extends SuperServiceImpl<DmpThirdExchangeRateMapper, DmpThirdExchangeRateEntity> implements DmpThirdExchangeRateService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpThirdExchangeRateDTO.AddDTO addDTO) {
        DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity = new DmpThirdExchangeRateEntity();
        BeanMapperUtils.copy(addDTO, dmpThirdExchangeRateEntity);

        // 数据处理
        handleData(dmpThirdExchangeRateEntity);

        log.info("开始新增第三方汇率");
        boolean save = super.save(dmpThirdExchangeRateEntity);
        if(!save) {
            throw new ServiceException("第三方汇率保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方汇率" , dmpThirdExchangeRateEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpThirdExchangeRateEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpThirdExchangeRateEntity.getId(), dmpThirdExchangeRateEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpThirdExchangeRateDTO.UpdateDTO updateDTO) {
        DmpThirdExchangeRateEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方汇率"));
        
        DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity =  BeanMapperUtils.map(DmpThirdExchangeRateEntity.class, updateDTO);

        // 数据处理
        handleData(dmpThirdExchangeRateEntity);
        log.info("编辑 开始修改第三方汇率数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpThirdExchangeRateEntity);
        if(!save) {
            throw new ServiceException("第三方汇率保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方汇率日志数据，id：【{}】", dmpThirdExchangeRateEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpThirdExchangeRateEntity.getId(), "第三方汇率");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpThirdExchangeRateEntity, null, dmpThirdExchangeRateEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpThirdExchangeRateEntity dmpThirdExchangeRateEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
