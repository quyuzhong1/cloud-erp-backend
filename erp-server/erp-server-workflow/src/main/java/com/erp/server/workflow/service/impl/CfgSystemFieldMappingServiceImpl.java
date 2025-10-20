package com.erp.server.workflow.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.entity.OmsPushMsgEntity;
import com.erp.model.workflow.dto.CfgSystemFieldMappingDTO;
import com.erp.model.workflow.entity.CfgSystemFieldMappingEntity;
import com.erp.server.workflow.mapper.CfgSystemFieldMappingMapper;
import com.erp.server.workflow.service.CfgSystemFieldMappingService;
import com.erp.server.workflow.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 * 远程查询配置 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-10-17
 */
@Slf4j
@Service
public class CfgSystemFieldMappingServiceImpl extends SuperServiceImpl<CfgSystemFieldMappingMapper, CfgSystemFieldMappingEntity> implements CfgSystemFieldMappingService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSystemFieldMappingDTO.AddDTO addDTO) {
        CfgSystemFieldMappingEntity cfgSystemFieldMappingEntity = new CfgSystemFieldMappingEntity();
        BeanMapperUtils.copy(addDTO, cfgSystemFieldMappingEntity);

        // 数据处理
        handleData(cfgSystemFieldMappingEntity);

        log.info("开始新增远程查询配置");
        boolean save = super.save(cfgSystemFieldMappingEntity);
        if(!save) {
            throw new ServiceException("远程查询配置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "远程查询配置" , cfgSystemFieldMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgSystemFieldMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgSystemFieldMappingEntity.getId(), cfgSystemFieldMappingEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSystemFieldMappingDTO.UpdateDTO addOrUpdateDTO) {
        CfgSystemFieldMappingEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "远程查询配置"));
        CfgSystemFieldMappingEntity cfgSystemFieldMappingEntity =  BeanMapperUtils.map(CfgSystemFieldMappingEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgSystemFieldMappingEntity);
        log.info("编辑 开始修改远程查询配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgSystemFieldMappingEntity);
        if(!save) {
            throw new ServiceException("远程查询配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录远程查询配置日志数据，id：【{}】", cfgSystemFieldMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgSystemFieldMappingEntity.getId(), "远程查询配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgSystemFieldMappingEntity, null, cfgSystemFieldMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgSystemFieldMappingEntity> listSystemFieldMapping(List<CfgSystemFieldMappingDTO.FieldMappingParamDTO> paramList) {
        return baseMapper.listSystemFieldMapping(paramList);
    }

    @Override
    public List<Object> listFeignQueryData(CfgSystemFieldMappingEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getFeignPath()) || CharSequenceUtil.isBlank(entity.getFeignMethod())) {
            return Collections.emptyList();
        }
        List<JSONObject> jsonObjectList = null;
        try {
             jsonObjectList = FeignQuery.invokeList(JSONObject.class, entity.getFeignPath(), entity.getFeignMethod(),
                    Collections.singletonList(entity.getFeignParam()));
        } catch (Exception e) {
            throw new ServiceException("远程查询失败,feignPath:{},feignMethod:{},msg:{}"+entity.getFeignPath() + entity.getFeignMethod() + e.getMessage());
        }




        return ;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgSystemFieldMappingEntity cfgSystemFieldMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
