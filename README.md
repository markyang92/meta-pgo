# meta-pgo

Profile-guided optimization (PGO) for the compilers in a Yocto build.

## Toolchain

Each compiler gets a producer recipe. It builds an instrumented copy,
runs a training workload and installs the profile.

The original recipe reads the profile from its sysroot. Changes to the
original recipes are kept small.

Timings below are `do_compile` from buildstats on an AMD Ryzen 9 7950X
(16C/32T, `PARALLEL_MAKE = "-j 32"`), MACHINE qemuarm64.

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
