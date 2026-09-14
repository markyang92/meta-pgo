#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

# 0042: Propagate OE sysroot flags to the training sub-builds.

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI += " \
    file://0042-perf-training-Propagate-OE-sysroot-flags-to-sub-buil.patch \
"
