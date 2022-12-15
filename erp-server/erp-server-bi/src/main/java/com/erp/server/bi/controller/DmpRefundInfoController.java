package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpRefundInfoDTO;
import com.erp.model.bi.dto.DmpRefundInfoSearchDTO;
import com.erp.server.bi.service.DmpRefundInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:15
 */
@RestController
@RequestMapping("bi/dmpRefundInfo")
public class DmpRefundInfoController extends BaseController {

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

   /**
    * 退款数据-分页查询
    * @author Will
    * @date: 2022/12/15 10:36
    * @param dto
    * @return ApiResult<PagingVO<DmpRefundInfoDTO>>
    */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpRefundInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpRefundInfoSearchDTO> dto) {
        PagingVO<DmpRefundInfoDTO> pagingVO = dmpRefundInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  退款数据-导出
     * @author Will
     * @date: 2022/12/15 11:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody DmpRefundInfoSearchDTO dto, HttpServletResponse response) {
        dmpRefundInfoService.exportExcel(dto, response);
    }


}
