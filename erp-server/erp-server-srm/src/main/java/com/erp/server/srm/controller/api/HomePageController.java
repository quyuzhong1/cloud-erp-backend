package com.erp.server.srm.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.model.sys.dto.SysUserInfoDTO;
import com.erp.model.sys.dto.UpdateUserStateDTO;
import com.erp.model.sys.dto.UserPagingSearchDTO;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.rpc.wms.feign.SupplierUserFeign;
import com.erp.server.srm.service.HomePageService;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collections;

/**
 * 首页
 */
@Slf4j
@RestController
@LogSystemModule("首页")
@RequestMapping("/homePage")
public class HomePageController extends BaseController {

    @Resource
    private SupplierUserFeign supplierUserFeign;

    @Resource
    private HomePageService homePageService;

    /**
     * 查询账号信息
     * @return
     */
    @GetMapping("/getAccountInfo")
    public ApiResult<HomePageDTO.AccountInfoDTO> getAccountInfo(){
        return success(homePageService.getAccountInfo());
    }

    /**
     * 查询待办信息
     * @return
     */
    @GetMapping("/getToDoItems")
    public ApiResult<HomePageDTO.ToDoItems> getToDoItems(){
        return success(homePageService.getToDoItems());
    }

}
