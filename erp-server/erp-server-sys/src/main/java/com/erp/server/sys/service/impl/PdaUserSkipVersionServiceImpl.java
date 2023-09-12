package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.sys.entity.PdaUserSkipVersionEntity;
import com.erp.server.sys.mapper.PdaUserSkipVersionMapper;
import com.erp.server.sys.service.PdaUserSkipVersionService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.PdaUserSkipVersionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * PDA用户跳过版本升级记录表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-12
 */
@Slf4j
@Service
public class PdaUserSkipVersionServiceImpl extends SuperServiceImpl<PdaUserSkipVersionMapper, PdaUserSkipVersionEntity> implements PdaUserSkipVersionService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(PdaUserSkipVersionDTO.AddDTO addDTO) {
        PdaUserSkipVersionEntity pdaUserSkipVersionEntity = new PdaUserSkipVersionEntity();
        BeanMapperUtils.copy(addDTO, pdaUserSkipVersionEntity);

        // 数据处理
        handleData(pdaUserSkipVersionEntity);

        log.info("开始新增PDA用户跳过版本升级记录单");
        boolean save = super.save(pdaUserSkipVersionEntity);
        if(!save) {
            throw new ServiceException("PDA用户跳过版本升级记录单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "PDA用户跳过版本升级记录单" , pdaUserSkipVersionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, pdaUserSkipVersionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return pdaUserSkipVersionEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PdaUserSkipVersionDTO.UpdateDTO updateDTO) {
        PdaUserSkipVersionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "PDA用户跳过版本升级记录单"));
        PdaUserSkipVersionEntity pdaUserSkipVersionEntity =  BeanMapperUtils.map(PdaUserSkipVersionEntity.class, updateDTO);

        // 数据处理
        handleData(pdaUserSkipVersionEntity);
        log.info("编辑 开始修改PDA用户跳过版本升级记录单数据，id：【{}】", old.getId());
        boolean save = super.updateById(pdaUserSkipVersionEntity);
        if(!save) {
            throw new ServiceException("PDA用户跳过版本升级记录单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录PDA用户跳过版本升级记录单日志数据，id：【{}】", pdaUserSkipVersionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), pdaUserSkipVersionEntity.getId(), "PDA用户跳过版本升级记录单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, pdaUserSkipVersionEntity, null, pdaUserSkipVersionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PdaUserSkipVersionEntity pdaUserSkipVersionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
