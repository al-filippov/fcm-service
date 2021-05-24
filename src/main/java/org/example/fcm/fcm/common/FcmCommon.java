package org.example.fcm.fcm.common;

import com.google.common.util.concurrent.AtomicDouble;
import org.example.fcm.fcm.model.FcmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class FcmCommon<T> {
    private final Logger log = LoggerFactory.getLogger(FcmCommon.class);

    protected final FcmExecutor fcmExecutor;
    private final ResettableCountDownLatch latch;
    protected int numClusters;
    protected final double[][] degreeOfMembership;
    protected double epsilon;
    protected double fuzziness;
    protected int maxIterationCount;
    protected final List<T> dataPoints;
    protected final List<T> clusterCentre;

    protected FcmCommon(final FcmExecutor fcmExecutor,
                        final List<T> dataPoints,
                        int numClusters,
                        double fuzziness,
                        double epsilon,
                        int maxIterationCount) throws FcmException {
        if (dataPoints.size() == 0) {
            throw new FcmException("Number of data points should be > 0");
        }
        if (numClusters <= 0) {
            throw new FcmException("Number of clusters should be > 0");
        }
        if (fuzziness <= 1.0) {
            throw new FcmException("Fuzzyness coefficient should be > 1.0");
        }
        if (epsilon <= 0.0 || epsilon > 1.0) {
            throw new FcmException("Termination criterion should be > 0.0 and <= 1.0");
        }
        if (maxIterationCount <= 0) {
            throw new FcmException("Max iteration count should be > 0");
        }
        this.numClusters = numClusters <= 1 ? 2 : Math.min(numClusters, 10);
        this.fcmExecutor = fcmExecutor;
        this.latch = new ResettableCountDownLatch(0);
        this.dataPoints = Collections.unmodifiableList(dataPoints);
        this.degreeOfMembership = new double[dataPoints.size()][this.numClusters];
        this.clusterCentre = new ArrayList<>();
        this.epsilon = epsilon;
        this.fuzziness = fuzziness;
        this.maxIterationCount = maxIterationCount;
        double degree;
        int maxDegree, randomValue;
        int i, j;
        for (i = 0; i < this.numClusters; i++) {
            clusterCentre.add(null);
        }
        final Random random = new Random(System.currentTimeMillis());
        for (i = 0; i < dataPoints.size(); i++) {
            degree = 0.0;
            maxDegree = 100;
            for (j = 1; j < this.numClusters; j++) {
                randomValue = random.nextInt(32767) % (maxDegree + 1);
                maxDegree -= randomValue;
                degreeOfMembership[i][j] = randomValue / 100.0;
                degree += degreeOfMembership[i][j];
            }
            degreeOfMembership[i][0] = 1.0 - Math.min(degree, 1.0);
        }
    }

    protected void calculateFuzzyDegreeOfMembership() {
        int i, j;
        double[][] fuzzyDegreeOfMembership = new double[dataPoints.size()][numClusters];
        for (i = 0; i < dataPoints.size(); i++) {
            for (j = 0; j < numClusters; j++) {
                fuzzyDegreeOfMembership[i][j] = Math.pow(degreeOfMembership[i][j], fuzziness);
            }
        }
        calculateCentreVectors(fuzzyDegreeOfMembership);
    }

    protected double getNewValue(int i, int j) {
        int k;
        double power = 2 / (fuzziness - 1), sum = 0.0, top, bottom;
        for (k = 0; k < numClusters; k++) {
            top = getNormalization(i, j);
            bottom = getNormalization(i, k);
            if (top == 0.0 || bottom == 0.0) {
                continue;
            }
            sum += Math.pow(top / bottom, power);
        }
        if (sum == 0.0) {
            return 0.0;
        }
        return 1.0 / sum;
    }

    private class UpdateDegreeOfMembershipTask implements Runnable {
        private final int j;
        private final AtomicDouble maxDiff;

        UpdateDegreeOfMembershipTask(int j, AtomicDouble maxDiff) {
            this.j = j;
            this.maxDiff = maxDiff;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < dataPoints.size(); i++) {
                    double newUij, diff;
                    newUij = getNewValue(i, j);
                    diff = newUij - degreeOfMembership[i][j];
                    if (diff > maxDiff.get()) {
                        maxDiff.set(diff);
                    }
                    synchronized (degreeOfMembership) {
                        degreeOfMembership[i][j] = newUij;
                    }
                }
            } finally {
                fcmExecutor.signalUnlock();
                latch.countDown();
            }
        }
    }

    protected double updateDegreeOfMembership() {
        final AtomicDouble maxDiff = new AtomicDouble();
        latch.reset(numClusters);
        for (int j = 0; j < numClusters; j++) {
            fcmExecutor.executeTask(new UpdateDegreeOfMembershipTask(j, maxDiff));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Latch await error - {}", e.getMessage());
        }
        return maxDiff.get();
    }

    protected abstract double getNormalization(int i, int j);

    protected abstract void calculateCentreVectors(double[][] fuzzyDegreeOfMembership);

    public FcmResult<T> fcm() {
        double maxDiff;
        int step = 0;
        do {
            step++;
            calculateFuzzyDegreeOfMembership();
            maxDiff = updateDegreeOfMembership();
            log.debug("Step: {} of {}; Function: {}", step, maxIterationCount, maxDiff);
        } while (maxDiff > epsilon && step < maxIterationCount);
        Map<T, List<Double>> result = new LinkedHashMap<>();
        int i, j;
        List<Double> degrees = new ArrayList<>();
        for (i = 0; i < dataPoints.size(); i++) {
            degrees.clear();
            for (j = 0; j < numClusters; j++) {
                degrees.add(degreeOfMembership[i][j]);
            }
            result.put(dataPoints.get(i), new ArrayList<>(degrees));
        }
        return new FcmResult<>(clusterCentre, result);
    }
}
