package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.exception.ServiceException;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.BatchTaskPhaseDTO;
import com.erp.model.plm.dto.TaskPhaseDTO;
import com.erp.model.plm.entity.ProjectPhaseEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.ProjectPhaseMapper;
import com.erp.server.plm.service.ProjectPhaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.service.SysTaskPhaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务阶段表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class ProjectPhaseServiceImpl extends ServiceImpl<ProjectPhaseMapper, ProjectPhaseEntity> implements ProjectPhaseService {


    @Autowired
    private SysTaskPhaseService sysTaskPhaseService;

    /**
     * 获取 产品任务的阶段名
     *
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     * @author yl
     * @date 2022-09-13 17:39
     */
    @Override
    public List<TaskPhaseDTO> findList(BasicProductIdDTO dto) {
        List<TaskPhaseDTO> resultList = new ArrayList<>();
        //先从系统里面取
        List<TaskPhaseDTO> sysList = sysTaskPhaseService.getSysTaskPhase();
        if (CollectionUtils.isNotEmpty(sysList)) {
            resultList.addAll(sysList);
        }
        String productId = dto.getProductId();
        //根据产品id 获取到对应的阶段名
        List<TaskPhaseDTO> productList = getTaskPhaseByProductId(productId);
        if (CollectionUtils.isNotEmpty(productList)) {
            resultList.addAll(productList);
        }
        return resultList;
    }

    /**
     * 批量保存或者修改任务阶段
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-09-14 16:52
     */
    @Override
    public void batchSaveOrUpdate(BatchTaskPhaseDTO dto) {
        List<TaskPhaseDTO> list = dto.getTaskPhases();
        String productId = dto.getProductId();
        //获取到任务阶段的
        List<TaskPhaseDTO> taskPhaseList = list.stream().filter(t -> IsConstant.NO == t.getIsSys()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskPhaseList)) {
            //获取不是系统的阶段名 那就是产品的阶段名
            List<String> phaseNames = taskPhaseList.stream().map(TaskPhaseDTO::getName).collect(Collectors.toList());
            //获取产品加系统的阶段名
            List<String> dbPhaseNames = getDbTaskPhaseNames(productId);
            //获取交集
            List<String> intersections = phaseNames.stream().filter(item -> dbPhaseNames.contains(item)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(intersections)) {
                String intersectionName = String.join(",", intersections);
                throw new ServiceException(1, intersectionName + " 阶段名已存在,不可重复提交");
            }
            List<ProjectPhaseEntity> updateList = new LinkedList<>();
            for (TaskPhaseDTO item : taskPhaseList) {
                ProjectPhaseEntity entity = new ProjectPhaseEntity();
                entity.setId(item.getId());
                entity.setName(item.getName());
                entity.setProductId(productId);
                updateList.add(entity);
            }
            this.saveOrUpdateBatch(updateList);
        }
    }

    /**
     * 保存任务阶段
     *
     * @param flagId
     * @param productId
     * @return void
     * @author yl
     * @date 2022-09-20 15:10
     */
    @Override
    public void savePhase(String flagId, String productId) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        List<ProjectPhaseEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            for (ProjectPhaseEntity item : list) {
                item.setProductId(flagId);
            }
            this.saveBatch(list);
        }

    }


    /**
     * 根据产品id 获取 产品下任务阶段名 然后在加上 系统的任务阶段名
     *
     * @param productId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-09-14 17:53
     */
    private List<String> getDbTaskPhaseNames(String productId) {
        LambdaQueryWrapper<ProjectPhaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ProjectPhaseEntity::getName);
        queryWrapper.eq(ProjectPhaseEntity::getProductId, productId);
        List<String> list = this.listObjs(queryWrapper, Object::toString);
        List<String> sysList = sysTaskPhaseService.getSysTaskPhaseNames();
        List<String> results = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            results.addAll(list);
        }
        if (CollectionUtils.isNotEmpty(sysList)) {
            results.addAll(sysList);
        }
        return results;
    }


    /**
     * 根据产品id 获取到 产品任务阶段名
     *
     * @param productId
     * @return java.util.List<com.erp.model.plm.dto.TaskPhaseDTO>
     * @author yl
     * @date 2022-09-13 18:00
     */

    private List<TaskPhaseDTO> getTaskPhaseByProductId(String productId) {

        return baseMapper.getTaskPhaseByProductId(productId);
    }
}
