package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.threadlocal.UserContext;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cCoreDTO;
import com.erp.server.oms.service.SoB2cCoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
/**
 * b2c扩展类
 *
 * @author will
 * @date 2025/4/24 20:10
 */
@Slf4j
@RestController
@RequestMapping("/soB2cCore")
@Validated
public class SoB2cCoreController extends BaseController {

    @Resource
    private SoB2cCoreService soB2cCoreService;

    /**
     * 重新出库数据回显
     * @author will
     * @date 2025/4/24 20:21
     * @param dto
     * @return ApiResult<List<ListRetryOutstockDTO>>
     */
    @PostMapping("/listRetryOutstock")
    public ApiResult<List<SoB2cCoreDTO.ListRetryOutstockDTO>> listRetryOutstock(@RequestBody @Validated BaseIdsDTO.IdsDTO dto)  {
        return success(soB2cCoreService.listRetryOutstock(dto));
    }

    /**
     * 重新出库保存
     * @author will
     * @date 2025/4/24 20:27
     * @param list
     * @return ApiResult<Boolean>
     */
    @PostMapping("/retryOutstock")
    public ApiResult<Boolean> retryOutstock(@RequestBody @Validated List<SoB2cCoreDTO.RetryOutstockDTO> list)  {
        try {
            UserContext.setIsUserSystem(true);
            return success(soB2cCoreService.retryOutstock(list));
        }finally {
            UserContext.clearIsUserSystem();
        }
    }
}
