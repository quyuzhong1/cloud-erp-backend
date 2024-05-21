package com.erp.server.wms.wdt.impl;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockin.StockinAPI;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import cn.wangdian.erp.sdk.api.wms.stockin.dto.CreateOtherStockinResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OtherInstockDetailEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.server.wms.mapper.OtherInstockDetailMapper;
import com.erp.server.wms.service.WarehouseService;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 同步其他入库单到旺店通
 * @author tanmujin
 * @date 2024-05-15
 */
@Slf4j
@Service
public class SyncWdtOtherInStockServiceImpl implements SyncWdtOtherInStockService {

    @Resource
    private Client wdtClient;

    @Resource
    private OtherInstockDetailMapper otherInstockDetailMapper;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Override
    public void syncDataToWdt(OtherInstockEntity entity) {
        QueryWrapper<OtherInstockDetailEntity> queryWrapper = new QueryWrapper<OtherInstockDetailEntity>()
                .eq("main_id", entity.getId())
                .eq("is_deleted", false);
        List<OtherInstockDetailEntity> detailList = otherInstockDetailMapper.selectList(queryWrapper);
        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>();
        detailList.forEach(item -> {
            CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
            goods.setSpecNo(item.getSkuNo());
            goods.setNum(BigDecimal.valueOf(item.getActualQty()));
            goods.setPositionNo(item.getWarehouseLocation());
            goodsList.add(goods);
        });

        syncDataToWdt(goodsList, entity.getWarehouseId(), entity.getCode());
    }

    @Override
    public void syncDataToWdt(List<CreateOtherStockinRequest.GoodsList> goodsList, String warehouseId, String outerNo){
        CreateOtherStockinRequest request = new CreateOtherStockinRequest();
        request.setOuterNo(outerNo == null ? docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTRK) : outerNo);
        request.setWarehouseNo("wjkj03-test");  //todo 根据出货仓库匹配旺店通仓库编号, @韩月娇: 仓库数据任务
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);

        StockinAPI stockinAPI = ApiFactory.get(wdtClient, StockinAPI.class);
        CreateOtherStockinResponse response = null;
        try {
            response = stockinAPI.createOtherOrder(request);
        } catch (WdtErpException e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_3000.code, e.getMessage());
        }

        if(response.getStatus() != 0){
            log.error(response.toString());
            throw new ServiceException(ApiError.ERROR_3000.code, response.getMessage());
        }
        log.info("其他入库单推送旺店通成功: {}", request);
    }
}
