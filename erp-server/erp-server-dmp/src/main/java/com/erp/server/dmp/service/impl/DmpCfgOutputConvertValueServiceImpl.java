package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgOutputConvertValueEntity;
import com.erp.server.dmp.mapper.DmpCfgOutputConvertValueMapper;
import com.erp.server.dmp.service.DmpCfgOutputConvertValueService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgOutputConvertValueDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 推送字段映射值 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-20
 */
@Slf4j
@Service
public class DmpCfgOutputConvertValueServiceImpl extends SuperServiceImpl<DmpCfgOutputConvertValueMapper, DmpCfgOutputConvertValueEntity> implements DmpCfgOutputConvertValueService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputConvertValueDTO.AddDTO addDTO) {
        DmpCfgOutputConvertValueEntity dmpCfgOutputConvertValueEntity = new DmpCfgOutputConvertValueEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputConvertValueEntity);

        // 数据处理
        handleData(dmpCfgOutputConvertValueEntity);

        log.info("开始新增推送字段映射值");
        boolean save = super.save(dmpCfgOutputConvertValueEntity);
        if(!save) {
            throw new ServiceException("推送字段映射值保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送字段映射值" , dmpCfgOutputConvertValueEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgOutputConvertValueEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputConvertValueEntity.getId(), dmpCfgOutputConvertValueEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputConvertValueDTO.UpdateDTO updateDTO) {
        DmpCfgOutputConvertValueEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送字段映射值"));
        DmpCfgOutputConvertValueEntity dmpCfgOutputConvertValueEntity =  BeanMapperUtils.map(DmpCfgOutputConvertValueEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputConvertValueEntity);
        log.info("编辑 开始修改推送字段映射值数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputConvertValueEntity);
        if(!save) {
            throw new ServiceException("推送字段映射值保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送字段映射值日志数据，id：【{}】", dmpCfgOutputConvertValueEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputConvertValueEntity.getId(), "推送字段映射值");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgOutputConvertValueEntity, null, dmpCfgOutputConvertValueEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputConvertValueEntity dmpCfgOutputConvertValueEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
