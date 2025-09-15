package com.erp.server.wms.controller.app;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.server.wms.service.SampleLedgerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * 样品台账统计app端
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品台账统计app端")
@RequestMapping("/app/sampleLedger")
public class SampleLedgerAppController extends BaseController {

    @Resource
    private SampleLedgerService sampleLedgerService;


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
