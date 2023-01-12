package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.*;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.common.modules.validator.UpdateGroup;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.server.bi.service.BiLayoutService;
import com.erp.server.bi.service.BiModuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 模块表(BiModule)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@RestController
@RequestMapping("bi/module")
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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "bi:module:paging",
            tableAlias = "bm1"
    )
    public ApiResult<PagingVO<ModulePagingDTO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<ModulePagingDTO> pagingVO = biModuleService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增模块
     *
     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult add(@ModelAttribute @Validated ModuleDTO dto) {
        Boolean flag = this.biModuleService.insert(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新增模块
     *
     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/details")
    public ApiResult<ModuleDTO> add(@RequestBody @Validated BaseIdDTO dto) {
        ModuleDTO result = this.biModuleService.details(dto.getId());
        return success(result);
    }


    /**
     * 编辑数据
     *
     * @param dto 实体
     * @return 编辑结果
     */
    @PostMapping("/update")
    public ApiResult edit(@ModelAttribute @Validated(value = {UpdateGroup.class}) ModuleDTO dto) {
        Boolean flag = this.biModuleService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = this.biModuleService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }


    /**
     * 设置模板状态
     *
     * @return 删除是否成功
     */
    @PostMapping("/updateState")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "bi:module:updateState",
            serviceClass = BiLayoutService.class
    )
    public ApiResult updateState(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean flag = this.biModuleService.updateState(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 布局添加模块
     * 模块列表
     *
     * @return
     */
    @PostMapping("/category/list")
    public ApiResult<List<CategoryModuleDTO>> categoryList(@RequestBody @Validated BaseSearchDTO dto) {
        List<CategoryModuleDTO> list = biModuleService.categoryList(dto.getSearchKeyword());
        return success(list);
    }

}

