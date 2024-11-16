package com.erp.server.bi.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.threadlocal.UserContext;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.dto.CopySubjectDTO;
import com.erp.model.bi.dto.MyDashboardDTO;
import com.erp.model.bi.dto.SubjectDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.server.bi.service.BiSubjectDefaultService;
import com.erp.server.bi.service.BiSubjectService;
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
@LogSystemModule("我的仪表盘")
@RequestMapping("dashboard")
public class BiDashboardController extends BaseController {


    @Resource
    private BiSubjectService subjectService;

    @Resource
    private BiSubjectDefaultService subjectDefaultService;


    /**
     * 新增仪表盘
     *
     * @param dto 实体
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仪表盘")
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
    public ApiResult<SubjectLayoutDetailsDTO> info() {
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
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置仪表盘默认,id={id}")
    @PostMapping("/setDefault")
    public ApiResult<Object> setShare(@RequestBody @Validated BaseIdDTO dto) {
        boolean flag = subjectDefaultService.setDefault(dto.getId());
        return flag ? success() : failure();
    }


    /**
     * 我的仪表盘
     *
     * @return 查询结果
     */
    @GetMapping("/my/list")
    public ApiResult<MyDashboardDTO> setShare(String searchKeyword) {
        String userId = UserContext.getDefaultLoginUser().getUid();
        MyDashboardDTO myDashboard = subjectService.myDashboard(userId, searchKeyword);
        return success(myDashboard);
    }

    /**
     * 复制仪表盘
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "复制仪表盘:专题id={subjectId}")
    @PostMapping("/copy")
    public ApiResult<String> copy(@RequestBody @Validated CopySubjectDTO dto) {
        String id = subjectService.copyDashboard(dto);
        if (StringUtils.isBlank(id)) {
            return failure();
        }
        return success(id);
    }

}

