package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.server.plm.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志
 * @author Will
 * @version 1.0
 * @date 2022/12/5 20:36
 */
@RestController
@RequestMapping("/plm/sys/log")
public class SysLogController extends BaseController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2022/12/5 21:29
     * @param dto
     * @return ApiResult<PagingVO<SysLogShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysLogShowDTO>> paging(@RequestBody @Validated PagingDTO<SysLogSelectDTO> dto){
        PagingVO<SysLogShowDTO> pagingVO=sysLogService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 操作日志-类路径-产品信息(SKU)
     * @author Will
     * @date: 2022/12/7 13:26
     * @return ApiResult
     */
    @GetMapping("/getProductDetailClassPath")
    public ApiResult getProductDetailClassPath() {
        Class<ProductDetailEntity> classPath = ProductDetailEntity.class;
        return success(String.valueOf(classPath));
    }

    /**
     * 操作日志-类路径-产品管理(SPU)
     * @author Will
     * @date: 2022/12/12 13:26
     * @return ApiResult
     */
    @GetMapping("/getProductInfoClassPath")
    public ApiResult getProductInfoClassPath() {
        Class<ProductInfoEntity> classPath = ProductInfoEntity.class;
        return success(String.valueOf(classPath));
    }
}
