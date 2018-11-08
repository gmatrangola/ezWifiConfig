#!/bin/sh

#  GenerateProto.sh
#  ProtoBLEiOS
#
#  Created by Geoffrey Matrangola on 8/24/18.

CODEGEN_DIR=$HOME/tools/codegen-1.0-SNAPSHOT
protoc --proto_path=$CODEGEN_DIR/proto/ --proto_path=Proto --plugin=protoc-gen-protoble=$CODEGEN_DIR/bin/SwiftClientPlugin --swift_out=Generated --protoble_out=Generated WifiConfig.proto
