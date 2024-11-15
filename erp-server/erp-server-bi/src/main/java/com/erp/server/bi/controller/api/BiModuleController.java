package com.erp.server.bi.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.BiBatchShareDTO;
import com.erp.model.bi.dto.CategoryModuleDTO;
import com.erp.model.bi.dto.ModuleDTO;
import com.erp.model.bi.dto.ModulePagingDTO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.server.bi.service.BiModuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 模块管理
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Slf4j
@RestController
@LogSystemModule("模块管理")
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增模块")
    @PostMapping("/add")
    public ApiResult<Void> add(@Validated @ModelAttribute  ModuleDTO dto) {
        boolean flag = this.biModuleService.insert(dto);
        return flag ? success() : failure();
    }

    /**
     * 模块详情
     *
     * @param dto 实体
     * @return 新增结果
     */
    @LogViewService
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新模块")
    @PostMapping("/update")
    public ApiResult<Void> edit(@ModelAttribute @Validated(value = {UpdateGroup.class}) ModuleDTO dto) {
        boolean flag = this.biModuleService.update(dto);
        return flag ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @return 删除是否成功
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除模块")
    @PostMapping("/delete")
    public ApiResult<Void> deleteById(@RequestBody @Validated BaseIdDTO dto) {
        boolean flag = this.biModuleService.deleteById(dto.getId());
        return flag ? success() : failure();
    }


    /**
     * 设置模板状态
     *
     * @return 删除是否成功
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置模板状态:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateState")
    public ApiResult<Void> updateState(@RequestBody @Validated UpdateStateDTO dto) {
        boolean flag = this.biModuleService.updateState(dto);
        return flag ? success() : failure();
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
     * @param dtoList
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "模板批量设置权限:id={id}, 分享标示={shareFlag},分享的身份id列表={shareFlagIdList}")
    @PostMapping("/batchShare")
    public ApiResult<List<BatchResultDTO>> batchShare(@RequestBody @Validated List<BiBatchShareDTO> dtoList) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (BiBatchShareDTO dto : dtoList) {
            BatchResultDTO submit;
            try {
                submit = biModuleService.updateShare(dto.checkAndGetShareFlagIdList(), dto.getId(), dto.getShareFlag());
            }catch (Exception e){
                log.error("模板批量设置权限失败:{}", e.getMessage());
                BiModuleEntity entity = biModuleService.getById(dto.getId());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(dto.getId(), dto.getId(), "专题不存在, 提交失败");
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

