#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# Shadow of libgcc-initial for the stage1 ladder (see
# gcc-stage1-common.inc). PN comes from this filename and keeps the
# -initial suffix: staging.bbclass special-cases '*-initial' manifests
# to avoid file overlap in the toplevel sysroot, and the shadow must
# get the same treatment. BPN stays "libgcc" (load-bearing for the
# ${S}/${BPN}/configure paths). nopackages/PACKAGES=""/deltask
# do_build are inherited from libgcc-initial.inc.
#

require recipes-devtools/gcc/libgcc-initial_${PV}.bb
require gcc-stage1-common.inc

SUMMARY = "Shadow libgcc-initial built by gcc-cross-stage1"

# The original's only DEPENDS is virtual/cross-cc, which bitbake
# rewrites at parse time to the real gcc-cross; replace it with the
# stage1 compiler explicitly.
DEPENDS = "gcc-cross-stage1-${TARGET_ARCH}"

# The inherited nativesdk variant would wire itself to gcc-crosssdk
# (COMPILERDEP:class-nativesdk outranks our plain assignment) — the
# very coupling this ladder exists to avoid.
BBCLASSEXTEND = ""
