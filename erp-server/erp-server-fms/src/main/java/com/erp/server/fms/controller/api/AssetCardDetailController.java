package com.erp.server.fms.controller.api;


import com.erp.model.fms.dto.AssetCardDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.fms.service.AssetCardDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.fms.dto.AssetCardDetailDTO;

import java.util.List;

/**
 * 资产卡片明细表
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@RestController
@LogSystemModule("资产卡片明细表")
@RequestMapping("/assetCardDetail")
public class AssetCardDetailController extends BaseController {

    @Resource
    private AssetCardDetailService assetCardDetailService;

    /**
     * 模糊搜索
     * @author jack
     * @date: 2025-10-30
     * @param dto
     * @return
     */
    @PostMapping("/searchAssetCardDetail")
    public ApiResult<List<AssetCardDetailDTO.SearchCardDetailDTO>> searchAssetCardDetail(@RequestBody AssetCardDetailDTO.SearchDTO dto) {
        return success(assetCardDetailService.searchAssetCardDetail(dto));
    }



}
