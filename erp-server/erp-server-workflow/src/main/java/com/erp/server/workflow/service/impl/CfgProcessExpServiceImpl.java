package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.CfgProcessExpDTO;
import com.erp.model.workflow.entity.CfgProcessExpEntity;
import com.erp.server.workflow.mapper.CfgProcessExpMapper;
import com.erp.server.workflow.service.CfgProcessExpService;
import com.erp.server.workflow.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<CfgProcessExpEntity> processExpEntities = BeanUtil.copyToList(addDTO, CfgProcessExpEntity.class);
        //数据处理
        handleAddData(processExpEntities, ruleId);

        this.saveBatch(processExpEntities);
        // 操作日志
        String msg = StrUtil.format("新增流程设置审核条件");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId,msg);
        return new BaseResultDTO.AddDTO();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.UpdateDTO  addOrUpdate( String cfgProcessId,String ruleId,List<CfgProcessExpDTO.AddOrUpdateDTO> addOrUpdateList) {
        //查询历史数据
        List<CfgProcessExpEntity> oldList = listByRuleIdList(Collections.singletonList(ruleId));
        List<CfgProcessExpEntity> list = CollUtil.isEmpty(addOrUpdateList) ? Collections.emptyList() : BeanMapperUtils.copyList(CfgProcessExpEntity.class, addOrUpdateList);
        //删除字段条件为空的数据
        List<CfgProcessExpEntity> needAddList =  list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getField())).collect(Collectors.toList());
        if (CollUtil.isEmpty(needAddList)) {
            //删除历史数据
            if (CollUtil.isNotEmpty(oldList)) {
                List<String> idsToDelete = oldList.stream()
                        .map(CfgProcessExpEntity::getId)
                        .collect(Collectors.toList());
                this.removeByIds(idsToDelete);
                log.info("删除流程设置审核条件: {}", idsToDelete);
            }
            return new BaseResultDTO.UpdateDTO();
        }

        List<String> deleteIds = getDeleteIds(needAddList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        //数据处理
        handleAddData(needAddList,ruleId);

        log.info("开始新增流程设置审核条件");
        this.saveOrUpdateBatch(needAddList);

        Map<String, CfgProcessExpEntity> expEntityMap = oldList.stream()
                .collect(Collectors.toMap(CfgProcessExpEntity::getId, entity -> entity));
        // 操作日志
        needAddList.forEach(existingEntity -> {
            CfgProcessExpEntity updateEntity = expEntityMap.get(existingEntity.getId());
            if (ObjectUtil.isNotEmpty(updateEntity)) {
                operateLogService.addModuleOperateLogByObj(existingEntity, updateEntity, ModuleTypeEnum.CFG_PROCESS.getCode(), cfgProcessId, "编辑审核条件");
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
        List<CfgProcessExpEntity> cfgProcessExpEntityList = this.list(new LambdaQueryWrapper<CfgProcessExpEntity>().in(CfgProcessExpEntity::getRuleId, mainIds).eq(CfgProcessExpEntity::getIsDeleted, false));
        if (CollUtil.isEmpty(cfgProcessExpEntityList)) {
            return;
        }
        List<String> ids = cfgProcessExpEntityList.stream().map(CfgProcessExpEntity::getId).collect(Collectors.toList());
        removeByIds(ids);
        //TODO 日志
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

    @Override
    public List<CfgProcessExpEntity> listByRuleIdList(List<String> ruleIdList) {
        if (CollUtil.isEmpty(ruleIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(CfgProcessExpEntity::getRuleId,ruleIdList).list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgProcessExpEntity> newList, List<CfgProcessExpEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> CharSequenceUtil.isNotBlank(g.getId())).
                map(CfgProcessExpEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgProcessExpEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     *  数据处理
     * @author will
     * @date 2025/7/15 12:26
     * @param list
     * @param ruleId
     * @return List<CfgProcessExpEntity>
     */
    private  List<CfgProcessExpEntity> handleAddData (List<CfgProcessExpEntity> list,String ruleId) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<CfgProcessExpEntity> resultList = new ArrayList<>();
        Integer index = MathUtil.ONE;
        for (CfgProcessExpEntity expEntity : list) {
            // 字段为空的无需新增
            if (CharSequenceUtil.isBlank(expEntity.getField())) {
                continue;
            }
            expEntity.setIndex(index);
            expEntity.setRuleId(ruleId);
            resultList.add(expEntity);
            index += 1;
        }
        return resultList;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgProcessExpEntity cfgProcessExpEntity) {

    }

}
