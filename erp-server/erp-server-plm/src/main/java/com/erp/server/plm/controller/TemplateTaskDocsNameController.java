package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TmeplateDocsNameDTO;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/16 12:23
 */
@RestController
@RequestMapping("/plm/templateTaskName")
public class TemplateTaskDocsNameController extends BaseController {

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;

    /**
     * 保存交付文件名称
     *
     * @author Will
     * @date: 2022/11/16 13:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/save")
    public ApiResult saveDocsName(@RequestBody @Validated TmeplateDocsNameDTO dto) {
        Boolean flag = templateTaskDocsNameService.saveDocsName(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询交付文件名称
     *
     * @author Will
     * @date: 2022/11/16 13:02
     * @param templateId
     * @return ApiResult<List<DocsDTO>>
     */
    @GetMapping("/list")
    public ApiResult<List<DocsDTO>> list(@RequestParam(value = "templateId") String templateId) {
        List<DocsDTO> list = templateTaskDocsNameService.getDocsNameList(templateId);
        return success(list);
    }

}
