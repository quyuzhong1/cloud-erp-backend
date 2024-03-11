package com.erp.server.sys.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;

/**
 * 
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶部门")
@RequestMapping("/kingdeeDepartment")
public class KingdeeDepartmentController extends BaseController {

    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;


    /**
     * 初始化金蝶数据
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeeDepartmentService.init();
        return result ? success() : failure();
    }


    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KingdeeDepartmentDTO.AddDTO dto) {
        return success(kingdeeDepartmentService.add(dto));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult<?> update(@RequestBody @Validated KingdeeDepartmentDTO.UpdateDTO dto) {
        kingdeeDepartmentService.update(dto);
        return success();
    }


    /**
     * 详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<KingdeeDepartmentDTO.ViewDTO> view(@Param("id") String id) {
        return success(kingdeeDepartmentService.view(id));
    }



}
