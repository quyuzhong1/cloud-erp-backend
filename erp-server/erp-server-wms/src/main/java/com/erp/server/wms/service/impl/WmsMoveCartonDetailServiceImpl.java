package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.AfterSalesWarehouseLocationSuggestDto;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseLocationMoveEntity;
import com.erp.model.wms.entity.WmsMoveCartonDetailEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.server.wms.mapper.WmsMoveCartonDetailMapper;
import com.erp.server.wms.service.WarehouseLocationMoveService;
import com.erp.server.wms.service.WarehouseLocationService;
import com.erp.server.wms.service.WmsMoveCartonDetailService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仓位移动箱唛明细表 服务实现
 *
 * @author liuchao
 * @since 2026-05-18
 */
@Service
public class WmsMoveCartonDetailServiceImpl
        extends SuperServiceImpl<WmsMoveCartonDetailMapper, WmsMoveCartonDetailEntity>
        implements WmsMoveCartonDetailService {

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private WarehouseLocationMoveService warehouseLocationMoveService;

    @Override
    public List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> listBoxMoveDetail(String mainId) {
        List<WmsMoveCartonDetailEntity> entityList = lambdaQuery()
                .eq(WmsMoveCartonDetailEntity::getMainId, mainId)
                .orderByAsc(WmsMoveCartonDetailEntity::getSkuNo)
                .list();
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyList();
        }

        // 从主单获取仓库id，用于精确匹配仓位
        WarehouseLocationMoveEntity moveMain = warehouseLocationMoveService.getById(mainId);
        if (moveMain == null || moveMain.getWarehouseId() == null) {
            throw new ServiceException("仓位移动主单不存在或仓库信息缺失，mainId: " + mainId);
        }
        String warehouseId = moveMain.getWarehouseId();

        // 提取所有移出和移入仓位编码
        Set<String> locationCodes = entityList.stream()
                .map(WmsMoveCartonDetailEntity::getOutWarehouseLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        entityList.stream()
                .map(WmsMoveCartonDetailEntity::getInWarehouseLocation)
                .filter(Objects::nonNull)
                .forEach(locationCodes::add);

        Map<String, String> locationNameMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(locationCodes)) {
            // 根据仓位编码列表查库，并带上 type='location' 及仓库id以精确匹配
            List<WarehouseLocationEntity> locations = warehouseLocationService.lambdaQuery()
                    .eq(WarehouseLocationEntity::getType, WarehouseLocationTypeEnum.LOCATION.getCode())
                    .eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                    //这个in内容不会很大，所以此处使用in查询
                    .in(WarehouseLocationEntity::getCode, locationCodes)
                    .list();
            if (CollUtil.isNotEmpty(locations)) {
                // key为code，value为name。如果存在重复code（正常不会），此处也只需要取其一（或者按需处理，本处使用最新或第一个）
                locationNameMap = locations.stream()
                        .filter(l -> l.getCode() != null)
                        .collect(Collectors.toMap(
                                WarehouseLocationEntity::getCode,
                                WarehouseLocationEntity::getName,
                                (existing, replacement) -> existing
                        ));
            }
        }

        List<AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto> dtoList = BeanMapperUtils.copyList(AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto.class, entityList);

        // 回填对应的仓位名称，若没有则用"未知仓位"替代
        for (AfterSalesWarehouseLocationSuggestDto.BoxMoveDetailListDto dto : dtoList) {
            String outLoc = dto.getOutWarehouseLocation();
            if (outLoc != null) {
                dto.setOutWarehouseLocationName(locationNameMap.getOrDefault(outLoc, "未知仓位"));
            } else {
                dto.setOutWarehouseLocationName("未知仓位");
            }

            String inLoc = dto.getInWarehouseLocation();
            if (inLoc != null) {
                dto.setInWarehouseLocationName(locationNameMap.getOrDefault(inLoc, "未知仓位"));
            } else {
                dto.setInWarehouseLocationName("未知仓位");
            }
        }

        return dtoList;
    }
}
