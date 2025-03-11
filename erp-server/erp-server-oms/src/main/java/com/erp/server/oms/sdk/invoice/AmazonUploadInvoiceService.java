package com.erp.server.oms.sdk.invoice;

import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.api.FeedsApi;
import com.erp.sdk.oms.amz.spapi.enums.AmazonEndpointsEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.feeds.*;
import io.seata.common.util.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * 亚马逊上传发票
 *
 **/
@Component
public class AmazonUploadInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(AmazonUploadInvoiceService.class);
    @Resource
    private DmpAmazonFeign dmpAmazonFeign;

    public void uploadInvoice(SoB2cEntity soB2cEntity,String fileUrl,String invoiceCode) throws Exception{
        // 获取店铺授权信息
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(soB2cEntity.getShopId());
        if (null == shopInfoDTO) {
            throw new ServiceException("亚马逊店铺授权为空");
        }
        if(StringUtils.isBlank(fileUrl)){
            throw new ServiceException("发票文件地址为空");
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        FeedsApi feedsApi = FeedsApi.initApi(marketplaceEnum.getEndpointsEnum(),shopInfoDTO);
        //获取亚马逊上传文档
        CreateFeedDocumentSpecification body = new CreateFeedDocumentSpecification();
        body.setContentType("application/pdf");
        CreateFeedDocumentResponse response = feedsApi.createFeedDocument(body);
        String url = response.getUrl();
        String documentId = response.getFeedDocumentId();
        if(StringUtils.isBlank(url)|| StringUtils.isBlank(documentId)){
            throw new ServiceException("获取亚马逊上传文档失败");
        }
        InputStream inputStream = FastDFSClientUtil.getInputStream(fileUrl);
        if(Objects.isNull(inputStream)){
            throw new ServiceException("获取发票文件流失败");
        }
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut putRequest = new HttpPut(url);
            putRequest.setHeader("Content-Type", "application/pdf");
            putRequest.setEntity(new InputStreamEntity(inputStream));
            // 执行请求
            HttpResponse httpResponse = httpClient.execute(putRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200 || statusCode == 201) {
                CreateFeedSpecification createFeedSpecification = new CreateFeedSpecification();
                createFeedSpecification.setFeedType("UPLOAD_VAT_INVOICE");
                createFeedSpecification.setInputFeedDocumentId(documentId);
                createFeedSpecification.setMarketplaceIds(new ArrayList<>(shopInfoDTO.getMarketplaceShopIdMap().keySet()));
                FeedOptions feedOptions = new FeedOptions();
                feedOptions.put("OrderId",soB2cEntity.getPlatformCode());
                feedOptions.put("InvoiceNumber",invoiceCode);
                feedOptions.put("DocumentType","Invoice");
                createFeedSpecification.setFeedOptions(feedOptions);
                CreateFeedResponse createFeedResponse = feedsApi.createFeed(createFeedSpecification);
                if (null != createFeedResponse) {
                    log.error("亚马逊上传发票成功,feedId:{}", createFeedResponse.getFeedId());
                } else {
                    log.error("亚马逊上传发票,feed失败,传参:{}", JSONUtil.toJsonStr(createFeedSpecification));
                    throw new ServiceException("上传发票失败");
                }
            } else {
                throw new ServiceException("预签名url上传发票失败");
            }
        } catch (Exception e) {
            log.error("亚马逊上传发票失败", e);
            throw new ServiceException("亚马逊上传发票失败",e);
        }
    }
}
