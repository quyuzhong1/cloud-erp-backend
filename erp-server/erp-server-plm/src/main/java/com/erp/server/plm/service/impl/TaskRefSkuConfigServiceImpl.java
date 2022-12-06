package com.erp.server.plm.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.TaskRefSkuConfigEntity;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.TaskRefSkuConfigMapper;
import com.erp.server.plm.service.TaskRefSkuConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务sku配置关系表(TaskRefSkuConfig)表服务实现类
 *
 * @author Lambda
 * @since 2022-11-21 14:01:00
 */
@Service
public class TaskRefSkuConfigServiceImpl extends ServiceImpl<TaskRefSkuConfigMapper, TaskRefSkuConfigEntity> implements TaskRefSkuConfigService {
    @Resource
    private TaskRefSkuConfigMapper taskRefSkuConfigMapper;


    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return this.taskRefSkuConfigMapper.deleteById(id) > 0;
    }

    /**
     * 添加任务 与 sku 配置 字段的关系
     *
     * @param taskId
     * @param productId
     * @param fieldConfigType
     * @param fieldJson
     * @return void
     * @author yl
     * @date 2022-11-21 15:31
     */
    @Override
    public void addSkuField(String taskId, String productId, String fieldConfigType, String fieldJson) {
        if (StringUtils.isNotBlank(fieldConfigType)) {
            TaskRefSkuConfigEntity existEntity = getByTaskId(taskId);
            TaskRefSkuConfigEntity addEntity = new TaskRefSkuConfigEntity();
            if (existEntity != null) {
                addEntity.setId(existEntity.getId());
            }
            addEntity.setFieldJson(fieldJson);
            addEntity.setTaskId(taskId);
            addEntity.setProductId(productId);
            addEntity.setFieldConfigType(fieldConfigType);
            this.saveOrUpdate(addEntity);
        }

    }

    /**
     * 根据任务id 删除 表字段关系
     *
     * @param taskId
     * @return void
     * @author yl
     * @date 2022-11-21 17:52
     */
    @Override
    public void deleteByTaskId(String taskId) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskRefSkuConfigEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }


    /**
     * 删除SKU 后需要删除SKU 关联关系
     *
     * @param skuId
     * @return void
     * @author yl
     * @date 2022-11-22 9:21
     */
    @Override
    public void removeTaskRefSku(String skuId) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskRefSkuConfigEntity::getSkuId, skuId);
        this.remove(queryWrapper);

    }

    /**
     * 根据任务id 集合 获取对应关系
     *
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskRefSkuConfigEntity>
     * @author yl
     * @date 2022-11-23 11:48
     */
    @Override
    public List<TaskRefSkuConfigEntity> getByTaskIds(List<String> taskIds) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            queryWrapper.in(TaskRefSkuConfigEntity::getTaskId, taskIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();

    }


    /**
     * 根据任务id获取配置信息
     *
     * @param taskId
     * @return com.erp.model.plm.entity.TaskRefSkuConfigEntity
     * @author yl
     * @date 2022-11-23 19:20
     */
    @Override
    public TaskRefSkuConfigEntity getByTaskId(String taskId) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskRefSkuConfigEntity::getTaskId, taskId);
        queryWrapper.orderByDesc(TaskRefSkuConfigEntity::getCreateTime);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    /**
     * 根据产品id获取sku 与字段的配置关系表
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.TaskRefSkuConfigEntity>
     * @author yl
     * @date 2022-11-24 16:32
     */
    @Override
    public List<TaskRefSkuConfigEntity> getByProductId(String productId) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskRefSkuConfigEntity::getProductId, productId);
        return this.list(queryWrapper);
    }

    /**
     * 根据产品id 获取被禁用的字段
     *
     * @param productId
     * @return com.erp.model.plm.entity.TaskRefSkuConfigEntity
     * @author yl
     * @date 2022-11-28 11:04
     */
    @Override
    public List<TaskRefSkuConfigEntity> getDisableFieldByProductId(String productId) {
        List<Integer> stateList = new ArrayList<>(10);
        stateList.add(TaskStateEnum.APPROVAL_ING.getCode());
        stateList.add(TaskStateEnum.APPROVAL_NO_PASS.getCode());
        stateList.add(TaskStateEnum.FINISH.getCode());
        List<TaskRefSkuConfigEntity> resultList = baseMapper.getDisableFieldByProductId(productId, stateList);
        return resultList;
    }


    /**
     * 自动生成sku配置
     *
     * @param taskIdList
     * @param configType
     * @return void
     * @author yl
     * @date 2022-12-05 19:47
     */
    @Override
    public void autoCreateSkuConfig(List<String> taskIdList, String configType, String productId) {
        List<TaskRefSkuConfigEntity> addList = new ArrayList<>(taskIdList.size());
        for (String taskId : taskIdList) {
            TaskRefSkuConfigEntity item = new TaskRefSkuConfigEntity();
            item.setFieldJson("");
            item.setTaskId(taskId);
            item.setFieldConfigType(configType);
            item.setProductId(productId);
            addList.add(item);
        }
        this.saveBatch(addList);

    }


    /**
     * 根据任务id 和 产品ｉｄ 获取关系表
     *
     * @param
     * @return com.erp.model.plm.entity.TaskRefSkuConfigEntity
     * @author yl
     * @date 2022-11-21 15:33
     */
    public TaskRefSkuConfigEntity getByTaskId(String taskId, String productId) {
        LambdaQueryWrapper<TaskRefSkuConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskRefSkuConfigEntity::getTaskId, taskId);
        queryWrapper.eq(TaskRefSkuConfigEntity::getProductId, productId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }
}

    

