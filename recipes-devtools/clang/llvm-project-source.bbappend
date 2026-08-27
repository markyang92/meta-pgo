#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

# 0042: Propagate OE sysroot flags to the training sub-builds.
# 0044: Move perf-training out of CLANG_INCLUDE_TESTS -- merged upstream
#       as PR #192163; drop when clang moves to 23.

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://0042-perf-training-Propagate-OE-sysroot-flags-to-sub-buil.patch \
    file://0044-Move-perf-training-out-of-CLANG_INCLUDE_TESTS.patch \
"
