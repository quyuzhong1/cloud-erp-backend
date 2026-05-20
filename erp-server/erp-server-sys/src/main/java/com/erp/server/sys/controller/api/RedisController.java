package com.erp.server.sys.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.RedisDTO;
import com.erp.server.sys.service.AuthUserShopService;
import com.erp.server.sys.service.ErpRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户-店铺权限
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@RestController
@LogSystemModule("缓存管理")
@RequestMapping("/redis")
public class RedisController extends BaseController {

    @Resource
    private ErpRedisService erpRedisService;

    /**
     * 同步redis缓存
     */
    @PostMapping("/copyKey")
    public ApiResult<Boolean> copyKey(@RequestBody @Validated RedisDTO.OperationDTO operationDTO) {
        return ApiResult.success(erpRedisService.copyKey(operationDTO.getSourcePrefix(), operationDTO.getTargetPrefix()));
    }

    /**
     * 批量同步redis缓存
     */
    @PostMapping("/batchCopyKey")
    public ApiResult<List<BatchResultDTO>> batchCopyKey(@RequestBody @Validated RedisDTO.BatchOperationDTO batchOperationDTO) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>(batchOperationDTO.getOperationDTOList().size());
        for (RedisDTO.OperationDTO operationDTO : batchOperationDTO.getOperationDTOList()) {
            try {
                Boolean b = erpRedisService.copyKey(operationDTO.getSourcePrefix(), operationDTO.getTargetPrefix());
                if (b) {
                    batchResultDTOList.add(new BatchResultDTO(operationDTO.getSourcePrefix(), operationDTO.getTargetPrefix(), "操作成功", Boolean.TRUE));
                } else {
                    batchResultDTOList.add(new BatchResultDTO(operationDTO.getSourcePrefix(), operationDTO.getTargetPrefix(), "操作失败", Boolean.FALSE));
                }
            } catch (Exception e) {
                log.error("批量同步redis缓存失败", e);
                batchResultDTOList.add(new BatchResultDTO(operationDTO.getSourcePrefix(), operationDTO.getTargetPrefix(), "操作失败", Boolean.FALSE));
            }
        }
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);

    }
}
