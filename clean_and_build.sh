#!/bin/bash

make clean

make verilog

mill -i "emulator[freechips.rocketchip.system.TestHarness,freechips.rocketchip.system.DefaultConfig].elf"
