package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.DictSampleUseUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname SampleUseUserFeignController
 * @Date 2025-01-27 18:18
 * @Created by Lambda
 */
@RestController
@RequestMapping("feign/sampleUseUser")
public class SampleUseUserFeignController extends BaseController {

    @Resource
    private DictSampleUseUserService sampleUseUserService;

    @PostMapping("/getByIds")
    public List<BaseIdDTO> getByIds(@RequestBody List<String> ids) {
        List<BaseIdDTO> list = sampleUseUserService.getByIds(ids);
        return list;
    }
}
