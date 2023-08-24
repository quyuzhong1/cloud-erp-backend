package com.erp.server.oms.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.server.oms.service.ListingInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import java.util.List;

/**
 * listing
 *
 * @author Lambda
 * @since 2023-08-18
 */
@RestController
@RequestMapping("/listing")
public class ListingInfoController extends BaseController {


    private ListingInfoService listingInfoService;


    @GetMapping("list")
    public ApiResult<List<ListingInfoDTO.ListDTO>> listByType(@RequestParam("type") String type) {
        List<ListingInfoDTO.ListDTO> list = listingInfoService.listByType(type);
        return success(list);
    }

}
