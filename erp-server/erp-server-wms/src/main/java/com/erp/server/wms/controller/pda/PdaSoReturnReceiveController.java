package com.erp.server.wms.controller.pda;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.SoReturnReceiveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:销售退货签收单
 * @Author Luo_WG
 * @Date 2023/8/15 10:23
 **/
@RestController
@RequestMapping("/pdaSoReturnReceive")
public class PdaSoReturnReceiveController extends BaseController {

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/15 11:24
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnReceiveDTO.PdaPagingView>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:pdaSoReturnReceive:paging",
            tableAlias = "srr"
    )
    public ApiResult<PagingVO<SoReturnReceiveDTO.PdaPagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnReceiveDTO.PdaPagingParamDTO> dto) {
        PagingVO<SoReturnReceiveDTO.PdaPagingView> pagingVO = soReturnReceiveService.pdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/11 10:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.PdaPoReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "receive_user_id",
            menuCode = "wms:pdaSoReturnReceive:paging",
            tableAlias = "srr"
    )
    public ApiResult<List<SoReturnReceiveDTO.PdaPoReceiveCount>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnReceiveDTO.PdaPoReceiveCount> pdaPoReceiveCount = soReturnReceiveService.pdaListCount(dto);
        return success(pdaPoReceiveCount);
    }


}
