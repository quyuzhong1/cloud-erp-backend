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
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dmpInputKingdeeApiInitRequest.getFormId());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(dmpInputKingdeeApiInitRequest.getFilterStr(), dmpInputKingdeeApiInitRequest.getFieldKeys(), pageSize, pageIndex, 0);
            log.info("获取金蝶销售订单数据第[{}]页 有{}条记录", pageIndex, result.size());
            dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
            dmpInputTaskInitDTO.setContentType(DmpInputTaskFileContentTypeEnum.JSON);
            result.forEach(r -> r.put("formId", dmpInputKingdeeApiInitRequest.getFormId()));
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
