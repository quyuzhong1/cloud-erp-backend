package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.server.tms.query.TransferLogisticsSupplierQueryHandler;
import com.erp.server.tms.service.TransferLogisticsSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 中转报关服务商
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("物理商表")
@RequestMapping("/transferLogisticsSupplier")
public class TransferLogisticsSupplierController extends BaseController {

    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;

    /**
     * tab页
     * @Author Luo_WG
     * @Date 2024/1/19 18:02
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsSupplier:paging",
            tableAlias = "ls"
    )
    public ApiResult<List<TransferLogisticsSupplierDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<TransferLogisticsSupplierDTO.TabListDTO> tabList = transferLogisticsSupplierService.tabList(dto);
        return success(tabList);
    }

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2024/1/19 18:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.tms.dto.TransferLogisticsSupplierDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsSupplier:paging",
            tableAlias = "ls"
    )
    @WebAdvanceQuery(handler = TransferLogisticsSupplierQueryHandler.class)
    public ApiResult<PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<TransferLogisticsSupplierDTO.PagingParamDTO> dto) {
        PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> pagingVO = transferLogisticsSupplierService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2024/1/19 18:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO.AddDTO>
     **/
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关服务商表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferLogisticsSupplierDTO.AddDTO dto) {
        return success(transferLogisticsSupplierService.add(dto));
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2024/1/19 18:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsSupplier:update",
            serviceClass = TransferLogisticsSupplierService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转报关服务商表修改")
    public ApiResult<Object>update(@RequestBody @Validated TransferLogisticsSupplierDTO.UpdateDTO dto) {
        transferLogisticsSupplierService.update(dto);
        return success();
    }

    /**
     * 分页列表展开渠道列表
     */
    @PostMapping("/channelView")
    public ApiResult<List<TransferLogisticsSupplierDTO.ChannelViewDTO>> channelView(@RequestBody BaseIdDTO dto){
        List<TransferLogisticsSupplierDTO.ChannelViewDTO> channelViewList=transferLogisticsSupplierService.listChannelView(dto.getId(),dto.getName());
        return success(channelViewList);
    }

    /**
     * 物流渠道同步
     * @Author Luo_WG
     * @Date 2024/1/19 18:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @PostMapping("/sync")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "中转报关服务商物流渠道同步")
    public ApiResult<List<BatchResultDTO>> sync(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {

        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = transferLogisticsSupplierService.sync(id);
            } catch (Exception e) {
                log.error("物流渠道同步失败{}", e);
                TransferLogisticsSupplierEntity entity = transferLogisticsSupplierService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "中转报关服务商不存在, 同步失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getSupplierName(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2024/1/19 18:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出excel")
    @WebAdvanceQuery(handler = TransferLogisticsSupplierQueryHandler.class)
    public ApiResult<Object>export(@RequestBody @Valid TransferLogisticsSupplierDTO.ExportDTO dto) {
        Boolean result = transferLogisticsSupplierService.export(dto);
        return result ? success() : failure();
    }

    /**
     * 物流商删除
     * @Author Luo_WG
     * @Date 2024/1/19 18:05
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "中转报关服务商删除")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:transferLogisticsSupplier:delete",
            serviceClass = TransferLogisticsSupplierService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = transferLogisticsSupplierService.delete(id);
            } catch (Exception e) {
                log.error("物流商删除失败{}", e);
                TransferLogisticsSupplierEntity entity = transferLogisticsSupplierService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "中转报关服务商不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSupplierName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 所有中转报关服务商下拉
     * @Author Luo_WG
     * @Date 2024/1/19 18:05
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>>
     **/
    @GetMapping("/listAll")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAll(){
        return success(transferLogisticsSupplierService.listAll());
    }

    /**
     * 所有已授权的中转报关服务商下拉
     * @Author Luo_WG
     * @Date 2024/1/29 15:34
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>>
     **/
    @GetMapping("/listAlreadyAll")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listAlreadyAll(){
        return success(transferLogisticsSupplierService.listAlreadyAll());
    }

    /**
     * 中转报关服务商渠道树形结构
     * @return
     */
    @GetMapping("/tree")
    public ApiResult<List<BaseChildDTO.ListChildTreeDTO>> tree(){
        return success(transferLogisticsSupplierService.tree());
    }


}
