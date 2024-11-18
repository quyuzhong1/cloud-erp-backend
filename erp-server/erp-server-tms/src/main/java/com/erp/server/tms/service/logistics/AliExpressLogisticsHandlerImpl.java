package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.enums.UnDeliverableDecisionEnum;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.channel.response.ChannelResult;
import com.erp.tms.aliexpress.model.label.request.LabelRequest;
import com.erp.tms.aliexpress.model.label.request.WarehouseOrderQuery;
import com.erp.tms.aliexpress.model.label.response.LabelResponse;
import com.erp.tms.aliexpress.model.label.response.LabelResult;
import com.erp.tms.aliexpress.model.order.request.*;
import com.erp.tms.aliexpress.model.order.response.*;
import com.erp.tms.aliexpress.model.query.request.QueryLogisticsRequest;
import com.erp.tms.aliexpress.model.query.response.LogisticsServiceResponse;
import com.erp.tms.aliexpress.model.query.response.ServiceResult;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName AliExpressLogisticsHandlerImpl
 * @date 2023年11月17日
 * @version: 1.0
 */
@Slf4j
@Component
@RefreshScope
@LogisticsPlatformType(LogisticsPlatformEnum.ALI_EXPRESS)
public class AliExpressLogisticsHandlerImpl extends AbstractLogisticsHandler {
    public static final String ORDER_ID = "orderId";
    public static final String CHILD_ORDER_ID = "childOrderId";
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private AliExpressShipperService aliExpressShipperService;
    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Value("${tms.AliExpress.orderId}")
    private String orderId;

    @Value("${tms.AliExpress.childOrderId}")
    private String childOrderId;

