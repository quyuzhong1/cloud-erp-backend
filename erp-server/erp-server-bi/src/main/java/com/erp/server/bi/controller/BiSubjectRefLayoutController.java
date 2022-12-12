package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiSubjectRefLayoutEntity;
import com.erp.server.bi.service.BiSubjectRefLayoutService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 专题与布局关系表(BiSubjectRefLayout)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:32:40
 */
@RestController
@RequestMapping("biSubjectRefLayout")
public class BiSubjectRefLayoutController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiSubjectRefLayoutService biSubjectRefLayoutService;

    /**
     * 分页查询
     *
     * @return 查询结果
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<BiSubjectRefLayoutEntity>> queryByPage() {
        return success(this.biSubjectRefLayoutService.queryByPage());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ApiResult<BiSubjectRefLayoutEntity> queryById(@PathVariable("id") Integer id) {
        return success(this.biSubjectRefLayoutService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param biSubjectRefLayout 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(BiSubjectRefLayoutEntity biSubjectRefLayout) {
        Boolean flag=this.biSubjectRefLayoutService.insert(biSubjectRefLayout);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biSubjectRefLayout 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(BiSubjectRefLayoutEntity biSubjectRefLayout) {
         Boolean flag=this.biSubjectRefLayoutService.update(biSubjectRefLayout);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     * @return 删除是否成功
     */
     @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag=this.biSubjectRefLayoutService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

