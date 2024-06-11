package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.server.dmp.mapper.DmpCfgOutputMapper;
import com.erp.server.dmp.service.DmpCfgOutputService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 推送数据配置 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpCfgOutputServiceImpl extends SuperServiceImpl<DmpCfgOutputMapper, DmpCfgOutputEntity> implements DmpCfgOutputService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpCfgOutputDTO.AddDTO addDTO) {
        DmpCfgOutputEntity dmpCfgOutputEntity = new DmpCfgOutputEntity();
        BeanMapperUtils.copy(addDTO, dmpCfgOutputEntity);

        // 数据处理
        handleData(dmpCfgOutputEntity);

        log.info("开始新增推送数据配置");
        boolean save = super.save(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送数据配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送数据配置" , dmpCfgOutputEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpCfgOutputEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpCfgOutputEntity.getId(), dmpCfgOutputEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpCfgOutputDTO.UpdateDTO updateDTO) {
        DmpCfgOutputEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送数据配置"));
        DmpCfgOutputEntity dmpCfgOutputEntity =  BeanMapperUtils.map(DmpCfgOutputEntity.class, updateDTO);

        // 数据处理
        handleData(dmpCfgOutputEntity);
        log.info("编辑 开始修改推送数据配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpCfgOutputEntity);
        if(!save) {
            throw new ServiceException("推送数据配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送数据配置日志数据，id：【{}】", dmpCfgOutputEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpCfgOutputEntity.getId(), "推送数据配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpCfgOutputEntity, null, dmpCfgOutputEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpCfgOutputEntity dmpCfgOutputEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
