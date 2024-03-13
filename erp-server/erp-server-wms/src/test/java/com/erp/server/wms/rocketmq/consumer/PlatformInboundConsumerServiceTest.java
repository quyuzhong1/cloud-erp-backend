package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.PlatformInboundDTO;
import com.common.business.enums.OverseasInstockStatusEnum;
import com.common.business.enums.WarehousePlatformTypeEnum;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.erp.server.wms.ErpServerWmsApplication;
import com.sdk.wms.iml.dto.response.ImlProductResp;
import com.sdk.wms.iml.dto.response.ImlResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformInboundConsumerServiceTest {

    @Resource
    private KingdeeB2CSoOutstockConsumer service;

    @Test
    public void handleTest() {
        String json = " { \"uid\": \"89\",\"userName\": \"userName_b5ac956ceb75\",\"realName\": \"realName_8b96a3eddf6b\",\"mobile\": \"mobile_b9f223db92d4\",\"userAccount\": \"userAccount_79da526a2a23\",\"accessToken\": \"accessToken_781996e1abec\",\"bindingPlatform\": \"bindingPlatform_da261b9f43d8\",\"menuList\": [\"menuList_a57ee08c1209\"],\"permissionList\": [\"permissionList_dd9c46aadd7b\"]}";

        LoginUser loginUser = JSONObject.parseObject(json,new TypeReference<LoginUser>() {}.getType());
        CommonInterceptor.threadLocal.set(loginUser);
        String json2 = "{\"_id\":\"65eee50b4f027c03e957f33a\",\"dataSources\":\" \",\"downloadTime\":\"2024-03-11 19:03:39\",\"fApproveDate\":\"2024-03-11T17:43:35.943\",\"fApproverName\":\"祝梦彬\",\"fBillAllAmount\":79.0,\"fBillAllAmount_LC\":79.0,\"fBillNo\":\"XSCKD7212883\",\"fBillTypeID\":\"5580d51e423ec9\",\"fBillTypeName\":\"B2C销售出库单\",\"fBussinessType\":\"NORMAL\",\"fCancelStatus\":\"A\",\"fCarriageNO\":\"JDVC22808535186\",\"fCreateDate\":\"2024-01-23T11:10:06.007\",\"fCreatorName\":\"Administrator\",\"fCustomerID\":\"330099\",\"fCustomerName\":\"京东-Ulanzi旗舰店\",\"fCustomerNumber\":\"801001\",\"fDate\":\"2024-01-23T00:00:00\",\"fDocumentStatus\":\"C\",\"fEThirdBillNo\":\"SDO697019958168\",\"fEntryCostAmount\":\"0.0\",\"fExchangeRate\":\"1.0\",\"fGyDate\":\"2024-01-23T11:05:52\",\"fId\":\"9383303\",\"fIsGenForIos\":false,\"fLinkMan\":\" \",\"fLinkPhone\":\" \",\"fLogisticsNos\":\" \",\"fModifierName\":\"Administrator\",\"fModifyDate\":\"2024-01-23T11:10:06.57\",\"fReceiveAddress\":\"80663c274e4c42461edd6331a39f6b73\",\"fReceiverName\":\"京东-Ulanzi旗舰店\",\"fSaleDeptName\":\"京东组\",\"fSaleOrgId\":\"260365\",\"fSaleOrgName\":\"深圳市优篮子科技有限公司\",\"fSalesManID\":\"0\",\"fSalesManName\":\"null\",\"fSalesManNumber\":\"null\",\"fSettleCurrCode\":\"CNY\",\"fStockerName\":\"null\",\"fStockerNumber\":\"null\",\"f_ulz_BaseProperty2\":\"京东\",\"f_ulz_BaseProperty2Code\":\"007\",\"f_ulz_Text3\":\" \",\"isClean\":0,\"kingdeeOutStockItemEntityList\":[{\"fAllAmount\":79.0,\"fAllAmount_LC\":79.0,\"fAmount\":\"69.91\",\"fAmount_LC\":69.91,\"fArrivalDate\":\"null\",\"fArrivalStatus\":\" \",\"fBarcode\":\" \",\"fBillNo\":\"XSCKD7212883\",\"fCostAmount_LC\":0.0,\"fCostPrice\":0.0,\"fCustMatName\":\"null\",\"fDocumentStatus\":\"C\",\"fEntryId\":\"15171293\",\"fEntryNote\":\"组合商品：3173+C059GBB1\",\"fExchangeRate\":\"1.0\",\"fIsFree\":\"false\",\"fMateriaModel\":\" \",\"fMateriaType\":\"原材料\",\"fMaterialID\":\"305438\",\"fMaterialName\":\"Ulanzi 九合一相机清洁套装\",\"fMaterialNumber\":\"3173\",\"fNote\":\" \",\"fPrice\":\"69.911504\",\"fRealQty\":\"1.0\",\"fSalCostPrice\":0.0,\"fSrcBillNo\":\" \",\"fSrcType\":\" \",\"fStockLoc\":\"0\",\"fStockName\":\"B2C京东官方仓\",\"fStockNumber\":\"jp-jdzyc\",\"fStockStatusID\":\"10000\",\"fStockStatusName\":\"可用\",\"fTaxPrice\":\"79.0\",\"fUnitName\":\"Pcs\",\"f_ulz_BaseProperty1\":\" \",\"f_ulz_Text1\":\" \"},{\"fAllAmount\":0.0,\"fAllAmount_LC\":0.0,\"fAmount\":\"0.0\",\"fAmount_LC\":0.0,\"fArrivalDate\":\"null\",\"fArrivalStatus\":\" \",\"fBarcode\":\" \",\"fBillNo\":\"XSCKD7212883\",\"fCostAmount_LC\":0.0,\"fCostPrice\":0.0,\"fCustMatName\":\"null\",\"fDocumentStatus\":\"C\",\"fEntryId\":\"15171294\",\"fEntryNote\":\"组合商品：3173+C059GBB1\",\"fExchangeRate\":\"1.0\",\"fIsFree\":\"false\",\"fMateriaModel\":\"CO25\",\"fMateriaType\":\"原材料\",\"fMaterialID\":\"1511220\",\"fMaterialName\":\"小湿布 超纤维数码清洁布（50个装）\",\"fMaterialNumber\":\"C059GBB1\",\"fNote\":\" \",\"fPrice\":\"0.0\",\"fRealQty\":\"1.0\",\"fSalCostPrice\":0.0,\"fSrcBillNo\":\" \",\"fSrcType\":\" \",\"fStockLoc\":\"0\",\"fStockName\":\"B2C京东官方仓\",\"fStockNumber\":\"jp-jdzyc\",\"fStockStatusID\":\"10000\",\"fStockStatusName\":\"可用\",\"fTaxPrice\":\"0.0\",\"fUnitName\":\"Pcs\",\"f_ulz_BaseProperty1\":\" \",\"f_ulz_Text1\":\" \"}]}";
        DmpSyncMqDTO dmpSyncMqDTO = new DmpSyncMqDTO();
        dmpSyncMqDTO.setMqData(json2);
        dmpSyncMqDTO.setDmpSyncTaskId("1767438246767235074");
        PlatformInboundDTO dto = JSONObject.parseObject(json2,new TypeReference<PlatformInboundDTO>() {}.getType());
//        PlatformInboundDTO dto = JSONObject.parseObject(json,new TypeReference<PlatformInboundDTO>() {}.getType());
//        PlatformInboundDTO dto = new PlatformInboundDTO();
//        dto.setWarehousePlatformType(WarehousePlatformTypeEnum.OVERSEAS_WAREHOUSE.code);
//        dto.setReceivingCode("RVG1149-231207-0001");
//        dto.setProvider("goodcang");
//        dto.setPlatform("goodcang");
        dto.setDownloadTime(LocalDateTime.now());
        service.onMessage(dmpSyncMqDTO);
    }
}