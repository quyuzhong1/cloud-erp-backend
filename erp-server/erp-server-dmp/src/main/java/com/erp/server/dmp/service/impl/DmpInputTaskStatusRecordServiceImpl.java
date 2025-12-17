package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpInputTaskStatusRecordDTO;
import com.erp.model.dmp.entity.DmpInputTaskStatusRecordEntity;
import com.erp.server.dmp.mapper.DmpInputTaskStatusRecordMapper;
import com.erp.server.dmp.service.DmpInputTaskStatusRecordService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 拉取任务状态记录 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpInputTaskStatusRecordServiceImpl extends SuperServiceImpl<DmpInputTaskStatusRecordMapper, DmpInputTaskStatusRecordEntity> implements DmpInputTaskStatusRecordService {
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpInputTaskStatusRecordDTO.AddDTO addDTO) {
        DmpInputTaskStatusRecordEntity dmpInputTaskStatusrecordEntity = new DmpInputTaskStatusRecordEntity();
        BeanMapperUtils.copy(addDTO, dmpInputTaskStatusrecordEntity);

        // 数据处理
        handleData(dmpInputTaskStatusrecordEntity);

        log.info("开始新增拉取任务状态记录");
        boolean save = super.save(dmpInputTaskStatusrecordEntity);
        if(!save) {
            throw new ServiceException("拉取任务状态记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "拉取任务状态记录" , dmpInputTaskStatusrecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpInputTaskStatusrecordEntity.getId(), dmpInputTaskStatusrecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpInputTaskStatusRecordDTO.UpdateDTO updateDTO) {
        DmpInputTaskStatusRecordEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "拉取任务状态记录"));
        DmpInputTaskStatusRecordEntity dmpInputTaskStatusrecordEntity =  BeanMapperUtils.map(DmpInputTaskStatusRecordEntity.class, updateDTO);

        // 数据处理
        handleData(dmpInputTaskStatusrecordEntity);
        log.info("编辑 开始修改拉取任务状态记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpInputTaskStatusrecordEntity);
        if(!save) {
            throw new ServiceException("拉取任务状态记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录拉取任务状态记录日志数据，id：【{}】", dmpInputTaskStatusrecordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpInputTaskStatusrecordEntity.getId(), "拉取任务状态记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpInputTaskStatusRecordEntity dmpInputTaskStatusrecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
