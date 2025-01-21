package com.common.business.dto.base;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BatchParamsDTO<T> implements Serializable {

    @Valid
    @Size(min = 1, message = "参数不能为空")
    private List<T> params;

}
