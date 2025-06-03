package com.erp.server.workflow.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.workflow.dto.*;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.server.workflow.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * cfgQueryOption Feign
 * @date 2025-05-29
 * @author jack
 */
@RestController
@RequestMapping("feign/cfgQueryOption")
@Slf4j
public class CfgQueryOptionFeignController extends BaseController {

    @Resource
    public CfgQueryOptionService cfgQueryOptionService;

    /**
     */
    @PostMapping("/listByMqParams")
    public List<CfgQueryOptionEntity> listByMqParams(@RequestBody CfgQueryOptionDTO.MqParamsDTO mqParamsDTO) {
        return cfgQueryOptionService.listByMqParams(mqParamsDTO);
    }

    /**
     */
    @PostMapping("/listByIds")
    public List<CfgQueryOptionEntity> listByIds(@RequestBody List<String> ids) {
        return cfgQueryOptionService.lambdaQuery().in(CfgQueryOptionEntity::getId, ids).list();
    }

}
