package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputKingdeeApiInitRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * dmp输入init任务基础处理器下的金蝶api获取数据方式
 * @author Administrator
 *
 */
@Service
@Slf4j
@Scope("prototype")
public class DmpInputKingdeeApiInitHandler implements DmpInputApiInitHandler{

	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		DmpInputKingdeeApiInitRequest dmpInputKingdeeApiInitRequest = (DmpInputKingdeeApiInitRequest) dmpInputApiInitRequest;
        //当前页数
        Integer pageIndex = 0;
        //每次最多获取100条
        Integer pageSize = 10000;
        DmpInputTaskInitDTO dmpInputTaskInitDTO = null;
        while (true) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
//            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dmpInputKingdeeApiInitRequest.getFormId(), 1);
//            List<Map<String, Object>> result = kingdeeApiUtils.queryList(dmpInputKingdeeApiInitRequest.getFilterStr(), dmpInputKingdeeApiInitRequest.getFieldKeys(), pageSize, pageIndex, 0);
//            log.info("获取金蝶销售订单数据第[{}]页 有{}条记录", pageIndex, result.size());
            dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.JSON);
//            result.forEach(r -> r.put("formId", dmpInputKingdeeApiInitRequest.getFormId()));
            dmpInputTaskInitDTO.setMsg("[{\"FID\":\"9497566\",\"FBillTypeID\":\"ad0779a4685a43a08f08d2e42d7bf3e9\",\"FBillTypeID.FName\":\"标准销售出库单\",\"FBillNo\":\"CK2024070129\",\"FSoOrDerNo\":\"11131685101758660\",\"FDate\":\"2024-01-18T00:00:00\",\"FSaleOrgId\":\"236226\",\"FSaleOrgId.FName\":\"唯跡科技有限公司VIJIM LIMITED\",\"FCarriageNO\":\" \",\"FStockerID.FNumber\":\"null\",\"FStockerID.FName\":\"null\",\"FCustomerID\":\"302215\",\"FCustomerID.FName\":\"美国2站\",\"FCustomerID.FNumber\":\"102001\",\"FSaleDeptID.FName\":\"亚马逊美国组\",\"FSalesManID\":\"0\",\"FSalesManID.FName\":\"null\",\"FSalesManID.FNumber\":\"null\",\"FReceiverID.FName\":\"美国2站\",\"FTransferBizType.FName\":\"跨组织销售\",\"F_ulz_BaseProperty2\":\"亚马逊\",\"F_ulz_BaseProperty2.FNumber\":\"019\",\"FLinkPhone\":\" \",\"FLinkMan\":\" \",\"FBussinessType\":\"NORMAL\",\"FDocumentStatus\":\"C\",\"FNote\":\"111-3168510-1758660\",\"FReceiveAddress\":\" \",\"FCreatorId.FName\":\"Administrator\",\"FCreateDate\":\"2024-01-29T05:45:04.393\",\"FModifierId.FName\":\"Administrator\",\"FModifyDate\":\"2024-01-29T05:45:04.647\",\"FApproverID.FName\":\"Administrator\",\"FApproveDate\":\"2024-01-29T05:45:06.38\",\"FCancelStatus\":\"A\",\"FGYDATE\":\"null\",\"FLogisticsNos\":\" \",\"F_ulz_Text3\":\" \",\"FSettleCurrID.FCode\":\"USD\",\"FExchangeRate\":\"7.077\",\"FISGENFORIOS\":\"false\",\"FEntity_FENTRYID\":\"15335092\",\"FBillAllAmount\":\"24.95\",\"FBillAllAmount_LC\":\"176.57\",\"FAllAmount\":\"24.95\",\"FAllAmount_LC\":\"176.57\",\"FAmount_LC\":\"176.57\",\"FTaxAmount\":\"0.0\",\"FTaxAmount_LC\":\"0.0\",\"FBillTaxAmount\":\"0.0\",\"FEntryTaxAmount\":\"0.0\",\"FSrcBillNo\":\"11131685101758660\",\"FCustMatName\":\"null\",\"F_ulz_BaseProperty1\":\" \",\"FMaterialID\":\"237740\",\"FMaterialID.FNumber\":\"2029\",\"FMaterialID.FName\":\"VIJIM VL120 双色温补光灯\",\"FStockLocID\":\"0\",\"FBarcode\":\" \",\"FMateriaModel\":\"VL120\",\"FMateriaType\":\"原材料\",\"FRealQty\":\"1.0\",\"FUnitID.FName\":\"Pcs\",\"FPrice\":\"24.95\",\"FIsFree\":\"false\",\"FArrivalStatus\":\" \",\"FArrivalDate\":\"null\",\"FAmount\":\"24.95\",\"FStockStatusID\":\"10000\",\"FStockStatusID.FName\":\"可用\",\"FStockID.FName\":\"FBA北美2站仓\",\"FStockID.FNumber\":\"FBA-NA2\",\"F_ulz_Text1\":\" \",\"FEntryCostAmount\":\"10.39\",\"FEntrynote\":\" \",\"FSrcType\":\"SAL_SaleOrder\",\"FTaxPrice\":\"24.95\",\"FCostPrice\":\"73.517061\",\"FCostAmount_LC\":\"73.52\",\"FSalCostPrice\":\"10.39\",\"F_ULZ_data_sources\":\" \",\"FETHIRDBILLNO\":\" \"}]");
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
            break;
//            if (result.size() < pageSize) {
//            }
//            pageIndex++;
        }
		
		return dmpInputTaskInitDTOList;
	}

}
