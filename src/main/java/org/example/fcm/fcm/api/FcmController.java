package org.example.fcm.fcm.api;

import org.example.fcm.core.configuration.RestServiceConstants;
import org.example.fcm.core.model.Response;
import org.example.fcm.fcm.model.FcmResult;
import org.example.fcm.fcm.service.FcmService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(FcmController.URL)
public class FcmController {
    public final static String URL = RestServiceConstants.API_1_0 + "fcm";

    private final FcmService fcmService;

    public FcmController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping
    public Response<FcmResult<Double>> runFcm(@RequestBody List<Double> dataPoints,
                                              @RequestParam(defaultValue = "3") int numClusters,
                                              @RequestParam(defaultValue = "1.3") double fuzziness,
                                              @RequestParam(defaultValue = "0.00001") double epsilon,
                                              @RequestParam(defaultValue = "100") int maxIterationCount) {
        return new Response<>(fcmService.run(dataPoints, numClusters, fuzziness, epsilon, maxIterationCount));
    }
}
