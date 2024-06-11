package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgDbEntity;
import com.erp.server.dmp.mapper.DmpCfgDbMapper;
import com.erp.server.dmp.service.DmpCfgDbService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgDbDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 输入输出db信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgDbServiceImpl extends SuperServiceImpl<DmpCfgDbMapper, DmpCfgDbEntity> implements DmpCfgDbService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgDbDTO.AddDTO addDTO) {
        DmpCfgDbEntity dmpCfgDbEntity = new DmpCfgDbEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgDbEntity);

        // 数据处理
        handleData(dmpCfgDbEntity);

        log.info("开始新增输入输出db信息");
        boolean save = super.save(dmpCfgDbEntity);
        if(!save) {
            throw new ServiceException("输入输出db信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "输入输出db信息" , dmpCfgDbEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgDbEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgDbEntity.getId(), dmpCfgDbEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgDbDTO.UpdateDTO updateDTO) {
        DmpCfgDbEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "输入输出db信息"));
        DmpCfgDbEntity dmpCfgDbEntity =  BeanMapperUtils.map(DmpCfgDbEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgDbEntity);
        log.info("编辑 开始修改输入输出db信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgDbEntity);
        if(!save) {
            throw new ServiceException("输入输出db信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录输入输出db信息日志数据，id：【{}】", dmpCfgDbEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgDbEntity.getId(), "输入输出db信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgDbEntity, null, dmpCfgDbEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgDbEntity dmpCfgDbEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
