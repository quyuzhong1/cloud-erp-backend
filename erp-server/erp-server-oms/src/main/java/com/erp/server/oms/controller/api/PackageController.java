package com.erp.server.oms.controller.api;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.PackageDTO;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 分拨组包
 * @author Lambda
 * @Classname PackageController
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

}
