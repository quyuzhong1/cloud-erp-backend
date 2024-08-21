package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.erp.server.tms.mapper.FirstMileWeightAllocationMapper;
import com.erp.server.tms.service.FirstMileWeightAllocationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 头程重量分摊 服务实现类
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
@Slf4j
@Service
public class FirstMileWeightAllocationServiceImpl extends SuperServiceImpl<FirstMileWeightAllocationMapper, FirstMileWeightAllocationEntity> implements FirstMileWeightAllocationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FirstMileWeightAllocationDTO.AddDTO addDTO) {
        FirstMileWeightAllocationEntity firstMileWeightAllocationEntity = new FirstMileWeightAllocationEntity();
        BeanMapperUtils.copy(addDTO, firstMileWeightAllocationEntity);

        // 数据处理
        handleData(firstMileWeightAllocationEntity);

        log.info("开始新增头程重量分摊");
        boolean save = super.save(firstMileWeightAllocationEntity);
        if(!save) {
            throw new ServiceException("头程重量分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "头程重量分摊" , firstMileWeightAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, firstMileWeightAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(firstMileWeightAllocationEntity.getId(), firstMileWeightAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FirstMileWeightAllocationDTO.UpdateDTO updateDTO) {
        FirstMileWeightAllocationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "头程重量分摊"));
        FirstMileWeightAllocationEntity firstMileWeightAllocationEntity =  BeanMapperUtils.map(FirstMileWeightAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(firstMileWeightAllocationEntity);
        log.info("编辑 开始修改头程重量分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(firstMileWeightAllocationEntity);
        if(!save) {
            throw new ServiceException("头程重量分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录头程重量分摊日志数据，id：【{}】", firstMileWeightAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), firstMileWeightAllocationEntity.getId(), "头程重量分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, firstMileWeightAllocationEntity, null, firstMileWeightAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FirstMileWeightAllocationEntity firstMileWeightAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
