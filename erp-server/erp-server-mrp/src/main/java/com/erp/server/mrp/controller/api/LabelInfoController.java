package com.erp.server.mrp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.LabelInfoDTO;
import com.erp.model.mrp.entity.LabelInfoEntity;
import com.erp.server.mrp.service.LabelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签信息表
 *
 * @author will
 * @since 2024-08-30
 */
@Slf4j
@RestController
@LogSystemModule("标签信息表")
@RequestMapping("/labelInfo")
public class LabelInfoController extends BaseController {

    @Resource
    private LabelInfoService labelInfoService;

    /**
     * 列表查询
     * @author will
     * @date 2024/9/4 16:35
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @GetMapping("/listLabelInfo")
    public ApiResult<List<LabelInfoDTO.ListDTO>> listLabelInfo() {
        return success(labelInfoService.listLabelInfo());
    }

    /**
    * 新增
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "标签信息表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LabelInfoDTO.AddDTO dto) {
        return success(labelInfoService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-30
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    public ApiResult<?> update(@RequestBody @Validated LabelInfoDTO.UpdateDTO dto) {
        labelInfoService.update(dto);
        return success();
    }


    /**
     * 批量删除
     * @author will
     * @date 2024/8/30 14:25
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "删除标签管理")
    public ApiResult<?> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = labelInfoService.delete(id);
            }catch (Exception e){
                log.error("删除标签管理",e);
                LabelInfoEntity entity = labelInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "标签管理不存在, 删除标签管理失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启禁用变更
     * @author will
     * @date 2024/8/30 14:34
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateDisabled")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启禁用变更")
    public ApiResult<?> updateDisabled(@RequestBody @Validated LabelInfoDTO.UpdateDisabledDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = labelInfoService.updateDisabled(id,dto.getDisabled());
            }catch (Exception e){
                log.error("启禁用变更",e);
                LabelInfoEntity entity = labelInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "标签管理不存在, 启禁用变更失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
