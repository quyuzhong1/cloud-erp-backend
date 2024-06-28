package com.sdk.third.qimen;

import com.qimencloud.api.DefaultQimenCloudClient;
import com.qimencloud.api.QimenCloudClient;
import com.qimencloud.api.scene3ldsmu02o9.request.WdtWmsStockinOtherCreateotherstockinorderRequest;
import com.qimencloud.api.scene3ldsmu02o9.request.WdtWmsStockspecQuerychangehistoryRequest;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockinOtherCreateotherstockinorderResponse;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockspecQuerychangehistoryResponse;
import com.sdk.third.qimen.config.QiMenConfigurationProperties;
import com.sdk.third.qimen.config.QiMenUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 奇门对接客户端服务
 *
 * @author tanmujin
 * @date 2024-06-06
 */
@Slf4j
@EnableConfigurationProperties(QiMenConfigurationProperties.class)
public class QiMenClientService {
    private QimenCloudClient qimenCloudClient;
    private QiMenConfigurationProperties properties;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    QiMenClientService(QiMenConfigurationProperties properties){
        this.properties = properties;
        this.qimenCloudClient = new DefaultQimenCloudClient(properties.getServerUrl(), properties.getAppKey(), properties.getAppSecret());
    }

    public <T extends TaobaoResponse> T execute(TaobaoRequest<T> request) throws ApiException{
        return this.qimenCloudClient.execute(request);
    }

    public String getTargetAppKey(){
        return properties.getTargetAppKey();
    }

    public String getWdtAppKey(){
        return properties.getWdtAppKey();
    }

    public String getWdtSecret(){
        return properties.getWdtSecret();
    }

    public String getWdtSalt(){
        return properties.getWdtSalt();
    }

    public String format(Date date){
        return dateFormat.format(date);
    }

    public String getCustomerIdKey(){
        return "wdt3_customer_id";
    }

    public String getCustomerIdValue(){
        return properties.getWdt3CustomerId();
    }

    public WdtWmsStockspecQuerychangehistoryResponse testStockQueryHistory() {
        WdtWmsStockspecQuerychangehistoryRequest request = new WdtWmsStockspecQuerychangehistoryRequest();
        request.setTargetAppKey(properties.getTargetAppKey());
        request.setDatetime(dateFormat.format(new Date()));

        WdtWmsStockspecQuerychangehistoryRequest.Params params = new WdtWmsStockspecQuerychangehistoryRequest.Params();
        params.setSpecNo(null);
        params.setStartDate("2020-08-23 13:00:00");
        params.setEndDate("2020-08-23 14:00:00");
        params.setWarehouseNo("wjkj03-test");
        request.setParams(params);

        WdtWmsStockspecQuerychangehistoryRequest.Pager pager = new WdtWmsStockspecQuerychangehistoryRequest.Pager();
        pager.setPageNo(1L);
        pager.setPageSize(10L);
        request.setPager(pager);
        request.putOtherTextParam("wdt3_customer_id", properties.getWdt3CustomerId());
        request.setWdtAppkey(properties.getWdtAppKey());
        request.setWdtSalt(properties.getWdtSalt());
        request.setWdtSign(QiMenUtils.getQimenCustomWdtSign(request, properties.getWdtSecret()));

        WdtWmsStockspecQuerychangehistoryResponse response = null;
        try {
            response = qimenCloudClient.execute(request);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public WdtWmsStockinOtherCreateotherstockinorderResponse testCreateOtherOut() {
        WdtWmsStockinOtherCreateotherstockinorderRequest.GoodsList goods = new WdtWmsStockinOtherCreateotherstockinorderRequest.GoodsList();
        goods.setNum("2");
        goods.setSpecNo("daba1");

        WdtWmsStockinOtherCreateotherstockinorderRequest.StockinOrder order = new WdtWmsStockinOtherCreateotherstockinorderRequest.StockinOrder();
        order.setOuterNo("outer_no");
        order.setWarehouseNo("wjkj03-test");
        order.setGoodsList(Arrays.asList(goods));

        WdtWmsStockinOtherCreateotherstockinorderRequest request = new WdtWmsStockinOtherCreateotherstockinorderRequest();
        request.setStockinOrder(order);
        request.setTargetAppKey("21363512");
        request.setDatetime(dateFormat.format(new Date()));
//		request.putOtherTextParam("wdt3_customer_id", "wdtapi3");
		request.putOtherTextParam("wdt3_customer_id", "wjkj03");
        request.setWdtAppkey(properties.getWdtAppKey());
        request.setWdtSalt(properties.getWdtSalt());
        request.setWdtSign(QiMenUtils.getQimenCustomWdtSign(request, properties.getWdtSecret()));

        WdtWmsStockinOtherCreateotherstockinorderResponse response = null;
        try {
            response = qimenCloudClient.execute(request);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
