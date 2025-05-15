package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.server.workflow.mapper.CfgProcessExpMapper;
import com.erp.server.workflow.service.CfgProcessExpService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessExpDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;

/**
 * <p>
 * 流程设置审核条件 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessExpServiceImpl extends SuperServiceImpl<CfgProcessExpMapper, CfgProcessExpEntity> implements CfgProcessExpService {
    @Autowired
    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add( String cfgProcessId, String ruleId,List<CfgProcessExpDTO.AddOrUpdateDTO> addDTO) {
        log.info("开始新增流程设置审核条件");
        // 遍历 addDTO
        List<CfgProcessExpEntity> processExpEntities = BeanUtil.copyToList(addDTO, CfgProcessExpEntity.class);
        this.saveBatch(processExpEntities);
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置审核条件", "");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");
        return new BaseResultDTO.AddDTO();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO  addOrUpdate( String cfgProcessId,String ruleId,List<CfgProcessExpDTO.AddOrUpdateDTO> addOrUpdateDTO) {
        //遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessExpEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessExpEntity>()
                        .eq(CfgProcessExpEntity::getRuleId, ruleId)
                        .eq(CfgProcessExpEntity::getIsDeleted, false)
        );

        // 提取 addDTO 中的 id
        List<String> addDTOIds = addOrUpdateDTO.stream()
                .map(CfgProcessExpDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessExpEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());

        //处理更新中的add
        List<CfgProcessExpDTO.AddOrUpdateDTO> addDTOS = addOrUpdateDTO.stream()
                .filter(dto -> StrUtil.isEmpty(dto.getId()))
                .collect(Collectors.toList());
        add(cfgProcessId, ruleId,addDTOS);
        addOrUpdateDTO.removeAll(addDTOS);

        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置审核条件: {}", idsToDelete);
        }

        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        List<CfgProcessExpEntity> updateEntitys = addOrUpdateDTO.stream().map(item -> {
            CfgProcessExpEntity cfgProcessExpEntity = new CfgProcessExpEntity();
            BeanUtil.copyProperties(item, cfgProcessExpEntity);
            cfgProcessExpEntity.setRuleId(ruleId);
            return cfgProcessExpEntity;
        }).collect(Collectors.toList());
        log.info("开始新增流程设置审核条件");
        this.saveBatch(updateEntitys);

        Map<String, CfgProcessExpEntity> expEntityMap = updateEntitys.stream()
                .collect(Collectors.toMap(CfgProcessExpEntity::getId, entity -> entity));
        // 操作日志
        existingEntities.forEach(existingEntity -> {
            CfgProcessExpEntity updateEntity = expEntityMap.get(existingEntity.getId());
            if (ObjectUtil.isNotEmpty(updateEntity)) {
                operateLogService.addModuleOperateLogByObj(existingEntities, updateEntitys, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "新增操作");
            }
        });

        return new BaseResultDTO.UpdateDTO();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<String> mainIds) {
        // 当前用户信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessExpEntity> cfgProcessExpEntityList = this.list(new LambdaQueryWrapper<CfgProcessExpEntity>().in(CfgProcessExpEntity::getRuleId, mainIds));
        if (CollUtil.isEmpty(cfgProcessExpEntityList)) {
            return;
        }
        ArrayList<String> ids = new ArrayList<>(cfgProcessExpEntityList.size());
        cfgProcessExpEntityList.forEach(item -> {
            item.setIsDeleted(true)
                    .setUpdateTime(LocalDateTime.now())
                    .setUpdateUserId(loginUser.getUid())
                    .setUpdateUserName(loginUser.getUserName());
            ids.add(item.getId());
        });
        // 批量更新
        this.updateBatchById(cfgProcessExpEntityList);
        log.info("删除流程设置执行条件: {}", ids);
    }

    /**
     * TODO view接口未处理
     *
     * @param ruleId
     * @return
     */
    @Override
    public List<CfgProcessExpDTO.ViewDTO> view(String ruleId) {
        List<CfgProcessExpEntity> processExpEntityList = this.list(new LambdaQueryWrapper<CfgProcessExpEntity>().eq(CfgProcessExpEntity::getRuleId, ruleId).eq(CfgProcessExpEntity::getIsDeleted, false));
        return processExpEntityList.stream().map(item -> {
            CfgProcessExpDTO.ViewDTO viewDTO = new CfgProcessExpDTO.ViewDTO();
            BeanMapperUtils.copy(item, viewDTO);
            return viewDTO;
        }).collect(Collectors.toList());
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessExpEntity cfgProcessExpEntity) {
        // TODO 验证数据 & 数据赋值
    }

}
