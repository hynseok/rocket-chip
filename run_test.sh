#!/bin/bash

ROCKET_DIR=$(pwd)
RISCV_TEST_DIR="$ROCKET_DIR/../riscv/riscv-tests"
BENCHMARK_DIR="$RISCV_TEST_DIR/benchmarks"
BENCHMARK_BIN_DIR="$RISCV/riscv64-unknown-elf/share/riscv-tests/benchmarks"
EMULATOR="$ROCKET_DIR/out/emulator/freechips.rocketchip.system.TestHarness/freechips.rocketchip.system.DefaultConfig/verilator/elf.dest/emulator"

TEST_NAME=$1
BINARY="$BENCHMARK_BIN_DIR/$TEST_NAME.riscv"

make -C "$BENCHMARK_DIR"
cp "$BENCHMARK_DIR/$TEST_NAME.riscv" "$BINARY"

# $EMULATOR +verbose "$BINARY" 2>&1 | spike-dasm > trace.log
$EMULATOR +verbose "$BINARY" 2> trace.log
