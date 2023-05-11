package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 区域表 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@RequestMapping("/dict/global/area")
public class DictGlobalAreaController extends BaseController {

    @Resource
    private DictGlobalAreaService dictGlobalAreaService;


    /**
     * 添加地区
     *
     * @param list
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated List<DictGlobalAreaDTO.AddOrUpdateDTO> list) {
        Boolean result = dictGlobalAreaService.addOrUpdate(list);
        return result ? success() : failure();
    }
}
