package com.erp.server.mrp.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import com.erp.server.mrp.service.PurchaseSuggestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 建议采购
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@RestController
@LogSystemModule("建议采购")
@RequestMapping("/purchaseSuggest")
public class PurchaseSuggestController extends BaseController {

    @Resource
    private PurchaseSuggestService purchaseSuggestService;


    /**
     * 列表查询
     * @author will
     * @date 2024/9/9 11:59
     * @param params
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/list")
    public ApiResult<List<PurchaseSuggestDTO.ListDTO>> list(@RequestBody @Validated PurchaseSuggestDTO.ListParamDTO params) {
        List<PurchaseSuggestDTO.ListDTO> paging = purchaseSuggestService.list(params);
        return success(paging);
    }

}
