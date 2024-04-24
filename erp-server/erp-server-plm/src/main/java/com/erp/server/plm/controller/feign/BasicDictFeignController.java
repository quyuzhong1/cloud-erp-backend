package com.erp.server.plm.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.server.plm.service.BasicCategoryService;
import com.erp.server.plm.service.BasicDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("feign/dict")
public class BasicDictFeignController extends BaseController {

    @Autowired
    private BasicDictService basicDictService;

    @GetMapping("/listDictByType")
    public List<BasicDictEntity> listDictByType(@RequestParam("type") String type) {
        return basicDictService.listByType(type);
    }

}
