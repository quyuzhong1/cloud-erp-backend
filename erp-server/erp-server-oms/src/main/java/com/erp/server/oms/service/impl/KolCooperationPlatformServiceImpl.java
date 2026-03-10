package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolCooperationPlatformEntity;
import com.erp.server.oms.mapper.KolCooperationPlatformMapper;
import com.erp.server.oms.service.KolCooperationPlatformService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolCooperationPlatformDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 达人合作平台信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
@Slf4j
@Service
public class KolCooperationPlatformServiceImpl extends SuperServiceImpl<KolCooperationPlatformMapper, KolCooperationPlatformEntity> implements KolCooperationPlatformService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolCooperationPlatformDTO.AddDTO addDTO) {
        KolCooperationPlatformEntity kolCooperationPlatformEntity = new KolCooperationPlatformEntity();
        BeanMapperUtils.copy(addDTO, kolCooperationPlatformEntity);

        // 数据处理
        handleData(kolCooperationPlatformEntity);

        log.info("开始新增达人合作平台信息");
        boolean save = super.save(kolCooperationPlatformEntity);
        if(!save) {
            throw new ServiceException("达人合作平台信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "达人合作平台信息" , kolCooperationPlatformEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolCooperationPlatformEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolCooperationPlatformEntity.getId(), kolCooperationPlatformEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolCooperationPlatformDTO.UpdateDTO addOrUpdateDTO) {
        KolCooperationPlatformEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "达人合作平台信息"));
        KolCooperationPlatformEntity kolCooperationPlatformEntity =  BeanMapperUtils.map(KolCooperationPlatformEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolCooperationPlatformEntity);
        log.info("编辑 开始修改达人合作平台信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolCooperationPlatformEntity);
        if(!save) {
            throw new ServiceException("达人合作平台信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录达人合作平台信息日志数据，id：【{}】", kolCooperationPlatformEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolCooperationPlatformEntity.getId(), "达人合作平台信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolCooperationPlatformEntity, null, kolCooperationPlatformEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolCooperationPlatformEntity kolCooperationPlatformEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
