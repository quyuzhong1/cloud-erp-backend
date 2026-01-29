package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.AsyncTaskDetailRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.AsyncTaskDetailRecordDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;

/**
 * 异步任务记录明细
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@RestController
@LogSystemModule("异步任务记录明细")
@RequestMapping("/asyncTaskDetailRecord")
public class AsyncTaskDetailRecordController extends BaseController {

    @Resource
    private AsyncTaskDetailRecordService asyncTaskDetailRecordService;


}
