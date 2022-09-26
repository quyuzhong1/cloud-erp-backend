package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsNameDTO;
import com.erp.model.plm.entity.SysDocsEntity;
import com.erp.model.plm.entity.TaskDocsNameEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.TaskDocsNameMapper;
import com.erp.server.plm.service.SysDocsService;
import com.erp.server.plm.service.TaskDocsNameService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname TaskDocsNameServiceImpl
 * @Description TODO
 * @Date 2022-09-22 12:21
 * @Created by yl
 */
@Service
public class TaskDocsNameServiceImpl extends ServiceImpl<TaskDocsNameMapper, TaskDocsNameEntity> implements TaskDocsNameService {

    @Autowired
    private SysDocsService sysDocsService;

    /**
     * 保存文档名
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-22 12:24
     */
    @Override
    public Boolean saveDocsName(DocsNameDTO dto) {
        String name = dto.getName();
        String productId = dto.getProductId();
        List<DocsDTO> docksNames = getDocsNameList(productId);
        List<String> names = docksNames.stream().map(DocsDTO::getName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(names) && names.contains(name)) {
            throw new ServiceException(ApiError.ERROR_95012);
        }
        TaskDocsNameEntity entity = new TaskDocsNameEntity();
        entity.setName(name);
        entity.setProductId(productId);
        return this.save(entity);
    }


    /**
     * 获取文档列表  先从系统里面拿
     * 然后在从文档表拿
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-22 14:02
     */
    public List<DocsDTO> getDocsNameList(String productId) {
        List<DocsDTO> resultList = new LinkedList<>();
        int state = IsConstant.YES;
        List<DocsDTO> sysDocsNames = sysDocsService.getDocsNames(state);
        for (DocsDTO item : sysDocsNames) {
            item.setIsSys(state);
        }
        resultList.addAll(sysDocsNames);
        LambdaQueryWrapper<TaskDocsNameEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsNameEntity::getProductId, productId);
        List<TaskDocsNameEntity> list = list(queryWrapper);
        List<DocsDTO> docsNames = BeanMapper.copyList(list, DocsDTO.class);
        int noSys = IsConstant.NO;
        for (DocsDTO item : docsNames) {
            item.setIsSys(noSys);
        }
        resultList.addAll(docsNames);
        return resultList;

    }
}
