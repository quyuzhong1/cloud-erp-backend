package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.plm.dto.TaskFinishSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProjectTaskRefSkuEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProjectTaskRefSkuMapper;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProjectTaskRefSkuService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 14:56
 */
@Service
public class ProjectTaskRefSkuServiceImpl extends ServiceImpl<ProjectTaskRefSkuMapper, ProjectTaskRefSkuEntity> implements ProjectTaskRefSkuService {


    @Autowired
    private ProductDetailService productDetailService;

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
     * 根据产品id 获取对应关系
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.ProjectTaskRefSkuEntity>
     * @author yl
     * @date 2022-11-28 12:12
     */
    @Override
    public List<ProjectTaskRefSkuEntity> getByProductId(String productId) {
        LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskRefSkuEntity::getProductId, productId);
        return this.list(queryWrapper);
    }


    /**
     * 完成任务的时候 检查 关联的sku 是否已完成
     *
     * @param taskIdList
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-28 18:14
     */
    @Override
    public List<String> checkTaskRefSkuFinish(List<String> taskIdList) {
        List<ProjectTaskRefSkuEntity> list = getByTaskIdList(taskIdList);
        Integer noFinish = IsConstant.NO;
        List<String> skuIdList = list.stream().filter(r -> r.getIsFinishTask().equals(noFinish)).map(ProjectTaskRefSkuEntity::getSkuId).collect(Collectors.toList());
        List<BaseIdDTO> notFinishList = productDetailService.getNotFinish(skuIdList);
        return notFinishList.stream().map(BaseIdDTO::getName).distinct().collect(Collectors.toList());
    }


    /**
     * 根据任务id 集合获取对应sku 关系
     *
     * @return
     * @parms
     * @author yl
     * @date 2022-11-29
     */
    @Override
    public List<ProjectTaskRefSkuEntity> getByTaskIdList(List<String> taskIdList) {
        if (CollectionUtils.isNotEmpty(taskIdList)) {
            LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProjectTaskRefSkuEntity::getTaskId, taskIdList);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 完成 任务相关的sku
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-12-05 11:00
     */
    @Override
    @Transactional
    public void taskFinishRefSku(TaskFinishSkuDTO dto) {
        String taskId = dto.getTaskId();
        String productId = dto.getProductId();
        List<String> skuIdList = dto.getSkuIdList();
        List<String> allList = dto.getAllList();
        List<ProjectTaskRefSkuEntity> addList = new ArrayList<>();
        for (String skuId: allList) {
            ProjectTaskRefSkuEntity entity = this.getBySkuIdAndTaskId(skuId, taskId);
            if (ObjectUtils.isEmpty(entity)) {
                ProjectTaskRefSkuEntity addEntity = new ProjectTaskRefSkuEntity();
                addEntity.setTaskId(taskId);
                addEntity.setSkuId(taskId);
                addEntity.setProductId(productId);
                addEntity.setIsFinishTask(IsConstant.NO);
                addList.add(entity);
            }
        }
        if (CollectionUtils.isNotEmpty(addList)) {
            this.saveBatch(addList);
        }
        setTaskNoFinishRefSku(taskId);
        if (CollectionUtils.isNotEmpty(skuIdList)) {
            LambdaUpdateWrapper<ProjectTaskRefSkuEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ProjectTaskRefSkuEntity::getTaskId, taskId);
            updateWrapper.in(ProjectTaskRefSkuEntity::getSkuId, skuIdList);
            updateWrapper.set(ProjectTaskRefSkuEntity::getIsFinishTask, 1);
            this.update(updateWrapper);
        }

    }

    @Override
    public List<ProjectTaskRefSkuEntity> listBySkuId(String skuId) {
        LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskRefSkuEntity::getSkuId,skuId);
        return this.list(queryWrapper);
    }


    public void setTaskNoFinishRefSku(String taskId) {
        LambdaUpdateWrapper<ProjectTaskRefSkuEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProjectTaskRefSkuEntity::getTaskId, taskId);
        updateWrapper.set(ProjectTaskRefSkuEntity::getIsFinishTask, 0);
        this.update(updateWrapper);
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
    /**
     * @description: 根据skuId和任务id查询关联信息
     * @author Will
     * @date: 2023/2/7 16:42
     * @param skuId
     * @param taskId
     * @return ProjectTaskRefSkuEntity
     */
    public ProjectTaskRefSkuEntity getBySkuIdAndTaskId(String skuId,String taskId) {
        LambdaQueryWrapper<ProjectTaskRefSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectTaskRefSkuEntity::getSkuId, skuId);
        queryWrapper.eq(ProjectTaskRefSkuEntity::getTaskId, taskId);
        queryWrapper.last("limit 1");
       return this.getOne(queryWrapper);
    }
}
