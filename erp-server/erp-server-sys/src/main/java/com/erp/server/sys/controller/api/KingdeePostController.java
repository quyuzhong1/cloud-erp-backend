package com.erp.server.sys.controller.api;


import com.erp.model.sys.dto.KingdeeDepartmentDTO;
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
import com.erp.server.sys.service.KingdeePostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeePostDTO;

/**
 * 金蝶架构管理-岗位分配
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶岗位表")
@RequestMapping("/kingdeePost")
public class KingdeePostController extends BaseController {

    @Resource
    private KingdeePostService kingdeePostService;





    /**
     * 初始化金蝶数据
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeePostService.init();
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
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶岗位表新增")
    public ApiResult add(@RequestBody @Validated KingdeePostDTO.AddDTO dto) {
        Boolean result = kingdeePostService.add(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<KingdeePostDTO.ViewDTO> view(@Param("id") String id) {
        return success(kingdeePostService.view(id));
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated KingdeePostDTO.UpdateDTO dto) {
        Boolean result = kingdeePostService.update(dto);
        return result ? success() : failure();
    }



}
