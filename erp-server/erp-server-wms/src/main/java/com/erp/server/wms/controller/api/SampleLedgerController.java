package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.server.wms.query.SampleLedgerQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleLedgerService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.common.business.annotation.WebAdvanceQuery;
import javax.servlet.http.HttpServletResponse;

import java.util.Objects;
import java.util.List;

/**
 * 样品台账统计
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品台账统计")
@RequestMapping("/sampleLedger")
public class SampleLedgerController extends BaseController {

    @Resource
    private SampleLedgerService sampleLedgerService;


    /**
     * 列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return ApiResult<PagingVO<SampleLedgerDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleLedger:paging",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleLedgerQueryHandler.class)
    public ApiResult<PagingVO<SampleLedgerDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SampleLedgerDTO.PagingParamDTO> dto) {
        return success(sampleLedgerService.paging(dto));
    }

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return ApiResult<Boolean>
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:sampleLedger:export",
            tableAlias = ""
    )
    @WebAdvanceQuery(handler = SampleLedgerQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "样品台账统计导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated SampleLedgerDTO.ExportDTO dto, HttpServletResponse response) {
        sampleLedgerService.exportList(dto, response);
        return success(true);
    }

    /**
     * 添加产品
     * @author jack
     * @date: 2025-08-20
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

    /**
     *
     * @author jack
     * @date: 2025-08-20
     * @param dto
     * @return ApiResult<SampleLedgerDTO.SampleScrapView>
     */
    @PostMapping("/generateSampleScrapView")
    public ApiResult<SampleLedgerDTO.SampleScrapView> generateSampleScrapView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(sampleLedgerService.generateSampleScrapView(dto.getIds()));
    }

    /**
     *
     * @author jack
     * @date: 2025-08-20
     * @param dto
     * @return ApiResult<SampleLedgerDTO.ExhibitionOrderView>
     */
    @PostMapping("/generateExhibitionOrderView")
    public ApiResult<SampleLedgerDTO.ExhibitionOrderView> generateExhibitionOrderView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(sampleLedgerService.generateExhibitionOrderView(dto.getIds()));
    }



}
