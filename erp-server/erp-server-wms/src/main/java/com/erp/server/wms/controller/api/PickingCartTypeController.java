package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.server.wms.service.PickingCartTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public ApiResult<?> batchUpdate(@RequestBody @Validated List<PickingCartTypeDTO.BatchUpdateDTO> list) {
        pickingCartTypeService.batchUpdate(list);
        return success();
    }

    /**
     * 删除拣货车类型
     * @author will
     * @date 2024/6/24 10:42
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "拣货车类型删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = pickingCartTypeService.delete(id);
            }catch (Exception e){
                log.error("拣货车类型删除失败",e);
                PickingCartTypeEntity entity = pickingCartTypeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "拣货车类型不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 拣货车类型下拉
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

    /**
     * 验证是否被用,true被用，false未被用
     * @author will
     * @date 2024/6/25 16:39
     * @param baseIdDTO
     * @return ApiResult<Boolean>
     */
    @PostMapping("/checkIsUsed")
    public ApiResult<Boolean> checkIsUsed(@RequestBody @Validated BaseIdDTO baseIdDTO) {
        Boolean isUsed = pickingCartTypeService.checkIsUsed(baseIdDTO.getId());
        return success(isUsed);
    }
}
