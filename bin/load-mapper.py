#!/usr/bin/env python

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from avro.io import BinaryEncoder, DatumWriter
import avro.protocol
import yaml
import sys,time

COSC = 'column_or_supercolumn'

# parse the current avro schema
proto = avro.protocol.parse(open('cassandra.avpr').read())
schema = proto.types_dict['StreamingMutation']
# open an avro encoder and writer for stdout
enc = BinaryEncoder(sys.stdout)
writer = DatumWriter(schema)

def column(name, value):
    column = dict()
    column['name'] = '%s' % name
    column['value'] = '%s' % value
    column['clock'] = {'timestamp': long(time.time() * 1e6)}
    column['ttl'] = 0
    return column

# parse top level yaml records and output a series of objects matching
# 'StreamingMutation' in the Avro interface
mutation = dict()
mutation['mutation'] = {COSC: {'column': None}}
try:
    iter = yaml.parse(sys.stdin)
    while True:
        event = iter.next()
        if isinstance(event, yaml.ScalarEvent):
            # scalars mark the beginnings of rows or columns
            scalar = event.value.encode()
            event = iter.next()
            if isinstance(event, yaml.MappingStartEvent):
                # new row
                mutation['key'] = scalar.encode()
            else:
                # new column
                value = event.value.encode()
                mutation['mutation'][COSC]['column'] = column(scalar, value)
                # flush the mutation
                writer.write(mutation, enc)
except StopIteration:
    pass
finally:
    sys.stdout.flush()

