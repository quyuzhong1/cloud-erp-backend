package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.SHOPEE)
public class ShopeeShipOrder extends AbstractShipOrder {
    @Resource
    private SoB2cFeign soB2cFeign;
    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询合并来源关系
        List<SoB2cRefEntity> refEntityList = soB2cFeign.findMergeByTargetId(dto.getSoB2cId());
        if (CollectionUtils.isEmpty(refEntityList)){
            // 无合并
            //检查销售订单是否存在
            SoB2cEntity mainEntity = soB2cFeign.getById(dto.getSoB2cId());
            if (ObjectUtil.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            return soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getId).distinct().collect(Collectors.toList());
        } else {
            // 有合并
            List<String> mainIds = refEntityList.stream().map(SoB2cRefEntity::getSourceId).distinct().collect(Collectors.toList());
            List<String> detailIds = refEntityList.stream().map(SoB2cRefEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            List<SoB2cEntity> sourceOrderList = soB2cFeign.listByIds(mainIds);
            //检查销售订单是否存在
            if (CollectionUtils.isEmpty(sourceOrderList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            // 查询所有明细
            List<SoB2cDetailEntity> allDetailList = soB2cFeign.listDetailByIds(detailIds);
            return allDetailList.stream().map(SoB2cDetailEntity::getId).distinct().collect(Collectors.toList());
        }
    }
    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList){
        return null;
    }
}
