package org.example.fcm.fcm.service;

import org.example.fcm.fcm.common.FcmCommon;
import org.example.fcm.fcm.common.FcmException;
import org.example.fcm.fcm.common.FcmExecutor;

import java.util.List;

class FcmSimple extends FcmCommon<Double> {
    public FcmSimple(FcmExecutor fcmExecutor,
                     List<Double> dataPoints,
                     int numClusters,
                     double fuzziness,
                     double epsilon,
                     int maxIterationCount) throws FcmException {
        super(fcmExecutor, dataPoints, numClusters, fuzziness, epsilon, maxIterationCount);
    }

    @Override
    protected double getNormalization(int i, int j) {
        return Math.sqrt(Math.pow(dataPoints.get(i) - clusterCentre.get(j), 2));
    }

    @Override
    protected void calculateCentreVectors(double[][] fuzzyDegreeOfMembership) {
        int i, j;
        double numerator, denominator;
        for (j = 0; j < numClusters; j++) {
            numerator = 0.0;
            denominator = 0.0;
            for (i = 0; i < dataPoints.size(); i++) {
                numerator += fuzzyDegreeOfMembership[i][j] * dataPoints.get(i);
                denominator += fuzzyDegreeOfMembership[i][j];
            }
            clusterCentre.set(j, numerator / denominator);
        }
    }

}
