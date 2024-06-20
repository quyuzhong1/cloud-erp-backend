package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputKingdeeApiInitRequest;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DmpInputKingdeeApiInitHandler implements DmpInputApiInitHandler{

	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
		DmpInputKingdeeApiInitRequest dmpInputKingdeeApiInitRequest = (DmpInputKingdeeApiInitRequest) dmpInputApiInitRequest;
		
        LocalDateTime startTime = dmpInputKingdeeApiInitRequest.getStartTime();
        LocalDateTime endTime = dmpInputKingdeeApiInitRequest.getEndTime();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        LinkedList<String> queryFilters = new LinkedList<>();
//            queryFilters.add(StrUtil.format("FBillNo ='{}'", "XSD-20230105-33831"));
        // 移除 订单类型过滤
//            queryFilters.add(String.format("fBillTypeID = '%s'", "eacb50844fc84a10b03d7b841f3a6278"));
        queryFilters.add(StrUtil.format("FDocumentStatus = '{}'", "C"));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(startTime),sdf.format(endTime)));
        String filterStr = String.join(" and ",  queryFilters );

        String fieldKeys = "FID,FBillNo,FDate,FBillTypeId.FName,FBillTypeId.FNumber,FBillTypeId," +
                "FDocumentStatus,FCustId.FName,FCustId.FNumber,FSaleDeptId.FName,FSalerId.FName,FSalerId.FNumber,FReceiveAddress,FLinkMan,FLinkPhone," +
                "FApproverId.FName,FApproveDate,FCloseStatus,FCloseDate,FCancelStatus,FChangerId," +
                "FReceiveId.FName,FNote,FHeadDeliveryWay,FHEADLOCID,FCorrespondOrgId,FSaleGroupId," +
                "FChangeReason,FBusinessType,FReceiveContact,FChargeId,FCreatorId,FCreateDate,FModifierId,FModifierId.FName," +
                "FModifyDate,FSaleOrgId,FSaleOrgId.FName,FVersionNo,FSignStatus,FSOFrom,F_SK_Date,F_SHGJ1,FExchangeRate,FSettleCurrId.FCode";

        //当前页数
        Integer pageIndex = 0;
        //每次最多获取100条
        Integer pageSize = 10000;
        DmpInputTaskInitDTO dmpInputTaskInitDTO = null;
        while (true) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dmpInputKingdeeApiInitRequest.getFormId(), 1);
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶销售订单数据第[{}]页 有{}条记录", pageIndex, pageSize);
            dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.JSON);
            dmpInputTaskInitDTO.setMsg(JSON.toJSONString(result , SerializerFeature.WriteMapNullValue));
            dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
            if (result.size() < pageSize) {
            	break;
            }
            pageIndex++;
        }
		
		return dmpInputTaskInitDTOList;
	}

}
