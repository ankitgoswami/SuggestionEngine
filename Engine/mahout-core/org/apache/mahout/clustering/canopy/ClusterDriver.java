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

package org.apache.mahout.clustering.canopy;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.mahout.matrix.SparseVector;
import org.apache.mahout.matrix.Vector;
import org.apache.mahout.utils.CommandLineUtil;
import org.apache.mahout.utils.SquaredEuclideanDistanceMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClusterDriver {

  private static final Logger log = LoggerFactory.getLogger(ClusterDriver.class);

  public static final String DEFAULT_CLUSTER_OUTPUT_DIRECTORY = "/clusters";

  private ClusterDriver() {
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException {

    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();

    Option vectorClassOpt = obuilder.withLongName("vectorClass").withRequired(false).withArgument(
        abuilder.withName("vectorClass").withMinimum(1).withMaximum(1).create()).
        withDescription("The Vector implementation class name.  Default is SparseVector.class")
        .withShortName("v").create();
    Option t1Opt = obuilder.withLongName("t1").withRequired(true).withArgument(
        abuilder.withName("t1").withMinimum(1).withMaximum(1).create()).
        withDescription("t1").withShortName("t1").create();
    Option t2Opt = obuilder.withLongName("t2").withRequired(true).withArgument(
        abuilder.withName("t2").withMinimum(1).withMaximum(1).create()).
        withDescription("t2").withShortName("t2").create();

    Option pointsOpt = obuilder.withLongName("points").withRequired(true).withArgument(
        abuilder.withName("points").withMinimum(1).withMaximum(1).create()).
        withDescription("The path containing the points").withShortName("p").create();

    Option canopiesOpt = obuilder.withLongName("canopies").withRequired(true).withArgument(
        abuilder.withName("canopies").withMinimum(1).withMaximum(1).create()).
        withDescription("The location of the canopies, as a Path").withShortName("c").create();

    Option measureClassOpt = obuilder.withLongName("distance").withRequired(false).withArgument(
        abuilder.withName("distance").withMinimum(1).withMaximum(1).create()).
        withDescription("The Distance Measure to use.  Default is SquaredEuclidean").withShortName("m").create();

    Option outputOpt = obuilder.withLongName("output").withRequired(true).withArgument(
        abuilder.withName("output").withMinimum(1).withMaximum(1).create()).
        withDescription("The Path to put the output in").withShortName("o").create();

    Option helpOpt = obuilder.withLongName("help").
        withDescription("Print out help").withShortName("h").create();

    Group group = gbuilder.withName("Options").withOption(vectorClassOpt)
        .withOption(t1Opt).withOption(t2Opt)
        .withOption(pointsOpt).withOption(canopiesOpt).withOption(measureClassOpt).withOption(outputOpt)
        .withOption(helpOpt).create();

    try {
      Parser parser = new Parser();
      parser.setGroup(group);
      CommandLine cmdLine = parser.parse(args);
      if (cmdLine.hasOption(helpOpt)) {
        CommandLineUtil.printHelp(group);
        return;
      }

      String measureClass = SquaredEuclideanDistanceMeasure.class.getName();
      if (cmdLine.hasOption(measureClassOpt)) {
        measureClass = cmdLine.getValue(measureClassOpt).toString();
      }
      String output = cmdLine.getValue(outputOpt).toString();
      String canopies = cmdLine.getValue(canopiesOpt).toString();
      String points = cmdLine.getValue(pointsOpt).toString();
      Class<? extends Vector> vectorClass = cmdLine.hasOption(vectorClassOpt) == false ?
          SparseVector.class
          : (Class<? extends Vector>) Class.forName(cmdLine.getValue(vectorClassOpt).toString());
      double t1 = Double.parseDouble(cmdLine.getValue(t1Opt).toString());
      double t2 = Double.parseDouble(cmdLine.getValue(t2Opt).toString());

      runJob(points, canopies, output, measureClass, t1, t2, vectorClass);

    } catch (OptionException e) {
      log.error("Exception", e);
      CommandLineUtil.printHelp(group);
    }


  }

  /**
   * Run the job
   *
   * @param points           the input points directory pathname String
   * @param canopies         the input canopies directory pathname String
   * @param output           the output directory pathname String
   * @param measureClassName the DistanceMeasure class name
   * @param t1               the T1 distance threshold
   * @param t2               the T2 distance threshold
   * @param vectorClass      The {@link Class} of Vector to use for the Output Value Class.  Must be concrete.
   */
  public static void runJob(String points, String canopies, String output,
                            String measureClassName, double t1, double t2, Class<? extends Vector> vectorClass) throws IOException {
    JobClient client = new JobClient();
    JobConf conf = new JobConf(
        org.apache.mahout.clustering.canopy.ClusterDriver.class);

    conf.set(Canopy.DISTANCE_MEASURE_KEY, measureClassName);
    conf.set(Canopy.T1_KEY, String.valueOf(t1));
    conf.set(Canopy.T2_KEY, String.valueOf(t2));
    conf.set(Canopy.CANOPY_PATH_KEY, canopies);

    conf.setInputFormat(SequenceFileInputFormat.class);

    /*conf.setMapOutputKeyClass(Text.class);
    conf.setMapOutputValueClass(SparseVector.class);*/
    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(vectorClass);
    conf.setOutputFormat(SequenceFileOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(points));
    Path outPath = new Path(output + DEFAULT_CLUSTER_OUTPUT_DIRECTORY);
    FileOutputFormat.setOutputPath(conf, outPath);

    conf.setMapperClass(ClusterMapper.class);
    conf.setReducerClass(IdentityReducer.class);

    client.setConf(conf);
    FileSystem dfs = FileSystem.get(outPath.toUri(), conf);
    if (dfs.exists(outPath)) {
      dfs.delete(outPath, true);
    }
    JobClient.runJob(conf);
  }

}
