package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.server.oms.service.SoInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售管理-销售订单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/so")
public class SoInfoController extends BaseController {

    @Resource
    private SoInfoService soInfoService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<SoInfoDTO.TabListDTO>> tabList() {
        return success(null);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SoInfoDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoInfoDTO.PagingParamDTO> dto) {
        return success(null);
    }


    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:add",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult add(@RequestBody @Validated SoInfoDTO.AddDTO dto) {
        String id = soInfoService.add(dto);
        return StringUtils.isNotBlank(id)?success():failure();
    }


    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:submit",
            serviceClass = SoInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soInfoService.submit(dto.getIds());
        return result?success():failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:addAndSubmit",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SoInfoDTO.AddDTO dto) {
        Boolean result = soInfoService.addAndSubmit(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:view",
            serviceClass = SoInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<SoInfoDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success(null);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SoInfoDTO.UpdateDTO dto) {
        return success(null);
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoInfoDTO.UpdateDTO dto) {
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
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
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
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @PostMapping("/invalid")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        return success();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoInfoDTO.ExportDTO dto, HttpServletResponse response) {

        return success();
    }


}
