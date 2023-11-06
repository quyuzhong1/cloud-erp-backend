package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.annotation.PlatformType;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.tms.convert.LogisticsOrderConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsAuthService;
import com.sdk.tms.disifang.model.base.ResponseMsg;
import com.sdk.tms.disifang.model.order.request.OrderRequest;
import com.sdk.tms.disifang.service.DsfShipperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName DsfLogisticsHandlerImpl
 * @description: TODO
 * @date 2023年11月02日
 * @version: 1.0
 */
@Slf4j
@Component
@PlatformType(PlatformDictEnum.SDF)
public class DsfLogisticsHandlerImpl extends AbstractLogisticsHandler {

    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private DsfShipperService dsfShipperService;

    @Override
    public LogisticsAuthEntity getLogisticsAuthConfig(String authId) {
        //自定义渠道配置信息 支持 物流：渠道 = 1：n
        return logisticsAuthService.getById(authId);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        ApiResult apiResult = new ApiResult();
        OrderRequest orderRequest = LogisticsOrderConverter.INSTANCE.orderRequestToDsf(logisticsOrderVO);
//        OrderRequest orderRequest =OrderRequest.builder().build();
        ResponseMsg responseMsg = dsfShipperService.createOrder(logisticsOrderVO.getLogisticsAuthEntity().getAccount(), logisticsOrderVO.getLogisticsAuthEntity().getPassword(), orderRequest);
        if (StringUtils.isBlank(responseMsg.getResult()) || !Objects.equals("1", responseMsg.getResult())) {
            apiResult.setMsg(responseMsg.getMsg());
            apiResult.setCode(-1);
        } else {
            apiResult.setCode(200);
            JSONObject parse = JSONUtil.parseObj(responseMsg.getData());
            apiResult.setData(parse.get("collect_no"));
            apiResult.setMsg(responseMsg.getMsg());
        }
        return apiResult;
    }
}
