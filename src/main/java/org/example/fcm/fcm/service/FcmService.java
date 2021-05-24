package org.example.fcm.fcm.service;

import org.example.fcm.fcm.common.FcmException;
import org.example.fcm.fcm.common.FcmExecutor;
import org.example.fcm.fcm.model.FcmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FcmService {
    private final static Logger LOG = LoggerFactory.getLogger(FcmService.class);

    public FcmResult<Double> run(List<Double> dataPoints,
                                 int numClusters,
                                 double fuzziness,
                                 double epsilon,
                                 int maxIterationCount) {
        try (final FcmExecutor fcmExecutor = new FcmExecutor(4)) {
            FcmSimple fcm = new FcmSimple(fcmExecutor, dataPoints, numClusters, fuzziness, epsilon, maxIterationCount);
            FcmResult<Double> result = fcm.fcm();
            int cluster;
            int maxCluster;
            double maxDegree;
            for (Double value : result.getDataPoints()) {
                LOG.info("value: {}", value);
                cluster = 0;
                maxCluster = 0;
                maxDegree = 0.0;
                for (Double degree : result.getDataPointDegrees(value)) {
                    cluster++;
                    if (degree > maxDegree) {
                        maxCluster = cluster;
                        maxDegree = degree;
                    }
                    LOG.info("cluster {}: {}", cluster, degree);
                }
                LOG.info("this point is member of {} cluster with degree {}", maxCluster, maxDegree);
                LOG.info("=============");
            }
            return result;
        }
    }
}
