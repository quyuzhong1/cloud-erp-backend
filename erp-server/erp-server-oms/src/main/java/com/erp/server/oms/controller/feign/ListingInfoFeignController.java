package com.erp.server.oms.controller.feign;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ListingInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.server.oms.service.ListingInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * SKU对照表管理Feign
 *
 * @author Jim
 * @date 2023/11/2
 */
@RestController
@RequestMapping("/feign/listing")
public class ListingInfoFeignController extends BaseController {

    @Resource
    private ListingInfoService listingInfoService;


    /**
     * 通过条件查询ListingInfoEntity列表
     *
     * @author Jim
     * @date 2023/11/2
     */
    @GetMapping("/list")
    public ApiResult<List<ListingInfoEntity>> listByType(@RequestBody ListingInfoParamDTO dto) {
        List<ListingInfoEntity> list = listingInfoService.findList(dto);
        return success(list);
    }

}
