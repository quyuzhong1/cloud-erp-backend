package com.erp.server.dmp.push.service.wdt.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.annotation.DataIdempotent;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.rpc.wms.feign.VirtualWarehouseAllocationDetailFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianVwPushHandleDetailService;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.virtualWarehouse.VwPushHandleDetailAPI;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 旺店通虚拟仓订单创建处理明细
 */
@Service
@Slf4j
public class WangDianVwPushHandleDetailServiceImpl implements WangDianVwPushHandleDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;
    @Resource
    private VirtualWarehouseAllocationDetailFeign allocationDetailFeign;

    private static String SEARCH_VIRTUAL_WAREHOUSE_URL = "setting.strategy.VirtualWarehouse.orderSearch";

    @Override
    @DataIdempotent(keyIdName = "pushDTOS.virtual_warehouse_no", waitTime = 10)
    public ApiResult<?> executeConsumer(VwPushHandelDetailPushDTO pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return ApiResult.error(PlatformEnum.WANGDIAN.getName()+"平台类型未获取到");
        }

        //查询分货单推送编号存在则不推送
        Boolean isExist = checkPushWdtHandleDetailExist(pushDTOS);
        if (isExist) {
            return ApiResult.success("旺店通已存在对应虚拟仓的分货单推送编号，无需重复推送");
        }

        VirtualWarehouseAllocationDTO.SyncUpdateDto dto = new VirtualWarehouseAllocationDTO.SyncUpdateDto();
        String msg = null;
        VwPushHandleDetailAPI api = wangDianClientService.get(VwPushHandleDetailAPI.class);
        log.info("旺店通虚拟仓订单创建：消费者接收数据：{}", pushDTOS);
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(pushDTOS), new TypeReference<Map<String, Object>>() {
        });
        Object bizType = map.get("bizType");
        Map<String, Object> request = commonService.makeApiFieldMap(map, platformEntity.getId(), ApiModuleTypeEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL.getCode());
        log.info("旺店通虚拟仓订单创建：请求参数：{}", request);
        //审核时间: 仅在order_type=3时生效, 格式: yyyy-MM-dd HH:mm, 时间要大于当前服务器时间2分钟以上
        Object orderTypeObj = request.get("order_type");
        if (Objects.nonNull(orderTypeObj) && !StringUtil.isEmpty(orderTypeObj.toString()) && Objects.equals(orderTypeObj.toString(), "3")) {
            request.put("pre_time", LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        try {
            VwPushHandelDetailResponse pushResult = api.push(request, request.get("detailList"));
            msg = JSONObject.toJSONString(pushResult);
            log.info("旺店通虚拟仓订单创建：响应结果：{}", pushResult);
            if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                dto.setSysType(ThirdSysTypeEnum.WDT.getCode());
                dto.setSysTypeName(ThirdSysTypeEnum.WDT.getName());
                buildResultData(pushResult, dto);
                dto.setHandelDetailId(map.get("sourceId").toString());
                log.info("旺店通虚拟仓订单创建：同步分货单：{}", dto);
                allocationDetailFeign.updateSyncStatus(dto);
            }
        } catch (WdtErpException e) {
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
            msg = e.getMessage();
            if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                dto.setHandelDetailId(map.get("sourceId").toString());
                dto.setFinishDescription(e.getMessage());
                log.info("旺店通虚拟仓订单创建：同步分货单：{}", dto);
                allocationDetailFeign.updateSyncStatus(dto);
            }
        }
        if (VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode().equals(dto.getSyncStatus())){
            return ApiResult.success(msg);
        }else {
            return ApiResult.error(msg);
        }
    }

    /**
     * 检查分货单推送编号是否存在
     * @author will
     * @date 2026/1/28 15:57
     * @param pushDTOS
     * @return Boolean
     */
    private Boolean checkPushWdtHandleDetailExist(VwPushHandelDetailPushDTO pushDTOS) {
        WdtSearchHandelDetailDTO.SearchVirtualWarehouseParamDTO detailDTO = new WdtSearchHandelDetailDTO.SearchVirtualWarehouseParamDTO();
        detailDTO.setVirtual_warehouse_no(pushDTOS.getVirtual_warehouse_no());
        detailDTO.setOrder_type(pushDTOS.getOrder_type());
        detailDTO.setStart_time(LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        detailDTO.setEnd_time(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        List<WdtSearchHandelDetailDTO.SearchVirtualWarehouseDTO> resultList = searchVirtualWarehouseApi(detailDTO);
        if (CollUtil.isEmpty(resultList)) {
            return Boolean.FALSE;
        }
        log.warn("虚拟仓订单创建->获取分货单推送编号，查询响应数据：{}", resultList);

        List<String> sourceCodeList = Arrays.stream(pushDTOS.getRemark().split("原始单据号：")).collect(Collectors.toList());
        String sourceCode;
        if (CollUtil.isNotEmpty(sourceCodeList) && sourceCodeList.size() > 1) {
            sourceCode = sourceCodeList.get(1).trim();
        } else {
            //未找到来源编码不推旺店通
            log.error("虚拟仓订单创建->获取分货单推送编号失败，来源单号remark:{}", pushDTOS.getRemark());
           return Boolean.TRUE;
        }
        long count = resultList.stream().filter(obj -> Objects.equals(obj.getVirtualWarehouseCode(), pushDTOS.getVirtual_warehouse_no())
                && Objects.equals(obj.getOrderType(), pushDTOS.getOrder_type())
                && obj.getRemark().contains(sourceCode)).count();
        if (count > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 构建返回数据
     * @param pushResult
     * @param dto
     */
    private void buildResultData(VwPushHandelDetailResponse pushResult, VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        if (Objects.nonNull(pushResult.getStatus()) && 0 == pushResult.getStatus()){
            dto.setThirdCode(pushResult.getMessage());
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode());
            dto.setFinishDescription("");
        }else {
            // 正则表达式匹配模式
            String pattern = "\\bVO\\d{12}\\b";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);
            // 创建 Matcher 对象
            Matcher m = r.matcher(pushResult.getMessage());
            // 查找匹配的
            if (m.find()) {
                String extractedString = m.group(0);
                dto.setThirdCode(extractedString);
            }
            dto.setFinishDescription(pushResult.getMessage());
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
        }
    }


    /**
     * 查询库存数据
     * @author will
     * @date 2025/12/19 16:28
     * @param detailDTO
     * @return List<SearchVirtualWarehouseDTO>
     */
    private List<WdtSearchHandelDetailDTO.SearchVirtualWarehouseDTO> searchVirtualWarehouseApi (WdtSearchHandelDetailDTO.SearchVirtualWarehouseParamDTO detailDTO) {

        List<WdtSearchHandelDetailDTO.SearchVirtualWarehouseDTO> resultList = new ArrayList<>();
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
                throw new ServiceException("调用旺店通" + SEARCH_VIRTUAL_WAREHOUSE_URL + "接口报错，错误原因：" + ExceptionUtil.stacktraceToOneLineString(e));
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
                throw new ServiceException("调用旺店通" + SEARCH_VIRTUAL_WAREHOUSE_URL + "接口报错，错误原因：" + message);
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
            List<WdtSearchHandelDetailDTO.SearchVirtualWarehouseDTO> thisOrderList = BeanUtil.copyToList(order, WdtSearchHandelDetailDTO.SearchVirtualWarehouseDTO.class);
            resultList.addAll(thisOrderList);
            if (currTotal >= total) {
                break;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return resultList;
    }
}
