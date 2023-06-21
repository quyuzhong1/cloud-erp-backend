package com.erp.server.sys.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.sys.service.DictBasicService;
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

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
    @GetMapping("/getByType")
    public List<DictBasicDTO.ViewDTO> getByType(@RequestParam(value = "type") String type) {
        return  dictBasicService.listByType(type);
    }


}
