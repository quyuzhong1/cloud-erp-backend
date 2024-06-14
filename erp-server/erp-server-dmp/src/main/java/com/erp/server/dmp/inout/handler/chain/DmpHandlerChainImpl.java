package com.erp.server.dmp.inout.handler.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.erp.server.dmp.inout.dto.request.DmpRequest;
import com.erp.server.dmp.inout.dto.response.DmpResponse;
import com.erp.server.dmp.inout.handler.DmpHandler;

import cn.hutool.core.collection.CollUtil;

@Component
@Scope("prototype")
public class DmpHandlerChainImpl implements DmpHandlerChain{
	private List<DmpHandler> dmpHandlers;
    private int index;
	
    public DmpHandlerChainImpl() {
    	dmpHandlers = new ArrayList<>();
        index = 0;
    }
	
	@Override
	public void doDmpHandler(DmpRequest dmpRequest, DmpResponse dmpResponse) {
		if (index < dmpHandlers.size()) {
			DmpHandler dmpHandler = dmpHandlers.get(index++);
			dmpHandler.doDmpHandler(dmpRequest, dmpResponse, this);
        }
	}

	public void addDmpHandler(DmpHandler dmpHandler) {
		if(dmpHandler != null) {
			this.dmpHandlers.add(dmpHandler);
		}
	}

	@Override
	public void addFirstDmpHandlerList(List<DmpHandler> dmpHandlerList) {
		if(CollUtil.isNotEmpty(dmpHandlerList)) {
			dmpHandlerList.removeIf(Objects::isNull);
			if(CollUtil.isNotEmpty(dmpHandlerList)) {
				if(index == dmpHandlers.size()) {
					dmpHandlers = dmpHandlerList;
				}else {
					dmpHandlers = dmpHandlers.subList(index, dmpHandlers.size());
					DmpHandler[] newDmpHandlers = new DmpHandler[dmpHandlers.size() + dmpHandlerList.size()];
					System.arraycopy(dmpHandlers.toArray(new DmpHandler[] {}), 0, newDmpHandlers, dmpHandlerList.size(), dmpHandlers.size());
					for(int i = 0; i < dmpHandlerList.size(); i++) {
						newDmpHandlers[i] = dmpHandlerList.get(i);
					}
					dmpHandlers = Arrays.asList(newDmpHandlers);
				}
				index = 0;
			}
		}
	}
	
}
