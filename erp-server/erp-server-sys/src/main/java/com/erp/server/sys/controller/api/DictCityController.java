package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.server.sys.service.DictCityService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 *字典管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@RequestMapping("/dict/city")
public class DictCityController extends BaseController {

    @Resource
    private DictCityService dictCityService;


    /**
     * 添加城市
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated DictCityDTO.AddDTO dto) {
        Boolean result = dictCityService.add(dto);
        return result ? success() : failure();
    }

    /**
     * 获取省份城市列表
     *
     * @param countryCode
     * @return
     */
    @GetMapping("/treeList")
    public ApiResult<List<DictCityDTO.ListDTO>> list(@RequestParam("countryCode") String countryCode) {
        List<DictCityDTO.ListDTO> list = dictCityService.listCity(countryCode);
        return success(list);
    }

}
