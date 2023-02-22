package com.erp.server.bi.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.dto.CopySubjectDTO;
import com.erp.model.bi.dto.MyDashboardDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.server.bi.service.BiSubjectDefaultService;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 仪表盘
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
@RestController
@RequestMapping("bi/dashboard")
public class BiDashboardController extends BaseController {


    @Resource
    private BiSubjectService subjectService;

    @Resource
    private BiSubjectDefaultService subjectDefaultService;


    @Resource
    private CommonService commonService;


    /**
     * 新增仪表盘
     *
     * @param dto 实体
     * @return 新增结果
     */
    @PostMapping("/add")
    public ApiResult<String> add(@RequestBody @Validated SubjectDTO dto) {
        String id = subjectService.addDashboard(dto);
        if (StringUtils.isNotBlank(id)) {
            return success(id);
        }
        return failure();
    }


    /**
     * 获取默认的仪表盘
     */
    @GetMapping("/info")
    public ApiResult<SubjectLayoutDetailsDTO> Info() {
        SubjectLayoutDetailsDTO details = subjectService.dashboardInfo();
        if (Objects.isNull(details)) {
            throw new ServiceException(ApiError.ERROR_97019);
        }
        return success(details);
    }

    /**
     * 设置仪表盘默认
     *
     * @return 查询结果
     */
    @PostMapping("/setDefault")
    public ApiResult setShare(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = subjectDefaultService.setDefault(dto.getId());
        return flag == true ? success() : failure();
    }


    /**
     * 我的仪表盘
     *
     * @return 查询结果
     */
    @GetMapping("/my/list")
    public ApiResult<MyDashboardDTO> setShare(String searchKeyword) {
        String userId = commonService.getUserInfo().getUid();
        MyDashboardDTO myDashboard = subjectService.myDashboard(userId, searchKeyword);
        return success(myDashboard);
    }

    /**
     * 复制仪表盘
     */
    @PostMapping("/copy")
    public ApiResult copy(@RequestBody @Validated CopySubjectDTO dto) {
        String id = subjectService.copyDashboard(dto);
        if (StringUtils.isBlank(id)) {
            return failure();
        }
        return success(id);
    }

}

