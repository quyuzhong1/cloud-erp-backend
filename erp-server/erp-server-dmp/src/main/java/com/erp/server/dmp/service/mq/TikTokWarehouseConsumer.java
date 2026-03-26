package com.erp.server.dmp.service.mq;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.dto.ErpWarehouseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * TikTok 销售仓数据消费，独立于旺店通仓库链路。
 */
@Component
@Slf4j
public class TikTokWarehouseConsumer {

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    public ApiResult<?> handle(Object ext) {
        log.info("TikTok销售仓数据处理：{}", JSONUtil.parse(ext).toString());
        ErpWarehouseDto dto = JSONUtil.toBean(ext.toString(), ErpWarehouseDto.class);
        String warehouseId = StrUtil.blankToDefault(dto.getWarehouseId(), dto.getUniqueId());
        if (StrUtil.isBlank(warehouseId)) {
            return ApiResult.error("TikTok销售仓消费失败：warehouseId不能为空");
        }

        ThirdWarehouseEntity entity = new ThirdWarehouseEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setCategory(ThirdSysTypeEnum.WAREHOUSE.getCode());
        entity.setSysType(StrUtil.blankToDefault(dto.getSysType(), PlatformDictEnum.TIK_TOK.getCode()));
        entity.setCode(StrUtil.blankToDefault(dto.getCode(), warehouseId));
        entity.setDisabled(Objects.nonNull(dto.getDisabled()) ? dto.getDisabled() : Boolean.FALSE);

        ThirdWarehouseEntity existEntity = thirdWarehouseService.getByWarehouseId(warehouseId, ThirdSysTypeEnum.WAREHOUSE.getCode());
        if (existEntity == null) {
            thirdWarehouseService.save(entity);
            return ApiResult.success();
        }

        entity.setId(existEntity.getId());
        thirdWarehouseService.updateById(entity);
        return ApiResult.success();
    }
}
