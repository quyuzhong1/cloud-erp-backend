package com.erp.server.oms.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lambda
 * @Classname PackageController
 * @Description TODO
 * @Date 2024-01-26 14:33
 * @Created by yl
 */
@Slf4j
@RestController
@RequestMapping("/package")
public class PackageController extends BaseController {


    /**
     * @description
     * @param
     * @author Lambda
     * @return
     * @create 2024-01-26 14:35
     */
    @GetMapping("/scan")
    public ApiResult  scan(@RequestParam("code")String code){
        return success();
    }

}
