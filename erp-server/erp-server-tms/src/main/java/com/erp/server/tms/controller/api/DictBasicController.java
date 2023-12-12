package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.DictBasicService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.DictBasicDTO;

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

    @Autowired
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.AddOrUpdateDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }



}
