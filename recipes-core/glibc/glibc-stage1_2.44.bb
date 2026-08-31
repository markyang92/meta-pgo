#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# Shadow glibc for the stage1 ladder: populates a target sysroot with
# libc headers/libs for the PGO profile producer's training compiles,
# built by gcc-cross-stage1 so nothing here depends on the real
# gcc-cross (see gcc-stage1-common.inc in recipes-devtools/gcc).
# Follows the in-tree shadow precedent glibc-testsuite_2.44.bb.
#

require recipes-core/glibc/glibc_${PV}.bb

SUMMARY = "Shadow glibc built by the stage1 ladder for PGO training"

# FILESPATH is derived from BPN (glibc-stage1), so the original
# recipe's patches would not be found (precedent: glibc-tests.inc).
FILESEXTRAPATHS:prepend := "${COREBASE}/meta/recipes-core/glibc/glibc:"

# Only the real glibc may provide virtual/libc and friends.
PROVIDES = ""

# virtual/cross-cc would be rewritten at parse time to the real
# gcc-cross, and libgcc-initial is the real chain's rung; repoint both
# at the stage1 ladder. The cycle-safe deps (binutils, kernel headers,
# native tools) stay shared with the real chain.
DEPENDS:remove = "virtual/cross-cc libgcc-initial"
DEPENDS += "gcc-cross-stage1-${TARGET_ARCH} libgcc-stage1-initial"

# The locale stash lands in a shared sstate dir keyed without PN --
# keeping the task would collide with the real glibc's stash
# (precedent: glibc-testsuite).
deltask do_stash_locale

# Sysroot consumers only: no packages, no SPDX (do_create_spdx would
# register the literal package names, colliding with the real glibc's),
# no world builds, no nativesdk variant, and no duplicate CVE reports
# for the same source.
inherit nopackages nospdx
EXCLUDE_FROM_WORLD = "1"
BBCLASSEXTEND = ""
CVE_PRODUCT = ""

# mirrors.bbclass enables shallow clones only for pn-glibc.
BB_GIT_SHALLOW = "1"
