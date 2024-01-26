package com.erp.server.oms.controller.api;

import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.model.wms.dto.WeightingOutboundDTO;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
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


    /**
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
     *
     * @description
     * @param
     * @return
     * @date 2024-01-26 17:45
     * @author Lambda
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PackageDTO.PagingViewDTO>> paging(@RequestBody PackageDTO.PagingParamDTO dto){
        PagingVO<PackageDTO.PagingViewDTO> pagingView = soB2cService.packagePing(dto);
        return success(pagingView);
    }

}
