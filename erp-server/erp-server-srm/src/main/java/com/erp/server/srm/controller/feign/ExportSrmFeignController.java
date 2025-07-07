package com.erp.server.srm.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.StatementDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.PoReconciliationDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.dto.SalesSharingDTO;
import com.erp.server.srm.query.PoReconciliationDetailQueryHandler;
import com.erp.server.srm.query.PoReconciliationDetailScmQueryHandler;
import com.erp.server.srm.query.PoReconciliationQueryHandler;
import com.erp.server.srm.query.PoReconciliationScmQueryHandler;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationScmService;
import com.erp.server.srm.service.SalesSharingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/feign/export")
public class ExportSrmFeignController {

    @Resource
    private PoReconciliationScmService poReconciliationScmService;
    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;
    @Resource
    private SalesSharingService salesSharingService;
    @PostMapping("/poReconciliationDetail")
    @WebAdvanceQuery(handler = PoReconciliationDetailQueryHandler.class)
    public PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetail(@RequestBody PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto){
        return poReconciliationDetailScmService.exportPoReconciliationDetailScm(dto);
    }
    @PostMapping("/poReconciliationDetailScm")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:paging",
            tableAlias = "prd"
    )
    @WebAdvanceQuery(handler = PoReconciliationDetailScmQueryHandler.class)
    public PagingVO<PoReconciliationDetailDTO.ListDTO> exportPoReconciliationDetailScm(@RequestBody PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto){
        return poReconciliationDetailScmService.exportPoReconciliationDetailScm(dto);
    }
    @PostMapping("/poReconciliationScmExport")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliation:scm:paging",
            tableAlias = "pr"
    )
    @WebAdvanceQuery(handler = PoReconciliationScmQueryHandler.class)
    public PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationScmExport(@RequestBody PagingDTO<PoReconciliationDTO.PagingParamDTO> dto){
        return poReconciliationScmService.exportPoReconciliationScmExport(dto);
    }
    @PostMapping("/poReconciliationExport")
    @WebAdvanceQuery(handler = PoReconciliationQueryHandler.class)
    public PagingVO<PoReconciliationDTO.ListDTO> exportPoReconciliationExport(@RequestBody PagingDTO<PoReconciliationDTO.PagingParamDTO> dto){
        return poReconciliationScmService.exportPoReconciliationScmExport(dto);
    }
    @PostMapping("/poReconciliation")
    public StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliation(@RequestBody PoReconciliationDTO.PagingParamDTO dto){
        return poReconciliationScmService.exportPoReconciliationScm(dto);
    }
    @PostMapping("/poReconciliationScm")
    @WebAdvanceQuery(handler = PoReconciliationScmQueryHandler.class)
    public StatementDTO<PoReconciliationDTO.ExportDTO, PoReconciliationDetailDTO.ListDTO> exportPoReconciliationScm(@RequestBody PoReconciliationDTO.PagingParamDTO dto){
        return poReconciliationScmService.exportPoReconciliationScm(dto);
    }

    @PostMapping("/salesSharing")
    @WebAdvanceQuery
    public PagingVO<SalesSharingDTO.ListDTO> paging(@RequestBody @Validated PagingDTO<SalesSharingDTO.PagingParamDTO> pagingParamDTO) {
        return salesSharingService.paging(pagingParamDTO);
    }
}
