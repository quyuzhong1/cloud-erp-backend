package com.common.core.utils;

import com.common.core.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DetailEntityChangeLogger {

    public interface Logger {
        void addModuleOperateLog(String message, String moduleType, String mainId, String operationType);
        void addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String mainId, String operationType);
    }

    /**
     * 记录对比两个列表后的增删改操作日志
     * 此方法用于比较新旧两个实体列表，根据列表内容的变化生成相应的日志记录，
     * 包括删除、新增和更新操作的日志信息
     *
     * @param newList 新列表，代表最新的数据状态
     * @param oldList 旧列表，代表之前的数据状态
     * @param mainId 主标识符，用于标识日志的主要对象
     * @param moduleType 模块类型，标识操作所属的模块
     * @param removeMsg 删除操作的自定义日志信息
     * @param newMsg 新增操作的自定义日志信息
     * @param updateMsg 更新操作的自定义日志信息
     * @param userName 用户名，执行操作的用户
     * @param logger 日志记录器，用于添加操作日志
     * @param <T> 泛型参数，表示实体类类型，必须继承自BaseEntity
     */
    public static <T extends BaseEntity> void logChanges(List<T> newList,
                                                         List<T> oldList,
                                                         String mainId,
                                                         String moduleType,
                                                         String removeMsg,
                                                         String newMsg,
                                                         String updateMsg,
                                                         String userName,
                                                         Logger logger) {
        // 空值保护
        if (newList == null)return;
        if (oldList == null) return;
        if (logger == null) return;

        // 提取新列表中的所有非空ID
        List<String> ids = newList.stream()
                .map(T::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 找出旧列表中被删除的实体
        List<T> removeList = oldList.stream()
                .filter(e -> !ids.contains(e.getId()))
                .collect(Collectors.toList());

        // 如果有实体被删除，生成删除日志
        if (!removeList.isEmpty()) {
            if(StringUtils.isBlank(removeMsg)){
                removeMsg = String.format("用户【%s】删除", userName);
            }
            for (T e : removeList) {
                logger.addModuleOperateLog(removeMsg, moduleType, mainId, "编辑信息");
            }
        }

        // 找出新列表中新增的实体
        List<T> newEntities = newList.stream()
                .filter(e -> StringUtils.isBlank(e.getId()))
                .collect(Collectors.toList());

        // 如果有新增的实体，生成新增日志
        if (!newEntities.isEmpty()) {
            if(StringUtils.isBlank(newMsg)){
                newMsg = String.format("用户【%s】新增", userName);
            }
            for (T e : newEntities) {
                logger.addModuleOperateLog(newMsg, moduleType, mainId, "编辑信息");
            }
        }

        // 找出新列表中可能被更新的实体
        List<T> updateList = newList.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toList());

        // 如果有实体可能被更新，生成更新日志
        if (!updateList.isEmpty()) {
            if(StringUtils.isBlank(updateMsg)){
                updateMsg = String.format("用户【%s】编辑",userName);
            }
            for (T e : updateList) {
                T oldEntity = oldList.stream()
                        .filter(o -> e.getId().equals(o.getId()))
                        .findFirst()
                        .orElse(null);
                if (oldEntity != null) {
                    logger.addModuleOperateLogByObj(oldEntity, e, moduleType, mainId, updateMsg);
                }
            }
        }
    }

}
