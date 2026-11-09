# meta-pgo

Profile-guided optimization (PGO) for the compilers in a Yocto build.

## Toolchain

Each compiler gets a producer recipe. It builds an instrumented copy,
runs a training workload and installs the profile.

The original recipe reads the profile from its sysroot. Changes to the
original recipes are kept small.

Timings below are `do_compile` from buildstats on an AMD Ryzen 9 7950X
(16C/32T, `PARALLEL_MAKE = "-j 32"`), MACHINE qemuarm64.

### gcc-cross

PGO and LTO for gcc-cross.

```
local.conf

GCC_CROSS_LTO_PGO = "1"
```

```
gcc-cross-stage1 -> libgcc-stage1-initial -> glibc-stage1   training sysroot
        |
gcc-cross-profdata      instrumented gcc compiles sqlite3.c -> .gcda tree
        |
gcc-cross               rebuilt with -fprofile-use -flto=auto
```

gcc's `profiledbootstrap` only works for native compilers. The training
therefore runs in a separate recipe.

The instrumented compiler needs a target sysroot to compile anything.
gcc-cross is built before glibc, so that sysroot does not exist yet.

The stage1 recipes build a small separate gcc and glibc for training.
They do not depend on the real gcc-cross, since that would be a cycle.

The training input is the sqlite3 amalgamation: one large C file that
needs only libc headers and builds with any gcc version.

It is public domain and ships in the layer. The recipe shares gcc's
work-shared source, so it cannot fetch it.

| gcc-cross          | sqlite3 do_compile | linux-yocto 6.18 do_compile |
|--------------------|--------------------|-----------------------------|
| oe-core default    | 32.5s              | 66.2s                       |
| GCC_CROSS_LTO_PGO  | 28.8s              | 61.4s                       |

### clang-native, llvm-native

PGO and ThinLTO for clang-native and llvm-native.

```
local.conf

PACKAGECONFIG:append:pn-clang-native = " thin-lto-pgo"
PACKAGECONFIG:append:pn-llvm-native = " thin-lto-pgo"
```

```
clang-stage1-native      plain clang/lld, built with gcc
        |
clang-profdata-native    instrumented clang + training -> clang.profdata
        |
llvm-native              Release + PGO + ThinLTO
clang-native             Release + PGO + ThinLTO
```

sqlite3 compiled with `PREFERRED_TOOLCHAIN_TARGET = "clang"`:

| clang-native, llvm-native | CMAKE_BUILD_TYPE | sqlite3 do_compile |
|---------------------------|------------------|--------------------|
| oe-core                   | MinSizeRel       | 37.93s             |
| oe-core                   | Release          | 24.06s             |
| thin-lto-pgo              | MinSizeRel       | 18.77s             |
| thin-lto-pgo              | Release          | 18.29s             |

oe-core master builds clang with Release and llvm with MinSizeRel since
[d30524753955](https://git.openembedded.org/openembedded-core/commit/?id=d30524753955adad8a9adf5b6d01f546963dc45b).
The table above predates that change.
