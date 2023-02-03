package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.server.plm.mapper.ProjectPlanMapper;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 项目计划表(ProjectPlan)表服务实现类
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@Service("projectPlanService")
public class ProjectPlanServiceImpl extends ServiceImpl<ProjectPlanMapper, ProjectPlanEntity> implements ProjectPlanService {

    @Resource
    private ProjectPlanMapper projectPlanMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ProjectPlanEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<ProjectPlanEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param projectPlan 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(ProjectPlanEntity projectPlan) {
        return true;
    }

    /**
     * 修改数据
     *
     * @param projectPlan 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(ProjectPlanEntity projectPlan) {
        return true;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Boolean deleteById(String id) {
        return true;
    }
}
