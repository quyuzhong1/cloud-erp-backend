package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.server.oms.service.SoDetailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soDetail")
public class SoDetailController extends BaseController {

    @Resource
    private SoDetailService soDetailService;


    /**
     * 根据skuId 获取产品明细
     */
    @GetMapping("/getSkuInfoBySkuId")
    public ApiResult<SoDetailDTO.SkuDTO> getSkuInfoBySkuId(@RequestParam("skuNo") String skuNo,@RequestParam("warehouseId") String warehouseId) {
        SoDetailDTO.SkuDTO skuDTO = soDetailService.getSkuInfoBySkuNo(skuNo,warehouseId);
        return success(skuDTO);
    }


    /**
     * 根据销售订单id 获取到对应
     * 产品明细
     */
    @GetMapping("/listBySoId")
    public ApiResult<List<SoDetailDTO.ViewDTO>> listBySoId(@RequestParam("soId") String soId) {
        List<SoDetailDTO.ViewDTO> list = soDetailService.listBySoId(soId);
        return success(list);
    }


    /**
     * 销售订单 产品信息导入
     */
    @PostMapping("/import")
    public ApiResult<SoDetailDTO.ImportDTO> importSku(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "warehouseId") String warehouseId, HttpServletResponse response) {
        SoDetailDTO.ImportDTO result = soDetailService.importSku(excelFile, response, warehouseId);
        return success(result);
    }

    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        soDetailService.downloadTemplate(response);
        return success();
    }


}
