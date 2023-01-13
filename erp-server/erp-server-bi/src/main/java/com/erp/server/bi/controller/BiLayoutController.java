package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.enums.DataAttributeEnum;
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
@RequestMapping("bi/layout")
public class BiLayoutController extends BaseController {


    @Resource
    private BiLayoutService layoutService;


    /**
     * 添加整个专题
     *
     * @param dto
     * @return
     */
    @PostMapping("/addSubject")
    public ApiResult addSubjectLayout(@RequestBody @Validated AddTotalSubjectDTO dto) {
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
    @PostMapping("/addSubjectLayout")
    public ApiResult addSubjectLayout(@RequestBody @Validated SubjectLayoutDTO dto) {
        Boolean flag = layoutService.addSubjectLayout(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 修改专题布局
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateSubjectLayout")
    public ApiResult updateSubjectLayout(@RequestBody @Validated SubjectLayoutDetailsDTO dto) {
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
     * @return com.erp.common.dto.base.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @PostMapping("/subjectInfo")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "bi:layout:subjectInfo",
            serviceClass = BiLayoutService.class
    )
    public ApiResult<SubjectLayoutDetailsDTO> subjectInfo(@RequestBody @Validated BaseIdDTO dto) {
        SubjectLayoutDetailsDTO details = layoutService.subjectInfo(dto.getId());
        return success(details);
    }


    /**
     * 删除布局模块
     *
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @PostMapping("/deleteLayoutModule")
    public ApiResult deleteLayoutModule(@RequestBody @Validated DeleteLayoutModuleDTO dto) {
        Boolean flag = layoutService.deleteLayoutModule(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除布局
     *
     * @param dto
     * @return com.erp.common.dto.base.ApiResult
     * @author yl
     * @date 2022-12-13 17:06
     */
    @PostMapping("/deleteLayout")
    public ApiResult deleteLayout(@RequestBody @Validated DeleteLayoutModuleDTO dto) {
        Boolean flag = layoutService.deleteLayout(dto);
        return flag == true ? success() : failure();
    }


}

