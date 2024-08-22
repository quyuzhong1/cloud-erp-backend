package com.erp.server.dmp.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertValueEntity;
import com.erp.server.dmp.mapper.DmpCfgInputConvertValueMapper;
import com.erp.server.dmp.service.DmpCfgInputConvertValueService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgInputConvertValueDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-08
 */
@Slf4j
@Service
public class DmpCfgInputConvertValueServiceImpl extends SuperServiceImpl<DmpCfgInputConvertValueMapper, DmpCfgInputConvertValueEntity> implements DmpCfgInputConvertValueService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputConvertValueDTO.AddDTO addDTO) {
        DmpCfgInputConvertValueEntity dmpCfgInputConvertValueEntity = new DmpCfgInputConvertValueEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputConvertValueEntity);

        // 数据处理
        handleData(dmpCfgInputConvertValueEntity);

        log.info("开始新增");
        boolean save = super.save(dmpCfgInputConvertValueEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , dmpCfgInputConvertValueEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgInputConvertValueEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputConvertValueEntity.getId(), dmpCfgInputConvertValueEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputConvertValueDTO.UpdateDTO updateDTO) {
        DmpCfgInputConvertValueEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DmpCfgInputConvertValueEntity dmpCfgInputConvertValueEntity =  BeanMapperUtils.map(DmpCfgInputConvertValueEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputConvertValueEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputConvertValueEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", dmpCfgInputConvertValueEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputConvertValueEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgInputConvertValueEntity, null, dmpCfgInputConvertValueEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValue() {
        return baseMapper.listMappingAndValue();
    }

    @Override
    public List<DmpCfgInputConvertValueDTO.MappingAndValueDTO> listMappingAndValueByFreshCacheTime(DateTime freshCacheTime) {
        return baseMapper.listMappingAndValueByFreshCacheTime(freshCacheTime);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputConvertValueEntity dmpCfgInputConvertValueEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
