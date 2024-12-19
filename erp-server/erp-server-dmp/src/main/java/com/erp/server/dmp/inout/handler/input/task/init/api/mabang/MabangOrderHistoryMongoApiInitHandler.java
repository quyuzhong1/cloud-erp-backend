package com.erp.server.dmp.inout.handler.input.task.init.api.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.UrlContant;
import com.common.business.dto.ParamHeaderVO;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 马帮历史订单接口API
 */
@Service
@Slf4j
@Scope("prototype")
public class MabangOrderHistoryMongoApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MongoService mongoService;

	@Override
	public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String requestParam = dmpInputApiInitRequest.getRequestParam();
        Map<String, Object> paramMap = JSON.parseObject(requestParam, Map.class);

        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData("createDate", "createDate", PannoEnum.GT, dmpInputApiInitRequest.getStartTime().format(formatter)));
        paramDataList.add(new ParamData("createDate", "createDate", PannoEnum.LT, dmpInputApiInitRequest.getEndTime().format(formatter)));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "original_mabang_order");
        // 直接将 List 转换为 JSON 字符串
        String jsonString = JSON.toJSONString(dmpInputMongoChildList);

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(jsonString);
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);
        return dmpInputTaskInitDTOList;
	}

}
