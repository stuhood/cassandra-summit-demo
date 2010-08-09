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
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.{Job, Reducer, ReduceContext}
import org.apache.hadoop.util.{Tool, ToolRunner}

class Summarize extends Configured with Tool {
    val KEYSPACE = "Summit"
    val COLUMN_FAMILY = "Regnums"

    val EMPTY_BYTES = ByteBuffer.allocate(0)

    /**
     * Converts per-key entries into mutations which the OutputFormat will execute.
     * TODO: Generic parameters depend on input
     */
    class MutationReducer extends Reducer[Text, Text, ByteBuffer, List[Mutation]] {
        def wrap(value: Text): ByteBuffer =
            ByteBuffer.wrap(value.getBytes(), 0, value.getLength())

        def mutation(name: Text): Mutation = {
            val col = new Column
            col.name = wrap(name)
            col.value = EMPTY_BYTES
            col.clock = new Clock
            col.clock.timestamp = System.currentTimeMillis * 1000
            col.ttl = 0
            val mut = new Mutation
            mut.column_or_supercolumn = new ColumnOrSuperColumn
            mut.column_or_supercolumn.column = col
            return mut
        }

        override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, ByteBuffer, List[Mutation]]#Context) = {
            // build mutations for the key
            val mutations = new java.util.ArrayList[Mutation]
            for (value <- values)
                mutations.add(mutation(value))
            context.write(wrap(key), mutations)
        }
    }

    override def run(args: Array[String]): Int = {
        val job = new Job(getConf(), "summarize")
        job.setJarByClass(classOf[Summarize])
        job.setReducerClass(classOf[MutationReducer])
        // TODO: Generic parameters depend on input
        job.setOutputKeyClass(classOf[ByteBuffer])
        job.setOutputValueClass(classOf[List[Mutation]])
        job.setOutputFormatClass(classOf[ColumnFamilyOutputFormat])

        // TODO: Target CF for secondary index
        ConfigHelper.setOutputColumnFamily(job.getConfiguration(), KEYSPACE, COLUMN_FAMILY)

        return if (job.waitForCompletion(true)) 0 else 1
    }

    def main(args: Array[String]): Unit = {
        // let ToolRunner handle generic command-line options
        ToolRunner.run(new Configuration, new Summarize, args)
    }
}

