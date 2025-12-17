package com.erp.server.tms.controller.feign;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.DictHsCodeDTO;
import com.erp.model.tms.entity.DictHsCodeEntity;
import com.erp.server.tms.service.DictHsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 出口申报要素表
 *
 * @author jack
 * @since 2025-07-31
 */
@Slf4j
@RestController
@LogSystemModule("出口申报要素表")
@RequestMapping("/feign/dictHsCode")
public class DictHsCodeFeignController extends BaseController {

    @Resource
    private DictHsCodeService dictHsCodeService;
    /**
     * 分页查询
     *
     * @return
     */
    @PostMapping("/pagingByBR")
    public PagingVO<DictHsCodeDTO.ListBRDTO> pagingByBR(@RequestBody @Validated PagingDTO<DictHsCodeDTO.PagingParamDTO> dto) {
        return dictHsCodeService.pagingByBR(dto);
    }

}
