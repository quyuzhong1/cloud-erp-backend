package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.DictBasicEntity;
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

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     */
    @GetMapping("/dict/getByTypeAndValue")
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        return dictBasicService.getByTypeAndValue(type, value);
    }
}
