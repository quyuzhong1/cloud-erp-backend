package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.entity.CfgRuleCommonEntity;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.server.mrp.mapper.CfgRuleCommonMapper;
import com.erp.server.mrp.service.CfgRuleCommonService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Boolean update(CfgRuleCommonDTO.UpdateDTO updateDTO) {
        // 数据处理
        List<CfgRuleCommonEntity> list = handleData(updateDTO);

        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("公共配置（规则设置）保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CfgRuleCommonDTO.ViewDTO> view(String platformType,String type) {
        //是否存在海外仓
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity = cgRuleWarehouseService.getByPlatformType(platformType);
        Boolean isEnableOverseas = Boolean.FALSE;
        if (ObjectUtil.isNotEmpty(cfgRuleWarehouseEntity) && cfgRuleWarehouseEntity.getIsEnableOverseas()) {
            isEnableOverseas = Boolean.TRUE;
        }

        //查询已存在数据
        List<CfgRuleCommonDTO.ViewDTO> viewList = baseMapper.listRuleCommon(platformType,type, isEnableOverseas);
        if (CollectionUtils.isEmpty(viewList)) {
            //返回初始化数据
           return this.listDefaultRuleCommonTree(platformType,type,isEnableOverseas);
        }
        //返回新增数据
        List<CfgRuleCommonDTO.ViewDTO> treeList = viewList.stream().filter(obj -> StrUtil.isBlank(obj.getParentId())).map(item -> {
            item.setChildrenList(getChildren(item, viewList));
            return item;
        }).collect(Collectors.toList());
        return treeList;
    }


    /**
     * 获取默认配置
     * @author will
     * @date 2024/8/27 9:28
     * @param platformType
     * @param isEnableOverseas
     * @return List<ViewDTO>
     */
    private List<CfgRuleCommonDTO.ViewDTO> listDefaultRuleCommonTree (String platformType,String type,Boolean isEnableOverseas) {
        List<CfgRuleCommonDTO.ViewDTO> viewList = baseMapper.listDefaultRuleCommon(platformType,type, isEnableOverseas);
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
    private List<CfgRuleCommonEntity> handleData(CfgRuleCommonDTO.UpdateDTO updateDTO) {
        List<CfgRuleCommonEntity> resultList = new ArrayList<>();
        CfgRuleCommonEntity entity = BeanMapperUtils.map(CfgRuleCommonEntity.class, updateDTO);
        //是否默认，默认保存则清空默认数据
        Boolean isDefault = updateDTO.getIsDefault();
        if (isDefault) {
            updateDTO.setId(null);
            updateDTO.setIsDefault(Boolean.FALSE);
        }
        resultList.add(entity);
        //子级赋值
        getChildrenEntity(updateDTO,resultList,isDefault);
        return resultList;
    }

    /**
     * 子级赋值
     * @author will
     * @date 2024/8/28 10:04
     * @param updateDTO
     * @param resultList
     */
    private void getChildrenEntity(CfgRuleCommonDTO.UpdateDTO updateDTO,List<CfgRuleCommonEntity> resultList,Boolean isDefault) {
        if (CollectionUtils.isEmpty(updateDTO.getChildrenList())) {
            return;
        }
        for (CfgRuleCommonDTO.UpdateDTO childUpdateDTO :updateDTO.getChildrenList()) {
            CfgRuleCommonEntity childEntity = BeanMapperUtils.map(CfgRuleCommonEntity.class, childUpdateDTO);
            if (isDefault) {
                updateDTO.setId(null);
                updateDTO.setIsDefault(Boolean.FALSE);
            }
            resultList.add(childEntity);
            getChildrenEntity(childUpdateDTO,resultList,isDefault);
        }
    }
}
