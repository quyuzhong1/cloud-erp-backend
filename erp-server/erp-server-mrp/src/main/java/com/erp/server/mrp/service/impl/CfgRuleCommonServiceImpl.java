package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;
import com.erp.model.mrp.enums.CfgRuleCommonTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleCommonMapper;
import com.erp.server.mrp.service.CfgRuleCommonService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 公共配置（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleCommonServiceImpl extends SuperServiceImpl<CfgRuleCommonMapper, CfgRuleCommonEntity> implements CfgRuleCommonService {
    @Autowired
    private OperateLogService operateLogService;


    @Autowired
    private CfgRuleWarehouseService cgRuleWarehouseService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    @CacheEvict(cacheNames = "cache:mrp:getCfgRuleCommon", allEntries = true, beforeInvocation = true)
    public Boolean update(List<CfgRuleCommonDTO.UpdateDTO> updateList) {
        // 数据处理
        List<CfgRuleCommonEntity> list = handleData(updateList);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("公共配置（规则设置）保存失败");
        }
        //操作日志
        addOperateLog(updateList,list);
        return Boolean.TRUE;
    }

    /**
     * 添加日志
     * @author will
     * @date 2024/9/11 16:32
     * @param updateList
     */
    private void addOperateLog (List<CfgRuleCommonDTO.UpdateDTO> updateList,List<CfgRuleCommonEntity> list) {
        if (CollectionUtils.isEmpty(updateList)) {
            return;
        }
        for (CfgRuleCommonDTO.UpdateDTO updateDTO : updateList) {
            StringBuffer msg = new StringBuffer();
            appendOperateLog(updateDTO,msg);
            //最上级id
            String id = list.stream().filter(obj -> StrUtil.equals(updateDTO.getName(), obj.getName())).findFirst().map(CfgRuleCommonEntity::getId).orElse("");
            operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), id, CfgRuleCommonTypeEnum.getName(updateDTO.getType()));
        }
    }
    /**
     * 添加日志
     * @author will
     * @date 2024/9/11 16:18
     * @param updateDTO
     * @param msg
     */
    private void appendOperateLog (CfgRuleCommonDTO.UpdateDTO updateDTO,StringBuffer msg) {
        //循环添加
        if (CollectionUtils.isNotEmpty(updateDTO.getChildrenList())) {
            msg.append(updateDTO.getName().concat("<br>"));
            updateDTO.getChildrenList().stream().forEach(obj -> appendOperateLog(obj, msg));
            msg.append("<br>");
        }  else {
            if (StrUtil.equals(updateDTO.getValue(),"true")) {
                msg.append(updateDTO.getName().concat(","));
            }
        }
    }

    @Override
    public List<CfgRuleCommonDTO.ViewDTO> view(String platformType,String type) {
        //查询已存在数据
        List<CfgRuleCommonDTO.ViewDTO> viewList = baseMapper.listRuleCommon(platformType,type);
        if (CollectionUtils.isEmpty(viewList)) {
            //返回初始化数据
           return this.listDefaultRuleCommonTree(platformType,type);
        }
        //返回新增数据
        List<CfgRuleCommonDTO.ViewDTO> treeList = viewList.stream().filter(obj -> StrUtil.isBlank(obj.getParentId())).map(item -> {
            item.setChildrenList(getChildren(item, viewList));
            return item;
        }).collect(Collectors.toList());
        return treeList;
    }

    @Override
    @Cacheable(cacheNames = "cache:mrp:getCfgRuleCommon",keyGenerator = "myKeyGenerator")
    public List<CfgRuleCommonDTO.StrategyResultDTO> getCfgRuleCommon(String platformType, String type) {
        List<CfgRuleCommonDTO.StrategyResultDTO> strategyList = baseMapper.listByPlatformTypeAndType(platformType, type, false);
        if (CollectionUtils.isEmpty(strategyList)) {
            //返回初始化数据
            strategyList  = baseMapper.listByPlatformTypeAndType(platformType,type, true);
        }
        return buildTree(strategyList);
    }


    /**
     * 根据 platformType 和 type 构建树形结构
     *
     * @param strategyList 所有的 StrategyResultDTO 数据
     * @return 构建好的树形结构列表
     */
    public List<CfgRuleCommonDTO.StrategyResultDTO> buildTree(List<CfgRuleCommonDTO.StrategyResultDTO> strategyList) {
        // 获取所有的根节点（没有父级的节点，通常 parentId 为 null 或空）
        List<CfgRuleCommonDTO.StrategyResultDTO> rootNodes = strategyList.stream()
                .filter(item -> item.getParentId() == null || item.getParentId().isEmpty())
                .collect(Collectors.toList());

        // 递归设置子节点
        rootNodes.forEach(root -> setChildren(root, strategyList));
        return rootNodes;
    }

    /**
     * 递归设置子节点
     *
     * @param parentNode 父节点
     * @param allNodes   所有的节点数据
     */
    private void setChildren(CfgRuleCommonDTO.StrategyResultDTO parentNode, List<CfgRuleCommonDTO.StrategyResultDTO> allNodes) {
        // 找到所有 parentId 等于父节点 id 的节点，作为其子节点
        List<CfgRuleCommonDTO.StrategyResultDTO> children = allNodes.stream()
                .filter(item -> parentNode.getId().equals(item.getParentId()))
                .collect(Collectors.toList());
        // 设置子节点
        parentNode.setChildrenList(children);
        // 对每个子节点递归查找其子节点
        children.forEach(child -> setChildren(child, allNodes));
    }


    /**
     * 获取默认配置
     * @author will
     * @date 2024/8/27 9:28
     * @param platformType
     * @return List<ViewDTO>
     */
    private List<CfgRuleCommonDTO.ViewDTO> listDefaultRuleCommonTree (String platformType,String type) {
        List<CfgRuleCommonDTO.ViewDTO> viewList = baseMapper.listDefaultRuleCommon(platformType,type);
        if (CollectionUtils.isEmpty(viewList)) {
            return Collections.EMPTY_LIST;
        }
        //返回新增数据
        List<CfgRuleCommonDTO.ViewDTO> treeList = viewList.stream().filter(obj -> StrUtil.isBlank(obj.getParentId())).map(item -> {
            item.setChildrenList(getChildren(item, viewList));
            return item;
        }).collect(Collectors.toList());
        return  treeList;
    }
    /**
     * 获取子级信息
     * @author will
     * @date 2024/8/26 12:10
     * @param viewDTO
     * @param viewList
     * @return List<ViewDTO>
     */
    private List<CfgRuleCommonDTO.ViewDTO> getChildren(CfgRuleCommonDTO.ViewDTO viewDTO, List<CfgRuleCommonDTO.ViewDTO> viewList) {
        List<CfgRuleCommonDTO.ViewDTO> list = viewList.stream().filter(obj -> viewDTO.getId().equals(obj.getParentId()))
                .map(obj -> {
                    obj.setChildrenList(getChildren(obj, viewList));
                    return obj;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(list) ? null : list;
    }

    /**
    * 新增修改处理数据
    */
    private List<CfgRuleCommonEntity> handleData(List<CfgRuleCommonDTO.UpdateDTO> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
           throw new ServiceException("保存数据不能为空");
        }
        List<CfgRuleCommonEntity> resultList = new ArrayList<>();

        for (CfgRuleCommonDTO.UpdateDTO updateDTO : updateList) {
            CfgRuleCommonEntity entity = BeanMapperUtils.map(CfgRuleCommonEntity.class, updateDTO);
            //是否默认，默认保存则清空默认数据
            Boolean isDefault = updateDTO.getIsDefault();
            if (isDefault) {
                entity.setId(IdWorker.getIdStr());
                entity.setIsDefault(Boolean.FALSE);
            }
            resultList.add(entity);
            //子级赋值
            getChildrenEntity(entity,resultList,isDefault);
        }
        return resultList;
    }

    /**
     * 子级赋值
     * @author will
     * @date 2024/8/28 10:04
     * @param entity
     * @param resultList
     */
    private void getChildrenEntity(CfgRuleCommonEntity entity,List<CfgRuleCommonEntity> resultList,Boolean isDefault) {
        if (CollectionUtils.isEmpty(entity.getChildrenList())) {
            return;
        }
        for (CfgRuleCommonDTO.UpdateDTO childUpdateDTO :entity.getChildrenList()) {
            CfgRuleCommonEntity childEntity = BeanMapperUtils.map(CfgRuleCommonEntity.class, childUpdateDTO);
            if (isDefault) {
                childEntity.setId(IdWorker.getIdStr());
                childEntity.setIsDefault(Boolean.FALSE);
                //父级Id
                childEntity.setParentId(entity.getId());
            }
            resultList.add(childEntity);
            getChildrenEntity(childEntity,resultList,isDefault);
        }
    }
}
