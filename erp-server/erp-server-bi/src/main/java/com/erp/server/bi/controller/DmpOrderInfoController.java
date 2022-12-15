package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpOrderInfoSearchDTO;
import com.erp.model.bi.dto.DmpOrderStateDTO;
import com.erp.server.bi.service.DmpOrderInfoService;
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
@RequestMapping("bi/dmpOrderInfo")
public class DmpOrderInfoController extends BaseController {

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    /**
     * @description: 销售数据-分页查询
     * @author Will
     * @date: 2022/12/15 10:48
     * @param dto
     * @return ApiResult<PagingVO<DmpOrderInfoDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<DmpOrderInfoDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpOrderInfoSearchDTO> dto) {
        PagingVO<DmpOrderInfoDTO> pagingVO = dmpOrderInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 销售数据-修改状态
     * @author Will
     * @date: 2022/12/15 10:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated DmpOrderStateDTO dto) {
        Boolean flag = dmpOrderInfoService.updateState(dto);
        return flag == true ? this.success() : this.failure();
    }


    /**
     *  销售数据-导出
     * @author Will
     * @date: 2022/12/15 10 10:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    public void exportExcel(@RequestBody DmpOrderInfoSearchDTO dto, HttpServletResponse response) {
        dmpOrderInfoService.exportExcel(dto, response);
    }

}
