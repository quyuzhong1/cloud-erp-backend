package com.erp.server.oms.controller.api;


import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
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
import com.erp.server.oms.service.SoMultiChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import com.erp.model.oms.entity.SoMultiChannelEntity;

/**
 * 多渠道订单
 *
 * @author Jim
 * @since 2024-05-30
 */
@Slf4j
@RestController
@LogSystemModule("多渠道订单")
@RequestMapping("/soMultiChannel")
public class SoMultiChannelController extends BaseController {



}
