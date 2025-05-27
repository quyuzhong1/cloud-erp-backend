package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.model.workflow.enums.ThirdProcessDefinitionTypeEnum;
import com.erp.server.workflow.mapper.ThirdProcessDefinitionMapper;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ThirdProcessDefinitionDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 三方审批定义 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ThirdProcessDefinitionServiceImpl extends SuperServiceImpl<ThirdProcessDefinitionMapper, ThirdProcessDefinitionEntity> implements ThirdProcessDefinitionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessDefinitionDTO.AddDTO addDTO) {
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity = new ThirdProcessDefinitionEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessDefinitionEntity);

        // 数据处理
        handleData(thirdProcessDefinitionEntity);

        log.info("开始新增三方审批定义");
        boolean save = super.save(thirdProcessDefinitionEntity);
        if(!save) {
            throw new ServiceException("三方审批定义保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方审批定义" , thirdProcessDefinitionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, thirdProcessDefinitionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdProcessDefinitionEntity.getId(), thirdProcessDefinitionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessDefinitionDTO.UpdateDTO addOrUpdateDTO) {
        ThirdProcessDefinitionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方审批定义"));
        ThirdProcessDefinitionEntity thirdProcessDefinitionEntity =  BeanMapperUtils.map(ThirdProcessDefinitionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thirdProcessDefinitionEntity);
        log.info("编辑 开始修改三方审批定义数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessDefinitionEntity);
        if(!save) {
            throw new ServiceException("三方审批定义保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方审批定义日志数据，id：【{}】", thirdProcessDefinitionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdProcessDefinitionEntity.getId(), "三方审批定义");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, thirdProcessDefinitionEntity, null, thirdProcessDefinitionEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<ThirdProcessDefinitionDTO.DropDownDTO> dropDown(String type) {
        //1、定时拉取获取定义状态
        //2、保存启动条件时验证定义状态
        List<ThirdProcessDefinitionEntity> thirdProcessDefinitionEntities = this.list(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode())
                .eq(ThirdProcessDefinitionEntity::getType, type).eq(ThirdProcessDefinitionEntity::getIsDeleted, false));
        //stream遍历thirdProcessDefinitionEntities 处理entity
        List<ThirdProcessDefinitionDTO.DropDownDTO> dropDownDTOS = thirdProcessDefinitionEntities.stream().map(thirdProcessDefinitionEntity -> {
            ThirdProcessDefinitionDTO.DropDownDTO dropDownDTO = new ThirdProcessDefinitionDTO.DropDownDTO();
            BeanMapperUtils.copy(thirdProcessDefinitionEntity, dropDownDTO);
            dropDownDTO.setCode(thirdProcessDefinitionEntity.getApprovalCode());
            dropDownDTO.setName(thirdProcessDefinitionEntity.getName()+thirdProcessDefinitionEntity.getDictApprovalGroup());
            return dropDownDTO;
        }).collect(Collectors.toList());
        return dropDownDTOS;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdProcessDefinitionEntity thirdProcessDefinitionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
