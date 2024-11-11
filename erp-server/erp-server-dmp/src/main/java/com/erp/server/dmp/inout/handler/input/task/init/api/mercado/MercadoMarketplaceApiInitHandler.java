package com.erp.server.dmp.inout.handler.input.task.init.api.mercado;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@Service
@Scope("prototype")
public class MercadoMarketplaceApiInitHandler implements DmpInputApiInitHandler {
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;
    @Resource
    protected MongoService mongoService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest dmpInputApiInitRequest) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();


        String nextLevelId = dmpInputApiInitRequest.getNextLevelId();

        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(nextLevelId);
        if (null == shopInfoDTO) {
            log.error("[美客多产品下载]从缓存中获取美客多 token 失败: shopId={}", nextLevelId);
            return Collections.emptyList();
        }

        //平台接口地址
        String url = MercadoConstant.URL;


        String path = dmpInputApiInitRequest.getApiType().replace("{userId}", shopInfoDTO.getUserId().toString());

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = new ApiResult();
        Object data = null;
        long sleepTime = 1000;
        int count = 0;
        while (ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if (shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if (count == 10) {
                    throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
            data = shipmentResult.getData();
        }

        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多账户市场站点数据失败，返回值 responseMap={}", url + path, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多账户市场站点请求失败，返回值 responseMap={}",
                    url + path, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }


        if (ObjectUtil.isEmpty(shipmentResult.getData())) {
            return Collections.emptyList();
        }

        DmpInputTaskInitDTO dmpInputTaskInitDTO = new DmpInputTaskInitDTO();
        dmpInputTaskInitDTO.setMsg(shipmentResult.getData().toString());
        dmpInputTaskInitDTOList.add(dmpInputTaskInitDTO);

        return dmpInputTaskInitDTOList;
    }
}
