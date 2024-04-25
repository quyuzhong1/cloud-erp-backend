package com.erp.server.wms.query;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.query.AbstractQueryHandler;
import com.common.business.vo.LoginUser;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.sys.entity.SysPostUserEntity;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.PoReturnStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PackageForecastQueryHandler extends AbstractQueryHandler {

    @Override
    protected String handleSqlLogic(String field, Object value, String compareCodeSplicingValueSql) {

        if("tab".equals(field)){
            if (PackageUploadStatusEnum.WAIT.getCode().equals(value)) {
                super.buildDefaultDTO("pf.upload_status", PackageUploadStatusEnum.WAIT.getCode());
            }
            if (PackageUploadStatusEnum.UPLOAD_FAILURE.getCode().equals(value)) {
                super.buildDefaultDTO("pf.upload_status", PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
            }
            if (PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(value)) {
                super.buildDefaultDTO("pf.upload_status", PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            }
            if (PackageUploadStatusEnum.NOT.getCode().equals(value)) {
                super.buildDefaultDTO("pf.upload_status", PackageUploadStatusEnum.NOT.getCode());
            }


            return super.getSplicingSQL();
        }
        if("platformNo".equals(field)){
           return "and (pf.handover_no ILIKE concat('%',"+value+"::text,'%') " +
                   "OR pf.platform_package_no ILIKE concat('%',"+value+"::text,'%'))";
        }
        return null;
    }
}
