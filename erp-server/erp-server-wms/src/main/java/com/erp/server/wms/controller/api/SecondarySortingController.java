package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.renovation.SecondarySortingDTO;
import com.erp.server.wms.service.SecondarySortingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 二次分货
 */
@RestController
@RequestMapping("/secondarySorting")
public class SecondarySortingController extends BaseController {

    @Resource
    private SecondarySortingService secondarySortingService;

    /**
     * 扫描拣货车编号/波次编号
     * @param code 拣货车编号/波次编号
     * @see SecondarySortingDTO.ScanCodeView
     */
    @GetMapping("/scanCode")
    public ApiResult<SecondarySortingDTO.ScanCodeView> scanCode(@RequestParam("code") String code) {
        SecondarySortingDTO.ScanCodeView view = secondarySortingService.scanCode(code);
        return success(view);
    }

    /**
     * 扫描sku信息
     * @param code 波次编号
     * @param skuCode 产品编码/EAN码
     * @see SecondarySortingDTO.ScanSkuView
     */
    @GetMapping("/scanSku")
    public ApiResult<SecondarySortingDTO.ScanSkuView> scanSku(@RequestParam("code") String code, @RequestParam("skuCode") String skuCode) {
        SecondarySortingDTO.ScanSkuView view = secondarySortingService.scanSku(code, skuCode);
        return success(view);
    }

    /**
     * 篮子明细
     * @param code 波次编号
     * @param basketNo 篮号
     * @see SecondarySortingDTO.BasketDetail
     */
    @GetMapping("/basketDetail")
    public ApiResult<List<SecondarySortingDTO.BasketDetail>> basketDetail(@RequestParam("code") String code, @RequestParam("basketNo") String basketNo) {
        List<SecondarySortingDTO.BasketDetail> details = secondarySortingService.basketDetail(code, basketNo);
        return success(details);
    }

    /**
     * 打印配货单
     * @param code 波次编号
     * @see SecondarySortingDTO.BasketDetail
     */
    @GetMapping("/printDistribution")
    public void printDistribution(@RequestParam("code") String code) {
        secondarySortingService.printDistribution(code);
    }

}
