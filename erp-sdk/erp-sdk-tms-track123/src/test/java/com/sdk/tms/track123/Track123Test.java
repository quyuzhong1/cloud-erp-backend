package com.sdk.tms.track123;

import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.TrackResponse;
import com.sdk.tms.track123.service.TrackShipperService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= TrackShipperService.class)
public class Track123Test {

	@Resource
	private TrackShipperService trackShipperService;
	private Map<String, String> authMap = new HashMap<>();

	public Track123Test(){
		authMap.put("clientId", "Yg4Zf06w_sxZs3A5D");
		authMap.put("clientSecret", "579cf53f55694d89aef0887d81886aec");
	}
	@Test
	public void getTrack() {
		String token = authMap.get("clientSecret");
		TrackRequest trackRequest = TrackRequest.builder()
				.trackNos(Collections.singletonList("JDVC28603701823"))
				.cursor("")
				.queryPageSize(100)
				.build();
		TrackResponse track = trackShipperService.getTrack(token, trackRequest);
		System.out.println(track);
	}
}
