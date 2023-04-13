package com.erp.server.wms.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.QcRuleDTO;
import com.erp.server.wms.service.QcRuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 质检规则
 *
 * @author lambda
 * @since 2023-04-13
 */
@RestController
@RequestMapping("/qcRule")
public class QcRuleController extends BaseController {

    @Resource
    private QcRuleService qcRuleService;


    /**
     * 添加质检规则
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated QcRuleDTO.AddDTO dto) {
        String id = qcRuleService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

}