    @Resource
    private SoB2cFeign soB2cFeign;
    /**
     * 根据平台获取授权列表
     *
     * @param platform
     * @return
     */
    @Override
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(String platform) {
        ApiResult<List<ShopAuthEntity>> authShops = shopInfoFeign.getAuthShopByPlatformType(getPlatForm().getCode());
        if (!authShops.isSuccess() || CollectionUtils.isEmpty(authShops.getData())) return Collections.emptyList();
        //获取商铺配置信息
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return Collections.emptyList();
        }
        List<Map<String, String>> mapList = new ArrayList<>(authShops.getData().size());
        CfgAppClientEntity finalCfgAppClient = cfgAppClient;
        authShops.getData().forEach(shopAuthEntity -> {
            Map<String, String> map = new HashMap<>();
            map.put("id", finalCfgAppClient.getId());
            map.put("logisticsPlatform", getPlatForm().getCode());
            map.put("clientSecret", finalCfgAppClient.getClientSecret());
            map.put("clientId", finalCfgAppClient.getClientId());
            map.put("url", finalCfgAppClient.getUrl());
            map.put("token", shopAuthEntity.getToken());
            map.put("shopId", shopAuthEntity.getShopId());
            map.put(ORDER_ID, orderId);
            map.put(CHILD_ORDER_ID,childOrderId);
            mapList.add(map);
        });
        return mapList;
    }

    /**
     * 速卖通  authId 需要是店铺 shopId
     *
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_LOGISTICS;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = null;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return new HashMap<>() ;
        }
        if (Objects.isNull(cfgAppClient)) return new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        map.put(ORDER_ID, orderId);
        map.put(CHILD_ORDER_ID,childOrderId);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                map.put("token", shopAuth.getAccessToken());
            }
        }
        return map;
    }

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     */
    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
        OrderRequest orderRequest = processCreateOrderData(logisticsOrderVO);
        boolean success = false;
        OrderResult response = null;
        ValidatorUtil.validateEntity(orderRequest);
        try {
            response = aliExpressShipperService.createOrder(logisticsOrderVO.getAuthMap(), orderRequest);
            //转换实体
            if (!Objects.isNull(response) && !Objects.isNull(response.getResultSuccess()) && Boolean.TRUE.equals(response.getResultSuccess())) {
                OrderResponse orderResponse = response.getResult();
                responseVO.setDeliveryNo(orderResponse.getTradeOrderId());
                responseVO.setTransportNo(orderResponse.getOutOrderCode());
                success = true;
                responseVO.success();
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response), false);
            } else {
                if(Objects.isNull(response)){
                    throw new ServiceException("创建订单失败，返回结果为空");
                }
                String msg = Objects.nonNull(response.getResult()) ? response.getResult().getErrorDesc() : response.getErrorResponse().getMsg();
                responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), msg);
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(response), false);
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            responseVO.failure(getPlatForm().getName(), logisticsOrderVO.getDeliveryNo(), e.getMessage());
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(e), true);
        }
        return success ? success(responseVO) : failure(responseVO);
    }

    /**
     * 订单数据整理
     *
     * @param logisticsOrderVO
     * @return
     */
    private OrderRequest processCreateOrderData(LogisticsOrderVO logisticsOrderVO) {
        String oaid = logisticsOrderVO.getOaid();
        String orderType = logisticsOrderVO.getOrderType();
        //申报产品信息
        List<DeclareProduct> declareProducts = LogisticsOrderConverter.INSTANCE.orderRequestProductByAliExpress(logisticsOrderVO.getLogisticsProductVOList());
        //收寄信息
        AddressDTO addressDTO = new AddressDTO();
        if (Objects.nonNull(logisticsOrderVO.getSenderInfo())){
            Address sender = LogisticsOrderConverter.INSTANCE.orderRequestSendUserByAliExpress(logisticsOrderVO);
            addressDTO.setSender(sender);
        }
        if (Objects.nonNull(logisticsOrderVO.getPickUpInfo())){
            Address pickUp = LogisticsOrderConverter.INSTANCE.orderRequestPickUpUserByAliExpress(logisticsOrderVO);
            addressDTO.setPickup(pickUp);
        }
        if (Objects.nonNull(logisticsOrderVO.getReturnInfo())){
            Address refund = LogisticsOrderConverter.INSTANCE.orderRequestRefundUserByAliExpress(logisticsOrderVO);
            addressDTO.setRefund(refund);
        }
        if (Objects.nonNull(logisticsOrderVO.getReceiverInfoVO())){
            Address receiver = LogisticsOrderConverter.INSTANCE.orderRequestReceiverUserByAliExpress(logisticsOrderVO);
            addressDTO.setReceiver(encryptByOrderType(orderType,receiver));
        }
        if(Objects.isNull(addressDTO.getPickup())){
            Address sender = addressDTO.getSender();
            addressDTO.setPickup(sender);
            addressDTO.getPickup().setMemberType("pickup");
        }
        String undeliverableDecision = "1";
        if (!StringUtils.isBlank(logisticsOrderVO.getLogisticsChannelEntity().getUndeliverableDecision()) && UnDeliverableDecisionEnum.RETURN.getCode().equals(logisticsOrderVO.getLogisticsChannelEntity().getUndeliverableDecision())){
            undeliverableDecision = "0";
        }
        return OrderRequest.builder()
                .oaid(oaid)
                .pickup_type(logisticsOrderVO.getLogisticsChannelEntity().getDeliveryType())
                .undeliverable_decision(undeliverableDecision)
                .declareProducts(declareProducts)
                .domestic_logistics_company("自送")
                .domestic_logistics_company_id(-1L)
//                .domestic_tracking_no(logisticsOrderVO.getDeliveryNo())
                .package_num(logisticsOrderVO.getParceInfoVO().getTotalQuantity())
                .trade_order_from(logisticsOrderVO.getOrderSource())
                .trade_order_id(logisticsOrderVO.getDeliveryNo())
                .undeliverable_decision("1")
                .warehouse_carrier_service(logisticsOrderVO.getLogisticsChannelEntity().getCode())
                //托寄物信息
                .address_d_t_os(addressDTO)
                .is_agree_upgrade_reverse_parcel_insure(false)
                .top_user_key(logisticsOrderVO.getTopUserKey())
                .build();
    }
    private Address encryptByOrderType(String orderType, Address address){
        if (!StringUtils.isBlank(orderType) && !SourceTypeEnum.SELF_ADD.getCode().equals(orderType)){
            return address;
        }
        //除了第一个姓其他的转换成*
        String name = address.getName();
        address.setName(maskExceptFirstChar(name));
        //隐藏后四位之前的四位
        String phone = address.getPhone();
        address.setPhone(maskLastFourBeforeFour(phone));
        //隐藏街道地址从后向前8位字符
        String streetAddress = address.getStreetAddress();
        address.setStreetAddress(maskLastEightChars(streetAddress));
        //email：隐藏@前的 1/3 字符，至少一个字符
        String email = address.getEmail();
        address.setEmail(obfuscateEmail(email));
        return address;
    }

    public static String obfuscateEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null; // 如果邮箱无效，直接返回原字符串
        }

        String[] parts = email.split("@", 2); // 分割邮箱为用户名和域名两部分
        String username = parts[0];
        String domain = parts[1];

        // 计算要隐藏的字符数，至少隐藏一个字符
        int lengthToHide = Math.max(1, username.length() / 3);

        // 使用星号(*)隐藏指定数量的字符
        StringBuilder obfuscatedUsername = new StringBuilder();
        for (int i = 0; i < lengthToHide; i++) {
            obfuscatedUsername.append("*");
        }
        // 如果用户名还有剩余字符，则追加剩余部分
        if (username.length() > lengthToHide) {
            obfuscatedUsername.append(username.substring(lengthToHide));
        }

        // 拼接隐藏后的用户名和原域名
        return obfuscatedUsername.toString() + "@" + domain;
    }
    /**
     * 姓名： 按照隐私开头三个字母后的所有字符，如果姓名不足3个字符，则至少隐去1个字符；
     * 例如 Андрей, Борис->Анд*** Joseph Jacques Césaire Joffre->Jos*** Georges Clemenceau->Geo***
     *
     * Name test ==========
     * 惊允 -> 惊*
     * 惊允允 -> 惊**
     * 惊允允允 -> 惊允允*
     * Mi chael -> Mi c****
     * Michael -> Mic****
     * Андрей, Борис -> Анд*********
     * Joseph Jacques Césaire Joffre -> Jos***********************
     * Georges Clemenceau -> Geo**************
     *
     * @param str
     * @return
     */
    public static String maskExceptFirstChar(String str) {
        if (StringUtils.isBlank(str)) {
            // 如果字符串为空或只有一个字符，直接返回原字符串
            return str;
        }
        StringBuilder sb = new StringBuilder();
        if (str.length() <= 3){
            // 添加第一个字符
            sb.append(str.charAt(0));
            // 从第二个字符开始，全部替换为'*'
            for (int i = 1; i < str.length(); i++) {
                sb.append('*');
            }
            return sb.toString();
        }
        // 添加前三个字符
        sb.append(str, 0, 3); // 添加前缀
        // 从第二个字符开始，全部替换为'*'
        for (int i = 3; i < str.length(); i++) {
            sb.append('*');
        }
        // 将StringBuilder转换回String
        return sb.toString();
    }

    /**
     * 隐藏后四位之前的四位
     * 电话： 隐去中间一半的字符，例如： 12345678901->123****1901, +12345678-> +12***78
     *
     * Phone test ==========
     * 1 -> *
     * 12 -> 1*
     * 123 -> 1*
     * 1234 -> 1**4
     * 1234567 -> 12***67
     * 12345678 -> 12****78
     * 123456789 -> 12****789
     * 13339561234 -> 133*****234
     * 1333956123456 -> 133******3456
     * 8613339561234 -> 861******1234
     * 0113339561234 -> 011******1234
     *
     * @param str
     * @return
     */
    public static String maskLastFourBeforeFour(String str) {
        int length = str.length();
        if (str.length() <= 1){
            return "*";
        }
        StringBuilder sb = new StringBuilder();
        String sb1 = handleStr(str, length, sb);
        if (sb1 != null) return sb1;
        // 计算需要隐藏字符的起始位置
        // 如果字符串长度减去8小于4，则从字符串开头隐藏到可能的最大位置
        int start = Math.max(0, length - 8);
        // 使用StringBuilder来构建结果字符串
        sb.append(str, 0, start); // 添加前缀
        if (length - 4 > start) { // 如果还有空间可以隐藏字符
            for (int i = 0; i < 4; i++) {
                sb.append('*'); // 添加四个*
            }
        }
        sb.append(str, length - 4, length); // 添加最后四位
        return sb.toString();
    }

    private static @Nullable String handleStr(String str, int length, StringBuilder sb) {
        if (length < 3) {
            // 添加第一个字符
            sb.append(str.charAt(0));
            for (int i = 1; i < str.length(); i++) {
                sb.append('*');
            }
            return sb.toString();
        }
        // 如果字符串长度小于8，则无法隐藏后四位之前的四个字符，直接返回原字符串
        if (length < 9) {
            int start = 2;
            // 使用StringBuilder来构建结果字符串
            sb.append(str, 0, start); // 添加前缀
            if (length - 2 > start) { // 如果还有空间可以隐藏字符
                for (int i = 0; i < 2; i++) {
                    sb.append('*'); // 添加四个*
                }
            }
            sb.append(str, length - 2, length); // 添加最后四位
            return sb.toString();
        }
        if (length < 12) {
            int start = 3;
            // 使用StringBuilder来构建结果字符串
            sb.append(str, 0, start); // 添加前缀
            if (length - 3 > start) { // 如果还有空间可以隐藏字符
                for (int i = 0; i < 3; i++) {
                    sb.append('*'); // 添加四个*
                }
            }
            sb.append(str, length - 3, length); // 添加最后四位
            return sb.toString();
        }
        return null;
    }

    public static String maskLastEightChars(String address) {
        if (address == null || address.length() < 8) {
            // 如果字符串为空或长度小于8，直接返回原字符串
            return address;
        }
        // 使用StringBuilder来构建结果字符串
        StringBuilder sb = new StringBuilder(address);
        // 从后向前替换最后8个字符为*
        for (int i = address.length() - 1; i >= address.length() - 8 && i >= 0; i--) {
            sb.setCharAt(i, '*');
        }
        // 将StringBuilder转换回String
        return sb.toString();
    }
    /**
     * 查询订单(批量)
     *
     * @param logisticsQueryVOList
     * @return
     */
    @Override
    public ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(List<LogisticsQueryBaseVO> logisticsQueryVOList) {
        if (logisticsQueryVOList.size() > 20){
            throw new ServiceException("速卖通：单次打印面单不能超过20个");
        }
        List<LogisticsOrderResponseVO> list = new ArrayList<>();
        boolean isSuccess = true;
        for (LogisticsQueryBaseVO logisticsQueryBaseVO:logisticsQueryVOList) {
            QueryOrderRequest queryOrderRequest = QueryOrderRequest.builder()
                    .current_page(1)
                    .page_size(20)
                    .trade_order_id(logisticsQueryBaseVO.getDeliveryNo())
                    .build();
            BaseResult iopResponse = null;
            ValidatorUtil.validateEntity(queryOrderRequest);
            try {
                iopResponse = aliExpressShipperService.queryLogisticsOrder(logisticsQueryBaseVO.getAuthMap(), queryOrderRequest);
                ErrorResponse errorResponse = iopResponse.getErrorResponse();
                if (Objects.nonNull(errorResponse)){
                    LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
                    responseVO.failure(getPlatForm().getName(), logisticsQueryBaseVO.getDeliveryNo(), errorResponse.getMsg());
                    list.add(responseVO);
                    isSuccess = false;
                    continue;
                }
                QueryResponse queryResponse = JSON.parseObject(iopResponse.getResult(), QueryResponse.class);
                //失败
                isSuccess = handleQuery(logisticsQueryBaseVO, queryResponse, iopResponse, list, isSuccess);
            } catch (ApiException e) {
                logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                        logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(e.getMessage()));
                LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
                responseVO.failure(getPlatForm().getName(), logisticsQueryBaseVO.getDeliveryNo(), e.getMessage());
                list.add(responseVO);
            }

        }
        return isSuccess? success(list):failure(list);
    }

    private boolean handleQuery(LogisticsQueryBaseVO logisticsQueryBaseVO, QueryResponse queryResponse, BaseResult iopResponse, List<LogisticsOrderResponseVO> list, boolean isSuccess) {
        if (Objects.isNull(queryResponse) || Objects.isNull(queryResponse.getSuccess()) || Boolean.TRUE.equals(!queryResponse.getSuccess())) {
            logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                    logisticsQueryBaseVO.getDeliveryNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(iopResponse));
            LogisticsOrderResponseVO responseVO = new LogisticsOrderResponseVO();
            if(Objects.nonNull(queryResponse)){
                responseVO.failure(getPlatForm().getName(), logisticsQueryBaseVO.getDeliveryNo(), queryResponse.getErrorDesc());
                list.add(responseVO);
            }
            isSuccess = false;
        } else {
            List<QueryResult> responses = queryResponse.getResultList();
            if (CollectionUtils.isNotEmpty(responses)) {
                QueryResult queryResult = responses.stream().filter(e -> !StringUtils.isBlank(e.getLogistics_order_id()) && logisticsQueryBaseVO.getTransportNo().equals(e.getOut_order_code())).findFirst().orElse(null);
                if (Objects.nonNull(queryResult)){
                    LogisticsOrderResponseVO orderResponseVO = LogisticsOrderResponseVO.builder()
                            .transportNo(queryResult.getOut_order_code())
                            .trackNo(queryResult.getInternational_logistics_num())
                            .deliveryNo(queryResult.getTrade_order_id())
                            .logisticsChannelNo(queryResult.getLogistics_service_list().get(0).getCode())
                            .build();
                    logisticsOperateService.pullOperateLog(logisticsQueryBaseVO.getOrderId(),
                            logisticsQueryBaseVO.getTransportNo(), BusinessTypeEnum.QUERY_ORDER.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                            RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryBaseVO), JSONUtil.toJsonStr(responses));
                    list.add(orderResponseVO);
                }
            }
        }
        return isSuccess;
    }

    /**
     * 根据查询记录获取 面单编号 然后进行面单查询
     *
     * @param logisticsQueryVO
     * @return
     * @throws IOException
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        return this.getLabel(logisticsQueryVO);
    }

    /**
     * 获取标签
     * request_no 请求单号（支持4PX单号、客户单号和面单号
     *
     * @param logisticsQueryVO
     * @return
     */

    public ApiResult<List<LogisticsPrintLabelResponse>> getLabel(List<LogisticsGetLabelVO> logisticsQueryVO) throws IOException {
        List<LogisticsPrintLabelResponse> responses = new ArrayList<>();
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsQueryVO.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        List<WarehouseOrderQuery> warehouseOrderQueries = new ArrayList<>(logisticsQueryVO.size());
        logisticsQueryVO.stream().forEach(logisticsGetLabelVO1 -> {
            WarehouseOrderQuery warehouseOrderQuery = new WarehouseOrderQuery();
            warehouseOrderQuery.setInternational_logistics_id(logisticsGetLabelVO1.getTrackNo());
            warehouseOrderQueries.add(warehouseOrderQuery);
        });
        LabelRequest labelRequest = LabelRequest.builder()
                .print_detail(false)
                .warehouseOrderQueries(warehouseOrderQueries)
                .build();
        IopResponse iopResponse = null;
        ValidatorUtil.validateEntity(labelRequest);
        try {
            LogisticsPrintLabelResponse response = new LogisticsPrintLabelResponse();
            iopResponse = aliExpressShipperService.getLabelList(logisticsGetLabelVO.getAuthMap(), labelRequest);

            if (!StringUtils.isEmpty(iopResponse.getMessage())){
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(iopResponse));
                response.failure(LogisticsPlatformEnum.ALI_EXPRESS.getName(), logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.joining(",")), iopResponse.getMessage());
                responses.add(response);
                return failure(responses);
            }
            LabelResult labelList = JSON.parseObject(iopResponse.getBody(), LabelResult.class);
            LabelResponse labelResponse = JSON.parseObject(labelList.getResult(), LabelResponse.class);
            //失败
            if (Objects.isNull(labelList.getResult()) || Objects.isNull(labelResponse) || !StringUtils.isBlank(labelResponse.getErrorDesc())) {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                response.failure(LogisticsPlatformEnum.ALI_EXPRESS.getName(), logisticsQueryVO.stream().map(LogisticsGetLabelVO::getDeliveryNo).collect(Collectors.joining(",")), Objects.isNull(labelResponse)?"速卖通获取物流单失败":labelResponse.getErrorDesc());
                responses.add(response);
                return failure(responses);
            } else {
                logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                        logisticsGetLabelVO.getDeliveryNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(labelList));
                //结果："http://bss-fss.i4px.com/fpx-print-label-e1298724-0b8d-4be3-8238-bd7a96d9874b.pdf" 需要考虑 pdf转图片
                String prefix = "data:application/pdf;base64,";
                response = LogisticsPrintLabelResponse.builder()
                        .transportNoList(logisticsQueryVO.stream().map(LogisticsGetLabelVO::getTransportNo).collect(Collectors.toList()))
                        .base64(prefix + labelResponse.getBody()).build();
                response.success();
                responses.add(response);
                return success(responses);
            }
        } catch (ApiException e) {
            log.error("速卖通getLabelList接口调用失败：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(logisticsGetLabelVO.getOrderId(),
                    logisticsGetLabelVO.getTransportNo(), BusinessTypeEnum.GET_LABEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsQueryVO), JSONUtil.toJsonStr(iopResponse));
            return failure(e.getMessage());
        }
    }


    /**
     * 渠道查询
     *
     * @param chanelQueryVO
     * @return
     */

    /**
     * 订单可用服务列表
     *
     * @param chanelQueryVO
     * @return
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        //为空时去授权找订单id--增加容错
        if (Objects.isNull(chanelQueryVO.getOrderId())){
            chanelQueryVO.setOrderId(chanelQueryVO.getAuthMap().get(ORDER_ID));
        }
        if (Objects.isNull(chanelQueryVO.getChildOrderId())){
            chanelQueryVO.setChildOrderId(chanelQueryVO.getAuthMap().get(CHILD_ORDER_ID));
        }
        if (Objects.isNull(chanelQueryVO.getOrderId()) || Objects.isNull(chanelQueryVO.getChildOrderId())){
            throw new ServiceException("速卖通：获取订单可用服务时，订单编号不能为空");
        }
        QueryLogisticsRequest child =  QueryLogisticsRequest.builder()
                .order_id(Long.valueOf(chanelQueryVO.getChildOrderId()))
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
//                .order_id(1102175972276889L)
                .build();
        QueryLogisticsRequest request =  QueryLogisticsRequest.builder()
                .order_id(Long.valueOf(chanelQueryVO.getOrderId()))
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
                .sub_order_list(Collections.singletonList(child))
                .build();
        try {
            IopResponse iopResponse = aliExpressShipperService.getLogisticsService(chanelQueryVO.getAuthMap(),request);
            if (Objects.isNull(iopResponse) || !StringUtils.isEmpty(iopResponse.getMessage())){
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(iopResponse));
                return failure(getPlatForm().getName() + ":" + iopResponse.getMessage());
            }
            LogisticsServiceResponse responseMsg = JSON.parseObject(iopResponse.getBody(), LogisticsServiceResponse.class);
            //失败
            if (Objects.isNull(responseMsg) || !StringUtils.isEmpty(responseMsg.getErrorDesc())) {
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return failure(getPlatForm().getName() + ":" + responseMsg.getErrorDesc());
            } else {
                List<ServiceResult> serviceResults = responseMsg.getResultResponse().getResultList();
                logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                        chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(responseMsg));
                return success(LogisticsChannelConverter.INSTANCE.serviceConvertByAliExpress(serviceResults));
            }
        } catch (Exception e) {
            log.error("速卖通getChannel接口调用失败：{}", e.getMessage());
            logisticsOperateService.pullOperateLog(chanelQueryVO.getOrderId(),
                    chanelQueryVO.getTransportMode(), BusinessTypeEnum.GET_CHANEL_LIST.getCode(), LogisticsPlatformEnum.ALI_EXPRESS.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(chanelQueryVO), JSONUtil.toJsonStr(e));
            return failure(e.getMessage());
        }

    }
    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        try {
            ChannelResult responseMsg = aliExpressShipperService.getChanelList(authMap);
            if (Objects.isNull(responseMsg) || Objects.isNull(responseMsg.getResultSuccess()) || Boolean.TRUE.equals(!responseMsg.getResultSuccess())) {
                //授权失败
                return failure("授权失败");
            } else {
                return success("授权成功");
            }
        } catch (Exception e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }
    /**
     * 获取到速卖通支持的物流服务列表
     * @description
     * @param
     * @return
     * @date 2024-03-04 15:22
     * @author Lambda
     */
    @Override
    public ApiResult<List<LogisticsServiceResponseVO>>  listLogisticsService(Map<String, String> authMap){
        try {
            IopResponse  response= aliExpressShipperService.listLogisticsService(authMap);
            String body = response.getBody();
            JSONObject jsonObject= JSON.parseObject(body);
            if(jsonObject.containsKey("result_list")){
                List<LogisticsServiceDTO> list = JSON.parseArray(jsonObject.getString("result_list"), LogisticsServiceDTO.class);
                List<LogisticsServiceResponseVO> result=new ArrayList<>(list.size());
                for (LogisticsServiceDTO item : list) {
                    LogisticsServiceResponseVO vo = new LogisticsServiceResponseVO();
                    vo.setServiceName(item.getDisplayName());
                    vo.setLogisticsType(item.getServiceName());
                    result.add(vo);
                }
                return success(result);
            }else{
                return success(Collections.emptyList());
            }
        } catch (ApiException e) {
            return failure(getPlatForm().getName() + ":" + e.getMessage());
        }
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.ALI_EXPRESS;
    }
}
