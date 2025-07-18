package com.erp.server.sys.controller.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.utils.JsonFieldDiffUtil;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.MqConsumerRecordDTO;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mq消费记录
 *
 * @author jack
 * @since 2025-05-29
 */
@Slf4j
@RestController
@LogSystemModule("mq消费记录")
@RequestMapping("/mqConsumerRecord")
public class MqConsumerRecordController extends BaseController {

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    /**
     *
     *
     */
    @PostMapping("/testPush")
    public void testPush(@RequestBody MqConsumerRecordDTO.TestPushDTO dto) {
        Object json = dto.getJson();
        String jsonStr = String.valueOf(json);
        log.info("MqRecordConsumerService 开始");
        if (StringUtils.isBlank(jsonStr)) {
            return;
        }
        // 建议：增加debug日志
        log.info("接收到MQ消息内容: {}", jsonStr);

        Gson gson = new Gson();
        List<Map<String, Map<String, Object>>> list = new ArrayList<>();

        try {
            //jsonStr 有可能是数组的，也有可能是非数组
            if (jsonStr.startsWith("[")) {
                Type mapType = new TypeToken<List<Map<String, Map<String, Object>>>>(){}.getType();
                list = gson.fromJson(jsonStr, mapType);
            } else {
                Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
                Map<String, Map<String, Object>> jsonMap = gson.fromJson(jsonStr, mapType);
                list.add(jsonMap);
            }
        } catch (Exception e) {
            log.error("MQ消息JSON解析异常: {}", jsonStr, e);
            return;
        }

        for (Map<String, Map<String, Object>> jsonMap : list) {
            if (Objects.isNull(jsonMap)) {
                log.error("解析后的jsonMap为null");
                continue;
            }
            Map<String, Object> before = jsonMap.getOrDefault("before", null);
            Map<String, Object> after = jsonMap.getOrDefault("after", null);
            if (Objects.isNull(after)) {
                log.error("解析后的after为null");
                continue;
            }
            //获取变动字段
            List<String> diffFields = JsonFieldDiffUtil.compare(before, after);
            if (CollUtil.isEmpty(diffFields)) {
                log.error("解析后的diffFields为null");
                continue;
            }
            //转驼峰
            List<String> convertedDiffFields = diffFields.stream()
                    .map(CharSequenceUtil::toCamelCase)
                    .collect(Collectors.toList());

            try {
                //深拷贝
                Map<String, Object> safeAfter = new HashMap<>(after);
                List<String> safeConvertedDiffFields = new ArrayList<>(convertedDiffFields);

                // 使用线程池直接异步执行
                thirdNoticePushRecordService.sendThirdNoticeByMqAsync(safeAfter, safeConvertedDiffFields);
            } catch (Exception e) {
                log.error("异步调用 sendThirdNoticeByMqAsync 失败", e);
            }
        }
        log.info("MqRecordConsumerService 结束");
    }

}
