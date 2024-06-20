package com.erp.server.wms.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.server.wms.service.PickingCartTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 拣货车类型
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@RestController
@LogSystemModule("拣货车类型")
@RequestMapping("/pickingCartType")
public class PickingCartTypeController extends BaseController {

    @Resource
    private PickingCartTypeService pickingCartTypeService;

    /**
    * 编辑
    * @author will
    * @date:  2024-06-20
    * @param list
    * @return ApiResult
    */
    @PostMapping("/batchUpdate")
    public ApiResult<?> batchUpdate(@RequestBody @Validated List<PickingCartTypeDTO.batchUpdateDTO> list) {
        pickingCartTypeService.batchUpdate(list);
        return success();
    }

    /**
     * 拣货车类型
     * @author will
     * @date 2024/6/20 18:22
     * @param selectDTO
     * @return ApiResult<ListDTO>
     */
    @PostMapping("/select")
    public ApiResult<List<PickingCartTypeDTO.ListDTO>> select(@RequestBody @Validated PickingCartTypeDTO.SelectDTO selectDTO) {
        List<PickingCartTypeDTO.ListDTO> list = pickingCartTypeService.select(selectDTO);
        return success(list);
    }

}
