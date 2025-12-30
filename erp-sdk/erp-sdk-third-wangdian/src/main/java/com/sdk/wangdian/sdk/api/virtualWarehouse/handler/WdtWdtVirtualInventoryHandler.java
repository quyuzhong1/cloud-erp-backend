package com.sdk.wangdian.sdk.api.virtualWarehouse.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.service.WdtVirtualInventoryService;
import com.common.core.exception.ServiceException;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.virtualWarehouse.VwPushHandleDetailAPI;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class WdtWdtVirtualInventoryHandler implements WdtVirtualInventoryService {

    @Resource
    private WangDianClientService wangDianClientService;

    private static String SEARCH_VIRTUAL_INVENTORY_URL = "setting.strategy.VirtualWarehouse.stockSearch";

    @Override
    public List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventory(WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO) {
        log.info("旺店通虚拟仓库存查询：request：{}", detailDTO);
        List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> resultList;
        try {
            resultList = searchInventoryApi(detailDTO);
        }catch (Exception e) {
            log.error("旺店通虚拟仓库存查询异常：{}", e.getMessage(), e);
            throw new ServiceException("旺店通虚拟仓库存查询异常：{}", e.getMessage());
        }
        return resultList;
    }
    /**
     * 查询库存数据
     * @author will
     * @date 2025/12/19 16:28
     * @param detailDTO
     * @return List<SearchVirtualInventoryDTO>
     */
    private List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchInventoryApi (WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO) {

        List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> resultList = new ArrayList<>();
        Pager pager = new Pager();
        pager.setPageSize(200);
        pager.setCalcTotal(true);
        pager.setPageNo(0);
        VwPushHandleDetailAPI api = wangDianClientService.get(VwPushHandleDetailAPI.class);
        int currTotal = 0;
        while (true) {
            String execute = "";
            try {
                execute = api.search(JSON.toJSONString(Collections.singletonList(detailDTO)), pager);
            } catch (WdtErpException e) {
                throw new ServiceException("调用旺店通" + SEARCH_VIRTUAL_INVENTORY_URL + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
            }
            JSONObject jsonObject = JSON.parseObject(execute);
            Integer status = jsonObject.getInteger("status");
            if (status != 0) {
                String message = jsonObject.getString("message");
                if ("sid 'wjkj03' is not found".equals(message)) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                throw new ServiceException("调用旺店通" + SEARCH_VIRTUAL_INVENTORY_URL + "接口报错，错误原因：" + message);
            }
            JSONObject data = jsonObject.getJSONObject("data");

            log.warn("旺店通虚拟仓查询响应：response：{}", data);

            Integer total = data.getInteger("total_count");
            JSONArray order = data.getJSONArray("order");
            if (order == null) {
                order = data.getJSONArray("detail_list");
            }
            currTotal = currTotal + order.size();
            //返回数据
            List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> thisOrderList = BeanUtil.copyToList(order, WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO.class);
            resultList.addAll(thisOrderList);
            if (currTotal >= total) {
                break;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return resultList;
    }
}
