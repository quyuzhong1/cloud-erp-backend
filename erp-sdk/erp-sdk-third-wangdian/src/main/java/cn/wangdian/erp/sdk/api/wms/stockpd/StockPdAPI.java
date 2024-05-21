package cn.wangdian.erp.sdk.api.wms.stockpd;

import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryDetailRequest;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryDetailResponse;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryRequest;
import cn.wangdian.erp.sdk.api.wms.stockpd.dto.StockPdQueryResponse;
import cn.wangdian.erp.sdk.impl.Api;

public interface StockPdAPI
{
    @Api(value = "wms.StockPd.queryStockPd", paged = true)
    StockPdQueryResponse search(StockPdQueryRequest request, Pager pager) throws WdtErpException;

    @Api(value = "wms.StockPd.queryStockPdDetail", paged = true)
    StockPdQueryDetailResponse search(StockPdQueryDetailRequest request, Pager pager) throws WdtErpException;
}
