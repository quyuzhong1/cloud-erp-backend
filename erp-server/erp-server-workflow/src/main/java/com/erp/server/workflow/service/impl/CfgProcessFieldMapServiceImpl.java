package com.erp.server.workflow.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.vo.LoginUser;
import com.erp.model.workflow.dto.CfgProcessFieldSubMapDTO;
import com.erp.model.workflow.dto.CfgProcessValueMapDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.DictCfgSysFieldEntity;
import com.erp.model.workflow.enums.DictCfgSysFieldFieldTypeEnum;
import com.erp.server.workflow.mapper.CfgProcessFieldMapMapper;
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
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 流程设置字段配置 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@Service
public class CfgProcessFieldMapServiceImpl extends SuperServiceImpl<CfgProcessFieldMapMapper, CfgProcessFieldMapEntity> implements CfgProcessFieldMapService {
    @Autowired
//    private OperateLogService operateLogService;

    @Resource
    private CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    private DictCfgSysFieldService dictCfgSysFieldService;
    @Autowired
    private CfgProcessFieldSubMapService cfgProcessFieldSubMapService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(String bussinessKey, String ruleId, List<CfgProcessFieldMapDTO.AddOrUpdateDTO> addDTO) {
        // 遍历addDTO，id为空的保存，id不为空的更新，使用ruleId查询ruleId的记录，如果查询的结果数小于addDTO数量，那么找出结果中未包含于addDTO的中的id，然后将此id对应的entiy删除
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessFieldMapEntity> existingEntities = this.list(
                new LambdaQueryWrapper<CfgProcessFieldMapEntity>()
                        .eq(CfgProcessFieldMapEntity::getCfgId, ruleId)
                        .eq(CfgProcessFieldMapEntity::getIsDeleted, false)
        );

        // 提取 addDTO 中的 id
        List<String> addDTOIds = addDTO.stream()
                .map(CfgProcessFieldMapDTO.AddOrUpdateDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 找出 existingEntities 中未包含于 addDTOIds 的记录
        List<String> idsToDelete = existingEntities.stream()
                .map(CfgProcessFieldMapEntity::getId)
                .filter(id -> !addDTOIds.contains(id))
                .collect(Collectors.toList());

        // 删除未包含的记录
        if (!idsToDelete.isEmpty()) {
            this.removeByIds(idsToDelete);
            log.info("删除流程设置字段配置: {}", idsToDelete);
        }

        //
        List<String> fieldList = dictCfgSysFieldService.list(new LambdaQueryWrapper<DictCfgSysFieldEntity>().eq(DictCfgSysFieldEntity::getBussinessKey, bussinessKey).eq(DictCfgSysFieldEntity::getIsDeleted, false)).stream().map(DictCfgSysFieldEntity::getField).collect(Collectors.toList());
        // 遍历 addDTO，id 为空的保存，id 不为空的更新
        // 先校验所有 DTO，收集需要新增和更新的实体
        List<CfgProcessFieldMapEntity> entitiesToAddOrUpdate = new ArrayList<>();

        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            // 校验 dto 的 third_field_type 和 sys_field_type
            DictCfgSysFieldFieldTypeEnum thirdFieldType = DictCfgSysFieldFieldTypeEnum.valueOf(dto.getThirdFieldType().toUpperCase());
            DictCfgSysFieldFieldTypeEnum sysFieldType = DictCfgSysFieldFieldTypeEnum.valueOf(dto.getSysFieldType().toUpperCase());

            if ((thirdFieldType == DictCfgSysFieldFieldTypeEnum.INPUT || thirdFieldType == DictCfgSysFieldFieldTypeEnum.TEXTAREA)&&
                    (sysFieldType == DictCfgSysFieldFieldTypeEnum.NUMBER || sysFieldType == DictCfgSysFieldFieldTypeEnum.ATTACHMENTV2)) {
                throw new ServiceException("飞书文本不可生成数值，附件类型");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.NUMBER && sysFieldType == DictCfgSysFieldFieldTypeEnum.ATTACHMENTV2) {
                throw new ServiceException("飞书数值不可生成附件");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.ATTACHMENTV2 && sysFieldType != DictCfgSysFieldFieldTypeEnum.ATTACHMENTV2) {
                throw new ServiceException("飞书附件仅支持生成附件");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.RADIOV2 &&
                    (sysFieldType == DictCfgSysFieldFieldTypeEnum.NUMBER || sysFieldType == DictCfgSysFieldFieldTypeEnum.ATTACHMENTV2)) {
                throw new ServiceException("飞书单选项不可生成数值，附件");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.CHECKBOXV2 && sysFieldType != DictCfgSysFieldFieldTypeEnum.CHECKBOXV2) {
                throw new ServiceException("飞书多选项仅可支持生成多选项");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.DATETIME && sysFieldType != DictCfgSysFieldFieldTypeEnum.DATETIME) {
                throw new ServiceException("飞书日期仅支持转日期");
            }
            if (thirdFieldType == DictCfgSysFieldFieldTypeEnum.FIELDLIST && sysFieldType != DictCfgSysFieldFieldTypeEnum.FIELDLIST) {
                throw new ServiceException("明细只能对应明细");
            }
            // 判断 dto 的 field 是否存在于 fieldList，是则从 fieldList 中去除
            if (!fieldList.contains(dto.getSysField())) {
                throw new ServiceException("存在{}尚未映射，无法提交保存",dto.getSysField());
            }
            // 校验通过后，进行保存或更新操作
            dto.setId(IdWorker.getIdStr());
            CfgProcessFieldMapEntity entity = new CfgProcessFieldMapEntity();
            BeanMapperUtils.copy(dto, entity);
            entity.setCfgId(ruleId); // 设置关联的 ruleId
            entitiesToAddOrUpdate.add(entity);
        }
        // 批量插入和更新
        this.saveOrUpdateBatch(entitiesToAddOrUpdate);
        //
        for (CfgProcessFieldMapDTO.AddOrUpdateDTO dto : addDTO) {
            String id = dto.getId();
            List<CfgProcessValueMapDTO.AddOrUpdateDTO> processValueMapDTOList = dto.getProcessValueMapDTOList();
            List<CfgProcessFieldSubMapDTO.AddOrUpdateDTO> processFieldSubMapDTOList = dto.getProcessFieldSubMapDTOList();
            if (ObjectUtil.isNotEmpty(processValueMapDTOList)) {
                cfgProcessValueMapService.addOrUpdate(id, processValueMapDTOList);
            }
            if (ObjectUtil.isNotEmpty(processFieldSubMapDTOList)){
                cfgProcessFieldSubMapService.addOrUpdate(id, processFieldSubMapDTOList);
            }
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "流程设置字段配置", "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // operateLogService.addModuleOperateLog(msg, null, cfgProcessFieldMapEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgProcessFieldMapDTO.UpdateDTO addOrUpdateDTO) {
        CfgProcessFieldMapEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "流程设置字段配置"));
        CfgProcessFieldMapEntity cfgProcessFieldMapEntity = BeanMapperUtils.map(CfgProcessFieldMapEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgProcessFieldMapEntity);
        log.info("编辑 开始修改流程设置字段配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgProcessFieldMapEntity);
        if (!save) {
            throw new ServiceException("流程设置字段配置保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录流程设置字段配置日志数据，id：【{}】", cfgProcessFieldMapEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgProcessFieldMapEntity.getId(), "流程设置字段配置");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, cfgProcessFieldMapEntity, null, cfgProcessFieldMapEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<CfgProcessFieldMapDTO.ViewDTO> view(String ruleId) {
        // 查询数据库中与 ruleId 关联的记录
        List<CfgProcessFieldMapDTO.ViewDTO> viewDTOList = baseMapper.getFieldWithSub(ruleId);
        return viewDTOList;
    }

    @Override
    @Transactional
    public void delete(List<String> mainIds) {
        // 当前用户信息
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        // 符合 ruleId 存在于 ids 的更新
        List<CfgProcessFieldMapEntity> cfgProcessFieldMapEntityList = this.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().in(CfgProcessFieldMapEntity::getCfgId, mainIds));
        if (CollectionUtils.isEmpty(cfgProcessFieldMapEntityList)) {
            return;
        }
        List<String> ids = new ArrayList<>(cfgProcessFieldMapEntityList.size());
        cfgProcessFieldMapEntityList.forEach(item -> {
            item.setIsDeleted(true)
                    .setUpdateTime(LocalDateTime.now())
                    .setUpdateUserId(loginUser.getUid())
                    .setUpdateUserName(loginUser.getUserName());
            ids.add(item.getId());
        });
        // 批量更新
        this.updateBatchById(cfgProcessFieldMapEntityList);
        log.info("删除流程设置执行条件: {}", ids);
        //删除选项条件设置
        cfgProcessValueMapService.delete(cfgProcessFieldMapEntityList.stream().map(CfgProcessFieldMapEntity::getId).collect(Collectors.toList()));
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessFieldMapEntity cfgProcessFieldMapEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
