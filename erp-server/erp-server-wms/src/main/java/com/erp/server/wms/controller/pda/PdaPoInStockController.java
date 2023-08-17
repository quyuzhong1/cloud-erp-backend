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
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.server.wms.service.PoInstockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * PDA:采购入库单
 * @Author Luo_WG
 * @Date 2023/8/15 10:23
 **/
@RestController
@RequestMapping("/pdaPoInStock")
public class PdaPoInStockController extends BaseController {
    @Resource
    private PoInstockService poInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/16 14:50
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PoInstockDTO.PdaListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<PagingVO<PoInstockDTO.PdaPagingView>> paging(@RequestBody @Validated PagingDTO<PoInstockDTO.PdaSearchParamDTO> dto) {
        PagingVO<PoInstockDTO.PdaPagingView> pagingVO = poInstockService.PdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @Author Luo_WG
     * @Date 2023/8/16 18:00
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.PoInstockDTO.PdaPoInStockCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<List<PoInstockDTO.PdaPoInStockCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PoInstockDTO.PdaPoInStockCountDTO> list = poInstockService.pdaListCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/11 10:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.add(dto, Boolean.FALSE);
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
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/8/17 9:15
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.PoInstockDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:poInStock:view",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<PoInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        PoInstockDTO.ViewDTO dto = poInstockService.view(id);
        return success(dto);
    }

    /**
     * 新增并提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:add",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:submit",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:delete",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:invalid",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = poInstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param baseApproveParamDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:approve",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        poInstockService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:disApprove",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:poInStock:cancelProcess",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = poInstockService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }
}
