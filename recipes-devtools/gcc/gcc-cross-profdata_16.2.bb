#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

#
# PGO profile producer for gcc-cross: instrumented host tools compile
# a real-world training workload; the .gcda tree reaches the real
# gcc-cross through the sysroot. gcc's own profiledbootstrap is
# native-only.
#
# The workload is C only, so glibc-stage1's libc headers are the whole
# training sysroot — no path back to the real gcc-cross (keeps its
# DEPENDS on this recipe acyclic, see gcc-stage1-common.inc). The C++
# front end is untrained; the shared middle end/backend still carry
# the benefit.
#

require recipes-devtools/gcc/gcc-cross_${PV}.bb

PN = "gcc-cross-profdata-${TARGET_ARCH}"

SUMMARY = "PGO profile data (gcda tree) for gcc-cross, real-workload trained"

# The real gcc-cross already carries this CVE surface.
CVE_PRODUCT = ""

# libc headers for the training compile.
DEPENDS += "glibc-stage1"

# The stash sstate dir is keyed without PN — would collide with the
# real gcc-cross's stash.
deltask do_gcc_stash_builddir

# Carried in the layer: work-shared deltasks unpack, so SRC_URI is dead here.
GCC_PGO_TRAINING := "${THISDIR}/gcc-cross-profdata"
do_compile[file-checksums] += "${GCC_PGO_TRAINING}/sqlite3-3.53.4.c:True"

do_compile () {
	export CC="${BUILD_CC}"

	oe_runmake all-host \
		CFLAGS="${BUILD_CFLAGS} -fprofile-generate" \
		CXXFLAGS="${BUILD_CXXFLAGS} -fprofile-generate" \
		LDFLAGS="${BUILD_LDFLAGS} -fprofile-generate"

	# If the workload fails to compile the training sysroot is broken — fatal.
	${B}/gcc/xgcc -B${B}/gcc/ --sysroot=${STAGING_DIR_TARGET} \
		-O2 -c ${GCC_PGO_TRAINING}/sqlite3-3.53.4.c -o ${B}/pgo-train-out.o || \
		bbfatal "training compile failed (sqlite3.c)"
	bbnote "training: sqlite3.c compiled"
}

do_install () {
	# The ${B}-relative .gcda tree is the product; the consumer
	# replants it into its own identical ${B} layout.
	install -d ${D}${datadir}/gcc-cross-pgo
	find . -name '*.gcda' | tar -c -T - | tar -x -C ${D}${datadir}/gcc-cross-pgo
	# An empty tree = silent PGO no-op downstream
	# (-Wno-missing-profile); refuse it.
	n=$(find ${D}${datadir}/gcc-cross-pgo -name '*.gcda' | wc -l)
	[ $n -gt 100 ] || bbfatal "training produced almost no counters ($n gcda)"
}
