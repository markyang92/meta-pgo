#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# Bottom rung of the stage1 shadow ladder: a plain cross compiler that
# only exists to build glibc-stage1, so the PGO profile producer can
# get target libc headers without touching the real gcc-cross (see
# gcc-stage1-common.inc for the cycle this breaks). DEPENDS needs no
# rewiring: virtual/cross-binutils and linux-libc-headers have no path
# back to gcc-cross.
#

require recipes-devtools/gcc/gcc-cross_${PV}.bb
require gcc-stage1-common.inc

PN = "gcc-cross-stage1-${TARGET_ARCH}"

SUMMARY = "Shadow cross compiler feeding the stage1 PGO training sysroot"

# The real gcc-cross already carries this CVE surface.
CVE_PRODUCT = ""

# The stash normally lands in a shared sstate dir keyed only by
# BUILD_ARCH+TARGET_SYS; fork it so it cannot collide with the real
# gcc-cross's stash (the stage1 rungs read it back through the
# extract_stashed_builddir redefinition in gcc-stage1-common.inc).
do_gcc_stash_builddir[sstate-outputdirs] = "${GCC_STAGE1_STASH}"
do_gcc_stash_builddir[sstate-fixmedir] = "${GCC_STAGE1_STASH}"
