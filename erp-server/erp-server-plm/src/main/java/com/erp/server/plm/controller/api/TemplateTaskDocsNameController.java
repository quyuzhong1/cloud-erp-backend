package com.erp.server.plm.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.TmeplateDocsNameDTO;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import org.apache.ibatis.annotations.Param;
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
@LogSystemModule("系统通用设置")
@RequestMapping("templateTaskName")
public class TemplateTaskDocsNameController extends BaseController {

    @Autowired
    private TemplateTaskDocsNameService templateTaskDocsNameService;


    /**
     * 模板详情-输出物-新增文档
     *
     * @author Will
     * @date: 2022/11/16 13:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "模板详情-输出物-新增文档")
    @PostMapping("/save")
    public ApiResult saveDocsName(@RequestBody @Validated TmeplateDocsNameDTO dto) {
        Boolean flag = templateTaskDocsNameService.saveDocsName(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 模板详情-输出物-编辑文档
     *
     * @author Will
     * @date: 2022/11/17 13:02
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "模板详情-输出物-编辑文档", keyIdName = "deliveryDocsId")
    @PutMapping("/update")
    public ApiResult updateDocsName(@RequestBody @Validated TmeplateDocsNameDTO dto) {
        Boolean flag = templateTaskDocsNameService.updateDocsName(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 模板详情-输出物-交付文档名称下拉数据
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

    /**
     * 文档详情
     * @author Jim
     * @date: 2023/10/25
     * @param deliveryDocsId String
     * @return ApiResult<DocsDTO>
     */
    @LogViewService
    @GetMapping("/details")
    public ApiResult<DocsDTO> view(@Param("deliveryDocsId") String deliveryDocsId) {
        DocsDTO result = templateTaskDocsNameService.view(deliveryDocsId);
        return success(result);
    }
}
