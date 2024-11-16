package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.OrderCategoryDTO;
import com.erp.model.oms.dto.OrderCategoryDetailDTO;
import com.erp.model.oms.entity.OrderCategoryEntity;
import com.erp.server.oms.query.OrderCategoryQueryHandler;
import com.erp.server.oms.service.OrderCategoryDetailService;
import com.erp.server.oms.service.OrderCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 订单规则-订单分类表
 *
 * @author Lambda
 * @since 2023-08-24
 */
@RestController
@RequestMapping("/orderCategory")
@Slf4j
public class OrderCategoryController extends BaseController {

    @Resource
    private OrderCategoryService orderCategoryService;


    @Resource
    private OrderCategoryDetailService orderCategoryDetailService;

    /**
     * 订单分类分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = OrderCategoryQueryHandler.class)
    public ApiResult<PagingVO<OrderCategoryDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<OrderCategoryDTO.PagingParamDTO> dto) {
        PagingVO<OrderCategoryDTO.PagingViewDTO> pagingVO = orderCategoryService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 获取订单分类列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<OrderCategoryDetailDTO.ListDTO>> list() {
        List<OrderCategoryDetailDTO.ListDTO> list = orderCategoryDetailService.listOrderCategory();
        return success(list);
    }


    /**
     * 保存订单分类
     *
     * @return
     */
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated OrderCategoryDTO.AddDTO dto) {
        Boolean result = orderCategoryService.add(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }


    /**
     * 修改订单分类
     *
     * @return
     */
    @PostMapping("/update")
    public ApiResult<Object> update(@RequestBody @Validated OrderCategoryDTO.UpdateDTO dto) {
        Boolean result = orderCategoryService.updateCategory(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 详情
     *
     * @return
     */
    @PostMapping("/view")
    public ApiResult<OrderCategoryDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        OrderCategoryDTO.ViewDTO result = orderCategoryService.view(dto.getId());
        return success(result);
    }

    /**
     * 启用或者禁用店铺
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-22 14:37
     */
    @PostMapping("/updateStatus")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        Boolean disabled = dto.getDisabled();
        for (String id : ids) {
            BatchResultDTO submit;
            String flagCode = id;
            try {
                OrderCategoryEntity orderCategory = orderCategoryService.getById(id);
                if (Objects.isNull(orderCategory)) {
                    submit = BatchResultDTO.fail(id, id, "订单分类不存在");
                } else {
                    submit = orderCategoryService.updateStatus(orderCategory, disabled);
                }
            } catch (Exception e) {
                log.error("店铺更改状态失败>>>>{}", e);
                submit = BatchResultDTO.fail(id, flagCode, e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 分类的树结构
     * @return
     */
    @GetMapping("/tree")
    public ApiResult<List<BaseChildDTO.ListChildTreeDTO>> tree() {
        List<BaseChildDTO.ListChildTreeDTO> list=orderCategoryService.tree();
        return success(list);

    }

}
