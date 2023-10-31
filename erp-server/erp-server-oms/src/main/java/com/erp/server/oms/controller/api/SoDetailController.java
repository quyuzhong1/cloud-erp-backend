package com.erp.server.oms.controller.api;


import com.common.business.validator.AddGroup;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.server.oms.service.SoDetailService;
import org.springframework.validation.annotation.Validated;
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
@LogSystemModule("销售订单")
@RequestMapping("/soDetail")
public class SoDetailController extends BaseController {

    @Resource
    private SoDetailService soDetailService;


    /**
     * 检测 sku 是否缺货
     *
     * @param dto
     * @return
     */
    @PostMapping("/checkScarce")
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) SoInfoDTO.AddDTO dto) {
       List<String> skuNoList= soDetailService.checkSkuQty(dto.getWarehouseId(), dto.getDetailList());
        return success(skuNoList);
    }


    /**
     * 根据skuId 获取产品明细
     */
    @GetMapping("/getSkuInfoBySkuId")
    public ApiResult<SoDetailDTO.SkuDTO> getSkuInfoBySkuId(@RequestParam("skuNo") String skuNo, @RequestParam("warehouseId") String warehouseId) {
        SoDetailDTO.SkuDTO skuDTO = soDetailService.getSkuInfoBySkuNo(skuNo, warehouseId);
        return success(skuDTO);
    }

    /**
     * 根据skuNo list 获取到sku 信息
     *
     */
    @PostMapping("/listSkuInfoBySkuNo")
    public ApiResult<List<SoDetailDTO.SkuDTO>> listSkuInfoBySkuNo(@RequestBody @Validated SoDetailDTO.ListSkuParamDTO dto) {
        List<SoDetailDTO.SkuDTO> resultList = soDetailService.listSkuInfoBySkuNo(dto);
        return success(resultList);
    }


    /**
     * 根据销售订单id 获取到对应产品明细
     */
    @GetMapping("/listBySoId")
    public ApiResult<List<SoDetailDTO.ViewDTO>> listBySoId(@RequestParam("soId") String soId) {
        List<SoDetailDTO.ViewDTO> list = soDetailService.listBySoId(soId);
        return success(list);
    }


    /**
     * 销售订单 产品信息导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入销售订单产品信息")
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
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板销售订单产品信息")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        soDetailService.downloadTemplate(response);
        return success();
    }


}
