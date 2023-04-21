package com.erp.server.workflow.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.server.workflow.service.WorkMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工作台菜单基础服务实现类
 *
 * @author Cloud
 * @since 2023-04-21
 */
@RestController
@RequestMapping("/work/menu")
public class WorkMenuController extends BaseController {
    @Resource
    private WorkMenuService workMenuService;

    /**
     * 关联单据下拉列表
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> workMenuDropDown(@RequestParam(value = "code", required = false) String code) {
        List<DictBasicDTO.DropDownDTO> result =  workMenuService.listByCode(code);
        return success(result);
    }
}
