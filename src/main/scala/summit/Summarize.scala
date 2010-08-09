/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package summit

import org.apache.hadoop.conf.{Configured, Configuration}
import org.apache.hadoop.util.{Tool, ToolRunner}

object Summarize extends Configured with Tool {

    /**
     * Converts per-key entries into mutations which the OutputFormat will execute.
     * TODO: Generic parameters depend on input
     */
    class MutationReducer extends Reducer[Text, Text, ByteBuffer, List[Mutation]] {
        override def reduce(k: Text, v: Iterable[Text], context: Context): Unit {
            // build mutations for each key
        }
    }

    override def run(args: Array[String]): Int {
        val job = new Job(getConf(), "wordcount")
        job.setJarByClass(Markset.class)
        // TODO: Depending on input, may need a mapper to set key properly
        job.setMapperClass(TokenizerMapper.class)
        job.setReducerClass(MutationReducer.class)
        // TODO: Generic parameters depend on input
        job.setOutputKeyClass(Text.class)
        job.setOutputValueClass(IntWritable.class)

        // TODO: Find InputFormat to interface with PigStorage
        job.setInputFormatClass(ColumnFamilyInputFormat.class)
        job.setOutputFormatClass(ColumnFamilyOutputFormat.class)

        // TODO: Target CF for secondary index
        ConfigHelper.setOutputColumnFamily(job.getConfiguration(), KEYSPACE, COLUMN_FAMILY)

        job.waitForCompletion(true)
    }

    def main(args: Array[String]): Unit {
        // let ToolRunner handle generic command-line options
        ToolRunner.run(new Configuration(), new WordCount(), args)
    }
}

