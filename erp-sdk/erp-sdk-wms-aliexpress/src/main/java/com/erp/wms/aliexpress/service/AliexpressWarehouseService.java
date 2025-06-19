package com.erp.wms.aliexpress.service;

import com.erp.wms.aliexpress.api.IopClient;
import com.erp.wms.aliexpress.api.IopClientImpl;
import com.erp.wms.aliexpress.api.IopRequest;
import com.erp.wms.aliexpress.api.IopResponse;
import com.erp.wms.aliexpress.domain.Protocol;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.util.ApiException;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliexpressWarehouseService {

    private static String CLIENT_ID = "clientId";
    private static String CLIENT_SECRET = "clientSecret";

    public static void main(String[] args) throws ApiException {
        String url = "https://api-sg.aliexpress.com";
        String appkey = "503630";
        String appSecret = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String accessToken = "50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j";
        IopClient client = new IopClientImpl(url, appkey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName("cainiao.cnap.singleitem.synchronize");
        request.addApiParameter("action_type", "add");
        AliexpressProductDTO aliexpressProductDTO = new AliexpressProductDTO();
        aliexpressProductDTO.setItemCode("0044");
        aliexpressProductDTO.setItemName("YN50MM F1.8 C");
        aliexpressProductDTO.setBarCode("0044");
        aliexpressProductDTO.setItemType("ZC");
        request.addApiParameter("item", JSONUtil.toJsonStr(aliexpressProductDTO));
        request.addApiParameter("owner_code", "17379911544");
        request.addApiParameter("warehouse_code", "STB");
        IopResponse response = client.execute(request, accessToken, Protocol.TOP);
        String result = response.getBody();
        System.out.println(response.getBody());
    }

}
