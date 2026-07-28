package com.erp.server.auth.controller.openapi;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.UserInfoDTO;
import com.erp.rpc.dmp.feign.KingdeeFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * 金蝶数据对接开放接口
 * @Author cloud
 * @since 2025-09-30
 */
@OpenApi
public class kingdeeOpenApi {

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private com.erp.rpc.sys.feign.KingdeeFeign sysKingdeeFeign;
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
    /**
     * 业务员列表 用于B2B 销售订单下拉
     */
    @OpenApi("listKingdeeUser")
    public ApiResult<List<UserInfoDTO.BusinessOperationUserDTO>> listKingdeeUser (KingdeeBusinessOperatorDTO.ListBusinessOperatorUserDTO dto) {
        return sysKingdeeFeign.listUser(dto);
    }
}
