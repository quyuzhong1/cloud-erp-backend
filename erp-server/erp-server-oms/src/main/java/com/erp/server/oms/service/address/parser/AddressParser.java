package com.erp.server.oms.service.address.parser;

import com.erp.server.oms.service.address.parser.model.ParsedAddress;

/**
 * Address parser contract.
 */
public interface AddressParser {

    ParsedAddress parse(String input);
}
