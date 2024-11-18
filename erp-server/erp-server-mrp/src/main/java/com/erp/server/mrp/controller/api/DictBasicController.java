package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.DictBasicDTO;
import com.erp.server.mrp.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("字典表")
@RequestMapping("/dictBasic")
public class DictBasicController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult<String> saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.AddOrUpdateDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }



    @GetMapping("/treeByType")
    public ApiResult<List<DictBasicDTO.TreeDTO>> treeByType(@RequestParam String type) {
        List<DictBasicDTO.TreeDTO> dtos = dictBasicService.treeByType(type);
        return success(dtos);
    }

}
