package com.erp.server.oms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.validator.AddGroup;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售订单")
@RequestMapping("/soDetail")
public class SoDetailController extends BaseController {

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoInfoService soInfoService;

    /**
     * 检测 sku 是否缺货
     *
     * @param dto
     * @return
     */
    @PostMapping("/checkScarce")
    public ApiResult add(@RequestBody @Validated({AddGroup.class}) SoInfoDTO.AddDTO dto) {
        String msg = soDetailService.checkSkuQty(dto.getWarehouseId(), dto.getDetailList());
        return success(msg);
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

    /**
     * 批量释放库存
     * @author will
     * @date 2024/7/15 17:32
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "释放库存" ,keyIdName = "detailIdList")
    @PostMapping("/batchUnLockVirtualInventory")
    public ApiResult<List<BatchResultDTO>> batchUnLockVirtualInventory(@RequestBody @Validated BaseIdsDTO.DetailIdListDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getDetailIdList().size());
        for (String id : dto.getDetailIdList()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soDetailService.batchUnLockVirtualInventory(id,null);
            }catch (Exception e){
                log.error("销售订单明细释放库存失败",e);
                SoDetailEntity entity = soDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "销售订单明细不存在, 释放库存失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                SoInfoEntity soInfoEntity = soInfoService.getById(entity.getMainId());
                if (ObjectUtil.isEmpty(soInfoEntity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "销售订单不存在, 锁定库存失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(),  CharSequenceUtil.format("【{}】{}",soInfoEntity.getCode(),entity.getSkuNo()), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
