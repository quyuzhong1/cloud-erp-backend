package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.server.bi.service.BiModuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 模块表(BiModule)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@RestController
@RequestMapping("biModule")
public class BiModuleController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiModuleService biModuleService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiModuleEntity>> queryByPage() {
        return success(this.biModuleService.queryByPage());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResult<BiModuleEntity> queryById(@PathVariable("id") String id) {
        return success(this.biModuleService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param biModule 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(BiModuleEntity biModule) {
        Boolean flag=this.biModuleService.insert(biModule);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biModule 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(BiModuleEntity biModule) {
         Boolean flag=this.biModuleService.update(biModule);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.biModuleService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

