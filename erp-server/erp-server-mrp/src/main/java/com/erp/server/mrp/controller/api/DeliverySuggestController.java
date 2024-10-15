package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.server.mrp.service.DeliverySuggestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 发货计划
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("发货计划")
@RequestMapping("/deliverySuggest")
public class DeliverySuggestController extends BaseController {

    @Resource
    private DeliverySuggestService deliverySuggestService;


    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:59
     * @param params
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<DeliverySuggestDTO.ListDTO>> list(@RequestBody @Validated DeliverySuggestDTO.ListParamDTO params) {
        List<DeliverySuggestDTO.ListDTO> paging = deliverySuggestService.list(params);
        return success(paging);
    }

}
