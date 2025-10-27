package com.erp.server.wms.controller.feign;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.server.wms.query.SampleLedgerFlowQueryHandler;
import com.erp.server.wms.query.SampleLedgerQueryHandler;
import com.erp.server.wms.service.SampleLedgerFlowService;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.SampleLedgerFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 样品台账统计
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品台账统计")
@RequestMapping("/feign/sampleLedger")
public class SampleLedgerFeignController extends BaseController {

    @Resource
    private SampleLedgerService sampleLedgerService;

    @Resource
    private SampleLedgerFlowService sampleLedgerFlowService;

    /**
     * 添加产品
     * @author jack
     * @date: 2025-08-21
     * @param dto
     * @return List<SampleLedgerDTO.SkuAvailableQtyDTO>
     */
    @PostMapping("/listLedgerByUserId")
    public List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(@RequestBody SampleLedgerDTO.SearchDTO dto) {
        return sampleLedgerService.listLedgerByUserId(dto);
    }


    /**
     * 添加产品
     * @author jack
     * @date: 2025-08-21
     * @param dto
     * @return List<SampleLedgerDTO.SkuAvailableQtyDTO>
     */
    @PostMapping("/listLedgerAll")
    public List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerAll(@RequestBody SampleLedgerDTO.SearchAllDTO dto) {
        return sampleLedgerService.listLedgerAll(dto);
    }

    /**
     * 添加样品台账流水
     * @author wuhaotian
     * @date: 2025-10-21
     * @param addDTO 台账流水新增参数
     * @return 是否成功
     */
    @PostMapping("/addSampleLedgerFlow")
    public Boolean addSampleLedgerFlow(@RequestBody SampleLedgerFlowDTO.AddFlowDTO addDTO) {
        return sampleLedgerFlowService.addSampleLedgerFlow(addDTO);
    }

    /**
     * 批量查询样品台账数量
     * @author system
     * @date: 2025-10-23
     * @param sampleLedgerIds 样品台账ID列表
     * @return 台账ID到数量的映射 Map<sampleLedgerId, qty>
     */
    @PostMapping("/getLedgerQtyMap")
    public Map<String, Integer> getLedgerQtyMap(@RequestBody List<String> sampleLedgerIds) {
        return sampleLedgerService.getLedgerQtyMap(sampleLedgerIds);
    }


    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-09-15
     * @param param 权限参数对象，用于控制数据访问权限
     * @return 标签页列表，包含全部、启用、禁用三个标签页的统计信息
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "user_id",
            menuCode = "wms:sampleLedger:paging",
            tableAlias = "sl"
    )
    public ApiResult<List<SampleLedgerDTO.TabListDTO>> tabListApp(@RequestBody PermissionsDTO param) {
        return success(sampleLedgerService.tabListApp(param));
    }

    /**
     * APP端列表查询
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<PagingVO<SampleLedgerDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "user_id",
            menuCode = "wms:sampleLedger:paging",
            tableAlias = "sl"
    )
    @WebAdvanceQuery(handler = SampleLedgerQueryHandler.class)
    public ApiResult<PagingVO<SampleLedgerDTO.ListDTO>> pagingApp(@RequestBody @Validated PagingDTO<SampleLedgerDTO.PagingParamDTO> dto) {
        return success(sampleLedgerService.pagingApp(dto));
    }

    /**
     * APP端详情
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id
     * @return ApiResult<SampleLedgerDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<SampleLedgerDTO.ViewDTO> viewApp(@RequestParam("id") String id) {
        return success(sampleLedgerService.view(id));
    }

    /**
     * 获取流水明细列表（移动端专用）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto
     * @return ApiResult<PagingVO<SampleLedgerFlowDTO.ListDTO>>
     */
    @PostMapping("/flowList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "user_id",
            menuCode = "wms:sampleLedgerFlow:paging",
            tableAlias = "slf"
    )
    @WebAdvanceQuery(handler = SampleLedgerFlowQueryHandler.class)
    public ApiResult<PagingVO<SampleLedgerFlowDTO.ListDTO>> getFlowList(@RequestBody @Validated PagingDTO<SampleLedgerFlowDTO.PagingParamDTO> dto) {
        return success(sampleLedgerFlowService.pagingApp(dto));
    }

    /**
     * 获取流水详情（移动端专用）
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id
     * @return ApiResult<SampleLedgerFlowDTO.ViewDTO>
     */
    @GetMapping("/flowDetail")
    @LogViewService
    public ApiResult<SampleLedgerFlowDTO.ViewDTO> getFlowDetail(@RequestParam("id") String id) {
        return success(sampleLedgerFlowService.view(id));
    }

    /**
     * 添加产品
     * @author jack
     * @date: 2025-09-15
     * @param pagingDTO
     * @return ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>>
     */
    @PostMapping("/listSku")
    public ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>> listSku(@RequestBody @Validated PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO) {
        if (Objects.isNull(pagingDTO.getParams())){
            return success();
        }
        return success(sampleLedgerService.listSku(pagingDTO));
    }

}
