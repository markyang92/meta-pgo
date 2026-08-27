#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# Native toolchain class.
# Cannot be used as a target toolchain.
#
# Pulled in by base.bbclass via inherit_defer toolchain/${TOOLCHAIN_NATIVE}-native.
# Selected by setting TOOLCHAIN_NATIVE = "clang-stage1".
#

CLANG_STAGE1_BIN = "${STAGING_LIBDIR_NATIVE}/clang-stage1/bin"

BUILD_CC = "${CCACHE}${CLANG_STAGE1_BIN}/clang ${BUILD_CC_ARCH}"
BUILD_CXX = "${CCACHE}${CLANG_STAGE1_BIN}/clang++ ${BUILD_CC_ARCH}"
BUILD_FC = "${BUILD_PREFIX}gfortran ${BUILD_CC_ARCH}"
BUILD_CPP = "${CLANG_STAGE1_BIN}/clang ${BUILD_CC_ARCH} -E"
BUILD_LD = "${BUILD_PREFIX}ld ${BUILD_LD_ARCH}"
BUILD_CCLD = "${CLANG_STAGE1_BIN}/clang ${BUILD_CC_ARCH}"
BUILD_AR = "${CLANG_STAGE1_BIN}/llvm-ar"
BUILD_AS = "${BUILD_PREFIX}as ${BUILD_AS_ARCH}"
BUILD_RANLIB = "${CLANG_STAGE1_BIN}/llvm-ranlib -D"
BUILD_STRIP = "${BUILD_PREFIX}strip"
BUILD_OBJCOPY = "${BUILD_PREFIX}objcopy"
BUILD_OBJDUMP = "${BUILD_PREFIX}objdump"
BUILD_NM = "${BUILD_PREFIX}nm"
BUILD_READELF = "${BUILD_PREFIX}readelf"

DEPENDS += "clang-stage1-native"
