package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputDetailEntity;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.lark.oapi.service.approval.v4.model.GetApprovalResp;
import com.lark.oapi.service.approval.v4.model.GetApprovalRespBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器，被init任务状态执行器继承，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputFeishuApprovalsInitHandler extends DmpInputInitHandler{
	
	@Resource
	private FsService fsService;
	
	@Override
	public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		DmpCfgInputDetailEntity detailEntity = dmpResponse.getDmpCfgInputDetailEntity();
		if (ObjUtil.isEmpty(detailEntity)) {
			return Collections.emptyList();
		}
		JSONArray result = new JSONArray();
		String approvalCode = detailEntity.getNextLevelId();
		try {
			GetApprovalResp approval = fsService.getApproval(approvalCode);
			GetApprovalRespBody data = approval.getData();
			String jsonString = JSON.toJSONString(data);
			JSONObject parseObject = JSON.parseObject(jsonString);
			parseObject.put("ulanzi_approval_code", approvalCode);
			result.add(parseObject);
		} catch (Exception e) {
			log.error("调用飞书失败,e= {}",e.getMessage());
			throw new ServiceException("调用飞书失败,msg= {}",e.getMessage());
		}
		return Collections.singletonList(DmpInputTaskInitDTO.initMsg(result.toJSONString()));
	}
	
}
