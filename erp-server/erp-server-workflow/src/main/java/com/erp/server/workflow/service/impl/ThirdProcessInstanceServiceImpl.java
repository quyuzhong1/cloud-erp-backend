package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.ThirdProcessInstanceEntity;
import com.erp.server.workflow.mapper.ThirdProcessInstanceMapper;
import com.erp.server.workflow.service.ThirdProcessInstanceService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.ThirdProcessInstanceDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 三方流程实例清单 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class ThirdProcessInstanceServiceImpl extends SuperServiceImpl<ThirdProcessInstanceMapper, ThirdProcessInstanceEntity> implements ThirdProcessInstanceService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdProcessInstanceDTO.AddDTO addDTO) {
        ThirdProcessInstanceEntity thirdProcessInstanceEntity = new ThirdProcessInstanceEntity();
        BeanMapperUtils.copy(addDTO, thirdProcessInstanceEntity);

        // 数据处理
        handleData(thirdProcessInstanceEntity);

        log.info("开始新增三方流程实例清单");
        boolean save = super.save(thirdProcessInstanceEntity);
        if(!save) {
            throw new ServiceException("三方流程实例清单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方流程实例清单" , thirdProcessInstanceEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, thirdProcessInstanceEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdProcessInstanceEntity.getId(), thirdProcessInstanceEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdProcessInstanceDTO.UpdateDTO addOrUpdateDTO) {
        ThirdProcessInstanceEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方流程实例清单"));
        ThirdProcessInstanceEntity thirdProcessInstanceEntity =  BeanMapperUtils.map(ThirdProcessInstanceEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(thirdProcessInstanceEntity);
        log.info("编辑 开始修改三方流程实例清单数据，id：【{}】", old.getId());
        boolean save = super.updateById(thirdProcessInstanceEntity);
        if(!save) {
            throw new ServiceException("三方流程实例清单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方流程实例清单日志数据，id：【{}】", thirdProcessInstanceEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdProcessInstanceEntity.getId(), "三方流程实例清单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, thirdProcessInstanceEntity, null, thirdProcessInstanceEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdProcessInstanceEntity thirdProcessInstanceEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
