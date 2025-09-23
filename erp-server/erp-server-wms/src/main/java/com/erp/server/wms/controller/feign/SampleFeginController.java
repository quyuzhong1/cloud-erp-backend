package com.erp.server.wms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
import com.erp.server.wms.query.SampleBorrowInfoQueryHandler;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 样品管理
 *
 * @author jack
 * @since 2025-09-10
 */
@Slf4j
@RestController
@LogSystemModule("样品管理")
@RequestMapping("/feign/sample")
public class SampleFeginController extends BaseController {

    @Resource
    private SampleBorrowInfoService sampleBorrowInfoService;
    @Resource
    private SampleReturnInfoService sampleReturnInfoService;
    @Resource
    private SampleScrapInfoService sampleScrapInfoService;
    @Resource
    private SampleRecipientService sampleRecipientService;
    @Resource
    private SampleBackInfoService sampleBackInfoService;


    //--------------------------------- 样品借用 ---------------------------------//
    //--------------------------------- 样品归还 ---------------------------------//
    //--------------------------------- 样品报废 ---------------------------------//
    //--------------------------------- 样品领用 ---------------------------------//
    //--------------------------------- 样品退回 ---------------------------------//


}
