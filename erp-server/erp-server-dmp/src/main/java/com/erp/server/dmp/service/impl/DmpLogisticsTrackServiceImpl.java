package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.erp.server.dmp.mapper.DmpLogisticsTrackMapper;
import com.erp.server.dmp.service.DmpLogisticsTrackService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpLogisticsTrackDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
 */
@Slf4j
@Service
public class DmpLogisticsTrackServiceImpl extends SuperServiceImpl<DmpLogisticsTrackMapper, DmpLogisticsTrackEntity> implements DmpLogisticsTrackService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpLogisticsTrackDTO.AddDTO addDTO) {
        DmpLogisticsTrackEntity dmpLogisticsTrackEntity = new DmpLogisticsTrackEntity();
        BeanMapperUtils.copy(addDTO, dmpLogisticsTrackEntity);

        // 数据处理
        handleData(dmpLogisticsTrackEntity);

        log.info("开始新增");
        boolean save = super.save(dmpLogisticsTrackEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , dmpLogisticsTrackEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpLogisticsTrackEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpLogisticsTrackEntity.getId(), dmpLogisticsTrackEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpLogisticsTrackDTO.UpdateDTO updateDTO) {
        DmpLogisticsTrackEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DmpLogisticsTrackEntity dmpLogisticsTrackEntity =  BeanMapperUtils.map(DmpLogisticsTrackEntity.class, updateDTO);

        // 数据处理
        handleData(dmpLogisticsTrackEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpLogisticsTrackEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", dmpLogisticsTrackEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpLogisticsTrackEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpLogisticsTrackEntity, null, dmpLogisticsTrackEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpLogisticsTrackEntity dmpLogisticsTrackEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
