package com.erp.server.dmp.inout.handler.input.task.init.api.tiktok;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.exception.ServiceException;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputInitHandler;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.order.WarehouseDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@Scope("prototype")
public class TikTokWarehouseApiInitHandler extends DmpInputInitHandler {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {

        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();

        String shopId = nextLevelId;
        if (StringUtils.isBlank(shopId) && StringUtils.isNotBlank(dmpInputTaskEntity.getExtendJson())) {
            JSONObject jsonObject = JSON.parseObject(dmpInputTaskEntity.getExtendJson());
            shopId = jsonObject.getString("nextLevelId");
        }
        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        if (ObjectUtil.isEmpty(tikTokShopInfoDTO)) {
            throw new ServiceException("TikTok店铺id：" + shopId + "未找到对应的店铺信息");
        }
        List<WarehouseDTO.DataDTO.WarehousesDTO> warehouses = tikTokSdkClientService.getSalesWarehouses(tikTokShopInfoDTO);
        JSONArray dataArray = new JSONArray();
        for (WarehouseDTO.DataDTO.WarehousesDTO warehouse : warehouses) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(warehouse);
            jsonObject.put("shopId", shopId);
            dataArray.add(jsonObject);
        }
        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(dataArray.toJSONString());
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);


        return dmpInputTaskInitDTOList;
    }
}
