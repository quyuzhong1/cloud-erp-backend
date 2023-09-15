package com.erp.server.bi.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiBatchShareDTO;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.model.bi.entity.BiSubjectEntity;
import com.erp.server.bi.service.BiModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 模块表(BiModule)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Slf4j
@RestController
@RequestMapping("module")
@Validated
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
    public ApiResult add(@Validated @ModelAttribute  ModuleDTO dto) {
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


    /**
     * 模板批量设置权限
     * @author Jim
     * @date: 2023-09-14
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchShare")
    public ApiResult<List<BatchResultDTO>> batchShare(@RequestBody @Validated BiBatchShareDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = biModuleService.updateShare(dto.getShareFlagIdList(), id, dto.getShareFlag());
            }catch (Exception e){
                log.error("模板批量设置权限失败:{}", e.getMessage());
                BiModuleEntity entity = biModuleService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "专题不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), "", e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}

