package com.erp.server.plm.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ApplicationCategoryDTO;
import com.erp.server.plm.service.ApplicationCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品应用分类
 *
 * @author liaohui
 * @since 2025-01-09
 */
@Slf4j
@RestController
@LogSystemModule("产品应用分类")
@RequestMapping("/applicationCategory")
public class ApplicationCategoryController extends BaseController {

    @Resource
    private ApplicationCategoryService applicationCategoryService;

    /**
    * 新增
    * @author liaohui
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "产品应用分类新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ApplicationCategoryDTO.AddDTO dto) {
        return success(applicationCategoryService.add(dto));
    }

    /**
    * 修改
    * @author liaohui
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "产品应用分类修改")
    public ApiResult<String> update(@RequestBody @Validated ApplicationCategoryDTO.UpdateDTO dto) {
        applicationCategoryService.update(dto);
        return success();
    }


    /**
     * 删除
     * @author liaohui
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "产品应用分类删除")
    public ApiResult<String> delete(@RequestBody BaseIdDTO dto) {
        applicationCategoryService.delete(dto.getId());
        return success();
    }


    /**
     * 列表
     * @param searchKeyword 筛选条件
     */
    @GetMapping("/list")
    public ApiResult<List<ApplicationCategoryDTO.ViewDTO>> list(@RequestParam(required = false) String searchKeyword) {
        List<ApplicationCategoryDTO.ViewDTO> viewDTOS = applicationCategoryService.list(searchKeyword);
        return success(viewDTOS);
    }


}
