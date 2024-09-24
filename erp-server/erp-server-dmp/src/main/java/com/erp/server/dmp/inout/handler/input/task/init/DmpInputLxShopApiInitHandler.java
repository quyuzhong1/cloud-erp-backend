package com.erp.server.dmp.inout.handler.input.task.init;

import com.alibaba.fastjson.JSON;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.sdk.third.lingxing.dto.ShopInfoDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * dmp输入init任务基础处理器下的api获取数据方式
 * 领星店铺
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxShopApiInitHandler extends DmpInputInitHandler {

    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<ShopInfoDTO> allShopList = LingxingApiUtils.getAllShopList();
        return Collections.singletonList(DmpInputTaskInitDTO.initMsg(JSON.toJSONString(allShopList)));
    }


}
