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

package org.apache.mahout.classifier.bayes;

import org.apache.hadoop.io.DefaultStringifier;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.GenericsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BayesThetaNormalizerMapper extends MapReduceBase implements
    Mapper<Text, DoubleWritable, Text, DoubleWritable> {

  private static final Logger log = LoggerFactory.getLogger(BayesThetaNormalizerMapper.class);

  private Map<String, Double> labelWeightSum = null;
  private double sigma_jSigma_k = 0.0;
  private double vocabCount = 0.0;

  /**
   * We need to calculate the thetaNormalization factor of each label
   *
   * @param key   The label,feature pair
   * @param value The tfIdf of the pair
   */
  @Override
  public void map(Text key, DoubleWritable value,
                  OutputCollector<Text, DoubleWritable> output, Reporter reporter)
      throws IOException {

    String labelFeaturePair = key.toString();
    double alpha_i = 1.0;

    String label = labelFeaturePair.split(",")[0];
    double weight = Math.log((value.get() + alpha_i) / (labelWeightSum.get(label) + vocabCount));
    output.collect(new Text(('_' + label).trim()), new DoubleWritable(weight));
  }

  @Override
  public void configure(JobConf job) {
    try {
      if (labelWeightSum == null) {
        labelWeightSum = new HashMap<String, Double>();

        DefaultStringifier<Map<String, Double>> mapStringifier = new DefaultStringifier<Map<String, Double>>(
            job, GenericsUtil.getClass(labelWeightSum));

        String labelWeightSumString = mapStringifier.toString(labelWeightSum);
        labelWeightSumString = job.get("cnaivebayes.sigma_k",
            labelWeightSumString);
        labelWeightSum = mapStringifier.fromString(labelWeightSumString);

        DefaultStringifier<Double> stringifier = new DefaultStringifier<Double>(
            job, GenericsUtil.getClass(sigma_jSigma_k));
        String sigma_jSigma_kString = stringifier.toString(sigma_jSigma_k);
        sigma_jSigma_kString = job.get("cnaivebayes.sigma_jSigma_k",
            sigma_jSigma_kString);
        sigma_jSigma_k = stringifier.fromString(sigma_jSigma_kString);

        String vocabCountString = stringifier.toString(vocabCount);
        vocabCountString = job.get("cnaivebayes.vocabCount", vocabCountString);
        vocabCount = stringifier.fromString(vocabCountString);

      }
    } catch (IOException ex) {
      log.warn(ex.toString(), ex);
    }
  }

}
