package com.erp.server.sys.controller.api;


import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.sys.service.FileTemplateService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;

/**
 * 文件模板url表
 *
 * @author wangwei
 * @since 2023-12-25
 */
@Slf4j
@RestController
@LogSystemModule("文件模板url表")
@RequestMapping("/fileTemplate")
public class FileTemplateController extends BaseController {

    @Resource
    private FileTemplateService fileTemplateService;

    /**
    * 新增
    * @author wangwei
    * @date:  2023-12-25
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "文件模板url表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FileTemplateDTO.AddDTO dto) {
        return success(fileTemplateService.add(dto));
    }

    /**
    * 修改
    * @author wangwei
    * @date:  2023-12-25
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "文件模板url表修改")
    public ApiResult<?> update(@RequestBody @Validated FileTemplateDTO.UpdateDTO dto) {
        fileTemplateService.update(dto);
        return success();
    }


    /**
     * fdfs文件模板新增或修改
     * @author Will
     * @date: 2023/12/25 14:47
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/fastdfsAddOrUpdate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "fdfs文件模板url表新增或修改")
    public ApiResult<?> fastdfsAddOrUpdate(@Validated @ModelAttribute FileTemplateDTO.FastdfsAddOrUpdateDTO dto) {
        fileTemplateService.fastdfsAddOrUpdate(dto);
        return success();
    }
}
