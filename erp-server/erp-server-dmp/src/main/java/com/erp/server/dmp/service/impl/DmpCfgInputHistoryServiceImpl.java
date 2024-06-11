package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgInputHistoryEntity;
import com.erp.server.dmp.mapper.DmpCfgInputHistoryMapper;
import com.erp.server.dmp.service.DmpCfgInputHistoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgInputHistoryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 外部系统接口明细补偿 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputHistoryServiceImpl extends SuperServiceImpl<DmpCfgInputHistoryMapper, DmpCfgInputHistoryEntity> implements DmpCfgInputHistoryService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputHistoryDTO.AddDTO addDTO) {
        DmpCfgInputHistoryEntity dmpCfgInputHistoryEntity = new DmpCfgInputHistoryEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputHistoryEntity);

        // 数据处理
        handleData(dmpCfgInputHistoryEntity);

        log.info("开始新增外部系统接口明细补偿");
        boolean save = super.save(dmpCfgInputHistoryEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细补偿保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "外部系统接口明细补偿" , dmpCfgInputHistoryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgInputHistoryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputHistoryEntity.getId(), dmpCfgInputHistoryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputHistoryDTO.UpdateDTO updateDTO) {
        DmpCfgInputHistoryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "外部系统接口明细补偿"));
        DmpCfgInputHistoryEntity dmpCfgInputHistoryEntity =  BeanMapperUtils.map(DmpCfgInputHistoryEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputHistoryEntity);
        log.info("编辑 开始修改外部系统接口明细补偿数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputHistoryEntity);
        if(!save) {
            throw new ServiceException("外部系统接口明细补偿保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录外部系统接口明细补偿日志数据，id：【{}】", dmpCfgInputHistoryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputHistoryEntity.getId(), "外部系统接口明细补偿");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgInputHistoryEntity, null, dmpCfgInputHistoryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputHistoryEntity dmpCfgInputHistoryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
