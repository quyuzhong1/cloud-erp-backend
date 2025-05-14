package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.workflow.dto.CfgProcessExpDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.model.workflow.enums.CfgProcessBussinessKeyEnum;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.server.workflow.mapper.CfgProcessRuleMapper;
import com.erp.server.workflow.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
//import com.erp.server.workflow.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgProcessRuleDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

import static com.common.core.controller.vo.ApiResult.success;


/**
 * <p>
 * 流程设置执行条件 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-13
 */
@Slf4j
@Service
public class CfgProcessRuleServiceImpl extends SuperServiceImpl<CfgProcessRuleMapper, CfgProcessRuleEntity> implements CfgProcessRuleService {
    //    @Autowired
    //private OperateLogService operateLogService;
    @Resource
    CfgProcessExpService cfgProcessExpService;
    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String bussinessKey,String cfgProcessId, List<CfgProcessRuleDTO.AddOrUpdateDTO> addDTO) {
        // 查询数据库中与 mainId 关联的记录
        List<CfgProcessRuleEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessRuleEntity>()
                        .eq(CfgProcessRuleEntity::getCfgProcessId, cfgProcessId)
                        .eq(CfgProcessRuleEntity::getIsDeleted, false)
        );

        // 提取 addDTO 中的 id
        Set<String> addDTOIds = addDTO.stream()
                .map(CfgProcessRuleDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessRuleEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());

        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置执行条件: {}", idsToDelete);
            delete(idsToDelete);
        }

        // 校验 type=sysProcess 的数量是否大于1
        long sysProcessCount = addDTO.stream()
                .filter(dto -> CfgProcessRuleTypeEnum.ERPPROGRESS.getCode().equals(dto.getType()))
                .count();
        if (sysProcessCount > 1) {
            throw new ServiceException("已配置流程，不可重复配置");
        }

        // 校验 type=fsProcess 的 processFieldMapDTOList 是否为空
        addDTO.stream()
                .filter(dto -> CfgProcessRuleTypeEnum.FSPROGRESS.getCode().equals(dto.getType()))
                .forEach(dto -> {
                    if (CollectionUtils.isEmpty(dto.getProcessFieldMapDTOList())) {
                        throw new ServiceException("字段配置必须填写");
                    }
                });

        // 转换DTO为Entity
        List<CfgProcessRuleEntity> entities = addDTO.stream()
                .map(dto -> {
                    CfgProcessRuleEntity entity = new CfgProcessRuleEntity();
                    if (StrUtil.isNotEmpty(dto.getId())){
                        dto.setId(IdWorker.getIdStr());
                    }
                    BeanMapperUtils.copy(dto, entity);
                    entity.setCfgProcessId(cfgProcessId);
                    return entity;
                }).collect(Collectors.toList());

        // 批量保存或更新
        if (!entities.isEmpty()) {
            log.info("批量保存或更新流程设置执行条件");
            boolean result = super.saveOrUpdateBatch(entities);
            if (!result) {
                throw new ServiceException("流程设置执行条件批量保存或更新失败");
            }
        }

        // 组装审核条件和字段配置的Map
        for (CfgProcessRuleDTO.AddOrUpdateDTO dto : addDTO) {
            String ruleId = dto.getId();
            List<CfgProcessExpDTO.AddOrUpdateDTO> processExpDTOList = dto.getProcessExpDTOList() != null
                    ? dto.getProcessExpDTOList() : Collections.emptyList();
            if (!processExpDTOList.isEmpty()) {
                cfgProcessExpService.addOrUpdate(ruleId, processExpDTOList);
            }
            List<CfgProcessFieldMapDTO.AddOrUpdateDTO> processFieldMapDTOList = dto.getProcessFieldMapDTOList() != null
                    ? dto.getProcessFieldMapDTOList() : Collections.emptyList();
            if (!processFieldMapDTOList.isEmpty()) {
                cfgProcessFieldMapService.addOrUpdate(bussinessKey,ruleId, processFieldMapDTOList);
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置执行条件", "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // operateLogService.addModuleOperateLog(msg, null, cfgProcessRuleEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return new BaseResultDTO.AddDTO();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids) {
        //删除执行条件
        removeByIds(ids);
        cfgProcessExpService.delete(ids);
        cfgProcessFieldMapService.delete(ids);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessRuleEntity cfgProcessRuleEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
