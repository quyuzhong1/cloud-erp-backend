package com.erp.server.sys.schedule;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.sdk.third.kingdee.utils.KingdeeExtensionUtils;
import com.erp.server.sys.service.DictKingdeeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.ws.RequestWrapper;
import java.util.Arrays;
import java.util.List;

/**
 * 金蝶扩展项目任务
 *
 * @author Jim
 * @since 2023-10-12
 */
@Component
@Slf4j
@EnableScheduling
public class KingdeeExtensionJob {

    @Resource
    private DictKingdeeService dictKingdeeService;

    /**
     * 拉取金蝶枚举(金蝶->sys)
     */
    @XxlJob("kingdeePullEnumJob")
    public ReturnT<String> kingdeePullWmsEnumJob() {
        XxlJobHelper.log("拉取枚举(金蝶->WMS)：开始执行");
        // Disable Bouncy Castle
        SecureUtil.disableBouncyCastle();
        String json = KingdeeExtensionUtils.getKingdeeEnum(Arrays.asList("其他出库单业务类型","其他出库单类型"));
        List<DictKingdeeDTO.CommonDTO> list = JSONObject.parseObject(json,new TypeReference<List<DictKingdeeDTO.CommonDTO>>() {}.getType());
        dictKingdeeService.addOrUpdate(list);
        XxlJobHelper.log("拉取枚举(金蝶->WMS)");
        return ReturnT.SUCCESS;
    }

}
