package com.common.business.dto.base;

import lombok.*;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuperDTO implements Serializable {

    private Boolean isUserSystem = false;
}
