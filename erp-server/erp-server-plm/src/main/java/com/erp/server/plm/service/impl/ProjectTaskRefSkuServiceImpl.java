package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProjectTaskRefSkuEntity;
import com.erp.server.plm.mapper.ProjectTaskRefSkuMapper;
import com.erp.server.plm.service.ProjectTaskRefSkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:56
 */
@Service
public class ProjectTaskRefSkuServiceImpl extends ServiceImpl<ProjectTaskRefSkuMapper, ProjectTaskRefSkuEntity> implements ProjectTaskRefSkuService {


    /**
     * 保存任务与sku 关系表
     *
     * @param taskId
     * @param productId
     * @param refSkuIdList
     * @return void
     * @author yl
     * @date 2022-11-21 15:46
     */
    @Override
    @Transactional
    public void addTaskSkuRef(String taskId, String productId, List<String> refSkuIdList) {
        deleteByTaskId(taskId);
        List<ProjectTaskRefSkuEntity> addList = new ArrayList<>(CollectionUtils.isEmpty(refSkuIdList) ? 10 : refSkuIdList.size());
        if (CollectionUtils.isNotEmpty(refSkuIdList)) {
            for (String skuId : refSkuIdList) {
                ProjectTaskRefSkuEntity addEntity = new ProjectTaskRefSkuEntity();
                addEntity.setProductId(productId);
                addEntity.setTaskId(taskId);
                addEntity.setSkuId(skuId);
                addList.add(addEntity);
            }
            if (CollectionUtils.isNotEmpty(addList)) {
                this.saveBatch(addList);
            }
        }
    }

    @Override
    public List<ProjectTaskRefSkuEntity> getByTaskId(String taskId) {
        LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskRefSkuEntity::getTaskId, taskId);
        return this.list(queryWrapper);
    }

    /**
     * 项目启动后 所 保存的任务 自动关联 sku
     *
     * @param addTaskIdList
     * @param productId
     * @return void
     * @author yl
     * @date 2022-11-24 17:10
     */
    @Override
    public void saveBatchTaskRefSku(List<String> addTaskIdList, String productId, List<ProductDetailEntity> skuList) {
        List<ProjectTaskRefSkuEntity> addList = new ArrayList<>();
        for (String taskId : addTaskIdList) {
            for (ProductDetailEntity item : skuList) {
                ProjectTaskRefSkuEntity ref = new ProjectTaskRefSkuEntity();
                ref.setSkuId(item.getId());
                ref.setTaskId(taskId);
                ref.setProductId(productId);
                addList.add(ref);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }

    }

    /**
     * 根据任务id 删除 任务与sku 关系
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-11-21 15:47
     */
    public void deleteByTaskId(String taskId) {
        LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskRefSkuEntity::getTaskId, taskId);
        this.remove(queryWrapper);

    }
}
