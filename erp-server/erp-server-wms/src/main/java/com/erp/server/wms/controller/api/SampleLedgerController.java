package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SampleLedgerService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SampleLedgerDTO;

import java.util.Objects;

/**
 * 样品库存统计
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@RestController
@LogSystemModule("样品库存统计")
@RequestMapping("/sampleLedger")
public class SampleLedgerController extends BaseController {

    @Resource
    private SampleLedgerService sampleLedgerService;

    /**
    * 新增
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "样品库存统计新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SampleLedgerDTO.AddDTO dto) {
        return success(sampleLedgerService.add(dto));
    }

    /**
    * 修改
    * @author wuhaotian
    * @date:  2025-08-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "样品库存统计修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:sampleLedger:update",
        serviceClass = SampleLedgerService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SampleLedgerDTO.UpdateDTO dto) {
        sampleLedgerService.update(dto);
        return success();
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-08-20
     * @param pagingDTO
     * @return ApiResult<PagingVO<SampleScrapInfoDTO.ListDTO>>
     */
    @PostMapping("/listSku")
    public ApiResult<PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO>> listSku(@RequestBody @Validated PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO) {
        if (Objects.isNull(pagingDTO.getParams())){
            return success();
        }
        return success(sampleLedgerService.listSku(pagingDTO));
    }
}
