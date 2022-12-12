package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutEntity;
import com.erp.server.bi.service.BiLayoutService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 布局表(BiLayout)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:28:26
 */
@RestController
@RequestMapping("biLayout")
public class BiLayoutController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiLayoutService biLayoutService;

    /**
     * 分页查询
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiLayoutEntity>> queryByPage() {
        return success(this.biLayoutService.queryByPage());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResult<BiLayoutEntity> queryById(@PathVariable("id") String id) {
        return success(this.biLayoutService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param biLayout 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(BiLayoutEntity biLayout) {
        Boolean flag=this.biLayoutService.insert(biLayout);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biLayout 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(BiLayoutEntity biLayout) {
         Boolean flag=this.biLayoutService.update(biLayout);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *

     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.biLayoutService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

