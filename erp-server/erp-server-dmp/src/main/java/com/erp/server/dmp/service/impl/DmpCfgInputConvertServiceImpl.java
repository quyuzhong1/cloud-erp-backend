package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.server.dmp.mapper.DmpCfgInputConvertMapper;
import com.erp.server.dmp.service.DmpCfgInputConvertService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgInputConvertDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 外部系统接口转换内部数据 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgInputConvertServiceImpl extends SuperServiceImpl<DmpCfgInputConvertMapper, DmpCfgInputConvertEntity> implements DmpCfgInputConvertService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgInputConvertDTO.AddDTO addDTO) {
        DmpCfgInputConvertEntity dmpCfgInputConvertEntity = new DmpCfgInputConvertEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgInputConvertEntity);

        // 数据处理
        handleData(dmpCfgInputConvertEntity);

        log.info("开始新增外部系统接口转换内部数据");
        boolean save = super.save(dmpCfgInputConvertEntity);
        if(!save) {
            throw new ServiceException("外部系统接口转换内部数据保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "外部系统接口转换内部数据" , dmpCfgInputConvertEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgInputConvertEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgInputConvertEntity.getId(), dmpCfgInputConvertEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgInputConvertDTO.UpdateDTO updateDTO) {
        DmpCfgInputConvertEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "外部系统接口转换内部数据"));
        DmpCfgInputConvertEntity dmpCfgInputConvertEntity =  BeanMapperUtils.map(DmpCfgInputConvertEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgInputConvertEntity);
        log.info("编辑 开始修改外部系统接口转换内部数据数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgInputConvertEntity);
        if(!save) {
            throw new ServiceException("外部系统接口转换内部数据保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录外部系统接口转换内部数据日志数据，id：【{}】", dmpCfgInputConvertEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgInputConvertEntity.getId(), "外部系统接口转换内部数据");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgInputConvertEntity, null, dmpCfgInputConvertEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgInputConvertEntity dmpCfgInputConvertEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
