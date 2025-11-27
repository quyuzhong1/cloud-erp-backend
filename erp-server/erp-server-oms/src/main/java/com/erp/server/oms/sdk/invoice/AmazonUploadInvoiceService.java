package com.erp.server.oms.sdk.invoice;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.sdk.oms.amz.spapi.api.FeedsApi;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.model.feeds.*;
import io.seata.common.util.StringUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
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

    public String uploadInvoice(SoB2cEntity soB2cEntity,String fileUrl,String invoiceCode) throws Exception{
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
        byte[] fileByte = FastDFSClientUtil.getFileByte(fileUrl);
        if(Objects.isNull(fileByte)){
            throw new ServiceException("获取发票文件流失败");
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type","application/pdf")
                .put(RequestBody.create(MediaType.parse("application/pdf"), fileByte))
                .build();

        Response preSignResponse = client.newCall(request).execute();
        if (!preSignResponse.isSuccessful()) {
            log.error("亚马逊预签名上传发票失败,{}", preSignResponse.code() + preSignResponse.message());
            throw new ServiceException("亚马逊预签名上传发票失败,code:{},msg:{}",preSignResponse.code(),preSignResponse.message());
        }else{
            CreateFeedSpecification createFeedSpecification = new CreateFeedSpecification();
            createFeedSpecification.setFeedType("UPLOAD_VAT_INVOICE");
            createFeedSpecification.setInputFeedDocumentId(documentId);
            createFeedSpecification.setMarketplaceIds(Collections.singletonList(marketplaceEnum.getMarketplaceId()));
            FeedOptions feedOptions = new FeedOptions();
            feedOptions.put("metadata:OrderId",soB2cEntity.getPlatformCode());
            feedOptions.put("metadata:InvoiceNumber",invoiceCode);
            feedOptions.put("metadata:DocumentType","Invoice");
            createFeedSpecification.setFeedOptions(feedOptions);
            CreateFeedResponse createFeedResponse = feedsApi.createFeed(createFeedSpecification);
            if (null != createFeedResponse) {
                log.error("亚马逊上传发票成功,feedId:{}", createFeedResponse.getFeedId());
                return createFeedResponse.getFeedId();
            } else {
                log.error("亚马逊上传发票,feed失败,传参:{}", JSONUtil.toJsonStr(createFeedSpecification));
                throw new ServiceException("上传发票失败");
            }
        }
    }


    /**
     * 获取发票处理结果
     */
    public ApiResult<Object> getInvoiceResult(String feedId,String shopId) throws Exception{
        AmazonShopInfoDTO shopInfoDTO = dmpAmazonFeign.getShopAuth(shopId);
        if (null == shopInfoDTO) {
            throw new ServiceException("亚马逊店铺授权为空");
        }
        AmazonMarketplaceEnum marketplaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        FeedsApi feedsApi = FeedsApi.initApi(marketplaceEnum.getEndpointsEnum(),shopInfoDTO);
        Feed feed = feedsApi.getFeed(feedId);
        if(Objects.isNull(feed)){
            return ApiResult.error("亚马逊获取feed结果为空");
        }
        if(Feed.ProcessingStatusEnum.CANCELLED.equals(feed.getProcessingStatus())){
            return ApiResult.error("亚马逊已取消上传发票");
        }
        if(Feed.ProcessingStatusEnum.FATAL.equals(feed.getProcessingStatus())){
            return ApiResult.error("亚马逊上传发票失败");
        }
        if(!Feed.ProcessingStatusEnum.DONE.equals(feed.getProcessingStatus())){
            return ApiResult.error(300,"处理中");
        }
        if(StringUtils.isBlank(feed.getResultFeedDocumentId())){
            return ApiResult.error("亚马逊获取结果文档为空");
        }
        FeedDocument feedDocument =  feedsApi.getFeedDocument(feed.getResultFeedDocumentId());
        if(Objects.isNull(feedDocument) || StringUtils.isBlank(feedDocument.getUrl())){
            return ApiResult.error("亚马逊获取结果文档为空");
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(feedDocument.getUrl())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return ApiResult.error("读取亚马逊文档失败"+response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                // 获取压缩内容
                byte[] compressedContent = responseBody.bytes();

                // 将解压缩的内容转换为字符串
                String content = new String(compressedContent);
                String key = "Number of records successful";
                int keyIndex = content.indexOf(key);
                if (keyIndex == -1) {
                    throw new IllegalArgumentException("Key not found: " + key);
                }

                // 找到关键字后的值的位置
                int valueStartIndex = keyIndex + key.length();
                String remainingText = content.substring(valueStartIndex).trim();

                // 提取值（假设值是一个整数）
                StringBuilder valueBuilder = new StringBuilder();
                for (char c : remainingText.toCharArray()) {
                    if (Character.isDigit(c)) {
                        valueBuilder.append(c);
                    } else {
                        break; // 遇到非数字字符时停止
                    }
                }
                // 将提取的值转换为整数
                int a = Integer.parseInt(valueBuilder.toString());
                if(1 == a){
                    return ApiResult.success();
                }else{
                    return ApiResult.error(CharSequenceUtil.format("亚马逊处理发票失败,处理结果：{}",content));
                }
            }
        }
        return ApiResult.error("亚马逊处理发票失败");
    }


}
