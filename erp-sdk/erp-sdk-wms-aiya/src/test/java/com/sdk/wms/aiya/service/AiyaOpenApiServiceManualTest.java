package com.sdk.wms.aiya.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.erp.model.wms.dto.AiyaInboundCancelDTO;
import com.erp.model.wms.dto.AiyaInboundQueryDTO;
import com.erp.model.wms.dto.AiyaInventoryQueryDTO;
import com.erp.model.wms.dto.AiyaSkuQueryDTO;
import com.sdk.wms.aiya.dto.response.AiyaInboundResp;
import com.sdk.wms.aiya.dto.response.AiyaOutboundResp;
import com.sdk.wms.aiya.dto.response.AiyaReturnOrderResp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIYA（爱亚）海外仓 SDK 手工验证测试类（本地暂存，不提交 git，仅供联调时按需运行）。
 * <p>
 * 覆盖 {@link AiyaOpenApiService} 当前暴露的全部接口，本次「商品数据查询/SKU」对接联调时可
 * 一并核对其它接口的签名、请求/响应结构是否与《爱亚海外仓对接方案文档》一致，重点关注：
 * <ul>
 *     <li>{@link #querySkuTest()}：{@code page}（非 pageNum）/{@code itemList}/{@code status} 字段，
 *         详见 docs/integrations/aiya-overseas-warehouse/README.md「待产品确认」；</li>
 *     <li>签名是否正确：签名失败会在 {@code AiyaOpenApiService.doQuery} 内直接抛 {@code ServiceException}；</li>
 *     <li>各接口 {@code serviceType} 常量（{@code AiyaConstants}）是否为真实值，部分仍是骨架占位命名。</li>
 * </ul>
 * 使用方式：把下面 {@code ACCESS_TOKEN}/{@code SECRET}/{@code CUSTOMER_CODE} 换成真实沙箱
 * partnerId/partnerKey/customerCode（对应 overseas_provider.auth_json），按需修改各方法业务参数后单独运行。
 * <p>
 * 注意：本类不做断言，仅打印响应，方便人工核对；未配置真实凭证时直接运行会因签名校验/网络调用失败，
 * 不属于本模块正常单测范畴，不应被 CI 或 {@code mvn test} 自动执行覆盖率统计。
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AiyaOpenApiService.class, BusinessCommonConstants.class})
public class AiyaOpenApiServiceManualTest {

    /**
     * TODO：替换为真实沙箱 partnerId（对应 overseas_provider.auth_json.partnerId）
     */
    private static final String ACCESS_TOKEN = "YQ427002_wyouqian1784167592726";//"YQ427002_wyouqian1782970582833";

    /**
     * TODO：替换为真实沙箱 partnerKey（对应 overseas_provider.auth_json.partnerKey，仅用于本地签名）
     */
    private static final String SECRET = "4b6fe5d2e97ab94a";

    /**
     * TODO：替换为真实沙箱 customerCode（对应 overseas_provider.auth_json.customerCode）
     */
    private static final String CUSTOMER_CODE = "YQ427002";

    /**
     * 账户只有生产账号，无独立测试环境；生产账号下专门划出的测试仓库编码。
     * 文档里 warehouseCode 为「是」（必填）的接口（queryTransport/queryInventory/saveInorder/
     * save2cOrder/query2cOrderPage）必须带这个仓库编码，避免误操作到真实生产仓库数据。
     * 注：batchQueryAsn（入库单批量查询 GLINK_BATCH_QUERY_ASN_NOTIFY）warehouseCode 为必填，须带该仓库编码。
     */
    private static final String TEST_WAREHOUSE_CODE = "SHENZHEN-01";

    @Resource
    private AiyaOpenApiService aiyaOpenApiService;

    // ===================== 基础数据 =====================

