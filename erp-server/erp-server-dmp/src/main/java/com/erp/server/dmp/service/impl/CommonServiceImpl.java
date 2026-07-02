package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.SuperService;
import com.common.core.entity.BaseEntity;
import com.erp.server.dmp.service.CommonService;
import com.erp.server.dmp.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public <T extends BaseEntity> void updateDetail(String businessId, String moduleType, SuperService service, List<T> detailList, List<T> oldDetailList, List<String> keyFieldNames) {
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
                for (T entity : remove) {
                    addDetailLog(entity, keyFieldNames, moduleType, businessId, "删除【{}】");
                }
            }
        }

        List<T> addList = detailList.stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(addList)) {
            service.saveBatch(addList);
            for (T entity : addList) {
                addDetailLog(entity, keyFieldNames, moduleType, businessId, "新增【{}】");
            }
        }

        List<T> updateList = detailList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(updateList)) {
            service.updateBatchById(updateList);
            for (T entity : updateList) {
                T oldDetail = (T) oldDetailList.stream()
                        .filter(e -> Objects.equals(e.getId(), entity.getId()))
                        .findFirst()
                        .orElse(null);
                if (Objects.nonNull(oldDetail)) {
                    operateLogService.addModuleOperateLogByObj(oldDetail, entity, moduleType, oldDetail.getId(), "编辑信息");
                }
            }
        }
    }

    private <T extends BaseEntity> void addDetailLog(T entity, List<String> keyFieldNames, String moduleType, String businessId, String messageTemplate) {
        try {
            StringBuilder fieldName = new StringBuilder();
            Class<?> clazz = entity.getClass();
            for (String keyFieldName : keyFieldNames) {
                Field nameField = clazz.getDeclaredField(keyFieldName);
                nameField.setAccessible(true);
                Object nameValue = nameField.get(entity);
                if (Objects.nonNull(nameValue)) {
                    fieldName.append(nameValue).append("+");
                }
            }
            if (fieldName.length() > 0) {
                fieldName.deleteCharAt(fieldName.length() - 1);
            }
            operateLogService.addModuleOperateLog(StrUtil.format(messageTemplate, fieldName), moduleType, businessId, "编辑信息");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}