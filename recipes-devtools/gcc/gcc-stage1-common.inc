#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# Shared rewiring for the stage1 shadow ladder:
#
#   gcc-cross-stage1 -> libgcc-stage1-initial -> glibc-stage1
#
# The ladder gives gcc-cross-profdata the target libc headers its
# training compile needs without any path back to the real gcc-cross
# (which DEPENDS on the producer — a real-chain path would be a cycle).
#
# The gcc-family rungs must dodge two couplings to the real gcc-cross:
# 1. the stashed-builddir sstate path is keyed without PN and would
#    collide — fork the producer side and the consumer copy together;
# 2. COMPILERDEP and virtual/cross-cc resolve to the real gcc-cross at
#    parse time — replace them with literal stage1 PNs.
#

GCC_STAGE1_STASH = "${COMPONENTS_DIR}/${BUILD_ARCH}/gcc-stage1-stashed-builddir-${TARGET_SYS}"

# Point the runtime rungs' do_configure at the stage1 compiler's stash
# task instead of the real gcc-cross's (gcc-common.inc hardcodes the
# real PN).
COMPILERDEP = "gcc-cross-stage1-${TARGET_ARCH}:do_gcc_stash_builddir"

# Same-name redefinition of gcc-common.inc's helper: the stash source
# path is hardcoded inside the function body, so there is no variable
# to override.
python extract_stashed_builddir () {
    src = d.getVar("GCC_STAGE1_STASH")
    dest = d.getVar("B")
    oe.path.copyhardlinktree(src, dest)
    staging_processfixme([src + "/fixmepath"], dest, d.getVar("RECIPE_SYSROOT"), d.getVar("RECIPE_SYSROOT_NATIVE"), d)
}
