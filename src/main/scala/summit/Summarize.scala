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

import scala.collection.JavaConversions.asIterable

import java.util.List
import java.nio.ByteBuffer

import org.apache.cassandra.avro.{Mutation, Column, ColumnOrSuperColumn, Clock}
import org.apache.cassandra.hadoop.{ColumnFamilyOutputFormat, ConfigHelper}

import org.apache.hadoop.conf.{Configured, Configuration}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.{Job, Reducer, ReduceContext}
import org.apache.hadoop.util.{Tool, ToolRunner}

object Summarize {
    def main(args: Array[String]): Unit = {
        // let ToolRunner handle generic command-line options
        ToolRunner.run(new Configuration, new SummarizeJob, args)
    }
}

/**
 * Converts per-key entries into mutations which the OutputFormat will execute.
 */
class MutationReducer extends Reducer[LongWritable, Text, ByteBuffer, List[Mutation]] {
    val EMPTY_BYTES = ByteBuffer.allocate(0)

    def wrap(value: Text, off: Int, len: Int): ByteBuffer =
        ByteBuffer.wrap(value.getBytes(), off, len)

    def mutation(name: ByteBuffer): Mutation = {
        val col = new Column
        col.name = name
        col.value = EMPTY_BYTES
        col.clock = new Clock
        col.clock.timestamp = System.currentTimeMillis * 1000
        col.ttl = 0
        val mut = new Mutation
        mut.column_or_supercolumn = new ColumnOrSuperColumn
        mut.column_or_supercolumn.column = col
        return mut
    }

    override def reduce(key: LongWritable, values: java.lang.Iterable[Text], context: Reducer[LongWritable, Text, ByteBuffer, List[Mutation]]#Context) = {
        // build mutations for the key
        val mutations = new java.util.ArrayList[Mutation]
        for (value <- values) {
            // split the key and value on a tab
            val tab = value.find("\t")
            mutations.add(mutation(wrap(value, tab + 1, value.getLength - (tab + 1))))
            context.write(wrap(value, 0, tab), mutations)
            mutations.clear
        }
    }
}

class SummarizeJob extends Configured with Tool {
    val KEYSPACE = "Summit"
    val COLUMN_FAMILY = "Regnums"

    override def run(args: Array[String]): Int = {
        val job = new Job(getConf(), "summarize")
        job.setJarByClass(classOf[SummarizeJob])
        job.setReducerClass(classOf[MutationReducer])
        job.setMapOutputKeyClass(classOf[LongWritable])
        job.setMapOutputValueClass(classOf[Text])
        job.setOutputKeyClass(classOf[ByteBuffer])
        job.setOutputValueClass(classOf[List[Mutation]])
        job.setOutputFormatClass(classOf[ColumnFamilyOutputFormat])

        FileInputFormat.setInputPaths(job, new Path(args(0)))
        ConfigHelper.setOutputColumnFamily(job.getConfiguration(), KEYSPACE, COLUMN_FAMILY)

        return if (job.waitForCompletion(true)) 0 else 1
    }
}

