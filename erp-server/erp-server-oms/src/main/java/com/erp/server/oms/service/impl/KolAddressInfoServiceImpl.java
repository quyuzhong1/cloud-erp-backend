package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolAddressInfoEntity;
import com.erp.server.oms.mapper.KolAddressInfoMapper;
import com.erp.server.oms.service.KolAddressInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolAddressInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 达人地址信息 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
@Slf4j
@Service
public class KolAddressInfoServiceImpl extends SuperServiceImpl<KolAddressInfoMapper, KolAddressInfoEntity> implements KolAddressInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolAddressInfoDTO.AddDTO addDTO) {
        KolAddressInfoEntity kolAddressInfoEntity = new KolAddressInfoEntity();
        BeanMapperUtils.copy(addDTO, kolAddressInfoEntity);

        // 数据处理
        handleData(kolAddressInfoEntity);

        log.info("开始新增达人地址信息");
        boolean save = super.save(kolAddressInfoEntity);
        if(!save) {
            throw new ServiceException("达人地址信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "达人地址信息" , kolAddressInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolAddressInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolAddressInfoEntity.getId(), kolAddressInfoEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolAddressInfoDTO.UpdateDTO addOrUpdateDTO) {
        KolAddressInfoEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "达人地址信息"));
        KolAddressInfoEntity kolAddressInfoEntity =  BeanMapperUtils.map(KolAddressInfoEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolAddressInfoEntity);
        log.info("编辑 开始修改达人地址信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolAddressInfoEntity);
        if(!save) {
            throw new ServiceException("达人地址信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录达人地址信息日志数据，id：【{}】", kolAddressInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolAddressInfoEntity.getId(), "达人地址信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolAddressInfoEntity, null, kolAddressInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolAddressInfoEntity kolAddressInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
