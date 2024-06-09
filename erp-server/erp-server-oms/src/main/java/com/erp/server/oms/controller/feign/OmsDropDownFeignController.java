package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description: 下拉列表
 */
@RestController
@RequestMapping("feign/drop/down")
public class OmsDropDownFeignController {

    @Resource
    private DictBasicService dictBasicService;
    /**
     * 根据type返回树状结构
     *
     * @param key
     * @return
     */
    @GetMapping("/dict/tree")
    public List<BaseDropDownDTO.Tree> tree(@RequestParam("key") String key) {
        return dictBasicService.getTreeByKey(key);
    }
}
