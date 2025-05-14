package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.erp.server.workflow.mapper.CfgProcessExpMapper;
import com.erp.server.workflow.service.CfgProcessExpService;
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
import com.erp.model.workflow.dto.CfgProcessExpDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

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
//    @Autowired
//    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String ruleId, List<CfgProcessExpDTO.AddOrUpdateDTO> addDTO) {
        //遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessExpEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessExpEntity>()
                        .eq(CfgProcessExpEntity::getRuleId, ruleId)
                        .eq(CfgProcessExpEntity::getIsDeleted, false)
        );

        // 提取 addDTO 中的 id
        List<String> addDTOIds = addDTO.stream()
                .map(CfgProcessExpDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessExpEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());

        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置审核条件: {}", idsToDelete);
        }

        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        for (CfgProcessExpDTO.AddOrUpdateDTO dto : addDTO) {
            CfgProcessExpEntity entity = new CfgProcessExpEntity();
            BeanMapperUtils.copy(dto, entity);
            entity.setRuleId(ruleId); // 设置关联的 ruleId

            if (StrUtil.isEmpty(dto.getId())) {
                // id 为空，新增
                this.save(entity);
            } else {
                // id 不为空，更新
                this.updateById(entity);
            }
        }

        log.info("开始新增流程设置审核条件");

        // 操作日志
//        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置审核条件", cfgProcessExpEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, cfgProcessExpEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgProcessExpDTO.AddOrUpdateDTO addOrUpdateDTO) {
        CfgProcessExpEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "流程设置审核条件"));
        CfgProcessExpEntity cfgProcessExpEntity = BeanMapperUtils.map(CfgProcessExpEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgProcessExpEntity);
        log.info("编辑 开始修改流程设置审核条件数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgProcessExpEntity);
        if (!save) {
            throw new ServiceException("流程设置审核条件保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录流程设置审核条件日志数据，id：【{}】", cfgProcessExpEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgProcessExpEntity.getId(), "流程设置审核条件");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, cfgProcessExpEntity, null, cfgProcessExpEntity.getId(), msg);
        return Boolean.TRUE;
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
