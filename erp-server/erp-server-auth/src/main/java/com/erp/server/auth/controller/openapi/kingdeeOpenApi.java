package com.erp.server.auth.controller.openapi;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.rpc.dmp.feign.AfterSaleFeign;
import com.erp.rpc.dmp.feign.KingdeeFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 金蝶数据对接开放接口
 * @Author cloud
 * @since 2025-09-30
 */
@OpenApi
public class kingdeeOpenApi {

    @Resource
    private KingdeeFeign kingdeeFeign;


    /**
     * 汇率批量保存
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @OpenApi("kingdeeBatchAdd")
    public ApiResult batchAdd(@RequestBody HashMap<String, List<HashMap<String, Object>>> paramMap){
        return kingdeeFeign.batchAdd(paramMap);
    }

    /**
     * 汇率批量审核
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @OpenApi("kingdeeBatchApprove")
    public ApiResult batchApprove(@RequestBody HashMap<String, List<HashMap<String, Object>>> paramMap){
        return kingdeeFeign.batchApprove(paramMap);
    }



}
