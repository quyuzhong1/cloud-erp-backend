package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.server.sys.service.DictCountryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@RequestMapping("dict/country")
public class DictCountryController extends BaseController {


    @Resource
    private DictCountryService dictCountryService;


    /**
     * 获取国家列表
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictCountryDTO.ListDTO>> list() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        return success(list);
    }
}
