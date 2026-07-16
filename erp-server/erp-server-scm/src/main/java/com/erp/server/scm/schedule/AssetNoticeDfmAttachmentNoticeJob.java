package com.erp.server.scm.schedule;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSONObject;
import com.erp.model.scm.dto.AssetNoticeDTO;
import com.erp.server.scm.service.AssetNoticeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 开模通知单 DFM 附件缺失飞书提醒
 */
@Component
@Slf4j
public class AssetNoticeDfmAttachmentNoticeJob {

    @Resource
    private AssetNoticeService assetNoticeService;

    /**
     * 查询创建时间 + N 天且 DFM 附件为空的开模通知单，飞书通知申请人/创建人
     * <p>
     * jobParam 示例：
     * {"offsetDays":30,"receiverField":"applyUserId",
     * "title":"上传DMF附件","contentTemplate":"请确认开模通知单【{code}】是否已和供应商确认DFM附件，已确认及时在数大臣系统上传！"}
     */
    @XxlJob("assetNoticeMissingDfmNotice")
    public ReturnT<String> assetNoticeMissingDfmNotice() {
        XxlJobHelper.log("=====开模通知单DFM附件缺失飞书提醒 开始=====");
        long start = System.currentTimeMillis();
        AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO param = parseJobParam(XxlJobHelper.getJobParam());
        XxlJobHelper.log("任务参数：{}", JSONObject.toJSONString(param));
        int sentCount = assetNoticeService.notifyMissingDfmAttachment(param);
        long end = System.currentTimeMillis();
        XxlJobHelper.log("发送飞书通知 {} 条，耗时 {} ms", sentCount, (end - start));
        XxlJobHelper.log("=====开模通知单DFM附件缺失飞书提醒 结束=====");
        return ReturnT.SUCCESS;
    }

    private AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO parseJobParam(String jobParam) {
        if (CharSequenceUtil.isBlank(jobParam)) {
            return new AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO();
        }
        try {
            AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO param =
                    JSONObject.parseObject(jobParam, AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO.class);
            return param == null ? new AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO() : param;
        } catch (Exception e) {
            XxlJobHelper.log("jobParam 解析失败，将使用默认参数：{}", e.getMessage());
            log.warn("开模通知单DFM附件缺失提醒 jobParam 解析失败：{}", jobParam, e);
            return new AssetNoticeDTO.DfmAttachmentNoticeJobParamDTO();
        }
    }
}
