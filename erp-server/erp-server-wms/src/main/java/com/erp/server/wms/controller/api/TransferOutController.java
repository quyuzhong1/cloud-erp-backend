package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.server.wms.service.TransferOutService;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 调拨管理-分布式调出
 *
 * @author lambda
 * @since 2023-05-10
 */
@AllArgsConstructor
@RestController
@RequestMapping("/transfer/out")
public class TransferOutController extends BaseController {

    private final TransferOutService transferOutService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<TransferOutDTO.TabListDTO>> tabList() {
        return success(null);
    }


    /**
     * 分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:transferOut:paging",
            tableAlias = "tfo"
    )
    public ApiResult<PagingVO<TransferOutDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferOutDTO.PagingParamDTO> dto) {
        return success(transferOutService.paging(dto));
    }

    /**
     * 新增
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated TransferOutDTO.AddDTO dto) {
        return  success();
    }

    /**
     * 提交
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return  success();
    }

    /**
     * 新增并提交
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated TransferOutDTO.AddDTO dto) {
        return  success();
    }

    /**
     * 详情
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<TransferOutDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success(null);
    }

    /**
     * 修改
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated TransferOutDTO.UpdateDTO dto) {
        return success(null);
    }

    /**
     * 修改并提交
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated TransferOutDTO.UpdateDTO dto) {
        return success(null);
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }

    /**
     * 反审核
     *
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return  success();
    }
    /**
     * 删除仓库
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        return  success();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid TransferOutDTO.ExportDTO dto, HttpServletResponse response) {

        return success();
    }

}
