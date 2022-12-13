package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.bi.dto.SubjectLayoutDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.server.bi.service.BiLayoutService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 布局表(BiLayout)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:28:26
 */
@RestController
@RequestMapping("bi/layout")
public class BiLayoutController extends BaseController {


    @Resource
    private BiLayoutService layoutService;

    @PostMapping("/addSubjectLayout")
    public ApiResult addSubjectLayout(@RequestBody @Validated SubjectLayoutDTO dto) {
        Boolean flag = layoutService.addSubjectLayout(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 编辑专题布局获取详情
     *
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @PostMapping("/subjectInfo")
    public ApiResult<SubjectLayoutDetailsDTO> subjectInfo(@RequestBody @Validated BaseIdDTO dto) {
        SubjectLayoutDetailsDTO details = layoutService.subjectInfo(dto.getId());
        return success(details);
    }


}

