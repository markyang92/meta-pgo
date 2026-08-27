#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# PGO profile production recipe.
# Builds an instrumented clang with the stage1 clang and collects profile
# data by running the training workload, producing a clang.profdata for
# clang-native and llvm-native to consume.
#

SUMMARY = "PGO profile data for clang/llvm native builds"
HOMEPAGE = "http://llvm.org"
LICENSE = "Apache-2.0-with-LLVM-exception"
SECTION = "devel"

require recipes-devtools/clang/common-clang.inc
require recipes-devtools/clang/common-source.inc

LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=8a15a0759ef07f2682d2ba4b893c9afe"

inherit cmake
inherit_defer native

PR = "r0"

TOOLCHAIN_NATIVE = "clang-stage1"

OECMAKE_SOURCEPATH = "${S}/llvm"

PGO_INSTR_TARGETS ?= "X86;AArch64"
# e.g. -DCLANG_PGO_TRAINING_DATA=<path to a custom lit training suite>
PGO_EXTRA_OECMAKE ?= ""

# llvm_git.bb defaults PACKAGECONFIG to "eh rtti", so the consumers build
# with exceptions/RTTI enabled. Unless EH/RTTI are enabled here as well,
# the CFG hashes diverge from the consumers and the profiles of most hot
# functions get discarded (hash mismatch), losing much of the PGO gain.
EXTRA_OECMAKE = "\
    -DCMAKE_BUILD_TYPE=Release \
    -DLLVM_BUILD_INSTRUMENTED=IR \
    -DLLVM_VP_COUNTERS_PER_SITE=8 \
    -DLLVM_ENABLE_EH=ON \
    -DLLVM_ENABLE_RTTI=ON \
    -DLLVM_ENABLE_PROJECTS=clang \
    -DLLVM_TARGETS_TO_BUILD='${PGO_INSTR_TARGETS}' \
    -DLLVM_PROFDATA=${CLANG_STAGE1_BIN}/llvm-profdata \
    -DLLVM_EXTERNAL_LIT=${S}/llvm/utils/lit/lit.py \
    -DLLVM_INCLUDE_TESTS=OFF \
    -DLLVM_INCLUDE_EXAMPLES=OFF \
    -DLLVM_INCLUDE_BENCHMARKS=OFF \
    -DLLVM_ENABLE_ZLIB=OFF \
    -DLLVM_ENABLE_ZSTD=OFF \
    -DLLVM_ENABLE_LIBXML2=OFF \
    -DLLVM_ENABLE_LIBEDIT=OFF \
    ${PGO_EXTRA_OECMAKE} \
"

OECMAKE_TARGET_COMPILE = "generate-profdata"

PGO_PROFDATA = "${B}/tools/clang/utils/perf-training/clang.profdata"

do_install() {
    [ -s ${PGO_PROFDATA} ] || bbfatal "generate-profdata produced no output: ${PGO_PROFDATA}"

    # Consumers read it from ${STAGING_DATADIR_NATIVE}/clang-pgo/clang.profdata
    install -D -m644 ${PGO_PROFDATA} ${D}${datadir}/clang-pgo/clang.profdata
}
