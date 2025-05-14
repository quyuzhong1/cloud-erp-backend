package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.workflow.entity.CfgProcessFieldSubMapEntity;
import com.erp.server.workflow.mapper.CfgProcessFieldSubMapMapper;
import com.erp.server.workflow.service.CfgProcessFieldSubMapService;
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
import com.erp.model.workflow.dto.CfgProcessFieldSubMapDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * fieldList 明细字段映射 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-14
 */
@Slf4j
@Service
public class CfgProcessFieldSubMapServiceImpl extends SuperServiceImpl<CfgProcessFieldSubMapMapper, CfgProcessFieldSubMapEntity> implements CfgProcessFieldSubMapService {
    @Autowired
//    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String parentId,List<CfgProcessFieldSubMapDTO.AddOrUpdateDTO> addDTOList) {
        // 遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        log.info("开始新增fieldList 明细字段映射");
        // 查询数据库中 parentId 下的所有记录
        List<CfgProcessFieldSubMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessFieldSubMapEntity>()
                        .eq(CfgProcessFieldSubMapEntity::getParentId, parentId)
                        .eq(CfgProcessFieldSubMapEntity::getIsDeleted, false)
        );
        // 提取 addDTOList 中的 id
        List<String> addDTOIds = addDTOList.stream()
                .map(CfgProcessFieldSubMapDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessFieldSubMapEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());
        // 逻辑删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            List<CfgProcessFieldSubMapEntity> toDelete = existingEntities.stream()
                    .filter(e -> idsToDelete.contains(e.getId()))
                    .peek(e -> e.setIsDeleted(true))
                    .collect(Collectors.toList());
            this.updateBatchById(toDelete);
        }
        // 新增和更新
        List<CfgProcessFieldSubMapEntity> entities = addDTOList.stream()
                .map(dto -> {
                    CfgProcessFieldSubMapEntity entity = new CfgProcessFieldSubMapEntity();
                    BeanMapperUtils.copy(dto, entity);
                    entity.setParentId(parentId);
                    return entity;
                })
                .collect(Collectors.toList());
        if (!entities.isEmpty()) {
            this.saveOrUpdateBatch(entities);
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "fieldList 明细字段映射" , "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // operateLogService.addModuleOperateLog(msg, null, cfgProcessFieldSubMapEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return new BaseResultDTO.AddDTO();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgProcessFieldSubMapEntity cfgProcessFieldSubMapEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
