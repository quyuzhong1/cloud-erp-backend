package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import com.erp.server.bi.service.BiLayoutRefModuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 布局与模块关系表(BiLayoutRefModule)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
@RestController
@RequestMapping("biLayoutRefModule")
public class BiLayoutRefModuleController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiLayoutRefModuleService biLayoutRefModuleService;

    /**
     * 分页查询

     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiLayoutRefModuleEntity>> queryByPage() {
        return success(this.biLayoutRefModuleService.queryByPage());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResult<BiLayoutRefModuleEntity> queryById(@PathVariable("id") String id) {
        return success(this.biLayoutRefModuleService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param biLayoutRefModule 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(BiLayoutRefModuleEntity biLayoutRefModule) {
        Boolean flag=this.biLayoutRefModuleService.insert(biLayoutRefModule);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biLayoutRefModule 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(BiLayoutRefModuleEntity biLayoutRefModule) {
         Boolean flag=this.biLayoutRefModuleService.update(biLayoutRefModule);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.biLayoutRefModuleService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

