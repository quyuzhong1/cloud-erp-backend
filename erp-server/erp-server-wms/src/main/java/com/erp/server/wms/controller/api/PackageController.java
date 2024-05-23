package com.erp.server.wms.controller.api;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.server.wms.service.PackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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
    private PackageService packageService;


    /**
     * 扫描
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-26 14:35
     */
    @PostMapping("/scan")
    public ApiResult<PackageDTO.ScanResultDTO> scan(@RequestBody @Validated PackageDTO.ScanDTO dto) {
        PackageDTO.ScanResultDTO scanResultDTO = packageService.packageScan(dto);
        return success(scanResultDTO);
    }


    /**
     * 组包合并  注意对应的ids 为销售订单ids  就是 soId 的集合
     *
     * @return
     */
    @PostMapping("/merge")
    public ApiResult<List<BatchResultDTO>> merge(@RequestBody @Validated PackageDTO.MergePackageDTO dto) {
        List<BatchResultDTO> result = packageService.mergePackage(dto);
        return result.stream().allMatch(BatchResultDTO::getSuccess) ? success(result) : failure(result);
    }

}
