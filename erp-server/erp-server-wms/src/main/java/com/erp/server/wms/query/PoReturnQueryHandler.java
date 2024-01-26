package com.erp.server.wms.query;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.PoReturnStatusEnum;
import com.erp.rpc.sys.feign.SysPostFeign;
import com.erp.server.wms.service.CommonService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
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
                super.buildDefaultDTO("pro.approve_status", ApproveStatusEnum.WAIT_SUBMIT.getCode());
                super.buildDefaultDTO("pro.invalid_status", Arrays.asList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            }
            if (PoReturnStatusEnum.TO_BE_APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", ApproveStatusEnum.APPROVE_ING.getCode());
                super.buildDefaultDTO("pro.invalid_status", Arrays.asList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            }
            if (PoReturnStatusEnum.APPROVE.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", ApproveStatusEnum.APPROVE.getCode());
                super.buildDefaultDTO("pro.invalid_status", Arrays.asList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            }
            if (PoReturnStatusEnum.REJECT.getCode().equals(value)) {
                super.buildDefaultDTO("pro.approve_status", ApproveStatusEnum.REJECT.getCode());
                super.buildDefaultDTO("pro.invalid_status", Arrays.asList(InvalidStatusEnum.NOT_VOIDED.getStatus()));
            }
            if (PoReturnStatusEnum.WAIT_FOR_ME_HANDLE.getCode().equals(value)) {
                LoginUser userInfo = commonService.getUserInfo();
                List<SysPostUserEntity> postUserList = sysPostFeign.getPostUserByUserId(userInfo.getUid());
                List<String> postIdStr = postUserList.stream().map(req -> req.getPostId()).distinct().collect(Collectors.toList());
                super.buildDefaultDTO("pro.confirm_status", PoReturnConfirmStatusEnum.WAIT_CONFIRM.getCode());

                if (CollectionUtil.isNotEmpty(postIdStr)) {
                    super.buildDefaultDTO("pro.unusual_handle_user_id", postIdStr);
                } else {
                    postIdStr.add("1");
                    super.buildDefaultDTO("pro.unusual_handle_user_id", postIdStr);
                }

            }

            return super.getSplicingSQL();
        }
        return null;
    }
}
