package com.erp.server.wms.query;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.vo.LoginUser;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.PoReturnStatusEnum;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.server.wms.service.CommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PoReturnQueryHandler extends AbstractQueryHandler {
    @Resource
    private SysPostFeign sysPostFeign;

    @Resource
    private CommonService commonService;

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {
        if("pro.source_type".equals(field)){
            if ("other".equals(value)) {
                return "pro.source_type != 'qcInfo'";
            } else {
                return "pro.source_type = 'qcInfo'";
            }
        }
        if("tab".equals(field)){
            if (PoReturnStatusEnum.WAIT_SUBMIT.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", PoReturnStatusEnum.WAIT_SUBMIT.getCode());
            }
            if (PoReturnStatusEnum.TO_BE_APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", PoReturnStatusEnum.TO_BE_APPROVE.getCode());
            }
            if (PoReturnStatusEnum.APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", PoReturnStatusEnum.APPROVE.getCode());
            }
            if (PoReturnStatusEnum.REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", PoReturnStatusEnum.REJECT.getCode());
            }
            if (PoReturnStatusEnum.WAIT_FOR_ME_HANDLE.getCode().equals(value)) {
                LoginUser userInfo = commonService.getUserInfo();
                List<SysPostUserEntity> postUserList = sysPostFeign.getPostUserByUserId(userInfo.getUid());
                List<String> postIdStr = postUserList.stream().map(req -> req.getPostId()).distinct().collect(Collectors.toList());
                super.buildDefaultDTO("pro.confirm_status", PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode());
                super.buildDefaultDTO("pro.unusual_handle_user_id", postIdStr);
            }
        }
        return null;
    }
}
