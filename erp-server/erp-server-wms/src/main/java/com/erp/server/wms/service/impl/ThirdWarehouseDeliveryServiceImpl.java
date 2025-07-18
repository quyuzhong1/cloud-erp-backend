package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 三方仓发货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryMapper, ThirdWarehouseDeliveryEntity> implements ThirdWarehouseDeliveryService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private ThirdWarehouseDeliveryDetailService detailService;

    @Resource
    private WarehouseService warehouseService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ThirdWarehouseDeliveryEntity entity) {

        // 生成单号
        boolean save = super.save(entity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }
        entity.getDetailEntityList().forEach(v->{
            v.setMainId(entity.getId());
        });
        detailService.saveBatch(entity.getDetailEntityList());
        return entity.getId();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getByCodeAndSoId(String outCode, String soId) {
        if(StringUtils.isBlank(outCode) || StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode,outCode).eq(ThirdWarehouseDeliveryEntity::getSoId,soId).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestBySoId(String soId) {
        if(StringUtils.isBlank(soId)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getSoId, soId)
                .orderByDesc(ThirdWarehouseDeliveryEntity::getCreateTime).last("LIMIT 1").one();
    }

    @Override
    public ThirdWarehouseDeliveryEntity getLatestByCode(String code) {
        if(StringUtils.isBlank(code)){
            return null;
        }
        return lambdaQuery().eq(ThirdWarehouseDeliveryEntity::getCode, code).one();
    }

    @Override
    public PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> paging(PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ThirdWarehouseDeliveryDTO.PagingViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        List<ThirdWarehouseDeliveryDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        List<String> shopIds = list.stream().map(ThirdWarehouseDeliveryDTO.PagingViewDTO::getShopId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        List<String> warehouseIds = list.stream().map(ThirdWarehouseDeliveryDTO.PagingViewDTO::getWarehouseId).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(warehouseIds);
        for (ThirdWarehouseDeliveryDTO.PagingViewDTO pagingViewDTO : list) {
            pagingViewDTO.setPlatformName(PlatformDictEnum.getNameByCode(pagingViewDTO.getPlatform()));
            shopInfoEntityList.stream()
                    .filter(shop -> shop.getId().equals(pagingViewDTO.getShopId()))
                    .findFirst().ifPresent(shopInfoEntity -> pagingViewDTO.setShopName(shopInfoEntity.getName()));
            pagingViewDTO.setStatusName(SoB2cWarehouseDeliveryStatusEnum.getName(pagingViewDTO.getStatus()));
            warehouseEntityList.stream()
                    .filter(warehouse -> Objects.equals(warehouse.getId(), pagingViewDTO.getWarehouseId()))
                    .findFirst().ifPresent(warehouseEntity -> pagingViewDTO.setWarehouseName(warehouseEntity.getName()));
        }
        return new PagingVO<>(pageData);
    }

}
