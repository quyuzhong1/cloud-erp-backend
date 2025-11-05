package com.erp.server.wms.controller.feign;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.server.wms.query.SampleLedgerQueryHandler;
import com.erp.server.wms.service.SampleLedgerService;
import com.erp.server.wms.service.SampleLedgerFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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

}
