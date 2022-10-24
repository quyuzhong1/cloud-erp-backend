package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.BasicProductIdDTO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.DocsNameDTO;
import com.erp.server.plm.service.TaskDocsNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  产品开发管理
 * @Classname TaskDocsNameController
 * @Description TODO
 * @Date 2022-09-22 12:12
 * @Created by yl
 */
@RestController
@RequestMapping("/plm/taskName")
public class TaskDocsNameController extends BaseController {

    @Autowired
    private TaskDocsNameService taskDocsNameService;


    @PostMapping("/save")
    //@RequestPermissions("plm:taskName:save")
    public ApiResult saveDocsName(@RequestBody @Validated DocsNameDTO dto) {
        Boolean flag = taskDocsNameService.saveDocsName(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新建任务-设置目标交付文档-获取交付文档名称
     * @param dto
     * @return
     */
    @GetMapping("/list")
    //@RequestPermissions("plm:taskName:list")
    public ApiResult list(@RequestBody @Validated BasicProductIdDTO dto) {
        List<DocsDTO> list = taskDocsNameService.getDocsNameList(dto.getProductId());
        return success(list);
    }
}
