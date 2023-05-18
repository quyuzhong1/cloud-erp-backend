package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoReturnDetailService;
import com.erp.server.oms.service.SoReturnService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售退货单
 * @author LUO_WG
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soReturn")
public class SoReturnController extends BaseController {
    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoReturnDetailService soReturnDetailService;
    
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "osp"
    )
    public ApiResult<PagingVO<SoReturnDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnDTO.PagingParam> dto) {
        PagingVO<SoReturnDTO.PagingView> pagingVO = soReturnService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoReturnDTO.soReturnCountDTO>>
     **/
    @PostMapping("/listCount")

    public ApiResult<List<SoReturnDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoReturnDTO.StatusCountDTO> soReturnCountDTOS = soReturnService.listCount(dto);
        return success(soReturnCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoReturnDTO.Add dto) {
        String id = soReturnService.add(dto);
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
            tableField = "purchase_user_id,receive_user_id",
            menuCode = "oms:soReturn:update",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoReturnDTO.Update dto) {
        Boolean flag = soReturnService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoReturnDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "seller_id",
            menuCode = "oms:soReturn:view",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult<SoReturnDTO.View> view(@RequestParam("id") String id) {
        SoReturnDTO.View dto = soReturnService.view(id);
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:submit",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnService.submit(dto.getIds());
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:add",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoReturnDTO.Add dto) {
        Boolean flag = soReturnService.addAndSubmit(dto);
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:update",
            serviceClass = SoReturnService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoReturnDTO.Update dto) {
        Boolean flag = soReturnService.updateAndSubmit(dto);
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:approve",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        Boolean flag = soReturnService.approve(baseApproveParamDTO);
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:disApprove",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnService.disApprove(dto.getIds());
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:cancelProcess",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soReturnService.cancelProcess(dto.getIds());
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
            tableField = "seller_id",
            menuCode = "oms:soReturn:invalid",
            serviceClass = SoReturnService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soReturnService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        Boolean flag = soReturnService.delete(idsDTO.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "seller_id",
            menuCode = "oms:soReturn:paging",
            tableAlias = "osp"
    )
    public ApiResult exportExcel(@RequestBody SoReturnDTO.PagingParam dto, HttpServletResponse response) {
        Boolean flag = soReturnService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }

    /**
     * 下推退货通知单-列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoReturnNoticeView")
    public ApiResult<List<SoReturnDTO.GenerateSoReturnNoticeView>> generateSoReturnNoticeView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeViews = soReturnService.generateSoReturnNoticeView(dto.getIds());
        return success(generateSoReturnNoticeViews);
    }

    /**
     * 获取所有已审核订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping(value = "/listSoReturnByApproveStatus")
    public ApiResult<List<SoReturnEntity>> listSoReturnByApproveStatus() {
        List<SoReturnEntity> entityList = soReturnService.listSoReturnByApproveStatus();
        return success(entityList);
    }

    /**
     * 根据退货单id查询退货单信息
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping(value = "/getSoReturnById")
    public ApiResult<SoReturnEntity> getSoReturnById(@RequestParam("id") String id) {
        SoReturnEntity entity = soReturnService.getById(id);
        return success(entity);
    }

    /**
     * 添加详情按钮-列表查询
     *
     * @param id id
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.SoDetailDTO.AddDetailView>>
     * @Author Luo_WG
     * @Date 2023/5/16 18:43
     **/
    @PostMapping("/listAddDetailView")
    public ApiResult<List<SoDetailDTO.AddDetailView>> listAddDetailView(@RequestParam("id") String id) {
        List<SoDetailDTO.AddDetailView> addDetailViews = soReturnDetailService.listAddDetailView(id);
        return success(addDetailViews);
    }
}
