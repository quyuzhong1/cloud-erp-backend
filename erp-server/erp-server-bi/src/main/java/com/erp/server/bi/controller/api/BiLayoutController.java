package com.erp.server.bi.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.AddTotalSubjectDTO;
import com.erp.model.bi.dto.DeleteLayoutModuleDTO;
import com.erp.model.bi.dto.SubjectLayoutDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.server.bi.service.BiLayoutService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author yl
 * @since 2022-12-08 14:28:26
 */
@RestController
@LogSystemModule("专题管理")
@RequestMapping("layout")
public class BiLayoutController extends BaseController {


    @Resource
    private BiLayoutService layoutService;


    /**
     * 添加整个专题
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加整个专题")
    @PostMapping("/addSubject")
    public ApiResult<String> addSubjectLayout(@RequestBody @Validated AddTotalSubjectDTO dto) {
        String subjectId = layoutService.addSubject(dto);
        if (StringUtils.isBlank(subjectId)) {
            return failure();
        }
        return success(subjectId);
    }


    /**
     * 添加专题布局
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加专题布局")
    @PostMapping("/addSubjectLayout")
    public ApiResult<Object> addSubjectLayout(@RequestBody @Validated SubjectLayoutDTO dto) {
        boolean flag = layoutService.addSubjectLayout(dto);
        return flag ? success() : failure();
    }


    /**
     * 修改专题布局
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改专题布局")
    @PostMapping("/updateSubjectLayout")
    public ApiResult<String> updateSubjectLayout(@RequestBody @Validated SubjectLayoutDetailsDTO dto) {
        String subjectId = layoutService.updateSubjectLayout(dto);
        if(StringUtils.isBlank(subjectId)){
           return  failure();
        }
        return success(subjectId);
    }


    /**
     * 编辑专题布局获取详情
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @LogViewService
    @PostMapping("/subjectInfo")
    public ApiResult<SubjectLayoutDetailsDTO> subjectInfo(@RequestBody @Validated BaseIdDTO dto) {
        SubjectLayoutDetailsDTO details = layoutService.subjectInfo(dto.getId());
        return success(details);
    }


    /**
     * 删除布局模块
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除布局模块")
    @PostMapping("/deleteLayoutModule")
    public ApiResult<Object> deleteLayoutModule(@RequestBody @Validated DeleteLayoutModuleDTO dto) {
        boolean flag = layoutService.deleteLayoutModule(dto);
        return flag ? success() : failure();
    }

    /**
     * 删除布局
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除布局")
    @PostMapping("/deleteLayout")
    public ApiResult<Object> deleteLayout(@RequestBody @Validated DeleteLayoutModuleDTO dto) {
        boolean flag = layoutService.deleteLayout(dto);
        return flag ? success() : failure();
    }


}

