package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.PdfUtil;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.file.dto.FileDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.sdk.oms.amz.spapi.api.FbaInboundApi;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.fulfillmentinbound.GetLabelsResponse;
import com.erp.sdk.oms.amz.spapi.utils.AmazonSpApiInitUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputInitResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.CfgAppClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaShipmentLabelInitHandler extends DmpInputAmzCommonInitHandler {
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private CfgAppClientService cfgAppClientService;
    @Resource
    private FileFeign fileFeign;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = getParentStorageMongoData();
        if (CollectionUtils.isEmpty(findMongoData)) {
            // 主数据不存在明细无需处理
            log.warn("FBA标签下载主任务taskId={},结果为空明细无需处理", dmpInputTaskEntity.getParentTaskId());
            return Collections.emptyList();
        }
        String shopId = findMongoData.get(0).getOrDefault("nextLevelId", "").toString();
        if (StringUtils.isBlank(shopId)){
            ServiceException.runError("未找到mongo中nextLevelId信息:taskId=" + dmpInputTaskEntity.getId());
        }
        // 获取店铺授权
        AmazonShopInfoDTO shopInfoDTO = cfgAppClientService.cacheAndFindShopAuth(shopId);
        // 当前站点
        AmazonMarketplaceEnum marketPlaceEnum = AmazonMarketplaceEnum.getByCountryCode(shopInfoDTO.getDictCountryCode());
        // 初始化API
        FbaInboundApi api = AmazonSpApiInitUtils.create(FbaInboundApi.class, shopInfoDTO, false);

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        for (Map<String, Object> findMongo : findMongoData) {
            String shipmentId = findMongo.get("shipmentId").toString();
            // 检查来源
            if (StringUtils.isBlank(shipmentId)) {
                String msg = StrUtil.format("FBA货件来源ID为空:{}", JSONUtil.toJsonStr(findMongo));
                throw new ServiceException(msg);
            }
            // 缓存获取结果
            String shipmentIdResultKey = StrUtil.format(RedisCacheConstants.AMZ_SP_API_RESULT_PREFIX, AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_LABEL.getBusinessTypeName(), shipmentId);
            Object resultObj = redisUtil.get(shipmentIdResultKey);
            if (null != resultObj) {
                List<JSONObject> curItemList = JSONArray.parseArray(resultObj.toString(), JSONObject.class);
                dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curItemList)));
                continue;
            }
            AmazonRequestTypeRateLimiterEnum requestTypeRateLimiterEnum = AmazonRequestTypeRateLimiterEnum.FBA_SHIPMENT_LABEL;
            // 平台请求中:平台类型:sellerId:业务类型:请求的端点区域
            String limitKey = StrUtil.format(RedisCacheConstants.PLATFORM_RATE_LIMIT, PlatformDictEnum.AMAZON.getCode(), shopInfoDTO.getPlatformShopCode(), requestTypeRateLimiterEnum.getBusinessTypeName());
            // 默认请求速率配置
            // 获取动态速率
            Object limitObj = redisUtil.get(limitKey);
            if (null != limitObj) {
                log.warn("【FBA货件标签拉取】 platformShopCode={},存在429等待恢复:放弃当前请求任务", shopInfoDTO.getPlatformShopCode());
                // 触发限流不执行当前
                DmpInputInitResponse initDmpResponse = (DmpInputInitResponse) dmpResponse;
                initDmpResponse.setDoNextStatus(false);
                return Collections.emptyList();
            }
//            String rateLimitStr = requestTypeRateLimiterEnum.getRateLimit();
            try {
                // 查询FBA货件item

                GetLabelsResponse response = api.getLabels(shipmentId, "PackageLabel_Plain_Paper", "BARCODE_2D", null, null, null, 100, 0);
                String labelUrl = "";
                if (Objects.nonNull(response) && Objects.nonNull(response.getPayload())){
                    String downloadURL = response.getPayload().getDownloadURL();
                    labelUrl = PdfUtil.convertPdfUrlToErpUrl(downloadURL, true);
                }
                JSONObject jsonObject = (JSONObject)JSON.toJSON(response);
                jsonObject.put("shipment_id",shipmentId);
                jsonObject.put("label_url",labelUrl);
                List<JSONObject> curJsonList = Collections.singletonList(jsonObject);
                // 缓存倒redis
                redisUtil.set(shipmentIdResultKey, JSONArray.toJSONString(curJsonList), 300);
                dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(curJsonList)));
            } catch (Exception e) {
                //获取异常就记录为空，erp支持手动获取
                return Collections.emptyList();
            }
            log.warn("查询亚马逊FBA货件标签成功, shipmentId={}, platformShopCode={}", shipmentId, shopInfoDTO.getPlatformShopCode());
        }
        return dmpInputTaskInitDTOList;
    }

}
