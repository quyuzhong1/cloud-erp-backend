package com.erp.server.sys.controller.feign;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.service.DictBasicAllService;
import com.erp.server.sys.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理feign控制器
 * @author zhangchunlin
 * @since 2023-06-21
 */
@RestController
@RequestMapping("/feign/dictBasic")
public class DictBasicFeignController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private DictBasicAllService dictBasicAllService;
}
