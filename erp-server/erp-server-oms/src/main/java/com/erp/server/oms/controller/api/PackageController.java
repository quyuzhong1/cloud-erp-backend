package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.server.oms.service.PackageService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 分拨组包
 * @author Lambda
 * @Classname PackageController
 * @Description TODO
 * @Date 2024-01-26 14:33
 * @Created by yl
 */
@Slf4j
@RestController
@RequestMapping("/package")
public class PackageController extends BaseController {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private PackageService packageService;


    /**
     * 扫描
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-26 14:35
     */
    @GetMapping("/scan")
    public ApiResult<PackageDTO.ScanResultDTO> scan(@RequestParam("code") String code) {
        PackageDTO.ScanResultDTO scanResultDTO = soB2cService.packageScan(code);
        return success(scanResultDTO);
    }

    /**
     * 批量分包分页查询
     *
     * @param
     * @return
     * @description
     * @date 2024-01-26 17:45
     * @author Lambda
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PackageDTO.PagingViewDTO>> paging(@RequestBody PagingDTO<PackageDTO.PagingParamDTO> dto) {
        PagingVO<PackageDTO.PagingViewDTO> pagingView = soB2cService.packagePing(dto);
        return success(pagingView);
    }

    /**
     * 组包合并  注意对应的ids 为销售订单ids  就是 soId 的集合
     *
     * @return
     */
    @PostMapping("/merge")
    public ApiResult merge(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = packageService.mergePackage(dto.getIds());
        return result ? success() : failure();
    }

}
