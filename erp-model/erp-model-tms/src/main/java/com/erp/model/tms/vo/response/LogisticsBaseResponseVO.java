package com.erp.model.tms.vo.response;

import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.utils.StrUtils;
import com.erp.model.tms.enums.LogisticsPlatformResultEnum;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 物流平台统一结果
 */
@Data
@ToString
public class LogisticsBaseResponseVO implements Serializable {

    private String code;

    private String message;

    public void success(){
        this.code = LogisticsPlatformResultEnum.SUCESS.getCode();
        this.message = LogisticsPlatformResultEnum.SUCESS.getDesc();
    }

    public void failure(String platform,String orderCode,String failureMessage){
        this.code = LogisticsPlatformResultEnum.FAILURE.getCode();
        this.message = StrUtil.format(LogisticsPlatformResultEnum.FAILURE.getDesc(), platform,orderCode,failureMessage);
    }
}
