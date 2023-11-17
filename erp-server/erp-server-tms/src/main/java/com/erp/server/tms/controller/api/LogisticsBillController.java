package com.erp.server.tms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.server.tms.service.LogisticsTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsBillService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;

import java.util.List;

/**
 * 物流单
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流单")
@RequestMapping("/logisticsBill")
public class LogisticsBillController extends BaseController {

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsTrackService logisticsTrackService;


    /**
     * tab 列表
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "logistics_bill_detail"
    )
    public ApiResult<List<LogisticsBillDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsBillDTO.TabListDTO> tabList = logisticsBillService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    public ApiResult<PagingVO<LogisticsBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsBillDTO.PagingVO> pagingVO = logisticsBillService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    public ApiResult exportExcel(@RequestBody @Valid LogisticsBillDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = logisticsBillService.exportExcel(dto, response);
        return result ? success() : failure();
    }
    /**
     * 获取物流轨迹明细
     * @return
     */
    @GetMapping("/getTrackInfo")
    public ApiResult<List<LogisticsTrackDTO.ViewDTO>> listTrack(@RequestParam(value = "trackNo") String  trackNo){
        List<LogisticsTrackDTO.ViewDTO> list=logisticsTrackService.listByTrackNo(trackNo);
        return success(list);

    }


}
