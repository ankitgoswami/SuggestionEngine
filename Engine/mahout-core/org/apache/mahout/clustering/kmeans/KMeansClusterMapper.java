package org.apache.mahout.clustering.kmeans;
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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.mahout.matrix.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KMeansClusterMapper extends MapReduceBase implements
    Mapper<WritableComparable<?>, Vector, Text, Text> {
  protected List<Cluster> clusters;


  @Override
  public void map(WritableComparable<?> key, Vector point, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
    Cluster.outputPointWithClusterInfo(point, clusters, output);
  }

  /**
   * Configure the mapper by providing its clusters. Used by unit tests.
   *
   * @param clusters a List<Cluster>
   */
  void config(List<Cluster> clusters) {
    this.clusters = clusters;
  }

  @Override
  public void configure(JobConf job) {
    super.configure(job);
    Cluster.configure(job);

    clusters = new ArrayList<Cluster>();

    KMeansUtil.configureWithClusterInfo(job.get(Cluster.CLUSTER_PATH_KEY),
        clusters);

    if (clusters.isEmpty()) {
      throw new NullPointerException("Cluster is empty!!!");
    }
  }

}
