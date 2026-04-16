package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperService;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.tms.service.CommonService;
import com.erp.server.tms.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yl
 * @Classname CommonServiceImpl

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {


    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private OperateLogService operateLogService;


    @Override
    public <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, List<String> keyFieldNames) {
        // 处理需要删除的数据
        if (CollUtil.isNotEmpty(oldDetailList)) {
            List<String> detailIds = detailList.stream()
                    .map(BaseEntity::getId)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            List<T> remove = oldDetailList.stream()
                    .filter(oldEntity -> !detailIds.contains(oldEntity.getId()))
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(remove)) {
                service.removeByIds(remove.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                //添加日志
                for (T entity : remove) {
                    try {
                        StringBuffer fieldName = new StringBuffer();
                        // 获取实体类的Class对象
                        Class<?> clazz = entity.getClass();
                        for (String keyFieldName : keyFieldNames) {
                            // 同样地，可以获取其他字段的值
                            Field nameField = clazz.getDeclaredField(keyFieldName); // 替换为你想访问的字段名
                            nameField.setAccessible(true);
                            Object nameValue = nameField.get(entity);
                            if(Objects.nonNull(nameValue)){
                                fieldName.append(nameValue);
                                fieldName.append("+");
                            }
                        }
                        String fieldNameStr = fieldName.toString();
                        //移除fieldNameStr最后的一个+字符
                        fieldNameStr = fieldName.toString().substring(0, fieldNameStr.length() - 1);
                        // 添加日志记录的逻辑
                        operateLogService.addModuleOperateLog(StrUtil.format("删除【{}】",fieldNameStr), moduleType, businessId, "编辑信息");
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 处理需要新增的数据
        List<T> addList = detailList.stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            service.saveBatch(addList);
            //添加日志
            for (T entity : addList) {
                try {
                    StringBuffer fieldName = new StringBuffer();
                    // 获取实体类的Class对象
                    Class<?> clazz = entity.getClass();
                    for (String keyFieldName : keyFieldNames) {
                        // 同样地，可以获取其他字段的值
                        Field nameField = clazz.getDeclaredField(keyFieldName); // 替换为你想访问的字段名
                        nameField.setAccessible(true);
                        Object nameValue = nameField.get(entity);
                        if(Objects.nonNull(nameValue)){
                            fieldName.append(nameValue);
                            fieldName.append("+");
                        }
                    }
                    String fieldNameStr = fieldName.toString();
                    //移除fieldNameStr最后的一个+字符
                    fieldNameStr = fieldName.toString().substring(0, fieldNameStr.length() - 1);
                    // 添加日志记录的逻辑
                    operateLogService.addModuleOperateLog(StrUtil.format("新增【{}】",fieldNameStr), moduleType, businessId, "编辑信息");
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // 处理需要更新的数据
        List<T> updateList = detailList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(updateList)) {
            service.updateBatchById(updateList);
            //如何从updateList获取到指定的字段的值
            for (T entity : updateList) {
                T oldDetail = (T) oldDetailList.stream()
                        .filter(e -> Objects.equals(e.getId(), entity.getId()))
                        .findFirst()
                        .orElse(null);
                if (Objects.nonNull(oldDetail)) {
                    // 可以在这里添加日志记录的逻辑
                    operateLogService.addModuleOperateLogByObj(oldDetail, entity, moduleType, oldDetail.getId(), "编辑信息");
                }
            }
        }
    }

}
