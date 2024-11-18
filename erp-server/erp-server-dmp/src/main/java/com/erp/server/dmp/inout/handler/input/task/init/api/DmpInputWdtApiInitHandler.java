package com.erp.server.dmp.inout.handler.input.task.init.api;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.sdk.wangdian.sdk.Client;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.server.WangDianClientService;

import cn.hutool.core.exceptions.ExceptionUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtApiInitHandler implements DmpInputApiInitHandler{

	@Resource
    private WangDianClientService wangDianClientService;
	
	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputWdtApiInitRequest) {
		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        
		String requestParam = dmpInputWdtApiInitRequest.getRequestParam();
		JSONObject parseObject = JSON.parseObject(requestParam);
		parseObject.put("start_time", dmpInputWdtApiInitRequest.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		parseObject.put("end_time", dmpInputWdtApiInitRequest.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		
		Pager pager = new Pager();
        Integer pageSize = parseObject.getInteger("page_size");
        if(pageSize == null || pageSize <= 0) {
        	pageSize = 200;
        }
        
        pager.setPageSize(pageSize);
        pager.setCalcTotal(true);
        pager.setPageNo(0);
        
        String apiType = dmpInputWdtApiInitRequest.getApiType();
        Client client = wangDianClientService.getClient();
    	
        int currTotal = 0;
        while(true) {
        	String execute = "";
			try {
				execute = client.execute(apiType, JSON.toJSONString(Arrays.asList(parseObject)), pager);
			} catch (WdtErpException | IOException e) {
				throw new ServiceException("调用旺店通" + apiType + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
			}
        	JSONObject jsonObject = JSON.parseObject(execute);
        	Integer status = jsonObject.getInteger("status");
        	if(status != 0) {
        		String message = jsonObject.getString("message");
        		if("sid 'wjkj03' is not found".equals(message)) {
					try {Thread.sleep(30000);} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				throw new ServiceException("调用旺店通" + apiType + "接口报错，错误原因：" + message);
        	}
        	
        	JSONObject data = jsonObject.getJSONObject("data");
        	
        	Integer total = data.getInteger("total_count");
        	JSONArray order = data.getJSONArray("order");
        	if(order == null) {
				order = data.getJSONArray("detail_list");
			}
        	
        	currTotal = currTotal + order.size();
        	DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        	dmpInputTaskInitDTO.setMsg(order.toJSONString());
        	dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        	if(currTotal >= total) {
        		break;
        	}
        	pager.setPageNo(pager.getPageNo() + 1);
        }
		
		return dmpInputTaskInitDTOList;
	}

}
