package com.erp.server.scm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.enums.DictBasicEnum;
import com.erp.server.scm.mapper.DictBasicMapper;
import com.erp.server.scm.service.DictBasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {


    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 12:21
     */
    @Override
    public Boolean saveOrUpdateDict(List<DictBasicDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        return this.saveOrUpdateBatch(addList);
    }


    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return java.util.List<com.erp.model.scm.dto.DictBasicDTO>
     * @author yl
     * @date 2023-03-17 14:16
     */
    @Override
    public List<DictBasicDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.class);
        return resultList;
    }


    /**
     * 根据key list 获取对应数据
     *
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     * @author yl
     * @date 2023-03-20 14:24
     */
    @Override
    public List<DictBasicEntity> getByKeyList(List<String> keyList) {
        if (CollectionUtils.isEmpty(keyList)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DictBasicEntity::getType, keyList);
        return this.list(queryWrapper);
    }

    @Override
    public List<DictBasicEntity> listByNameList(List<String> nameList, DictBasicEnum dictBasicEnum) {
        if (CollUtil.isEmpty(nameList) || dictBasicEnum == null) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(DictBasicEntity::getName,nameList).eq(DictBasicEntity::getType,dictBasicEnum.getType()).list();
    }

    @Override
    public List<DictBasicDTO> tree(String key) {
        List<DictBasicEntity> list = listByKey(key);
        return buildTree(BeanMapperUtils.copyList(DictBasicDTO.class, list));
    }

    /**
     * @return 构建好的树形结构列表
     */
    public List<DictBasicDTO> buildTree(List<DictBasicDTO> treeList) {
        // 获取所有的根节点（没有父级的节点，通常 parentId 为 null 或空）
        List<DictBasicDTO> rootNodes = treeList.stream()
                .filter(item -> ObjectUtils.isEmpty(item.getRemark()))
                .collect(Collectors.toList());
        // 递归设置子节点
        rootNodes.forEach(root -> setChildren(root, treeList));
        return rootNodes;
    }

    /**
     * 递归设置子节点
     *
     * @param parentNode 父节点
     * @param allNodes   所有的节点数据
     */
    private void setChildren(DictBasicDTO parentNode, List<DictBasicDTO> allNodes) {
        // 找到所有 parentId 等于父节点 id 的节点，作为其子节点
        List<DictBasicDTO> children = allNodes.stream()
                .filter(item -> parentNode.getId().equals(item.getRemark()))
                .collect(Collectors.toList());
        // 设置子节点
        parentNode.setChildList(children);
        // 对每个子节点递归查找其子节点
        children.forEach(child -> setChildren(child, allNodes));
    }


    private List<DictBasicEntity> listByKey(String key) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, key);
        return this.list(queryWrapper);
    }

}
