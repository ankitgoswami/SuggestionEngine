/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.classifier.cbayes;

import org.apache.mahout.common.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CBayesModel extends Model {

  private static final Logger log = LoggerFactory.getLogger(CBayesModel.class);

  @Override
  protected double getWeight(Integer label, Integer feature) {
    double result = 0.0;
    Map<Integer, Double> featureWeights = featureLabelWeights.get(feature);

    if (featureWeights.containsKey(label)) {
      result = featureWeights.get(label);
    }
    double vocabCount = featureList.size();
    double sumLabelWeight = getSumLabelWeight(label);
    double sigma_j = getSumFeatureWeight(feature);

    double numerator = sigma_j - result + alpha_i;
    double denominator = (sigma_jSigma_k - sumLabelWeight + vocabCount);

    double weight = Math.log(numerator / denominator);
    result = -weight / getThetaNormalizer(label);
    return result;
  }

  @Override
  protected double getWeightUnprocessed(Integer label, Integer feature) {
    double result;
    Map<Integer, Double> featureWeights = featureLabelWeights.get(feature);

    if (featureWeights.containsKey(label)) {
      result = featureWeights.get(label);
    } else {
      result = 0.0;
    }
    return result;
  }

  @Override
  public void initializeNormalizer() {


    log.info("{}", thetaNormalizer);
    double perLabelWeightSumNormalisationFactor = Double.MAX_VALUE;
    for (Map.Entry<Integer, Double> integerDoubleEntry1 : thetaNormalizer.entrySet()) {
      double Sigma_W_ij = integerDoubleEntry1.getValue();
      if (perLabelWeightSumNormalisationFactor > Math.abs(Sigma_W_ij)) {
        perLabelWeightSumNormalisationFactor = Math.abs(Sigma_W_ij);
      }
    }

    for (Map.Entry<Integer, Double> integerDoubleEntry : thetaNormalizer.entrySet()) {
      double Sigma_W_ij = integerDoubleEntry.getValue();
      thetaNormalizer.put(integerDoubleEntry.getKey(), Sigma_W_ij
          / perLabelWeightSumNormalisationFactor);
    }
    log.info("{}", thetaNormalizer);

    /*for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {
     thetaNormalizer.put(label, 0.0);
   }
   for (int feature = 0, maxFeatures = featureList.size(); feature < maxFeatures; feature++) {
     for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {

       double D_ij = getWeightUnprocessed(label, feature);
       double sumLabelWeight = getSumLabelWeight(label);
       double sigma_j = getSumFeatureWeight(feature);
       double vocabCount = featureList.size();

       double numerator = (sigma_j ) + alpha_i;
       double denominator = (sigma_jSigma_k - sumLabelWeight + vocabCount);
       double denominator1 = 0.5 *(sigma_jSigma_k/vocabCount + D_ij * (double) maxLabels);
       double weight = Math.log(numerator / denominator) + Math.log( 1 - D_ij/denominator1 );

       thetaNormalizer.put(label, weight+thetaNormalizer.get(label));

     }
   }
   perLabelWeightSumNormalisationFactor = Double.MAX_VALUE;
   log.info("{}", thetaNormalizer);
   for (Integer label : thetaNormalizer.keySet()) {
     double Sigma_W_ij = thetaNormalizer.get(label);
     if (perLabelWeightSumNormalisationFactor > Math.abs(Sigma_W_ij)) {
       perLabelWeightSumNormalisationFactor = Math.abs(Sigma_W_ij);
     }
   }

   for (Integer label : thetaNormalizer.keySet()) {
     double Sigma_W_ij = thetaNormalizer.get(label);
     thetaNormalizer.put(label, Sigma_W_ij
         / perLabelWeightSumNormalisationFactor);
   }
   log.info("{}", thetaNormalizer);*/
  }

  @Override
  public void generateModel() {
    double vocabCount = featureList.size();

    double[] perLabelThetaNormalizer = new double[labelList.size()];

    for (int feature = 0, maxFeatures = featureList.size(); feature < maxFeatures; feature++) {
      Integer featureInt = feature;
      for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {

        Integer labelInt = label;
        double D_ij = getWeightUnprocessed(labelInt, featureInt);
        double sumLabelWeight = getSumLabelWeight(labelInt);
        double sigma_j = getSumFeatureWeight(featureInt);

        double numerator = (sigma_j - D_ij) + alpha_i;
        double denominator = (sigma_jSigma_k - sumLabelWeight) + vocabCount;

        double weight = Math.log(numerator / denominator);

        if (D_ij != 0) {
          setWeight(labelInt, featureInt, weight);
        }

        perLabelThetaNormalizer[label] += weight;

      }
    }
    log.info("Normalizing Weights");
    double perLabelWeightSumNormalisationFactor = Double.MAX_VALUE;
    for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {
      double Sigma_W_ij = perLabelThetaNormalizer[label];
      if (perLabelWeightSumNormalisationFactor > Math.abs(Sigma_W_ij)) {
        perLabelWeightSumNormalisationFactor = Math.abs(Sigma_W_ij);
      }
    }

    for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {
      double Sigma_W_ij = perLabelThetaNormalizer[label];
      perLabelThetaNormalizer[label] = Sigma_W_ij
          / perLabelWeightSumNormalisationFactor;
    }

    for (int feature = 0, maxFeatures = featureList.size(); feature < maxFeatures; feature++) {
      Integer featureInt = feature;
      for (int label = 0, maxLabels = labelList.size(); label < maxLabels; label++) {
        Integer labelInt = label;
        double W_ij = getWeightUnprocessed(labelInt, featureInt);
        if (W_ij == 0) {
          continue;
        }
        double Sigma_W_ij = perLabelThetaNormalizer[label];
        double normalizedWeight = -1.0 * (W_ij / Sigma_W_ij);
        setWeight(labelInt, featureInt, normalizedWeight);
      }
    }
  }


  /**
   * Get the weighted probability of the feature.
   *
   * @param label   The label of the feature
   * @param feature The feature to calc. the prob. for
   * @return The weighted probability
   */
  @Override
  public double featureWeight(Integer label, Integer feature) {
    return getWeight(label, feature);
  }

}
