package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.mapper.DmpInputTaskMapper;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpInputTaskDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 拉取任务 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpInputTaskServiceImpl extends SuperServiceImpl<DmpInputTaskMapper, DmpInputTaskEntity> implements DmpInputTaskService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputTaskDTO.AddDTO addDTO) {
        DmpInputTaskEntity dmpInputTaskEntity = new DmpInputTaskEntity();
        BeanMapperUtils.copy(addDTO, dmpInputTaskEntity);

        // 数据处理
        handleData(dmpInputTaskEntity);

        log.info("开始新增拉取任务");
        boolean save = super.save(dmpInputTaskEntity);
        if(!save) {
            throw new ServiceException("拉取任务保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取任务" , dmpInputTaskEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpInputTaskEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputTaskEntity.getId(), dmpInputTaskEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputTaskDTO.UpdateDTO updateDTO) {
        DmpInputTaskEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取任务"));
        DmpInputTaskEntity dmpInputTaskEntity =  BeanMapperUtils.map(DmpInputTaskEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputTaskEntity);
        log.info("编辑 开始修改拉取任务数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputTaskEntity);
        if(!save) {
            throw new ServiceException("拉取任务保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拉取任务日志数据，id：【{}】", dmpInputTaskEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputTaskEntity.getId(), "拉取任务");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpInputTaskEntity, null, dmpInputTaskEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputTaskEntity dmpInputTaskEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
