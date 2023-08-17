package com.erp.server.wms.controller.pda;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.server.wms.service.SoReturnReceiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ApiResult<List<SoReturnReceiveDTO.PdaSoReturnReceiveCount>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnReceiveDTO.PdaSoReturnReceiveCount> pdaPoReceiveCount = soReturnReceiveService.pdaListCount(dto);
        return success(pdaPoReceiveCount);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnReceiveDTO.Add dto) {
        String id = soReturnReceiveService.add(dto);
        return StringUtils.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnReceive:update",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnReceiveDTO.Update dto) {
        Boolean flag = soReturnReceiveService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnReceiveDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnReceive:view",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult<SoReturnReceiveDTO.View> view(@RequestParam("id") String id) {
        SoReturnReceiveDTO.View dto = soReturnReceiveService.view(id);
        return success(dto);
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:submit",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnReceiveService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:add",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnReceiveDTO.Add dto) {
        Boolean flag = soReturnReceiveService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:update",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnReceiveDTO.Update dto) {
        Boolean flag = soReturnReceiveService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:approve",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = soReturnReceiveService.approve(baseApproveParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:disApprove",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnReceiveService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:cancelProcess",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnReceiveService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soReturnReceive:invalid",
            serviceClass = SoReturnReceiveService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnReceiveService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }
}
