package com.erp.server.tms.schedule;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Lambda
 * @Classname GetLogisticsTrackNoTaskJOB
 * @Description TODO
 * @Date 2024-01-04 15:45
 * @Created by yl
 */
@Slf4j
@Component
public class GetLogisticsTrackNoTaskJob {


    @XxlJob("getLogisticsTrackNo")
    public void getLogisticsTrackNo(){



    }
}
