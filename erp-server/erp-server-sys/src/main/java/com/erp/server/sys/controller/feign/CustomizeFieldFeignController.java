package com.erp.server.sys.controller.feign;

import com.erp.common.controller.BaseController;
import com.erp.model.sys.dto.CustomizeFieldHiddenDTO;
import com.erp.model.sys.dto.FindCustomizeFieldDTO;
import com.erp.server.sys.service.CustomizeFieldHiddenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname CustomizeFieldFeignController
 * @Description TODO
 * @Date 2023-02-08 16:07
 * @Created by yl
 */
@RestController
@RequestMapping("sys/feign/customize/field")
public class CustomizeFieldFeignController extends BaseController {

    @Resource
    private CustomizeFieldHiddenService customizeFieldHiddenService;


    @PostMapping("/add")
    public Boolean saveHiddenField(@RequestBody List<CustomizeFieldHiddenDTO> dto) {
        Boolean result = customizeFieldHiddenService.add(dto);
        return result;
    }

    @PostMapping("/getByUserId")
    public List<CustomizeFieldHiddenDTO> getByUserId(@RequestBody FindCustomizeFieldDTO  dto) {
        List<CustomizeFieldHiddenDTO> list = customizeFieldHiddenService.getByUserId(dto);
        return list;
    }
}
