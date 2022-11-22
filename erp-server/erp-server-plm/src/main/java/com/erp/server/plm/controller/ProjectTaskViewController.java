package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.ProductTaskPersonnelViewDTO;
import com.erp.model.plm.dto.ProductTaskViewSearchDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品管理
 *
 * @author Will
 * @version 1.0
 * @date 2022/11/22 18:06
 */
@RestController
@RequestMapping("/plm/task/view")
public class ProjectTaskViewController extends BaseController {


    @GetMapping("/viewList")
    public ApiResult<List<ProductTaskPersonnelViewDTO>> viewList(@RequestBody ProductTaskViewSearchDTO dto) {

        return success();
    }
}
