package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.entity.ProjectPlanEntity;

/**
 * 项目计划表(ProjectPlan)表服务接口
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
public interface ProjectPlanService  extends IService<ProjectPlanEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ProjectPlanEntity queryById(String id);

    /**
     * 分页查询
     *
     * @param projectPlan 筛选条件
     * @param pageRequest      分页对象
     * @return 查询结果
     */
    PagingVO<ProjectPlanEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param projectPlan 实例对象
     * @return 实例对象
     */
    Boolean insert(ProjectPlanEntity projectPlan);

    /**
     * 修改数据
     *
     * @param projectPlan 实例对象
     * @return 实例对象
     */
    Boolean update(ProjectPlanEntity projectPlan);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

}
