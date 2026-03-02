package com.erp.server.oms.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.server.oms.query.SoB2cQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoB2cDetailService;
import com.common.core.controller.vo.ApiResult;

import java.util.List;


/**
 * B2C销售订单明细表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cDetail")
public class SoB2cDetailController extends BaseController {

    @Resource
    private SoB2cDetailService soB2cDetailService;


    /**
     * 明细查询
     *
     * @param dto
     * @return ApiResult<PagingVO < SoB2cDTO.ListDTO>>
     * @author zdy
     * @date: 2023-08-18
     */
    @PostMapping("/listDetailById")
    public ApiResult<List<SoB2cDetailDTO.ListDTO>> listDetailByMainId(@RequestBody @Validated BaseIdDTO dto) {
        return success(soB2cDetailService.listDetailByMainId(dto.getId()));
    }
}
