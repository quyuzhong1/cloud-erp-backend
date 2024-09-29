package com.erp.server.tms.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.server.tms.service.ShippingCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 运费计算控制层
 *
 * @author Will
 * @version 1.0
 * @date 2023/11/10 12:16
 */
@Slf4j
@RestController
@LogSystemModule("运费计算")
@RequestMapping("/shippingCalculation")
public class ShippingCalculationController extends BaseController {

    @Resource
    private ShippingCalculationService shippingCalculationService;


    /**
     * 运费计算列表
     *
     * @param dto
     * @return ApiResult<PagingVO < ListDTO>>
     * @author Will
     * @date: 2023/11/10 17:35
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ShippingCalculationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        PagingVO<ShippingCalculationDTO.ListDTO> pagingVO = shippingCalculationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 运费计算导出
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/11/10 17:36
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody ShippingCalculationDTO.PagingParamDTO dto) {
        Boolean flag = shippingCalculationService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询分区城市
     *
     * @param dto
     * @return ApiResult<List < String>>
     * @author Will
     * @date: 2023/11/14 17:24
     */
    @PostMapping(value = "/listRegionCity")
    public ApiResult<List<String>> listRegionCity(@RequestBody @Validated ShippingCalculationDTO.ListRegionCityParamDTO dto) {
        List<String> list = shippingCalculationService.listRegionCity(dto);
        return success(list);
    }

    /**
     *  B2C销售订单运费测算
     * orderId 订单id
     *
     * @return
     */
    @GetMapping("/listChannelCost")
    public ApiResult<ShippingCalculationDTO.CostCalculationResultDTO> listChannelCost(@RequestParam(value = "orderId") String orderId) {
        ShippingCalculationDTO.CostCalculationResultDTO result = shippingCalculationService.listChannelCost(orderId);
        return success(result);
    }

}
