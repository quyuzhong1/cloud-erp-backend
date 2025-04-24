package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.server.wms.query.SupplierDeliveryQueryHandler;
import com.erp.server.wms.service.SupplierDeliveryOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 供应商送货单
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@RestController
@LogSystemModule("供应商送货单")
@RequestMapping("/supplierDeliveryOrder")
public class SupplierDeliveryOrderController extends BaseController {

    @Resource
    private SrmDeliveryOrderFeign srmDeliveryFeign;

    @Resource
    private SupplierDeliveryOrderService service;

    /**
     * 获取 tab列表
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<DeliveryOrderDTO.TabListDTO>> tabList() {
        DeliveryOrderDTO.ParamDTO paramDTO = new DeliveryOrderDTO.ParamDTO();
        List<DeliveryOrderDTO.TabListDTO> tabList = srmDeliveryFeign.tabList(paramDTO);
        return success(tabList);
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<DeliveryOrderDTO.ViewDTO> view(@Param("id") String id) {
        DeliveryOrderDTO.ViewDTO view = srmDeliveryFeign.view(id);
        return success(view);
    }

    /**
     * 分页
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = SupplierDeliveryQueryHandler.class)
    public ApiResult<PagingVO<DeliveryOrderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return success(srmDeliveryFeign.paging(dto));
    }

    /**
     * 合计
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     */
    @PostMapping("/pagingTotal")
    @WebAdvanceQuery(handler = SupplierDeliveryQueryHandler.class)
    public ApiResult<DeliveryOrderDTO.TotalInfo> pagingTotal(@RequestBody @Validated DeliveryOrderDTO.ParamDTO dto) {
        return success(srmDeliveryFeign.pagingTotal(dto));
    }


    /**
     * 打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/print")
    public ApiResult<List<DeliveryOrderDTO.PrintDTO>> print(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(srmDeliveryFeign.print(dto));
    }

    /**
     * 确认打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/confirmPrint")
    public ApiResult confirmPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(srmDeliveryFeign.confirmPrint(dto));
    }

    /**
     * 取消打印送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelPrint")
    public ApiResult<List<BatchResultDTO>> cancelPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = srmDeliveryFeign.cancelPrint(dto);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess)?success(batchResultDTOList):failure(batchResultDTOList);
    }

    /**
     * 导出送货单
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/export")
    public ApiResult<Boolean> export(@RequestBody  DeliveryOrderDTO.ParamDTO dto) {
        return success(service.export(dto));
    }

    /**
     * 下推收货单查询列表
     * @author lrp
     * @date:  2024-01-12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/listGenerateReceive")
    public ApiResult<List<DeliveryOrderDTO.GenerateReceiveListDTO>> listGenerateReceive(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<DeliveryOrderDTO.GenerateReceiveListDTO> receiveListDTOList = srmDeliveryFeign.listGenerateReceive(dto);
        return success(receiveListDTOList);
    }


    /**
     * 下推收货单
     * @author lrp
     * @date:  2024-01-12
     * @return ApiResult
     */
    @PostMapping("/generateReceive")
    public ApiResult<List<BatchResultDTO>> generateReceive(@RequestBody @Validated DeliveryOrderDTO.GenerateDTO dto) {
        List<BatchResultDTO> batchResultDTOList = service.generateReceive(dto);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess)?success(batchResultDTOList):failure(batchResultDTOList);
    }
}
