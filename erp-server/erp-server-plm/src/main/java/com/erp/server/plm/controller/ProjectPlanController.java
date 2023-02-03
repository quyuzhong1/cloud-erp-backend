package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.entity.ProjectPlanEntity;
import com.erp.server.plm.service.ProjectPlanService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 产品排期
 *
 * @author yl
 * @since 2023-02-03 15:14:29
 */
@RestController
@RequestMapping("projectPlan")
public class ProjectPlanController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private ProjectPlanService projectPlanService;

    /**
     * 项目计划
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ProjectPlanEntity>> queryByPage() {
        return success(this.projectPlanService.queryByPage());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResult<ProjectPlanEntity> queryById(@PathVariable("id") String id) {
        return success(this.projectPlanService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param projectPlan 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(ProjectPlanEntity projectPlan) {
        Boolean flag=this.projectPlanService.insert(projectPlan);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param projectPlan 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(ProjectPlanEntity projectPlan) {
         Boolean flag=this.projectPlanService.update(projectPlan);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @param
     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.projectPlanService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

