package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.server.workflow.mapper.CfgProcessValueMapMapper;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(String cfgProcessId,String fieldMapId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
        List<CfgProcessValueMapEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessValueMapEntity entity = new CfgProcessValueMapEntity();
                    BeanMapperUtils.copy(dto, entity);
                    entity.setFieldMapId(fieldMapId);
                    return entity;
                })
                .collect(Collectors.toList());
        if (entities.isEmpty()) {
            return new BaseResultDTO.AddDTO();
        }
        boolean b = this.saveOrUpdateBatch(entities);
        if(!b){
            throw new ServiceException("保存值映射失败");
        };
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置值映射");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");

        return new BaseResultDTO.AddDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String cfgProcessId,String fieldMapId,List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTO) {
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
        //分离add
        List<CfgProcessValueMapDTO.AddOrUpdateDTO> addDTOs = addDTO.stream()
                .filter(dto -> StrUtil.isEmpty(dto.getId()))
                .collect(Collectors.toList());
        add(cfgProcessId, fieldMapId, addDTOs);
        addDTO.removeAll(addDTOs);
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
        log.info("批量保存或更新流程设置值映射: {}", entities);
        Map<String, CfgProcessValueMapEntity> entityMap = entities.stream()
                .collect(Collectors.toMap(CfgProcessValueMapEntity::getId, entity -> entity));
        if (entities.isEmpty()) {
            return new BaseResultDTO.AddDTO();
        }
        //保存
        boolean b = this.saveOrUpdateBatch(entities);
        if(!b){
            throw new ServiceException("保存值映射失败");
        };
        //生成日志
        existingEntities.forEach(entity -> {
            CfgProcessValueMapEntity ruleEntity = entityMap.get(entity.getId());
            if (ObjectUtil.isNotEmpty(ruleEntity)) {
                operateLogService.addModuleOperateLogByObj(entity, ruleEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "更新操作");
            }
        });

        return new BaseResultDTO.AddDTO();
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
