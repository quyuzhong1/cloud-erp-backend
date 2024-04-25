package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.server.oms.query.SoB2cAbnormalQueryHandler;
import com.erp.server.oms.service.SoB2cAbnormalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
@LogSystemModule("异常订单")
@RequestMapping("/soB2cAbnormal")
public class SoB2cAbnormalController extends BaseController {

    @Resource
    private SoB2cAbnormalService soB2cAbnormalService;


    /**
     * 异常订单分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public ApiResult<PagingVO<SoB2cAbnormalDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        return success(soB2cAbnormalService.abnormalPaging(dto));
    }


    /**
     * 异常订单导出
     * @author Will
     * @date: 2024/4/22 19:50
     * @param dto
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出B2C销售异常订单信息")
    @PostMapping(value = "/abnormalExportExcel")
    @WebAdvanceQuery(handler = SoB2cAbnormalQueryHandler.class)
    public ApiResult abnormalExportExcel(@RequestBody SoB2cAbnormalDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = soB2cAbnormalService.abnormalExportExcel(dto, response);
        return flag == true ? success() : failure();
    }



}
