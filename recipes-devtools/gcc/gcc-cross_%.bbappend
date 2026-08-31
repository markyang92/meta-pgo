#
# Copyright (c) 2026 markyang92
#
# SPDX-License-Identifier: MIT
#

# Consume the .gcda tree produced by gcc-cross-profdata. The producer
# gets its target headers from the stage1 shadow ladder (see
# gcc-stage1-common.inc), never from this recipe, so the DEPENDS is
# acyclic and the profile arrives through the sysroot like any other
# dependency.

GCC_CROSS_LTO_PGO ??= "0"

DEPENDS += "${@bb.utils.contains('GCC_CROSS_LTO_PGO', '1', 'gcc-cross-profdata-${TARGET_ARCH}', '', d)}"

GCC_PGO_USE_FLAGS = "-fprofile-use -fprofile-correction -Wno-missing-profile -flto=auto -ffat-lto-objects"
GCC_PGO_EXTRA_OEMAKE = 'CFLAGS="${BUILD_CFLAGS} ${GCC_PGO_USE_FLAGS}" CXXFLAGS="${BUILD_CXXFLAGS} ${GCC_PGO_USE_FLAGS}" LDFLAGS="${BUILD_LDFLAGS} ${GCC_PGO_USE_FLAGS}"'
EXTRA_OEMAKE += "${@bb.utils.contains('GCC_CROSS_LTO_PGO', '1', '${GCC_PGO_EXTRA_OEMAKE}', '', d)}"

do_compile:prepend () {
	if [ "${GCC_CROSS_LTO_PGO}" = "1" ]; then
		# Replant the producer's .gcda tree into ${B}: gcc looks the
		# counters up by this build's object paths, so matching the
		# relative layout is all that is needed.
		(cd ${STAGING_DATADIR_NATIVE}/gcc-cross-pgo && tar -c .) | tar -x -C ${B}
	fi
}
