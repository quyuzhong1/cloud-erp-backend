package com.common.business.dto.base;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class BatchParamsDTO<T> implements Serializable {

    @Valid
    private List<T> params;

}
