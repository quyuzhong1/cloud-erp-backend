package com.erp.server.oms.service.address.parser.region;

import java.util.List;
import java.util.Map;

/**
 * Region lexicon.
 */
public interface RegionLexicon {

    List<RegionNode> allNodes();

    Map<String, RegionNode> idIndex();

    Map<String, List<RegionNode>> aliasIndex();
}
