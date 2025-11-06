package com.erp.server.sys.controller.feign;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicDTO;
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

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
    @GetMapping("/getByType")
    public List<DictBasicDTO.ViewDTO> getByType(@RequestParam(value = "type") String type) {
        return  dictBasicService.listByType(type);
    }


    @PostMapping("feign/dictBasic/paging")
    public PagingVO<DictBasicAllDTO.ViewDTO> paging(@RequestBody PagingDTO< DictBasicAllDTO.PagingParamDTO > dto){
        return dictBasicAllService.paging(dto);
    }
}
