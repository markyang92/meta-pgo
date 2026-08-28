#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# PACKAGECONFIG:append:pn-clang-native = " thin-lto-pgo"
# PACKAGECONFIG:append:pn-llvm-native = " thin-lto-pgo"
#
PACKAGECONFIG[thin-lto-pgo] = "-DLLVM_PROFDATA_FILE=${STAGING_DATADIR_NATIVE}/clang-pgo/clang.profdata -DLLVM_ENABLE_LTO=Thin -DLLVM_USE_LINKER=lld -DCMAKE_BUILD_TYPE=Release,,clang-profdata-native"

PACKAGECONFIG:remove:class-target = "thin-lto-pgo"

TOOLCHAIN_NATIVE = "${@bb.utils.contains('PACKAGECONFIG', 'thin-lto-pgo', 'clang-stage1', '${PREFERRED_TOOLCHAIN_NATIVE}', d)}"
TOOLCHAIN:class-native = "${@bb.utils.contains('PACKAGECONFIG', 'thin-lto-pgo', 'clang', '${PREFERRED_TOOLCHAIN_NATIVE}', d)}"
