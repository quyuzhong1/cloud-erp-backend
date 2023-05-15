package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * sys 字典feign
 *
 * @author
 * @Classname DictFeignController
 * @Description TODO
 * @Date 2023-05-15 11:36
 * @Created by yl
 */
@RestController
@RequestMapping("feign/dict")
public class DictFeignController extends BaseController {


    @Resource
    private DictGlobalAreaService dictGlobalAreaService;


    /**
     * 根据币种查询
     */
    @PostMapping("/listGlobalAreaByCountryIds")
    public List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(@RequestBody List<String> countryIds) {
        return dictGlobalAreaService.listGlobalAreaByCountryIds(countryIds);
    }
}
