package com.erp.server.dmp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpTransferInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 直接调拨单
 *
 * @author Cloud
 * @since 2023-06-19
 */
@RestController
@RequestMapping("/dmpTransferInfo")
public class DmpTransferInfoController extends BaseController {

}
