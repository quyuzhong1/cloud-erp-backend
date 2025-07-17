package com.erp.server.tms.controller.api;


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
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.server.tms.service.LogisticsAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流地址
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物流地址")
@RequestMapping("/logisticsAddress")
public class LogisticsAddressController extends BaseController {

    @Resource
    private LogisticsAddressService logisticsAddressService;


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:paging",
            tableAlias = "la"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<LogisticsAddressDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<LogisticsAddressDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsAddressDTO.PagingViewDTO> pagingVO = logisticsAddressService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 导出
     *
     * @param dto
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult<Object>exportExcel(@Validated @RequestBody LogisticsAddressDTO.ExportDTO dto) {
        Boolean result = logisticsAddressService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流地址表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@Validated @RequestBody LogisticsAddressDTO.AddDTO dto) {
        return success(logisticsAddressService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流地址表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:update",
            serviceClass = LogisticsAddressService.class,
            keyIdName = "id")
    public ApiResult<Object>update(@Validated @RequestBody LogisticsAddressDTO.UpdateDTO dto) {
        logisticsAddressService.update(dto);
        return success();
    }

    /**
     * 详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:view",
            serviceClass = LogisticsAddressService.class,
            keyIdName = "id")
    public ApiResult<LogisticsAddressDTO.ViewDTO> view(@RequestParam("id") String id) {
        LogisticsAddressDTO.ViewDTO view = logisticsAddressService.view(id);
        return success(view);
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAddress:delete",
            serviceClass = LogisticsAddressService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@Validated @RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsAddressService.delete(id);
            } catch (Exception e) {
                log.error("物流地址删除失败{}", e);
                LogisticsAddressEntity entity = logisticsAddressService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "物流地址不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);

        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 根据地址类型获取地址列表
     *
     * @return
     */
    @GetMapping("/listByType")
    public ApiResult<List<LogisticsAddressDTO.ListDTO>> listByType(@RequestParam("type") String type) {
        List<LogisticsAddressDTO.ListDTO> list = logisticsAddressService.listByType(type);
        return success(list);
    }
    /**
     * 根据地址类型获取地址列表远程搜索
     * @return
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<LogisticsAddressDTO.ListDTO>> pagingSelect(@RequestBody @Validated PagingDTO<LogisticsAddressDTO.SelectDTO> dto){
        return success(logisticsAddressService.pagingSelect(dto));
    }

}
