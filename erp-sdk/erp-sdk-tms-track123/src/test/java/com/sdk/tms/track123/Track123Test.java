package com.sdk.tms.track123;

import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.sdk.tms.track123.model.request.ExtendField;
import com.sdk.tms.track123.model.request.RegisterRequest;
import com.sdk.tms.track123.model.request.TrackRequest;
import com.sdk.tms.track123.model.response.RegisterResult;
import com.sdk.tms.track123.model.response.TrackResponse;
import com.sdk.tms.track123.service.TrackShipperService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= TrackShipperService.class)
public class Track123Test {

	@Resource
	private TrackShipperService trackShipperService;
	private Map<String, String> authMap = new HashMap<>();

	public Track123Test(){
		//prod
//		authMap.put("clientId", "Yg4Zf06w_sxZs3A5D");
//		authMap.put("clientSecret", "579cf53f55694d89aef0887d81886aec");
		//test
		authMap.put("clientSecret","9fa500686633410a84ff0b00daed555e");
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

	@Test
	public void updateTrack() {
		String token = authMap.get("clientSecret");
		RegisterRequest registerRequest = new RegisterRequest();
		registerRequest.setTrackNo("76726447955668");
		registerRequest.setCourierCode("ztoexpress");
		ExtendField extendFieldMap = new ExtendField();
		extendFieldMap.setPhoneSuffix("8341");
		registerRequest.setExtendFieldMap(extendFieldMap);
		RegisterResult result = trackShipperService.updateTrack(token, registerRequest);
		System.out.println(result);
	}
}
