package com.erp.server.scm.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.ContractInfoDTO;
import com.erp.model.scm.entity.ContractInfoEntity;
import com.erp.server.scm.query.ContractInfoQueryHandler;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.erp.server.scm.service.ContractInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 合同管理表
 *
 * @author will
 * @since 2025-06-16
 */
@Slf4j
@RestController
@LogSystemModule("合同管理表")
@RequestMapping("feign/contractInfo")
public class ContractInfoFeignController extends BaseController {

    @Resource
    private ContractInfoService contractInfoService;

    /**
     * 批量更新合同名称
     * @return
     */
    @PostMapping("/updateContractNameByTempId")
    public void updateContractNameByTempId(@RequestBody ContractInfoDTO.UpdateContractNameDTO dto){
        contractInfoService.lambdaUpdate()
                .set(ContractInfoEntity::getName, dto.getName())
                .eq(ContractInfoEntity::getTemplateId, dto.getTemplateId())
                .update();


    }

}
