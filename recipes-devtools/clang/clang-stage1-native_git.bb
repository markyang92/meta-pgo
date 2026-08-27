#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

# Stage1 compiler of the PGO chain.
#
# This compiler is used in two places:
#  1) clang-profdata-native
#      Builds the instrumented clang for PGO.
#  2) clang-native, llvm-native
#      When the thin-lto-pgo PACKAGECONFIG is enabled, each is built with
#      the stage1 clang.

SUMMARY = "Stage1 clang/lld for PGO profile production and consumption"
HOMEPAGE = "http://llvm.org"
LICENSE = "Apache-2.0-with-LLVM-exception"
SECTION = "devel"

require recipes-devtools/clang/common-clang.inc
require recipes-devtools/clang/common-source.inc

LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=8a15a0759ef07f2682d2ba4b893c9afe"

inherit cmake
inherit_defer native

PR = "r0"

TOOLCHAIN_NATIVE = "gcc"

DEPENDS = "llvm-tblgen-native"

OECMAKE_SOURCEPATH = "${S}/llvm"

# This clang is a tool dedicated to PGO builds; no optional features are needed.
EXTRA_OECMAKE = "\
    -DCMAKE_BUILD_TYPE=Release \
    -DLLVM_ENABLE_PROJECTS='clang;lld' \
    -DLLVM_ENABLE_RUNTIMES=compiler-rt \
    -DRUNTIMES_CMAKE_ARGS='-DCOMPILER_RT_BUILD_SANITIZERS=OFF;-DCOMPILER_RT_BUILD_XRAY=OFF;-DCOMPILER_RT_BUILD_LIBFUZZER=OFF;-DCOMPILER_RT_BUILD_MEMPROF=OFF;-DCOMPILER_RT_BUILD_CTX_PROFILE=OFF;-DCOMPILER_RT_BUILD_ORC=OFF;-DCOMPILER_RT_BUILD_GWP_ASAN=OFF' \
    -DLLVM_TARGETS_TO_BUILD=X86 \
    -DLLVM_TABLEGEN=${STAGING_BINDIR_NATIVE}/llvm-tblgen \
    -DCLANG_TABLEGEN=${STAGING_BINDIR_NATIVE}/clang-tblgen \
    -DLLVM_INCLUDE_TESTS=OFF \
    -DLLVM_INCLUDE_EXAMPLES=OFF \
    -DLLVM_INCLUDE_BENCHMARKS=OFF \
    -DLLVM_ENABLE_ZLIB=OFF \
    -DLLVM_ENABLE_ZSTD=OFF \
    -DLLVM_ENABLE_LIBXML2=OFF \
    -DLLVM_ENABLE_LIBEDIT=OFF \
"

OECMAKE_TARGET_COMPILE = "clang lld llvm-profdata llvm-ar runtimes"

do_install() {
    # Install outside the standard bindir to avoid clashing with
    # clang-native's own output. The resource dir (which contains the
    # profile runtime) is located relative to the binary at ../lib/clang,
    # so preserve the bin/lib layout.
    install -d ${D}${libdir}/clang-stage1/bin ${D}${libdir}/clang-stage1/lib
    install -m755 ${B}/bin/clang-${MAJOR_VER} ${D}${libdir}/clang-stage1/bin/
    ln -sf clang-${MAJOR_VER} ${D}${libdir}/clang-stage1/bin/clang
    ln -sf clang-${MAJOR_VER} ${D}${libdir}/clang-stage1/bin/clang++
    install -m755 ${B}/bin/lld ${D}${libdir}/clang-stage1/bin/
    ln -sf lld ${D}${libdir}/clang-stage1/bin/ld.lld
    install -m755 ${B}/bin/llvm-profdata ${D}${libdir}/clang-stage1/bin/
    install -m755 ${B}/bin/llvm-ar ${D}${libdir}/clang-stage1/bin/
    ln -sf llvm-ar ${D}${libdir}/clang-stage1/bin/llvm-ranlib
    cp -R --no-dereference --preserve=mode,links ${B}/lib/clang ${D}${libdir}/clang-stage1/lib/
}
