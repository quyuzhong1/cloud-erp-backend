package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.server.workflow.mapper.CfgProcessValueMapMapper;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
//import com.erp.server.workflow.service.OperateLogService;
import com.erp.server.workflow.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 流程设置值映射 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessValueMapServiceImpl extends SuperServiceImpl<CfgProcessValueMapMapper, CfgProcessValueMapEntity> implements CfgProcessValueMapService {
    @Autowired
//    private OperateLogService operateLogService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String fieldMapId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
        //遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        // 查询数据库中与 field_map_id 关联的记录
        List<CfgProcessValueMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessValueMapEntity>()
                        .eq(CfgProcessValueMapEntity::getFieldMapId, fieldMapId)
                        .eq(CfgProcessValueMapEntity::getIsDeleted, false)
        );
        // 提取 addDTO 中的 id
        List<String> addDTOIds = addDTO.stream()
                .map(CfgProcessValueMapDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessValueMapEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());
        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置值映射: {}", idsToDelete);
        }
        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        List<CfgProcessValueMapEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessValueMapEntity entity = new CfgProcessValueMapEntity();
                    BeanMapperUtils.copy(dto, entity);
                    entity.setFieldMapId(fieldMapId);
                    return entity;
                })
                .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            this.saveOrUpdateBatch(entities);
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置值映射" , "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, cfgProcessValueMapEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgProcessValueMapDTO.UpdateDTO addOrUpdateDTO) {
        CfgProcessValueMapEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "流程设置值映射"));
        CfgProcessValueMapEntity cfgProcessValueMapEntity =  BeanMapperUtils.map(CfgProcessValueMapEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgProcessValueMapEntity);
        log.info("编辑 开始修改流程设置值映射数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgProcessValueMapEntity);
        if(!save) {
            throw new ServiceException("流程设置值映射保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录流程设置值映射日志数据，id：【{}】", cfgProcessValueMapEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgProcessValueMapEntity.getId(), "流程设置值映射");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, cfgProcessValueMapEntity, null, cfgProcessValueMapEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgProcessValueMapDTO.ViewDTO> view(String fieldId) {
        // 查询数据库中与 fieldId 关联的记录
        List<CfgProcessValueMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessValueMapEntity>()
                        .eq(CfgProcessValueMapEntity::getFieldMapId, fieldId)
                        .eq(CfgProcessValueMapEntity::getIsDeleted, false)
        );
        List<CfgProcessValueMapDTO.ViewDTO> viewDTOList = BeanUtil.copyToList(existingEntities, CfgProcessValueMapDTO.ViewDTO.class);
        return viewDTOList;
    }

    @Override
    public void delete(List<String> mainIds) {
        // 当前用户信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessValueMapEntity> cfgProcessValueMapEntities = this.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessValueMapEntities)) {
            return;
        }
        //创建一个List<String>长度为cfgProcessValueMapEntities的长度
        List<String> ids = new ArrayList<>(cfgProcessValueMapEntities.size());
        cfgProcessValueMapEntities.forEach(item -> {item.setIsDeleted(true)
                .setUpdateTime(LocalDateTime.now())
                .setUpdateUserId(loginUser.getUid())
                .setUpdateUserName(loginUser.getUserName());
            ids.add(item.getId());
        });
        // 批量更新
        this.updateBatchById(cfgProcessValueMapEntities);
        log.info("删除流程设置执行条件: {}", ids);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgProcessValueMapEntity cfgProcessValueMapEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
