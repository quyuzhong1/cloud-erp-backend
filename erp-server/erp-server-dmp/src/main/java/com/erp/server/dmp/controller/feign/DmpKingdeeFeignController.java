package com.erp.server.dmp.controller.feign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncOperateEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.dmp.push.service.business.KingdeeExchangeRateConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cloud
 * @description:金蝶数据处理类
 */
@Slf4j
@RestController
@RequestMapping("feign/k3cloud")
public class DmpKingdeeFeignController extends BaseController {

    @Resource
    private KingdeeExchangeRateConsumerService kingdeeExchangeRateConsumerService;


    /**
     * 汇率批量保存
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @PostMapping("/batch/add")
    public ApiResult batchAdd(@RequestBody HashMap<String, List<HashMap<String, Object>>> requestMap) {
        log.info("批量新增汇率数据开始，请求参数：{}", JSONUtil.toJsonStr(requestMap));
        if (CollUtil.isEmpty(requestMap)) {
            return failure("请求参数不能为空");
        }
        try {
            List<HashMap<String, Object>> dataList = requestMap.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return failure("数据列表不能为空");
            }

            for (HashMap<String, Object> data : dataList) {
                HashMap<String, Object> paramMap = new HashMap<>(data);
                paramMap.put("operate", SyncOperateEnum.OPERATE_ADD.getCode());
                kingdeeExchangeRateConsumerService.executeConsumer(paramMap);
            }

            log.info("批量新增汇率数据完成");
            return success();
        } catch (Exception e) {
            log.error("批量新增汇率数据异常", e);
            return failure("批量新增汇率数据失败：" + e.getMessage());
        }
    }

    /**
     * 汇率批量审核
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @PostMapping("/batch/approve")
    public ApiResult batchApprove(@RequestBody HashMap<String, List<HashMap<String, Object>>> requestMap) {
        log.info("批量审核汇率数据开始，请求参数：{}", JSONUtil.toJsonStr(requestMap));
        if (CollUtil.isEmpty(requestMap)) {
            return failure("请求参数不能为空");
        }

        try {
            List<HashMap<String, Object>> dataList = requestMap.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return failure("数据列表不能为空");
            }

            for (HashMap<String, Object> data : dataList) {
                HashMap<String, Object> paramMap = new HashMap<>(data);
                paramMap.put("operate", SyncOperateEnum.OPERATE_APPROVE.getCode());
                kingdeeExchangeRateConsumerService.executeConsumer(paramMap);
            }
            log.info("批量审核汇率数据完成，处理数据量：{}", dataList.size());
            return success();
        } catch (Exception e) {
            log.error("批量审核汇率数据异常", e);
            return failure("批量审核汇率数据失败：" + e.getMessage());
        }
    }


}
