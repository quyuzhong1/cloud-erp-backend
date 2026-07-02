package com.erp.server.dmp.inout.handler.input.task.init.api.magalu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgApiEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.sdk.oms.magalu.dto.MagaluShopInfoDTO;
import com.sdk.oms.magalu.service.MagaluService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Scope("prototype")
public class MagaluOrderDetailInitHandler extends DmpInputInitHandler {

    @Resource
    private MagaluService magaluService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<Map<String, Object>> findMongoData = null;
        String parentStorageName = this.getParentStorageName(DmpInputTaskStatusEnum.MONGO);
        if (StringUtils.isNotBlank(parentStorageName)) {
            List<ParamData> paramDataList = new ArrayList<>();
            paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, dmpInputTaskEntity.getParentTaskId()));
            findMongoData = mongoService.findMongoData(paramDataList, parentStorageName);
        }
        if (CollectionUtil.isEmpty(findMongoData)) {
            return new ArrayList<>();
        }

        MagaluShopInfoDTO shopInfoDTO = magaluService.getShopInfoByShopId(findMongoData.get(0).get("nextLevelId").toString());
        if (shopInfoDTO == null) {
            throw new ServiceException("Magalu店铺id：" + this.nextLevelId + "未找到对应的店铺信息");
        }

        DmpCfgApiEntity dmpCfgApiEntity = dmpCfgApiService.getById(dmpCfgInputEntity.getTypeId());
        String apiPath = dmpCfgApiEntity.getApiType();

        Set<String> orderCodeSet = new LinkedHashSet<>();
        for (Map<String, Object> item : findMongoData) {
            Object code = item.get("code");
            if (code == null) {
                code = item.get("id");
            }
            if (code != null && CharSequenceUtil.isNotBlank(code.toString())) {
                orderCodeSet.add(code.toString());
            }
        }

        List<DmpInputTaskInitDTO> resultList = new ArrayList<>();
        for (String orderCode : orderCodeSet) {
            JSONObject detail = magaluService.getOrderDetail(shopInfoDTO, apiPath, orderCode);
            if (detail == null || detail.isEmpty()) {
                log.warn("[Magalu订单详情下载]订单{}未查询到数据", orderCode);
                continue;
            }
            resultList.add(DmpInputTaskInitDTO.initMsg(JSONArray.toJSONString(java.util.Collections.singletonList(detail))));
        }
        return resultList;
    }
}