    @Test
    public void queryWarehouseTest() {
        JSONObject response = aiyaOpenApiService.queryWarehouse(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, null);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void querySkuTest() {
        // 真实接口要求 skus/createdTime范围/updatedTime范围 三者至少非空一组，否则报
        // INVALID_DATA: Created time and Updated time and SKUs cannot be both empty（文档未列出）
        AiyaSkuQueryDTO.QueryReqDTO reqDTO = new AiyaSkuQueryDTO.QueryReqDTO();
        reqDTO.setAccessToken(ACCESS_TOKEN);
        reqDTO.setSecret(SECRET);
        reqDTO.setCustomerCode(CUSTOMER_CODE);
        reqDTO.setPageNum(1);
        reqDTO.setPageSize(AiyaSkuQueryDTO.DEFAULT_PAGE_SIZE);
        // reqDTO.setStatus("Active"); // 可选：按状态过滤，真实大小写/取值需联调确认
        reqDTO.setCreatedTimeFrom("2026-07-01 00:00:00"); // 锚点：本次爱亚对接开发起始日期，与 AiyaSkuInitHandler 一致
        reqDTO.setCreatedTimeTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        JSONObject response = aiyaOpenApiService.querySku(reqDTO);
        System.out.println(JSONUtil.toJsonStr(response));
        // 重点核对：response.itemList 是否存在、每条 item 的 sku/name/barcode/status 字段名与文档是否一致，
        // 以及本页 itemList.size() 是否能用来判断翻页终止（见 AiyaSkuInitHandler 的 TODO）
    }

    @Test
    public void queryInventoryTest() {
        AiyaInventoryQueryDTO.QueryReqDTO reqDTO = new AiyaInventoryQueryDTO.QueryReqDTO();
        reqDTO.setAccessToken(ACCESS_TOKEN);
        reqDTO.setSecret(SECRET);
        reqDTO.setCustomerCode(CUSTOMER_CODE);
        // 文档：warehouseCode 必填；生产账号下只能用测试专属仓库编码，否则会查到真实生产仓库数据
        reqDTO.setWarehouseCode(TEST_WAREHOUSE_CODE);
        reqDTO.setPageNum(1);
        reqDTO.setPageSize(AiyaInventoryQueryDTO.DEFAULT_PAGE_SIZE);
        // stockStatus 文档标"必填"，但实测确认非必传、且最好不传（不传即查全部状态库存），故不设置
        // domainCode 为截图新发现的可选字段，接口文档未给出参数描述，暂不传，联调时可尝试传值核对作用
        JSONObject response = aiyaOpenApiService.queryInventory(reqDTO);
        System.out.println(JSONUtil.toJsonStr(response));
        // 重点核对（已按接口文档截图更新字段清单）：
        // 1) response.inventoryVOList 是否存在，每条 item 的 customerCode/warehouseCode/sku/
        //    skuDescription/barcode/skuStatus/totalQty/occupiedQty/salableQty/duePutawayQty/
        //    unavailableQty 字段名与截图是否一致；
        // 2) [已确认] stockStatus 不传即可查询全部状态库存，无需按 GOOD/DAMAGE 分别查两次；
        // 3) 仍需确认 skuStatus 是否会让同一 warehouseCode+sku 拆成多条明细（如 GOOD 一条、DAMAGE
        //    一条），这将影响 DMP 去重唯一键设计，详见 README「待产品确认」。
    }

    @Test
    public void queryTransportTest() {
        Map<String, Object> bizParams = new HashMap<>();
        // 文档：warehouseCode 必填
        bizParams.put("warehouseCode", TEST_WAREHOUSE_CODE);
        JSONObject response = aiyaOpenApiService.queryTransport(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, bizParams);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    // ===================== 入库单相关 =====================

    @Test
    public void saveInorderTest() {
        Map<String, Object> bizParams = new HashMap<>();
        // 文档：warehouseCode 必填（目的仓的仓库编码），生产账号必须带测试专属仓库编码
        bizParams.put("warehouseCode", TEST_WAREHOUSE_CODE);
        // TODO：按 AiyaConstants.GLINK_CREATE_ASN_NOTIFY 真实字段补充剩余业务参数（refNumber/markList 明细等）
        JSONObject response = aiyaOpenApiService.saveInorder(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, bizParams);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void batchQueryAsnTest() {
        // 入库单批量查询（GLINK_BATCH_QUERY_ASN_NOTIFY）：warehouseCode 必填、按上架完成时间窗口分页（pageSize 最大 200）
        AiyaInboundQueryDTO.QueryReqDTO reqDTO = new AiyaInboundQueryDTO.QueryReqDTO();
        reqDTO.setAccessToken(ACCESS_TOKEN);
        reqDTO.setSecret(SECRET);
        reqDTO.setCustomerCode(CUSTOMER_CODE);
        reqDTO.setWarehouseCode(TEST_WAREHOUSE_CODE);
        reqDTO.setPageNum(1);
        reqDTO.setPageSize(AiyaInboundQueryDTO.DEFAULT_PAGE_SIZE);
        reqDTO.setPutawayCompletedTimeFrom("2026-07-01 00:00:00");
        reqDTO.setPutawayCompletedTimeTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        AiyaInboundResp response = aiyaOpenApiService.batchQueryAsn(reqDTO);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void cancelInorderTest() {
        // cancelInorder 入参已收敛为强类型 AiyaInboundCancelDTO（仅必填 asnNumbers[]）
        AiyaInboundCancelDTO request = AiyaInboundCancelDTO.builder()
                .asnNumbers(Arrays.asList("TODO-填真实入库单号(即发货单号asnNumber)"))
                .build();
        JSONObject response = aiyaOpenApiService.cancelInorder(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, request);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    // ===================== 2C 出库单相关 =====================

    @Test
    public void save2cOrderTest() {
        Map<String, Object> bizParams = new HashMap<>();
        // 文档：warehouseCode 必填（《B2C销售订单》的[发货仓库]），生产账号必须带测试专属仓库编码
        bizParams.put("warehouseCode", TEST_WAREHOUSE_CODE);
        // TODO：按 AiyaConstants.TWO_C_ORDER_SAVE 真实字段补充剩余业务参数（收件人信息、商品明细等）
        JSONObject response = aiyaOpenApiService.save2cOrder(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, bizParams);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void search2cOrderTest() {
        List<String> noList = Arrays.asList("TODO-填真实出库单号");
        List<AiyaOutboundResp.OutboundOrderDTO> response = aiyaOpenApiService.search2cOrder(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, noList);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void query2cOrderPageTest() {
        Map<String, Object> bizParams = new HashMap<>();
        // 文档：warehouseCode 必填
        bizParams.put("warehouseCode", TEST_WAREHOUSE_CODE);
        AiyaOutboundResp response = aiyaOpenApiService.query2cOrderPage(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, 1, 100, bizParams);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void intercept2cOrderTest() {
        String no = "TODO-填真实出库单号";
        JSONObject response = aiyaOpenApiService.intercept2cOrder(ACCESS_TOKEN, SECRET, CUSTOMER_CODE, no);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    // ===================== 退货订单相关 =====================

    @Test
    public void queryReturnOrderPageTest() {
        AiyaReturnOrderResp response = aiyaOpenApiService.queryReturnOrderPage(
                ACCESS_TOKEN, SECRET, CUSTOMER_CODE, null, null, 1, 100);
        System.out.println(JSONUtil.toJsonStr(response));
    }
}
