package com.erp.server.wms.wdt.impl;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.api.wms.stockout.StockoutAPI;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import cn.wangdian.erp.sdk.api.wms.stockout.dto.CreateOtherStockoutResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OtherOutstockDetailEntity;
import com.erp.model.wms.entity.OtherOutstockEntity;
import com.erp.server.wms.mapper.OtherOutstockDetailMapper;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 将erp其他出库单同步至旺店通
 *
 * @author tanmujin
 * @date 2024-05-16
 */
@Slf4j
@Service
public class SyncWdtOtherOutStockServiceImpl implements SyncWdtOtherOutStockService {

    @Resource
    private Client wdtClient;

    @Resource
    private OtherOutstockDetailMapper outDetailMapper;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    /**
     * 审核通过后将其他出库单同步至旺店通
     */
    @Override
    public void syncDataToWdt(OtherOutstockEntity entity) {
        QueryWrapper<OtherOutstockDetailEntity> queryWrapper = new QueryWrapper<OtherOutstockDetailEntity>()
                .eq("main_id", entity.getId())
                .eq("is_deleted", false);
        List<OtherOutstockDetailEntity> detailList = outDetailMapper.selectList(queryWrapper);

        //填充SKU明细
        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>();
        detailList.forEach(item -> {
            CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
            goods.setSpecNo(item.getSkuNo());
            goods.setNum(BigDecimal.valueOf(item.getActualQty()));
            goods.setPositionNo(item.getWarehouseLocation());
            goodsList.add(goods);
        });

        //推送数据
        syncDataToWdt(goodsList, entity.getWarehouseId(), entity.getCode());
    }

    /**
     * @param goodsList 商品明细列表 CreateOtherStockoutRequest.GoodsList
     * @param warehouseId 仓库ID
     * @param outerNo 单据编码, 若为空则自动生成
     * @return void
     * @date: 2024-05-17
     * @author: tanmujin
     */
    @Override
    public void syncDataToWdt(List<CreateOtherStockoutRequest.GoodsList> goodsList, String warehouseId, @Nullable String outerNo) {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        request.setOuterNo(outerNo == null ? docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTCK) : outerNo);
        request.setWarehouseNo("wjkj03-test");  //todo 根据发货仓库匹配旺店通仓库编号, @韩月娇: 仓库数据任务
        request.setisCheck(Boolean.TRUE);
        request.setGoodsList(goodsList);

        StockoutAPI stockoutAPI = ApiFactory.get(wdtClient, StockoutAPI.class);
        CreateOtherStockoutResponse response = null;
        try {
            response = stockoutAPI.createOtherOutOrder(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(ApiError.ERROR_3000.code, e.getMessage());
        }
        if(response.getStatus() != 0){
            log.error(response.toString());
            throw new ServiceException(ApiError.ERROR_3000.code, response.getMessage());
        }
        log.info("推送其他出库单到旺店通成功: {}", request);
    }
}
