package com.erp.server.tms.service.logistics;

import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.server.tms.convert.LogisticsChannelConverter;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.setting.SettingAPI;
import com.sdk.wangdian.sdk.api.setting.dto.LogisticsQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.LogisticsQueryResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jim
 * @ClassName AmazonLogisticsHandlerImpl
 * @description: 亚马逊物流接口开发
 * @date 2023年12月25日
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.WDT)
public class WdtLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private WangDianClientService clientService;
    /**
     * 渠道查询
     *
     */
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        LogisticsQueryRequest query = new LogisticsQueryRequest();

        Pager pager = new Pager();
        pager.setPageNo(0);
        pager.setPageSize(200);
        pager.setCalcTotal(true);

        SettingAPI settingAPI = clientService.get(SettingAPI.class);
        List<LogisticsSaleChannelEntity> detailsList = new ArrayList<>();
        boolean hasNext = true;
        int pageSize = 200;

        //分页循环拉取数据
        while (hasNext) {
            LogisticsQueryResponse response = null;
            try {
                response = settingAPI.queryLogistics(query, pager);
            } catch (Exception e) {
                log.error("拉取旺店通虚拟仓数据失败，原因【{}】", e.getMessage(), e);
                return detailsList.isEmpty() ? ApiResult.success(new ArrayList<>()) :
                        ApiResult.success(BeanMapperUtils.copyList(LogisticsSaleChannelEntity.class, detailsList));
            }
            if (response == null || response.getDetailList().isEmpty()) {
                return detailsList.isEmpty() ? ApiResult.success(new ArrayList<>()) :
                        ApiResult.success(detailsList);
            }
            //数据转换

            detailsList.addAll(LogisticsChannelConverter.INSTANCE.channelConvertByWdt(response.getDetailList()));
            Integer totalCount = response.getTotal();
            if (totalCount <= (pager.getPageNo() + 1) * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return detailsList.isEmpty() ? ApiResult.success(new ArrayList<>()) :
                ApiResult.success(detailsList);
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.AMZ_MULTI_CHANNEL;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");

    }
    /**
     * 授权判断
     *
     * @param authMap
     * @return
     */
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return success("授权成功");
    }
}
