package com.erp.tms.aliexpress.service;

import com.erp.tms.aliexpress.api.IopClient;
import com.erp.tms.aliexpress.api.IopClientImpl;
import com.erp.tms.aliexpress.api.IopRequest;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.constants.PathConstants;
import com.erp.tms.aliexpress.domain.Protocol;
import com.erp.tms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zdy
 * @ClassName AliExpressShipperService
 * @description: TODO
 * @date 2023年11月17日
 * @version: 1.0
 */
@Slf4j
@Component
public class AliExpressShipperService {
    static String accessToken = "";
    public void getChanelList() throws ApiException, InterruptedException {
        IopClient client = new IopClientImpl(PathConstants.BASE_URL, PathConstants.APP_KEY, PathConstants.APP_SECRET);
        IopRequest request = new IopRequest();
        request.setApiName("aliexpress.logistics.redefining.listlogisticsservice");
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        System.out.println(response.getBody());
        Thread.sleep(10);
    }
}
